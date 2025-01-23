--liquibase formatted sql
--changeset adam.czerwonka:202501211157_initial_schema.sql

create table if not exists public.access_key
(
    id        uuid         not null
        primary key,
    key_value varchar(255) not null
        unique
);

alter table public.access_key
    owner to eduvirtadmin;

create table if not exists public.administrative_break
(
    begin_at    timestamp(6) not null,
    end_at      timestamp(6) not null,
    version     bigint       not null,
    cluster_id  uuid,
    id          uuid         not null
        primary key,
    type        varchar(16)  not null
        constraint administrative_break_type_check
            check ((type)::text = ANY ((ARRAY ['SYSTEM'::character varying, 'CLUSTER'::character varying])::text[])),
    cause       varchar(128) not null,
    description varchar(256)
);

alter table public.administrative_break
    owner to eduvirtadmin;

create table if not exists public.course
(
    version       bigint       not null,
    cluster_id    uuid         not null,
    id            uuid         not null
        primary key,
    name          varchar(100) not null
        unique,
    description   varchar(1000),
    external_link varchar(1000),
    course_type   varchar(255) not null
        constraint course_course_type_check
            check ((course_type)::text = ANY
                   ((ARRAY ['TEAM_BASED'::character varying, 'SOLO'::character varying])::text[]))
);

alter table public.course
    owner to eduvirtadmin;

create table if not exists public.course_access_key
(
    course_id uuid not null
        constraint fkg22cg9ov10phyomlwnkx3hrqj
            references public.course,
    id        uuid not null
        primary key
        constraint fklyki578iue03l7gf5pwr7un12
            references public.access_key
);

alter table public.course_access_key
    owner to eduvirtadmin;

create table if not exists public.metric
(
    id       uuid        not null
        primary key,
    category varchar(32) not null
        constraint metric_category_check
            check ((category)::text = ANY
                   ((ARRAY ['MEMORY'::character varying, 'COUNTABLE'::character varying])::text[])),
    name     varchar(64) not null
        unique
);

alter table public.metric
    owner to eduvirtadmin;

create table if not exists public.course_metric
(
    value     double precision not null,
    course_id uuid             not null
        constraint fkdpf1ykyya67ejc2lr3rq7si27
            references public.course,
    metric_id uuid             not null
        constraint fk5r8atdh6m9ia2l9miitik81rx
            references public.metric,
    primary key (course_id, metric_id)
);

alter table public.course_metric
    owner to eduvirtadmin;

create table if not exists public.metric_cluster
(
    metric_value double precision
        constraint metric_cluster_metric_value_check
            check (metric_value <= ('9223372036854775807'::bigint)::double precision),
    cluster_id   uuid not null,
    id           uuid not null
        primary key,
    metric_id    uuid not null
        constraint cluster_metric_metric_id_fk
            references public.metric,
    constraint cluster_metric_cluster_id_unique
        unique (cluster_id, metric_id)
);

alter table public.metric_cluster
    owner to eduvirtadmin;

create index if not exists cluster_metric_metric_id_idx
    on public.metric_cluster (metric_id);

create table if not exists public.private_vlans_range
(
    range_from integer not null,
    range_to   integer not null,
    id         uuid    not null
        primary key
);

alter table public.private_vlans_range
    owner to eduvirtadmin;

create table if not exists public.resource_group
(
    max_rent_time integer      not null,
    stateless     boolean      not null,
    _created_at   timestamp(6),
    _updated_at   timestamp(6),
    version       bigint       not null,
    created_by    uuid,
    id            uuid         not null
        primary key,
    updated_by    uuid,
    name          varchar(100) not null,
    description   varchar(1000)
);

alter table public.resource_group
    owner to eduvirtadmin;

create table if not exists public.course_resource_group
(
    course_id         uuid not null
        constraint fk8ep49c6crwmoh9fihflstaryk
            references public.course,
    resource_group_id uuid not null
        unique
        constraint fkjmyjh3awtwdg5cqlpt3w0gttn
            references public.resource_group
);

alter table public.course_resource_group
    owner to eduvirtadmin;

create table if not exists public.resource_group_network
(
    id                uuid not null
        primary key,
    resource_group_id uuid
        constraint fk56p68dbxnphkbp63hfjeb67qh
            references public.resource_group,
    name              varchar(255)
);

alter table public.resource_group_network
    owner to eduvirtadmin;

create table if not exists public.resource_group_pool
(
    grace_period  integer      not null,
    max_rent      integer      not null,
    max_rent_time integer      not null,
    _created_at   timestamp(6),
    _updated_at   timestamp(6),
    version       bigint       not null,
    course_id     uuid
        constraint fka0s5kjeut8ww6erwt57hmevp2
            references public.course,
    created_by    uuid,
    id            uuid         not null
        primary key,
    updated_by    uuid,
    name          varchar(100) not null,
    description   varchar(1000),
    unique (name, course_id)
);

alter table public.resource_group_pool
    owner to eduvirtadmin;

create table if not exists public.resource_group_pool_resource_groups
(
    resource_group_pool_id uuid not null
        constraint fkp8jr120yc8lbps07mibbe5ro0
            references public.resource_group_pool,
    resource_groups_id     uuid not null
        unique
        constraint fkk3qawpc3kavvegn0ltkhe4ue2
            references public.resource_group
);

alter table public.resource_group_pool_resource_groups
    owner to eduvirtadmin;

create table if not exists public.team
(
    active    boolean      not null,
    max_size  integer      not null,
    version   bigint       not null,
    course_id uuid         not null
        constraint fkrdbahenwatuua698jkpnfufta
            references public.course,
    id        uuid         not null
        primary key,
    name      varchar(255) not null,
    constraint team_name_course_id_unique
        unique (name, course_id)
);

alter table public.team
    owner to eduvirtadmin;

create table if not exists public.pod_stateful
(
    max_rent  integer not null,
    course_id uuid    not null
        constraint pod_stateful_course_id_fk
            references public.course,
    id        uuid    not null
        primary key,
    rg_id     uuid    not null
        constraint pod_stateful_rg_id_fk
            references public.resource_group,
    team_id   uuid    not null
        constraint pod_stateful_team_id_fk
            references public.team
);

alter table public.pod_stateful
    owner to eduvirtadmin;

create table if not exists public.pod_stateless
(
    course_id uuid not null
        constraint pod_stateless_course_id_fk
            references public.course,
    id        uuid not null
        primary key,
    rgp_id    uuid not null
        constraint pod_stateless_rg_id_fk
            references public.resource_group_pool,
    team_id   uuid not null
        constraint pod_stateless_team_id_fk
            references public.team
);

alter table public.pod_stateless
    owner to eduvirtadmin;

create table if not exists public.reservation
(
    automatic_startup boolean      not null,
    notification_time integer      not null,
    _created_at       timestamp(6),
    _updated_at       timestamp(6),
    reservation_end   timestamp(6) not null,
    reservation_start timestamp(6) not null,
    version           bigint       not null,
    created_by        uuid,
    id                uuid         not null
        primary key,
    rg_id             uuid         not null
        constraint reservation_rg_id_fk
            references public.resource_group,
    team_id           uuid         not null
        constraint reservation_team_id_fk
            references public.team,
    updated_by        uuid,
    status            varchar(255) not null
        constraint reservation_status_check
            check ((status)::text = ANY
                   ((ARRAY ['PENDING'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying])::text[]))
);

alter table public.reservation
    owner to eduvirtadmin;

create table if not exists public.executor_task
(
    _created_at    timestamp(6),
    _updated_at    timestamp(6),
    version        bigint       not null,
    id             uuid         not null
        primary key,
    reservation_id uuid         not null
        constraint reservation_id_fk
            references public.reservation,
    description    varchar(200),
    status         varchar(255) not null
        constraint executor_task_status_check
            check ((status)::text = ANY
                   ((ARRAY ['SUCCESSFUL'::character varying, 'FAILED'::character varying, 'IN_PROGRESS'::character varying])::text[])),
    type           varchar(255) not null
        constraint executor_task_type_check
            check ((type)::text = ANY
                   ((ARRAY ['POD_INIT'::character varying, 'POD_DESTRUCT'::character varying, 'END_RESERVATION'::character varying])::text[]))
);

alter table public.executor_task
    owner to eduvirtadmin;

create table if not exists public.executor_subtask
(
    successful  boolean,
    _created_at timestamp(6),
    _updated_at timestamp(6),
    version     bigint      not null,
    id          uuid        not null
        primary key,
    task_id     uuid        not null
        constraint task_id_fk
            references public.executor_task,
    vm_id       uuid,
    kind        varchar(31) not null,
    description varchar(200),
    type        varchar(255)
        constraint executor_subtask_type_check
            check ((type)::text = ANY
                   ((ARRAY ['CHECK_VMS_STATUSES'::character varying, 'ASSIGN_VNIC_PROFILE'::character varying, 'REMOVE_VNIC_PROFILE'::character varying, 'START_VM'::character varying, 'SHUTDOWN_VM'::character varying, 'POWER_OFF'::character varying, 'REBOOT_VM'::character varying, 'ASSIGN_PERMISSION'::character varying, 'REVOKE_PERMISSION'::character varying])::text[]))
);

alter table public.executor_subtask
    owner to eduvirtadmin;

create table if not exists public.executor_subtask_permission
(
    id uuid not null
        primary key
        constraint executor_subtask_permission_fk
            references public.executor_subtask
);

alter table public.executor_subtask_permission
    owner to eduvirtadmin;

create table if not exists public.executor_subtask_preconditions_check
(
    id uuid not null
        primary key
        constraint executor_subtask_preconditions_check_fk
            references public.executor_subtask
);

alter table public.executor_subtask_preconditions_check
    owner to eduvirtadmin;

create table if not exists public.executor_subtask_vm
(
    id uuid not null
        primary key
        constraint executor_subtask_vm_fk
            references public.executor_subtask
);

alter table public.executor_subtask_vm
    owner to eduvirtadmin;

create table if not exists public.executor_subtask_vnic_profile
(
    id              uuid not null
        primary key
        constraint executor_subtask_vnic_profile_fk
            references public.executor_subtask,
    nic_id          uuid,
    vnic_profile_id uuid
);

alter table public.executor_subtask_vnic_profile
    owner to eduvirtadmin;

create index if not exists executor_task_reservation_id_idx
    on public.executor_task (reservation_id);

create table if not exists public.mail_notification
(
    _created_at    timestamp(6),
    id             uuid         not null
        primary key,
    reservation_id uuid         not null
        constraint mail_notification_reservation_id_fk
            references public.reservation,
    type           varchar(255) not null
        constraint mail_notification_type_check
            check ((type)::text = ANY
                   ((ARRAY ['RESERVATION_START'::character varying, 'RESERVATION_END'::character varying])::text[])),
    constraint reservation_notification_type_unique
        unique (reservation_id, type)
);

alter table public.mail_notification
    owner to eduvirtadmin;

create index if not exists mail_notification_reservation_id_idx
    on public.mail_notification (reservation_id);

create index if not exists reservation_rg_id_idx
    on public.reservation (rg_id);

create index if not exists reservation_team_id_idx
    on public.reservation (team_id);

create index if not exists team_course_id_idx
    on public.team (course_id);

create table if not exists public.team_access_key
(
    id      uuid not null
        primary key
        constraint fk518uioh1ffn3v2cfm6wwddbvo
            references public.access_key,
    team_id uuid not null
        unique
        constraint fkq20d96dbpedh41ogur5doitnl
            references public.team
);

alter table public.team_access_key
    owner to eduvirtadmin;

create table if not exists public.users
(
    ovirt_id   uuid         not null
        unique,
    user_id    uuid         not null
        primary key,
    email      varchar(255) not null
        unique,
    first_name varchar(255),
    language   varchar(255),
    last_name  varchar(255),
    time_zone  varchar(255),
    user_name  varchar(255) not null
);

alter table public.users
    owner to eduvirtadmin;

create table if not exists public.course_teachers
(
    course_id        uuid not null
        constraint fklmee8ivi6ymoe34wgwknlurpb
            references public.course,
    teachers_user_id uuid not null
        constraint fkbitvwux46973c2a09i6rvriug
            references public.users
);

alter table public.course_teachers
    owner to eduvirtadmin;

create table if not exists public.team_users
(
    team_id uuid not null
        constraint fkrkg4q3rf6sjc1mpnw9enibvh3
            references public.team,
    user_id uuid not null
        constraint fkq5kde75x24rqmpev4dsp1g2qv
            references public.users
);

alter table public.team_users
    owner to eduvirtadmin;

create table if not exists public.user_roles
(
    user_id uuid not null
        constraint fkhfh9dx7w3ubf1co1vdev94g3f
            references public.users,
    role    varchar(255)
);

alter table public.user_roles
    owner to eduvirtadmin;

create table if not exists public.virtual_machine
(
    hidden            boolean not null,
    version           bigint  not null,
    id                uuid    not null
        primary key,
    resource_group_id uuid
        constraint fkq6ya7s5mpg86hsc9b9df7qnt0
            references public.resource_group
);

alter table public.virtual_machine
    owner to eduvirtadmin;

create table if not exists public.network_interface
(
    id                        uuid not null
        primary key,
    resource_group_network_id uuid
        constraint fk3fote2fcujyxfvkppbxbxg1eq
            references public.resource_group_network,
    virtual_machine_id        uuid
        constraint fk84l82rohmq0uub5el7q2specm
            references public.virtual_machine
);

alter table public.network_interface
    owner to eduvirtadmin;

create table if not exists public.vnic_profile_pool
(
    in_use      boolean not null,
    vlan_id     integer not null
        unique,
    _created_at timestamp(6),
    version     bigint  not null,
    created_by  uuid,
    id          uuid    not null
        primary key
);

alter table public.vnic_profile_pool
    owner to eduvirtadmin;

