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
        current_timestamp + '4 minutes',
        current_timestamp + '1 minutes',
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
       ('4f362a97-4857-4453-9e85-2748634259f1', '20120dc7-aad9-46f5-a533-4539b5059a77'),
       ('11d4b89e-40ab-48a7-840e-b21b37cd8991', '20120dc7-aad9-46f5-a533-4539b5059a77');

INSERT INTO public.team (active, max_size, course_id, id, key, name)
VALUES (true, 3, null, 'fe1c320a-9aa2-4e8a-b091-ea97821afdbc', 'czesc', 'testTeam');

INSERT INTO public.user_team (team_id, user_id)
VALUES ('fe1c320a-9aa2-4e8a-b091-ea97821afdbc', 'e36f3900-c09b-48a2-b80d-8bbc35889e30');

-- Networking
INSERT INTO public.resource_group_network (id, resource_group_id, name)
VALUES ('869173fb-adbf-4626-8529-facf9d9be0d2', '20120dc7-aad9-46f5-a533-4539b5059a77', 'test_seg_1'),
       ('64296ed2-0865-4ffe-8260-408eaca53787', '20120dc7-aad9-46f5-a533-4539b5059a77', 'test_seg_2');

INSERT INTO public.vm_nic (vm_id, nic_id)
VALUES
    ('7185834c-4a19-4270-81ec-e99ab8bc5033', 'd407c755-fe05-47e6-b3f3-f1f216e61360'), -- vm1_nic3
    ('4f362a97-4857-4453-9e85-2748634259f1', 'be797cba-2049-46c2-8424-8669e371ba5f'), -- vm2_nic3
    ('4f362a97-4857-4453-9e85-2748634259f1', '7f133d0e-7192-40a6-92bb-7ead5e623547'), -- vm2_nic4
    ('11d4b89e-40ab-48a7-840e-b21b37cd8991', '7d74d7d1-4110-449d-8ee3-097c8bde037f');  -- vm3_nic1

INSERT INTO public.resource_group_network_vm_nic (resource_group_network_id, vm_nic_vm_id, vm_nic_nic_id)
VALUES
    ('869173fb-adbf-4626-8529-facf9d9be0d2', '7185834c-4a19-4270-81ec-e99ab8bc5033', 'd407c755-fe05-47e6-b3f3-f1f216e61360'),
    ('869173fb-adbf-4626-8529-facf9d9be0d2', '4f362a97-4857-4453-9e85-2748634259f1', 'be797cba-2049-46c2-8424-8669e371ba5f'),
    ('64296ed2-0865-4ffe-8260-408eaca53787', '4f362a97-4857-4453-9e85-2748634259f1', '7f133d0e-7192-40a6-92bb-7ead5e623547'),
    ('64296ed2-0865-4ffe-8260-408eaca53787', '11d4b89e-40ab-48a7-840e-b21b37cd8991', '7d74d7d1-4110-449d-8ee3-097c8bde037f');
