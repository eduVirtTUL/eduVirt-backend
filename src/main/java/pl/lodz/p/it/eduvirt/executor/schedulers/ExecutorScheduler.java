package pl.lodz.p.it.eduvirt.executor.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ovirt.engine.sdk4.types.User;
import org.ovirt.engine.sdk4.types.Vm;
import org.ovirt.engine.sdk4.types.VmStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupNetwork;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.entity.network.VnicProfilePoolMember;
import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;
import pl.lodz.p.it.eduvirt.executor.service.ExecutorTaskService;
import pl.lodz.p.it.eduvirt.repository.ReservationRepository;
import pl.lodz.p.it.eduvirt.service.OVirtAssignedPermissionService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.VnicProfilePoolService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//IMPROVEMENTS michal: handling stateful pods
//IMPROVEMENTS michal: perhaps improvement -> .stream().parallel() when calling oVirt Api (d871bd94490e9d4f0e7f72e7c4da6b2ac48e5df7 -> last revision with comments where it could be used)
//IMPROVEMENTS michal: IF NETWORK SEGMENTS ARE DEFINED PER CLUSTER OR THEY ARE COMMON IN THE DATA CENTER

//IMPROVEMENTS michal: verifications count/type of registered subtasks
//IMPROVEMENTS michal: check system behavior if system was down for few hours (conflicting reservations to end and start)
//IMPROVEMENTS michal: on start-up check if other students have permissions to these VMs (If they have, reservation should failed)
//IMPROVEMENTS michal: perhaps real pooling instead of invoking checking conditions in fixed time
//IMPROVEMENTS michal: rethink transactions
//IMPROVEMENTS michal: maybe include checking VMs statues in subtasks
//IMPROVEMENTS michal: limit number of retries to create/destroy pod (after reaching this limit, maybe administrators should be informed about problems)
//IMPROVEMENTS michal: maybe implement different exceptions for different statues of VM (that is not in DOWN status)
//IMPROVEMENTS michal: separate assigning/revoking permissions to different scheduled tasks

@Slf4j
@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Profile({"prod", "dev"})
//@Profile({"prod"})
public class ExecutorScheduler {

    // Model
    private final OVirtVmService oVirtVmService;
    private final OVirtAssignedPermissionService ovirtAssignedPermissionService;
    private final VnicProfilePoolService vnicProfilePoolService;

    //TODO michal: perhaps service instead of repository
    private final ReservationRepository reservationRepository;

    // Handling logging
    private final ExecutorTaskService executorTaskService;

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = 0)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createPods() {
        reservationRepository.findReservationsToBegin()
                //.stream().parallel()
                .forEach(this::startUpPod);
    }

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = 0)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void destroyPods() {
        reservationRepository.findReservationsToFinish()
                //.stream().parallel()
                .forEach(this::stopPod);
    }

    //--------------PRIVATE METHODS--------------

    //TODO michal: Start new attempt to start up POD from the last successful subtask

    private void startUpPod(Reservation reservation) {
        ExecutorTask executorTask = executorTaskService.registerPodInitTask(reservation);
        try {
            ResourceGroup resourceGroup = reservation.getResourceGroup();
            Team team = reservation.getTeam();
            List<VirtualMachine> virtualMachines = resourceGroup.getVms();

            //Optimization step to fetch VMs from oVirt and then pass their references
            //to checkIfVmDownStatus() and assignVnicProfileToNIC() (to avoid multi fetching VMs operations)
            Map<UUID, Vm> ovirtVmsIdMap = fetchOvirtVms(virtualMachines);

            // Check if all VMs are down
            checkIfVmsDownStatus(new ArrayList<>(ovirtVmsIdMap.values()));

            //TODO michal: Verify resources or handle insufficient on VM startup command (probably the first one)

            //Network mapping
            List<ResourceGroupNetwork> networksToMap = resourceGroup.getNetworks();
            networksToMap
                    .forEach(
                            network -> {
                                // Fetch vnic profile from pool, checking conditions (if inUse equals false)
                                VnicProfilePoolMember chosenVnicProfile = vnicProfilePoolService.getVnicProfilesPool()
                                        .stream()
                                        .filter(vnicProfile -> !vnicProfile.getInUse())
                                        .findFirst()
                                        .orElseThrow(() -> new RuntimeException("No available vnic profile found in pool"));

                                // Set vnic profile's property "inUse" to true
                                vnicProfilePoolService.markVnicProfileAsOccupied(chosenVnicProfile.getId());

                                // Assign vnic profile to VMs NICs
                                network.getInterfaces()
                                        .forEach(
                                                nic -> {
                                                    Vm vm = Optional.ofNullable(ovirtVmsIdMap.get(nic.getVirtualMachine().getId()))
                                                            .orElseThrow(() -> new RuntimeException("VM not found"));
                                                    runAndRegister(
                                                            () -> assignVnicProfileToNIC(chosenVnicProfile.getId(), vm, nic.getId()),
                                                            executorTask, UUID.fromString(vm.id()), ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE, chosenVnicProfile.getId()
                                                    );
                                                }
                                        );
                            }
                    );

            //Start-up VMs
            if (reservation.getAutomaticStartup()) {
                virtualMachines
                        .forEach(
                                vm -> runAndRegister(() -> oVirtVmService.runVm(vm.getId().toString()),
                                        executorTask, vm.getId(), ExecutorSubtask.SubtaskType.START_VM
                                )
                        );
            }

            //TODO michal: Active waiting for all VMs are running ???

            //Assign permissions
            virtualMachines
                    .stream()
                    .filter(vm -> !vm.isHidden())
                    .forEach(
                            vm -> runAndRegister(() -> addTeamPermissionsToVm(vm.getId(), team.getUsers()),
                                    executorTask, vm.getId(), ExecutorSubtask.SubtaskType.ASSIGN_PERMISSION
                            )
                    );

            executorTaskService.finalizeTask(executorTask.getId(), true, null);
        } catch (Throwable e) {
            executorTaskService.finalizeTask(executorTask.getId(), false, e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private Map<UUID, Vm> fetchOvirtVms(List<VirtualMachine> virtualMachines) {
        Set<String> vmIdsStr = virtualMachines.stream()
                .map(vm -> vm.getId().toString())
                .collect(Collectors.toSet());
        return oVirtVmService.findVmsWithNicsByVmIds(vmIdsStr)
                .stream()
                .collect(Collectors.toMap(vm -> UUID.fromString(vm.id()), vm -> vm));
    }

    private void checkIfVmsDownStatus(List<Vm> vms) {
        String invalidStatusesConcString = vms.stream()
                .filter(vm -> !vm.status().equals(VmStatus.DOWN))
                .map(vm -> "VM %s in %s status".formatted(vm.name(), vm.status().name()))
                .collect(Collectors.joining(";"));

        if (!invalidStatusesConcString.isEmpty()) {
            String errorMessage = "Some VMs are in invalid statuses: " + invalidStatusesConcString;
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    private void assignVnicProfileToNIC(UUID vnicProfileId, Vm vm, UUID vmNicId) {
        oVirtVmService.assignVnicProfileToVm(
                vm,
                vmNicId.toString(),
                vnicProfileId.toString()
        );
    }

    private void addTeamPermissionsToVm(UUID vmId, List<UUID> teamMembersIds) {
        teamMembersIds
                .forEach(
                        userId -> ovirtAssignedPermissionService.assignPermissionToVmToUser(vmId, userId,
                                "00000000-0000-0000-0001-000000000001")
                );
    }


    //TODO michal: Start new attempt to stop POD from the last successful subtask

    //TODO michal: if pod doesnt start should we invoke stopping it??? - now stopping is invoking in any cases
    private void stopPod(Reservation reservation) {
        ExecutorTask executorTask = executorTaskService.registerPodDestroyTask(reservation);
        try {
            ResourceGroup resourceGroup = reservation.getResourceGroup();
            Team team = reservation.getTeam();
            List<VirtualMachine> virtualMachines = resourceGroup.getVms();

            //Revoke permissions
            virtualMachines
                    .forEach(
                            vm -> runAndRegister(() -> revokeTeamPermissionsToVm(vm.getId(), team.getUsers()),
                                    executorTask, vm.getId(), ExecutorSubtask.SubtaskType.REVOKE_PERMISSION
                            )
                    );

            //Private networks cleaning
            List<ResourceGroupNetwork> networksToRemove = resourceGroup.getNetworks();
            networksToRemove
                    .forEach(
                            network -> {
                                // Remove vnic profile from VMs NICs
                                Set<UUID> removedVnicProfilesIdsSet = network.getInterfaces()
                                        .stream()
//                                        .parallel()
                                        .map(
                                                nic -> {
                                                    UUID vmId = nic.getVirtualMachine().getId();
                                                    return runAndRegister(
                                                            () -> removeVnicProfileFromNIC(vmId, nic.getId()),
                                                            executorTask, vmId, ExecutorSubtask.SubtaskType.REMOVE_VNIC_PROFILE
                                                    );
                                                }
                                        )
                                        .collect(Collectors.toSet());

                                // Set vnic profile's property "inUse" to false
                                removedVnicProfilesIdsSet.forEach(vnicProfilePoolService::markVnicProfileAsFree);
                            }
                    );

            //Shutdown VMs
            virtualMachines
                    .forEach(
                            vm -> runAndRegister(() -> oVirtVmService.shutdownVm(vm.getId().toString()),
                                    executorTask, vm.getId(), ExecutorSubtask.SubtaskType.SHUTDOWN_VM
                            )
                    );

            //TODO michal: when waiting to vm shutdown for a long time, use power off

            executorTaskService.finalizeTask(executorTask.getId(), true, null);
        } catch (Throwable e) {
            executorTaskService.finalizeTask(executorTask.getId(), false, e.getMessage());
            throw e;
        }
    }

    private void revokeTeamPermissionsToVm(UUID vmId, List<UUID> teamMembersIds) {
        teamMembersIds
                .forEach(
                        userId ->
                                ovirtAssignedPermissionService.findPermissionsByVmId(vmId)
                                        .forEach(permission -> {
                                            User userOpt = permission.user();
                                            if (Objects.nonNull(userOpt) && userOpt.id().equals(userId.toString())) {
                                                ovirtAssignedPermissionService.revokePermissionToVmFromUser(
                                                        UUID.fromString(permission.id())
                                                );
                                            }
                                        })
                );
    }

    private UUID removeVnicProfileFromNIC(UUID vmId, UUID vmNicId) {
        return Optional.ofNullable(
                oVirtVmService.removeVnicProfileFromVm(vmId.toString(), vmNicId.toString())
        ).map(UUID::fromString).orElse(null);
    }

    /* Registering subtasks methods */

    private <T> T runAndRegister(Supplier<T> supplier, ExecutorTask task, UUID vmId, ExecutorSubtask.SubtaskType type,
                                 UUID additionalId) {
        UUID sanitizedVmId = Objects.requireNonNullElse(vmId, UUID.fromString("00000000-0000-0000-0000-000000000000"));
        try {
            T tmpVal = supplier.get();
            if (Objects.isNull(additionalId) && tmpVal instanceof UUID) {
                additionalId = (UUID) tmpVal;
            }
            executorTaskService.registerSubTask(task.getId(), sanitizedVmId, type, true, null, additionalId);
            return tmpVal;
        } catch (Throwable e) {
            executorTaskService.registerSubTask(task.getId(), sanitizedVmId, type, false, e.getMessage(), additionalId);
            throw e;
        }
    }

    private <T> T runAndRegister(Supplier<T> supplier, ExecutorTask task, UUID vmId, ExecutorSubtask.SubtaskType type) {
        return runAndRegister(supplier, task, vmId, type, null);
    }

    private void runAndRegister(Runnable runnable, ExecutorTask task, UUID vmId, ExecutorSubtask.SubtaskType type,
                                UUID additionalId) {
        Supplier<?> castedSupplier = () -> {
            runnable.run();
            return null;
        };
        runAndRegister(castedSupplier, task, vmId, type, additionalId);
    }

    private void runAndRegister(Runnable runnable, ExecutorTask task, UUID vmId, ExecutorSubtask.SubtaskType type) {
        runAndRegister(runnable, task, vmId, type, null);
    }
}
