package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.PodStateful;
import java.util.List;
import java.util.UUID;

public interface PodStatefulService {
    PodStateful createStatefulPod(PodStateful pod, UUID teamId, UUID resourceGroupId);
    List<PodStateful> getStatefulPodsByTeam(UUID teamId);
    List<PodStateful> getStatefulPodsByCourse(UUID courseId);
    List<PodStateful> getStatefulPodsByResourceGroup(UUID resourceGroupId);
    PodStateful getStatefulPod(UUID podId);
    void deleteStatefulPod(UUID podId);

}