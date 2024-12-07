package pl.lodz.p.it.eduvirt.executor.schedulers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ovirt.engine.sdk4.types.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.VirtualMachine;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.Reservation;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;
import pl.lodz.p.it.eduvirt.executor.service.ExecutorTaskService;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.TeamRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt._FakeReservation;
import pl.lodz.p.it.eduvirt.repository.eduvirt._FakeReservationRepository;
import pl.lodz.p.it.eduvirt.service.OVirtAssignedPermissionService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@LoggerInterceptor
@RequiredArgsConstructor
//@Transactional
public class ExecutorScheduler {

    private final OVirtVmService oVirtVmService;
    private final OVirtAssignedPermissionService ovirtAssignedPermissionService;
    private final _FakeReservationRepository reservationRepository;

    private final ExecutorTaskService executorTaskService;

    private void startUpPod(_FakeReservation reservation) {
        ExecutorTask executorTask = executorTaskService.registerPodInitTask(reservation);
        try {

            List<VirtualMachine> virtualMachines = reservation.getResourceGroup().getVms();

            //TODO michal: verify resources or handle insufficient on VM startup command (probably the first one)

            //TODO michal: network mapping

            //Start-up VMs
            if (reservation.getAutomaticStartup()) {
                virtualMachines
                        .stream()
                        .parallel()
                        .forEach(
                                vm -> runAndRegister(() -> oVirtVmService.runVm(vm.getId().toString()),
                                        executorTask, vm, ExecutorSubtask.SubtaskType.START_VM
                                )
                        );
            }

            //TODO michal: active waiting for all VMs are running

            //Assign permissions
            virtualMachines
                    .stream()
                    .parallel()
                    .forEach(
                            vm -> runAndRegister(() -> addTeamPermissionsToVm(vm.getId(), reservation.getTeam().getUsers()),
                                    executorTask, vm, ExecutorSubtask.SubtaskType.ASSIGN_PERMISSIONS
                            )
                    );

            executorTaskService.finalizeTask(executorTask.getId(), true, null);
        } catch (Throwable e) {
            executorTaskService.finalizeTask(executorTask.getId(), false, e.getMessage());
            throw e;
        }
    }

    private void addTeamPermissionsToVm(UUID vmId, List<UUID> teamMembersIds) {
        teamMembersIds
                .stream()
                .parallel()
                .forEach(
                        userId -> ovirtAssignedPermissionService.assignPermissionToVmToUser(vmId, userId,
                                "00000000-0000-0000-0001-000000000001")
                );
    }

    private void stopPod(_FakeReservation reservation) {
        ExecutorTask executorTask = executorTaskService.registerPodDestroyTask(reservation);
        try {
            List<VirtualMachine> virtualMachines = reservation.getResourceGroup().getVms();

            //TODO michal: private network cleaning

            //Shutdown VMs
            virtualMachines
                    .stream()
                    .parallel()
                    .forEach(
                            vm -> runAndRegister(() -> oVirtVmService.shutdownVm(vm.getId().toString()),
                                    executorTask, vm, ExecutorSubtask.SubtaskType.SHUTDOWN_VM
                            )
                    );

            //Revoke permissions
            virtualMachines
                    .stream()
                    .parallel()
                    .forEach(
                            vm -> runAndRegister(() -> revokeTeamPermissionsToVm(vm.getId(), reservation.getTeam().getUsers()),
                                    executorTask, vm, ExecutorSubtask.SubtaskType.REVOKE_PERMISSIONS
                            )
                    );

            executorTaskService.finalizeTask(executorTask.getId(), true, null);
        } catch (Throwable e) {
            executorTaskService.finalizeTask(executorTask.getId(), false, e.getMessage());
            throw e;
        }
    }

    private void revokeTeamPermissionsToVm(UUID vmId, List<UUID> teamMembersIds) {
        teamMembersIds
                .stream()
                .parallel()
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

    //---------TESTING--------------

    private final ResourceGroupRepository resourceGroupRepository;
    private final TeamRepository teamRepository;

    //TODO michal, real pooling

    @Scheduled(fixedRate = 5L, timeUnit = TimeUnit.MINUTES, initialDelay = -1L)
    @Transactional
    public void testExecuteBegin() {
        startUpPod(getFakeReservation());
    }

    @Scheduled(fixedRate = 3L, timeUnit = TimeUnit.MINUTES, initialDelay = 3L)
    @Transactional
    public void testExecuteEnd() {
        stopPod(getFakeReservation());
    }

    private _FakeReservation getFakeReservation() {
        Reservation reservation = reservationRepository.findById(
                UUID.fromString("52998fd2-30e2-4b04-9ba9-8113a5123f86")
        ).orElseThrow(EntityNotFoundException::new);
        return new _FakeReservation(
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getAutomaticStartup(),
                resourceGroupRepository.findById(reservation.getResourceGroupId())
                        .orElseThrow(EntityNotFoundException::new),
                teamRepository.findById(reservation.getTeamId())
                        .orElseThrow(EntityNotFoundException::new)
        );
    }

    private void runAndRegister(Runnable runnable,
                                ExecutorTask task,
                                VirtualMachine vm,
                                ExecutorSubtask.SubtaskType type) {
        try {
            runnable.run();
            executorTaskService.registerSubTask(task.getId(), vm.getId(), type, true, null);
        } catch (Throwable e) {
            executorTaskService.registerSubTask(task.getId(), vm.getId(), type, false, e.getMessage());
            throw e;
        }
    }
}
