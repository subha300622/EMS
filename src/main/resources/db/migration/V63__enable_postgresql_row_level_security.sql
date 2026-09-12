-- =============================================================================
-- Flyway Migration: V63__enable_postgresql_row_level_security.sql
-- Enables PostgreSQL Row-Level Security (RLS) policies on tenant-owned tables
-- protecting SELECT, INSERT, UPDATE, and DELETE operations.
-- FORCE ROW LEVEL SECURITY ensures table owners cannot bypass RLS.
-- =============================================================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'goals') THEN
        ALTER TABLE goals ENABLE ROW LEVEL SECURITY;
        ALTER TABLE goals FORCE ROW LEVEL SECURITY;
        
        DROP POLICY IF EXISTS tenant_isolation_goals_policy ON goals;
        CREATE POLICY tenant_isolation_goals_policy ON goals
            FOR ALL
            USING (
                organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                OR current_setting('app.is_platform_admin', true) = 'true'
            )
            WITH CHECK (
                organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                OR current_setting('app.is_platform_admin', true) = 'true'
            );
    END IF;

    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'employees') THEN
        ALTER TABLE employees ENABLE ROW LEVEL SECURITY;
        ALTER TABLE employees FORCE ROW LEVEL SECURITY;

        DROP POLICY IF EXISTS tenant_isolation_employees_policy ON employees;
        CREATE POLICY tenant_isolation_employees_policy ON employees
            FOR ALL
            USING (
                organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                OR current_setting('app.is_platform_admin', true) = 'true'
            )
            WITH CHECK (
                organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                OR current_setting('app.is_platform_admin', true) = 'true'
            );
    END IF;

    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'teams') THEN
        ALTER TABLE teams ENABLE ROW LEVEL SECURITY;
        ALTER TABLE teams FORCE ROW LEVEL SECURITY;

        DROP POLICY IF EXISTS tenant_isolation_teams_policy ON teams;
        CREATE POLICY tenant_isolation_teams_policy ON teams
            FOR ALL
            USING (
                organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                OR current_setting('app.is_platform_admin', true) = 'true'
            )
            WITH CHECK (
                organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                OR current_setting('app.is_platform_admin', true) = 'true'
            );
    END IF;

    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'departments') THEN
        ALTER TABLE departments ENABLE ROW LEVEL SECURITY;
        ALTER TABLE departments FORCE ROW LEVEL SECURITY;

        DROP POLICY IF EXISTS tenant_isolation_departments_policy ON departments;
        CREATE POLICY tenant_isolation_departments_policy ON departments
            FOR ALL
            USING (
                organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                OR current_setting('app.is_platform_admin', true) = 'true'
            )
            WITH CHECK (
                organization_id = NULLIF(current_setting('app.current_tenant_id', true), '')::bigint
                OR current_setting('app.is_platform_admin', true) = 'true'
            );
    END IF;
END $$;
