-- V77: Enforce unique constraint for 1 employee + 1 cycle = 1 appraisal
CREATE UNIQUE INDEX IF NOT EXISTS uq_appraisals_cycle_emp 
ON public.appraisals (cycle_id, employee_id) 
WHERE cycle_id IS NOT NULL;
