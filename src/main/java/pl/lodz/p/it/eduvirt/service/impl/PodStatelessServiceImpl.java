package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamNotFoundException;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.service.PodStatelessService;

import java.util.List;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class PodStatelessServiceImpl implements PodStatelessService {

    private final PodStatelessRepository podStatelessRepository;
    private final ResourceGroupPoolRepository resourceGroupPoolRepository;
    private final TeamRepository teamRepository;
    private final CourseRepository courseRepository;

    @PreAuthorize("isAuthenticated()")
    @Override
    public PodStateless createStatelessPod(PodStateless pod, UUID teamId, UUID resourceGroupPoolId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        Course course = courseRepository.findById(team.getCourse().getId())
                .orElseThrow(() -> new CourseNotFoundException(team.getCourse().getId()));

        ResourceGroupPool resourceGroupPool = resourceGroupPoolRepository.findById(resourceGroupPoolId)
                .orElseThrow(() -> new CourseNotFoundException(resourceGroupPoolId));

        if (resourceGroupPool.getCourse() != team.getCourse()) {
            throw new RuntimeException("Resource group pool does not belong to the course the team is in");
        }

        if (podStatelessRepository.existsByResourceGroupPoolId(resourceGroupPoolId)) {
            throw new RuntimeException("Resource group pool already has a pod assigned to it");
        }

        pod.setResourceGroupPool(resourceGroupPool);
        pod.setTeam(team);
        pod.setCourse(course);

        return podStatelessRepository.saveAndFlush(pod);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public void deleteStatelessPod(UUID podId) {
        if (!podStatelessRepository.existsById(podId)) {
            throw new PodNotFoundException();
        }
        podStatelessRepository.deleteById(podId);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<PodStateless> getStatelessPodsByTeam(UUID teamId) {
        return podStatelessRepository.findByTeamId(teamId);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<PodStateless> getStatelessPodsByCourse(UUID courseId) {
        return podStatelessRepository.findByCourseId(courseId);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<PodStateless> getStatelessPodsByResourceGroupPool(UUID resourceGroupPoolId) {
        return podStatelessRepository.findByResourceGroupPoolId(resourceGroupPoolId);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public PodStateless getStatelessPod(UUID podId) {
        return podStatelessRepository.findById(podId)
                .orElseThrow(PodNotFoundException::new);
    }
}
