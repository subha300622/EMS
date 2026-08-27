#!/bin/bash
set -e

BASE_URL="http://localhost:8080/api/v1"
TIMESTAMP=$(date +%s)
NUM1=$((RANDOM % 9000 + 1000))
NUM2=$((RANDOM % 9000 + 1000))

echo "============================================================"
echo "      EMS BACKEND LIVE API TENANT ISOLATION TEST SUITE      "
echo "============================================================"
echo ""

# 1. Register Org A (Acme Corp)
echo "1. Registering Organization A (Acme Corp)..."
EMAIL_A="admin.acme.$TIMESTAMP@acme.com"
PASS_A="Password123!"

SIGNUP_A_RESP=$(curl -s -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Admin Acme",
    "email": "'$EMAIL_A'",
    "countryCode": "+1",
    "phone": "555'$NUM1'",
    "password": "'$PASS_A'",
    "orgName": "Acme Corp '$TIMESTAMP'",
    "industry": "Technology",
    "country": "USA",
    "state": "California",
    "city": "San Francisco",
    "address": "100 Innovation Way, Suite 400",
    "companySize": "51-200",
    "plan": "ENTERPRISE",
    "billingCycle": "ANNUAL"
  }')

LOGIN_A_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'$EMAIL_A'",
    "password": "'$PASS_A'"
  }')

TOKEN_A=$(echo "$LOGIN_A_RESP" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

echo "Org A Token acquired: ${TOKEN_A:0:30}..."

# 2. Register Org B (Beta Inc)
echo ""
echo "2. Registering Organization B (Beta Inc)..."
EMAIL_B="admin.beta.$TIMESTAMP@beta.com"
PASS_B="Password123!"

SIGNUP_B_RESP=$(curl -s -X POST "$BASE_URL/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Admin Beta",
    "email": "'$EMAIL_B'",
    "countryCode": "+1",
    "phone": "555'$NUM2'",
    "password": "'$PASS_B'",
    "orgName": "Beta Inc '$TIMESTAMP'",
    "industry": "Finance",
    "country": "USA",
    "state": "New York",
    "city": "New York",
    "address": "200 Wall Street, Floor 12",
    "companySize": "11-50",
    "plan": "PRO",
    "billingCycle": "MONTHLY"
  }')

LOGIN_B_RESP=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'$EMAIL_B'",
    "password": "'$PASS_B'"
  }')

TOKEN_B=$(echo "$LOGIN_B_RESP" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

echo "Org B Token acquired: ${TOKEN_B:0:30}..."

# 3. Create Employee in Org A with Body organizationId Spoof Attempt (organizationId: 99999)
echo ""
EMP_CODE="EMP-ACME-$TIMESTAMP"
echo "3. Creating Employee in Org A ($EMP_CODE) attempting organizationId: 99999 spoof..."
CREATE_EMP_RESP=$(curl -s -X POST "$BASE_URL/employees" \
  -H "Authorization: Bearer $TOKEN_A" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe.'$TIMESTAMP'@acme.com",
    "employeeId": "'$EMP_CODE'",
    "department": "Engineering",
    "designation": "Software Engineer",
    "joiningDate": "2026-01-15",
    "annualSalary": 120000,
    "organizationId": 99999
  }')

RETURNED_ORG_ID=$(echo "$CREATE_EMP_RESP" | grep -o '"organizationId":[0-9]*' | head -n1 | cut -d':' -f2)

echo "Returned organizationId in Response: $RETURNED_ORG_ID"

if [ "$RETURNED_ORG_ID" == "99999" ]; then
  echo "❌ CRITICAL SECURITY FAILURE: Body organizationId spoofed tenant ID!"
  exit 1
elif [ -n "$RETURNED_ORG_ID" ]; then
  echo "✅ PASS: Body organizationId spoof successfully overridden by JWT tenant context (organizationId: $RETURNED_ORG_ID)."
fi

# 4. Get All Employees for Org A
echo ""
echo "4. Fetching All Employees for Org A..."
GET_ALL_RESP=$(curl -s -X GET "$BASE_URL/employees" \
  -H "Authorization: Bearer $TOKEN_A")
echo "Get All Employees Response Snippet: ${GET_ALL_RESP:0:250}..."

# 5. Cross-Tenant Access Protection Verification (Org B trying to access Org A's Employee)
echo ""
echo "5. Testing Cross-Tenant Hiding (Org B accessing Org A Employee: $EMP_CODE)..."

echo "a. GET /api/v1/employees/$EMP_CODE with Org B token:"
CROSS_GET_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/employees/$EMP_CODE" \
  -H "Authorization: Bearer $TOKEN_B")
echo "HTTP Status Code: $CROSS_GET_CODE"

if [ "$CROSS_GET_CODE" == "404" ]; then
  echo "✅ PASS: Cross-Tenant GET returned 404 Not Found (Information Leakage Blocked)."
else
  echo "❌ FAIL: Cross-Tenant GET returned $CROSS_GET_CODE instead of 404."
  exit 1
fi

echo ""
echo "b. PUT /api/v1/employees/$EMP_CODE with Org B token:"
CROSS_PUT_RESP=$(curl -s -X PUT "$BASE_URL/employees/$EMP_CODE" \
  -H "Authorization: Bearer $TOKEN_B" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Hacked",
    "lastName": "User",
    "email": "hacked@beta.com",
    "employeeId": "'$EMP_CODE'",
    "department": "Security",
    "designation": "Hacker",
    "joiningDate": "2026-01-01"
  }')
echo "Cross-Tenant UPDATE Response: $CROSS_PUT_RESP"

if echo "$CROSS_PUT_RESP" | grep -q "not found"; then
  echo "✅ PASS: Cross-Tenant UPDATE rejected with 'not found' error (Unauthorized Mutation Blocked)."
else
  echo "❌ FAIL: Cross-Tenant UPDATE allowed unexpected mutation."
  exit 1
fi

echo ""
echo "============================================================"
echo "   ✅ ALL API LIVE TENANT ISOLATION TESTS PASSED 100%!       "
echo "============================================================"
