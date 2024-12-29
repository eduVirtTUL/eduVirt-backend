package pl.lodz.p.it.eduvirt.entity.key;

import jakarta.persistence.*;
import lombok.*;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;

@Entity
@Table(name = "team_access_key")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TeamAccessKey extends AccessKey {
    @OneToOne(optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}