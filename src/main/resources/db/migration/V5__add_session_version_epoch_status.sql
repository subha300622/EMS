-- Migration to add session_version, session_epoch, and status to user_sessions table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='user_sessions' AND column_name='session_version') THEN
        ALTER TABLE public.user_sessions ADD COLUMN session_version integer NOT NULL DEFAULT 1;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='user_sessions' AND column_name='session_epoch') THEN
        ALTER TABLE public.user_sessions ADD COLUMN session_epoch bigint NOT NULL DEFAULT 1;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='user_sessions' AND column_name='status') THEN
        ALTER TABLE public.user_sessions ADD COLUMN status character varying(50) NOT NULL DEFAULT 'ACTIVE';
    END IF;
END $$;
