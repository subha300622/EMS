-- Drop old analytics event log table
DROP TABLE IF EXISTS public.analytics_event_log CASCADE;

-- Create immutable append-only event ledger
CREATE TABLE public.analytics_event_log (
    event_id character varying(255) NOT NULL,
    subscription_id bigint NOT NULL,
    event_type character varying(100) NOT NULL,
    event_sequence bigint NOT NULL,
    payload jsonb NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    event_global_sequence bigserial NOT NULL,
    CONSTRAINT analytics_event_log_pkey PRIMARY KEY (event_id)
);
