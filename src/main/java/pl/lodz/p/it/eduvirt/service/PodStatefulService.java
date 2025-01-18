package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.PodStateful;

import java.util.List;
import java.util.UUID;

public interface PodStatefulService {
    PodStateful createStatefulPod(PodStateful pod, UUID teamId, UUID resourceGroupId);

    PodStateful getStatefulPodById(UUID podId);

    List<PodStateful> getStatefulPodsByTeam(UUID teamId);

    List<PodStateful> getStatefulPodsByCourse(UUID courseId);

    List<PodStateful> getStatefulPodsByResourceGroup(UUID resourceGroupId);

    void deleteStatefulPod(UUID podId);

}