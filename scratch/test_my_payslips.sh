#!/bin/bash
# test_my_payslips.sh - Manual verification script for My Payslips Self-Service Module

set -e

BASE_URL="http://localhost:8080/api/v1"

echo "=== 1. Logging in to get JWT access token ==="
RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "emssuperadmin@gmail.com", "password": "Admin@123"}')

TOKEN=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['tokens']['accessToken'])")

if [ -z "$TOKEN" ]; then
  echo "Failed to get access token!"
  exit 1
fi

AUTH_HEADER="Authorization: Bearer ${TOKEN}"

echo "=== 2. Creating a test Payroll run (for June 2026) ==="
RUN_RESP=$(curl -s -X POST "${BASE_URL}/payroll-runs" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{"month": 6, "year": 2026}')

# Extract first payroll id
PAYROLL_ID=$(echo "$RUN_RESP" | python3 -c "
import sys, json
data = json.load(sys.stdin)['data']
if isinstance(data, list) and len(data) > 0:
    print(data[0]['id'])
else:
    # If already generated, find id from list
    print('ALREADY_GENERATED')
")

if [ "$PAYROLL_ID" = "ALREADY_GENERATED" ] || [ -z "$PAYROLL_ID" ]; then
  echo "Payroll already generated or list empty, fetching existing payroll list..."
  LIST_RESP=$(curl -s -X GET "${BASE_URL}/payroll-runs" -H "${AUTH_HEADER}")
  PAYROLL_ID=$(echo "$LIST_RESP" | python3 -c "
import sys, json
records = json.load(sys.stdin)['data']
june_records = [r for r in records if r['month'] == 6 and r['year'] == 2026]
if june_records:
    print(june_records[0]['id'])
else:
    print(records[0]['id'])
")
fi

echo "Selected Payroll ID: ${PAYROLL_ID}"

echo "=== 3. Moving payroll through review/approve/process/paid lifecycle ==="
# Review
curl -s -X PUT "${BASE_URL}/payroll-runs/${PAYROLL_ID}/review" -H "${AUTH_HEADER}" > /dev/null || true
# Approve
curl -s -X PATCH "${BASE_URL}/payroll-runs/${PAYROLL_ID}/approve" -H "${AUTH_HEADER}" > /dev/null || true
# Process
curl -s -X PUT "${BASE_URL}/payroll-runs/${PAYROLL_ID}/process" -H "${AUTH_HEADER}" > /dev/null || true
# Pay
curl -s -X PATCH "${BASE_URL}/payroll-runs/${PAYROLL_ID}/payment" -H "${AUTH_HEADER}" > /dev/null || true

echo "=== 4. Generating payslips ==="
curl -s -X POST "${BASE_URL}/payslips/generate" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{"month": 6, "year": 2026}' > /dev/null || true

# Find a payslip ID that belongs to the current user from their history
HIST_RESP=$(curl -s -X GET "${BASE_URL}/my-payslips/history" -H "${AUTH_HEADER}")
PAYSLIP_ID=$(echo "$HIST_RESP" | python3 -c "
import sys, json
data = json.load(sys.stdin).get('data', {})
items = data.get('data', [])
if items:
    print(items[0]['payslipId'])
else:
    print('1')
")

echo "Selected Payslip ID for Self-Service: ${PAYSLIP_ID}"

echo "=== Endpoint 1: GET /api/v1/my-payslips/dashboard ==="
curl -s -X GET "${BASE_URL}/my-payslips/dashboard" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "=== Endpoint 2: GET /api/v1/my-payslips/history ==="
curl -s -X GET "${BASE_URL}/my-payslips/history?page=0&size=10" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "=== Endpoint 3: GET /api/v1/my-payslips/{id} ==="
curl -s -X GET "${BASE_URL}/my-payslips/${PAYSLIP_ID}" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "=== Endpoint 4: GET /api/v1/my-payslips/{id}/preview ==="
curl -s -X GET "${BASE_URL}/my-payslips/${PAYSLIP_ID}/preview" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "=== Endpoint 5: GET /api/v1/my-payslips/{id}/download ==="
curl -s -I -X GET "${BASE_URL}/my-payslips/${PAYSLIP_ID}/download" -H "${AUTH_HEADER}"

echo "=== Endpoint 6: GET /api/v1/my-payslips/annual-statement ==="
curl -s -X GET "${BASE_URL}/my-payslips/annual-statement?financialYear=FY+2025-26" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "=== Endpoint 7: GET /api/v1/my-payslips/annual-statement/download ==="
curl -s -I -X GET "${BASE_URL}/my-payslips/annual-statement/download?financialYear=FY+2025-26" -H "${AUTH_HEADER}"

echo "=== Endpoint 8: GET /api/v1/my-payslips/salary-revisions ==="
curl -s -X GET "${BASE_URL}/my-payslips/salary-revisions" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "=== Endpoint 9: GET /api/v1/my-payslips/tax-summary ==="
curl -s -X GET "${BASE_URL}/my-payslips/tax-summary" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "=== Endpoint 10: POST /api/v1/my-payslips/{id}/email ==="
curl -s -X POST "${BASE_URL}/my-payslips/${PAYSLIP_ID}/email" \
  -H "${AUTH_HEADER}" \
  -H "Content-Type: application/json" \
  -d '{"email": "subhashinib3006@gmail.com"}' | python3 -m json.tool

echo "=== Endpoint 11: GET /api/v1/my-payslips/timeline ==="
curl -s -X GET "${BASE_URL}/my-payslips/timeline" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "Manual verification complete!"
