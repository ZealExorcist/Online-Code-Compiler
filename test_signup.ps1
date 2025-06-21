# PowerShell test script for signup endpoint
Write-Host "Testing signup endpoint..." -ForegroundColor Green

# Test 1: Valid signup request
Write-Host "`nTest 1: Valid signup request" -ForegroundColor Yellow
$body1 = @{
    username = "testuser"
    email = "test@example.com"
    password = "password123"
    confirmPassword = "password123"
} | ConvertTo-Json

try {
    $response1 = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method POST -Body $body1 -ContentType "application/json"
    Write-Host "Success: $($response1 | ConvertTo-Json)" -ForegroundColor Green
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.Exception.Response)" -ForegroundColor Red
}

# Test 2: Invalid signup (missing confirmPassword)
Write-Host "`nTest 2: Invalid signup (missing confirmPassword)" -ForegroundColor Yellow
$body2 = @{
    username = "testuser2"
    email = "test2@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    $response2 = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method POST -Body $body2 -ContentType "application/json"
    Write-Host "Unexpected success: $($response2 | ConvertTo-Json)" -ForegroundColor Yellow
} catch {
    Write-Host "Expected error: $($_.Exception.Message)" -ForegroundColor Green
}

# Test 3: Invalid signup (password mismatch)
Write-Host "`nTest 3: Invalid signup (password mismatch)" -ForegroundColor Yellow
$body3 = @{
    username = "testuser3"
    email = "test3@example.com"
    password = "password123"
    confirmPassword = "different123"
} | ConvertTo-Json

try {
    $response3 = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method POST -Body $body3 -ContentType "application/json"
    Write-Host "Unexpected success: $($response3 | ConvertTo-Json)" -ForegroundColor Yellow
} catch {
    Write-Host "Expected error: $($_.Exception.Message)" -ForegroundColor Green
}

# Test 4: Invalid email format
Write-Host "`nTest 4: Invalid email format" -ForegroundColor Yellow
$body4 = @{
    username = "testuser4"
    email = "invalid-email"
    password = "password123"
    confirmPassword = "password123"
} | ConvertTo-Json

try {
    $response4 = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method POST -Body $body4 -ContentType "application/json"
    Write-Host "Unexpected success: $($response4 | ConvertTo-Json)" -ForegroundColor Yellow
} catch {
    Write-Host "Expected error: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`nTesting complete!" -ForegroundColor Green
