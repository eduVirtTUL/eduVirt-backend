alter table public.course_teachers
    rename to course_teacher;

alter table public.course_teacher
    rename column teachers_user_id to teacher_id;

alter table public.team_users
    rename to team_user;

alter table public.user_roles
    rename to user_role;
