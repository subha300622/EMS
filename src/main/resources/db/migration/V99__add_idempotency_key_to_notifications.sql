-- V99: Add idempotency_key to notifications table for deduplication and race-condition safety
ALTER TABLE public.notifications ADD COLUMN IF NOT EXISTS idempotency_key character varying(150);

DROP INDEX IF EXISTS public.idx_notifications_idempotency_key;

CREATE UNIQUE INDEX IF NOT EXISTS idx_notifications_idempotency_key 
ON public.notifications (idempotency_key);
