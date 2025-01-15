package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import pl.lodz.p.it.eduvirt.dto.course.CourseDto;
import pl.lodz.p.it.eduvirt.dto.course.CreateCourseDto;
import pl.lodz.p.it.eduvirt.dto.course.UpdateCourseDto;
import pl.lodz.p.it.eduvirt.entity.Course;

import java.util.List;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    CourseDto courseToCourseDto(Course course);

    List<CourseDto> toCourseDtoList(Stream<Course> courses);

    @Mapping(target = "teachers", ignore = true)
    Course courseCreateDtoToCourse(CreateCourseDto createCourseDto);

    Course toEntity(UpdateCourseDto updateCourceDto);
}
