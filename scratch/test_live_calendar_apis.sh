#!/bin/bash
PORT=8080
BASE_URL="http://localhost:$PORT/api/v1"

# 1. Login as employee
echo "=== Step 1: Log in as Employee ==="
EMP_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "employee@company.com", "password": "employee@3"}')
EMP_TOKEN=$(echo "$EMP_LOGIN" | jq -r '.data.tokens.accessToken')
echo "Employee Login Message: $(echo "$EMP_LOGIN" | jq -r '.message')"

# 2. Check-in for employee
echo ""
echo "=== Step 2: Employee Check-in (for today: 2026-06-23) ==="
CHECKIN_RES=$(curl -s -X POST "$BASE_URL/attendance/me/check-in" \
  -H "Authorization: Bearer $EMP_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"notes": "Arrived at Chennai office"}')
echo "Check-in Status: $(echo "$CHECKIN_RES" | jq -r '.message')"
echo "Record data:"
echo "$CHECKIN_RES" | jq .data

# 3. Login as manager
echo ""
echo "=== Step 3: Log in as Manager ==="
MGR_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "manager@company.com", "password": "manager@4"}')
MGR_TOKEN=$(echo "$MGR_LOGIN" | jq -r '.data.tokens.accessToken')
echo "Manager Login Message: $(echo "$MGR_LOGIN" | jq -r '.message')"

# 4. Request calendar monthly
echo ""
echo "=== Step 4: GET /api/v1/team-attendance/calendar/monthly?month=2026-06&view=full ==="
curl -s -X GET "$BASE_URL/team-attendance/calendar/monthly?month=2026-06&view=full" \
  -H "Authorization: Bearer $MGR_TOKEN" | jq .

# 5. Request calendar heatmap
echo ""
echo "=== Step 5: GET /api/v1/team-attendance/calendar/heatmap?month=2026-06 ==="
curl -s -X GET "$BASE_URL/team-attendance/calendar/heatmap?month=2026-06" \
  -H "Authorization: Bearer $MGR_TOKEN" | jq .

# 6. Request employee calendar drilldown
echo ""
echo "=== Step 6: GET /api/v1/team-attendance/calendar/employee/3?month=2026-06 ==="
curl -s -X GET "$BASE_URL/team-attendance/calendar/employee/3?month=2026-06" \
  -H "Authorization: Bearer $EMP_TOKEN" | jq .

# 7. Request calendar summary
echo ""
echo "=== Step 7: GET /api/v1/team-attendance/calendar/summary?month=2026-06 ==="
curl -s -X GET "$BASE_URL/team-attendance/calendar/summary?month=2026-06" \
  -H "Authorization: Bearer $MGR_TOKEN" | jq .
