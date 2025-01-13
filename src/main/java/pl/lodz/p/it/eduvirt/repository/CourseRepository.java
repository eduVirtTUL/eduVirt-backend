package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findByResourceGroupPoolsContaining(ResourceGroupPool resourceGroupPool);

    @Query("SELECT c FROM Course c WHERE :user IN (SELECT t.users FROM Team t WHERE t.course = c)")
    List<Course> findAllCoursesForStudent(@Param("user") User user, Pageable pageable);

    Course findByStateFullResourceGroupsContaining(ResourceGroup resourceGroup);
}
