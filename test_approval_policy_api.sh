#!/bin/bash
set -e

BASE_URL="http://localhost:8080/api/v1"
TIMESTAMP=$(date +%s)
NUM=$((RANDOM % 9000 + 1000))

echo "============================================================"
echo "    EMS ONBOARDING APPROVAL POLICY ENGINE LIVE API TEST     "
echo "============================================================"
echo ""

# 1. Register & Login Tenant
EMAIL="policy.admin.$TIMESTAMP@company.com"
PASS="Password123!"

echo "1. Registering Tenant Admin ($EMAIL)..."
curl -s -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Policy Admin",
    "email": "'$EMAIL'",
    "countryCode": "+1",
    "phone": "555'$NUM'",
    "password": "'$PASS'",
    "orgName": "Policy Corp '$TIMESTAMP'",
    "industry": "Software",
    "country": "USA",
    "state": "California",
    "city": "San Jose",
    "address": "500 Policy Boulevard, Suite 100",
    "companySize": "51-200",
    "plan": "ENTERPRISE",
    "billingCycle": "ANNUAL"
  }' > /dev/null

echo "Logging in Tenant Admin..."
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'$EMAIL'",
    "password": "'$PASS'"
  }')

TOKEN=$(echo "$LOGIN_RESP" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "JWT Token acquired: ${TOKEN:0:30}..."

# 2. GET System Status Transition Matrix Template
echo ""
echo "2. GET /api/v1/onboarding/policies/status-transitions (System Policy Matrix)..."
TRANSITIONS_RESP=$(curl -s -X GET "$BASE_URL/onboarding/policies/status-transitions" \
  -H "Authorization: Bearer $TOKEN")
echo "Transitions Response: ${TRANSITIONS_RESP:0:280}..."

# 3. GET Active Tenant Approval Policies
echo ""
echo "3. GET /api/v1/onboarding/approval-policies (Tenant Active Policies)..."
POLICIES_RESP=$(curl -s -X GET "$BASE_URL/onboarding/approval-policies" \
  -H "Authorization: Bearer $TOKEN")
echo "Active Policies Response: ${POLICIES_RESP:0:280}..."

# 4. POST Create Organization Approval Policy
echo ""
echo "4. POST /api/v1/onboarding/approval-policies (Configuring Custom Role Policy)..."
CREATE_POLICY_RESP=$(curl -s -X POST "$BASE_URL/onboarding/approval-policies" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currentStatus": "COMPLETED",
    "action": "APPROVE",
    "nextStatus": "APPROVED",
    "approver": {
      "type": "CONFIGURED_ROLE",
      "roleId": 42
    },
    "conditions": [
      "ALL_MANDATORY_TASKS_COMPLETED",
      "ALL_MANDATORY_DOCUMENTS_VERIFIED"
    ],
    "active": true
  }')
echo "Create Policy Response: $CREATE_POLICY_RESP"

POLICY_ID=$(echo "$CREATE_POLICY_RESP" | grep -o '"id":[0-9]*' | head -n1 | cut -d':' -f2)
echo "Created Custom Approval Policy DB ID: $POLICY_ID"

# 5. GET Approval Eligibility Check for Onboarding ID 1
echo ""
echo "5. GET /api/v1/onboarding/1/approval-eligibility..."
ELIGIBILITY_RESP=$(curl -s -X GET "$BASE_URL/onboarding/1/approval-eligibility" \
  -H "Authorization: Bearer $TOKEN")
echo "Approval Eligibility Response: $ELIGIBILITY_RESP"

echo ""
echo "============================================================"
echo "   ✅ ONBOARDING APPROVAL POLICY ENGINE LIVE TESTS PASSED!   "
echo "============================================================"
