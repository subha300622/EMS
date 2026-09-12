-- Migration to add attendance_type, location, server_time, is_late and late_by to attendance table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='attendance_type') THEN
        ALTER TABLE public.attendance ADD COLUMN attendance_type character varying(255);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='location') THEN
        ALTER TABLE public.attendance ADD COLUMN location character varying(255);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='server_time') THEN
        ALTER TABLE public.attendance ADD COLUMN server_time timestamp with time zone;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='is_late') THEN
        ALTER TABLE public.attendance ADD COLUMN is_late boolean DEFAULT false;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='attendance' AND column_name='late_by') THEN
        ALTER TABLE public.attendance ADD COLUMN late_by character varying(255);
    END IF;
END $$;
