INSERT INTO public.metric (id, name, category)
VALUES ('2865efff-f8e5-4960-a0ce-fc05e98828ba', 'cpu_count', 'COUNTABLE'),
       ('63490da4-d0f1-4e7a-88fc-3342633accc0', 'memory_size', 'MEMORY'),
       ('1929c2b2-ba03-4180-ae90-79bd2335f2a8', 'network_count', 'COUNTABLE');

INSERT INTO public.metric_cluster (id, cluster_id, metric_id, metric_value)

VALUES (gen_random_uuid(), 'c282a57c-624e-448b-823e-a68352d10914', '2865efff-f8e5-4960-a0ce-fc05e98828ba', 100),
       (gen_random_uuid(), 'c282a57c-624e-448b-823e-a68352d10914', '63490da4-d0f1-4e7a-88fc-3342633accc0', 10737418240),
       (gen_random_uuid(), 'c282a57c-624e-448b-823e-a68352d10914', '1929c2b2-ba03-4180-ae90-79bd2335f2a8', 10);

INSERT INTO public.private_vlans_range (range_from, range_to, id)
VALUES (0, 1000, '0978f66d-050c-4c28-a376-9b8934d6167a');

INSERT INTO public.resource_group(id, name, description, stateless, version, max_rent_time)
VALUES (gen_random_uuid(), 'testStatefulRG1', '', false, 0, 120);

INSERT INTO public.resource_group(id, name, description, stateless, version, max_rent_time)
VALUES (gen_random_uuid(), 'testStatefulRG2', '', false, 0, 130);

INSERT INTO public.resource_group(id, name, description, stateless, version, max_rent_time)
VALUES (gen_random_uuid(), 'testStatelessRG1', '', true, 0, 120);

INSERT INTO public.resource_group(id, name, description, stateless, version, max_rent_time)
VALUES (gen_random_uuid(), 'testStatelessRG2', '', true, 0, 130);

INSERT INTO public.resource_group_pool(id, name, max_rent, version, grace_period, max_rent_time, description)
VALUES (gen_random_uuid(), 'testStatelessRGPool', 120, 0, 120, 120, '');


INSERT INTO public.resource_group_pool_resource_groups(resource_group_pool_id, resource_groups_id)
VALUES ((SELECT id FROM public.resource_group_pool WHERE name = 'testStatelessRGPool'),
        (SELECT id FROM public.resource_group WHERE name = 'testStatelessRG1'));

INSERT INTO public.resource_group_pool_resource_groups(resource_group_pool_id, resource_groups_id)
VALUES ((SELECT id FROM public.resource_group_pool WHERE name = 'testStatelessRGPool'),
        (SELECT id FROM public.resource_group WHERE name = 'testStatelessRG2'));


-- INSERT INTO public.course(id, name, description, course_type)
-- VALUES ('784cca54-f15d-43e7-b76c-f95a342fdf69', 'testTeamBasedCourse', 'testTeamBasedDescription', 'TEAM_BASED');
--
-- INSERT INTO public.course(id, name, description, course_type)
-- VALUES ('882357a8-a4e6-4684-1623-0edd22854877', 'testSoloCourse', 'testSoloDescription', 'SOLO');
--
-- INSERT INTO public.team(id, name, course_id, max_size, active, version)
-- VALUES ('eedf635f-f2c4-4f62-9401-8cbbd00632f5', 'testTeamTBC1',
--         (SELECT id FROM public.course WHERE name = 'testTeamBasedCourse'), '3', true, '1');
--
-- INSERT INTO public.team(id, name, course_id, max_size, active, version)
-- VALUES ('ff127467-a3d5-4f73-8512-9dcc11743f66', 'testTeamTBC2',
--         (SELECT id FROM public.course WHERE name = 'testTeamBasedCourse'), '3', true, '1');
--
-- INSERT INTO public.access_key(id, key_value)
-- VALUES
--     ('aa11bb22-cc33-dd44-ee55-ff6677889900', 'TEAM-ALPHA-KEY-2024'),
--     ('bb22cc33-dd44-ee55-ff66-778899001122', 'TEAM-BETA-KEY-2024'),
--     ('cc33dd44-ee55-ff66-7788-990011223344', 'TEAM-GAMMA-KEY-2024');
--
-- INSERT INTO public.team_access_key(id, team_id)
-- VALUES
--     ('aa11bb22-cc33-dd44-ee55-ff6677889900', 'eedf635f-f2c4-4f62-9401-8cbbd00632f5'),
--     ('bb22cc33-dd44-ee55-ff66-778899001122', 'ff127467-a3d5-4f73-8512-9dcc11743f66');
--
-- INSERT INTO public.course_access_key(id, course_id)
-- VALUES
--     ('cc33dd44-ee55-ff66-7788-990011223344', '882357a8-a4e6-4684-1623-0edd22854877');
-- VALUES (gen_random_uuid(), 'test', '', false, 0, 0);

------------------------------------------
--- Sample data for maintenance module ---
------------------------------------------

INSERT INTO public.administrative_break (id, version, cause, description, type, cluster_id, begin_at, end_at)
VALUES ('2e8989cb-6811-46ce-be27-9ec7b6ea788c', 0, 'Some random cause #1', 'Some description of the break', 'SYSTEM',null, timestamp 'yesterday' - interval '1 hour', timestamp 'yesterday' + interval '7 hours'),
       ('eb4c5e2e-215c-442f-82e8-bb38900d0b47', 0, 'Some random cause #2', 'Some description of the break', 'CLUSTER','c282a57c-624e-448b-823e-a68352d10914', timestamp 'today' + interval '18 hour',timestamp 'today' + interval '19 hours'),
       ('0136324d-fa42-4d4a-a046-33469ecb9d0b', 0, 'Some random cause #3', 'Some description of the break', 'CLUSTER','c282a57c-624e-448b-823e-a68352d10914', timestamp 'tomorrow' - interval '1 hour',timestamp 'tomorrow' + interval '7 hours');

-- INSERT INTO public.user (id, ovirt_id, email, user_name, first_name, last_name)
-- VALUES ('4edf0d7d-e5cc-4ba5-a7a9-8acaa2763eca', '11f4cd51-350e-4ed5-8074-4f13c012e6cb' , '242447@edu.p.lodz.pl', 'nexto','Piotr', 'Kwiatkowski');

------------------------------------------
--- Sample data for reservation module ---
------------------------------------------

INSERT INTO public.course (id, name, description, cluster_id, course_type, version)
VALUES ('b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'Systemy operacyjne', 'Operating Systems',
        'c282a57c-624e-448b-823e-a68352d10914', 'SOLO', 0),
       ('a7556146-23a6-4936-903c-c337c794a8c7', 'Infrastruktury środowisk rozwojowych i produkcyjnych',
        'Infrastructures of Development and Production Environments', 'c282a57c-624e-448b-823e-a68352d10914',
        'TEAM_BASED', 0),
       ('e485ded6-c166-45f6-a924-13ce44666f7a', 'Sieciowe systemy baz danych', 'Network Database Systems',
        'c282a57c-624e-448b-823e-a68352d10914', 'TEAM_BASED', 0),
       ('1decd050-1328-4eca-b2de-84793a8474c2', 'Techniki utrzymania aplikacji',
        'Techniques of Application Maintenance', 'c282a57c-624e-448b-823e-a68352d10914', 'TEAM_BASED', 0);

INSERT INTO access_key (id, key_value)
VALUES ('b96844a7-7cb6-48f1-b4e6-129198825f2c', 'SO-AccessKey'),
       ('fc2da32b-2146-4db6-adfa-d754575fdf22', 'ISRP-AccessKey'),
       ('c1c58086-f1dc-45ce-a01c-62b9be04b6ef', 'SSBD-AccessKey'),
       ('87b11363-bb7e-4d54-8f57-ed5a52146210', 'TUA-AccessKey');

INSERT INTO public.course_access_key (course_id, id)
VALUES ('b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', 'b96844a7-7cb6-48f1-b4e6-129198825f2c'),
       ('a7556146-23a6-4936-903c-c337c794a8c7', 'fc2da32b-2146-4db6-adfa-d754575fdf22'),
       ('e485ded6-c166-45f6-a924-13ce44666f7a', 'c1c58086-f1dc-45ce-a01c-62b9be04b6ef'),
       ('1decd050-1328-4eca-b2de-84793a8474c2', '87b11363-bb7e-4d54-8f57-ed5a52146210');

INSERT INTO public.users (user_id, ovirt_id, email, first_name, last_name, user_name)
VALUES ('4edf0d7d-e5cc-4ba5-a7a9-8acaa2763eca', gen_random_uuid(), '242447@edu.p.lodz.pl', 'Piotr', 'Kwiatkowski','pkwiatkowski'),
       ('4e88cecc-fa80-4145-b5a8-e2e4acf24279', gen_random_uuid(), 'admin@localhost', 'FirstName01', 'LastName01','UserName01'),
       ('c7ceee90-734d-4e6c-8756-2cd9a44ff668', gen_random_uuid(), 'example02@email.com', 'FirstName02', 'LastName02','UserName02'),
       ('f00de025-d801-4276-9b53-9d5ab8f89ac8', gen_random_uuid(), 'example03@email.com', 'FirstName03', 'LastName03','UserName03'),
       ('44b21e72-50d2-42c0-a449-4791177251aa', gen_random_uuid(), 'example04@email.com', 'FirstName04', 'LastName04','UserName04'),
       ('99403b0e-c184-4d18-95b6-384cb322b449', gen_random_uuid(), 'example05@email.com', 'FirstName05', 'LastName05','UserName05'),
       ('b9d89a2a-75ff-4ca4-a9b4-068aab962524', gen_random_uuid(), 'example06@email.com', 'FirstName06', 'LastName06','UserName06'),
       ('8551a9c9-0877-4831-b520-bcc8a137cf7c', gen_random_uuid(), 'example07@email.com', 'FirstName07', 'LastName07','UserName07'),
       ('5ed87f00-bebc-41ab-aebb-66c587335065', gen_random_uuid(), 'example08@email.com', 'FirstName08', 'LastName08','UserName08'),
       ('348213e4-7895-4a06-809c-35fe174eee5a', gen_random_uuid(), 'example09@email.com', 'FirstName09', 'LastName09','UserName09'),
       ('08304074-48c3-48b8-8693-342c9add70a6', gen_random_uuid(), 'example10@email.com', 'FirstName10', 'LastName10','UserName10'),
       ('e29eadad-0fea-4c08-afe1-15c8b931de3c', gen_random_uuid(), 'example11@email.com', 'FirstName11', 'LastName11','UserName11'),
       ('7be81ff0-f42f-4bdf-8002-792b10a2181d', gen_random_uuid(), 'example12@email.com', 'FirstName12', 'LastName12','UserName12'),
       ('79b84d69-9761-4c2e-b9d6-223cd55cd917', gen_random_uuid(), 'example13@email.com', 'FirstName13', 'LastName13','UserName13'),
       ('9b2db558-1ee7-47e1-9fd2-6626ec95d230', gen_random_uuid(), 'example14@email.com', 'FirstName14', 'LastName14','UserName14'),
       ('582d50de-6eab-4343-a2d2-0157c8a6c14d', gen_random_uuid(), 'example15@email.com', 'FirstName15', 'LastName15','UserName15'),
       ('6b599d28-d631-41e5-ab76-f66369bccb14', gen_random_uuid(), 'example16@email.com', 'FirstName16', 'LastName16','UserName16'),
       ('04adeaa1-c232-46bc-aa0c-caab2e0efacd', gen_random_uuid(), 'example17@email.com', 'FirstName17', 'LastName17','UserName17'),
       ('5d2842e9-a219-4afa-bbb9-28cfce5082e8', gen_random_uuid(), 'example18@email.com', 'FirstName18', 'LastName18','UserName18'),
       ('a053ddfa-4308-4064-bf16-a07d830bd9fa', gen_random_uuid(), 'example19@email.com', 'FirstName19', 'LastName19','UserName19'),
       ('a666ee8c-4b1a-413a-8e89-c45a688f5d1a', gen_random_uuid(), 'example20@email.com', 'FirstName20', 'LastName20','UserName20');

--------------------------
--- Systemy operacyjne ---
--------------------------

INSERT INTO public.course_teachers (course_id, teachers_user_id)
VALUES ('b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', '4edf0d7d-e5cc-4ba5-a7a9-8acaa2763eca');

--- Teams ---

INSERT INTO public.team (id, version, name, active, max_size, course_id)
VALUES ('72fc908f-5d02-4c81-91a3-2bccaa627946', 0, 'SO-Student001', true, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9'),
       ('cb97b2f0-646f-4876-906f-0fd44cf6d63a', 0, 'SO-Student002', true, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9'),
       ('12ca7f40-c596-44ae-a8d2-671843ecc9e5', 0, 'SO-Student003', true, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9'),
       ('83698296-0b9f-40ae-a8e7-2caec8068e1e', 0, 'SO-Student004', true, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9'),
       ('b4232713-05fa-411a-a40d-9a3e15f13fa0', 0, 'SO-Student005', true, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9'),
       ('d82a711e-a308-4127-95f9-2c38851c3e71', 0, 'SO-Student006', true, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9'),
       ('89f530ca-f5a0-40cd-b00a-1997c96dd9d3', 0, 'SO-Student007', true, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9'),
       ('bbf54d7d-3ccb-4cea-9726-baef3c58e2dd', 0, 'SO-Student008', true, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9'),
       ('c5aaa2ae-4ba7-4b87-86b5-52643d92de47', 0, 'SO-Student009', true, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9'),
       ('33e44d4e-c937-4d13-a060-f41a4c3c05fb', 0, 'SO-Student010', true, 1, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9');

INSERT INTO public.team_users (team_id, user_id)
VALUES ('72fc908f-5d02-4c81-91a3-2bccaa627946', '4e88cecc-fa80-4145-b5a8-e2e4acf24279'),
       ('cb97b2f0-646f-4876-906f-0fd44cf6d63a', 'c7ceee90-734d-4e6c-8756-2cd9a44ff668'),
       ('12ca7f40-c596-44ae-a8d2-671843ecc9e5', 'f00de025-d801-4276-9b53-9d5ab8f89ac8'),
       ('83698296-0b9f-40ae-a8e7-2caec8068e1e', '44b21e72-50d2-42c0-a449-4791177251aa'),
       ('b4232713-05fa-411a-a40d-9a3e15f13fa0', '99403b0e-c184-4d18-95b6-384cb322b449'),
       ('d82a711e-a308-4127-95f9-2c38851c3e71', 'b9d89a2a-75ff-4ca4-a9b4-068aab962524'),
       ('89f530ca-f5a0-40cd-b00a-1997c96dd9d3', '8551a9c9-0877-4831-b520-bcc8a137cf7c'),
       ('bbf54d7d-3ccb-4cea-9726-baef3c58e2dd', '5ed87f00-bebc-41ab-aebb-66c587335065'),
       ('c5aaa2ae-4ba7-4b87-86b5-52643d92de47', '348213e4-7895-4a06-809c-35fe174eee5a'),
       ('33e44d4e-c937-4d13-a060-f41a4c3c05fb', '348213e4-7895-4a06-809c-35fe174eee5a');

--- Resource groups ---

INSERT INTO public.resource_group (id, version, name, description, stateless, max_rent_time)
VALUES ('e028a269-9890-4b02-81d9-b477ea7f552a', 0, 'SO-RG01', '', true, 3),
       ('023192a9-9a7e-4861-95e7-a77ac29ca039', 0, 'SO-RG02', '', true, 3),
       ('e205cf0a-6966-4cc0-8237-d2424de35e22', 0, 'SO-RG03', '', true, 3),
       ('2ef59c08-2ec5-40cc-8730-53723d3abda4', 0, 'SO-RG04', '', true, 3);

--- Resource group pools ---

INSERT INTO public.resource_group_pool (id, version, name, grace_period, max_rent, course_id, description,
                                        max_rent_time)
VALUES ('4778c01d-4962-4cbd-a653-c90aea9dbddf', 0, 'SysOp-Pool', 6, 3, 'b99fde5c-8200-4eb5-80e2-1c6b4b6019b9', '', 3);

INSERT INTO public.resource_group_pool_resource_groups (resource_group_pool_id, resource_groups_id)
VALUES ('4778c01d-4962-4cbd-a653-c90aea9dbddf', 'e028a269-9890-4b02-81d9-b477ea7f552a'),
       ('4778c01d-4962-4cbd-a653-c90aea9dbddf', '023192a9-9a7e-4861-95e7-a77ac29ca039'),
       ('4778c01d-4962-4cbd-a653-c90aea9dbddf', 'e205cf0a-6966-4cc0-8237-d2424de35e22'),
       ('4778c01d-4962-4cbd-a653-c90aea9dbddf', '2ef59c08-2ec5-40cc-8730-53723d3abda4');

------------------------------------------------------------
--- Infrastruktury środowisk rozwojowych i produkcyjnych ---
------------------------------------------------------------

INSERT INTO public.course_teachers (course_id, teachers_user_id)
VALUES ('a7556146-23a6-4936-903c-c337c794a8c7', '4edf0d7d-e5cc-4ba5-a7a9-8acaa2763eca');

--- Teams ---

INSERT INTO public.team (id, version, name, active, max_size, course_id)
VALUES ('f15e7fe3-60a6-4d2c-a124-ad763f6869e2', 0, 'ISRP-01', true, 5, 'a7556146-23a6-4936-903c-c337c794a8c7'),
       ('5ef19d54-c429-499d-9654-ac052d83f3e7', 0, 'ISRP-02', true, 5, 'a7556146-23a6-4936-903c-c337c794a8c7'),
       ('64da3d79-52be-4936-97e3-b88597bac8b9', 0, 'ISRP-03', true, 5, 'a7556146-23a6-4936-903c-c337c794a8c7'),
       ('60deabdf-ba7d-482a-b6a5-26e440850496', 0, 'ISRP-04', true, 5, 'a7556146-23a6-4936-903c-c337c794a8c7');

INSERT INTO public.access_key (id, key_value)
VALUES ('44c2419e-20bc-4dd5-a2e9-e7a44b1e8552', 'ISRP-Team01-Key'),
       ('a827ddcd-bda9-40c2-a4cd-6c26bd5d717b', 'ISRP-Team02-Key'),
       ('883f2352-416b-479a-8323-cb62a663fe41', 'ISRP-Team03-Key'),
       ('492c81df-b3c5-44d5-84e6-5d3eb77b4728', 'ISRP-Team04-Key');

INSERT INTO public.team_access_key (id, team_id)
VALUES ('44c2419e-20bc-4dd5-a2e9-e7a44b1e8552', 'f15e7fe3-60a6-4d2c-a124-ad763f6869e2'),
       ('a827ddcd-bda9-40c2-a4cd-6c26bd5d717b', '5ef19d54-c429-499d-9654-ac052d83f3e7'),
       ('883f2352-416b-479a-8323-cb62a663fe41', '64da3d79-52be-4936-97e3-b88597bac8b9'),
       ('492c81df-b3c5-44d5-84e6-5d3eb77b4728', '60deabdf-ba7d-482a-b6a5-26e440850496');

INSERT INTO public.team_users (team_id, user_id)
VALUES ('f15e7fe3-60a6-4d2c-a124-ad763f6869e2', '4e88cecc-fa80-4145-b5a8-e2e4acf24279'),
       ('f15e7fe3-60a6-4d2c-a124-ad763f6869e2', 'c7ceee90-734d-4e6c-8756-2cd9a44ff668'),
       ('f15e7fe3-60a6-4d2c-a124-ad763f6869e2', 'f00de025-d801-4276-9b53-9d5ab8f89ac8'),
       ('f15e7fe3-60a6-4d2c-a124-ad763f6869e2', '44b21e72-50d2-42c0-a449-4791177251aa'),
       ('5ef19d54-c429-499d-9654-ac052d83f3e7', '99403b0e-c184-4d18-95b6-384cb322b449'),
       ('5ef19d54-c429-499d-9654-ac052d83f3e7', 'b9d89a2a-75ff-4ca4-a9b4-068aab962524'),
       ('5ef19d54-c429-499d-9654-ac052d83f3e7', '8551a9c9-0877-4831-b520-bcc8a137cf7c'),
       ('5ef19d54-c429-499d-9654-ac052d83f3e7', '5ed87f00-bebc-41ab-aebb-66c587335065'),
       ('5ef19d54-c429-499d-9654-ac052d83f3e7', '348213e4-7895-4a06-809c-35fe174eee5a'),
       ('64da3d79-52be-4936-97e3-b88597bac8b9', '4edf0d7d-e5cc-4ba5-a7a9-8acaa2763eca'),
       ('64da3d79-52be-4936-97e3-b88597bac8b9', 'e29eadad-0fea-4c08-afe1-15c8b931de3c'),
       ('64da3d79-52be-4936-97e3-b88597bac8b9', '7be81ff0-f42f-4bdf-8002-792b10a2181d'),
       ('64da3d79-52be-4936-97e3-b88597bac8b9', '79b84d69-9761-4c2e-b9d6-223cd55cd917'),
       ('60deabdf-ba7d-482a-b6a5-26e440850496', '9b2db558-1ee7-47e1-9fd2-6626ec95d230'),
       ('60deabdf-ba7d-482a-b6a5-26e440850496', '582d50de-6eab-4343-a2d2-0157c8a6c14d'),
       ('60deabdf-ba7d-482a-b6a5-26e440850496', '6b599d28-d631-41e5-ab76-f66369bccb14'),
       ('60deabdf-ba7d-482a-b6a5-26e440850496', '04adeaa1-c232-46bc-aa0c-caab2e0efacd'),
       ('60deabdf-ba7d-482a-b6a5-26e440850496', '5d2842e9-a219-4afa-bbb9-28cfce5082e8');

--- Resource groups ---

INSERT INTO public.resource_group (id, version, name, description, stateless, max_rent_time)
VALUES ('1b0912df-c4c0-4907-9dd4-b09573a3ef44', 0, 'ISRP-RG01', '', false, 6),
       ('dfe85896-7c82-41c6-ba31-f9401d10c4f2', 0, 'ISRP-RG02', '', false, 6),
       ('0454b258-1457-4719-99b6-a9cc9576de2d', 0, 'ISRP-RG03', '', false, 6),
       ('a1529025-aae8-4f33-b5cf-295353d77c48', 0, 'ISRP-RG04', '', false, 6);

INSERT INTO public.virtual_machine (hidden, id, resource_group_id, version)
VALUES (false, '5d1606b4-5263-4c78-a89b-57a2a26510cc', '1b0912df-c4c0-4907-9dd4-b09573a3ef44', 0),
       (false, '40861333-18c9-48b3-9956-a2168060cbca', 'dfe85896-7c82-41c6-ba31-f9401d10c4f2', 0),
       (false, 'c136faca-c487-4be6-8eff-90e2617ffdad', 'dfe85896-7c82-41c6-ba31-f9401d10c4f2', 0),
       (false, '4181e5f8-7cc6-4021-a653-b7c59f5ef16e', '0454b258-1457-4719-99b6-a9cc9576de2d', 0),
       (false, 'ef44305e-adc6-4329-97ba-78ebaa30eb98', '0454b258-1457-4719-99b6-a9cc9576de2d', 0),
       (false, '943584ee-66fb-406e-86f2-648156d78138', 'a1529025-aae8-4f33-b5cf-295353d77c48', 0);

--- Stateful pods ---

INSERT INTO public.pod_stateful (id, course_id, rg_id, team_id, max_rent)
VALUES ('0e542d51-ba4f-4dd5-bef8-eae5b7477103', 'a7556146-23a6-4936-903c-c337c794a8c7',
        '1b0912df-c4c0-4907-9dd4-b09573a3ef44', 'f15e7fe3-60a6-4d2c-a124-ad763f6869e2', 3),
       ('61807f79-334e-4fdd-985b-6aaa95c0bf8d', 'a7556146-23a6-4936-903c-c337c794a8c7',
        'dfe85896-7c82-41c6-ba31-f9401d10c4f2', '5ef19d54-c429-499d-9654-ac052d83f3e7', 3),
       ('c5cec07a-f8c1-41ca-a439-397a7aae63df', 'a7556146-23a6-4936-903c-c337c794a8c7',
        '0454b258-1457-4719-99b6-a9cc9576de2d', '64da3d79-52be-4936-97e3-b88597bac8b9', 3),
       ('e016831f-96e4-4c96-a14d-54c167fdd5d0', 'a7556146-23a6-4936-903c-c337c794a8c7',
        'a1529025-aae8-4f33-b5cf-295353d77c48', '60deabdf-ba7d-482a-b6a5-26e440850496', 3);

--- Resource group pools ---

INSERT INTO public.resource_group_pool (id, version, name, description, grace_period, max_rent, max_rent_time,
                                        course_id)
VALUES ('5407b59a-d4b0-4fcd-aea6-12b2101f628a', 0, 'ISRP-RGPool01', '', 4, 6, 20,
        'a7556146-23a6-4936-903c-c337c794a8c7'),
       ('37b6aad6-7cfd-472a-9a93-d9dc653fdbca', 0, 'ISRP-RGPool02', '', 4, 6, 20,
        'a7556146-23a6-4936-903c-c337c794a8c7'),
       ('e98a5bbf-c94e-488a-9e83-9025faa7c75a', 0, 'ISRP-RGPool03', '', 4, 6, 20,
        'a7556146-23a6-4936-903c-c337c794a8c7'),
       ('85bb749a-3666-43a7-aa6e-87c6bfcb8201', 0, 'ISRP-RGPool04', '', 4, 6, 20,
        'a7556146-23a6-4936-903c-c337c794a8c7');


INSERT INTO public.resource_group_pool_resource_groups (resource_group_pool_id, resource_groups_id)
VALUES ('5407b59a-d4b0-4fcd-aea6-12b2101f628a', '1b0912df-c4c0-4907-9dd4-b09573a3ef44'),
       ('37b6aad6-7cfd-472a-9a93-d9dc653fdbca', 'dfe85896-7c82-41c6-ba31-f9401d10c4f2'),
       ('e98a5bbf-c94e-488a-9e83-9025faa7c75a', '0454b258-1457-4719-99b6-a9cc9576de2d'),
       ('85bb749a-3666-43a7-aa6e-87c6bfcb8201', 'a1529025-aae8-4f33-b5cf-295353d77c48');


--- Example reservations ---

INSERT INTO public.reservation (id, version, rg_id, team_id, automatic_startup, notification_time, reservation_start,
                                reservation_end, status)
VALUES ('181426fb-6cb6-4fd9-9801-cbd03deccc2d', 0, '1b0912df-c4c0-4907-9dd4-b09573a3ef44',
        'f15e7fe3-60a6-4d2c-a124-ad763f6869e2', true, 0, timestamp 'yesterday' + interval '11 hours',
        timestamp 'yesterday' + interval '17 hours', 'COMPLETED'),
       ('24f78aff-d112-4233-b02f-e747854bcd23', 0, '1b0912df-c4c0-4907-9dd4-b09573a3ef44',
        'f15e7fe3-60a6-4d2c-a124-ad763f6869e2', true, 15, timestamp 'today' + interval '11 hours',
        timestamp 'today' + interval '17 hours', 'IN_PROGRESS'),
       ('73f95e89-3a47-4dd5-bb51-382f79d1b976', 0, '1b0912df-c4c0-4907-9dd4-b09573a3ef44',
        'f15e7fe3-60a6-4d2c-a124-ad763f6869e2', true, 0, timestamp 'tomorrow' + interval '11 hours',
        timestamp 'tomorrow' + interval '17 hours', 'PENDING');

-----------------------------------
--- Sieciowe systemy baz danych ---
-----------------------------------

INSERT INTO public.course_teachers (course_id, teachers_user_id)
VALUES ('e485ded6-c166-45f6-a924-13ce44666f7a', '4edf0d7d-e5cc-4ba5-a7a9-8acaa2763eca');

--- Teams ---

INSERT INTO public.team (id, version, name, active, max_size, course_id)
VALUES ('f3896c36-2133-4497-965e-0951e1f5aebf', 0, 'EventSymphony', true, 7, 'e485ded6-c166-45f6-a924-13ce44666f7a'),
       ('78908655-ee18-4863-9eef-e67519940a0b', 0, 'LandlordKingdom', true, 7, 'e485ded6-c166-45f6-a924-13ce44666f7a'),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', 0, 'Eldorado', true, 7, 'e485ded6-c166-45f6-a924-13ce44666f7a');

INSERT INTO public.access_key (id, key_value)
VALUES ('18ed4422-1976-4296-9924-56e8a03e59a3', 'SSBD-Team01-Key'),
       ('a6dcf8e8-c317-4d50-b2f7-41012856918b', 'SSBD-Team02-Key'),
       ('60ea45d0-f657-4a43-bc45-a4ea6ee6db88', 'SSBD-Team03-Key');

INSERT INTO public.team_access_key (id, team_id)
VALUES ('18ed4422-1976-4296-9924-56e8a03e59a3', 'f3896c36-2133-4497-965e-0951e1f5aebf'),
       ('a6dcf8e8-c317-4d50-b2f7-41012856918b', '78908655-ee18-4863-9eef-e67519940a0b'),
       ('60ea45d0-f657-4a43-bc45-a4ea6ee6db88', '40517c17-58b9-41ce-b53e-abaf0e7782fd');

INSERT INTO public.team_users (team_id, user_id)
VALUES ('f3896c36-2133-4497-965e-0951e1f5aebf', '4e88cecc-fa80-4145-b5a8-e2e4acf24279'),
       ('f3896c36-2133-4497-965e-0951e1f5aebf', 'c7ceee90-734d-4e6c-8756-2cd9a44ff668'),
       ('f3896c36-2133-4497-965e-0951e1f5aebf', 'f00de025-d801-4276-9b53-9d5ab8f89ac8'),
       ('f3896c36-2133-4497-965e-0951e1f5aebf', '44b21e72-50d2-42c0-a449-4791177251aa'),
       ('f3896c36-2133-4497-965e-0951e1f5aebf', '99403b0e-c184-4d18-95b6-384cb322b449'),
       ('f3896c36-2133-4497-965e-0951e1f5aebf', 'b9d89a2a-75ff-4ca4-a9b4-068aab962524'),
       ('f3896c36-2133-4497-965e-0951e1f5aebf', '8551a9c9-0877-4831-b520-bcc8a137cf7c'),
       ('78908655-ee18-4863-9eef-e67519940a0b', '5ed87f00-bebc-41ab-aebb-66c587335065'),
       ('78908655-ee18-4863-9eef-e67519940a0b', '348213e4-7895-4a06-809c-35fe174eee5a'),
       ('78908655-ee18-4863-9eef-e67519940a0b', '08304074-48c3-48b8-8693-342c9add70a6'),
       ('78908655-ee18-4863-9eef-e67519940a0b', 'e29eadad-0fea-4c08-afe1-15c8b931de3c'),
       ('78908655-ee18-4863-9eef-e67519940a0b', '7be81ff0-f42f-4bdf-8002-792b10a2181d'),
       ('78908655-ee18-4863-9eef-e67519940a0b', '79b84d69-9761-4c2e-b9d6-223cd55cd917'),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', '4edf0d7d-e5cc-4ba5-a7a9-8acaa2763eca'),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', '582d50de-6eab-4343-a2d2-0157c8a6c14d'),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', '6b599d28-d631-41e5-ab76-f66369bccb14'),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', '04adeaa1-c232-46bc-aa0c-caab2e0efacd'),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', '5d2842e9-a219-4afa-bbb9-28cfce5082e8'),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', 'a053ddfa-4308-4064-bf16-a07d830bd9fa'),
       ('40517c17-58b9-41ce-b53e-abaf0e7782fd', 'a666ee8c-4b1a-413a-8e89-c45a688f5d1a');

--- Resource groups ---

INSERT INTO public.resource_group (id, version, name, description, stateless, max_rent_time)
VALUES ('692bde41-c8ca-4873-bbaf-edb789ae7c87', 0, 'SSBD-RG01', '', false, 6),
       ('f13f85d1-14bf-4930-bc2a-b044f3feebe2', 0, 'SSBD-RG02', '', false, 6),
       ('50c319f6-d29d-4193-bfef-5e33b4e26353', 0, 'SSBD-RG03', '', false, 6);

INSERT INTO public.virtual_machine (hidden, id, resource_group_id, version)
VALUES (false, 'e3bfbebc-2497-45d3-b298-604039640a75', '692bde41-c8ca-4873-bbaf-edb789ae7c87', 0);

--- Stateful pods ---

INSERT INTO public.pod_stateful (id, course_id, rg_id, team_id, max_rent)
VALUES ('a57e55b8-471d-4754-acf0-15c57b877e99', 'e485ded6-c166-45f6-a924-13ce44666f7a',
        '692bde41-c8ca-4873-bbaf-edb789ae7c87', 'f3896c36-2133-4497-965e-0951e1f5aebf', 3),
       ('849f00e3-199d-4e81-9892-5b5c2a3b5b7d', 'e485ded6-c166-45f6-a924-13ce44666f7a',
        'f13f85d1-14bf-4930-bc2a-b044f3feebe2', '78908655-ee18-4863-9eef-e67519940a0b', 3),
       ('a5681e23-bcec-4353-9e4d-f0c60f2efb26', 'e485ded6-c166-45f6-a924-13ce44666f7a',
        '50c319f6-d29d-4193-bfef-5e33b4e26353', '40517c17-58b9-41ce-b53e-abaf0e7782fd', 3);

--- Resource group pools ---

INSERT INTO public.resource_group_pool (id, version, name, grace_period, max_rent, course_id, description,
                                        max_rent_time)
VALUES ('03d31c2f-d300-45af-a8f8-37c894d7a527', 0, 'SSBD-RGPool01', 12, 6, 'e485ded6-c166-45f6-a924-13ce44666f7a', '',
        6),
       ('0e484149-67a8-4c1c-80b6-901fa737f2c9', 0, 'SSBD-RGPool02', 12, 6, 'e485ded6-c166-45f6-a924-13ce44666f7a', '',
        6),
       ('9872c8c5-70e9-432e-8a29-7c6dafd35ddc', 0, 'SSBD-RGPool03', 12, 6, 'e485ded6-c166-45f6-a924-13ce44666f7a', '',
        6);


INSERT INTO public.resource_group_pool_resource_groups (resource_group_pool_id, resource_groups_id)
VALUES ('03d31c2f-d300-45af-a8f8-37c894d7a527', '692bde41-c8ca-4873-bbaf-edb789ae7c87'),
       ('0e484149-67a8-4c1c-80b6-901fa737f2c9', 'f13f85d1-14bf-4930-bc2a-b044f3feebe2'),
       ('9872c8c5-70e9-432e-8a29-7c6dafd35ddc', '50c319f6-d29d-4193-bfef-5e33b4e26353');

-------------------------------------
--- Techniki utrzymania aplikacji ---
-------------------------------------

INSERT INTO public.course_teachers (course_id, teachers_user_id)
VALUES ('1decd050-1328-4eca-b2de-84793a8474c2', '4edf0d7d-e5cc-4ba5-a7a9-8acaa2763eca');

--- Teams ---

INSERT INTO public.team (id, version, name, active, max_size, course_id)
VALUES ('d46387ee-7397-4184-91eb-d01d5f301c0e', 0, 'EventSymphony', true, 7, '1decd050-1328-4eca-b2de-84793a8474c2'),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', 0, 'LandlordKingdom', true, 7, '1decd050-1328-4eca-b2de-84793a8474c2'),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', 0, 'Eldorado', true, 7, '1decd050-1328-4eca-b2de-84793a8474c2');

INSERT INTO public.access_key (id, key_value)
VALUES ('91f834d9-3c1c-460e-b28d-0e9f46a8b5ac', 'TUA-Team01-Key'),
       ('c3e69479-681a-4b61-b627-e25f03c72eee', 'TUA-Team02-Key'),
       ('90cf4fdd-53b2-4062-b162-1d3c6f4c3ec0', 'TUA-Team03-Key');

INSERT INTO public.team_access_key (id, team_id)
VALUES ('91f834d9-3c1c-460e-b28d-0e9f46a8b5ac', 'd46387ee-7397-4184-91eb-d01d5f301c0e'),
       ('c3e69479-681a-4b61-b627-e25f03c72eee', 'e608c9d0-e871-4374-a052-f27ced4a9cec'),
       ('90cf4fdd-53b2-4062-b162-1d3c6f4c3ec0', '18750e93-22a7-4a23-8f8b-e0cabde8f793');

INSERT INTO public.team_users (team_id, user_id)
VALUES ('d46387ee-7397-4184-91eb-d01d5f301c0e', '4e88cecc-fa80-4145-b5a8-e2e4acf24279'),
       ('d46387ee-7397-4184-91eb-d01d5f301c0e', 'c7ceee90-734d-4e6c-8756-2cd9a44ff668'),
       ('d46387ee-7397-4184-91eb-d01d5f301c0e', 'f00de025-d801-4276-9b53-9d5ab8f89ac8'),
       ('d46387ee-7397-4184-91eb-d01d5f301c0e', '44b21e72-50d2-42c0-a449-4791177251aa'),
       ('d46387ee-7397-4184-91eb-d01d5f301c0e', '99403b0e-c184-4d18-95b6-384cb322b449'),
       ('d46387ee-7397-4184-91eb-d01d5f301c0e', 'b9d89a2a-75ff-4ca4-a9b4-068aab962524'),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', '8551a9c9-0877-4831-b520-bcc8a137cf7c'),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', '5ed87f00-bebc-41ab-aebb-66c587335065'),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', '348213e4-7895-4a06-809c-35fe174eee5a'),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', '08304074-48c3-48b8-8693-342c9add70a6'),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', 'e29eadad-0fea-4c08-afe1-15c8b931de3c'),
       ('e608c9d0-e871-4374-a052-f27ced4a9cec', '7be81ff0-f42f-4bdf-8002-792b10a2181d'),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', '4edf0d7d-e5cc-4ba5-a7a9-8acaa2763eca'),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', '9b2db558-1ee7-47e1-9fd2-6626ec95d230'),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', '582d50de-6eab-4343-a2d2-0157c8a6c14d'),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', '6b599d28-d631-41e5-ab76-f66369bccb14'),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', '04adeaa1-c232-46bc-aa0c-caab2e0efacd'),
       ('18750e93-22a7-4a23-8f8b-e0cabde8f793', '5d2842e9-a219-4afa-bbb9-28cfce5082e8');

--- Resource groups ---

INSERT INTO public.resource_group (id, version, name, description, stateless, max_rent_time)
VALUES ('a7919551-3807-4b35-88e3-fc3a868ba014', 0, 'TUA-RG01', '', false, 6),
       ('e51132d3-8ac3-4232-85e0-849c8afa6abc', 0, 'TUA-RG02', '', false, 6),
       ('7c7ed665-831c-4e24-b6fe-1947a16fcadb', 0, 'TUA-RG03', '', false, 6);

INSERT INTO public.course_state_full_resource_groups (course_id, state_full_resource_groups_id)
VALUES ('1decd050-1328-4eca-b2de-84793a8474c2', 'a7919551-3807-4b35-88e3-fc3a868ba014'),
       ('1decd050-1328-4eca-b2de-84793a8474c2', 'e51132d3-8ac3-4232-85e0-849c8afa6abc'),
       ('1decd050-1328-4eca-b2de-84793a8474c2', '7c7ed665-831c-4e24-b6fe-1947a16fcadb');

--- Stateful pods ---

INSERT INTO public.pod_stateful (id, course_id, rg_id, team_id, max_rent)
VALUES ('c971b368-92c8-4a01-86d2-dd783b5360a8', '1decd050-1328-4eca-b2de-84793a8474c2','a7919551-3807-4b35-88e3-fc3a868ba014', 'd46387ee-7397-4184-91eb-d01d5f301c0e', 3),
       ('161f4a45-f87d-40b5-9ad6-1ec056485e01', '1decd050-1328-4eca-b2de-84793a8474c2','e51132d3-8ac3-4232-85e0-849c8afa6abc', 'e608c9d0-e871-4374-a052-f27ced4a9cec', 3),
       ('c3db873c-cfeb-4ec2-b87b-342af78f869a', '1decd050-1328-4eca-b2de-84793a8474c2','7c7ed665-831c-4e24-b6fe-1947a16fcadb', '18750e93-22a7-4a23-8f8b-e0cabde8f793', 3);