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
VALUES (gen_random_uuid(), 'test', '', false, 0, 0);

INSERT INTO public.course(id, name, description, course_type)
VALUES (gen_random_uuid(), 'testTeamBasedCourse', 'testTeamBasedDescription', 'TEAM_BASED');

INSERT INTO public.course(id, name, description, course_type)
VALUES (gen_random_uuid(), 'testSoloCourse', 'testSoloDescription', 'SOLO');

INSERT INTO public.team(id, name, course_id, max_size, active, version)
VALUES (gen_random_uuid(), 'testTeam', (SELECT id FROM public.course WHERE name = 'testTeamBasedCourse'), '3', true, '1');

-- INSERT INTO public.team(id, name, course_id, max_size, active, version)
-- VALUES (
--     '550e8400-e29b-41d4-a716-446655440000',
--     'Sample Team A',
--     (SELECT id FROM public.course WHERE name = 'testTeamBasedCourse'),
--     4,
--     true,
--     0
-- );
--
-- INSERT INTO public.access_key(id, key_value, key_type, course_id, team_id)
-- VALUES (
--     gen_random_uuid(),
--     'TEAM1234',
--     'TEAM',
--     (SELECT id FROM public.course WHERE name = 'testTeamBasedCourse'),
--     '550e8400-e29b-41d4-a716-446655440000'
-- );