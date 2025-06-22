@echo off
echo =====================================
echo Online Compiler - Windows Setup
echo =====================================
echo.

:: Set color for better visibility
color 0A

:: Check if we're running as administrator
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Running with administrator privileges...
) else (
    echo WARNING: Some installations may require administrator privileges
    echo Consider running as administrator if you encounter permission issues
)

echo.
echo Checking prerequisites...
echo.

:: Check Java
echo [1/4] Checking Java installation...
java -version >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Java is installed
    java -version
) else (
    echo ❌ Java 17+ is required but not found
    echo Please install Java 17+ from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

echo.

:: Check Node.js
echo [2/4] Checking Node.js installation...
node --version >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Node.js is installed
    node --version
    npm --version
) else (
    echo ❌ Node.js is required but not found
    echo Please install Node.js LTS from: https://nodejs.org/
    echo.
    pause
    exit /b 1
)

echo.

:: Check Docker
echo [3/4] Checking Docker installation...
docker --version >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Docker is installed
    docker --version
) else (
    echo ❌ Docker is required but not found
    echo Please install Docker Desktop from: https://www.docker.com/products/docker-desktop/
    echo.
    pause
    exit /b 1
)

echo.

:: Check Git
echo [4/4] Checking Git installation...
git --version >nul 2>&1
if %errorLevel% == 0 (
    echo ✅ Git is installed
    git --version
) else (
    echo ⚠️ Git not found - you may need it for development
    echo Install from: https://git-scm.com/download/win
)

echo.
echo =====================================
echo All prerequisites verified!
echo Starting project setup...
echo =====================================
echo.

:: Setup Backend
echo [Step 1] Setting up Backend (Spring Boot)...
echo.

if not exist "backend" (
    echo ❌ Backend directory not found
    echo Make sure you're running this script from the project root directory
    pause
    exit /b 1
)

cd backend

echo Creating environment file...
if not exist ".env" (
    if exist ".env.example" (
        copy ".env.example" ".env" >nul
        echo ✅ Created .env from .env.example
    ) else (
        echo # Database Configuration > .env
        echo DB_HOST=localhost >> .env
        echo DB_PORT=5432 >> .env
        echo DB_NAME=compiler_db >> .env
        echo DB_USERNAME=postgres >> .env
        echo DB_PASSWORD=your_password >> .env
        echo. >> .env
        echo # JWT Configuration >> .env
        echo JWT_SECRET=your-256-bit-secret-key-change-this-in-production >> .env
        echo JWT_EXPIRATION=86400000 >> .env
        echo. >> .env
        echo # Docker Configuration >> .env
        echo DOCKER_ENABLED=true >> .env
        echo EXECUTION_TIMEOUT=30000 >> .env
        echo MAX_OUTPUT_SIZE=10240 >> .env
        echo. >> .env
        echo # Rate Limiting >> .env
        echo RATE_LIMIT_ENABLED=true >> .env
        echo REQUESTS_PER_MINUTE=10 >> .env
        echo ✅ Created default .env file
    )
) else (
    echo ✅ .env file already exists
)

echo.
echo Installing backend dependencies and building...
call mvnw.cmd clean compile
if %errorLevel% neq 0 (
    echo ❌ Backend build failed
    cd ..
    pause
    exit /b 1
)

echo ✅ Backend setup completed
cd ..

echo.
echo [Step 2] Setting up Frontend (Vue.js)...
echo.

if not exist "frontend" (
    echo ❌ Frontend directory not found
    cd ..
    pause
    exit /b 1
)

cd frontend

echo Creating environment file...
if not exist ".env" (
    if exist ".env.example" (
        copy ".env.example" ".env" >nul
        echo ✅ Created .env from .env.example
    ) else (
        echo # API Configuration > .env
        echo VITE_API_URL=http://localhost:8080 >> .env
        echo. >> .env
        echo # Application Configuration >> .env
        echo VITE_APP_NAME=Online Compiler >> .env
        echo VITE_APP_VERSION=1.0.0 >> .env
        echo. >> .env
        echo # Feature Flags >> .env
        echo VITE_ENABLE_ANALYTICS=false >> .env
        echo VITE_ENABLE_DEBUG=false >> .env
        echo. >> .env
        echo # Theme Configuration >> .env
        echo VITE_DEFAULT_THEME=dark >> .env
        echo. >> .env
        echo # Security Configuration >> .env
        echo VITE_ENABLE_CSP=true >> .env
        echo VITE_SESSION_TIMEOUT=3600000 >> .env
        echo ✅ Created default .env file
    )
) else (
    echo ✅ .env file already exists
)

echo.
echo Installing frontend dependencies...
call npm install
if %errorLevel% neq 0 (
    echo ❌ Frontend dependency installation failed
    cd ..
    pause
    exit /b 1
)

echo.
echo Building frontend...
call npm run build
if %errorLevel% neq 0 (
    echo ❌ Frontend build failed
    cd ..
    pause
    exit /b 1
)

echo ✅ Frontend setup completed
cd ..

echo.
echo [Step 3] Setting up Docker images for code execution...
echo.

if not exist "docker" (
    echo ❌ Docker directory not found
    pause
    exit /b 1
)

cd docker

echo Building Docker images for supported languages...
echo This may take several minutes...
echo.

if exist "build.bat" (
    call build.bat
    if %errorLevel% neq 0 (
        echo ⚠️ Some Docker images may have failed to build
        echo You can continue and build them individually later
    ) else (
        echo ✅ All Docker images built successfully
    )
) else (
    echo ⚠️ build.bat not found in docker directory
    echo You may need to build Docker images manually
)

cd ..

echo.
echo =====================================
echo 🎉 Setup completed successfully!
echo =====================================
echo.
echo Next steps:
echo.
echo 1. Start the backend server:
echo    cd backend
echo    mvnw.cmd spring-boot:run
echo.
echo 2. In a new terminal, start the frontend:
echo    cd frontend
echo    npm run dev
echo.
echo 3. Open your browser and go to:
echo    http://localhost:3000
echo.
echo For more information, see:
echo - README.md for quick start guide
echo - doc.md for comprehensive documentation
echo.
echo Happy coding! 🚀
echo.
pause
