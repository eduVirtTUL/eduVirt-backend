package pl.lodz.p.it.eduvirt.util;

public class I18n {

    // Cluster

    public static final String CLUSTER_NOT_FOUND = "cluster.not.found.exception";

    // Metric

    public static final String METRIC_NOT_FOUND = "metric.not.found.exception";
    public static final String METRIC_VALUE_NOT_DEFINED = "metric.value.not.defined.exception";
    public static final String METRIC_VALUE_ALREADY_DEFINED = "metric.value.already.defined.exception";

    // MaintenanceInterval

    public static final String MAINTENANCE_INTERVAL_NOT_FOUND = "maintenance.interval.not.found.exception";
    public static final String MAINTENANCE_INTERVAL_INVALID_TIME_WINDOW = "maintenance.interval.invalid.time.window.exception";
    public static final String MAINTENANCE_INTERVAL_BEGIN_AT_PAST = "maintenance.interval.begin.at.past.exception";
    public static final String MAINTENANCE_INTERVAL_CONFLICT = "maintenance.interval.conflict.exception";

    // Reservation

    public static final String RESERVATION_NOT_FOUND = "reservation.not.found.exception";
    public static final String RESERVATION_CONFLICT = "reservation.conflict.exception";
    public static final String RESERVATION_END_BEFORE_START = "reservation.end.before.start.exception";
    public static final String RESERVATION_START_IN_PAST = "reservation.start.in.past.exception";

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

    public static final String OPERATION_NOT_IMPLEMENTED = "operation.not.implemented.exception";
}
