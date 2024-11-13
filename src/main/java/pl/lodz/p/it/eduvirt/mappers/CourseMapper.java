package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import pl.lodz.p.it.eduvirt.dto.course.CourseDto;
import pl.lodz.p.it.eduvirt.dto.course.CreateCourseDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Course;

import java.util.List;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    CourseDto courseToCourseDto(Course course);
    List<CourseDto> toCourseDtoList(Stream<Course> courses);

    Course crouseCreateDtoToCourse(CreateCourseDto createCourseDto);

}
