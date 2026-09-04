-- Create subscription_metrics_daily table
CREATE TABLE public.subscription_metrics_daily (
    date date NOT NULL,
    total_mrr numeric(20, 2) NOT NULL DEFAULT 0.00,
    active_subscriptions integer NOT NULL DEFAULT 0,
    revenue_collected numeric(20, 2) NOT NULL DEFAULT 0.00,
    churn_rate numeric(5, 2) NOT NULL DEFAULT 0.00,
    pending_invoices_value numeric(20, 2) NOT NULL DEFAULT 0.00,
    updated_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT subscription_metrics_daily_pkey PRIMARY KEY (date)
);

-- Create subscription_plan_summary table
CREATE TABLE public.subscription_plan_summary (
    plan_code character varying(50) NOT NULL,
    organization_count integer NOT NULL DEFAULT 0,
    mrr numeric(20, 2) NOT NULL DEFAULT 0.00,
    updated_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT subscription_plan_summary_pkey PRIMARY KEY (plan_code)
);

-- Create subscription_renewals_view
CREATE OR REPLACE VIEW public.subscription_renewals_view AS
SELECT 
    s.id AS subscription_id,
    o.id AS organization_id,
    o.name AS organization_name,
    s.plan_code AS plan,
    s.expiry_date AS renewal_date,
    (s.expiry_date - CURRENT_DATE) AS days_left,
    COALESCE((s.billing_info->>'finalAmount')::numeric, 0.00) AS amount,
    COALESCE(s.billing_info->>'cycle', 'YEARLY') AS billing_cycle
FROM public.subscriptions s
JOIN public.organizations o ON s.organization_id = o.id
WHERE s.status = 'ACTIVE' AND o.is_deleted = false;
