package pl.lodz.p.it.eduvirt.executor.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ovirt.engine.sdk4.types.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
import pl.lodz.p.it.eduvirt.repository.ResourceGroupNetworkRepository;
import pl.lodz.p.it.eduvirt.service.OVirtAssignedPermissionService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.VnicProfilePoolService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//TODO michal: handling stateful pods

@Slf4j
@Service
@LoggerInterceptor
@RequiredArgsConstructor
//@Transactional
public class ExecutorScheduler {

    private final OVirtVmService oVirtVmService;
    private final OVirtAssignedPermissionService ovirtAssignedPermissionService;
    private final VnicProfilePoolService vnicProfilePoolService;

    //TODO michal: service instead of repository
    private final ReservationRepository reservationRepository;
    private final ResourceGroupNetworkRepository resourceGroupNetworkRepository;

    private final ExecutorTaskService executorTaskService;

    //TODO michal: real pooling instead of invoking checking conditions in fixed time

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = 0)
    @Transactional
    public void createPods() {
        reservationRepository.findReservationsToBegin()
                .forEach(this::startUpPod);
    }

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = 0)
    @Transactional
    public void destroyPods() {
        reservationRepository.findReservationsToFinish()
                .forEach(this::stopPod);
    }

    //--------------PRIVATE METHODS--------------

    private void startUpPod(Reservation reservation) {
        ExecutorTask executorTask = executorTaskService.registerPodInitTask(reservation);
        try {
            ResourceGroup resourceGroup = reservation.getResourceGroup();
            Team team = reservation.getTeam();
            List<VirtualMachine> virtualMachines = resourceGroup.getVms();

            //TODO michal: Verify resources or handle insufficient on VM startup command (probably the first one)

            //Network mapping
            List<ResourceGroupNetwork> networksToMap = resourceGroupNetworkRepository.getAllByResourceGroupId(resourceGroup.getId());
            networksToMap
                    //.stream().parallel()
                    .forEach(
                            network -> {
                                // TODO michal: CZY SEGMENTY SIECIOWE SĄ DEFINIOWANE PER CLUSTER? CZY DLA CAŁEGO DATA CENTER SĄ WSPÓLNE
                                // TODO michal: rejestrowanie wybierania vnic profilu z puli?
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
                                        //.stream().parallel()
                                        .forEach(
                                                nic -> {
                                                    UUID vmId = nic.getVirtualMachine().getId();
                                                    runAndRegister(
                                                            () -> assignVnicProfileToNIC(chosenVnicProfile.getId(), vmId, nic.getId()),
                                                            executorTask, vmId, ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE
                                                    );
                                                }
                                        );
                            }
                    );


            //TODO michal: Handle situation when some VMs are running

            //Start-up VMs
            if (reservation.getAutomaticStartup()) {
                virtualMachines
                        //.stream().parallel()
                        .forEach(
                                vm -> runAndRegister(() -> oVirtVmService.runVm(vm.getId().toString()),
                                        executorTask, vm.getId(), ExecutorSubtask.SubtaskType.START_VM
                                )
                        );
            }

            //TODO michal: Active waiting for all VMs are running
            //Assign permissions
            virtualMachines
                    .stream()
                    .filter(vm -> !vm.isHidden())
                    //.parallel()
                    .forEach(
                            vm -> runAndRegister(() -> addTeamPermissionsToVm(vm.getId(), team.getUsers()),
                                    executorTask, vm.getId(), ExecutorSubtask.SubtaskType.ASSIGN_PERMISSIONS
                            )
                    );

            executorTaskService.finalizeTask(executorTask.getId(), true, null);
        } catch (Throwable e) {
            executorTaskService.finalizeTask(executorTask.getId(), false, e.getMessage());
            throw e;
        }
    }

    private void assignVnicProfileToNIC(UUID vnicProfileId, UUID vmId, UUID vmNicId) {
        oVirtVmService.assignVnicProfileToVm(
                vmId.toString(),
                vmNicId.toString(),
                vnicProfileId.toString()
        );
    }

    //OLD_IMPL
//    private void assignVnicProfilesToNICs(UUID vnicProfileId, List<UUID> vmNicIdsList) {
//        vmNicIdsList.stream()
////                .parallel()
//                .forEach(
//                        vmNicId ->
//                                oVirtVmService.assignVnicProfileToVm(
//                                        vmNicMapping.get(vmNicId).toString(),
//                                        vmNicId.toString(),
//                                        vnicProfileId.toString()
//                                )
//                );
//    }

    private void addTeamPermissionsToVm(UUID vmId, List<UUID> teamMembersIds) {
        teamMembersIds
                //.stream().parallel()
                .forEach(
                        userId -> ovirtAssignedPermissionService.assignPermissionToVmToUser(vmId, userId,
                                "00000000-0000-0000-0001-000000000001")
                );
    }


    //TODO michal: if pod doesnt start should we invoke stopping it??? - now stopping is invoking in any cases
    private void stopPod(Reservation reservation) {
        ExecutorTask executorTask = executorTaskService.registerPodDestroyTask(reservation);
        try {
            ResourceGroup resourceGroup = reservation.getResourceGroup();
            Team team = reservation.getTeam();
            List<VirtualMachine> virtualMachines = resourceGroup.getVms();

            //Revoke permissions
            virtualMachines
                    //.stream().parallel()
                    .forEach(
                            vm -> runAndRegister(() -> revokeTeamPermissionsToVm(vm.getId(), team.getUsers()),
                                    executorTask, vm.getId(), ExecutorSubtask.SubtaskType.REVOKE_PERMISSIONS
                            )
                    );

            //Private networks cleaning
            //TODO michal: decide to do it before or after VMs shutdown
            List<ResourceGroupNetwork> networksToRemove = resourceGroupNetworkRepository.getAllByResourceGroupId(resourceGroup.getId());
            networksToRemove
                    //.stream().parallel()
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
                    //.stream().parallel()
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
                //.stream().parallel()
                //TODO michal: consider caching permissionIds
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
        return UUID.fromString(
                oVirtVmService.removeVnicProfileFromVm(
                        vmId.toString(),
                        vmNicId.toString()
                )
        );
    }

    //OLD_IMPL
//    private List<UUID> removeVnicProfilesFromNICs(List<UUID> vmNicIdsList) {
//        return vmNicIdsList.stream()
////                .parallel()
//                .map(
//                        //TODO michal: do null-safe mapping
//                        vmNicId ->
//                                UUID.fromString(
//                                        oVirtVmService.removeVnicProfileFromVm(
//                                                vmNicMapping.get(vmNicId),
//                                                vmNicId.toString()
//                                        )
//                                )
//                )
//                .toList();
//    }

    private <T> T runAndRegister(Supplier<T> runnable,
                                 ExecutorTask task,
                                 UUID vmId,
                                 ExecutorSubtask.SubtaskType type) {
        UUID sanitizedVmId = Objects.requireNonNullElse(vmId, UUID.fromString("00000000-0000-0000-0000-000000000000"));
        try {
//            synchronized (this) {
             T tmpVal = runnable.get();
//            }
            executorTaskService.registerSubTask(task.getId(), sanitizedVmId, type, true, null);
            return tmpVal;
        } catch (Throwable e) {
            executorTaskService.registerSubTask(task.getId(), sanitizedVmId, type, false, e.getMessage());
            throw e;
        }
    }

    private void runAndRegister(Runnable runnable,
                                ExecutorTask task,
                                UUID vmId,
                                ExecutorSubtask.SubtaskType type) {
        runAndRegister(
                () -> {
                    runnable.run();
                    return null;
                },
                task,
                vmId,
                type
        );
    }
}
