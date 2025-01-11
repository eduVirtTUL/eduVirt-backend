package pl.lodz.p.it.eduvirt.entity.key;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.lodz.p.it.eduvirt.entity.Course;

@Entity
@Table(name = "course_access_key")
@Getter
@Setter
@NoArgsConstructor
public class CourseAccessKey extends AccessKey {
    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}