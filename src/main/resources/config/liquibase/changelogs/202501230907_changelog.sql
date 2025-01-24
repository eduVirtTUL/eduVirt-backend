alter table public.metric_cluster
    rename to cluster_metric;

alter table public.executor_subtask
    drop constraint executor_subtask_type_check;

alter table public.executor_subtask
    add constraint executor_subtask_type_check check ((type)::text = ANY
                                                      (ARRAY [('CHECK_VMS_STATUSES'::character varying)::text, ('CHECK_RG_IN_USE'::character varying)::text, ('ASSIGN_VNIC_PROFILE'::character varying)::text, ('REMOVE_VNIC_PROFILE'::character varying)::text, ('START_VM'::character varying)::text, ('SHUTDOWN_VM'::character varying)::text, ('POWER_OFF'::character varying)::text, ('REBOOT_VM'::character varying)::text, ('ASSIGN_PERMISSION'::character varying)::text, ('REVOKE_PERMISSION'::character varying)::text]));
