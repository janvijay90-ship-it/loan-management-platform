try {
    $r = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/token" -Method POST -ContentType "application/json" -Body '{"username":"admin","role":"ADMIN"}'
    Write-Host "SUCCESS - accessToken: $($r.accessToken)"
} catch {
    Write-Host "FAILED: $_"
}
