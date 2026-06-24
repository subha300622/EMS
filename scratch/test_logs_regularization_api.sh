#!/bin/bash

PORT=8080
BASE_URL="http://localhost:$PORT/api/v1"

echo "========================================="
echo "STEP 1: Log in as Employee"
echo "========================================="
EMPLOYEE_TOKEN=""
for id in 1 2 3 4 5 6 7 8 9; do
  LOGIN_RES=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\": \"employee@company.com\", \"password\": \"employee@$id\"}")
  TOKEN=$(echo "$LOGIN_RES" | jq -r '.data.tokens.accessToken')
  if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    EMPLOYEE_TOKEN=$TOKEN
    echo "Employee logged in successfully with password: employee@$id"
    break
  fi
done

if [ -z "$EMPLOYEE_TOKEN" ]; then
  echo "Employee login failed."
  exit 1
fi

echo "========================================="
echo "STEP 2: Log in as Admin"
echo "========================================="
ADMIN_TOKEN=""
for id in 1 2 3 4 5 6 7 8 9; do
  LOGIN_RES=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\": \"admin@company.com\", \"password\": \"admin@$id\"}")
  TOKEN=$(echo "$LOGIN_RES" | jq -r '.data.tokens.accessToken')
  if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    ADMIN_TOKEN=$TOKEN
    echo "Admin logged in successfully with password: admin@$id"
    break
  fi
done

if [ -z "$ADMIN_TOKEN" ]; then
  echo "Admin login failed."
  exit 1
fi

# Resolve employee ID by listing employees using Admin token
EMPLOYEES_LIST=$(curl -s -X GET "$BASE_URL/employees" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
EMPLOYEE_ID=$(echo "$EMPLOYEES_LIST" | jq -r '.data.content[] | select(.name == "Employee User") | .id')
echo "Resolved Employee ID: $EMPLOYEE_ID"

echo "========================================="
echo "STEP 3: Check-in Employee (First Swipe)"
echo "========================================="
CHECKIN_RES=$(curl -s -X POST "$BASE_URL/attendance/me/check-in" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"notes": "Arrived at office"}')
echo "Check-in Response:"
echo "$CHECKIN_RES" | jq .
echo ""

echo "========================================="
echo "STEP 4: Immediate Double Check-in (Idempotency Safeguard)"
echo "========================================="
DOUBLE_CHECKIN_RES=$(curl -s -X POST "$BASE_URL/attendance/me/check-in" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"notes": "Accidental double check-in"}')
echo "Double Check-in Response (Expected Error):"
echo "$DOUBLE_CHECKIN_RES" | jq .
echo ""

echo "========================================="
echo "STEP 5: Wait 5 seconds and Try Check-in again"
echo "========================================="
echo "Waiting 5 seconds..."
sleep 5
LATE_CHECKIN_RES=$(curl -s -X POST "$BASE_URL/attendance/me/check-in" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"notes": "Check-in after 5 seconds"}')
echo "Late Check-in Response (Expected Already checked in today):"
echo "$LATE_CHECKIN_RES" | jq .
echo ""

echo "========================================="
echo "STEP 6: Retrieve Paginated Daily Swipe Logs for Employee"
echo "========================================="
LOGS_RES=$(curl -s -X GET "$BASE_URL/attendance/employee/$EMPLOYEE_ID/logs?page=0&size=5" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN")
echo "Employee's Daily Logs Response:"
echo "$LOGS_RES" | jq .
echo ""

echo "========================================="
echo "STEP 7: Standard Employee Tries to Access Other's Swipe Logs (Forbidden check)"
echo "========================================="
OTHER_LOGS_RES=$(curl -s -X GET "$BASE_URL/attendance/employee/1/logs?page=0&size=5" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN")
echo "Access other employee's logs response (Expected Forbidden):"
echo "$OTHER_LOGS_RES" | jq .
echo ""

echo "========================================="
echo "STEP 8: Admin Retrieves Employee's Swipe Logs"
echo "========================================="
ADMIN_LOGS_RES=$(curl -s -X GET "$BASE_URL/attendance/employee/$EMPLOYEE_ID/logs?page=0&size=5" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo "Admin Daily Logs Response:"
echo "$ADMIN_LOGS_RES" | jq .
echo ""

echo "========================================="
echo "STEP 9: Employee Submits Regularization Request"
echo "========================================="
REG_REQ_BODY="{
  \"employeeId\": $EMPLOYEE_ID,
  \"date\": \"$(date +%Y-%m-%d)\",
  \"proposedPunchInTime\": \"09:00:00\",
  \"proposedPunchOutTime\": \"17:00:00\",
  \"reason\": \"Forgot to punch out\"
}"
REG_SUBMIT_RES=$(curl -s -X POST "$BASE_URL/attendance/regularization" \
  -H "Authorization: Bearer $EMPLOYEE_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$REG_REQ_BODY")
echo "Regularization Request Body:"
echo "$REG_REQ_BODY" | jq .
echo "Regularization Request Submission Response:"
echo "$REG_SUBMIT_RES" | jq .
REG_ID=$(echo "$REG_SUBMIT_RES" | jq -r '.data.id')
echo "Regularization ID is: $REG_ID"
echo ""

echo "========================================="
echo "STEP 10: Admin Approves Regularization Request with Overrides and Notes"
echo "========================================="
APPROVE_BODY='{
  "correctedPunchInTime": "08:55:00",
  "correctedPunchOutTime": "17:05:00",
  "managerNotes": "Approved with exact time corrections from backup log"
}'
echo "Approve Request Body:"
echo "$APPROVE_BODY" | jq .
APPROVE_RES=$(curl -s -X PATCH "$BASE_URL/attendance/regularization/$REG_ID/approve" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$APPROVE_BODY")
echo "Approval Response:"
echo "$APPROVE_RES" | jq .
echo ""

echo "========================================="
echo "STEP 11: Get Final Employee Attendance Details to Verify Audit Preservation"
echo "========================================="
ATTENDANCE_RES=$(curl -s -X GET "$BASE_URL/attendance/employee/$EMPLOYEE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo "Final Attendance History (Verifying punchInTime, originalPunchInTime, manager notes):"
echo "$ATTENDANCE_RES" | jq .
echo ""
