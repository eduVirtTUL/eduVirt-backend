INSERT INTO public.i72_metric (id, name)
VALUES ('2865efff-f8e5-4960-a0ce-fc05e98828ba', 'cpu_count'),
       ('63490da4-d0f1-4e7a-88fc-3342633accc0', 'memory_size');

INSERT INTO public.i72_metric_cluster (cluster_id, metric_id, metric_value)
VALUES ('c16d4c0e-9d42-11ef-82b3-d20f15000104', '2865efff-f8e5-4960-a0ce-fc05e98828ba', 100),
       ('c16d4c0e-9d42-11ef-82b3-d20f15000104', '63490da4-d0f1-4e7a-88fc-3342633accc0', 1073741824);