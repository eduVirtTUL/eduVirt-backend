package pl.lodz.p.it.eduvirt.exceptions.course;

import pl.lodz.p.it.eduvirt.exceptions.general.ConflictException;

public class CourseConflictException extends ConflictException {
    public CourseConflictException() {
        super("Course optimistic lock conflict", "courseConflict");
    }
}
