#!/bin/bash
set -e

# Configuration
DB_URL="jdbc:postgresql://127.0.0.1:5432/employee_db"
PGPASSWORD=12345
export PGPASSWORD

echo "==========================================================="
# 1. Login
echo "1. POST /api/v1/auth/login"
LOGIN_PAYLOAD='{"email": "super_admin@company.com", "password": "super_admin@1"}'
echo "Request Body:"
echo "$LOGIN_PAYLOAD" | jq .

LOGIN_RESP=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "$LOGIN_PAYLOAD")

echo "Response Body:"
echo "$LOGIN_RESP" | jq .

ADMIN_TOKEN=$(echo "$LOGIN_RESP" | jq -r '.data.tokens.accessToken')
if [ "$ADMIN_TOKEN" == "null" ] || [ -z "$ADMIN_TOKEN" ]; then
  echo "ERROR: Login failed."
  exit 1
fi
echo "==========================================================="

# 2. Create Organization
echo "2. POST /api/v1/platform-admin/organizations"
ORG_PAYLOAD='{
  "name": "API Payload Testing Corp",
  "email": "api.test.admin@company.com",
  "phone": "9876543210",
  "website": "https://apitestcorp.com",
  "subscriptionPlan": "ENTERPRISE",
  "address": {
    "street": "123 Main St",
    "city": "Bengaluru",
    "state": "Karnataka",
    "country": "India",
    "zipCode": "560001"
  }
}'
echo "Request Body:"
echo "$ORG_PAYLOAD" | jq .

ORG_RESP=$(curl -s -X POST http://localhost:8080/api/v1/platform-admin/organizations \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$ORG_PAYLOAD")

echo "Response Body:"
echo "$ORG_RESP" | jq .

ORG_ID=$(echo "$ORG_RESP" | jq -r '.data.id')
echo "Created Organization ID: $ORG_ID"

# Clear default subscription created automatically by organization creation so we can create a clean one
psql -h 127.0.0.1 -U ems_user -d employee_db -c "DELETE FROM subscriptions WHERE organization_id = $ORG_ID;" > /dev/null
echo "==========================================================="

# 3. Create Subscription
echo "3. POST /api/v1/platform-admin/subscriptions"
SUB_PAYLOAD="{
  \"organizationId\": $ORG_ID,
  \"plan\": {
    \"code\": \"ENTERPRISE\",
    \"name\": \"Enterprise Plan\"
  },
  \"billing\": {
    \"cycle\": \"YEARLY\",
    \"amount\": 50000.00,
    \"currency\": \"INR\",
    \"taxAmount\": 9000.00,
    \"discountAmount\": 2000.00,
    \"finalAmount\": 57000.00
  },
  \"duration\": {
    \"startDate\": \"2026-07-01\",
    \"endDate\": \"2027-07-01\",
    \"autoRenew\": true,
    \"remainingDays\": 365
  },
  \"limits\": {
    \"maxEmployees\": 1000,
    \"maxAdmins\": 25,
    \"maxDepartments\": 100,
    \"maxStorageGB\": 500,
    \"maxApiRequestsPerMonth\": 1000000
  },
  \"features\": {
    \"employeeManagement\": true,
    \"attendance\": true,
    \"leaveManagement\": true,
    \"payroll\": true,
    \"recruitment\": true,
    \"performanceManagement\": true,
    \"assetManagement\": true,
    \"training\": true,
    \"helpDesk\": true,
    \"documentManagement\": true,
    \"reports\": true,
    \"apiAccess\": true,
    \"singleSignOn\": true
  },
  \"payment\": {
    \"method\": \"BANK_TRANSFER\",
    \"referenceNumber\": \"EMS-PAYLOAD-001\",
    \"paymentStatus\": \"PENDING\"
  },
  \"notes\": \"API payloads verification run\"
}"
echo "Request Body:"
echo "$SUB_PAYLOAD" | jq .

SUB_RESP=$(curl -s -X POST http://localhost:8080/api/v1/platform-admin/subscriptions \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$SUB_PAYLOAD")

echo "Response Body:"
echo "$SUB_RESP" | jq .

SUB_ID=$(echo "$SUB_RESP" | jq -r '.data.subscriptionId')
echo "Created Subscription ID: $SUB_ID"

# Query the generated Invoice ID
INVOICE_ID=$(psql -h 127.0.0.1 -U ems_user -d employee_db -t -A -c "SELECT id FROM subscription_invoices WHERE subscription_id = $SUB_ID;")
echo "Generated Invoice ID: $INVOICE_ID"
echo "==========================================================="

# 4. Create Payment Order
echo "4. POST /api/v1/platform-admin/payments/orders"
ORDER_PAYLOAD="{
  \"invoiceId\": $INVOICE_ID,
  \"gateway\": \"RAZORPAY\"
}"
echo "Request Body:"
echo "$ORDER_PAYLOAD" | jq .

ORDER_RESP=$(curl -s -X POST http://localhost:8080/api/v1/platform-admin/payments/orders \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$ORDER_PAYLOAD")

echo "Response Body:"
echo "$ORDER_RESP" | jq .

GATEWAY_ORDER_ID=$(echo "$ORDER_RESP" | jq -r '.data.payment.gatewayOrderId')
echo "Generated Gateway Order ID: $GATEWAY_ORDER_ID"
echo "==========================================================="

# 5. Gateway Webhook Trigger
echo "5. POST /api/v1/platform-admin/payments/webhook"
WEBHOOK_PAYLOAD="{
  \"event\": \"payment.captured\",
  \"payload\": {
    \"payment\": {
      \"entity\": {
        \"id\": \"pay_TEST$(date +%s)\",
        \"order_id\": \"$GATEWAY_ORDER_ID\",
        \"method\": \"upi\",
        \"amount\": 5700000,
        \"currency\": \"INR\"
      }
    }
  }
}"
echo "Request Body:"
echo "$WEBHOOK_PAYLOAD" | jq .

echo "Dispatching Webhook call..."
curl -i -s -X POST http://localhost:8080/api/v1/platform-admin/payments/webhook \
  -H "Content-Type: application/json" \
  -H "X-Razorpay-Signature: sig_mock_signature_for_api_test" \
  -d "$WEBHOOK_PAYLOAD"

echo ""
echo "==========================================================="
# 6. Verify final database state
echo "6. Final Invoice & Subscription Database State"
psql -h 127.0.0.1 -U ems_user -d employee_db -c "SELECT id, subscription_id, invoice_number, status, paid_at FROM subscription_invoices WHERE id = $INVOICE_ID;"
psql -h 127.0.0.1 -U ems_user -d employee_db -c "SELECT id, organization_id, plan_code, status FROM subscriptions WHERE id = $SUB_ID;"
psql -h 127.0.0.1 -U ems_user -d employee_db -c "SELECT idempotency_key, status, updated_at FROM idempotency_keys WHERE idempotency_key LIKE '%CoreBillingStateEventHandler';"
echo "==========================================================="
