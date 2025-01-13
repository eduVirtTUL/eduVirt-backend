package pl.lodz.p.it.eduvirt.executor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorSubtask;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ExecutorTaskRepository extends JpaRepository<ExecutorTask, UUID> {

    @Transactional(propagation = Propagation.REQUIRED)
    void deleteByReservation(Reservation reservation);

    //TODO michal: include reservations that has multiply failed tries to shutdown VMs

    @Query("""
            SELECT e FROM ExecutorTask e
            JOIN FETCH e.reservation
            JOIN FETCH e.reservation.resourceGroup
            JOIN FETCH e.reservation.resourceGroup.vms
            WHERE e.type = 'POD_DESTRUCT'
            AND e.status = 'SUCCESSFUL'
            AND e.updatedAt <= :probeTime
            AND e.reservation.status != 'COMPLETED'
            """)
    List<ExecutorTask> findReservationsToEndTasks(@Param("probeTime") LocalDateTime probeTime);

    @Query("SELECT e FROM ExecutorTask e WHERE e.status = 'IN_PROGRESS'")
    List<ExecutorTask> findReservationsInProgressTasks();

    @Query("SELECT st FROM ExecutorSubtask st WHERE st.successful IS NULL")
    List<ExecutorSubtask> findReservationsInProgressSubTasks();
}
