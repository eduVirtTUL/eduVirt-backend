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

-- --MOCKING data
--
-- INSERT INTO public.reservation (automatic_startup,
--                                 _created_at,
--                                 _updated_at,
--                                 reservation_end,
--                                 reservation_start,
--                                 version,
--                                 created_by,
--                                 id,
--                                 rg_id,
--                                 team_id,
--                                 updated_by)
-- VALUES (true,
--         current_timestamp,
--         current_timestamp,
--         current_timestamp + '4 minutes',
--         current_timestamp + '1 minutes',
--         0,
--         '00000000-0000-0000-0000-000000000000',
--         '52998fd2-30e2-4b04-9ba9-8113a5123f86',
--         '20120dc7-aad9-46f5-a533-4539b5059a77',
--         'fe1c320a-9aa2-4e8a-b091-ea97821afdbc',
--         '00000000-0000-0000-0000-000000000000');
--
-- INSERT INTO public.resource_group (max_rent_time,
--                                    stateless,
--                                    _created_at,
--                                    _updated_at,
--                                    version,
--                                    created_by,
--                                    id,
--                                    updated_by,
--                                    name,
--                                    description)
-- VALUES (2,
--         true,
--         current_timestamp,
--         current_timestamp,
--         0,
--         '00000000-0000-0000-0000-000000000000',
--         '20120dc7-aad9-46f5-a533-4539b5059a77',
--         '00000000-0000-0000-0000-000000000000',
--         'rgTest',
--         'RG for testing purposes');

-- MANUAL ADDING oVIRT IDs

-- vm1        = '4181e5f8-7cc6-4021-a653-b7c59f5ef16e';
-- vm2        = 'ef44305e-adc6-4329-97ba-78ebaa30eb98';
-- vm3        = '943584ee-66fb-406e-86f2-648156d78138';

-- vm1_nic3   = 'df660c1a-1d62-4098-a8fc-dbccea75f3a9'
-- vm2_nic3   = 'a79fd50a-4378-4cfe-a6c5-64e958a8dbfe'
-- vm2_nic4   = 'cc8243ee-25f5-4934-b4b1-0fde2a1a0453'
-- vm3_nic1   = '52735aec-f4af-40ea-8f60-fb6068f9fe60'

-- user1      = 'd8e03c79-59ed-4916-a684-fe66557eae65'

-- vnic_prof1 = 'c95a2982-fba7-486e-89da-9a065e8b5396'
-- vnic_prof2 = 'ffddbc7f-8b30-4555-a610-1968d853a278'

-- INSERT INTO public.virtual_machine (id, resource_group_id)
-- VALUES ('4181e5f8-7cc6-4021-a653-b7c59f5ef16e', '20120dc7-aad9-46f5-a533-4539b5059a77'),
--        ('ef44305e-adc6-4329-97ba-78ebaa30eb98', '20120dc7-aad9-46f5-a533-4539b5059a77'),
--        ('943584ee-66fb-406e-86f2-648156d78138', '20120dc7-aad9-46f5-a533-4539b5059a77');
--
-- INSERT INTO public.team (active, max_size, course_id, id, key, name)
-- VALUES (true, 3, null, 'fe1c320a-9aa2-4e8a-b091-ea97821afdbc', 'czesc', 'testTeam');
--
-- INSERT INTO public.user_team (team_id, user_id)
-- VALUES ('fe1c320a-9aa2-4e8a-b091-ea97821afdbc', 'd8e03c79-59ed-4916-a684-fe66557eae65');
--
-- -- Networking
-- INSERT INTO public.resource_group_network (id, resource_group_id, name)
-- VALUES ('869173fb-adbf-4626-8529-facf9d9be0d2', '20120dc7-aad9-46f5-a533-4539b5059a77', 'test_seg_1'),
--        ('64296ed2-0865-4ffe-8260-408eaca53787', '20120dc7-aad9-46f5-a533-4539b5059a77', 'test_seg_2');
--
-- INSERT INTO public.vm_nic (vm_id, nic_id)
-- VALUES
--     ('4181e5f8-7cc6-4021-a653-b7c59f5ef16e', 'df660c1a-1d62-4098-a8fc-dbccea75f3a9'), -- vm1_nic3
--     ('ef44305e-adc6-4329-97ba-78ebaa30eb98', 'a79fd50a-4378-4cfe-a6c5-64e958a8dbfe'), -- vm2_nic3
--     ('ef44305e-adc6-4329-97ba-78ebaa30eb98', 'cc8243ee-25f5-4934-b4b1-0fde2a1a0453'), -- vm2_nic4
--     ('943584ee-66fb-406e-86f2-648156d78138', '52735aec-f4af-40ea-8f60-fb6068f9fe60'); -- vm3_nic1
--
-- INSERT INTO public.resource_group_network_vm_nic (resource_group_network_id, vm_nic_vm_id, vm_nic_nic_id)
-- VALUES
--     ('869173fb-adbf-4626-8529-facf9d9be0d2', '4181e5f8-7cc6-4021-a653-b7c59f5ef16e', 'df660c1a-1d62-4098-a8fc-dbccea75f3a9'),
--     ('869173fb-adbf-4626-8529-facf9d9be0d2', 'ef44305e-adc6-4329-97ba-78ebaa30eb98', 'a79fd50a-4378-4cfe-a6c5-64e958a8dbfe'),
--     ('64296ed2-0865-4ffe-8260-408eaca53787', 'ef44305e-adc6-4329-97ba-78ebaa30eb98', 'cc8243ee-25f5-4934-b4b1-0fde2a1a0453'),
--     ('64296ed2-0865-4ffe-8260-408eaca53787', '943584ee-66fb-406e-86f2-648156d78138', '52735aec-f4af-40ea-8f60-fb6068f9fe60');
--
-- -- Vnic profile pool
-- INSERT INTO public.vnic_profile_pool (in_use, vlan_id, _created_at, version, created_by, id)
-- VALUES (false, 2500, current_timestamp, 0, null, 'c95a2982-fba7-486e-89da-9a065e8b5396'),
--        (false, 2550, current_timestamp, 0, null, 'ffddbc7f-8b30-4555-a610-1968d853a278');
