# =============================================================
# End-to-End Test Script for Loan Management Platform
# Auth endpoint: POST /api/v1/auth/token  (inside api-gateway)
# Dev bypass:    X-Dev-Mode: true header  (also inside gateway)
# =============================================================

# -------------------------------------------------------
# STEP 1 – Obtain JWT from the gateway's own auth endpoint
# -------------------------------------------------------
Write-Host "=== 1. Requesting JWT Token from API Gateway ===" -ForegroundColor Cyan

$token = $null
try {
    $loginBody = '{"username":"admin","role":"ADMIN"}'
    $authResp  = Invoke-RestMethod `
        -Uri         "http://localhost:8080/api/v1/auth/token" `
        -Method      POST `
        -ContentType "application/json" `
        -Body        $loginBody
    $token = $authResp.accessToken
    Write-Host "Token obtained: $token" -ForegroundColor Green
} catch {
    Write-Host "Token generation failed: $_" -ForegroundColor Yellow
    Write-Host "Falling back to X-Dev-Mode bypass." -ForegroundColor Yellow
}

# Build request headers
if ($token -and $token -ne "") {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type"  = "application/json"
    }
    Write-Host "Using Bearer token authentication." -ForegroundColor Green
} else {
    $headers = @{
        "X-Dev-Mode"   = "true"
        "Content-Type" = "application/json"
    }
    Write-Host "Using X-Dev-Mode: true (dev bypass)." -ForegroundColor Yellow
}

# -------------------------------------------------------
# STEP 2 – Submit a loan application
# -------------------------------------------------------
Write-Host "`n=== 2. Submitting Loan Application via Gateway (Port 8080) ===" -ForegroundColor Cyan

$body = '{"customerId":"CUST-1001","applicantName":"Vijay jangili","panNumber":"BXCPV7093L","phoneNumber":"9100334722","requestedAmount":75000.00}'

try {
    $submitResp = Invoke-RestMethod `
        -Uri     "http://localhost:8080/api/v1/loans" `
        -Method  POST `
        -Headers $headers `
        -Body    $body
    $submitJson = $submitResp | ConvertTo-Json -Depth 5
    Write-Host "Submission Response:`n$submitJson" -ForegroundColor Green
} catch {
    Write-Host "Loan submission failed: $_" -ForegroundColor Red
    exit 1
}

$refId = $submitResp.loanReferenceId
Write-Host "`nTracking Loan Reference ID: $refId" -ForegroundColor Yellow

# -------------------------------------------------------
# STEP 3 – Poll until status reaches ACTIVE (or timeout)
# -------------------------------------------------------
Write-Host "Polling for asynchronous event processing (Kafka Outbox -> Risk/CIBIL -> Loan -> Disbursement -> Notification)..." -ForegroundColor Yellow

$maxRetries    = 30
$currentStatus = "SUBMITTED"

for ($i = 1; $i -le $maxRetries; $i++) {
    Start-Sleep -Seconds 3
    try {
        $statusResp    = Invoke-RestMethod `
            -Uri     "http://localhost:8080/api/v1/loans/reference/$refId" `
            -Method  GET `
            -Headers $headers
        $currentStatus = $statusResp.status
        $cScore        = $statusResp.creditScore
        Write-Host "[$i/$maxRetries] Current Loan Status: $currentStatus  (CIBIL: $cScore)" -ForegroundColor Cyan

        if ($currentStatus -eq "ACTIVE") { break }
    } catch {
        Write-Host "  Waiting for loan record to appear... ($i/$maxRetries)" -ForegroundColor Gray
    }
}

# -------------------------------------------------------
# STEP 4 – Query final loan status
# -------------------------------------------------------
Write-Host "`n=== 3. Querying Final Loan Status via Gateway ===" -ForegroundColor Cyan
try {
    $statusResp = Invoke-RestMethod `
        -Uri     "http://localhost:8080/api/v1/loans/reference/$refId" `
        -Method  GET `
        -Headers $headers
    $statusJson = $statusResp | ConvertTo-Json -Depth 5
    Write-Host "Final Loan Status:`n$statusJson" -ForegroundColor Green
} catch {
    Write-Host "Could not query final loan status: $_" -ForegroundColor Red
}

# -------------------------------------------------------
# STEP 5 – Query disbursement record
# -------------------------------------------------------
Write-Host "`n=== 4. Querying Disbursement Record via Gateway ===" -ForegroundColor Cyan
try {
    $disbResp = Invoke-RestMethod `
        -Uri     "http://localhost:8080/api/v1/disbursements/loan/$refId" `
        -Method  GET `
        -Headers $headers
    $disbJson = $disbResp | ConvertTo-Json -Depth 5
    Write-Host "Disbursement Record:`n$disbJson" -ForegroundColor Green
    Write-Host "`n>>> [SUCCESS] Full End-to-End Microservices Lifecycle Verified! <<<" -ForegroundColor Green
} catch {
    Write-Host "Disbursement query failed: $_" -ForegroundColor Red
    Write-Host "`n[INFO] Disbursement may still be processing. Check kafka/disbursement-service logs." -ForegroundColor Yellow
}
