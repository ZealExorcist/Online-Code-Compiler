# Online Compiler Application

A web-based code compiler and executor that supports multiple programming languages with secure Docker-based sandboxing.

## Architecture

- **Frontend**: Vue.js 3 with Monaco Editor for code editing
- **Backend**: Spring Boot Java REST API
- **Execution Environment**: Docker containers for secure code execution
- **Sharing**: Unique snippet IDs for code sharing without authentication

## Supported Languages

- Python
- C/C++
- Java
- JavaScript
- Go
- Ruby
- R
- Rust
- C#
- HTML/CSS (preview mode)

## Features

- Real-time code editing with syntax highlighting
- Secure code execution in isolated Docker containers
- Code sharing via unique URLs
- Multiple language support
- Resource limiting and security hardening

## Development Setup

### Prerequisites

- JDK 17+
- Node.js (LTS)
- Docker
- Git

### Local Development

1. **Backend**: 
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

2. **Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Deployment

- **Frontend**: Deployed on Vercel
- **Backend**: Deployed on Render with Docker support

## Security

- Docker containers run with no network access
- Resource limits (CPU, memory, execution time)
- Non-root user execution
- Input validation and sanitization
- Rate limiting on API endpoints

## License

MIT
