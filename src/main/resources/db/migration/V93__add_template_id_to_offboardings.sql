-- V93: Add template_id to offboardings table for explicit template binding

ALTER TABLE public.offboardings
ADD COLUMN IF NOT EXISTS template_id bigint;

CREATE INDEX IF NOT EXISTS idx_offboardings_template_id
ON public.offboardings (template_id);
