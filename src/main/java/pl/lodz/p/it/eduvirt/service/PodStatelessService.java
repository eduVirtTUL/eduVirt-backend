package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.PodStateless;
import java.util.List;
import java.util.UUID;

public interface PodStatelessService {

    PodStateless createStatelessPod(PodStateless pod, UUID teamId, UUID resourceGroupPoolId);
    void deleteStatelessPod(UUID podId);
    List<PodStateless> getStatelessPodsByTeam(UUID teamId);
    List<PodStateless> getStatelessPodsByCourse(UUID courseId);
    List<PodStateless> getStatelessPodsByResourceGroupPool(UUID resourceGroupPoolId);
    PodStateless getStatelessPod(UUID podId);
}
