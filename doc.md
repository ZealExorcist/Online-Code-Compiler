# Online Compiler - Complete Documentation

A secure, multi-language online code compiler and execution platform with Docker-based sandboxing.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Supported Languages](#supported-languages)
- [Setup & Installation](#setup--installation)
- [Development](#development)
- [API Documentation](#api-documentation)
- [Security](#security)
- [Deployment](#deployment)
- [Configuration](#configuration)
- [Contributing](#contributing)

## Overview

The Online Compiler is a web-based platform that allows users to write, compile, and execute code in multiple programming languages within secure Docker containers. It features a modern Vue.js frontend with a Spring Boot backend, providing a seamless development experience without requiring local development environment setup.

### Key Benefits

- **Multi-language Support**: Execute code in 11+ programming languages
- **Security**: Docker-based sandboxing with resource limits
- **Real-time Collaboration**: Share code snippets via unique URLs
- **Modern UI**: Monaco Editor with syntax highlighting and IntelliSense
- **No Authentication Required**: Instant access for basic usage
- **API Access**: RESTful API for integration with external applications

## Architecture

### System Overview

```
Browser (Vue.js SPA)
   │
   ▼ (REST API calls)
┌─────────────────────────┐
│  Spring Boot Backend    │ ──► Docker Containers ──► Code Execution
│  (Java 17+)            │     (Isolated Sandboxes)    (Compile/Run)
├─────────────────────────┤
│  Database Layer         │
│  (PostgreSQL/H2)        │
└─────────────────────────┘
```

### Technology Stack

**Frontend:**
- Vue.js 3 with TypeScript
- Monaco Editor (VS Code editor for web)
- Vite build tool
- Axios for HTTP requests
- CSS Variables for theming

**Backend:**
- Spring Boot 3.x (Java 17+)
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL/H2 Database
- Docker Java API
- Maven build tool

**Infrastructure:**
- Docker containers for code execution
- Redis for caching (optional)
- Nginx for reverse proxy (production)

## Features

### Core Features

✅ **Multi-Language Code Execution**
- Python 3.11 with standard library
- Java 17 with compilation and execution
- C/C++ with GCC 11 compiler
- JavaScript/TypeScript with Node.js 18
- Go 1.21 with standard library
- Rust 1.70 with Cargo
- Ruby 3.2 with gems
- R 4.3 with CRAN packages
- C# .NET 7 runtime

✅ **Advanced Code Editor**
- Monaco Editor integration (VS Code-like experience)
- Syntax highlighting for all languages
- IntelliSense and auto-completion
- Error detection and highlighting
- Code formatting and indentation
- Multiple themes (dark/light with custom schemes)
- Configurable font size and tab size
- Search and replace functionality
- Bracket matching and code folding

✅ **Security & Sandboxing**
- Docker container isolation
- Network access disabled
- Resource limits (CPU, memory, execution time)
- Non-root user execution
- Input validation and sanitization
- Rate limiting on API endpoints

✅ **Code Sharing & Collaboration**
- Generate unique shareable URLs
- No authentication required for sharing
- Load shared snippets instantly
- Export/import functionality

✅ **User Management** (Optional)
- JWT-based authentication
- User profiles and preferences
- Snippet management dashboard
- API key generation for programmatic access
- Usage analytics and limits

### User Interface Features

✅ **Modern Design System**
- Responsive layout for all screen sizes
- Custom theme system with CSS variables
- Dark/Light themes with professional color schemes
- Intuitive toolbar with language selection
- Real-time output display with enhanced scrolling
- Input panel for interactive programs
- **Package Commands interface** with predefined library selection for secure package installation

✅ **Enhanced Editor Experience**
- **Monaco Editor**: Full VS Code-like experience
- **Smooth Scrolling**: Enhanced scroll wheel support for long outputs
- **Real-time Error Highlighting**: Syntax validation as you type
- **Code Intelligence**: Auto-completion, bracket matching, code folding
- **Customizable Interface**: Font size, tab size, theme preferences
- **Keyboard Shortcuts**: Standard IDE shortcuts (Ctrl+Enter, Ctrl+S)

✅ **Settings Management**
- **Centralized Settings**: Single settings component for all preferences
- **Real-time Sync**: Settings apply immediately across the interface
- **Gemini API Integration**: Secure API key management for AI features
- **Theme Customization**: Multiple theme options with instant preview
- **Editor Preferences**: Font, tab size, and display customization

✅ **Output Panel Enhancements**
- **AI Insights Integration**: Built-in AI code analysis for Master users
- **Scroll Optimization**: Smooth scrolling for large outputs
- **Error Highlighting**: Visual distinction between stdout and stderr
- **Execution Metadata**: Timing, tier information, and limits display
- **Interactive Elements**: Clickable links and formatted output

### Anonymous User Experience

✅ **Full Code Editor Access**
- Complete Monaco Editor with syntax highlighting
- All supported programming languages
- Code sharing and loading capabilities
- Keyboard shortcuts (Ctrl+Enter, Ctrl+S)

### Anonymous User Experience

✅ **Full Code Editor Access**
- Complete Monaco Editor with syntax highlighting
- All supported programming languages
- Code sharing and loading capabilities
- Keyboard shortcuts (Ctrl+Enter, Ctrl+S)
- Real-time error detection (optional)

✅ **Basic Code Execution**
- **5 executions per hour** (IP-based tracking)
- **60-second compilation delay** (promotes sign-up)
- **All language support** with full feature set
- **Rate limit information** displayed in output panel

✅ **No Registration Required**
- Instant access to the platform
- Save and share code snippets
- Load shared snippets from others
- Theme and editor customization

⚠️ **Limitations for Anonymous Users**
- Limited to 5 executions per hour
- 60-second delay before each execution
- IP-based rate limiting (shared on networks)
- No personal snippet management
- No usage analytics or history
- No AI Insights access

### Package Installation System

✅ **Dual-Mode Package Management**
- **Predefined Packages (All Users)**: Curated list of popular packages for each language
- **Custom Commands (Advanced/Master)**: Sandboxed execution of custom installation commands
- **Tier-Based Access**: Anonymous and Basic users get predefined packages only
- **Security First**: No arbitrary command execution for lower-tier users

**Predefined Package Mode:**
- **Zero Security Risk**: Only vetted, popular packages available
- **Educational**: Shows actual installation commands for learning
- **Multi-Language Support**: Categorized packages for all supported languages
- **Safe Simulation**: Demonstrates package installation without security concerns

**Custom Commands Mode (Advanced/Master Tier):**
- **Sandboxed Execution**: Commands run in isolated Docker containers
- **Resource Limits**: 30-second timeout, 256MB memory limit
- **Network Restrictions**: Only package repository access allowed
- **Fresh Environment**: Each session gets a clean container
- **Command Filtering**: Dangerous commands and shell operators blocked

**Available Package Categories:**
- **Python**: Data Science (numpy, pandas, matplotlib, scikit-learn), Web Development (requests, flask, fastapi), Utilities (pillow, tqdm)
- **JavaScript/TypeScript**: Utilities (lodash, moment, axios), Data Processing (csv-parser, json2csv)
- **Java**: Popular Libraries (gson, apache-commons-lang, guava) with Maven dependency info
- **Ruby**: Popular Gems (json, nokogiri, httparty)
- **Go**: Popular Packages (gorilla/mux, gin-gonic/gin) with go mod commands
- **Rust**: Popular Crates (serde, tokio, reqwest) with Cargo.toml snippets
- **R**: Data Science packages (ggplot2, dplyr, tidyr)

The dual-mode approach provides safe package access for all users while offering advanced capabilities for paying subscribers, balancing security with flexibility.
- IP-based rate limiting (shared on networks)
- No personal snippet management
- No usage analytics or history

### User Authentication & Tier System

✅ **Multi-Tier Access Control**
- **Anonymous Users**: Basic access with limitations (5 executions/hour, 60s delay)
- **Basic Tier**: Enhanced limits and standard features
- **Advanced Tier**: Package installation, reduced delays, higher limits
- **Master Tier**: AI Insights, custom commands, premium features
- **JWT Authentication**: Secure token-based user sessions

✅ **User Management**
- User registration and login system
- Profile management and preferences
- Secure password handling with encryption
- API key management for external integrations
- Usage analytics and tier-based restrictions

✅ **Authentication Features**
- JWT token generation and validation
- Refresh token mechanism for long sessions
- Secure logout and session management
- Role-based access control (RBAC)
- API endpoint protection based on user tier

### AI-Powered Code Analysis

✅ **AI Insights (Premium Feature)**
- **Google Gemini Integration**: Latest gemini-2.0-flash model
- **Comprehensive Code Review**: Security, performance, and quality analysis
- **User API Keys**: Option to use personal Gemini API keys
- **Detailed Reports**: In-depth analysis with specific recommendations
- **Multi-language Support**: AI analysis for all supported programming languages
- **Tier Restriction**: Available for Master tier users only

✅ **AI Analysis Features**
- **Security Review**: Identifies potential security vulnerabilities
- **Code Quality**: Best practices and optimization suggestions
- **Performance Analysis**: Performance bottlenecks and improvements
- **Bug Detection**: Potential runtime errors and logic issues
- **Documentation**: Code readability and documentation suggestions

### Real-time Error Detection

✅ **Advanced Error Highlighting**
- **Language-Specific Analysis**: Tailored error detection per language
- **Block vs Line Languages**: Different strategies for compiled vs interpreted languages
- **Real-time Feedback**: Instant error highlighting as you type
- **Context-Aware**: Smart bracket matching and syntax validation
- **Reduced False Positives**: Improved accuracy with context analysis

✅ **Error Detection Features**
- **Bracket Analysis**: Global bracket matching for block-based languages
- **Semicolon Checking**: Context-aware semicolon validation
- **String Literal Handling**: Proper handling of quotes and escape sequences
- **Comment Awareness**: Ignores syntax errors within comments
- **Settings Toggle**: User can enable/disable real-time error checking

## Supported Languages

| Language   | Version | Compiler/Runtime | Features |
|------------|---------|------------------|----------|
| Python     | 3.11    | CPython         | Standard library, pip packages |
| Java       | 17      | OpenJDK         | Full JDK, Maven dependencies |
| C++        | C++17   | GCC 11          | STL, common libraries |
| C          | C18     | GCC 11          | Standard library |
| JavaScript | ES2022  | Node.js 18      | NPM packages, built-ins |
| TypeScript | 5.x     | tsc + Node.js   | Full TypeScript support |
| Go         | 1.21    | Go compiler     | Standard library, modules |
| Rust       | 1.70    | rustc + Cargo   | Standard library, crates |
| Ruby       | 3.2     | MRI Ruby        | Standard gems |
| R          | 4.3     | R interpreter   | CRAN packages |
| C#         | .NET 7  | dotnet runtime  | .NET libraries |

## Setup & Installation

### Prerequisites

- **Java 17+** (OpenJDK recommended)
- **Node.js 18+** (LTS version)
- **Docker Engine** (for code execution)
- **Git** (for version control)
- **PostgreSQL** (for production) or H2 (for development)

### Quick Start

**Automated Setup (Recommended):**

For Windows:
```cmd
setup.bat
```

For Linux/macOS:
```bash
chmod +x setup.sh
./setup.sh
```

**Manual Setup:**

1. **Clone the repository:**
```bash
git clone https://github.com/yourusername/online-compiler.git
cd online-compiler
```

2. **Backend setup:**
```bash
cd backend
cp .env.example .env
# Edit .env with your configuration
./mvnw spring-boot:run
```

3. **Frontend setup:**
```bash
cd frontend
cp .env.example .env
# Edit .env with your configuration
npm install
npm run dev
```

4. **Docker images setup:**
```bash
cd docker
./build.sh  # Linux/macOS
# or
build.bat   # Windows
```

### Environment Configuration

**Backend (.env):**
```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=compiler_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your-256-bit-secret
JWT_EXPIRATION=86400000

# Docker
DOCKER_ENABLED=true
EXECUTION_TIMEOUT=30000
MAX_OUTPUT_SIZE=10240

# Rate Limiting
RATE_LIMIT_ENABLED=true
REQUESTS_PER_MINUTE=10
```

**Frontend (.env):**
```properties
# API Configuration
VITE_API_URL=http://localhost:8080

# Application
VITE_APP_NAME=Online Compiler
VITE_APP_VERSION=1.0.0

# Features
VITE_ENABLE_ANALYTICS=false
VITE_DEFAULT_THEME=dark
```

## Development

### Project Structure

```
online-compiler/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/com/example/compiler/
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Data repositories
│   │   ├── config/          # Configuration classes
│   │   └── security/        # Security components
│   ├── src/main/resources/
│   │   ├── application.yml  # Spring configuration
│   │   └── docker/          # Dockerfile templates
│   └── pom.xml             # Maven dependencies
├── frontend/               # Vue.js frontend
│   ├── src/
│   │   ├── components/     # Vue components
│   │   ├── views/          # Page components
│   │   ├── services/       # API services
│   │   ├── router/         # Vue Router config
│   │   └── assets/         # Static assets
│   ├── public/             # Public assets
│   └── package.json        # NPM dependencies
├── docker/                 # Docker configurations
│   ├── images/             # Language-specific Dockerfiles
│   ├── docker-compose.yml  # Development stack
│   └── build.sh           # Image build script
└── docs/                   # Additional documentation
```

### Development Workflow

1. **Feature Development:**
   - Create feature branch from main
   - Implement changes in backend/frontend
   - Write tests for new functionality
   - Update documentation as needed

2. **Testing:**
   - Backend: `./mvnw test`
   - Frontend: `npm run test`
   - Integration: `npm run test:e2e`

3. **Building:**
   - Backend: `./mvnw clean package`
   - Frontend: `npm run build`
   - Docker: `docker build -t online-compiler .`

## API Documentation

### Authentication

The API supports both authenticated and anonymous access:

- **Anonymous**: Limited access for basic code execution (5 requests/hour, 60s delay)
- **Authenticated**: Full access with JWT tokens (tier-based limits and features)

### Core Endpoints

#### Code Execution

**Execute Code (Anonymous & Authenticated)**
```http
POST /api/execute
Content-Type: application/json
Authorization: Bearer <token> (optional)

{
  "code": "print('Hello, World!')",
  "language": "python",
  "input": ""
}
```

**Response:**
```json
{
  "output": "Hello, World!\n",
  "error": "",
  "executionTime": 120,
  "success": true,
  "metadata": {
    "tier": "ANONYMOUS",
    "tierDescription": "Anonymous access - Sign up for higher limits",
    "remainingRequests": 4,
    "authenticated": false
  }
}
```

**Rate Limit Response (429):**
```json
{
  "error": "Rate limit exceeded for anonymous users. Please wait or sign up for higher limits. Remaining requests this hour: 0",
  "success": false
}
```

#### AI Insights (Master Tier Only)

**Get AI Code Analysis**
```http
POST /api/ai/analyze
Content-Type: application/json
Authorization: Bearer <token> (required)

{
  "code": "def factorial(n):\n    return n * factorial(n-1)",
  "language": "python"
}
```

**Response:**
```json
{
  "insights": "**Security Analysis:**\n- Risk of stack overflow for large inputs\n\n**Performance:**\n- Consider iterative approach for better performance\n\n**Code Quality:**\n- Missing base case for recursion",
  "success": true,
  "model": "gemini-2.0-flash",
  "analysisTime": 850
}
```

#### Authentication

**User Registration**
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**User Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "john_doe",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "tier": "BASIC"
  }
}
```

#### Snippet Management

**Save Snippet**
```http
POST /api/snippets
Content-Type: application/json
Authorization: Bearer <token>

{
  "title": "Hello World Example",
  "code": "print('Hello, World!')",
  "language": "python",
  "isPublic": true
}
```

**Get Snippet**
```http
GET /api/snippets/{id}
```

**Share Snippet**
```http
POST /api/snippets/{id}/share
```

### Rate Limiting

#### Tier-Based Limits

| Tier | Requests/Hour | Execution Delay | Features |
|------|---------------|-----------------|----------|
| **Anonymous** | 5 | 60 seconds | Basic execution, sharing |
| **Basic** | 10 | 30 seconds | Enhanced limits, user profile |
| **Advanced** | 50 | 15 seconds | Package installation, faster execution |
| **Master** | 200 | Instant | AI Insights, custom commands, premium |

#### Implementation Details

- **IP-based tracking** for anonymous users
- **User-based tracking** for authenticated users  
- **Rolling window** rate limiting (hourly reset)
- **Graceful degradation** with informative error messages
- **Tier upgrade prompts** when limits are reached

## Security

### Container Security

- **Isolation**: Each execution runs in a separate Docker container
- **Network**: Containers have no network access (`--network=none`)
- **Resources**: CPU and memory limits enforced
- **User**: Code runs as non-root user
- **Filesystem**: Read-only root filesystem with writable tmp

### Application Security

- **Input Validation**: All user inputs are validated and sanitized
- **SQL Injection**: Parameterized queries with JPA and Spring Data
- **XSS Protection**: Output encoding on frontend and CSP headers
- **CSRF**: CSRF tokens for state-changing operations  
- **JWT Security**: Secure token generation and validation with configurable expiration
- **API Rate Limiting**: Tier-based rate limits to prevent abuse
- **Password Security**: Bcrypt hashing with salt for password storage
- **API Key Management**: Secure storage and validation of user API keys (Gemini)
- **CORS Configuration**: Properly configured cross-origin resource sharing
- **Session Management**: Stateless JWT tokens with refresh mechanism

### AI Security

- **API Key Isolation**: User API keys stored securely and used only for their requests
- **Model Validation**: Only approved AI models allowed (gemini-2.0-flash)
- **Input Sanitization**: Code inputs sanitized before AI analysis
- **Rate Limiting**: AI requests subject to tier-based limits
- **Error Handling**: Secure error messages without exposing internal details
- **Timeout Protection**: AI requests timeout to prevent resource exhaustion

### Error Detection Security

- **Client-side Only**: Error detection runs entirely in browser
- **No Data Transmission**: Code analysis never sent to external servers
- **Safe Parsing**: Robust parsing that handles malformed code safely
- **Performance Bounds**: Error detection limited to prevent UI blocking

### Resource Limits

```yaml
Execution Limits:
  - Max execution time: 30 seconds
  - Max memory usage: 128MB
  - Max CPU usage: 1 core
  - Max output size: 10KB
  - No network access
  - No file system writes (except /tmp)
```

## Deployment

### Production Deployment

#### Using Docker Compose

```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  backend:
    build: ./backend
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    ports:
      - "8080:8080"
    depends_on:
      - database
  
  frontend:
    build: ./frontend
    ports:
      - "80:80"
  
  database:
    image: postgres:15
    environment:
      POSTGRES_DB: compiler_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

#### Cloud Deployment Options

**Recommended Platforms:**
- **Backend**: Render, Railway, or AWS ECS
- **Frontend**: Vercel, Netlify, or Cloudflare Pages
- **Database**: PostgreSQL on AWS RDS, Google Cloud SQL

**Environment Variables for Production:**
```bash
# Backend
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=postgresql://...
JWT_SECRET=your-production-secret
DOCKER_HOST=unix:///var/run/docker.sock

# Frontend
VITE_API_URL=https://your-api-domain.com
VITE_ENABLE_ANALYTICS=true
```

### Monitoring & Logging

- **Application Logs**: Structured logging with Logback
- **Performance**: Micrometer metrics with Prometheus
- **Health Checks**: Spring Boot Actuator endpoints
- **Error Tracking**: Integration with Sentry or similar

## Configuration

### Backend Configuration

**Application Properties:**
```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: jdbc:postgresql://localhost:5432/compiler_db
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  security:
    jwt:
      secret: ${JWT_SECRET:default-secret}
      expiration: 86400000

compiler:
  docker:
    enabled: true
    timeout: 30000
    max-output-size: 10240
  ai:
    gemini:
      api-key: ${GEMINI_API_KEY:}
      model: "gemini-2.0-flash"
      timeout: 30000
  rate-limit:
    enabled: true
    anonymous:
      requests-per-hour: 5
      delay-seconds: 60
    basic:
      requests-per-hour: 10
      delay-seconds: 30
    advanced:
      requests-per-hour: 50
      delay-seconds: 15
    master:
      requests-per-hour: 200
      delay-seconds: 0
  tiers:
    default: ANONYMOUS
    features:
      ai-insights: [MASTER]
      custom-packages: [ADVANCED, MASTER]
      unlimited-sharing: [ADVANCED, MASTER]
```

### Frontend Configuration

**Vite Configuration:**
```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router'],
          editor: ['monaco-editor']
        }
      }
    }
  }
})
```

## Contributing

### Development Guidelines

1. **Code Style:**
   - Backend: Google Java Style Guide
   - Frontend: ESLint + Prettier configuration
   - Use meaningful commit messages

2. **Testing:**
   - Write unit tests for all business logic
   - Integration tests for API endpoints
   - E2E tests for critical user flows

3. **Documentation:**
   - Update API documentation for new endpoints
   - Add JSDoc comments for complex functions
   - Update this documentation for major changes

### Pull Request Process

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write/update tests
5. Update documentation
6. Submit pull request
7. Address code review feedback

### Reporting Issues

When reporting bugs, please include:
- Steps to reproduce
- Expected vs actual behavior
- Browser/OS information
- Error messages or logs
- Code snippets if applicable

---

## License

MIT License - see LICENSE file for details.

## Support

- **Documentation**: This document and API docs
- **Issues**: GitHub Issues for bug reports
- **Discussions**: GitHub Discussions for questions
- **Email**: support@compiler-platform.com
