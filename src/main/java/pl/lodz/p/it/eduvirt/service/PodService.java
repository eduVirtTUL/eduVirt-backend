package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.entity.PodStateful;
import java.util.List;
import java.util.UUID;

public interface PodService {
    PodStateful createStatefulPod(CreatePodStatefulDto dto);
    List<PodStateful> getStatefulPodsByTeam(UUID teamId);
    List<PodStateful> getStatefulPodsByCourse(UUID courseId);
    List<PodStateful> getStatefulPodsByResourceGroup(UUID resourceGroupId);
    PodStateful getStatefulPod(UUID podId);
    void deleteStatefulPod(UUID podId);
    void createStatelessPod(UUID teamId, UUID resourceGroupId);
    void deleteStatelessPod(UUID teamId, UUID resourceGroupId);
    List<UUID> getStatelessPodsByTeam(UUID teamId);
}