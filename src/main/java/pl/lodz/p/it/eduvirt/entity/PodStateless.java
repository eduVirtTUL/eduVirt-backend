package pl.lodz.p.it.eduvirt.entity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name="pod_stateless")
@Entity
public class PodStateless extends AbstractEntity{

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "rgp_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "pod_stateless_rg_id_fk"),
            nullable = false
    )
    private ResourceGroupPool resourceGroupPool;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "team_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "pod_stateless_team_id_fk"),
            nullable = false
    )
    private Team team;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "course_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "pod_stateless_course_id_fk"),
            nullable = false
    )
    private Course course;

}
