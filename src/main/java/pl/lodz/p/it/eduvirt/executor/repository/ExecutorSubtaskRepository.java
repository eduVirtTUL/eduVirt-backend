package pl.lodz.p.it.eduvirt.executor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorSubtask;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ExecutorSubtaskRepository extends JpaRepository<ExecutorSubtask, UUID> {

    @Query("SELECT s FROM ExecutorSubtask s WHERE s.executorTask.reservation.id = :reservationId AND s.executorTask.type = :taskType")
    List<ExecutorSubtask> findByReservation(@Param("reservationId") UUID reservationId,
                                            @Param("taskType") ExecutorTask.TaskType taskType);
}
