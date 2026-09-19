CREATE TABLE public.idempotency_keys (
    idempotency_key character varying(255) NOT NULL,
    status character varying(20) NOT NULL, -- 'PROCESSING', 'COMPLETED', 'FAILED'
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    request_hash text,
    CONSTRAINT idempotency_keys_pkey PRIMARY KEY (idempotency_key)
);

UPDATE public.subscription_invoices SET status = 'ISSUED' WHERE status = 'PENDING';
