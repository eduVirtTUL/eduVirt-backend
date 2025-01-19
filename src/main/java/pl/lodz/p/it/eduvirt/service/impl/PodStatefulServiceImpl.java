package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.PodStateful;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.exceptions.course.*;
import pl.lodz.p.it.eduvirt.exceptions.pod.InvalidPodTypeException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.team.*;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.PodStatefulRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.service.PodStatefulService;

import java.util.List;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class PodStatefulServiceImpl implements PodStatefulService {

    /* Repositories */

    private final PodStatefulRepository podStatefulRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final TeamRepository teamRepository;
    private final CourseRepository courseRepository;

    /* Service methods */

    @Override
    @PreAuthorize("isAuthenticated()")
    public PodStateful createStatefulPod(PodStateful pod, UUID teamId, UUID resourceGroupId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        courseRepository.findById(team.getCourse().getId())
                .orElseThrow(() -> new CourseNotFoundException(team.getCourse().getId()));

        ResourceGroup resourceGroup = resourceGroupRepository.findById(resourceGroupId)
                .orElseThrow(() -> new ResourceGroupNotFoundException(resourceGroupId));

        if (resourceGroup.isStateless()) {
            throw new InvalidPodTypeException("Cannot create stateful pod for stateless resource group");
        }

        if (podStatefulRepository.existsByResourceGroupId(resourceGroup.getId())) {
            throw new PodAlreadyExistsException("Resource group with id %s already has a pod assigned to it".formatted(resourceGroup.getId()));
        }

        pod.setResourceGroup(resourceGroup);
        team.addStatefulPod(pod);

        return podStatefulRepository.saveAndFlush(pod);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public PodStateful getStatefulPodById(UUID podId) {
        return podStatefulRepository.findById(podId)
                .orElseThrow(() -> new PodNotFoundException("Stateful pod with id %s not found"
                        .formatted(podId)));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<PodStateful> getStatefulPodsByTeam(UUID teamId) {
        return podStatefulRepository.findByTeamId(teamId);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public List<PodStateful> getStatefulPodsByCourse(UUID courseId) {
        return podStatefulRepository.findByCourseId(courseId);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public List<PodStateful> getStatefulPodsByResourceGroup(UUID resourceGroupId) {
        return podStatefulRepository.findByResourceGroupId(resourceGroupId);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void deleteStatefulPod(UUID podId) {
        //TODO: come back here later
        podStatefulRepository.deleteById(podId);
    }

}