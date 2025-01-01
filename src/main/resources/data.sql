INSERT INTO public.metric (id, name)
VALUES ('2865efff-f8e5-4960-a0ce-fc05e98828ba', 'cpu_count'),
       ('63490da4-d0f1-4e7a-88fc-3342633accc0', 'memory_size'),
       ('1929c2b2-ba03-4180-ae90-79bd2335f2a8', 'network_count');

INSERT INTO public.metric_cluster (cluster_id, metric_id, metric_value)
VALUES ('c16d4c0e-9d42-11ef-82b3-d20f15000104', '2865efff-f8e5-4960-a0ce-fc05e98828ba', 100),
       ('c16d4c0e-9d42-11ef-82b3-d20f15000104', '63490da4-d0f1-4e7a-88fc-3342633accc0', 1073741824),
       ('9f952bd9-defc-40a0-848a-f02553e9763a', '1929c2b2-ba03-4180-ae90-79bd2335f2a8', 10);

INSERT INTO public.private_vlans_range (range_from, range_to, id)
VALUES (0, 4096, '0978f66d-050c-4c28-a376-9b8934d6167a');



INSERT INTO public.resource_group(id, name, description, stateless, version, max_rent_time)
VALUES (gen_random_uuid(), 'testStatefulRG1', '', false, 0, 120);

INSERT INTO public.resource_group(id, name, description, stateless, version, max_rent_time)
VALUES (gen_random_uuid(), 'testStatefulRG2', '', false, 0, 130);

INSERT INTO public.resource_group(id, name, description, stateless, version, max_rent_time)
VALUES (gen_random_uuid(), 'testStatelessRG1', '', true, 0, 120);

INSERT INTO public.resource_group(id, name, description, stateless, version, max_rent_time)
VALUES (gen_random_uuid(), 'testStatelessRG2', '', true, 0, 130);

INSERT INTO public.resource_group_pool(id, name, max_rent,version, grace_period)
VALUES (gen_random_uuid(), 'testStatelessRGPool', 120, 0, 120);

INSERT INTO public.resource_group_pool_resource_groups(resource_group_pool_id, resource_groups_id)
VALUES ((SELECT id FROM public.resource_group_pool WHERE name = 'testStatelessRGPool'), (SELECT id FROM public.resource_group WHERE name = 'testStatelessRG1'));

INSERT INTO public.resource_group_pool_resource_groups(resource_group_pool_id, resource_groups_id)
VALUES ((SELECT id FROM public.resource_group_pool WHERE name = 'testStatelessRGPool'), (SELECT id FROM public.resource_group WHERE name = 'testStatelessRG2'));


INSERT INTO public.course(id, name, description, course_type)
VALUES ('784cca54-f15d-43e7-b76c-f95a342fdf69', 'testTeamBasedCourse', 'testTeamBasedDescription', 'TEAM_BASED');

INSERT INTO public.course(id, name, description, course_type)
VALUES ('882357a8-a4e6-4684-1623-0edd22854877', 'testSoloCourse', 'testSoloDescription', 'SOLO');

INSERT INTO public.team(id, name, course_id, max_size, active, version)
VALUES ('eedf635f-f2c4-4f62-9401-8cbbd00632f5', 'testTeamTBC1', 
        (SELECT id FROM public.course WHERE name = 'testTeamBasedCourse'), '3', true, '1');

INSERT INTO public.team(id, name, course_id, max_size, active, version)
VALUES ('ff127467-a3d5-4f73-8512-9dcc11743f66', 'testTeamTBC2', 
        (SELECT id FROM public.course WHERE name = 'testTeamBasedCourse'), '3', true, '1');

INSERT INTO public.access_key(id, key_value)
VALUES
    ('aa11bb22-cc33-dd44-ee55-ff6677889900', 'TEAM-ALPHA-KEY-2024'),
    ('bb22cc33-dd44-ee55-ff66-778899001122', 'TEAM-BETA-KEY-2024'),
    ('cc33dd44-ee55-ff66-7788-990011223344', 'TEAM-GAMMA-KEY-2024');

INSERT INTO public.team_access_key(id, team_id)
VALUES
    ('aa11bb22-cc33-dd44-ee55-ff6677889900', 'eedf635f-f2c4-4f62-9401-8cbbd00632f5'),
    ('bb22cc33-dd44-ee55-ff66-778899001122', 'ff127467-a3d5-4f73-8512-9dcc11743f66');

INSERT INTO public.course_access_key(id, course_id)
VALUES
    ('cc33dd44-ee55-ff66-7788-990011223344', '882357a8-a4e6-4684-1623-0edd22854877');