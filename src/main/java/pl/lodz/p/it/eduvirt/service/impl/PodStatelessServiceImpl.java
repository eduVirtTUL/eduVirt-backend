package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.PodStateless;
import pl.lodz.p.it.eduvirt.entity.Reservation;
import pl.lodz.p.it.eduvirt.entity.Reservation.ReservationStatus;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.pod.InvalidPodTypeException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodDeletionException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.team.*;
import pl.lodz.p.it.eduvirt.repository.PodStatelessRepository;
import pl.lodz.p.it.eduvirt.repository.ReservationRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.service.PodStatelessService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class PodStatelessServiceImpl implements PodStatelessService {

    /* Repositories */

    private final PodStatelessRepository podStatelessRepository;
    private final ResourceGroupPoolRepository resourceGroupPoolRepository;
    private final TeamRepository teamRepository;
    private final ReservationRepository reservationRepository;

    /* Service methods */

    @Override
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
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


    @Override
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    @Transactional
    public List<PodStateless> createStatelessPodsBatch(List<PodStateless> pods, List<UUID> teamIds, List<UUID> resourceGroupPoolIds) {
        if (pods.size() != teamIds.size() || pods.size() != resourceGroupPoolIds.size()) {
            throw new IllegalArgumentException("Lists must be of equal size");
        }

        List<PodStateless> createdPods = new ArrayList<>();

        for (int i = 0; i < pods.size(); i++) {
            final int index = i;
            Team team = teamRepository.findById(teamIds.get(index))
                    .orElseThrow(() -> new TeamNotFoundException(teamIds.get(index)));

            ResourceGroupPool resourceGroupPool = resourceGroupPoolRepository.findById(resourceGroupPoolIds.get(i))
                    .orElseThrow(() -> new CourseNotFoundException(resourceGroupPoolIds.get(index)));

            if (resourceGroupPool.getCourse() != team.getCourse()) {
                throw new InvalidPodTypeException("Resource group pool does not belong to the course the team is in");
            }

            if (podStatelessRepository.existsByResourceGroupPoolIdAndTeamId(resourceGroupPool.getId(), team.getId())) {
                continue;
            }

            PodStateless pod = pods.get(i);
            pod.setResourceGroupPool(resourceGroupPool);
            pod.setTeam(team);
            pod.setCourse(team.getCourse());

            team.addStatelessPod(pod);
            createdPods.add(podStatelessRepository.saveAndFlush(pod));
        }

        return createdPods;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    @Transactional
    public void deleteStatelessPodsBatch(List<UUID> podIds) {
        List<PodStateless> pods = podStatelessRepository.findAllById(podIds);
        if (pods.size() != podIds.size()) {
            throw new PodNotFoundException("One or more pods not found");
        }
        podStatelessRepository.deleteAllById(podIds);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<PodStateless> getStatelessPodsByTeam(UUID teamId) {
        return podStatelessRepository.findByTeamId(teamId);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public List<PodStateless> getStatelessPodsByCourse(UUID courseId) {
        return podStatelessRepository.findByCourseId(courseId);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public List<PodStateless> getStatelessPodsByResourceGroupPool(UUID resourceGroupPoolId) {
        return podStatelessRepository.findByResourceGroupPoolId(resourceGroupPoolId);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public PodStateless getStatelessPodById(UUID podId) {
        return podStatelessRepository.findById(podId)
                .orElseThrow(() -> new PodNotFoundException("POD %s could not be found".formatted(podId)));
    }

    @Override
    @PreAuthorize("hasAuthority('teacher')")
    @Transactional
    public void deleteStatelessPod(UUID podId) {
        PodStateless pod = podStatelessRepository.findById(podId)
                .orElseThrow(() -> new PodNotFoundException("POD %s could not be found".formatted(podId)));

        List<Reservation> allReservations = reservationRepository
                .findAllRgPoolReservationsForGivenTeam(pod.getResourceGroupPool(), pod.getTeam());

        boolean hasActiveReservations = allReservations.stream()
                .anyMatch(r -> r.getStatus() == ReservationStatus.IN_PROGRESS &&
                        r.getEndTime().isAfter(LocalDateTime.now(ZoneOffset.UTC)));

        if (hasActiveReservations) {
            throw new PodDeletionException("Pod with id %s has active reservations".formatted(podId));
        }

        List<Reservation> futureReservations = allReservations.stream()
                .filter(r -> r.getStartTime().isAfter(LocalDateTime.now(ZoneOffset.UTC)))
                .toList();
        reservationRepository.deleteAll(futureReservations);

        podStatelessRepository.delete(pod);
    }
}
