-- V24: Add message column to support_notifications table
ALTER TABLE public.support_notifications ADD COLUMN message character varying(1000) NOT NULL;
