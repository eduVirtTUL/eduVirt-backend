alter table public.executor_subtask
    alter column description type varchar(500);

create index reservation_status_idx
    on public.reservation (status);

create index executor_task_type_idx
    on public.executor_task (type);

create index executor_task_status_idx
    on public.executor_task (status);

create index executor_subtask_task_id_idx
    on public.executor_subtask (task_id);

create index mail_notification_type_idx
    on public.mail_notification (type);
