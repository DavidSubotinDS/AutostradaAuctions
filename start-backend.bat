@echo off
echo Starting Autostrada Auctions Backend...
echo.
echo This will start:
echo - SQL Server database in Docker
echo - .NET API server
echo - Pre-populated with sample auction data
echo.
echo Make sure Docker Desktop is running!
echo.
pause

echo Building and starting services...
docker-compose up --build
