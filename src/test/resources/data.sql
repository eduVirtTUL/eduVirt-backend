INSERT INTO users (user_id, email, ovirt_id, user_name)
VALUES ('0bc4aa42-7e86-4b85-aab7-ba99bc7267ec', 'admin@internal.test', gen_random_uuid(), 'admin@internal'),
       ('cd36990f-5b25-4b8b-8f72-6708f092d970', 'teacher@internal.test', gen_random_uuid(), 'teacher@internal'),
       ('f281a0de-b30e-4448-8469-d946c9e65157', 'student@internal.test', gen_random_uuid(), 'student@internal');

INSERT INTO user_role (user_id, role)
VALUES ('0bc4aa42-7e86-4b85-aab7-ba99bc7267ec', 'administrator'),
       ('cd36990f-5b25-4b8b-8f72-6708f092d970', 'teacher'),
       ('f281a0de-b30e-4448-8469-d946c9e65157', 'student');

INSERT INTO course (version, cluster_id, id, name, description, external_link, course_type)
VALUES (0, '0bc4aa42-7e86-4b85-aab7-ba99bc7267ec', 'fb74ef32-c0c4-41b8-83b2-f28725f0b01d', 'Course 1',
        'Course 1 description', 'http://external.link', 'SOLO'),
       (0, '0bc4aa42-7e86-4b85-aab7-ba99bc7267ec', '14010c73-120b-471d-95c2-3905c521d8d4', 'Course 2',
        'Course 2 description', 'http://external.link', 'SOLO');

INSERT INTO course_teacher (course_id, teacher_id)
VALUES ('fb74ef32-c0c4-41b8-83b2-f28725f0b01d', 'cd36990f-5b25-4b8b-8f72-6708f092d970');

INSERT INTO resource_group (max_rent_time, stateless, version, id, name, description)
VALUES (0, false, 0, 'f5be595f-1179-4d07-8c9f-d5ed2685a1d3', 'Resource Group 1', 'Resource Group 1 description'),
       (0, false, 0, '90e7a49d-4084-4872-b914-072a0afd27e2', 'Resource Group 2', 'Resource Group 2 description');

INSERT INTO course_resource_group (course_id, resource_group_id)
VALUES ('fb74ef32-c0c4-41b8-83b2-f28725f0b01d', 'f5be595f-1179-4d07-8c9f-d5ed2685a1d3'),
       ('fb74ef32-c0c4-41b8-83b2-f28725f0b01d', '90e7a49d-4084-4872-b914-072a0afd27e2');

INSERT INTO resource_group_network (id, resource_group_id, name)
VALUES ('1d6b0f55-f5f0-41af-bf1f-a7593c7c93f2', 'f5be595f-1179-4d07-8c9f-d5ed2685a1d3', 'Network 1');

INSERT INTO resource_group_pool (id, grace_period, max_rent, max_rent_time, _created_at, _updated_at, version,
                                 course_id, created_by, updated_by, name, description)
VALUES ('04a518e7-e537-4493-afc8-149410e45fcb', 0, 0, 0, now(), now(), 0, 'fb74ef32-c0c4-41b8-83b2-f28725f0b01d',
        'cd36990f-5b25-4b8b-8f72-6708f092d970', 'cd36990f-5b25-4b8b-8f72-6708f092d970', 'Pool 1', 'Pool 1 description'),
       ('09d21403-48c0-4c69-a0cd-7ad86af4318f', 0, 0, 0, now(), now(), 0, 'fb74ef32-c0c4-41b8-83b2-f28725f0b01d',
        'cd36990f-5b25-4b8b-8f72-6708f092d970', 'cd36990f-5b25-4b8b-8f72-6708f092d970', 'Pool 2', 'Pool 2 description');