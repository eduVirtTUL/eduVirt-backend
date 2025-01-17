package pl.lodz.p.it.eduvirt.util;

public class I18n {

    /* Cluster keys*/

    public static final String CLUSTER_NOT_FOUND = "clusters.error.not.found";
    public static final String HOST_NOT_FOUND = "hosts.error.not.found";
    public static final String VM_NOT_FOUND = "vms.error.not.found";
    public static final String NETWORK_NOT_FOUND = "networks.error.not.found";
    public static final String EVENT_NOT_FOUND = "events.error.not.found";

    /* Course keys */

    public static final String COURSE_NOT_FOUND = "course.not.found";
    public static final String COURSE_INVALID_TYPE = "course.invalid.type";

    /* Resource group keys */

    public static final String RESOURCE_GROUP_POOL_NOT_FOUND = "resource.group.pool.not.found";

    /* Resource group keys */

    public static final String RESOURCE_GROUP_NOT_FOUND = "resource.group.not.found";

    /* User keys */

    public static final String USER_NOT_FOUND = "user.not.found";
    public static final String USERS_NOT_FOUND = "users.not.found.exception";

    /* Team keys */

    public static final String TEAM_NOT_FOUND = "team.not.found";
    public static final String TEAM_NOT_FOUND_IN_COURSE = "team.not.found.in.course";
    public static final String TEAM_ALREADY_EXISTS = "team.already.exists";
    public static final String TEAM_INVALID_SIZE = "team.invalid.size";
    public static final String TEAM_NOT_ACTIVE = "team.not.active";
    public static final String TEAM_USER_NOT_MEMBER = "team.user.not.member";
    public static final String TEAM_USER_ALREADY_MEMBER = "team.user.already.member";
    public static final String TEAM_USER_ALREADY_IN_COURSE = "team.user.already.in.course";
    public static final String TEAM_VALIDATION = "team.validation.exception";
    public static final String INCORRECT_TEAM_TYPE = "incorrect.team.type.exception";
    public static final String USER_ALREADY_IN_COURSE = "team.size.exception";

    /* Permission keys */

    public static final String PERMISSION_NOT_FOUND = "permission.not.found";

    /* Metric keys */

    public static final String METRIC_NOT_FOUND = "metrics.error.not.found";
    public static final String METRIC_DELETE_EXCEPTION = "metrics.error.delete.exception";

    public static final String CLUSTER_METRIC_VALUE_NOT_DEFINED = "clusterMetricValues.error.value.not.defined";
    public static final String CLUSTER_METRIC_VALUE_ALREADY_DEFINED = "clusterMetricValues.error.value.already.defined";

    public static final String COURSE_METRIC_VALUE_NOT_DEFINED = "courseLimits.error.value.not.defined";
    public static final String COURSE_METRIC_VALUE_ALREADY_DEFINED = "courseLimits.error.value.already.defined";

    /* Maintenance interval keys */

    public static final String MAINTENANCE_INTERVAL_NOT_FOUND = "maintenanceIntervals.error.not.found";
    public static final String MAINTENANCE_INTERVAL_TOO_LONG = "maintenanceIntervals.error.too.long";
    public static final String MAINTENANCE_INTERVAL_INVALID_TIME_WINDOW = "maintenanceIntervals.error.invalid.time.window";
    public static final String MAINTENANCE_INTERVAL_BEGIN_AT_PAST = "maintenanceIntervals.error.begin.at.past";
    public static final String MAINTENANCE_INTERVAL_CONFLICT = "maintenanceIntervals.error.conflict";
    public static final String MAINTENANCE_INTERVAL_ALREADY_FINISHED = "maintenanceIntervals.error.already.finished";

    /* Pod keys */
    public static final String POD_NOT_FOUND = "pod.not.found";
    public static final String POD_ALREADY_EXISTS = "pod.already.exists";
    public static final String POD_INVALID_TYPE = "pod.invalid.type";

    /* Reservation keys */

    public static final String RESERVATION_NOT_FOUND = "reservations.error.not.found";
    public static final String RESERVATION_CONFLICT = "reservations.error.conflict";
    public static final String RESERVATION_END_BEFORE_START = "reservations.error.end.before.start";
    public static final String RESERVATION_START_IN_PAST = "reservations.error.start.in.past";
    public static final String RESERVATION_TOO_SHORT = "reservations.error.too.short";

    public static final String RESERVATION_CREATION_ERROR = "reservations.error.creation.error";
    public static final String RESERVATION_DELETE_ERROR = "reservations.error.delete.error";
    public static final String RESERVATION_MAINTENANCE_INTERVAL_CONFLICT = "reservations.error.maintenance.interval.conflict";
    public static final String COURSE_RESOURCES_INSUFFICIENT = "reservations.error.course.resources.insufficient";
    public static final String CLUSTER_RESOURCES_INSUFFICIENT = "reservations.error.cluster.resources.insufficient";

    public static final String RESERVATION_MAX_LENGTH_EXCEEDED = "reservations.error.max.length.exceeded";
    public static final String POD_GRACE_PERIOD_NOT_FINISHED = "reservations.error.grace.period.not.finished";
    public static final String POD_GRACE_PERIOD_COULD_NOT_FINISH = "reservations.error.grace.period.could.not.finish";

    public static final String RESOURCE_GROUP_ALREADY_RESERVED = "reservations.error.resource.group.already.reserved";
    public static final String RESOURCE_GROUP_RESERVATION_COUNT_EXCEEDED = "reservations.error.reservation.count.exceeded";
    public static final String RESERVATION_STATUS_ALREADY_SET = "reservations.error.status.already.set";
    public static final String RESERVATION_ALREADY_FINISHED = "reservations.error.status.already.finished";

    public static final String STATEFUL_POD_NOT_ASSIGNED = "reservations.error.stateful.pod.not.assigned";
    public static final String STATELESS_POD_NOT_ASSIGNED = "reservations.error.stateless.pod.not.assigned";

    /* Vlan range keys */

    public static final String VLANS_RANGE_NOT_FOUND = "vlans.range.not.found";

    /* Vnic profile keys */

    public static final String VNIC_PROFILE_ALREADY_EXISTS = "vnic.profile.already.exists";
    public static final String VNIC_PROFILE_EDUVIRT_NOT_FOUND = "eduvirt.vnic.profile.not.found";
    public static final String VNIC_PROFILE_OVIRT_NOT_FOUND = "ovirt.vnic.profile.not.found";

    /* General keys */

    public static final String ACCESS_DENIED_ERROR = "general.error.access.denied.exception";
    public static final String OPERATION_NOT_IMPLEMENTED = "general.error.operation.not.implemented";
    public static final String CONNECTION_OPEN_ERROR = "general.error.connection.open.error";
    public static final String CONSTRAINT_VIOLATION_EXCEPTION = "general.error.constraint.violation.exception";
    public static final String OPTIMISTIC_LOCK_EXCEPTION = "general.error.optimistic.lock.exception";
    public static final String INTERNAL_SERVER_ERROR = "general.error.internal.server.error";
    public static final String PATH_NOT_FOUND = "general.error.path.not.found.error";

    /* AccessKey keys */

    public static final String ACCESS_KEY_NOT_FOUND = "access.key.not.found";
    public static final String ACCESS_KEY_ALREADY_EXISTS = "access.key.already.exists";
    public static final String ACCESS_KEY_INVALID_FORMAT = "access.key.invalid.format";
    public static final String ACCESS_KEY_INVALID_TYPE = "access.key.invalid.type";
    public static final String ACCESS_KEY_DUPLICATE = "access.key.duplicate";
}
