$ErrorActionPreference = "Stop"
$aws = "C:\Program Files\Amazon\AWSCLIV2\aws.exe"
$region = "ap-south-1"
$accountId = "306317596640"
$ecrBase = "$accountId.dkr.ecr.$region.amazonaws.com"

$repos = @(
    "loan-portal",
    "api-gateway",
    "loan-service",
    "risk-service",
    "disbursement-service",
    "notification-service",
    "service-registry",
    "config-server"
)

Write-Host "=== 1. Creating ECR Repositories in account $accountId ($region) ===" -ForegroundColor Cyan
foreach ($repo in $repos) {
    Write-Host "Creating repository: $repo..." -ForegroundColor Yellow
    try {
        & $aws ecr create-repository --repository-name $repo --region $region --image-scanning-configuration scanOnPush=true | Out-Null
        Write-Host "  Repository $repo created." -ForegroundColor Green
    } catch {
        Write-Host "  Repository $repo already exists (or verified)." -ForegroundColor Gray
    }
}

Write-Host "`n=== 2. Authenticating Docker to AWS ECR ===" -ForegroundColor Cyan
$loginPassword = & $aws ecr get-login-password --region $region
$loginPassword | docker login --username AWS --password-stdin $ecrBase

Write-Host "`n=== 3. Tagging and Pushing Docker Images ===" -ForegroundColor Cyan
$imageMap = @{
    "loan-management-platform-loan-portal:latest"             = "$ecrBase/loan-portal:latest"
    "loan-management-platform-api-gateway:latest"             = "$ecrBase/api-gateway:latest"
    "loan-management-platform-loan-service:latest"            = "$ecrBase/loan-service:latest"
    "loan-management-platform-risk-service:latest"            = "$ecrBase/risk-service:latest"
    "loan-management-platform-disbursement-service:latest"    = "$ecrBase/disbursement-service:latest"
    "loan-management-platform-notification-service:latest"    = "$ecrBase/notification-service:latest"
    "loan-management-platform-service-registry:latest"        = "$ecrBase/service-registry:latest"
    "loan-management-platform-config-server:latest"           = "$ecrBase/config-server:latest"
}

foreach ($entry in $imageMap.GetEnumerator()) {
    $localImg = $entry.Key
    $remoteImg = $entry.Value
    Write-Host "Tagging $localImg -> $remoteImg" -ForegroundColor Yellow
    docker tag $localImg $remoteImg
    Write-Host "Pushing $remoteImg..." -ForegroundColor Cyan
    docker push $remoteImg
}

Write-Host "`n>>> [SUCCESS] All 8 microservice container images pushed to Amazon ECR! <<<" -ForegroundColor Green
