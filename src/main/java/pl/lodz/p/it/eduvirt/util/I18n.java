package pl.lodz.p.it.eduvirt.util;

public class I18n {

    /* Cluster keys*/

    public static final String CLUSTER_NOT_FOUND = "cluster.not.found";

    /* Course keys */

    public static final String COURSE_NOT_FOUND = "course.not.found";

    /* Resource group keys */

    public static final String RESOURCE_GROUP_POOL_NOT_FOUND = "resource.group.pool.not.found";

    /* Resource group keys */

    public static final String RESOURCE_GROUP_NOT_FOUND = "resource.group.not.found";

    /* User keys */

    public static final String USER_NOT_FOUND = "user.not.found";

    /* Permission keys */

    public static final String PERMISSION_NOT_FOUND = "permission.not.found";

    /* Metric keys */

    public static final String METRIC_NOT_FOUND = "metric.not.found";
    public static final String METRIC_VALUE_NOT_DEFINED = "metric.value.not.defined";
    public static final String METRIC_VALUE_ALREADY_DEFINED = "metric.value.already.defined";

    /* Maintenance interval keys */

    public static final String MAINTENANCE_INTERVAL_NOT_FOUND = "maintenance.interval.not.found";
    public static final String MAINTENANCE_INTERVAL_INVALID_TIME_WINDOW = "maintenance.interval.invalid.time.window";
    public static final String MAINTENANCE_INTERVAL_BEGIN_AT_PAST = "maintenance.interval.begin.at.past";
    public static final String MAINTENANCE_INTERVAL_CONFLICT = "maintenance.interval.conflict";
    // Pod

    public static final String POD_NOT_FOUND = "pod.not.found.exception";
    public static final String POD_STATELESS_CONFLICT = "pod.conflict.exception";
    public static final String POD_ALREADY_EXISTS = "pod.already.exists.exception";

    // Team

    public static final String TEAM_NOT_FOUND = "team.not.found.exception";
    public static final String USER_ALREADY_IN_TEAM = "user.already.in.team.exception";
    public static final String USER_ALREADY_IN_COURSE = "user.already.in.course.exception";
    public static final String TEAM_ALREADY_EXISTS = "team.already.exists.exception";
    public static final String INCORRECT_TEAM_SIZE = "incorrect.team.size.exception";
    public static final String TEAM_VALIDATION = "team.validation.exception";
    public static final String INCORRECT_TEAM_TYPE = "incorrect.team.type.exception";

    // AccessKey

    public static final String DUPLICATE_KEY_VALUE = "duplicate.key.value.exception";
    public static final String ACCESS_KEY_NOT_FOUND = "access.key.not.found.exception";
    public static final String INCORRECT_KEY_TYPE = "incorrect.key.type.exception";

    // User

    public static final String USER_NOT_FOUND = "user.not.found.exception";
    public static final String USERS_NOT_FOUND = "users.not.found.exception";

    // General

    /* Reservation keys */

    public static final String RESERVATION_NOT_FOUND = "reservation.not.found";
    public static final String RESERVATION_CONFLICT = "reservation.conflict";
    public static final String RESERVATION_END_BEFORE_START = "reservation.end.before.start";
    public static final String RESERVATION_START_IN_PAST = "reservation.start.in.past";

    public static final String RESERVATION_CREATION_ERROR = "reservation.creation.error";
    public static final String RESERVATION_MAINTENANCE_INTERVAL_CONFLICT = "reservation.period.maintenance.interval.conflict";
    public static final String COURSE_RESOURCES_INSUFFICIENT = "reservation.course.resources.insufficient";
    public static final String CLUSTER_RESOURCES_INSUFFICIENT = "reservation.cluster.resources.insufficient";

    /* Vlan range keys */

    public static final String VLANS_RANGE_NOT_FOUND = "vlans.range.not.found";

    /* Vnic profile keys */

    public static final String VNIC_PROFILE_ALREADY_EXISTS = "vnic.profile.already.exists";
    public static final String VNIC_PROFILE_EDUVIRT_NOT_FOUND = "eduvirt.vnic.profile.not.found";
    public static final String VNIC_PROFILE_OVIRT_NOT_FOUND = "ovirt.vnic.profile.not.found";

    /* General keys */

    public static final String OPERATION_NOT_IMPLEMENTED = "operation.not.implemented";
    public static final String CONNECTION_OPEN_ERROR = "connection.open.error";
}
