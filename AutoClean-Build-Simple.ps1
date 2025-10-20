#!/usr/bin/env pwsh
# AutoClean-Build.ps1 - Automated Android build cleanup script

param(
    [string]$BuildTarget = "assembleDebug"
)

Write-Host "AutoClean Android Build Script" -ForegroundColor Green
Write-Host "==============================" -ForegroundColor Green

# Stop processes
Write-Host "Stopping Android/Java processes..." -ForegroundColor Yellow
try { .\gradlew --stop 2>$null } catch { }
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Get-Process -Name "studio64" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

Start-Sleep -Seconds 2

# Clean build directory
Write-Host "Cleaning build directory..." -ForegroundColor Yellow
$buildPath = "app\build"
if (Test-Path $buildPath) {
    Remove-Item -Path $buildPath -Recurse -Force -ErrorAction SilentlyContinue
    cmd /c "rmdir /s /q app\build" 2>$null
}

Start-Sleep -Seconds 1

# Build
Write-Host "Starting build: $BuildTarget" -ForegroundColor Cyan
$buildCmd = ".\gradlew $BuildTarget --no-daemon --no-build-cache"
Write-Host "Running: $buildCmd" -ForegroundColor Gray

try {
    Invoke-Expression $buildCmd
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Build completed successfully!" -ForegroundColor Green
    } else {
        Write-Host "Build failed with exit code $LASTEXITCODE" -ForegroundColor Red
    }
} catch {
    Write-Host "Build failed: $($_.Exception.Message)" -ForegroundColor Red
}