package pl.lodz.p.it.eduvirt.service.priviliges;

import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;

public interface PrivilegesService {
    boolean validateResourceGroupOwnership(ResourceGroup resourceGroup);

    boolean validateResourceGroupOwnershipOrAdmin(ResourceGroup resourceGroup);

    boolean validateCourseOwnership(Course course);

    boolean validateCourseOwnershipOrAdmin(Course course);
}
