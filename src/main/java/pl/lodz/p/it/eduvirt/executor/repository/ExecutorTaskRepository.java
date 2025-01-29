package pl.lodz.p.it.eduvirt.executor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.tasks.ExecutorTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ExecutorTaskRepository extends JpaRepository<ExecutorTask, UUID> {

    @Query("""
            SELECT DISTINCT e FROM ExecutorTask e
            WHERE e.type = 'POD_DESTRUCT'
            AND e.status = 'SUCCESSFUL'
            AND e.updatedAt <= :probeTimeWithGraceTime
            AND e.reservation.status != 'COMPLETED'
            """)
    List<ExecutorTask> findReservationsToEndTasks(@Param("probeTimeWithGraceTime") LocalDateTime probeTimeWithGraceTime);

    @Query("SELECT e FROM ExecutorTask e WHERE e.status = 'IN_PROGRESS'")
    List<ExecutorTask> findReservationsInProgressTasks();

    @Query("SELECT st FROM ExecutorSubtask st WHERE st.successful IS NULL")
    List<ExecutorSubtask> findReservationsInProgressSubTasks();
}
