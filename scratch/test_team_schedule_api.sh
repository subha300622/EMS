#!/bin/bash

# Port of running application
PORT=8080
BASE_URL="http://localhost:$PORT/api/v1"

echo "=== 1. Login as employee ==="
EMP_LOGIN_RES=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "employee@company.com", "password": "employee@3"}')

EMP_TOKEN=$(echo "$EMP_LOGIN_RES" | jq -r '.data.tokens.accessToken')

if [ -z "$EMP_TOKEN" ] || [ "$EMP_TOKEN" == "null" ]; then
  echo "Login failed for employee@company.com."
  exit 1
fi
echo "Employee login successful."
echo ""

echo "=== 2. Seed Employee Schedule Data (Triggered by fetching calendar) ==="
curl -s -X GET "$BASE_URL/my-schedule/calendar?startDate=2026-06-15&endDate=2026-06-21" \
  -H "Authorization: Bearer $EMP_TOKEN" > /dev/null
echo "Schedule data seeded."
echo ""

echo "=== 3. Submit Shift Change Request as Employee ==="
# Request to change shift on 2026-06-20 from GENERAL_SHIFT (101) to EVENING_SHIFT (102)
CHANGE_RES=$(curl -s -X POST "$BASE_URL/my-schedule/change-requests" \
  -H "Authorization: Bearer $EMP_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"currentShiftId": 101, "requestedShiftId": 102, "requestedDate": "2026-06-20", "requestType": "SHIFT_CHANGE", "reason": "Personal Appointment"}')
echo "$CHANGE_RES" | jq '.'
echo ""

echo "=== 4. Login as manager ==="
MGR_LOGIN_RES=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "manager@company.com", "password": "manager@4"}')

MGR_TOKEN=$(echo "$MGR_LOGIN_RES" | jq -r '.data.tokens.accessToken')

if [ -z "$MGR_TOKEN" ] || [ "$MGR_TOKEN" == "null" ]; then
  echo "Login failed for manager@company.com."
  exit 1
fi
echo "Manager login successful."
echo ""

echo "=== 5. GET /api/v1/team-schedule (Checking Swap Requests) ==="
GET_RES=$(curl -s -X GET "$BASE_URL/team-schedule?startDate=2026-06-15&endDate=2026-06-21" \
  -H "Authorization: Bearer $MGR_TOKEN")

echo "$GET_RES" | jq '.data.swaps'
echo ""

SWAP_REQ_ID=$(echo "$GET_RES" | jq -r '.data.swaps[0].requestId')

if [ -z "$SWAP_REQ_ID" ] || [ "$SWAP_REQ_ID" == "null" ]; then
  echo "No swap requests found in GET response."
else
  echo "=== 6. Approve Swap Request $SWAP_REQ_ID ==="
  curl -s -X POST "$BASE_URL/team-schedule/swap-requests/$SWAP_REQ_ID/approve" \
    -H "Authorization: Bearer $MGR_TOKEN" | jq '.'
  echo ""

  echo "=== 7. GET /api/v1/team-schedule (Verify Shift Swapped to EVENING on 2026-06-20) ==="
  curl -s -X GET "$BASE_URL/team-schedule?startDate=2026-06-15&endDate=2026-06-21" \
    -H "Authorization: Bearer $MGR_TOKEN" | jq '.data.grid[] | select(.name == "Employee User") | .shifts[] | select(.date == "2026-06-20")'
  echo ""
fi
