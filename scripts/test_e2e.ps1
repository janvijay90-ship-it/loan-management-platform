Write-Host "=== 1. Requesting JWT Token from API Gateway ===" -ForegroundColor Cyan
$authResp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/token" -Method POST -ContentType "application/json" -Body '{"username": "vijay", "role": "CUSTOMER"}'
$token = $authResp.accessToken
Write-Host "Token obtained: $token" -ForegroundColor Green

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type"  = "application/json"
}

Write-Host "`n=== 2. Submitting Loan Application via Gateway (Port 8080) ===" -ForegroundColor Cyan
$body = '{"customerId":"CUST-1001","applicantName":"Vijay jangili","panNumber":"BXCPV7093L","phoneNumber":"9100334722","requestedAmount":75000.00}'
$submitResp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/loans" -Method POST -Headers $headers -Body $body
$submitJson = $submitResp | ConvertTo-Json -Depth 5
Write-Host "Submission Response:`n$submitJson" -ForegroundColor Green

$refId = $submitResp.loanReferenceId
Write-Host "`nTracking Loan Reference ID: $refId" -ForegroundColor Yellow

Write-Host "Polling for asynchronous event processing (Kafka Outbox -> Risk/CIBIL -> Loan -> Disbursement -> Notification)..." -ForegroundColor Yellow
$maxRetries = 15
$currentStatus = "SUBMITTED"

for ($i = 1; $i -le $maxRetries; $i++) {
    Start-Sleep -Seconds 2
    try {
        $statusResp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/loans/reference/$refId" -Method GET -Headers $headers
        $currentStatus = $statusResp.status
        $cScore = $statusResp.creditScore
        Write-Host "[$i/$maxRetries] Current Loan Status: $currentStatus (CIBIL: $cScore)" -ForegroundColor Cyan
        if ($currentStatus -eq "ACTIVE") {
            break
        }
    } catch {
        Write-Host "Waiting for loan record..." -ForegroundColor Gray
    }
}

Write-Host "`n=== 3. Querying Final Loan Status via Gateway ===" -ForegroundColor Cyan
$statusResp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/loans/reference/$refId" -Method GET -Headers $headers
$statusJson = $statusResp | ConvertTo-Json -Depth 5
Write-Host "Final Loan Status:`n$statusJson" -ForegroundColor Green

Write-Host "`n=== 4. Querying Disbursement Record via Gateway ===" -ForegroundColor Cyan
try {
    $disbResp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/disbursements/loan/$refId" -Method GET -Headers $headers
    $disbJson = $disbResp | ConvertTo-Json -Depth 5
    Write-Host "Disbursement Record:`n$disbJson" -ForegroundColor Green
    Write-Host "`n>>> [SUCCESS] Full End-to-End Microservices Lifecycle Verified! <<<" -ForegroundColor Green
} catch {
    Write-Host "Disbursement query failed: $_" -ForegroundColor Red
}
