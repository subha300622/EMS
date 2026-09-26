-- 1. Create Rebuild Checkpoint Table
CREATE TABLE public.analytics_rebuild_checkpoint (
    checkpoint_key character varying(100) NOT NULL,
    last_completed_sequence bigint NOT NULL DEFAULT 0,
    updated_at timestamp(6) without time zone NOT NULL DEFAULT now(),
    CONSTRAINT analytics_rebuild_checkpoint_pkey PRIMARY KEY (checkpoint_key)
);

-- Seed billing checkpoint
INSERT INTO public.analytics_rebuild_checkpoint (checkpoint_key, last_completed_sequence)
VALUES ('BILLING_TELEMETRY', 0) ON CONFLICT DO NOTHING;

-- 2. Add event_id column as nullable first
ALTER TABLE public.analytics_payment_facts ADD COLUMN event_id character varying(255);

-- 3. Backfill
UPDATE public.analytics_payment_facts f
SET event_id = l.event_id
FROM public.analytics_event_log l
WHERE f.event_global_sequence = l.event_global_sequence;

-- 4. Apply fallback values for legacy/orphan facts to prevent validation failures
UPDATE public.analytics_payment_facts
SET event_id = 'legacy_orphan_' || id
WHERE event_id IS NULL;

-- 5. Enforce NOT NULL and UNIQUE constraint
ALTER TABLE public.analytics_payment_facts
ALTER COLUMN event_id SET NOT NULL,
ADD CONSTRAINT uq_facts_event_id UNIQUE (event_id);

CREATE INDEX idx_payment_facts_event_id ON public.analytics_payment_facts(event_id);
