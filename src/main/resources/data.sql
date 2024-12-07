INSERT INTO public.metric (id, name)
VALUES ('2865efff-f8e5-4960-a0ce-fc05e98828ba', 'cpu_count'),
       ('63490da4-d0f1-4e7a-88fc-3342633accc0', 'memory_size'),
       ('1929c2b2-ba03-4180-ae90-79bd2335f2a8', 'network_count');

INSERT INTO public.metric_cluster (cluster_id, metric_id, metric_value)
VALUES ('c16d4c0e-9d42-11ef-82b3-d20f15000104', '2865efff-f8e5-4960-a0ce-fc05e98828ba', 100),
       ('c16d4c0e-9d42-11ef-82b3-d20f15000104', '63490da4-d0f1-4e7a-88fc-3342633accc0', 1073741824),
       ('9f952bd9-defc-40a0-848a-f02553e9763a', '1929c2b2-ba03-4180-ae90-79bd2335f2a8', 10);

INSERT INTO public.resource_group(id, name, description, stateless, version, max_rent_time)
VALUES (gen_random_uuid(), 'test', '', false, 0, 0);

INSERT INTO public.private_vlans_range (range_from, range_to, id) VALUES (0, 4096, '0978f66d-050c-4c28-a376-9b8934d6167a');

INSERT INTO public.reservation (automatic_startup,
                                _created_at,
                                _updated_at,
                                reservation_end,
                                reservation_start,
                                version,
                                created_by,
                                id,
                                rg_id,
                                team_id,
                                updated_by)
VALUES (true,
        current_timestamp,
        current_timestamp,
        current_timestamp,
        current_timestamp,
        0,
        '00000000-0000-0000-0000-000000000000',
        '52998fd2-30e2-4b04-9ba9-8113a5123f86',
        '20120dc7-aad9-46f5-a533-4539b5059a77',
        'fe1c320a-9aa2-4e8a-b091-ea97821afdbc',
        '00000000-0000-0000-0000-000000000000');

INSERT INTO public.resource_group (max_rent_time,
                                   stateless,
                                   _created_at,
                                   _updated_at,
                                   version,
                                   created_by,
                                   id,
                                   updated_by,
                                   name,
                                   description)
VALUES (2,
        true,
        current_timestamp,
        current_timestamp,
        0,
        '00000000-0000-0000-0000-000000000000',
        '20120dc7-aad9-46f5-a533-4539b5059a77',
        '00000000-0000-0000-0000-000000000000',
        'rgTest',
        'RG for testing purposes');

INSERT INTO public.virtual_machine (id, resource_group_id)
VALUES ('7185834c-4a19-4270-81ec-e99ab8bc5033', '20120dc7-aad9-46f5-a533-4539b5059a77'),
       ('4f362a97-4857-4453-9e85-2748634259f1', '20120dc7-aad9-46f5-a533-4539b5059a77');

INSERT INTO public.team (active, max_size, course_id, id, key, name)
VALUES (true, 3, null, 'fe1c320a-9aa2-4e8a-b091-ea97821afdbc', 'czesc', 'testTeam');

INSERT INTO public.user_team (team_id, user_id)
VALUES ('fe1c320a-9aa2-4e8a-b091-ea97821afdbc', 'e36f3900-c09b-48a2-b80d-8bbc35889e30')