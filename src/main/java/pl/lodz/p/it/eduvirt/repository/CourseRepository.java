package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findByResourceGroupPoolsContaining(ResourceGroupPool resourceGroupPool);

    @Query("SELECT c FROM Course c WHERE :userId IN (SELECT t.users FROM Team t WHERE t.course = c)")
    List<Course> findAllCoursesForStudent(@Param("userId") UUID userId, Pageable pageable);

    Course findByStateFullResourceGroupsContaining(ResourceGroup resourceGroup);

    Page<Course> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
