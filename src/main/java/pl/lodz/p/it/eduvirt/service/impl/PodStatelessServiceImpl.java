package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.PodStateless;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.pod.InvalidPodTypeException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.team.*;
import pl.lodz.p.it.eduvirt.repository.PodStatelessRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
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

    @PreAuthorize("isAuthenticated()")
    @Override
    public PodStateless createStatelessPod(PodStateless pod, UUID teamId, UUID resourceGroupPoolId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        ResourceGroupPool resourceGroupPool = resourceGroupPoolRepository.findById(resourceGroupPoolId)
                .orElseThrow(() -> new CourseNotFoundException(resourceGroupPoolId));

        if (resourceGroupPool.getCourse() != team.getCourse()) {
            throw new InvalidPodTypeException("Resource group pool does not belong to the course the team is in");
        }

        if (podStatelessRepository.existsByResourceGroupPoolIdAndTeamId(resourceGroupPoolId, teamId)) {
            throw new PodAlreadyExistsException("This user already has a stateless pod for this resource group pool");
        }

        pod.setResourceGroupPool(resourceGroupPool);
        team.addStatelessPod(pod);

        return podStatelessRepository.saveAndFlush(pod);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public void deleteStatelessPod(UUID podId) {
        if (!podStatelessRepository.existsById(podId)) {
            throw new PodNotFoundException("POD %s could not be found".formatted(podId));
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
                .orElseThrow(() -> new PodNotFoundException("POD %s could not be found".formatted(podId)));
    }
}
