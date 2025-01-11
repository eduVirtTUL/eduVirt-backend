package pl.lodz.p.it.eduvirt.executor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutorTaskRepository extends JpaRepository<ExecutorTask, UUID> {

    //TODO michal: include reservations that has multiply failed tries to shutdown VMs

    @Query("""
            SELECT e FROM ExecutorTask e
            WHERE e.status = 'AWAITING_TO_END_RESERVATION'
            """)
    List<ExecutorTask> findReservationsToEndTasks();

    @Query("SELECT e FROM ExecutorTask e WHERE e.status = 'IN_PROGRESS'")
    List<ExecutorTask> findReservationsInProgressTasks();
}
