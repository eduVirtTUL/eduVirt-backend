alter table public.cluster_metric
add column version bigint;

alter table public.cluster_metric
alter column version set not null;