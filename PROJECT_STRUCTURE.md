# Project Structure

```
online-code-compiler/
├── README.md                 # Main project documentation
├── CONTRIBUTING.md           # Contribution guidelines
├── LICENSE                   # MIT License
├── .gitignore               # Git ignore rules
├── setup.bat                # Windows setup script
├── setup.sh                 # Linux/Mac setup script
├── SETUP.md                 # Detailed setup instructions
├── doc.md                   # Comprehensive documentation
├── tasks.md                 # Development tasks
├── main.md                  # Architecture details
├── features.txt             # Feature list
│
├── frontend/                # Vue.js Frontend Application
│   ├── package.json         # Node.js dependencies
│   ├── vite.config.ts       # Vite configuration
│   ├── tsconfig.json        # TypeScript configuration
│   ├── index.html          # Main HTML template
│   ├── .env.example        # Environment variables template
│   ├── src/
│   │   ├── main.ts         # Application entry point
│   │   ├── App.vue         # Root Vue component
│   │   ├── components/     # Reusable Vue components
│   │   │   ├── Header.vue
│   │   │   ├── Footer.vue
│   │   │   ├── CodeEditor.vue
│   │   │   ├── CommandsComponent.vue
│   │   │   ├── CompilerInterface.vue
│   │   │   ├── LoadComponent.vue
│   │   │   ├── OutputPanel.vue
│   │   │   ├── SettingsComponent.vue
│   │   │   ├── ShareComponent.vue
│   │   │   └── UpgradeComponent.vue
│   │   ├── views/          # Page-level components
│   │   │   ├── Home.vue
│   │   │   ├── AuthPage.vue
│   │   │   ├── SettingsPage.vue
│   │   │   └── Snippet.vue
│   │   ├── services/       # API and utility services
│   │   │   ├── api.ts      # Main API service
│   │   │   ├── auth.ts     # Authentication service
│   │   │   └── settings.ts # Settings service
│   │   ├── router/         # Vue Router configuration
│   │   │   └── index.ts
│   │   └── assets/         # Static assets and styles
│   │       ├── main.css
│   │       ├── base.css
│   │       └── logo.svg
│   └── public/             # Public static files
│       └── favicon.ico
│
├── backend/                 # Spring Boot Backend Application
│   ├── pom.xml             # Maven dependencies
│   ├── mvnw                # Maven wrapper (Linux/Mac)
│   ├── mvnw.cmd            # Maven wrapper (Windows)
│   ├── .env.example        # Environment variables template
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/compiler/
│   │       │   ├── CompilerApplication.java    # Main application class
│   │       │   ├── controller/                 # REST API controllers
│   │       │   │   ├── ExecuteController.java
│   │       │   │   ├── ShareController.java
│   │       │   │   └── SnippetController.java
│   │       │   ├── service/                    # Business logic services
│   │       │   │   ├── ExecutionService.java
│   │       │   │   ├── RateLimitService.java
│   │       │   │   └── UserService.java
│   │       │   ├── model/                      # Data models
│   │       │   │   ├── User.java
│   │       │   │   ├── Snippet.java
│   │       │   │   ├── ExecuteRequest.java
│   │       │   │   ├── ExecuteResponse.java
│   │       │   │   └── UserTier.java
│   │       │   ├── config/                     # Configuration classes
│   │       │   │   ├── SecurityConfig.java
│   │       │   │   ├── CorsConfig.java
│   │       │   │   └── DataInitializer.java
│   │       │   └── repository/                 # Data access layer
│   │       │       ├── UserRepository.java
│   │       │       └── SnippetRepository.java
│   │       └── resources/
│   │           ├── application.yml             # Main configuration
│   │           ├── application-dev.yml         # Development config
│   │           └── application-prod.yml        # Production config
│   └── target/             # Maven build output (ignored by git)
│
└── docker/                 # Docker Configuration
    ├── docker-compose.yml  # Multi-container orchestration
    ├── build.bat          # Windows build script
    ├── build.sh           # Linux/Mac build script
    ├── test.go            # Test file for Go container
    ├── README.md          # Docker setup instructions
    └── images/            # Language-specific Dockerfiles
        ├── Dockerfile.python
        ├── Dockerfile.java
        ├── Dockerfile.cpp
        ├── Dockerfile.javascript
        ├── Dockerfile.typescript
        ├── Dockerfile.go
        ├── Dockerfile.rust
        ├── Dockerfile.ruby
        ├── Dockerfile.r
        └── Dockerfile.csharp
```

## Key Features

### Frontend Features
- **Modern Vue.js 3** with Composition API and TypeScript
- **Monaco Editor** for advanced code editing with syntax highlighting
- **Responsive Design** that works on desktop and mobile
- **Dark/Light Theme** support with user preference persistence
- **Real-time Code Execution** with live output display
- **User Authentication** with JWT tokens and API keys
- **Code Snippet Management** - save, share, and load code snippets
- **Package Installation** - predefined packages for all users, custom commands for advanced users
- **Settings Management** - customizable editor preferences

### Backend Features
- **Spring Boot 3** with modern Java enterprise features
- **Secure Authentication** using JWT tokens and Spring Security
- **MongoDB Integration** for user data and code snippet storage
- **Docker-based Code Execution** in isolated containers
- **Rate Limiting** to prevent abuse from anonymous and authenticated users
- **RESTful API Design** with proper HTTP status codes and error handling
- **User Tier System** (Anonymous, Basic, Advanced, Master) with different privileges
- **CORS Configuration** for secure cross-origin requests

### DevOps Features
- **Docker Containerization** for secure code execution
- **Multi-environment Configuration** (development, production)
- **Automated Setup Scripts** for quick development environment setup
- **Comprehensive Documentation** with setup guides and API documentation
- **Security Best Practices** with input validation and sandboxed execution

## Technology Stack

- **Frontend**: Vue.js 3, TypeScript, Vite, Monaco Editor, Axios
- **Backend**: Spring Boot 3, Spring Security, MongoDB, Maven
- **Infrastructure**: Docker, MongoDB Atlas
- **Development**: Git, npm, Maven, Environment configuration

## Supported Programming Languages

1. **Python** - Full Python 3 support with pip package management
2. **Java** - OpenJDK with Maven dependency management
3. **JavaScript** - Node.js runtime environment
4. **TypeScript** - TypeScript compiler with Node.js
5. **C++** - GCC compiler with standard library
6. **C** - GCC compiler for C programs
7. **Go** - Official Go compiler and runtime
8. **Rust** - Rust compiler with Cargo package manager
9. **Ruby** - Ruby interpreter with gem support
10. **R** - R statistical computing environment
11. **C#** - .NET Core runtime environment

Each language runs in its own secure Docker container with appropriate tooling and package managers installed.
