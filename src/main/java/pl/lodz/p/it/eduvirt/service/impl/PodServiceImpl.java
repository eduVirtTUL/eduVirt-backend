package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.PodStateful;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamValidationException;
import pl.lodz.p.it.eduvirt.repository.PodRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.service.PodService;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.mappers.PodMapper;

import java.util.List;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class PodServiceImpl implements PodService {

    private final PodRepository podStatefulRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final TeamRepository teamRepository;
    private final CourseRepository courseRepository;
    private final PodMapper podMapper;

    // Stateful pods

    @Override
    @PreAuthorize("isAuthenticated()")
    public PodStateful createStatefulPod(CreatePodStatefulDto dto) {

        PodStateful pod = podMapper.createPodStatefulDtoToPodStateful(dto);

        Team team = teamRepository.findById(dto.teamId())
                .orElseThrow(TeamNotFoundException::new);

        Course course = courseRepository.findById(team.getCourse().getId())
                .orElseThrow(() -> new CourseNotFoundException(team.getCourse().getId()));

        ResourceGroup resourceGroup = resourceGroupRepository.findById(dto.resourceGroupId())
                .orElseThrow(() -> new ResourceGroupNotFoundException(dto.resourceGroupId()));

        if (resourceGroup.isStateless()) {
            throw new RuntimeException("Cannot create stateful pod for stateless resource group"); //TODO: custom exception
        }

        if (podStatefulRepository.existsByResourceGroupId(resourceGroup.getId())) {
            throw new RuntimeException("Resource group already has a pod assigned to it"); //TODO: custom exception
        }

        pod.setResourceGroup(resourceGroup);
        pod.setTeam(team);
        pod.setCourse(course);
        
        return podStatefulRepository.saveAndFlush(pod);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public List<PodStateful> getStatefulPodsByTeam(UUID teamId) {
        return podStatefulRepository.findByTeamId(teamId);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public List<PodStateful> getStatefulPodsByCourse(UUID courseId) {
        return podStatefulRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public List<PodStateful> getStatefulPodsByResourceGroup(UUID resourceGroupId) {
        return podStatefulRepository.findByResourceGroupId(resourceGroupId);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public PodStateful getStatefulPod(UUID podId) {
        return podStatefulRepository.findById(podId)
                .orElseThrow(PodNotFoundException::new);
    }

    //TODO: add logic if in use later
    @Override
    @PreAuthorize("isAuthenticated()")
    public void deleteStatefulPod(UUID podId) {

        PodStateful pod = podStatefulRepository.findById(podId)
                .orElseThrow(PodNotFoundException::new);


        podStatefulRepository.deleteById(podId);
    }

    //Stateless pods

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void createStatelessPod(UUID teamId, UUID resourceGroupId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        ResourceGroup resourceGroup = resourceGroupRepository.findById(resourceGroupId)
                .orElseThrow(() -> new ResourceGroupNotFoundException(resourceGroupId));

        if (!resourceGroup.isStateless()) {
            throw new RuntimeException("Cannot create stateless pod for stateful resource group");
        }

        if (team.getStatelessPods().contains(resourceGroupId)) {
            throw new TeamValidationException("Stateless pod for this team and resource group already exists");
        }

        team.getStatelessPods().add(resourceGroupId);
        teamRepository.saveAndFlush(team);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void deleteStatelessPod(UUID teamId, UUID resourceGroupId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        team.getStatelessPods().remove(resourceGroupId);
        teamRepository.saveAndFlush(team);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public List<UUID> getStatelessPodsByTeam(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        return team.getStatelessPods();
    }
}