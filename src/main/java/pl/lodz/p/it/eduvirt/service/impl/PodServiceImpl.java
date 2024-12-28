package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.PodStateful;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.repository.PodRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.service.PodService;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.mappers.PodStatefulMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PodServiceImpl implements PodService {

    private final PodRepository podStatefulRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final ResourceGroupPoolRepository resourceGroupPoolRepository;
    private final TeamRepository teamRepository;
    private final CourseRepository courseRepository;
    private final PodStatefulMapper podStatefulMapper;

    @Override
    @Transactional
    public PodStateful createPod(CreatePodStatefulDto dto) {

        PodStateful pod = podStatefulMapper.toEntity(dto);

        Team team = teamRepository.findById(dto.teamId())
                .orElseThrow(() -> new TeamNotFoundException(dto.teamId()));

        Course course = courseRepository.findById(team.getCourse().getId())
                .orElseThrow(() -> new CourseNotFoundException(team.getCourse().getId()));

        ResourceGroup resourceGroup = resourceGroupRepository.findById(dto.resourceGroupId())
                .orElseThrow(() -> new ResourceGroupNotFoundException(dto.resourceGroupId()));

        if (resourceGroup.isStateless()) {
            throw new RuntimeException("Cannot create stateful pod for stateless resource group"); //TODO
        }

        if (podStatefulRepository.existsByResourceGroupId(resourceGroup.getId())) {
            throw new RuntimeException("Resource group already has a pod assigned to it"); //TODO
        }

        pod.setResourceGroup(resourceGroup);
        pod.setTeam(team);
        pod.setCourse(course);
        
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
                .orElseThrow(() -> new PodNotFoundException(podId));
    }

    // add logic if in use later
    @Override
    public void deletePod(UUID podId) {
        podStatefulRepository.deleteById(podId);
    }

    @Override
    @Transactional
    public void createStatelessPod(UUID teamId, UUID resourceGroupPoolId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        resourceGroupPoolRepository.findById(resourceGroupPoolId)
                .orElseThrow(() -> new ResourceGroupPoolNotFoundException(resourceGroupPoolId));

        if (team.getStatelessPods().contains(resourceGroupPoolId)) {
            throw new TeamValidationException("Stateless pod for this team and resource group pool already exists");
        }

        team.getStatelessPods().add(resourceGroupPoolId);
        teamRepository.save(team);
    }

    @Override
    @Transactional
    public void deleteStatelessPod(UUID teamId, UUID resourceGroupPoolId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        team.getStatelessPods().remove(resourceGroupPoolId);
        teamRepository.save(team);
    }

    @Override
    public List<UUID> getStatelessPodsByTeam(UUID teamId) {
        return teamRepository.findStatelessPodsByTeamId(teamId);
    }
}