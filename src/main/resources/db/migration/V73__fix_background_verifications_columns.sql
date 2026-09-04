-- Migration to fix background_verifications table columns if V72 was already executed on an existing DB
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'background_verifications' AND column_name = 'agency') THEN
        ALTER TABLE public.background_verifications RENAME COLUMN agency TO verification_agency;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'background_verifications' AND column_name = 'notes') THEN
        ALTER TABLE public.background_verifications RENAME COLUMN notes TO reports_metadata;
    END IF;
END $$;
