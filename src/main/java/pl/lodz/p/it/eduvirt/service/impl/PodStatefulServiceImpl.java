package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.eduvirt.PodStateful;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;
import pl.lodz.p.it.eduvirt.exceptions.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.repository.eduvirt.PodStatefulRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.service.PodStatefulService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PodStatefulServiceImpl implements PodStatefulService {

    private final PodStatefulRepository podStatefulRepository;
    private final ResourceGroupRepository resourceGroupRepository;

    @Override
    public PodStateful createPod(PodStateful pod) {
        ResourceGroup resourceGroup = resourceGroupRepository.findById(pod.getResourceGroup().getId())
                .orElseThrow(() -> new ResourceGroupNotFoundException(pod.getResourceGroup().getId()));

        if (resourceGroup.isStateless()) {
            throw new IllegalStateException("Cannot create stateful pod for stateless resource group");
        }

        return podStatefulRepository.save(pod);
    }

    @Override
    public List<PodStateful> getPodsByTeam(UUID teamId) {
        return podStatefulRepository.findByTeamId(teamId);
    }

    @Override
    public List<PodStateful> getPodsByCourse(UUID courseId) {
        return podStatefulRepository.findByCourseId(courseId);
    }

    @Override
    public List<PodStateful> getPodsByResourceGroup(UUID resourceGroupId) {
        return podStatefulRepository.findByResourceGroupId(resourceGroupId);
    }

    @Override
    public PodStateful getPod(UUID podId) {
        return podStatefulRepository.findById(podId)
                .orElseThrow(() -> new RuntimeException("Pod not found")); // add custom exception later
    }

    @Override
    public void deletePod(UUID podId) {
        podStatefulRepository.deleteById(podId);
    }
}