package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import pl.lodz.p.it.eduvirt.dto.reservation.ReservationTimeframeModifiersDto;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.ReservationNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.exceptions.pod.PodNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.ReservationMapper;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.*;
import pl.lodz.p.it.eduvirt.util.RoleConstants;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/reservations")
@Transactional(propagation = Propagation.NEVER)
public class ReservationController {

    @Value("${window.length}")
    private int windowLength;

    @Value("${executor.task-time-tolerance}")
    private int taskTimeTolerance;

    @Value("${executor.vm.grace-time}")
    private int vmGraceTime;

    @PostConstruct
    public void validateProperty() {
        if (windowLength < 10) windowLength = 10;
        if (windowLength > 60) windowLength = 60;
    }

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

    @Operation(
        method = "POST", summary = "Create a new reservation",
        description = "This endpoint can be used to create a new reservation for the team they are a part of.",
        parameters = {
            @Parameter(name = "courseId", in = ParameterIn.PATH, description = "Identifier of the course, which the reserved POD belongs to.", required = true),
            @Parameter(name = "podId", in = ParameterIn.PATH, description = "Identifier of the POD, which is to be reserved.", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = """
            Data transfer object containing essential information about created reservation, like the start and end time of the reservation,
            information whether resources in given resource group should be started automatically and notification time, which is a time before
            the end of reservation, which the notification about the end of the reservation is sent."""
        ),
        responses = {
            @ApiResponse(responseCode = "204", description = "New reservation, for given resource group and team, that the current user is a part of was created successfully."),
            @ApiResponse(responseCode = "400", description = "Reservation could not be created, since one of the check's did not pass.", content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.", content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('student')")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PostMapping(path = "/course/{courseId}/pod/{podId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> createNewReservationForPod(@PathVariable("courseId") UUID courseId,
                                                    @PathVariable("podId") UUID podId,
                                                    @RequestBody @Validated CreateReservationDto createDto) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        Course course = courseService.getCourse(courseId);
        Team team = teamService.getTeamByCourseAndUser(course, userId);

        if (team.getStatelessPods().stream().anyMatch(statelessPod -> statelessPod.getId().equals(podId)))
            reservationService.createReservationForStatelessPod(team, team.getStatelessPod(podId), createDto);
        else if (team.getStatefulPods().stream().anyMatch(statefulPod -> statefulPod.getId().equals(podId)))
            reservationService.createReservationForStatefulPod(team, team.getStatefulPod(podId), createDto);
        else
            throw new PodNotFoundException("POD %s could not be found for the team %s, which the current user belongs to for course %s"
                    .formatted(podId, team.getId(), course.getId()));

        return ResponseEntity.noContent().build();
    }

    /* Read methods */

    @Operation(
        method = "GET", summary = "Get window length for the calendar component", hidden = true,
        description = "This endpoint can be used to fetch window length for the reservation calendar component in the UI.",
        responses = {
            @ApiResponse(responseCode = "200", description = """
                Window length, specified by the administrator in the deployment descriptor and validated by the application, is returned to the client.
                Could not be shorter that 10 minutes and longer than 60 minutes"""),
        }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/window-length")
    ResponseEntity<Integer> getWindowLength() {
        return ResponseEntity.ok(windowLength);
    }

    @Operation(
            method = "GET", summary = "Get anticipated reservation start time delay and reservation end time hastening", hidden = false,
            description = "This endpoint can be used to fetch anticipated reservation start time delay and reservation end time hastening.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reservation start time delay and reservation end time hastening, specified by the administrator in the deployment descriptor, is returned to the client."),
            }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/timeframe-modifiers")
    ResponseEntity<ReservationTimeframeModifiersDto> getReservationGlobalTimeframeModifiers() {
        int startTimeDelay = 0;
        int endTimeHastening = taskTimeTolerance + vmGraceTime;
        return ResponseEntity.ok(new ReservationTimeframeModifiersDto(startTimeDelay, endTimeHastening));
    }

    @Operation(
        method = "GET", summary = "Get detailed information about certain reservation",
        description = "This endpoint can be used to fetch detailed information about certain reservation, identified with the given identifier.",
        parameters = {
            @Parameter(name = "reservationId", in = ParameterIn.PATH, description = "Identifier of the reservation, which detailed information is to be found in the database.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Reservation, identified with given identifier was found and sent to the client successfully."),
            @ApiResponse(responseCode = "404", description = "Reservation, identified with given identifier could not be found in the database, or currently authenticated user did not have privileges to access it.", content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.", content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
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

            if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                    (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) ||
                    (authorities.contains(RoleConstants.STUDENT) && users.contains(user))) {
                return ResponseEntity.ok(reservationMapper.reservationToDetailsDto(foundReservation));
            }
        }

        throw new ReservationNotFoundException(reservationId);
    }

    @Operation(
        method = "GET", summary = "Get number of all the reservation of the given POD in given course",
        description = "This endpoint can be used to total number of reservations made for given POD by the team currently authenticated user is a part of.",
        parameters = {
            @Parameter(name = "courseId", in = ParameterIn.PATH, description = "Identifier of the course, which contains the POD, which total number of reservations is to be fetch for.", required = true),
            @Parameter(name = "podId", in = ParameterIn.PATH, description = "Identifier of the POD, which the total number of reservation is checked for.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = """
                POD, identified with given identifier was found for the team, that current user is a part of, and total number of
                reservations made for that POD by that team was found and sent to the client successfully."""),
            @ApiResponse(responseCode = "400", description = """
                POD identified with given identifier could not be found for the team, that the currently authenticated user is a part of.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('student')")
    @GetMapping(path = "/course/{courseId}/pods/{podId}/previous/count", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<Integer> getPreviousReservationsCount(
            @PathVariable("courseId") UUID courseId, @PathVariable("podId") UUID podId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        Course course = courseService.getCourse(courseId);
        Team team = teamService.getTeamByCourseAndUser(course, userId);

        int reservationsCount;
        if (team.getStatelessPods().stream().anyMatch(statelessPod -> statelessPod.getId().equals(podId)))
            reservationsCount = reservationService.findReservationCountForStatelessPod(team.getStatelessPod(podId), team);
        else if (team.getStatefulPods().stream().anyMatch(statefulPod -> statefulPod.getId().equals(podId)))
            reservationsCount = reservationService.findReservationCountForStatefulPod(team.getStatefulPod(podId), team);
        else throw new PodNotFoundException("POD %s for team %s in course %s could not be found"
                    .formatted(podId, team.getId(), course.getId()));

        return ResponseEntity.ok(reservationsCount);
    }

    @Operation(
        method = "GET", summary = "Get reservation of the given POD in given course",
        description = "This endpoint can be used to find reservations made for given POD by the team currently authenticated user is a part of (including pagination).",
        parameters = {
            @Parameter(name = "courseId", in = ParameterIn.PATH, description = "Identifier of the course, which contains the POD, which reservations are to be fetch for.", required = true),
            @Parameter(name = "podId", in = ParameterIn.PATH, description = "Identifier of the POD, which the reservation are to be fetched from the database for.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = """
                POD, identified with given identifier was found for the team, that current user is a part of, and
                reservations made for that POD by that team (that were found on the given page of the given size)
                were sent to the client successfully."""),
            @ApiResponse(responseCode = "204", description = "No reservations of the given POD were found for the given team on the given page of the given size."),
            @ApiResponse(responseCode = "400", description = """
                POD identified with given identifier could not be found for the team, that the currently authenticated user is a part of.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('student')")
    @GetMapping(path = "/course/{courseId}/pods/{podId}/previous", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<PageDto<ReservationDto>> getPreviousReservations(
            Pageable pageable, @PathVariable("courseId") UUID courseId, @PathVariable("podId") UUID podId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        Course course = courseService.getCourse(courseId);
        Team team = teamService.getTeamByCourseAndUser(course, userId);

        Page<Reservation> reservations;
        if (team.getStatelessPods().stream().anyMatch(statelessPod -> statelessPod.getId().equals(podId)))
            reservations = reservationService.findReservationsForStatelessPod(
                    team.getStatelessPod(podId), team, pageable);
        else if (team.getStatefulPods().stream().anyMatch(statefulPod -> statefulPod.getId().equals(podId)))
            reservations = reservationService.findReservationsForStatefulPod(
                    team.getStatefulPod(podId), team, pageable);
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

    @Operation(
        method = "GET", summary = "Get reservation for the resource group in course in given time window",
        description = "This endpoint can be used to find reservations made for given resource group in the given course in certain time window.",
        parameters = {
            @Parameter(name = "courseId", in = ParameterIn.PATH, description = "Identifier of the course, which contains the resource group.", required = true),
            @Parameter(name = "rgId", in = ParameterIn.PATH, description = "Identifier of the resource group, which the reservations are to be fetched for.", required = true),
            @Parameter(name = "start", in = ParameterIn.QUERY, description = "Start of the time window, which the searched reservations overlap with.", required = true),
            @Parameter(name = "end", in = ParameterIn.QUERY, description = "End of the time window, which the searched reservations overlap with", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = """
                Reservations, overlapping given time window, for the given resource group were found and were sent to the client successfully."""),
            @ApiResponse(responseCode = "204", description = """
                No reservations, overlapping given time window, of the given resource group were found or currently authenticated
                user does not have privileges to fetch them.""",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = """
                Course identified with given identifier or resource group with given identifier could not be found in the database.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/courses/{courseId}/resource-groups/{rgId}/period")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<List<ReservationDto>> getRgReservationsInGivenCourse(
            @PathVariable("courseId") UUID courseId, @PathVariable("rgId") UUID rgId,
            @RequestParam(value = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(value = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Course course = courseService.getCourse(courseId);
        ResourceGroup resourceGroup = resourceGroupService.getResourceGroup(rgId);
        List<Reservation> reservations = reservationService.findRgReservations(resourceGroup, start, end);

        List<ReservationDto> listOfDtos = reservations.stream().map(reservationMapper::reservationToDto).toList();

        /* Check authorization */

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        List<User> users = course.getTeams().stream().map(Team::getUsers).flatMap(Collection::stream).toList();
        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        if ((authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) ||
                (authorities.contains(RoleConstants.STUDENT) && users.contains(user))) &&
                !reservations.isEmpty()) {
            return ResponseEntity.ok(listOfDtos);
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(
        method = "GET", summary = "Get reservation for the resource group in course in given time window, that belong to the team currently authenticated user is a part of",
        description = "This endpoint can be used to find reservations made by the team, that the currently authenticated user is a part of, for given resource group in the given course in certain time window. ",
        parameters = {
            @Parameter(name = "courseId", in = ParameterIn.PATH, description = "Identifier of the course, which contains the resource group.", required = true),
            @Parameter(name = "rgId", in = ParameterIn.PATH, description = "Identifier of the resource group, which the reservations are to be fetched for.", required = true),
            @Parameter(name = "start", in = ParameterIn.QUERY, description = "Start of the time window, which the searched reservations overlap with.", required = true),
            @Parameter(name = "end", in = ParameterIn.QUERY, description = "End of the time window, which the searched reservations overlap with", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = """
                Reservations, overlapping given time window, for the given resource group were found and were sent to the client successfully."""),
            @ApiResponse(responseCode = "204", description = """
                No reservations, overlapping given time window, of the given resource group were found or currently authenticated
                user does not have privileges to fetch them.""",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = """
                Course identified with given identifier or resource group with given identifier could not be found in the database.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('student')")
    @GetMapping(path = "/courses/{courseId}/resource-groups/{rgId}/period/own")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<List<ReservationDto>> getOwnRgReservationsInGivenCourse(
            @PathVariable("courseId") UUID courseId, @PathVariable("rgId") UUID rgId,
            @RequestParam(value = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(value = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        Course course = courseService.getCourse(courseId);
        Team team = teamService.getTeamByCourseAndUser(course, userId);
        ResourceGroup resourceGroup = resourceGroupService.getResourceGroup(rgId);

        List<Reservation> reservations = reservationService.findRgReservationsForTeam(resourceGroup, team, start, end);
        List<ReservationDto> listOfDtos = reservations.stream().map(reservationMapper::reservationToDto).toList();

        if (reservations.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDtos);
    }

    @Operation(
        method = "GET", summary = "Get reservation for the resource group pool in course in given time window",
        description = "This endpoint can be used to find reservations made for given resource group pool in the given course in given time window.",
        parameters = {
            @Parameter(name = "courseId", in = ParameterIn.PATH, description = "Identifier of the course, which contains the resource group pool.", required = true),
            @Parameter(name = "rgPoolId", in = ParameterIn.PATH, description = "Identifier of the resource group pool, which the reservations are to be fetched for.", required = true),
            @Parameter(name = "start", in = ParameterIn.QUERY, description = "Start of the time window, which the searched reservations overlap with.", required = true),
            @Parameter(name = "end", in = ParameterIn.QUERY, description = "End of the time window, which the searched reservations overlap with", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = """
                Reservation, overlapping given time window, for the given resource group pool were found and were sent to the client successfully."""),
            @ApiResponse(responseCode = "204", description = """
                No reservations, overlapping given time window, of the given resource group pool were found or currently authenticated
                user does not have privileges to fetch them.""",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = """
                Course identified with given identifier or resource group pool with given identifier could not be found in the database.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/courses/{courseId}/resource-group-pools/{rgPoolId}/period")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<List<ReservationDto>> getRgPoolReservationsInGivenCourse(
            @PathVariable("courseId") UUID courseId, @PathVariable("rgPoolId") UUID rgPoolId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Course course = courseService.getCourse(courseId);
        ResourceGroupPool resourceGroupPool = resourceGroupPoolService.getResourceGroupPool(rgPoolId);
        List<Reservation> reservations = reservationService.findRgPoolReservations(resourceGroupPool, start, end);

        List<ReservationDto> listOfDtos = reservations.stream().map(reservationMapper::reservationToDto).toList();

        /* Check authorization */

        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        List<User> users = course.getTeams().stream().map(Team::getUsers).flatMap(Collection::stream).toList();
        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        if ((authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) ||
                (authorities.contains(RoleConstants.STUDENT) && users.contains(user))) && !reservations.isEmpty()) {
            return ResponseEntity.ok(listOfDtos);
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(
        method = "GET", summary = "Get reservation for the resource group pool in course in given time window for the team currently authenticated user is a part of",
        description = "This endpoint can be used to find reservations made, by the team that the currently authenticated user belongs to, for given resource group pool in the given course in given time window.",
        parameters = {
            @Parameter(name = "courseId", in = ParameterIn.PATH, description = "Identifier of the course, which contains the resource group pool.", required = true),
            @Parameter(name = "rgPoolId", in = ParameterIn.PATH, description = "Identifier of the resource group pool, which the reservations are to be fetched for.", required = true),
            @Parameter(name = "start", in = ParameterIn.QUERY, description = "Start of the time window, which the searched reservations overlap with.", required = true),
            @Parameter(name = "end", in = ParameterIn.QUERY, description = "End of the time window, which the searched reservations overlap with", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = """
                Reservation, overlapping given time window, for the given resource group pool were found and were sent to the client successfully."""),
            @ApiResponse(responseCode = "204", description = """
                No reservations, overlapping given time window, of the given resource group pool were found or currently authenticated
                user does not have privileges to fetch them.""",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = """
                Course identified with given identifier or resource group pool with given identifier could not be found in the database.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('student')")
    @GetMapping(path = "/courses/{courseId}/resource-group-pools/{rgPoolId}/period/own")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<List<ReservationDto>> getOwnRgPoolReservationsInGivenCourse(
            @PathVariable("courseId") UUID courseId, @PathVariable("rgPoolId") UUID rgPoolId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        Course course = courseService.getCourse(courseId);
        Team team = teamService.getTeamByCourseAndUser(course, userId);
        ResourceGroupPool resourceGroupPool = resourceGroupPoolService.getResourceGroupPool(rgPoolId);

        List<Reservation> reservations = reservationService.findRgPoolReservationsForTeam(resourceGroupPool, team, start, end);
        List<ReservationDto> listOfDtos = reservations.stream().map(reservationMapper::reservationToDto).toList();

        if (reservations.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDtos);
    }

    @Operation(
        method = "GET", summary = "Get active reservations for the certain course, that the currently authenticated user is a part of.",
        description = "This endpoint can be used to find active reservations made by the team, that currently authenticated user is a part of, in the given course.",
        parameters = {
            @Parameter(name = "courseId", in = ParameterIn.PATH, description = "Identifier of the course, which the active reservations should be fetched for.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = """
                Active reservations for the team that currently authenticated user belongs to (including dividing them into pages)
                and were found and sent to the client successfully."""),
            @ApiResponse(responseCode = "204", description = """
                No reservation were found (on the given page with given size) or currently authenticated
                user does not have privileges to fetch them.""",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = """
                Course identified with given identifier or team that currently authenticated user belongs to in given course, could not be found.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('student')")
    @GetMapping(path = "/active/courses/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<PageDto<ReservationDto>> getActiveReservations(
            @PathVariable("courseId") UUID courseId, @PageableDefault Pageable pageable) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());

        Course course = courseService.getCourse(courseId);
        Team team = teamService.getTeamByCourseAndUser(course, userId);

        Page<Reservation> reservationPage = reservationService.findActiveReservations(team.getId(), pageable);

        List<ReservationDto> listOfDTOs = reservationPage.getContent().stream()
                .map(reservationMapper::reservationToDto).toList();

        PageDto<ReservationDto> outputDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(reservationPage.getNumber(), reservationPage.getNumberOfElements(),
                        reservationPage.getTotalPages(), reservationPage.getTotalElements()));

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(outputDto);
    }

    @Operation(
        method = "GET", summary = "Get historical reservations for the certain course, that the currently authenticated user is a part of.",
        description = "This endpoint can be used to find historical reservations made by the team, that currently authenticated user is a part of, in the given course.",
        parameters = {
            @Parameter(name = "courseId", in = ParameterIn.PATH, description = "Identifier of the course, which the historical reservations should be fetched for.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = """
                Historical reservations for the team that currently authenticated user belongs to (including dividing them into pages)
                and were found and sent to the client successfully."""),
            @ApiResponse(responseCode = "204", description = """
                No reservation were found (on the given page with given size) or currently authenticated
                user does not have privileges to fetch them.""",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = """
                Course identified with given identifier or team that currently authenticated user belongs to in given course, could not be found.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('student')")
    @GetMapping(path = "/historic/courses/{courseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<PageDto<ReservationDto>> getHistoricReservations(
            @PathVariable("courseId") UUID courseId, @PageableDefault Pageable pageable) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        Course course = courseService.getCourse(courseId);
        Team team = teamService.getTeamByCourseAndUser(course, userId);

        Page<Reservation> reservationPage = reservationService.findHistoricalReservations(team.getId(), pageable);

        List<ReservationDto> listOfDTOs = reservationPage.getContent().stream()
                .map(reservationMapper::reservationToDto).toList();

        PageDto<ReservationDto> outputDto = new PageDto<>(listOfDTOs,
                new PageInfoDto(reservationPage.getNumber(), reservationPage.getNumberOfElements(),
                        reservationPage.getTotalPages(), reservationPage.getTotalElements()));

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(outputDto);
    }

    @Operation(
        method = "GET", summary = "Get active reservations for the certain team.",
        description = "This endpoint can be used by teachers and administrators to find active reservations made by the team, identified with given identifier.",
        parameters = {
            @Parameter(name = "teamId", in = ParameterIn.PATH, description = "Identifier of the team, which the active reservations should be fetched for.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = """
                Active reservations for the given team (including dividing them into pages) and were found and sent to the client successfully."""),
            @ApiResponse(responseCode = "204", description = """
                No reservation were found (on the given page with given size) or currently authenticated
                user does not have privileges to fetch them.""",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = """
                Currently authenticated user could not be found in the database or team identified with given identifier.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    @GetMapping(path = "/active/teams/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<PageDto<ReservationDto>> getActiveReservationsForTeam(
            @PathVariable("teamId") UUID teamId, @PageableDefault Pageable pageable) {
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

        if ((authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) &&
                !listOfDTOs.isEmpty()) {
            return ResponseEntity.ok(outputDto);
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(
        method = "GET", summary = "Get historical reservations for the certain team.",
        description = "This endpoint can be used by teachers and administrators to find historical reservations made by the team, identified with given identifier.",
        parameters = {
            @Parameter(name = "teamId", in = ParameterIn.PATH, description = "Identifier of the team, which the historical reservations should be fetched for.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = """
                Historical reservations for the given team (including dividing them into pages) and were found and sent to the client successfully."""),
            @ApiResponse(responseCode = "204", description = """
                No reservation were found (on the given page with given size) or currently authenticated
                user does not have privileges to fetch them.""",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = """
                Currently authenticated user could not be found in the database or team identified with given identifier.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    @GetMapping(path = "/historic/teams/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ResponseEntity<PageDto<ReservationDto>> getHistoricReservationsForTeam(
            @PathVariable("teamId") UUID teamId, @PageableDefault Pageable pageable) {
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

        if ((authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) &&
                !listOfDTOs.isEmpty()) {
            return ResponseEntity.ok(outputDto);
        }

        return ResponseEntity.noContent().build();
    }

    /* Update / delete methods */

    @Operation(
        method = "POST", summary = "Finish / remove certain reservation.",
        description = """
            This endpoint can be used to finish / remove certain reservation (depending on the time of the invocation of this method).
            If this method is invoked after the start of the reservation, it marks the reservation as finished and changes its end time.
            In the latter case, it removes the reservation from the database altogether.""",
        parameters = {
            @Parameter(name = "reservationId", in = ParameterIn.PATH, description = "Identifier of the reservation, which is to be finished or removed (depending on the current time).", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "204", description = """
                Reservation with given identifier was found, and finished / removed from the database successfully. In the first case,
                reservation stays in the database, effectively consuming one of the reservations from the maxRent
                parameter of the resource group / resource group pool.""",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = """
                Reservation identified with the given identifier could not be found, or the currently authenticated user
                does not have privileges to finish / remove it.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
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

            if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                    (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) {
                reservationService.finishReservationAsTeacherOrAdmin(foundReservation);
                return ResponseEntity.noContent().build();
            } else if (authorities.contains(RoleConstants.STUDENT) && team.getUsers().contains(user)) {
                reservationService.finishReservationAsStudent(foundReservation);
                return ResponseEntity.noContent().build();
            }
        }

        throw new ReservationNotFoundException(reservationId);
    }
}
