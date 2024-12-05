package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.eduvirt.PodStateful;
import java.util.List;
import java.util.UUID;

public interface PodStatefulService {
    PodStateful createPod(PodStateful pod);
    List<PodStateful> getPodsByTeam(UUID teamId);
    List<PodStateful> getPodsByCourse(UUID courseId);
    List<PodStateful> getPodsByResourceGroup(UUID resourceGroupId);
    PodStateful getPod(UUID podId);
    void deletePod(UUID podId);
}