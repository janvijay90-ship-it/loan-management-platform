#!/usr/bin/env bash
set -e

echo "=== 1. Requesting JWT Token from API Gateway ==="
AUTH_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/auth/token" \
  -H "Content-Type: application/json" \
  -d '{"username": "vijay", "role": "CUSTOMER"}')

TOKEN=$(echo "$AUTH_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "Failed to obtain JWT token! Response was: $AUTH_RESPONSE"
  exit 1
fi
echo "JWT Token successfully acquired."

echo ""
echo "=== 2. Submitting Loan Application via API Gateway ==="
SUBMIT_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/loans" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-1001","applicantName":"Vijay jangili","panNumber":"BXCPV7093L","phoneNumber":"9100334722","requestedAmount":75000.00}')

echo "Submission Response: $SUBMIT_RESPONSE"
REF_ID=$(echo "$SUBMIT_RESPONSE" | grep -o '"loanReferenceId":"[^"]*' | cut -d'"' -f4)

if [ -z "$REF_ID" ]; then
  echo "Failed to submit loan application! Response was: $SUBMIT_RESPONSE"
  exit 1
fi
echo "Tracking Loan Reference ID: $REF_ID"

echo ""
echo "Waiting 6 seconds for asynchronous Kafka outbox event processing..."
sleep 6

echo ""
echo "=== 3. Querying Final Loan Status ==="
STATUS_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/v1/loans/reference/$REF_ID" \
  -H "Authorization: Bearer $TOKEN")
echo "Status Response: $STATUS_RESPONSE"

echo ""
echo "=== 4. Querying Disbursement Record ==="
DISB_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/v1/disbursements/loan/$REF_ID" \
  -H "Authorization: Bearer $TOKEN")
echo "Disbursement Response: $DISB_RESPONSE"

echo ""
echo "E2E Smoke Test completed successfully!"
