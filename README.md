# Online Compiler

A secure, multi-language online code compiler with Docker-based sandboxing.

## Quick Start

### Automated Setup (Recommended)

**Windows:**
```cmd
setup.bat
```

**Linux/macOS:**
```bash
chmod +x setup.sh
./setup.sh
```

### Manual Setup

```bash
# Backend
cd backend
./mvnw spring-boot:run

# Frontend  
cd frontend
npm install
npm run dev
```

For detailed setup instructions, see [SETUP.md](SETUP.md)

## Features

- 11+ programming languages supported
- Secure Docker container execution
- Real-time code editing with Monaco Editor
- Code sharing via unique URLs
- RESTful API with JWT authentication
- Modern Vue.js frontend

## Documentation

📖 **[Complete Documentation](https://zealexorcist.gitbook.io/zealexorcist-blog/project-docs/online-code-compiler)** - Comprehensive guide including:
- Architecture overview
- Setup & installation
- API documentation  
- Security details
- Deployment guide

## Quick Links

- **Features**: [features.txt](features.txt) - Detailed feature list
- **Tasks**: [tasks.md](tasks.md) - Development task breakdown
- **Architecture**: [main.md](main.md) - Technical architecture details

## Supported Languages

Python • Java • C/C++ • JavaScript • TypeScript • Go • Rust • Ruby • R • C#

## License

MIT
