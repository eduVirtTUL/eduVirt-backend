package pl.lodz.p.it.eduvirt.util.search;

import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import pl.lodz.p.it.eduvirt.dto.search.SearchDto;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.entity.User;

import java.util.Objects;

@AllArgsConstructor
public class ResourceGroupPoolSpecification implements Specification<ResourceGroupPool> {
    private final SearchDto searchDto;

    @Override
    public Predicate toPredicate(Root<ResourceGroupPool> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        String strToSearch = searchDto.getValue().toString().toLowerCase();

        switch (Objects.requireNonNull(SearchOperation.getSimpleOperation(searchDto.getOperation()))) {
            case CONTAINS -> {
                return cb.like(cb.lower(root.get(searchDto.getFilterKey())), "%" + strToSearch + "%");
            }
            case EQUAL -> {
                if (searchDto.getFilterKey().equals("teachers")) {
                    Join<ResourceGroupPool, Course> courseJoin = courseJoin(root);
                    Join<Course, User> userJoin = userJoin(courseJoin);
                    return cb.equal(userJoin.get("id"), searchDto.getValue());
                }

                if (searchDto.getFilterKey().equals("courseId")) {
                    return cb.equal(courseJoin(root).get("id"), searchDto.getValue());
                }

                return cb.equal(root.get(searchDto.getFilterKey()), searchDto.getValue());
            }

            default -> throw new UnsupportedOperationException("Operation not supported: " + searchDto.getOperation());
        }
    }

    private Join<ResourceGroupPool, Course> courseJoin(Root<ResourceGroupPool> root) {
        return root.join("course");
    }

    private Join<Course, User> userJoin(Join<ResourceGroupPool, Course> courseJoin) {
        return courseJoin.join("teachers");
    }
}
