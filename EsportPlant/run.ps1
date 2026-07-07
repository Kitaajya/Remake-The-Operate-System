$OutputEncoding = [System.Text.UTF8Encoding]::new($true)
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($true)
$PSDefaultParameterValues['*:Encoding'] = 'utf8'

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   EsportPlant - Starting Application" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location -LiteralPath $ProjectDir

try {
    Write-Host "[*] Running Spring Boot application..." -ForegroundColor Yellow
    $env:MAVEN_OPTS = "-Dfile.encoding=UTF-8"
    & ".\mvnw.cmd" clean spring-boot:run
} catch {
    Write-Host "[ERROR] Application failed: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    Generated TXT File Content:" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
$txtPath = "D:\桌面\NameCodeForE_Plant.txt"
if (Test-Path -LiteralPath $txtPath) {
    Get-Content -LiteralPath $txtPath
} else {
    Write-Host "[*] File not found: $txtPath" -ForegroundColor Yellow
}
Write-Host ""
Read-Host "Press Enter to exit"
