package pl.lodz.p.it.eduvirt.exceptions.course;

import pl.lodz.p.it.eduvirt.exceptions.general.AlreadyExistsException;

public class CourseAlreadyExists extends AlreadyExistsException {
    public CourseAlreadyExists(String name) {
        super("Course with name " + name + " already exists", "courseAlreadyExists");
    }
}
