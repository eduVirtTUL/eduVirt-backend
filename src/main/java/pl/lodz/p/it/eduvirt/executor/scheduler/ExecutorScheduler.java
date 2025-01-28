package pl.lodz.p.it.eduvirt.executor.scheduler;

import jakarta.annotation.PostConstruct;
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
import pl.lodz.p.it.eduvirt.entity.NetworkInterface;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupNetwork;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
import pl.lodz.p.it.eduvirt.executor.exception.NoAvailableVnicProfileException;
import pl.lodz.p.it.eduvirt.executor.exception.ResourceGroupCurrentlyInUseException;
import pl.lodz.p.it.eduvirt.executor.exception.VmInvalidStatusException;
import pl.lodz.p.it.eduvirt.executor.exception.VmLaunchingStatusException;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorTask;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.subtasks.AdditionalId;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.subtasks.VnicProfileTask;
import pl.lodz.p.it.eduvirt.executor.service.ExecutorTaskService;
import pl.lodz.p.it.eduvirt.executor.service.MailNotificationService;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtPermissionService;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.ReservationService;
import pl.lodz.p.it.eduvirt.service.VnicProfilePoolService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// Check for final version
//TO_CHECK michal: improvements for transactions
//TO_CHECK michal: LoggerInterceptor on other services

// Priority 1
//TO_IMPROVE michal: handle task that in IN_PROGRESS status for a long time (timeouts??????????)
//TO_IMPROVE michal: send mail notification to administrators after multiply restarts of POD

// Priority 2
//TODO_OPTIONAL michal: Specified Persistence Unit for exception module (separate connection pool)

@Slf4j
@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Profile({"prod", "dev"})
@Transactional(propagation = Propagation.NEVER)
public class ExecutorScheduler {

    /* Model */
    private final OVirtVmService oVirtVmService;
    private final OVirtPermissionService OVirtPermissionService;
    private final VnicProfilePoolService vnicProfilePoolService;
    private final ReservationService reservationService;

    /* Handling logging */
    private final ExecutorTaskService executorTaskService;

    /* Mail notifications */
    private final MailNotificationService mailNotificationService;

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = 0L)
    @Transactional(propagation = Propagation.NEVER)
    public void createPods() {
        reservationService.findReservationsToBegin()
                .forEach(
                        reservation -> {
                            try {
                                startUpPod(reservation);
                            } catch (Throwable e) {
                                log.error(
                                        "An error occurred during the task of creating the POD for reservation {}" +
                                                " ~ exception: {}: {} ",
                                        reservation.getId().toString(), e.getClass().getName(), e.getMessage()
                                );
                            }
                        }
                );
    }

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = 0L)
    @Transactional(propagation = Propagation.NEVER)
    public void destroyPods() {
        reservationService.findReservationsToStop()
                .forEach(
                        reservation -> {
                            try {
                                stopPod(reservation);
                            } catch (Throwable e) {
                                log.error(
                                        "An error occurred during the task of destroying the POD for reservation {}" +
                                                " ~ exception: {}: {} ",
                                        reservation.getId().toString(), e.getClass().getName(), e.getMessage()
                                );
                            }
                        }
                );
    }

    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = 0)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void endReservations() {
        executorTaskService.getReservationsToEndTasks()
                .forEach(
                        task -> {
                            try {
                                finalizePodReservation(task);
                            } catch (Throwable e) {
                                log.error(
                                        "An error occurred during the task of ending the reservation {}" +
                                                " ~ exception: {}: {} ",
                                        task.getReservation().getId().toString(), e.getClass().getName(), e.getMessage()
                                );
                            }
                        }
                );
    }

    /* Aggregated operations methods */
    private void startUpPod(Reservation reservation) {
        ExecutorTask executorTask = executorTaskService.registerPodInitTask(reservation);
        List<ExecutorSubtask> existingSubtasks = executorTaskService.getReservationStartExistingSubTasks(reservation);

        try {
            // Mark reservation as started (unless its processing has already begun)
            if (reservation.getStatus() != Reservation.ReservationStatus.IN_PROGRESS) {
                reservationService.startReservation(reservation);
            }

            ResourceGroup resourceGroup = reservation.getResourceGroup();
            Team team = reservation.getTeam();
            List<VirtualMachine> originalVms = new ArrayList<>(resourceGroup.getVms());

            CHECK_CONDITION_ZONE:
            {
                //todo maybe do not repeat it after first successful check

                // Check if the RG is not used by another RG
                runAndRegister(
                        () -> checkIfRgIsInUse(reservation.getId(), resourceGroup),
                        executorTask, null, ExecutorSubtask.SubtaskType.CHECK_RG_IN_USE
                );

                // Check if VMs statuses were checked
                Predicate<ExecutorSubtask> predicateVmsStatuses = st ->
                        st.getType().equals(ExecutorSubtask.SubtaskType.CHECK_VMS_STATUSES) && st.getSuccessful();
                if (existingSubtasks.stream().anyMatch(predicateVmsStatuses)) {
                    break CHECK_CONDITION_ZONE;
                }

                // Filter properly started VMs
                List<VirtualMachine> filteredVmsToCheck = filterVmsBySubtasks(
                        existingSubtasks,
                        originalVms,
                        ExecutorSubtask.SubtaskType.START_VM,
                        true
                );
                List<Vm> ovirtVms = fetchOvirtVms(filteredVmsToCheck);

                // Check if all VMs are down
                runAndRegister(
                        () -> checkIfVmsDownStatus(ovirtVms),
                        executorTask, null, ExecutorSubtask.SubtaskType.CHECK_VMS_STATUSES
                );
            }

            MAP_PRIVATE_SEGMENTS_ZONE:
            {
                Predicate<ExecutorSubtask> predicate = st ->
                        st.getType().equals(ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE) && st.getSuccessful();
                // Map<K, V> -> K: nicId, V: vnicProfileId
                Map<UUID, UUID> nicsIdsToExclude = existingSubtasks.stream()
                        .filter(predicate)
                        .collect(Collectors.toMap(
                                st -> ((VnicProfileTask) st).getNicId(),
                                st -> ((VnicProfileTask) st).getVnicProfileId()
                        ));

                // Network mapping
                List<ResourceGroupNetwork> networksToMap = resourceGroup.getNetworks();
                networksToMap
                        .forEach(
                                network -> {
                                    // Filter already assigned NICs
                                    List<NetworkInterface> interfaces = network.getInterfaces();
                                    int numOfInterfacesBeforeFiltering = interfaces.size();

                                    UUID[] previousVnicProfileId = new UUID[1];
                                    interfaces.removeIf(nic -> {
                                        if (nicsIdsToExclude.containsKey(nic.getId())) {
                                            previousVnicProfileId[0] = nicsIdsToExclude.get(nic.getId());
                                            return true;
                                        }
                                        return false;
                                    });
                                    int numOfInterfacesAfterFiltering = interfaces.size();

                                    UUID chosenVnicProfileId;
                                    if (interfaces.isEmpty()) {
                                        return;
                                    } else if (numOfInterfacesBeforeFiltering > numOfInterfacesAfterFiltering) {
                                        chosenVnicProfileId = previousVnicProfileId[0];
                                    } else {
                                        // Fetch vnic profile from pool,
                                        // checking conditions (if inUse equals false and vnic profile is present in the oVirt)
                                        chosenVnicProfileId = vnicProfilePoolService.getFirstFreeVnicProfileFromPool()
                                                .orElseThrow(NoAvailableVnicProfileException::new)
                                                .getId();

                                        // Set vnic profile's property "inUse" to true
                                        vnicProfilePoolService.markVnicProfileAsOccupied(chosenVnicProfileId);
                                    }

                                    //TODO_OPTIONAL michal: potentially if error occurs on the first nic, in the next iteration
                                    // will be choose the new one vnic profile from pool (and the previous one will be
                                    // marked as occupied without assigning to any NIC - resource blocking)

                                    // Assign vnic profile to VMs NICs
                                    interfaces
                                            .forEach(
                                                    nic -> {
                                                        UUID vmId = nic.getVirtualMachine().getId();
                                                        runAndRegister(
                                                                () -> assignVnicProfileToNIC(chosenVnicProfileId, vmId, nic.getId()),
                                                                executorTask, vmId, ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE,
                                                                AdditionalId.VNIC_PROFILE.withId(chosenVnicProfileId),
                                                                AdditionalId.NIC.withId(nic.getId())
                                                        );
                                                    }
                                            );
                                }
                        );
            }


            START_VMS_ZONE:
            {
                // Start-up VMs
                if (reservation.getAutomaticStartup()) {
                    // Filter VMs for which an attempt was made to launch
                    List<VirtualMachine> filteredVmsToStart = filterVmsBySubtasks(
                            existingSubtasks,
                            originalVms,
                            ExecutorSubtask.SubtaskType.START_VM,
                            false
                    );

                    filteredVmsToStart
                            .forEach(
                                    vm -> {
                                        try {
                                            runAndRegister(
                                                    () -> oVirtVmService.runVm(vm.getId().toString()),
                                                    executorTask, vm.getId(), ExecutorSubtask.SubtaskType.START_VM
                                            );
                                        } catch (Throwable nestedException) {
                                            // If the exception was related to a call to oVirt's API is intentionally
                                            // swallowed, so as not to interrupt the exception algorithm
                                            // (this is conditioned on one attempt to run the VM,
                                            // next attempts can be made by students manually)
                                            if (nestedException.getCause() instanceof org.ovirt.engine.sdk4.Error) {
                                                return;
                                            }
                                            //todo ???Fault reason is "Operation Failed". Fault detail is
                                            // "[Cannot run VM. There is no host that satisfies current scheduling constraints. See below for details:, The host node2 did not satisfy internal filter Network because network(s) testNetworkNo1000 are missing., The host node2 did not satisfy internal filter Network because network(s) testNetworkNo1000 are missing., The host node2 did not satisfy internal filter Network because network(s) testNetworkNo1000 are missing.]". HTTP response code is "
                                            throw nestedException;
                                        }
                                    }
                            );
                }
            }

            ASSIGN_PERMISSION_ZONE:
            {
                // Get team users ids
                List<UUID> oVirtIds = team.getUsers().stream()
                        .map(pl.lodz.p.it.eduvirt.entity.User::getOVirtId)
                        .toList();

                // Filter VMs for which permission have been assigned
                List<VirtualMachine> filteredVmsToAssignPermission = filterVmsBySubtasks(
                        existingSubtasks,
                        originalVms,
                        ExecutorSubtask.SubtaskType.ASSIGN_PERMISSION,
                        true
                );

                // Assign permissions
                filteredVmsToAssignPermission
                        .stream()
                        .filter(vm -> !vm.isHidden())
                        .forEach(
                                vm -> runAndRegister(() -> addTeamPermissionsToVm(vm.getId(), oVirtIds),
                                        executorTask, vm.getId(), ExecutorSubtask.SubtaskType.ASSIGN_PERMISSION
                                )
                        );
            }

            executorTaskService.finalizeTask(executorTask.getId(), true);
        } catch (Throwable e) {
            executorTaskService.finalizeTask(executorTask.getId(), false, e.getMessage());
            throw e;
        }

        try {
            mailNotificationService.sendReservationStartNotification(reservation);
        } catch (Throwable e) {
            log.error("Some error occurred while sending reservation start notifications. Cause: {}", e.getMessage());
        }
    }

    private void stopPod(Reservation reservation) {
        ExecutorTask executorTask = executorTaskService.registerPodDestroyTask(reservation);
        List<ExecutorSubtask> existingSubtasks = executorTaskService.getStopPodExistingSubTasks(reservation);

        try {
            ResourceGroup resourceGroup = reservation.getResourceGroup();
            Team team = reservation.getTeam();
            List<VirtualMachine> originalVms = resourceGroup.getVms();

            REVOKE_PERMISSION_ZONE:
            {
                // Get team users ids
                List<UUID> oVirtIds = team.getUsers().stream()
                        .map(pl.lodz.p.it.eduvirt.entity.User::getOVirtId)
                        .toList();

                // Filter VMs for which permission have been assigned
                List<VirtualMachine> filteredVmsToRevokePermission = filterVmsBySubtasks(
                        existingSubtasks,
                        originalVms,
                        ExecutorSubtask.SubtaskType.REVOKE_PERMISSION,
                        true
                );

                // Revoke permissions
                // We do not do filtering by hidden flag in case, as a result of an error,
                // some permissions to a VM have been granted to this team
                filteredVmsToRevokePermission
                        .forEach(
                                vm -> runAndRegister(() -> revokeTeamPermissionsToVm(vm.getId(), oVirtIds),
                                        executorTask, vm.getId(), ExecutorSubtask.SubtaskType.REVOKE_PERMISSION
                                )
                        );
            }

            CLEAR_PRIVATE_SEGMENTS_ZONE:
            {
                Predicate<ExecutorSubtask> predicate = st ->
                        st.getType().equals(ExecutorSubtask.SubtaskType.REMOVE_VNIC_PROFILE) && st.getSuccessful();
                Set<UUID> nicsIdsToExclude = existingSubtasks.stream()
                        .filter(predicate)
                        .map(st -> ((VnicProfileTask) st).getNicId())
                        .collect(Collectors.toSet());

                // Private networks cleaning
                List<ResourceGroupNetwork> networksToRemove = resourceGroup.getNetworks();
                networksToRemove
                        .forEach(
                                network -> {
                                    // Filter already cleaned NICs
                                    List<NetworkInterface> interfaces = network.getInterfaces();
                                    interfaces.removeIf(nic -> nicsIdsToExclude.contains(nic.getId()));

                                    if (interfaces.isEmpty()) {
                                        return;
                                    }

                                    // Remove vnic profile from VMs NICs
                                    Set<UUID> removedVnicProfilesIdsSet = interfaces
                                            .stream()
                                            .map(
                                                    nic -> {
                                                        UUID vmId = nic.getVirtualMachine().getId();
                                                        return runAndRegister(
                                                                () -> removeVnicProfileFromNIC(vmId, nic.getId()),
                                                                executorTask, vmId, ExecutorSubtask.SubtaskType.REMOVE_VNIC_PROFILE,
                                                                AdditionalId.VNIC_PROFILE,
                                                                AdditionalId.NIC.withId(nic.getId())
                                                        );
                                                    }
                                            )
                                            .filter(Objects::nonNull)
                                            .collect(Collectors.toSet());

                                    // Set vnic profile's property "inUse" to false
                                    if (removedVnicProfilesIdsSet.size() == 1) {
                                        try {
                                            vnicProfilePoolService.markVnicProfileAsFree(removedVnicProfilesIdsSet.iterator().next());
                                        } catch (Throwable e) {
                                            log.error("An exception was thrown when marking the vnic profile as free - {}, ~ {}",
                                                    e.getClass().getName(),
                                                    e.getMessage()
                                            );
                                        }
                                    } else {
                                        log.warn("More than one (or none) assigned vnic profile was detected within " +
                                                "the private network segment, which prevented from marking, " +
                                                "the nominal vnic profile as free in the pool");
                                    }
                                }
                        );
            }

            STOP_VMS_ZONE:
            {
                // Filter VMs for which an attempt was made to shutdown
                List<VirtualMachine> filteredVmsToStop = filterVmsBySubtasks(
                        existingSubtasks,
                        originalVms,
                        ExecutorSubtask.SubtaskType.SHUTDOWN_VM,
                        false
                );

                // Shutdown VMs
                filteredVmsToStop
                        .forEach(
                                vm -> {
                                    try {
                                        runAndRegister(
                                                () -> oVirtVmService.shutdownVm(vm.getId().toString()),
                                                executorTask, vm.getId(), ExecutorSubtask.SubtaskType.SHUTDOWN_VM
                                        );
                                    } catch (Throwable nestedException) {
                                        // If the exception was related to a call to oVirt's API is intentionally
                                        // swallowed, so as not to interrupt the exception algorithm
                                        // (this is conditioned on one attempt to shutdown the VM,
                                        // attempts to stop will be made by another time task, as POWER_OFF operations
                                        if (nestedException.getCause() instanceof org.ovirt.engine.sdk4.Error) {
                                            return;
                                        }
                                        throw nestedException;
                                    }
                                }
                        );
            }

            executorTaskService.finalizeTask(executorTask.getId(), true);
        } catch (Throwable e) {
            executorTaskService.finalizeTask(executorTask.getId(), false, e.getMessage());
            throw e;
        }
    }

    private void finalizePodReservation(ExecutorTask task) {
        ExecutorTask executorTask = executorTaskService.registerEndReservationTask(task.getReservation());

        try {
            List<VirtualMachine> originalVms = task.getReservation().getResourceGroup().getVms();
            List<Vm> ovirtVms = fetchOvirtVms(originalVms);

            if (originalVms.size() != ovirtVms.size()) {
                throw new IllegalStateException("The number of VMs in eduVirt RG is different from the number of VMs " +
                        "fetched from oVirt, the reservation cannot be completed automatically"
                );
            }

            // Remove already shut down VMs from the list
            ovirtVms.removeIf(vm -> vm.status().equals(VmStatus.DOWN));

            if (!ovirtVms.isEmpty()) {
                ovirtVms
                        .forEach(
                                vm -> runAndRegister(() -> oVirtVmService.powerOffVm(vm.id()),
                                        executorTask, UUID.fromString(vm.id()), ExecutorSubtask.SubtaskType.POWER_OFF
                                )
                        );

                executorTaskService.finalizeTask(executorTask.getId(), false,
                        "The task must be repeated due to the need to check the success of the power off operations.");
            } else {
                // Mark reservation as completed
                reservationService.endReservation(task.getReservation());

                executorTaskService.finalizeTask(executorTask.getId(), true);
            }
        } catch (Throwable e) {
            executorTaskService.finalizeTask(executorTask.getId(), false, e.getMessage());
            throw e;
        }
    }

    /* Private methods */
    private void checkIfRgIsInUse(UUID processingReservationId, ResourceGroup resourceGroup) {
        List<Reservation> reservations = reservationService.findRgNotCompletedReservations(resourceGroup);
        if (!reservations.isEmpty()) {
            UUID[] reservationIds = reservations.stream()
                    .map(Reservation::getId)
                    .filter(id -> !id.equals(processingReservationId))
                    .toArray(UUID[]::new);
            if (reservationIds.length == 0) {
                // Protection against throwing RG unavailability error for the reservation currently being processed
                return;
            }
            throw new ResourceGroupCurrentlyInUseException(resourceGroup.getId(), reservationIds);
        }
    }

    /* Methods related to oVirt Api calls */
    private List<Vm> fetchOvirtVms(List<VirtualMachine> virtualMachines) {
        Set<String> vmIdsStr = virtualMachines.stream()
                .map(vm -> vm.getId().toString())
                .collect(Collectors.toSet());
        return oVirtVmService.findVmsWithNicsByVmIds(vmIdsStr);
    }

    private void checkIfVmsDownStatus(List<Vm> vms) {
        Set<Vm> invalidStatuses = new HashSet<>();
        Set<Vm> launchingStatuses = new HashSet<>();

        vms.forEach(vm -> {
                    switch (vm.status()) {
                        case DOWN, POWERING_DOWN, IMAGE_LOCKED -> {
                        }
                        case UP, MIGRATING, RESTORING_STATE,
                             SAVING_STATE, SUSPENDED, PAUSED,
                             NOT_RESPONDING, UNASSIGNED, UNKNOWN -> invalidStatuses.add(vm);
                        case REBOOT_IN_PROGRESS, POWERING_UP, WAIT_FOR_LAUNCH -> launchingStatuses.add(vm);
                    }
                }
        );

        if (!invalidStatuses.isEmpty()) {
            String invalidStatusesConcString = invalidStatuses.stream()
                    .map(vm -> "VM %s in %s status".formatted(vm.name(), vm.status().name()))
                    .collect(Collectors.joining(";"));

            String errorMessage = "Some VMs are in invalid statuses: " + invalidStatusesConcString;
            log.error(errorMessage);
            throw new VmInvalidStatusException(errorMessage);
        }

        if (!launchingStatuses.isEmpty()) {
            String transitionalStatusesConcString = launchingStatuses.stream()
                    .map(vm -> "VM %s in %s status".formatted(vm.name(), vm.status().name()))
                    .collect(Collectors.joining(";"));

            String errorMessage = "Some VMs are in launching statuses: " + transitionalStatusesConcString;
            log.error(errorMessage);
            throw new VmLaunchingStatusException(errorMessage);
        }
    }

    private void assignVnicProfileToNIC(UUID vnicProfileId, UUID vmId, UUID vmNicId) {
        oVirtVmService.assignVnicProfileToVm(
                vmId.toString(),
                vmNicId.toString(),
                vnicProfileId.toString()
        );
    }

    private void addTeamPermissionsToVm(UUID vmId, List<UUID> teamMembersIds) {
        teamMembersIds
                .forEach(
                        userId -> OVirtPermissionService.assignPermissionToVmToUser(vmId, userId,
                                "00000000-0000-0000-0001-000000000001")
                );
    }

    private void revokeTeamPermissionsToVm(UUID vmId, List<UUID> teamMembersIds) {
        teamMembersIds
                .forEach(
                        userId ->
                                OVirtPermissionService.findPermissionsByVmId(vmId)
                                        .forEach(permission -> {
                                            User userOpt = permission.user();
                                            if (Objects.nonNull(userOpt) && userOpt.id().equals(userId.toString())) {
                                                OVirtPermissionService.revokePermissionToVmFromUser(
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
                                 AdditionalId... additionalIds) {
        ExecutorSubtask executorSubtask = executorTaskService.registerSubTask(task.getId(), vmId, type);
        try {
            T tmpVal = supplier.get();
            if (tmpVal instanceof UUID && Objects.nonNull(additionalIds) &&
                    additionalIds.length >= 1 && Objects.isNull(additionalIds[0].getId())) {
                additionalIds[0].withId((UUID) tmpVal);
            }
            executorTaskService.finalizeSubTask(executorSubtask.getId(), true, additionalIds);
            return tmpVal;
        } catch (Throwable e) {
            executorTaskService.finalizeSubTask(executorSubtask.getId(), false, e.getMessage(), additionalIds);
            throw e;
        }
    }

    private void runAndRegister(Runnable runnable, ExecutorTask task, UUID vmId, ExecutorSubtask.SubtaskType type,
                                AdditionalId... additionalIds) {
        Supplier<?> castedSupplier = () -> {
            runnable.run();
            return null;
        };
        runAndRegister(castedSupplier, task, vmId, type, additionalIds);
    }

    /* On startup */
    @PostConstruct
    private void init() {
        // Fetch all IN_PROGRESS and set FAILED due to system restart
        // (solution for tasks and subtasks that are stuck in IN_PROGRESS status )
        executorTaskService.getReservationsInProgressTasks()
                .forEach(task -> executorTaskService.finalizeTask(task.getId(), false, "Failed due to system restart"));
        executorTaskService.getReservationsInProgressSubTasks()
                .forEach(subtask -> executorTaskService.finalizeSubTask(subtask.getId(), false, "Failed due to system restart"));
    }

    /* Utils methods */
    private static List<VirtualMachine> filterVmsBySubtasks(final List<ExecutorSubtask> subtasks,
                                                            final List<VirtualMachine> originalVms,
                                                            ExecutorSubtask.SubtaskType searchedSubtaskType,
                                                            boolean onlySuccessful) {
        Set<UUID> vmsIdsToExclude = subtasks.stream()
                .filter(subtask ->
                        subtask.getType().equals(searchedSubtaskType) && (!onlySuccessful || subtask.getSuccessful())
                )
                .map(ExecutorSubtask::getVmId)
                .collect(Collectors.toSet());
        List<VirtualMachine> filteredVms = new ArrayList<>(originalVms);
        filteredVms.removeIf(vm -> vmsIdsToExclude.contains(vm.getId()));

        return filteredVms;
    }
}
