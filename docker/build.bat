@echo off
REM Build script for all Docker images (Windows)

echo Building Docker images for Online Compiler...

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Docker is not running. Please start Docker and try again.
    exit /b 1
)

REM Build images using Docker Compose
echo Building all language containers...
docker-compose build

REM Verify images were built
echo Verifying built images...
docker images | findstr "online-compiler"

echo Docker image build complete!
echo.
echo Available images:
echo - online-compiler/python:latest
echo - online-compiler/java:latest
echo - online-compiler/cpp:latest
echo - online-compiler/javascript:latest
echo - online-compiler/go:latest

pause
