package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.dto.reservation.CreateReservationDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDetailsDto;
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.ReservationNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.ReservationMapper;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/reservations")
@Transactional(propagation = Propagation.NEVER)
public class ReservationController {

    /* Services */

    private final ReservationService reservationService;
    private final ResourceGroupService resourceGroupService;
    private final ResourceGroupPoolService resourceGroupPoolService;
    private final CourseService courseService;
    private final TeamService teamService;

    /* Repositories */

    private final UserRepository userRepository;

    /* Mappers */

    private final ReservationMapper reservationMapper;

    /* Create methods */

    @Operation(summary = "Create new reservation", description = "This endpoint can be used to create a new reservation for the team they are a part of.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "New reservation, for given resource group and team, that the current user is a part of was created successfully."),
            @ApiResponse(responseCode = "400", description = "New reservation, for given resource group and team, that the current user is a part of was created successfully."),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.")
    })
    @PreAuthorize("hasRole('student')")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PostMapping(path = "/course/{courseId}/pod/{podId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createNewReservationForPod(@PathVariable("courseId") UUID courseId,
                                                    @PathVariable("podId") UUID podId,
                                                    @RequestBody @Validated CreateReservationDto createDto) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());

        // TODO: Potential refactor if course will have a list of users
        Course course = courseService.getCourse(courseId);
        Team team = teamService.getTeamByCourseAndUser(course, userId);

        if (team.getStatelessPods().stream().anyMatch(statelessPod -> statelessPod.getId().equals(podId)))
            reservationService.createReservationForStatelessPod(team, team.getStatelessPod(podId), createDto);
        else if (team.getStatefulPods().stream().anyMatch(statefulPod -> statefulPod.getId().equals(podId)))
            reservationService.createReservationForStatefulPod(team, team.getStatefulPod(podId), createDto);
        else throw new PodNotFoundException("POD %s could not be found for the team %s, which the current user belongs to for course %s"
                    .formatted(podId, team.getId(), course.getId()));

        return ResponseEntity.noContent().build();
    }

    /* Read methods */

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/{reservationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<ReservationDetailsDto> getReservationDetails(@PathVariable("reservationId") UUID reservationId) {
        Optional<Reservation> reservationOptional = reservationService.findReservationById(reservationId);

        if (reservationOptional.isPresent()) {
            Reservation foundReservation = reservationOptional.get();

            /* Check authorization */

            UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
            User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
            Course course = foundReservation.getTeam().getCourse();
            List<User> users = course.getTeams().stream().map(Team::getUsers).flatMap(Collection::stream).toList();
            List<String> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream().map(GrantedAuthority::getAuthority).toList();

            if (authorities.contains("administrator") ||
                    (authorities.contains("teacher") && course.getTeachers().contains(user)) ||
                    (authorities.contains("student") && users.contains(user))) {
                return ResponseEntity.ok(reservationMapper.reservationToDetailsDto(foundReservation));
            }
        }

        throw new ReservationNotFoundException(reservationId);
    }

    @PreAuthorize("hasRole('student')")
    @GetMapping(path = "/course/{courseId}/pods/{podId}/previous", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageDto<ReservationDto>> getPreviousReservations(
            Pageable pageable, @PathVariable("courseId") UUID courseId, @PathVariable("podId") UUID podId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        Course course = courseService.getCourse(courseId);
        Team team = teamService.getTeamByCourseAndUser(course, userId);

        Page<Reservation> reservations;
        if (team.getStatelessPods().stream().anyMatch(statelessPod -> statelessPod.getId().equals(podId)))
            reservations = reservationService.findReservationsForStatefulPod(
                    team.getStatefulPod(podId), team, pageable);
        else if (team.getStatelessPods().stream().anyMatch(statelessPod -> statelessPod.getId().equals(podId)))
            reservations = reservationService.findReservationsForStatelessPod(
                    team.getStatelessPod(podId), team, pageable);
        else throw new PodNotFoundException("POD %s for team %s in course %s could not be found"
                    .formatted(podId, team.getId(), course.getId()));

        List<ReservationDto> listOfDtos = reservations.getContent()
                .stream().map(reservationMapper::reservationToDto).toList();

        PageDto<ReservationDto> outputDto = new PageDto<>(listOfDtos,
                new PageInfoDto(reservations.getNumber(), reservations.getNumberOfElements(),
                        reservations.getTotalPages(), reservations.getTotalElements()));

        if (listOfDtos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(outputDto);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/courses/{courseId}/resource-groups/{rgId}/period")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<List<ReservationDto>> getRgReservationsInGivenCourse(
            @PathVariable("courseId") UUID courseId, @PathVariable("rgId") UUID rgId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Course course = courseService.getCourse(courseId);
        ResourceGroup resourceGroup = resourceGroupService.getResourceGroup(rgId);
        List<Reservation> reservations = reservationService.findRgReservations(resourceGroup, course, start, end);

        List<ReservationDto> listOfDtos = reservations.stream().map(reservationMapper::reservationToDto).toList();

        /* Check authorization */

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        List<User> users = course.getTeams().stream().map(Team::getUsers).flatMap(Collection::stream).toList();
        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        if ((authorities.contains("administrator") ||
                (authorities.contains("teacher") && course.getTeachers().contains(user)) ||
                (authorities.contains("student") && users.contains(user))) &&
                !reservations.isEmpty()) {
            return ResponseEntity.ok(listOfDtos);
        }

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/courses/{courseId}/resource-group-pools/{rgPoolId}/period")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<List<ReservationDto>> getRgPoolReservationsInGivenCourse(
            @PathVariable("courseId") UUID courseId, @PathVariable("rgPoolId") UUID rgPoolId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Course course = courseService.getCourse(courseId);
        ResourceGroupPool resourceGroupPool = resourceGroupPoolService.getResourceGroupPool(rgPoolId);
        List<Reservation> reservations = reservationService.findRgPoolReservations(resourceGroupPool, course, start, end);

        List<ReservationDto> listOfDtos = reservations.stream().map(reservationMapper::reservationToDto).toList();

        /* Check authorization */

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        List<User> users = course.getTeams().stream().map(Team::getUsers).flatMap(Collection::stream).toList();
        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        if ((authorities.contains("administrator") ||
                !(authorities.contains("teacher") && course.getTeachers().contains(user)) ||
                !(authorities.contains("student") && users.contains(user))) && !reservations.isEmpty()) {
            return ResponseEntity.ok(listOfDtos);
        }

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('student')")
    @GetMapping(path = "/active/courses/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<PageDto<ReservationDto>> getActiveReservations(
            @PathVariable("courseId") UUID courseId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Course course = courseService.getCourse(courseId);
        Team team = teamService.getTeamByCourseAndUser(course, userId);

        Page<Reservation> reservationPage = reservationService.findActiveReservations(team.getId(), pageable);

        List<ReservationDto> listOfDTOs = reservationPage.getContent().stream()
                .map(reservationMapper::reservationToDto).toList();

        PageDto<ReservationDto> outputDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(reservationPage.getNumber(), reservationPage.getNumberOfElements(),
                        reservationPage.getTotalPages(), reservationPage.getTotalElements()));

        /* Check authorization */

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        if ((authorities.contains("administrator") ||
                (authorities.contains("teacher") && course.getTeachers().contains(user)) ||
                (authorities.contains("student") && team.getUsers().contains(user))) &&
                !listOfDTOs.isEmpty()) {
            return ResponseEntity.ok(outputDto);
        }

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('student')")
    @GetMapping(path = "/historic/courses/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<PageDto<ReservationDto>> getHistoricReservations(
            @PathVariable("courseId") UUID courseId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Course course = courseService.getCourse(courseId);
        Team team = teamService.getTeamByCourseAndUser(course, userId);

        Page<Reservation> reservationPage = reservationService.findHistoricalReservations(team.getId(), pageable);

        List<ReservationDto> listOfDTOs = reservationPage.getContent().stream()
                .map(reservationMapper::reservationToDto).toList();

        PageDto<ReservationDto> outputDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(reservationPage.getNumber(), reservationPage.getNumberOfElements(),
                        reservationPage.getTotalPages(), reservationPage.getTotalElements()));

        /* Check authorization */

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        if ((authorities.contains("administrator") ||
                (authorities.contains("teacher") && course.getTeachers().contains(user)) ||
                (authorities.contains("student") && team.getUsers().contains(user))) &&
                !listOfDTOs.isEmpty()) {
            return ResponseEntity.ok(outputDto);
        }

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @GetMapping(path = "/active/teams/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageDto<ReservationDto>> getActiveReservationsForTeam(
            @PathVariable("teamId") UUID teamId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamService.getTeamById(teamId);
        Course course = team.getCourse();

        Page<Reservation> reservationPage = reservationService.findActiveReservations(teamId, pageable);

        List<ReservationDto> listOfDTOs = reservationPage.getContent().stream()
                .map(reservationMapper::reservationToDto).toList();

        PageDto<ReservationDto> outputDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(reservationPage.getNumber(), reservationPage.getNumberOfElements(),
                        reservationPage.getTotalPages(), reservationPage.getTotalElements()));

        /* Check authorization */

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        if ((authorities.contains("administrator") ||
                (authorities.contains("teacher") && course.getTeachers().contains(user))) &&
                !listOfDTOs.isEmpty()) {
            return ResponseEntity.ok(outputDto);
        }

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @GetMapping(path = "/historic/teams/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageDto<ReservationDto>> getHistoricReservationsForTeam(
            @PathVariable("teamId") UUID teamId,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamService.getTeamById(teamId);
        Course course = team.getCourse();

        Page<Reservation> reservationPage = reservationService.findHistoricalReservations(teamId, pageable);

        List<ReservationDto> listOfDTOs = reservationPage.getContent().stream()
                .map(reservationMapper::reservationToDto).toList();

        PageDto<ReservationDto> outputDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(reservationPage.getNumber(), reservationPage.getNumberOfElements(),
                        reservationPage.getTotalPages(), reservationPage.getTotalElements()));

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        if ((authorities.contains("administrator") ||
                (authorities.contains("teacher") && course.getTeachers().contains(user))) &&
                !listOfDTOs.isEmpty()) {
            return ResponseEntity.ok(outputDto);
        }

        return ResponseEntity.noContent().build();
    }

    /* Update / delete methods */

    @PreAuthorize("isAuthenticated()")
    @PostMapping(path = "/{reservationId}/cancel")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<Void> finishReservation(@PathVariable("reservationId") UUID reservationId) {
        Optional<Reservation> reservationOptional = reservationService.findReservationById(reservationId);

        if (reservationOptional.isPresent()) {
            Reservation foundReservation = reservationOptional.get();

            UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
            User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
            Team team = foundReservation.getTeam();
            Course course = foundReservation.getTeam().getCourse();
            List<String> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream().map(GrantedAuthority::getAuthority).toList();

            if (authorities.contains("administrator") ||
                    (authorities.contains("teacher") && course.getTeachers().contains(user)) ||
                    (authorities.contains("student") && team.getUsers().contains(user))) {
                reservationService.finishReservation(foundReservation);
                return ResponseEntity.noContent().build();
            }
        }

        throw new ReservationNotFoundException(reservationId);
    }
}
