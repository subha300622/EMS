-- Migration to add revoked_at and revocation_event_id to user_sessions table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='user_sessions' AND column_name='revoked_at') THEN
        ALTER TABLE public.user_sessions ADD COLUMN revoked_at timestamp without time zone;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='user_sessions' AND column_name='revocation_event_id') THEN
        ALTER TABLE public.user_sessions ADD COLUMN revocation_event_id character varying(255);
    END IF;
END $$;
