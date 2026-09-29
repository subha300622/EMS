ALTER TABLE public.users ADD COLUMN IF NOT EXISTS organization_name character varying(255);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS branch character varying(255);
