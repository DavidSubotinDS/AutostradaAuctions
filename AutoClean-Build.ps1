#!/usr/bin/env pwsh
# AutoClean-Build.ps1 - Automated Android build cleanup script
# Usage: .\AutoClean-Build.ps1 [target]
# Example: .\AutoClean-Build.ps1 assembleDebug

param(
    [string]$BuildTarget = "assembleDebug",
    [switch]$Force = $false
)

Write-Host "🔧 AutoClean Android Build Script" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

function Stop-AndroidProcesses {
    Write-Host "🛑 Stopping Android/Java processes..." -ForegroundColor Yellow
    
    # Stop Gradle daemons
    try {
        & .\gradlew --stop 2>$null
        Write-Host "✅ Gradle daemons stopped" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  No Gradle daemons running" -ForegroundColor Yellow
    }
    
    # Kill Java processes
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
    if ($javaProcesses) {
        $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
        Write-Host "✅ Java processes terminated ($($javaProcesses.Count) processes)" -ForegroundColor Green
    }
    
    # Kill Android Studio (optional)
    if ($Force) {
        $studioProcesses = Get-Process -Name "studio64" -ErrorAction SilentlyContinue
        if ($studioProcesses) {
            $studioProcesses | Stop-Process -Force -ErrorAction SilentlyContinue  
            Write-Host "✅ Android Studio terminated" -ForegroundColor Green
        }
    }
}

function Remove-BuildDirectory {
    Write-Host "🗑️  Cleaning build directory..." -ForegroundColor Yellow
    
    $buildPath = "app\build"
    
    if (Test-Path $buildPath) {
        # Try multiple deletion methods
        try {
            # Method 1: PowerShell Remove-Item
            Remove-Item -Path $buildPath -Recurse -Force -ErrorAction Stop
            Write-Host "✅ Build directory cleaned (PowerShell)" -ForegroundColor Green
        } catch {
            try {
                # Method 2: CMD rmdir
                cmd /c "rmdir /s /q `"$buildPath`"" 2>`$null
                if (-not (Test-Path $buildPath)) {
                    Write-Host "✅ Build directory cleaned (CMD)" -ForegroundColor Green
                } else {
                    throw "Directory still exists"
                }
            } catch {
                # Method 3: Individual file deletion
                Write-Host "⚠️  Attempting individual file cleanup..." -ForegroundColor Yellow
                Get-ChildItem -Path $buildPath -Recurse -Force | Remove-Item -Force -Recurse -ErrorAction SilentlyContinue
                
                if (Test-Path $buildPath) {
                    Write-Host "❌ Some files may still be locked. Consider restarting if build fails." -ForegroundColor Red
                } else {
                    Write-Host "✅ Build directory cleaned (individual files)" -ForegroundColor Green
                }
            }
        }
    } else {
        Write-Host "✅ Build directory already clean" -ForegroundColor Green
    }
}

function Start-Build {
    param([string]$Target)
    
    Write-Host "🔨 Starting build: $Target" -ForegroundColor Cyan
    
    # Use no-daemon to prevent file locking
    $buildCmd = ".\gradlew $Target --no-daemon --no-build-cache --refresh-dependencies"
    
    Write-Host "📝 Running: $buildCmd" -ForegroundColor Gray
    
    try {
        Invoke-Expression $buildCmd
        if ($LASTEXITCODE -eq 0) {
            Write-Host "🎉 Build completed successfully!" -ForegroundColor Green
        } else {
            Write-Host "❌ Build failed with exit code $LASTEXITCODE" -ForegroundColor Red
        }
    } catch {
        Write-Host "❌ Build failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Main execution
try {
    Stop-AndroidProcesses
    Start-Sleep -Seconds 2
    Remove-BuildDirectory
    Start-Sleep -Seconds 1
    Start-Build -Target $BuildTarget
} catch {
    Write-Host "❌ Script failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}