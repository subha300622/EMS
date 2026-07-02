-- Drop priority level unique constraint to allow full SLA CRUD operations
ALTER TABLE public.support_sla DROP CONSTRAINT IF EXISTS support_sla_priority_key;

-- Extend support_sla table with descriptive and audit attributes
ALTER TABLE public.support_sla ADD COLUMN name character varying(255);
ALTER TABLE public.support_sla ADD COLUMN description character varying(1000);
ALTER TABLE public.support_sla ADD COLUMN is_default boolean DEFAULT false NOT NULL;
ALTER TABLE public.support_sla ADD COLUMN deleted boolean DEFAULT false NOT NULL;
ALTER TABLE public.support_sla ADD COLUMN created_by_id bigint;
ALTER TABLE public.support_sla ADD COLUMN updated_by_id bigint;
ALTER TABLE public.support_sla ADD COLUMN created_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE public.support_sla ADD COLUMN updated_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL;

-- Audit relationships foreign keys
ALTER TABLE public.support_sla ADD CONSTRAINT fk_support_sla_created_by FOREIGN KEY (created_by_id) REFERENCES public.users(id);
ALTER TABLE public.support_sla ADD CONSTRAINT fk_support_sla_updated_by FOREIGN KEY (updated_by_id) REFERENCES public.users(id);

-- Update existing data to default names
UPDATE public.support_sla SET name = 'Critical Priority SLA', description = 'Default SLA for critical support requests.', is_default = true WHERE priority = 'CRITICAL' AND name IS NULL;
UPDATE public.support_sla SET name = 'High Priority SLA', description = 'Default SLA for high support requests.' WHERE priority = 'HIGH' AND name IS NULL;
UPDATE public.support_sla SET name = 'Medium Priority SLA', description = 'Default SLA for medium support requests.' WHERE priority = 'MEDIUM' AND name IS NULL;
UPDATE public.support_sla SET name = 'Low Priority SLA', description = 'Default SLA for low support requests.' WHERE priority = 'LOW' AND name IS NULL;
