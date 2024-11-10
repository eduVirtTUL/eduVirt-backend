-- User
CREATE USER eduvirt PASSWORD 'pass';

-- Schema
CREATE SCHEMA eduvirt;

ALTER SCHEMA eduvirt OWNER TO eduvirt;

-- Grants
GRANT SELECT, REFERENCES ON public.vnic_profiles TO eduvirt;
