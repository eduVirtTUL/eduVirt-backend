alter table public.vnic_profile_pool
    add column name varchar(50) not null;

alter table public.vnic_profile_pool
    add column network_name varchar(50) not null;