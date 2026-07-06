-- Create daily revenue materialized view
CREATE MATERIALIZED VIEW public.mv_revenue_daily_summary AS
 SELECT COALESCE(p.paid_at, p.created_at)::date AS summary_date,
    COALESCE(sum(p.amount), 0.00) AS gross_revenue,
    COALESCE(sum(p.amount - COALESCE(p.refund_amount, 0.00)), 0.00) AS net_revenue,
    COALESCE(sum(COALESCE(p.refund_amount, 0.00)), 0.00) AS refund_amount,
    COALESCE(sum(COALESCE(i.tax, 0.00)), 0.00) AS tax_collected,
    COALESCE(sum(COALESCE(i.discount, 0.00)), 0.00) AS discount_amount,
    count(
        CASE
            WHEN p.status::text = 'SUCCESS'::text THEN 1
            ELSE NULL::integer
        END) AS successful_payments,
    count(
        CASE
            WHEN p.status::text = 'FAILED'::text THEN 1
            ELSE NULL::integer
        END) AS failed_payments
   FROM payments p
     LEFT JOIN subscription_invoices i ON p.invoice_id = i.id
  GROUP BY (COALESCE(p.paid_at, p.created_at)::date);

CREATE UNIQUE INDEX idx_mv_revenue_daily_summary ON public.mv_revenue_daily_summary (summary_date);

-- Create monthly revenue materialized view
CREATE MATERIALIZED VIEW public.mv_revenue_monthly_summary AS
 SELECT to_char(COALESCE(p.paid_at, p.created_at), 'YYYY-MM'::text) AS summary_month,
    COALESCE(sum(p.amount), 0.00) AS gross_revenue,
    COALESCE(sum(p.amount - COALESCE(p.refund_amount, 0.00)), 0.00) AS net_revenue,
    COALESCE(sum(COALESCE(p.refund_amount, 0.00)), 0.00) AS refund_amount,
    COALESCE(sum(COALESCE(i.tax, 0.00)), 0.00) AS tax_collected,
    COALESCE(sum(COALESCE(i.discount, 0.00)), 0.00) AS discount_amount,
    count(
        CASE
            WHEN p.status::text = 'SUCCESS'::text THEN 1
            ELSE NULL::integer
        END) AS successful_payments,
    count(
        CASE
            WHEN p.status::text = 'FAILED'::text THEN 1
            ELSE NULL::integer
        END) AS failed_payments
   FROM payments p
     LEFT JOIN subscription_invoices i ON p.invoice_id = i.id
  GROUP BY (to_char(COALESCE(p.paid_at, p.created_at), 'YYYY-MM'::text));

CREATE UNIQUE INDEX idx_mv_revenue_monthly_summary ON public.mv_revenue_monthly_summary (summary_month);
