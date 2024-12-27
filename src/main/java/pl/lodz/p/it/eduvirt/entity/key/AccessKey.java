package pl.lodz.p.it.eduvirt.entity.key;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;

@Entity
@Table(name = "access_key")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessKey extends AbstractEntity {

    @Column(name = "key_value", nullable = false, unique = true)
    private String keyValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false)
    private AccessKeyType accessKeyType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @PrePersist
    @PreUpdate
    private void validateRelationships() {
        if (accessKeyType == AccessKeyType.TEAM && team == null) {
            throw new IllegalStateException("Team access key must have a team");
        }
        if (accessKeyType == AccessKeyType.COURSE && team != null) {
            throw new IllegalStateException("Course access key cannot have a team");
        }
    }
}