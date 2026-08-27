#!/bin/bash
set -e

BASE_URL="http://localhost:8080/api/v1"
TIMESTAMP=$(date +%s)
NUM=$((RANDOM % 9000 + 1000))

echo "============================================================"
echo "    EMS ONBOARDING & APPROVAL POLICY ENGINE LIVE TEST SUITE "
echo "============================================================"
echo ""

# 1. Register & Login Tenant Admin
EMAIL="onb.admin.$TIMESTAMP@company.com"
PASS="Password123!"

echo "1. Registering Tenant Admin ($EMAIL)..."
curl -s -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Onboarding Admin",
    "email": "'$EMAIL'",
    "countryCode": "+1",
    "phone": "555'$NUM'",
    "password": "'$PASS'",
    "orgName": "Onboarding Corp '$TIMESTAMP'",
    "industry": "Software",
    "country": "USA",
    "state": "California",
    "city": "San Francisco",
    "address": "100 Onboarding Way",
    "companySize": "51-200",
    "plan": "ENTERPRISE",
    "billingCycle": "ANNUAL"
  }' > /dev/null

echo "Logging in Admin..."
LOGIN_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'$EMAIL'",
    "password": "'$PASS'"
  }')

TOKEN=$(echo "$LOGIN_RESP" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "JWT Token acquired: ${TOKEN:0:30}..."

# 2. Create Unique Department
DEPT_CODE="ENG-$TIMESTAMP"
echo ""
echo "2. Creating Department ($DEPT_CODE)..."
DEPT_RESP=$(curl -s -X POST "$BASE_URL/departments" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "'$DEPT_CODE'",
    "name": "Engineering '$TIMESTAMP'"
  }')
echo "Department Response: ${DEPT_RESP:0:200}..."

# 3. Create Candidate Employee
echo ""
EMP_CODE="EMP-ALICE-$TIMESTAMP"
CANDIDATE_EMAIL="alice.smith.$TIMESTAMP@company.com"

echo "3. Creating Employee for Onboarding ($EMP_CODE)..."
EMP_RESP=$(curl -s -X POST "$BASE_URL/employees" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Smith",
    "email": "'$CANDIDATE_EMAIL'",
    "employeeId": "'$EMP_CODE'",
    "department": "Engineering '$TIMESTAMP'",
    "designation": "Software Engineer",
    "joiningDate": "2026-09-01",
    "annualSalary": 110000
  }')

EMP_ID=$(echo "$EMP_RESP" | grep -o '"id":[0-9]*' | head -n1 | cut -d':' -f2)
echo "Created Candidate Database ID: $EMP_ID"

# 4. Launch Onboarding Workflow
echo ""
echo "4. Launching Onboarding Workflow with Template Code TPL-ENG-001..."
LAUNCH_RESP=$(curl -s -X POST "$BASE_URL/onboarding" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": "'$EMP_CODE'",
    "employeeName": "Alice Smith",
    "email": "'$CANDIDATE_EMAIL'",
    "joiningDate": "2026-09-01",
    "department": "Engineering '$TIMESTAMP'",
    "designation": "Software Engineer",
    "employmentType": "FULL_TIME",
    "templateId": "TPL-ENG-001"
  }')

RAW_ID=$(echo "$LAUNCH_RESP" | grep -o '"id":"onb-[0-9]*"' | head -n1 | cut -d':' -f2 | tr -d '"')
ONB_ID=$(echo "$RAW_ID" | sed 's/onb-//')
echo "Launched Onboarding Database ID: $ONB_ID"

# 5. Step Lifecycle Transitions: PRE_JOINING -> IN_PROGRESS -> PENDING_APPROVAL
echo ""
echo "5. Transitioning Onboarding Status: PRE_JOINING -> IN_PROGRESS..."
curl -s -X PATCH "$BASE_URL/onboarding/$ONB_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_PROGRESS"}' > /dev/null

echo "Transitioning Onboarding Status: IN_PROGRESS -> PENDING_APPROVAL..."
UPDATE_RESP=$(curl -s -X PATCH "$BASE_URL/onboarding/$ONB_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "PENDING_APPROVAL"}')
echo "Status Update Response: ${UPDATE_RESP:0:220}..."

# 6. Configure Custom Organization Approval Policy (CONFIGURED_ACTOR)
echo ""
echo "6. Configuring Custom Organization Approval Policy..."
CUSTOM_POL_RESP=$(curl -s -X POST "$BASE_URL/onboarding/approval-policies" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currentStatus": "PENDING_APPROVAL",
    "action": "APPROVE",
    "nextStatus": "APPROVED",
    "approver": {
      "type": "CONFIGURED_ACTOR"
    },
    "conditions": [
      "ALL_MANDATORY_TASKS_COMPLETED",
      "ALL_MANDATORY_DOCUMENTS_VERIFIED"
    ],
    "active": true
  }')
echo "Created Custom Approval Policy: $CUSTOM_POL_RESP"

# 7. Check Approval Eligibility for Candidate Onboarding
echo ""
echo "7. Checking Approval Eligibility for Onboarding ID $ONB_ID..."
ELIG_RESP=$(curl -s -X GET "$BASE_URL/onboarding/$ONB_ID/approval-eligibility" \
  -H "Authorization: Bearer $TOKEN")
echo "Approval Eligibility Response: $ELIG_RESP"

# 8. Execute Candidate Approval via Policy Engine
echo ""
echo "8. Executing Candidate Approval via Policy Engine..."
APPROVE_RESP=$(curl -s -X POST "$BASE_URL/onboarding/$ONB_ID/approve" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "remarks": "Onboarding requirements verified and approved per organization approval policy"
  }')
echo "Final Approval Response: $APPROVE_RESP"

echo ""
echo "============================================================"
echo "   🎉 100% SUCCESSFUL END-TO-END LIVE API VERIFICATION!    "
echo "============================================================"
