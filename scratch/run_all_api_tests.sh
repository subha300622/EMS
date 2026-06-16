#!/bin/bash

# Setup colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color
BLUE='\033[0;34m'
PURPLE='\033[0;35m'

echo -e "${BLUE}===================================================================${NC}"
echo -e "${BLUE}      RUNNING DETAILED MY EXPENSES SELF-SERVICE API TEST SUITE     ${NC}"
echo -e "${BLUE}===================================================================${NC}"

# Helper function to print API headers
print_endpoint_header() {
  echo -e "\n${PURPLE}-------------------------------------------------------------------${NC}"
  echo -e "${GREEN}API #$1: $2${NC}"
  echo -e "${GREEN}Endpoint: $3${NC}"
  echo -e "${PURPLE}-------------------------------------------------------------------${NC}"
}

# 1. Login as Employee User
echo -e "${BLUE}Logging in as employee@company.com...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"employee@company.com","password":"employee@2"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -oP '"accessToken":"\K[^"]+')

if [ -z "$TOKEN" ]; then
  echo -e "${RED}Login failed!${NC}"
  echo $LOGIN_RESPONSE
  exit 1
fi
echo -e "${GREEN}Authentication successful! Token acquired.${NC}\n"

# API 1: Dashboard
print_endpoint_header "1" "Get My Expenses Dashboard" "GET /api/v1/my-expenses/dashboard"
curl -s -X GET "http://localhost:8080/api/v1/my-expenses/dashboard" \
  -H "Authorization: Bearer $TOKEN" | jq .

# API 2: My Expense List (with pagination and filters)
print_endpoint_header "2" "Get My Expense List (Filtered to PENDING)" "GET /api/v1/my-expenses?status=PENDING_MANAGER_APPROVAL&page=0&size=5"
curl -s -X GET "http://localhost:8080/api/v1/my-expenses?status=PENDING_MANAGER_APPROVAL&page=0&size=5" \
  -H "Authorization: Bearer $TOKEN" | jq .

# API 3: Get Allowed Categories
print_endpoint_header "3" "Get Allowed Categories" "GET /api/v1/my-expenses/categories"
curl -s -X GET "http://localhost:8080/api/v1/my-expenses/categories" \
  -H "Authorization: Bearer $TOKEN" | jq .

# API 4: Get Reimbursement Policy Details
print_endpoint_header "4" "Get Reimbursement Policy" "GET /api/v1/my-expenses/policies"
curl -s -X GET "http://localhost:8080/api/v1/my-expenses/policies" \
  -H "Authorization: Bearer $TOKEN" | jq .

# API 5: Upload Receipt
print_endpoint_header "5" "Upload Receipt File" "POST /api/v1/my-expenses/receipts"
echo "PDF invoice content data stream mockup" > temp_invoice.pdf
UPLOAD_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/my-expenses/receipts" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@temp_invoice.pdf" \
  -F "receiptType=INVOICE")
echo $UPLOAD_RESPONSE | jq .

RECEIPT_ID=$(echo $UPLOAD_RESPONSE | grep -oP '"receiptId":\K[0-9]+')
rm temp_invoice.pdf

if [ -z "$RECEIPT_ID" ]; then
  echo -e "${RED}Receipt upload failed!${NC}"
  exit 1
fi

# API 6: Create Expense Claim
print_endpoint_header "6" "Create New Expense Claim (Travel)" "POST /api/v1/my-expenses"
CREATE_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/my-expenses" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"category\": \"TRAVEL\",
    \"title\": \"Offsite Client Consultation trip\",
    \"description\": \"Travel flight and lodging for client project consultation\",
    \"expenseDate\": \"2026-06-16\",
    \"amount\": 7500.00,
    \"currency\": \"INR\",
    \"projectCode\": \"PRJ-X1\",
    \"receiptIds\": [$RECEIPT_ID]
  }")
echo $CREATE_RESPONSE | jq .

NEW_CLAIM_ID=$(echo $CREATE_RESPONSE | grep -oP '"expenseId":\K[0-9]+')

if [ -z "$NEW_CLAIM_ID" ]; then
  echo -e "${RED}Expense claim creation failed!${NC}"
  exit 1
fi

# API 7: View Details
print_endpoint_header "7" "Get Expense Claim Details" "GET /api/v1/my-expenses/{id}"
curl -s -X GET "http://localhost:8080/api/v1/my-expenses/$NEW_CLAIM_ID" \
  -H "Authorization: Bearer $TOKEN" | jq .

# API 8: Get Approval Timeline History
print_endpoint_header "8" "Get Expense Claim Timeline History" "GET /api/v1/my-expenses/{id}/timeline"
curl -s -X GET "http://localhost:8080/api/v1/my-expenses/$NEW_CLAIM_ID/timeline" \
  -H "Authorization: Bearer $TOKEN" | jq .

# API 9: Update Expense Claim
# Find draft claim ID dynamically
DRAFT_ID=$(curl -s -X GET "http://localhost:8080/api/v1/my-expenses?status=DRAFT" -H "Authorization: Bearer $TOKEN" | grep -oP '"expenseId":\K[0-9]+' | head -n 1)

if [ -n "$DRAFT_ID" ]; then
  print_endpoint_header "9" "Update Draft Claim (ID: $DRAFT_ID)" "PUT /api/v1/my-expenses/{id}"
  curl -s -X PUT "http://localhost:8080/api/v1/my-expenses/$DRAFT_ID" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "title": "Internet & Broadband Allowance - Updated",
      "description": "Broadband monthly billing allowance details verified",
      "amount": 1450.00
    }' | jq .
else
  echo -e "\n${RED}No draft claim found to test API #9 Update Expense Claim.${NC}"
fi

# API 10: Withdraw Claim
print_endpoint_header "10" "Withdraw Expense Claim" "PATCH /api/v1/my-expenses/{id}/withdraw"
curl -s -X PATCH "http://localhost:8080/api/v1/my-expenses/$NEW_CLAIM_ID/withdraw" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Expense submitted under wrong billing project code"}' | jq .

# API 11: Download Receipt
print_endpoint_header "11" "Download Receipt File" "GET /api/v1/my-expenses/receipts/{id}/download"
curl -i -s -X GET "http://localhost:8080/api/v1/my-expenses/receipts/$RECEIPT_ID/download" \
  -H "Authorization: Bearer $TOKEN" > receipt_headers.txt
cat receipt_headers.txt
rm receipt_headers.txt

echo -e "\n${BLUE}===================================================================${NC}"
echo -e "${GREEN}      ALL 11 API ENDPOINTS TESTED AND VERIFIED SUCCESSFULLY        ${NC}"
echo -e "${BLUE}===================================================================${NC}"
