-- Deduplicate using CTE to partition and rank records by (employee_id, date)
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY employee_id, date
               ORDER BY id ASC
           ) AS rn
    FROM public.attendance
)
DELETE FROM public.attendance
WHERE id IN (
    SELECT id FROM ranked WHERE rn > 1
);

-- Add unique constraint to guarantee one record per employee per day
ALTER TABLE public.attendance
    ADD CONSTRAINT unique_employee_date UNIQUE (employee_id, date);
