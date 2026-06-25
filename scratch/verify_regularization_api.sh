#!/bin/bash
set -e

echo "=== Authentication / Login ==="

# 1. Login as Employee
echo "Logging in as Employee (employee@company.com)..."
EMP_LOGIN_RESP=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "employee@company.com", "password": "employee@3"}')

EMP_TOKEN=$(echo "$EMP_LOGIN_RESP" | grep -oP '"accessToken":"\K[^"]+')
if [ -z "$EMP_TOKEN" ]; then
  echo "Failed to get Employee token. Full response:"
  echo "$EMP_LOGIN_RESP"
  exit 1
fi
echo "Employee Login Successful."

# 2. Login as Admin
echo "Logging in as Admin (admin@company.com)..."
ADMIN_LOGIN_RESP=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@company.com", "password": "admin@6"}')

ADMIN_TOKEN=$(echo "$ADMIN_LOGIN_RESP" | grep -oP '"accessToken":"\K[^"]+')
if [ -z "$ADMIN_TOKEN" ]; then
  echo "Failed to get Admin token. Full response:"
  echo "$ADMIN_LOGIN_RESP"
  exit 1
fi
echo "Admin Login Successful."

echo ""
echo "=== Submit Regularization as Employee ==="
SUBMIT_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST http://localhost:8080/api/v1/attendance/regularization \
  -H "Authorization: Bearer $EMP_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"date": "2026-06-25", "proposedPunchInTime": "09:00:00", "proposedPunchOutTime": "17:00:00", "reason": "Forgot to check in"}' )

SUBMIT_STATUS=$(echo "$SUBMIT_RESP" | tail -n1 | cut -d':' -f2)
SUBMIT_BODY=$(echo "$SUBMIT_RESP" | sed '$d')

echo "Status: $SUBMIT_STATUS"
echo "Body: $SUBMIT_BODY"

if [ "$SUBMIT_STATUS" -ne 201 ]; then
  echo "FAIL: Expected 201 Created for submit regularization"
  exit 1
fi

echo ""
echo "=== Test Endpoint Scope Validation ==="

# 3. GET /api/v1/attendance/regularization as Employee (Should be 200 OK and return only employee's own requests)
echo "1. GET /attendance/regularization as Employee..."
EMP_GET_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET http://localhost:8080/api/v1/attendance/regularization \
  -H "Authorization: Bearer $EMP_TOKEN")

EMP_GET_STATUS=$(echo "$EMP_GET_RESP" | tail -n1 | cut -d':' -f2)
EMP_GET_BODY=$(echo "$EMP_GET_RESP" | sed '$d')

echo "Status: $EMP_GET_STATUS"
echo "Body: $EMP_GET_BODY"

if [ "$EMP_GET_STATUS" -ne 200 ]; then
  echo "FAIL: Expected 200 OK for employee access"
  exit 1
fi

# Ensure data list has exactly 1 item
if [[ "$EMP_GET_BODY" != *"\"data\":[{"* ]]; then
  echo "FAIL: Expected employee's regularization request in response data list"
  exit 1
fi

# 4. GET /api/v1/attendance/regularization as Admin (Should be 200 OK and return all requests)
echo ""
echo "2. GET /attendance/regularization as Admin..."
ADMIN_GET_RESP=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET http://localhost:8080/api/v1/attendance/regularization \
  -H "Authorization: Bearer $ADMIN_TOKEN")

ADMIN_GET_STATUS=$(echo "$ADMIN_GET_RESP" | tail -n1 | cut -d':' -f2)
ADMIN_GET_BODY=$(echo "$ADMIN_GET_RESP" | sed '$d')

echo "Status: $ADMIN_GET_STATUS"
echo "Body snippet: $(echo "$ADMIN_GET_BODY" | cut -c1-300)..."

if [ "$ADMIN_GET_STATUS" -ne 200 ]; then
  echo "FAIL: Expected 200 OK for admin access"
  exit 1
fi

if [[ "$ADMIN_GET_BODY" != *"\"data\":[{"* ]]; then
  echo "FAIL: Expected regularization request in admin response data list"
  exit 1
fi

# Clean up/delete the submitted test regularization request from db to keep it clean
echo ""
echo "Cleaning up database..."
PGPASSWORD=12345 psql -h 127.0.0.1 -U ems_user -d employee_db -c "DELETE FROM attendance_regularizations WHERE reason = 'Forgot to check in';"
echo "Cleanup successful."

echo ""
echo "=== SUCCESS: All API checks passed! ==="
