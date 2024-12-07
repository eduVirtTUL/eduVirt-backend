package pl.lodz.p.it.eduvirt.executor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.executor.entity.ExecutorTask;

import java.util.UUID;

@Repository
public interface ExecutorTaskRepository extends JpaRepository<ExecutorTask, UUID> {
}
