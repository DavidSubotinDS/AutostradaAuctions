@echo off
echo Starting Autostrada Auctions Backend on all network interfaces...
echo Server will be available at http://192.168.0.7:5000
echo.
cd backend\AutostradaAuctions.Api
dotnet run --urls "http://0.0.0.0:5000"
pause