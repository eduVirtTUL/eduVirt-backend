package pl.lodz.p.it.eduvirt.entity.eduvirt;

import jakarta.persistence.*;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "pod_stateful")
@Entity
public class PodStateful extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "rg_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "pod_stateful_rg_id_fk"),
            nullable = false
    )
    private ResourceGroup resourceGroup;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "team_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "pod_stateful_team_id_fk"),
            nullable = false
    )
    private Team team;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "course_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "pod_stateful_course_id_fk"),
            nullable = false
    )
    private Course course;

    @Column(name = "cluster_id", nullable = false)
    private String clusterId;

}
