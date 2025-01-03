INSERT INTO public.metric (id, name)
VALUES ('2865efff-f8e5-4960-a0ce-fc05e98828ba', 'cpu_count'),
       ('63490da4-d0f1-4e7a-88fc-3342633accc0', 'memory_size'),
       ('1929c2b2-ba03-4180-ae90-79bd2335f2a8', 'network_count');

INSERT INTO public.metric_cluster (id, cluster_id, metric_id, metric_value)
VALUES (gen_random_uuid(),'a5097950-d0c6-4d65-8b2e-4768809ad37a', '2865efff-f8e5-4960-a0ce-fc05e98828ba', 100),
       (gen_random_uuid(), 'a5097950-d0c6-4d65-8b2e-4768809ad37a', '63490da4-d0f1-4e7a-88fc-3342633accc0', 1073741824),
       (gen_random_uuid(), 'a5097950-d0c6-4d65-8b2e-4768809ad37a', '1929c2b2-ba03-4180-ae90-79bd2335f2a8', 10);

INSERT INTO public.private_vlans_range (range_from, range_to, id)
VALUES (0, 4096, '0978f66d-050c-4c28-a376-9b8934d6167a');

INSERT INTO public.resource_group(id, name, description, stateless, version, max_rent_time)
VALUES (gen_random_uuid(), 'test', '', false, 0, 0);

------------------------------------------
--- Sample data for reservation module ---
------------------------------------------

INSERT INTO public.course (id, name, description, team_based, cluster_id)
VALUES ('b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'Systemy operacyjne', 'Operating Systems', false, 'a5097950-d0c6-4d65-8b2e-4768809ad37a'),
       ('a7556146-23a6-4936-903c-c337c794a8c7', 'Infrastruktury środowisk rozwojowych i produkcyjnych', 'Infrastructures of Development and Production Environments', true, 'a5097950-d0c6-4d65-8b2e-4768809ad37a'),
       ('e485ded6-c166-45f6-a924-13ce44666f7a', 'Sieciowe systemy baz danych', 'Network Database Systems', true, 'a5097950-d0c6-4d65-8b2e-4768809ad37a'),
       ('1decd050-1328-4eca-b2de-84793a8474c2', 'Techniki utrzymania aplikacji', 'Techniques of Application Maintenance', true, 'a5097950-d0c6-4d65-8b2e-4768809ad37a');

--------------------------
--- Systemy operacyjne ---
--------------------------

--- Teams ---

INSERT INTO public.team (id, name, active, max_size, course_id, key)
VALUES ('72fc908f-5d02-4c81-91a3-2bccaa627946', 'SO-Student001', false, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'SO-Key001'),
       ('cb97b2f0-646f-4876-906f-0fd44cf6d63a', 'SO-Student002', false, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'SO-Key002'),
       ('12ca7f40-c596-44ae-a8d2-671843ecc9e5', 'SO-Student003', false, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'SO-Key003'),
       ('83698296-0b9f-40ae-a8e7-2caec8068e1e', 'SO-Student004', false, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'SO-Key004'),
       ('b4232713-05fa-411a-a40d-9a3e15f13fa0', 'SO-Student005', false, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'SO-Key005'),
       ('d82a711e-a308-4127-95f9-2c38851c3e71', 'SO-Student006', false, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'SO-Key006'),
       ('89f530ca-f5a0-40cd-b00a-1997c96dd9d3', 'SO-Student007', false, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'SO-Key007'),
       ('bbf54d7d-3ccb-4cea-9726-baef3c58e2dd', 'SO-Student008', false, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'SO-Key008'),
       ('c5aaa2ae-4ba7-4b87-86b5-52643d92de47', 'SO-Student009', false, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'SO-Key009'),
       ('33e44d4e-c937-4d13-a060-f41a4c3c05fb', 'SO-Student010', false, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'SO-Key010');


INSERT INTO public.user_team (team_id, user_id)
VALUES ('72fc908f-5d02-4c81-91a3-2bccaa627946', gen_random_uuid()),
       ('cb97b2f0-646f-4876-906f-0fd44cf6d63a', gen_random_uuid()),
       ('12ca7f40-c596-44ae-a8d2-671843ecc9e5', gen_random_uuid()),
       ('83698296-0b9f-40ae-a8e7-2caec8068e1e', gen_random_uuid()),
       ('b4232713-05fa-411a-a40d-9a3e15f13fa0', gen_random_uuid()),
       ('d82a711e-a308-4127-95f9-2c38851c3e71', gen_random_uuid()),
       ('89f530ca-f5a0-40cd-b00a-1997c96dd9d3', gen_random_uuid()),
       ('bbf54d7d-3ccb-4cea-9726-baef3c58e2dd', gen_random_uuid()),
       ('c5aaa2ae-4ba7-4b87-86b5-52643d92de47', gen_random_uuid()),
       ('33e44d4e-c937-4d13-a060-f41a4c3c05fb', gen_random_uuid());

--- Resource groups ---

INSERT INTO public.resource_group (id, version, name, description, stateless, max_rent_time)
VALUES ('e028a269-9890-4b02-81d9-b477ea7f552a', 0, 'SO-RG01', '', true, 3),
       ('023192a9-9a7e-4861-95e7-a77ac29ca039', 0, 'SO-RG02', '', true, 3),
       ('e205cf0a-6966-4cc0-8237-d2424de35e22', 0, 'SO-RG03', '', true, 3),
       ('2ef59c08-2ec5-40cc-8730-53723d3abda4', 0, 'SO-RG04', '', true, 3);

--- Resource group pools ---

INSERT INTO public.resource_group_pool (id, version, name, grace_period, max_rent, course_id)
VALUES ('4778c01d-4962-4cbd-a653-c90aea9dbddf', 0, 'SysOp-Pool', 6, 3, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9');

INSERT INTO public.resource_group_pool_resource_groups (resource_group_pool_id, resource_groups_id)
VALUES ('4778c01d-4962-4cbd-a653-c90aea9dbddf', 'e028a269-9890-4b02-81d9-b477ea7f552a'),
       ('4778c01d-4962-4cbd-a653-c90aea9dbddf', '023192a9-9a7e-4861-95e7-a77ac29ca039'),
       ('4778c01d-4962-4cbd-a653-c90aea9dbddf', 'e205cf0a-6966-4cc0-8237-d2424de35e22'),
       ('4778c01d-4962-4cbd-a653-c90aea9dbddf', '2ef59c08-2ec5-40cc-8730-53723d3abda4');

------------------------------------------------------------
--- Infrastruktury środowisk rozwojowych i produkcyjnych ---
------------------------------------------------------------

--- Teams ---

INSERT INTO public.team (id, name, key, active, max_size, course_id)
VALUES ('f15e7fe3-60a6-4d2c-a124-ad763f6869e2', 'JakubP', 'ISRP-AccessKey01', false, 5, 'a7556146-23a6-4936-903c-c337c794a8c7'),
       ('5ef19d54-c429-499d-9654-ac052d83f3e7', 'AdamC', 'ISRP-AccessKey02', false, 5, 'a7556146-23a6-4936-903c-c337c794a8c7'),
       ('64da3d79-52be-4936-97e3-b88597bac8b9', 'PiotrK', 'ISRP-AccessKey03', false, 5, 'a7556146-23a6-4936-903c-c337c794a8c7'),
       ('60deabdf-ba7d-482a-b6a5-26e440850496', 'KamilM', 'ISRP-AccessKey04', false, 5, 'a7556146-23a6-4936-903c-c337c794a8c7');

INSERT INTO public.user_team (team_id, user_id)
VALUES ('f15e7fe3-60a6-4d2c-a124-ad763f6869e2', gen_random_uuid()),
       ('f15e7fe3-60a6-4d2c-a124-ad763f6869e2', gen_random_uuid()),
       ('f15e7fe3-60a6-4d2c-a124-ad763f6869e2', gen_random_uuid()),
       ('f15e7fe3-60a6-4d2c-a124-ad763f6869e2', gen_random_uuid()),
       ('5ef19d54-c429-499d-9654-ac052d83f3e7', gen_random_uuid()),
       ('5ef19d54-c429-499d-9654-ac052d83f3e7', gen_random_uuid()),
       ('5ef19d54-c429-499d-9654-ac052d83f3e7', gen_random_uuid()),
       ('5ef19d54-c429-499d-9654-ac052d83f3e7', gen_random_uuid()),
       ('5ef19d54-c429-499d-9654-ac052d83f3e7', gen_random_uuid()),
       ('64da3d79-52be-4936-97e3-b88597bac8b9', gen_random_uuid()),
       ('64da3d79-52be-4936-97e3-b88597bac8b9', gen_random_uuid()),
       ('64da3d79-52be-4936-97e3-b88597bac8b9', gen_random_uuid()),
       ('64da3d79-52be-4936-97e3-b88597bac8b9', gen_random_uuid()),
       ('60deabdf-ba7d-482a-b6a5-26e440850496', gen_random_uuid()),
       ('60deabdf-ba7d-482a-b6a5-26e440850496', gen_random_uuid()),
       ('60deabdf-ba7d-482a-b6a5-26e440850496', gen_random_uuid()),
       ('60deabdf-ba7d-482a-b6a5-26e440850496', gen_random_uuid()),
       ('60deabdf-ba7d-482a-b6a5-26e440850496', gen_random_uuid());

--- Resource groups ---

INSERT INTO public.resource_group (id, version, name, description, stateless, max_rent_time)
VALUES ('1b0912df-c4c0-4907-9dd4-b09573a3ef44', 0, 'ISRP-RG01', '', false, 6),
       ('dfe85896-7c82-41c6-ba31-f9401d10c4f2', 0, 'ISRP-RG02', '', false, 6),
       ('0454b258-1457-4719-99b6-a9cc9576de2d', 0, 'ISRP-RG03', '', false, 6),
       ('a1529025-aae8-4f33-b5cf-295353d77c48', 0, 'ISRP-RG04', '', false, 6);

--- Stateful pods ---

INSERT INTO public.pod_stateful (id, course_id, rg_id, team_id, cluster_id)
VALUES ('0e542d51-ba4f-4dd5-bef8-eae5b7477103', 'a7556146-23a6-4936-903c-c337c794a8c7', '1b0912df-c4c0-4907-9dd4-b09573a3ef44', 'f15e7fe3-60a6-4d2c-a124-ad763f6869e2', 'a5097950-d0c6-4d65-8b2e-4768809ad37a'),
       ('61807f79-334e-4fdd-985b-6aaa95c0bf8d', 'a7556146-23a6-4936-903c-c337c794a8c7', 'dfe85896-7c82-41c6-ba31-f9401d10c4f2', '5ef19d54-c429-499d-9654-ac052d83f3e7', 'a5097950-d0c6-4d65-8b2e-4768809ad37a'),
       ('c5cec07a-f8c1-41ca-a439-397a7aae63df', 'a7556146-23a6-4936-903c-c337c794a8c7', '0454b258-1457-4719-99b6-a9cc9576de2d', '64da3d79-52be-4936-97e3-b88597bac8b9', 'a5097950-d0c6-4d65-8b2e-4768809ad37a'),
       ('e016831f-96e4-4c96-a14d-54c167fdd5d0', 'a7556146-23a6-4936-903c-c337c794a8c7', 'a1529025-aae8-4f33-b5cf-295353d77c48', '60deabdf-ba7d-482a-b6a5-26e440850496', 'a5097950-d0c6-4d65-8b2e-4768809ad37a');

--- Resource group pools ---

INSERT INTO public.resource_group_pool (id, version, name, grace_period, max_rent, course_id)
VALUES ('5407b59a-d4b0-4fcd-aea6-12b2101f628a', 0, 'ISRP-RGPool01', 12, 6, 'a7556146-23a6-4936-903c-c337c794a8c7'),
       ('37b6aad6-7cfd-472a-9a93-d9dc653fdbca', 0, 'ISRP-RGPool02', 12, 6, 'a7556146-23a6-4936-903c-c337c794a8c7'),
       ('e98a5bbf-c94e-488a-9e83-9025faa7c75a', 0, 'ISRP-RGPool03', 12, 6, 'a7556146-23a6-4936-903c-c337c794a8c7'),
       ('85bb749a-3666-43a7-aa6e-87c6bfcb8201', 0, 'ISRP-RGPool04', 12, 6, 'a7556146-23a6-4936-903c-c337c794a8c7');

INSERT INTO public.resource_group_pool_resource_groups (resource_group_pool_id, resource_groups_id)
VALUES ('5407b59a-d4b0-4fcd-aea6-12b2101f628a', '1b0912df-c4c0-4907-9dd4-b09573a3ef44'),
       ('37b6aad6-7cfd-472a-9a93-d9dc653fdbca', 'dfe85896-7c82-41c6-ba31-f9401d10c4f2'),
       ('e98a5bbf-c94e-488a-9e83-9025faa7c75a', '0454b258-1457-4719-99b6-a9cc9576de2d'),
       ('85bb749a-3666-43a7-aa6e-87c6bfcb8201', 'a1529025-aae8-4f33-b5cf-295353d77c48');

-----------------------------------
--- Sieciowe systemy baz danych ---
-----------------------------------

--- Teams ---

INSERT INTO public.team (id, name, key, active, max_size, course_id)
VALUES ('f3896c36-2133-4497-965e-0951e1f5aebf', 'EventSymphony', 'SSBD-AccessKey01', false, 7, 'e485ded6-c166-45f6-a924-13ce44666f7a'),
       ('78908655-ee18-4863-9eef-e67519940a0b', 'LandlordKingdom', 'SSBD-AccessKey02', false, 7, 'e485ded6-c166-45f6-a924-13ce44666f7a'),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', 'Eldorado', 'SSBD-AccessKey03', false, 7, 'e485ded6-c166-45f6-a924-13ce44666f7a');

INSERT INTO public.user_team (team_id, user_id)
VALUES ('f3896c36-2133-4497-965e-0951e1f5aebf', gen_random_uuid()),
       ('f3896c36-2133-4497-965e-0951e1f5aebf', gen_random_uuid()),
       ('f3896c36-2133-4497-965e-0951e1f5aebf', gen_random_uuid()),
       ('f3896c36-2133-4497-965e-0951e1f5aebf', gen_random_uuid()),
       ('f3896c36-2133-4497-965e-0951e1f5aebf', gen_random_uuid()),
       ('f3896c36-2133-4497-965e-0951e1f5aebf', gen_random_uuid()),
       ('f3896c36-2133-4497-965e-0951e1f5aebf', gen_random_uuid()),
       ('78908655-ee18-4863-9eef-e67519940a0b', gen_random_uuid()),
       ('78908655-ee18-4863-9eef-e67519940a0b', gen_random_uuid()),
       ('78908655-ee18-4863-9eef-e67519940a0b', gen_random_uuid()),
       ('78908655-ee18-4863-9eef-e67519940a0b', gen_random_uuid()),
       ('78908655-ee18-4863-9eef-e67519940a0b', gen_random_uuid()),
       ('78908655-ee18-4863-9eef-e67519940a0b', gen_random_uuid()),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', 'abfc5d9b-1350-444d-9d9a-1bfde79667ad'),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', gen_random_uuid()),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', gen_random_uuid()),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', gen_random_uuid()),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', gen_random_uuid()),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', gen_random_uuid()),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', gen_random_uuid());

--- Resource groups ---

--- Resource groups ---

INSERT INTO public.resource_group (id, version, name, description, stateless, max_rent_time)
VALUES ('692bde41-c8ca-4873-bbaf-edb789ae7c87', 0, 'SSBD-RG01', '', false, 6),
       ('f13f85d1-14bf-4930-bc2a-b044f3feebe2', 0, 'SSBD-RG02', '', false, 6),
       ('50c319f6-d29d-4193-bfef-5e33b4e26353', 0, 'SSBD-RG03', '', false, 6);

--- Stateful pods ---

INSERT INTO public.pod_stateful (id, course_id, rg_id, team_id, cluster_id)
VALUES ('a57e55b8-471d-4754-acf0-15c57b877e99', 'e485ded6-c166-45f6-a924-13ce44666f7a', '692bde41-c8ca-4873-bbaf-edb789ae7c87', 'f3896c36-2133-4497-965e-0951e1f5aebf', 'a5097950-d0c6-4d65-8b2e-4768809ad37a'),
       ('849f00e3-199d-4e81-9892-5b5c2a3b5b7d', 'e485ded6-c166-45f6-a924-13ce44666f7a', 'f13f85d1-14bf-4930-bc2a-b044f3feebe2', '78908655-ee18-4863-9eef-e67519940a0b', 'a5097950-d0c6-4d65-8b2e-4768809ad37a'),
       ('a5681e23-bcec-4353-9e4d-f0c60f2efb26', 'e485ded6-c166-45f6-a924-13ce44666f7a', '50c319f6-d29d-4193-bfef-5e33b4e26353', '40517c17-58b9-41ce-b53e-abaf0e7782fd', 'a5097950-d0c6-4d65-8b2e-4768809ad37a');

--- Resource group pools ---

INSERT INTO public.resource_group_pool (id, version, name, grace_period, max_rent, course_id)
VALUES ('03d31c2f-d300-45af-a8f8-37c894d7a527', 0, 'SSBD-RGPool01', 12, 6, 'e485ded6-c166-45f6-a924-13ce44666f7a'),
       ('0e484149-67a8-4c1c-80b6-901fa737f2c9', 0, 'SSBD-RGPool02', 12, 6, 'e485ded6-c166-45f6-a924-13ce44666f7a'),
       ('9872c8c5-70e9-432e-8a29-7c6dafd35ddc', 0, 'SSBD-RGPool03', 12, 6, 'e485ded6-c166-45f6-a924-13ce44666f7a');

INSERT INTO public.resource_group_pool_resource_groups (resource_group_pool_id, resource_groups_id)
VALUES ('03d31c2f-d300-45af-a8f8-37c894d7a527', '692bde41-c8ca-4873-bbaf-edb789ae7c87'),
       ('0e484149-67a8-4c1c-80b6-901fa737f2c9', 'f13f85d1-14bf-4930-bc2a-b044f3feebe2'),
       ('9872c8c5-70e9-432e-8a29-7c6dafd35ddc', '50c319f6-d29d-4193-bfef-5e33b4e26353');

-------------------------------------
--- Techniki utrzymania aplikacji ---
-------------------------------------

--- Teams ---

INSERT INTO public.team (id, name, key, active, max_size, course_id)
VALUES ('d46387ee-7397-4184-91eb-d01d5f301c0e', 'EventSymphony', 'TUA-AccessKey01', true, 7, '1decd050-1328-4eca-b2de-84793a8474c2'),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', 'LandlordKingdom', 'TUA-AccessKey02', true, 7, '1decd050-1328-4eca-b2de-84793a8474c2'),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', 'Eldorado', 'TUA-AccessKey03', true, 7, '1decd050-1328-4eca-b2de-84793a8474c2');

INSERT INTO public.user_team (team_id, user_id)
VALUES ('d46387ee-7397-4184-91eb-d01d5f301c0e', gen_random_uuid()),
       ('d46387ee-7397-4184-91eb-d01d5f301c0e', gen_random_uuid()),
       ('d46387ee-7397-4184-91eb-d01d5f301c0e', gen_random_uuid()),
       ('d46387ee-7397-4184-91eb-d01d5f301c0e', gen_random_uuid()),
       ('d46387ee-7397-4184-91eb-d01d5f301c0e', gen_random_uuid()),
       ('d46387ee-7397-4184-91eb-d01d5f301c0e', gen_random_uuid()),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', gen_random_uuid()),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', gen_random_uuid()),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', gen_random_uuid()),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', gen_random_uuid()),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', gen_random_uuid()),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', gen_random_uuid()),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', gen_random_uuid()),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', gen_random_uuid()),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', gen_random_uuid()),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', gen_random_uuid()),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', gen_random_uuid()),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', gen_random_uuid());

--- Resource groups ---

INSERT INTO public.resource_group (id, version, name, description, stateless, max_rent_time)
VALUES ('a7919551-3807-4b35-88e3-fc3a868ba014', 0, 'TUA-RG01', '', false, 6),
       ('e51132d3-8ac3-4232-85e0-849c8afa6abc', 0, 'TUA-RG02', '', false, 6),
       ('7c7ed665-831c-4e24-b6fe-1947a16fcadb', 0, 'TUA-RG03', '', false, 6);

--- Stateful pods ---

INSERT INTO public.pod_stateful (id, course_id, rg_id, team_id, cluster_id)
VALUES ('c971b368-92c8-4a01-86d2-dd783b5360a8', '1decd050-1328-4eca-b2de-84793a8474c2', 'a7919551-3807-4b35-88e3-fc3a868ba014', 'd46387ee-7397-4184-91eb-d01d5f301c0e', 'a5097950-d0c6-4d65-8b2e-4768809ad37a'),
       ('161f4a45-f87d-40b5-9ad6-1ec056485e01', '1decd050-1328-4eca-b2de-84793a8474c2', 'e51132d3-8ac3-4232-85e0-849c8afa6abc', 'e608c9d0-e871-4374-a052-f27ced4a9cec', 'a5097950-d0c6-4d65-8b2e-4768809ad37a'),
       ('c3db873c-cfeb-4ec2-b87b-342af78f869a', '1decd050-1328-4eca-b2de-84793a8474c2', '7c7ed665-831c-4e24-b6fe-1947a16fcadb', '18750e93-22a7-4a23-8f8b-e0cabde8f793', 'a5097950-d0c6-4d65-8b2e-4768809ad37a');

--- Resource group pools ---

INSERT INTO public.resource_group_pool (id, version, name, grace_period, max_rent, course_id)
VALUES ('33ad145f-10a6-4d4f-a06c-a1a869aa9d43', 0, 'TUA-RGPool01', 12, 6, '1decd050-1328-4eca-b2de-84793a8474c2'),
       ('ad1b03b6-7c19-4f92-b647-9670013ce1fc', 0, 'TUA-RGPool02', 12, 6, '1decd050-1328-4eca-b2de-84793a8474c2'),
       ('86e4d3cd-5764-426e-bff1-49eb21696187', 0, 'TUA-RGPool03', 12, 6, '1decd050-1328-4eca-b2de-84793a8474c2');

INSERT INTO public.resource_group_pool_resource_groups (resource_group_pool_id, resource_groups_id)
VALUES ('33ad145f-10a6-4d4f-a06c-a1a869aa9d43', 'a7919551-3807-4b35-88e3-fc3a868ba014'),
       ('ad1b03b6-7c19-4f92-b647-9670013ce1fc', 'e51132d3-8ac3-4232-85e0-849c8afa6abc'),
       ('86e4d3cd-5764-426e-bff1-49eb21696187', '7c7ed665-831c-4e24-b6fe-1947a16fcadb');
