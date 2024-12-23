//package pl.lodz.p.it.eduvirt.executor.schedulers;
//
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.ovirt.engine.sdk4.types.User;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
//import pl.lodz.p.it.eduvirt.entity.ResourceGroupNetwork;
//import pl.lodz.p.it.eduvirt.entity.VirtualMachine;
//import pl.lodz.p.it.eduvirt.entity.network.VnicProfilePoolMember;
//import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;
//import pl.lodz.p.it.eduvirt.executor.entity.ExecutorSubtask;
//import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;
//import pl.lodz.p.it.eduvirt.executor.service.ExecutorTaskService;
//import pl.lodz.p.it.eduvirt.repository.ReservationRepository;
//import pl.lodz.p.it.eduvirt.repository.ResourceGroupNetworkRepository;
//import pl.lodz.p.it.eduvirt.repository.ResourceGroupRepository;
//import pl.lodz.p.it.eduvirt.repository.TeamRepository;
//import pl.lodz.p.it.eduvirt.service.OVirtAssignedPermissionService;
//import pl.lodz.p.it.eduvirt.service.OVirtVmService;
//import pl.lodz.p.it.eduvirt.service.VnicProfilePoolService;
//
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
////write a-head log -> 49:00 minuta
//
//@Slf4j
//@Service
//@LoggerInterceptor
//@RequiredArgsConstructor
////@Transactional
//public class ExecutorScheduler {
//
//    private final OVirtVmService oVirtVmService;
//    private final OVirtAssignedPermissionService ovirtAssignedPermissionService;
//    private final VnicProfilePoolService vnicProfilePoolService;
//
//    //TODO michal: service instead of repository
//    private final ReservationRepository reservationRepository;
//    private final ResourceGroupNetworkRepository resourceGroupNetworkRepository;
//
//    private final ExecutorTaskService executorTaskService;
//
//    //TODO michal: real pooling instead of invoking checking conditions in fixed time
//
//    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = -1L)
//    @Transactional
//    public void createPods() {
//        reservationRepository.findReservationsToBegin()
//                .forEach(reservation -> startUpPod(reservation, getMockTeamAndRG(reservation)));
//    }
//
//    @Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES, initialDelay = -1L)
//    @Transactional
//    public void destroyPods() {
//        reservationRepository.findReservationsToFinish()
//                .forEach(reservation -> stopPod(reservation, getMockTeamAndRG(reservation)));
//    }
//
//    //--------------PRIVATE METHODS--------------
//
//    private void startUpPod(Reservation reservation, _FakeMockTeamAndRG mockTeamAndRG) {
//        ExecutorTask executorTask = executorTaskService.registerPodInitTask(reservation);
//        try {
//
//            List<VirtualMachine> virtualMachines = mockTeamAndRG.getResourceGroup().getVms();
//
//            //TODO michal: Verify resources or handle insufficient on VM startup command (probably the first one)
//
//            //Network mapping
//            List<ResourceGroupNetwork> networksToMap = resourceGroupNetworkRepository.getAllByResourceGroupId(mockTeamAndRG.getResourceGroup().getId());
//            networksToMap
//                    .stream()
//                    //.parallel()
//                    .forEach(
//                            network -> {
//                                // TODO michal: CZY SEGMENTY SIECIOWE SĄ DEFINIOWANE PER CLUSTER? CZY DLA CAŁEGO DATA CENTER SĄ WSPÓLNE
//                                runAndRegister(() -> {
//                                            // Fetch vnic profile from pool, checking conditions (if inUse equals false)
//                                            VnicProfilePoolMember chosenVnicProfile = vnicProfilePoolService.getVnicProfilesPool()
//                                                    .stream()
//                                                    .filter(vnicProfile -> !vnicProfile.getInUse())
//                                                    .findFirst()
//                                                    .orElseThrow(() -> new RuntimeException("No available vnic profile found in pool"));
//
//                                            // Set vnic profile's property "inUse" to true
//                                            vnicProfilePoolService.markVnicProfileAsOccupied(chosenVnicProfile.getId());
//
//                                            // Assign vnic profile to VMs NICs
//                                            assignVnicProfilesToNICs(chosenVnicProfile.getId(), network.getVmNic());
//                                        },
//                                        executorTask, null, ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE
//                                );
//                            }
//                    );
//
//
//            //TODO michal: Handle situation when some VMs are running
//
//            //Start-up VMs
//            if (reservation.getAutomaticStartup()) {
//                virtualMachines
//                        .stream()
//                        //.parallel()
//                        .forEach(
//                                vm -> runAndRegister(() -> oVirtVmService.runVm(vm.getId().toString()),
//                                        executorTask, vm, ExecutorSubtask.SubtaskType.START_VM
//                                )
//                        );
//            }
//
//            //TODO michal: Active waiting for all VMs are running
//
//            //Assign permissions
//            //TODO michal: handle HIDDEN flag
//            virtualMachines
//                    .stream()
//                    //.parallel()
//                    .forEach(
//                            vm -> runAndRegister(() -> addTeamPermissionsToVm(vm.getId(), mockTeamAndRG.getTeam().getUsers()),
//                                    executorTask, vm, ExecutorSubtask.SubtaskType.ASSIGN_PERMISSIONS
//                            )
//                    );
//
//            executorTaskService.finalizeTask(executorTask.getId(), true, null);
//        } catch (Throwable e) {
//            executorTaskService.finalizeTask(executorTask.getId(), false, e.getMessage());
//            throw e;
//        }
//    }
//
//    private void assignVnicProfilesToNICs(UUID vnicProfileId, List<_FakeVmNicEntity> vmNicList) {
//        vmNicList.stream()
////                .parallel()
//                .forEach(
//                        vmNic -> oVirtVmService.assignVnicProfileToVm(
//                                        vmNic.getVmId().toString(),
//                                        vmNic.getNicId().toString(),
//                                        vnicProfileId.toString())
//                );
//    }
//
//    private void addTeamPermissionsToVm(UUID vmId, List<UUID> teamMembersIds) {
//        teamMembersIds
//                .stream()
//                //.parallel()
//                .forEach(
//                        userId -> ovirtAssignedPermissionService.assignPermissionToVmToUser(vmId, userId,
//                                "00000000-0000-0000-0001-000000000001")
//                );
//    }
//
//    //TODO michal: if pod doesnt start should we invoke stopping it??? - now stopping is invoking in any cases
//    private void stopPod(Reservation reservation, _FakeMockTeamAndRG mockTeamAndRG) {
//        ExecutorTask executorTask = executorTaskService.registerPodDestroyTask(reservation);
//        try {
//            List<VirtualMachine> virtualMachines = mockTeamAndRG.getResourceGroup().getVms();
//
//            //Revoke permissions
//            virtualMachines
//                    .stream()
//                    //.parallel()
//                    .forEach(
//                            vm -> runAndRegister(() -> revokeTeamPermissionsToVm(vm.getId(), mockTeamAndRG.getTeam().getUsers()),
//                                    executorTask, vm, ExecutorSubtask.SubtaskType.REVOKE_PERMISSIONS
//                            )
//                    );
//
//            //TODO michal: Private networks cleaning
//            //TODO michal: decide to do it before or after VMs shutdown
//            List<ResourceGroupNetwork> networksToRemove = resourceGroupNetworkRepository.getAllByResourceGroupId(mockTeamAndRG.getResourceGroup().getId());
//            networksToRemove
//                    .stream()
//                    //.parallel()
//                    .forEach(
//                            network -> {
//                                runAndRegister(() -> {
//                                    //TODO michal: change LIST to SET or single value
//                                            // Remove vnic profile from VMs NICs
//                                            List<UUID> removedVnicProfileIdList = removeVnicProfilesFromNICs(network.getVmNic());
//                                            System.out.println("kanapkaVNIC PROFILE REMOVED: " + removedVnicProfileIdList);
//                                            // Set vnic profile's property "inUse" to false
//                                            removedVnicProfileIdList.forEach(
//                                                    vnicProfilePoolService::markVnicProfileAsFree
//                                            );
//                                        },
//                                        executorTask, null, ExecutorSubtask.SubtaskType.ASSIGN_VNIC_PROFILE
//                                );
//                            }
//                    );
//
//            //Shutdown VMs
//            virtualMachines
//                    .stream()
//                    //.parallel()
//                    .forEach(
//                            vm -> runAndRegister(() -> oVirtVmService.shutdownVm(vm.getId().toString()),
//                                    executorTask, vm, ExecutorSubtask.SubtaskType.SHUTDOWN_VM
//                            )
//                    );
//
//            //TODO michal: when waiting to vm shutdown for a long time, use power off
//
//            executorTaskService.finalizeTask(executorTask.getId(), true, null);
//        } catch (Throwable e) {
//            executorTaskService.finalizeTask(executorTask.getId(), false, e.getMessage());
//            throw e;
//        }
//    }
//
//    private void revokeTeamPermissionsToVm(UUID vmId, List<UUID> teamMembersIds) {
//        teamMembersIds
//                .stream()
//                //.parallel()
//                //TODO michal: consider caching permissionIds
//                .forEach(
//                        userId ->
//                                ovirtAssignedPermissionService.findPermissionsByVmId(vmId)
//                                        .forEach(permission -> {
//                                            User userOpt = permission.user();
//                                            if (Objects.nonNull(userOpt) && userOpt.id().equals(userId.toString())) {
//                                                ovirtAssignedPermissionService.revokePermissionToVmFromUser(
//                                                        UUID.fromString(permission.id())
//                                                );
//                                            }
//                                        })
//                );
//    }
//
//    private List<UUID> removeVnicProfilesFromNICs(List<_FakeVmNicEntity> vmNicList) {
//        return vmNicList.stream()
////                .parallel()
//                .map(
//                        //TODO michal: do null-safe mapping
//                        vmNic -> UUID.fromString(
//                                oVirtVmService.removeVnicProfileFromVm(vmNic.getVmId().toString(), vmNic.getNicId().toString())
//                        )
//                )
//                .toList();
//    }
//
//    private void runAndRegister(Runnable runnable,
//                                ExecutorTask task,
//                                VirtualMachine vm,
//                                ExecutorSubtask.SubtaskType type) {
//        UUID vmId = UUID.fromString("00000000-0000-0000-0000-000000000000");
//        try {
//            vmId = Optional.ofNullable(vm).map(VirtualMachine::getId)
//                    .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));
////            synchronized (this) {
//            runnable.run();
////            }
//            executorTaskService.registerSubTask(task.getId(), vmId, type, true, null);
//        } catch (Throwable e) {
//            executorTaskService.registerSubTask(task.getId(), vmId, type, false, e.getMessage());
//            throw e;
//        }
//    }
//
//    //--------------TESTING--------------
//
//    private final ResourceGroupRepository resourceGroupRepository;
//    private final TeamRepository teamRepository;
//
//    @Deprecated
//    private _FakeMockTeamAndRG getMockTeamAndRG(Reservation reservation) {
//        return new _FakeMockTeamAndRG(
//                resourceGroupRepository.findById(reservation.getResourceGroupId())
//                        .orElseThrow(EntityNotFoundException::new),
//                teamRepository.findById(reservation.getTeamId())
//                        .orElseThrow(EntityNotFoundException::new)
//        );
//    }
//}
