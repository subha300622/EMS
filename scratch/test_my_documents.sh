#!/bin/bash
# test_my_documents.sh - Manual verification script for My Documents Self-Service Module

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

echo "=== Endpoint 11: GET /api/v1/my-documents/document-types ==="
curl -s -X GET "${BASE_URL}/my-documents/document-types" -H "${AUTH_HEADER}" | python3 -m json.tool > /dev/null
echo "Document types retrieved successfully."

echo "=== Endpoint 1: GET /api/v1/my-documents/dashboard ==="
curl -s -X GET "${BASE_URL}/my-documents/dashboard" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "=== Endpoint 2: GET /api/v1/my-documents/categories ==="
curl -s -X GET "${BASE_URL}/my-documents/categories" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "=== Endpoint 3: GET /api/v1/my-documents/categories/1/documents ==="
DOCS_RESP=$(curl -s -X GET "${BASE_URL}/my-documents/categories/1/documents" -H "${AUTH_HEADER}")
echo "$DOCS_RESP" | python3 -m json.tool

# Extract passport documentId from category 1 documents
PASSPORT_ID=$(echo "$DOCS_RESP" | python3 -c "
import sys, json
data = json.load(sys.stdin).get('data', {})
docs = data.get('documents', [])
passport_docs = [d for d in docs if d.get('documentType') == 'PASSPORT']
if passport_docs:
    print(passport_docs[0]['documentId'])
else:
    print(docs[0]['documentId'])
")

echo "Selected Passport Document ID for testing: ${PASSPORT_ID}"

echo "=== Endpoint 6: GET /api/v1/my-documents/{documentId} ==="
curl -s -X GET "${BASE_URL}/my-documents/${PASSPORT_ID}" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "=== Endpoint 7: GET /api/v1/my-documents/{documentId}/preview ==="
curl -s -X GET "${BASE_URL}/my-documents/${PASSPORT_ID}/preview" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "=== Endpoint 8: GET /api/v1/my-documents/{documentId}/download ==="
curl -s -I -X GET "${BASE_URL}/my-documents/${PASSPORT_ID}/download" -H "${AUTH_HEADER}"

# Check if DRIVING_LICENSE has already been uploaded in a previous run
DL_DOC_ID=$(echo "$DOCS_RESP" | python3 -c "
import sys, json
data = json.load(sys.stdin).get('data', {})
docs = data.get('documents', [])
dl_docs = [d for d in docs if d.get('documentType') == 'DRIVING_LICENSE' and d.get('status') != 'NOT_UPLOADED']
if dl_docs:
    print(dl_docs[0]['documentId'])
else:
    print('')
")

if [ -n "$DL_DOC_ID" ]; then
  echo "Driving License already uploaded in previous run. Reusing existing Document ID: ${DL_DOC_ID}"
else
  echo "=== Endpoint 4: POST /api/v1/my-documents (Upload Driving License) ==="
  # Create dummy file to upload
  echo "Dummy DL File Content" > scratch/dummy_dl.pdf

  UPLOAD_RESP=$(curl -s -X POST "${BASE_URL}/my-documents" \
    -H "${AUTH_HEADER}" \
    -F "file=@scratch/dummy_dl.pdf" \
    -F "categoryId=1" \
    -F "documentType=DRIVING_LICENSE" \
    -F "documentNumber=DL987654" \
    -F "issuedDate=2022-01-01" \
    -F "expiryDate=2032-01-01" \
    -F "remarks=Test DL Upload")

  echo "$UPLOAD_RESP" | python3 -m json.tool

  DL_DOC_ID=$(echo "$UPLOAD_RESP" | python3 -c "
import sys, json
data = json.load(sys.stdin).get('data', {})
print(data.get('documentId', ''))
")

  echo "Uploaded DL Document ID: ${DL_DOC_ID}"
fi

echo "=== Endpoint 5: PUT /api/v1/my-documents/{documentId} (Replace Driving License) ==="
echo "Renewed Dummy DL File Content" > scratch/dummy_dl_renewed.pdf

curl -s -X PUT "${BASE_URL}/my-documents/${DL_DOC_ID}" \
  -H "${AUTH_HEADER}" \
  -F "file=@scratch/dummy_dl_renewed.pdf" \
  -F "remarks=Renewed DL Upload" | python3 -m json.tool

echo "=== Endpoint 9: GET /api/v1/my-documents/notifications ==="
curl -s -X GET "${BASE_URL}/my-documents/notifications" -H "${AUTH_HEADER}" | python3 -m json.tool

echo "=== Endpoint 10: GET /api/v1/my-documents/history ==="
curl -s -X GET "${BASE_URL}/my-documents/history?page=0&size=5" -H "${AUTH_HEADER}" | python3 -m json.tool

# Cleanup dummy files
rm -f scratch/dummy_dl.pdf
rm -f scratch/dummy_dl_renewed.pdf

echo "Manual verification of My Documents completed successfully!"
