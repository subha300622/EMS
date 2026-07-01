-- V11: Fix appraisals rating columns from INTEGER to DOUBLE PRECISION
-- These columns were originally created as INTEGER but the Appraisal entity
-- maps them as Double (float(53) / double precision). This migration corrects
-- the schema to match the entity, resolving Hibernate schema validation failure.

ALTER TABLE appraisals
    ALTER COLUMN final_rating TYPE double precision USING final_rating::double precision,
    ALTER COLUMN manager_rating TYPE double precision USING manager_rating::double precision,
    ALTER COLUMN self_rating TYPE double precision USING self_rating::double precision;
