--liquibase formatted sql
--changeset adam.czerwonka:202501211253_permissions.sql

GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.access_key TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.administrative_break TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.course TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.course_access_key TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.course_metric TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.course_resource_group TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.course_teachers TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.executor_subtask TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.executor_subtask_permission TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.executor_subtask_preconditions_check TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.executor_subtask_vm TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.executor_subtask_vnic_profile TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.executor_task TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.mail_notification TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.metric TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.metric_cluster TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.network_interface TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.pod_stateful TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.pod_stateless TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.private_vlans_range TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.reservation TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.resource_group TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.resource_group_network TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.resource_group_pool TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.resource_group_pool_resource_groups TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.team TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.team_access_key TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.team_users TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.user_roles TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.users TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.virtual_machine TO eduvirt;
GRANT SELECT, INSERT , DELETE, UPDATE ON TABLE public.vnic_profile_pool TO eduvirt;
