-- User
CREATE USER eduvirt PASSWORD 'pass';

-- Grants
GRANT SELECT ON public.vnic_profiles TO eduvirt;
GRANT SELECT ON public.cluster TO eduvirt;
