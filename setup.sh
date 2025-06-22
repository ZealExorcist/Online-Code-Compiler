#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅${NC} $1"
}

print_error() {
    echo -e "${RED}❌${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠️${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ️${NC} $1"
}

print_header() {
    echo -e "${CYAN}$1${NC}"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check Java version
check_java_version() {
    if command_exists java; then
        local version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        local major_version=$(echo $version | cut -d'.' -f1)
        if [ "$major_version" -ge 17 ]; then
            return 0
        else
            return 1
        fi
    else
        return 1
    fi
}

echo "====================================="
print_header "Online Compiler - Linux/macOS Setup"
echo "====================================="
echo

print_info "Checking prerequisites..."
echo

# Check Java
echo "[1/4] Checking Java installation..."
if check_java_version; then
    print_status "Java 17+ is installed"
    java -version
else
    print_error "Java 17+ is required but not found or version is too old"
    echo "Please install Java 17+ from:"
    echo "- Ubuntu/Debian: sudo apt install openjdk-17-jdk"
    echo "- CentOS/RHEL: sudo yum install java-17-openjdk-devel"
    echo "- macOS: brew install openjdk@17"
    echo "- Or download from: https://adoptium.net/"
    exit 1
fi

echo

# Check Node.js
echo "[2/4] Checking Node.js installation..."
if command_exists node; then
    local node_version=$(node --version | cut -d'v' -f2 | cut -d'.' -f1)
    if [ "$node_version" -ge 18 ]; then
        print_status "Node.js 18+ is installed"
        node --version
        npm --version
    else
        print_error "Node.js 18+ is required but found version $(node --version)"
        echo "Please update Node.js to version 18 or higher"
        exit 1
    fi
else
    print_error "Node.js is required but not found"
    echo "Please install Node.js LTS from:"
    echo "- Ubuntu/Debian: curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash - && sudo apt-get install -y nodejs"
    echo "- CentOS/RHEL: curl -fsSL https://rpm.nodesource.com/setup_lts.x | sudo bash - && sudo yum install -y nodejs"
    echo "- macOS: brew install node"
    echo "- Or download from: https://nodejs.org/"
    exit 1
fi

echo

# Check Docker
echo "[3/4] Checking Docker installation..."
if command_exists docker; then
    print_status "Docker is installed"
    docker --version
    
    # Check if Docker daemon is running
    if ! docker info >/dev/null 2>&1; then
        print_warning "Docker is installed but daemon is not running"
        print_info "Please start Docker daemon:"
        echo "- Linux: sudo systemctl start docker"
        echo "- macOS: Start Docker Desktop application"
    fi
else
    print_error "Docker is required but not found"
    echo "Please install Docker from:"
    echo "- Ubuntu/Debian: sudo apt install docker.io && sudo systemctl start docker"
    echo "- CentOS/RHEL: sudo yum install docker && sudo systemctl start docker"
    echo "- macOS: brew install --cask docker"
    echo "- Or download from: https://www.docker.com/products/docker-desktop/"
    exit 1
fi

echo

# Check Git
echo "[4/4] Checking Git installation..."
if command_exists git; then
    print_status "Git is installed"
    git --version
else
    print_warning "Git not found - you may need it for development"
    echo "Install with:"
    echo "- Ubuntu/Debian: sudo apt install git"
    echo "- CentOS/RHEL: sudo yum install git"
    echo "- macOS: brew install git"
fi

echo
echo "====================================="
print_status "All prerequisites verified!"
print_header "Starting project setup..."
echo "====================================="
echo

# Setup Backend
print_header "[Step 1] Setting up Backend (Spring Boot)..."
echo

if [ ! -d "backend" ]; then
    print_error "Backend directory not found"
    echo "Make sure you're running this script from the project root directory"
    exit 1
fi

cd backend

echo "Creating environment file..."
if [ ! -f ".env" ]; then
    if [ -f ".env.example" ]; then
        cp ".env.example" ".env"
        print_status "Created .env from .env.example"
    else
        cat > .env << EOF
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=compiler_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-change-this-in-production
JWT_EXPIRATION=86400000

# Docker Configuration
DOCKER_ENABLED=true
EXECUTION_TIMEOUT=30000
MAX_OUTPUT_SIZE=10240

# Rate Limiting
RATE_LIMIT_ENABLED=true
REQUESTS_PER_MINUTE=10
EOF
        print_status "Created default .env file"
    fi
else
    print_status ".env file already exists"
fi

echo
echo "Installing backend dependencies and building..."
if [ -f "./mvnw" ]; then
    chmod +x ./mvnw
    ./mvnw clean compile
else
    mvn clean compile
fi

if [ $? -ne 0 ]; then
    print_error "Backend build failed"
    cd ..
    exit 1
fi

print_status "Backend setup completed"
cd ..

echo
print_header "[Step 2] Setting up Frontend (Vue.js)..."
echo

if [ ! -d "frontend" ]; then
    print_error "Frontend directory not found"
    exit 1
fi

cd frontend

echo "Creating environment file..."
if [ ! -f ".env" ]; then
    if [ -f ".env.example" ]; then
        cp ".env.example" ".env"
        print_status "Created .env from .env.example"
    else
        cat > .env << EOF
# API Configuration
VITE_API_URL=http://localhost:8080

# Application Configuration
VITE_APP_NAME=Online Compiler
VITE_APP_VERSION=1.0.0

# Feature Flags
VITE_ENABLE_ANALYTICS=false
VITE_ENABLE_DEBUG=false

# Theme Configuration
VITE_DEFAULT_THEME=dark

# Security Configuration
VITE_ENABLE_CSP=true
VITE_SESSION_TIMEOUT=3600000
EOF
        print_status "Created default .env file"
    fi
else
    print_status ".env file already exists"
fi

echo
echo "Installing frontend dependencies..."
npm install

if [ $? -ne 0 ]; then
    print_error "Frontend dependency installation failed"
    cd ..
    exit 1
fi

echo
echo "Building frontend..."
npm run build

if [ $? -ne 0 ]; then
    print_error "Frontend build failed"
    cd ..
    exit 1
fi

print_status "Frontend setup completed"
cd ..

echo
print_header "[Step 3] Setting up Docker images for code execution..."
echo

if [ ! -d "docker" ]; then
    print_error "Docker directory not found"
    exit 1
fi

cd docker

echo "Building Docker images for supported languages..."
echo "This may take several minutes..."
echo

if [ -f "build.sh" ]; then
    chmod +x build.sh
    ./build.sh
    if [ $? -ne 0 ]; then
        print_warning "Some Docker images may have failed to build"
        echo "You can continue and build them individually later"
    else
        print_status "All Docker images built successfully"
    fi
else
    print_warning "build.sh not found in docker directory"
    echo "You may need to build Docker images manually"
fi

cd ..

echo
echo "====================================="
print_status "🎉 Setup completed successfully!"
echo "====================================="
echo
echo "Next steps:"
echo
echo "1. Start the backend server:"
echo "   cd backend"
echo "   ./mvnw spring-boot:run    # or mvn spring-boot:run"
echo
echo "2. In a new terminal, start the frontend:"
echo "   cd frontend"
echo "   npm run dev"
echo
echo "3. Open your browser and go to:"
echo "   http://localhost:3000"
echo
echo "For more information, see:"
echo "- README.md for quick start guide"
echo "- doc.md for comprehensive documentation"
echo
print_status "Happy coding! 🚀"
echo
