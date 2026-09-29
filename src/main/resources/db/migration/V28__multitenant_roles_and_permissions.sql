-- Drop the global unique constraint on role name
ALTER TABLE ONLY public.roles DROP CONSTRAINT IF EXISTS ukofx66keruapi6vyqpv6f2or37;

-- Add new columns for multi-tenant support, versioning, and system protection
ALTER TABLE public.roles ADD COLUMN organization_id bigint;
ALTER TABLE public.roles ADD COLUMN is_platform_template boolean NOT NULL DEFAULT false;
ALTER TABLE public.roles ADD COLUMN version integer NOT NULL DEFAULT 1;
ALTER TABLE public.roles ADD COLUMN system_role boolean NOT NULL DEFAULT false;

-- Add Foreign Key constraint to organizations
ALTER TABLE ONLY public.roles
    ADD CONSTRAINT fk_roles_organization FOREIGN KEY (organization_id) REFERENCES public.organizations(id);

-- Create composite unique index for tenant roles (where organization_id is not null)
CREATE UNIQUE INDEX idx_roles_org_name ON public.roles (organization_id, name) WHERE organization_id IS NOT NULL;

-- Create unique index for platform role templates (where organization_id is null)
CREATE UNIQUE INDEX idx_roles_template_name ON public.roles (name) WHERE organization_id IS NULL;
