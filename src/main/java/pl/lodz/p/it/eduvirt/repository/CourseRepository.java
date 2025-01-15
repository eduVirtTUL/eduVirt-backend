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

    Page<Course> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByIdNotAndName(UUID id, String name);

    @Query("SELECT count(n) FROM Course c JOIN c.stateFullResourceGroups r JOIN r.networks n WHERE c.id = :id GROUP BY r.id")
    List<Integer> getStatefulResourceGroupNetworkCount(UUID id);

    @Query("SELECT count(n) FROM Course c JOIN c.resourceGroupPools p JOIN p.resourceGroups r JOIN r.networks n WHERE c.id = :id GROUP BY r.id")
    List<Integer> getStatelessResourceGroupNetworkCount(UUID id);

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.teachers WHERE c.id = :id")
    Optional<Course> findByIdWithTeachers(@Param("id") UUID id);

    Page<Course> findAllByTeachersContaining(User user, Pageable of);

    List<Course> findAllByTeachersContaining(User user);

    Page<Course> findAllByTeachersContainingAndNameContainingIgnoreCase(User attr0, String name, Pageable of);
}
