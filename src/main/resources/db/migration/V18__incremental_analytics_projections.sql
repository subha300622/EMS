-- Add version tracking
ALTER TABLE public.subscription_metrics_daily
ADD COLUMN projection_version bigint NOT NULL DEFAULT 0;

-- Create analytics event idempotency & sequence guard
CREATE TABLE public.analytics_event_log (
    event_id character varying(255) NOT NULL,
    projection_type character varying(100) NOT NULL,
    subscription_id bigint,
    event_sequence bigint NOT NULL DEFAULT 0,
    processed_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT analytics_event_log_pkey PRIMARY KEY (event_id, projection_type)
);

-- Optimize analytics query scans
CREATE INDEX idx_metrics_date ON public.subscription_metrics_daily(date);
CREATE INDEX idx_plan_summary_plan ON public.subscription_plan_summary(plan_code);
