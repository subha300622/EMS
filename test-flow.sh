#!/bin/bash
# ============================================================
# EMS Full Auth Flow Test Script
# ============================================================
set -e
BASE="http://localhost:8080/api/v1"

echo "============================================"
echo "  EMS Login → Role → Permission → Access"
echo "============================================"

# ── Step 1: Login as SUPER_ADMIN ─────────────────────────────
echo ""
echo "▶ Step 1: SUPER_ADMIN Login"
LOGIN=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"super_admin@company.com","password":"super_admin@1"}')
echo "Response: $LOGIN"

TOKEN=$(echo "$LOGIN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('tokens',{}).get('accessToken',''))" 2>/dev/null)
if [ -z "$TOKEN" ]; then
  echo "❌ Login failed or token not found"
  echo "Response was: $LOGIN"
  exit 1
fi
echo "✅ Token obtained"
AUTH="Authorization: Bearer $TOKEN"

# ── Step 2: List all Roles ───────────────────────────────────
echo ""
echo "▶ Step 2: Get All Roles"
curl -s "$BASE/roles" -H "$AUTH" | python3 -m json.tool

# ── Step 3: List all Permissions ─────────────────────────────
echo ""
echo "▶ Step 3: Get All Permissions"
curl -s "$BASE/permissions" -H "$AUTH" -H "X-Admin-Email: super_admin@company.com" | python3 -m json.tool

# ── Step 4: Create a new test user ───────────────────────────
echo ""
echo "▶ Step 4: Create New User (test.manager@company.com)"
NEW_USER=$(curl -s -X POST "$BASE/users" \
  -H "$AUTH" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test Manager",
    "workEmail": "test.manager@company.com",
    "mobileNumber": "9876543210",
    "password": "TestPass@123",
    "confirmPassword": "TestPass@123",
    "role": "MANAGER",
    "department": "Engineering",
    "location": "HQ"
  }')
echo "Response: $NEW_USER"
USER_ID=$(echo "$NEW_USER" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('id',''))" 2>/dev/null)
echo "New User Internal ID: $USER_ID"

# ── Step 5: Get MANAGER role ID ──────────────────────────────
echo ""
echo "▶ Step 5: Get MANAGER Role ID"
ROLES=$(curl -s "$BASE/roles" -H "$AUTH")
MANAGER_ROLE_ID=$(echo "$ROLES" | python3 -c "
import sys,json
roles=json.load(sys.stdin)
data=roles.get('data',[]) if isinstance(roles, dict) else roles
for r in data:
    if r.get('name')=='MANAGER':
        print(r.get('id',''))
" 2>/dev/null)
echo "MANAGER Role ID: $MANAGER_ROLE_ID"

# ── Step 6: Assign MANAGER role to new user ──────────────────
if [ -n "$USER_ID" ] && [ -n "$MANAGER_ROLE_ID" ]; then
  echo ""
  echo "▶ Step 6: Assign MANAGER Role (ID=$MANAGER_ROLE_ID) to User (ID=$USER_ID)"
  ASSIGN=$(curl -s -X POST "$BASE/users/$USER_ID/assign-role" \
    -H "$AUTH" \
    -H "Content-Type: application/json" \
    -d "{\"roleId\": $MANAGER_ROLE_ID}")
  echo "Response: $ASSIGN"
else
  echo "⚠ Skipping role assignment (user or role not found)"
fi

# ── Step 7: Login as the new Manager user ────────────────────
echo ""
echo "▶ Step 7: Login as test.manager@company.com"
MGR_LOGIN=$(curl -s -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"test.manager@company.com","password":"TestPass@123"}')
echo "Response: $MGR_LOGIN"
MGR_TOKEN=$(echo "$MGR_LOGIN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('tokens',{}).get('accessToken',''))" 2>/dev/null)
if [ -n "$MGR_TOKEN" ]; then
  echo "✅ Manager logged in"
  MGR_AUTH="Authorization: Bearer $MGR_TOKEN"

  # ── Step 8: Manager reads team employees ─────────────────────
  echo ""
  echo "▶ Step 8: Manager reads employees (requires employee.team.read)"
  curl -s "$BASE/employees" -H "$MGR_AUTH" | python3 -m json.tool
else
  echo "⚠ Manager login failed"
fi

# ── Step 9: Verify SUPER_ADMIN permissions via /employees/me/profile ─
echo ""
echo "▶ Step 9: SUPER_ADMIN Profile (should show role=SUPER_ADMIN)"
curl -s "$BASE/employees/me/profile" -H "$AUTH" | python3 -m json.tool

echo ""
echo "============================================"
echo "  Flow Test Complete"
echo "============================================"
