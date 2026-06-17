#!/bin/bash
# verify_cleanup.sh - Verify API Cleanup and REST Standardization

BASE_URL="http://localhost:8080/api/v1"
ADMIN_EMAIL="emssuperadmin@gmail.com"
ADMIN_PASS="Admin@123"
EMP_EMAIL="employee@company.com"
EMP_PASS="employee@5"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'
BLUE='\033[0;34m'

echo -e "${BLUE}=== Starting API Cleanup Verification ===${NC}"

# 1. Login as Admin
echo -e "\nLogging in as Admin..."
ADMIN_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASS\"}")
ADMIN_TOKEN=$(echo $ADMIN_LOGIN | grep -oP '"accessToken":"\K[^"]+')

# 2. Login as Employee
echo -e "Logging in as Employee..."
EMP_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMP_EMAIL\",\"password\":\"employee@5\"}")
EMP_TOKEN=$(echo $EMP_LOGIN | grep -oP '"accessToken":"\K[^"]+')

ADMIN_AUTH="Authorization: Bearer $ADMIN_TOKEN"
EMP_AUTH="Authorization: Bearer $EMP_TOKEN"

# Use auto-inject if tokens are missing (local env)
if [ -z "$ADMIN_TOKEN" ]; then ADMIN_AUTH="X-Dummy: dummy"; fi
if [ -z "$EMP_TOKEN" ]; then EMP_AUTH="X-Dummy: dummy"; fi


# --- 1. Employees (Restored GET) ---
echo -e "\n${BLUE}1. Testing Restored Employee GET Endpoints...${NC}"
curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/employees" -H "$ADMIN_AUTH" | grep -q "200" && echo -e "${GREEN}SUCCESS: GET /employees (200 OK)${NC}" || echo -e "${RED}FAILED: GET /employees${NC}"

# --- 2. Directory (New Base Path) ---
echo -e "\n${BLUE}2. Testing Directory Endpoints (Base Path /directory)...${NC}"
curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/directory" -H "$EMP_AUTH" | grep -q "200" && echo -e "${GREEN}SUCCESS: GET /directory (200 OK)${NC}" || echo -e "${RED}FAILED: GET /directory${NC}"

# --- 3. Goals (Standardized /my) ---
echo -e "\n${BLUE}3. Testing Goals (/my)...${NC}"
curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/goals/my" -H "$EMP_AUTH" | grep -q "200" && echo -e "${GREEN}SUCCESS: GET /goals/my (200 OK)${NC}" || echo -e "${RED}FAILED: GET /goals/my${NC}"

# --- 4. Attendance (/me) ---
echo -e "\n${BLUE}4. Testing Attendance (/me)...${NC}"
# Check-out first to ensure clean state
curl -s -X POST "$BASE_URL/attendance/me/check-out" -H "$EMP_AUTH" > /dev/null
echo "POST /api/v1/attendance/me/check-in"
curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/attendance/me/check-in" \
  -H "$EMP_AUTH" \
  -H "Content-Type: application/json" \
  -d '{"location":"Office"}' | grep -qE "200|201" && echo -e "${GREEN}SUCCESS: POST /attendance/me/check-in (OK)${NC}" || echo -e "${RED}FAILED: POST /attendance/me/check-in${NC}"

# --- 5. Leave (/my and PATCH) ---
echo -e "\n${BLUE}5. Testing Leave (/my and PATCH)...${NC}"
curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/leaves/my" -H "$EMP_AUTH" | grep -q "200" && echo -e "${GREEN}SUCCESS: GET /leaves/my (200 OK)${NC}" || echo -e "${RED}FAILED: GET /leaves/my${NC}"

curl -s -o /dev/null -w "%{http_code}" -X PATCH "$BASE_URL/leave-types/1/deactivate" -H "$ADMIN_AUTH" | grep -q "200" && echo -e "${GREEN}SUCCESS: PATCH /leave-types/1/deactivate (200 OK)${NC}" || echo -e "${RED}FAILED: PATCH /leave-types/1/deactivate${NC}"

# --- 6. Notifications (/my) ---
echo -e "\n${BLUE}6. Testing Notifications (/my)...${NC}"
curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/notifications/my" -H "$EMP_AUTH" | grep -q "200" && echo -e "${GREEN}SUCCESS: GET /notifications/my (200 OK)${NC}" || echo -e "${RED}FAILED: GET /notifications/my${NC}"

# --- 7. Onboarding (/my) ---
echo -e "\n${BLUE}7. Testing Onboarding (/my)...${NC}"
curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/onboarding/my" -H "$EMP_AUTH" | grep -q "200" && echo -e "${GREEN}SUCCESS: GET /onboarding/my (200 OK)${NC}" || echo -e "${RED}FAILED: GET /onboarding/my${NC}"

# --- 8. Payroll (POST) ---
echo -e "\n${BLUE}8. Testing Payroll Run (POST process)...${NC}"
# Transition to APPROVED first
PAYROLL_ID=$(curl -s -X GET "$BASE_URL/payroll-runs" -H "$ADMIN_AUTH" | grep -oP '"id":\K[0-9]+' | head -n 1)
if [ -n "$PAYROLL_ID" ]; then
  curl -s -X PUT "$BASE_URL/payroll-runs/$PAYROLL_ID/review" -H "$ADMIN_AUTH" > /dev/null
  curl -s -X PATCH "$BASE_URL/payroll-runs/$PAYROLL_ID/approve" -H "$ADMIN_AUTH" > /dev/null
  curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/payroll-runs/$PAYROLL_ID/process" -H "$ADMIN_AUTH" | grep -q "200" && echo -e "${GREEN}SUCCESS: POST /payroll-runs/$PAYROLL_ID/process (200 OK)${NC}" || echo -e "${RED}FAILED: POST /payroll-runs/$PAYROLL_ID/process${NC}"
else
  echo -e "${RED}SKIP: No payroll record found.${NC}"
fi

# --- 9. Training (Complete) ---
echo -e "\n${BLUE}9. Testing Training Completion...${NC}"
SESSION_ID=$(curl -s -X GET "$BASE_URL/trainings/sessions" -H "$EMP_AUTH" | grep -oP '"sessionId":\K[0-9]+' | head -n 1)
if [ -n "$SESSION_ID" ]; then
  ENROLL_RESP=$(curl -s -X POST "$BASE_URL/trainings/enrollments" \
    -H "$EMP_AUTH" \
    -H "Content-Type: application/json" \
    -d "{\"sessionId\":$SESSION_ID}")
  ENROLL_ID=$(echo $ENROLL_RESP | grep -oP '"enrollmentId":\K[0-9]+')
  if [ -n "$ENROLL_ID" ]; then
    curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/trainings/$ENROLL_ID/complete" -H "$EMP_AUTH" | grep -q "200" && echo -e "${GREEN}SUCCESS: POST /trainings/$ENROLL_ID/complete (200 OK)${NC}" || echo -e "${RED}FAILED: POST /trainings/$ENROLL_ID/complete${NC}"
  else
    echo -e "${RED}SKIP: Could not enroll.${NC}"
  fi
else
  echo -e "${RED}SKIP: No training session found.${NC}"
fi

echo -e "\n${BLUE}=== Cleanup Verification Complete ===${NC}"
