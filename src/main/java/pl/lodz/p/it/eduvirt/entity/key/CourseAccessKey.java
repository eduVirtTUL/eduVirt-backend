package pl.lodz.p.it.eduvirt.entity.key;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.eduvirt.entity.Course;

@Entity
@Table(name = "course_access_key")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CourseAccessKey extends AccessKey {
    @OneToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}