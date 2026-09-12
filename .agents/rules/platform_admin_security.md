---
trigger: always_on
---

# Platform Admin & Multi-Tenant Authorization Security Rules

## Core Access Model

1. **JWT & Identity**:
   - `JWT` identifies **WHO** is calling (e.g. `PLATFORM_ADMIN` vs normal tenant `Organization User`).
   - Permissions determine **WHAT** actions the user can perform.
   - Organization Context determines **WHICH** organization's data is the target of the operation.

2. **Platform Admin Authorization & Request Scoping**:
   - `PLATFORM_ADMIN` users can access plaListed directory ems-backend
Created platform_admin_security.md

Understood! 💙 

I have created and stored your **Platform Admin & Multi-Tenant Authorization Security Rules** in [`.agents/rules/platform_admin_security.md`](file:///home/subashini/Documents/ems-backend/.agents/rules/platform_admin_security.md).

### Summary of Enforced Rule Architecture:

1. **Authentication & Identity (`JWT`)**:
   - `JWT` identifies **WHO** is calling (`PLATFORM_ADMIN` vs tenant `Organization User`).
   - Permissions control **WHAT** operations can be executed.
   - Organization Context defines **WHICH** organization's data is targeted.

2. **Platform Admin Scoping**:
   - `PLATFORM_ADMIN` can access platform management APIs and target tenant data via explicit platform-admin endpoints (e.g. `GET /api/v1/platform/organizations/{orgId}/employees`).
   - `organizationId` must **NEVER** be supplied in API request bodies.
   - Target tenant selection is explicitly passed via path/query parameters on platform-admin endpoints and validated by backend security policies.

3. **Tenant User Isolation**:
   - Normal users are automatically restricted to their own tenant via `TenantContext.requireOrganizationId()` derived from their JWT.
   - Clients cannot manipulate or override the tenant context to view other organizations' data.tform-wide APIs and organization-specific data via dedicated platform-admin endpoints (e.g., `/api/v1/platform/organizations/{orgId}/...`).
   - `organizationId` must **NEVER** be supplied in API request bodies.
   - For `PLATFORM_ADMIN` requesting a specific tenant's data, the target organization MUST be specified explicitly through a safe path parameter or query parameter on a dedicated platform-admin endpoint (e.g., `GET /api/v1/platform/organizations/{orgId}/employees`), where backend code validates platform access.
   - Never trust an `organizationId` sent blindly in request bodies or client-supplied attributes without backend authorization checks.

3. **Normal Organization User Authorization**:
   - For normal organization users, `organizationId` is automatically extracted from their authenticated JWT (`TenantContext.requireOrganizationId()`).
   - Queries and commands are strictly scoped to `organization_id = TenantContext.requireOrganizationId()`. Users cannot change or override their tenant scope to access another organization's data.
