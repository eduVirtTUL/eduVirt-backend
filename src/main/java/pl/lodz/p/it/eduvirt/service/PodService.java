package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.entity.PodStateful;
import java.util.List;
import java.util.UUID;

public interface PodService {
    PodStateful createPod(CreatePodStatefulDto dto);
    List<PodStateful> getPodsByTeam(UUID teamId);
    List<PodStateful> getPodsByCourse(UUID courseId);
    List<PodStateful> getPodsByResourceGroup(UUID resourceGroupId);
    PodStateful getPod(UUID podId);
    void deletePod(UUID podId);
    void createStatelessPod(UUID teamId, UUID resourceGroupId);
    void deleteStatelessPod(UUID teamId, UUID resourceGroupId);
    List<UUID> getStatelessPodsByTeam(UUID teamId);
}