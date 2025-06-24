# Online Compiler Platform

A secure, production-ready multi-language online code compiler with advanced features, user authentication, AI-powered insights, and Docker-based sandboxing.

## 🚀 Quick Start

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

## ✨ Key Features

### 🔐 **User Authentication & Tiers**
- **Anonymous Access**: 5 executions/hour, 60s delay
- **Basic Users**: Enhanced limits and features
- **Advanced Users**: Package installation, faster execution
- **Master Users**: AI Insights, custom commands, premium features
- JWT-based authentication with secure token management

### 💻 **Advanced Code Editor**
- **Monaco Editor**: VS Code-like experience in the browser
- **Real-time Error Detection**: Smart syntax checking for all languages
- **Language Support**: 11+ programming languages with full syntax highlighting
- **Code Intelligence**: Auto-completion, bracket matching, code folding
- **Customizable**: Multiple themes, adjustable font size, tab preferences
- **Keyboard Shortcuts**: Ctrl+Enter to run, Ctrl+S to save

### 🐳 **Secure Execution Environment**
- **Docker Sandboxing**: Isolated containers for each execution
- **Resource Limits**: CPU, memory, and time constraints
- **Network Isolation**: No external network access
- **Security Hardened**: Non-root execution, read-only filesystems

### 📦 **Package Management System**
- **Predefined Packages**: Curated libraries for all users
- **Custom Commands**: Advanced/Master users can install any packages
- **Educational Mode**: Shows actual installation commands
- **Security-First**: Sandboxed execution for custom installations

### 🤖 **AI Insights (Premium Feature)**
- **Code Analysis**: AI-powered security and quality reviews
- **Gemini Integration**: Google's latest AI model (gemini-2.0-flash)
- **User API Keys**: Bring your own Gemini API key option
- **Detailed Reports**: Comprehensive code review with suggestions

### � **Code Sharing & Collaboration**
- **Instant Sharing**: Generate shareable URLs with one click
- **No Registration Required**: Share code anonymously
- **Save to Local**: Download code files locally
- **Tier-based Limits**: Advanced sharing features for subscribers

### 🎨 **Modern UI/UX**
- **Responsive Design**: Works on all devices and screen sizes
- **Dark/Light Themes**: Multiple theme options
- **Smooth Interactions**: Enhanced scrolling and user experience
- **Real-time Feedback**: Live error highlighting and output streaming

## 🛠 Supported Languages

| Language | Version | Features |
|----------|---------|----------|
| **Python** | 3.11 | Full stdlib + pip packages |
| **Java** | 17 | Complete JDK + Maven deps |
| **C/C++** | C18/C++17 | GCC 11 + standard libraries |
| **JavaScript** | ES2022 | Node.js 18 + npm packages |
| **TypeScript** | 5.x | Full TS support + compilation |
| **Go** | 1.21 | Standard library + modules |
| **Rust** | 1.70 | rustc + Cargo crates |
| **Ruby** | 3.2 | MRI + standard gems |
| **R** | 4.3 | CRAN packages |
| **C#** | .NET 7 | .NET libraries |

## 📚 Documentation

📖 **[Complete Technical Documentation](doc.md)** - Comprehensive guide including:
- Detailed architecture overview
- Setup & installation instructions
- Complete API documentation  
- Security implementation details
- Deployment and production guides
- Configuration and customization

## 🏗 Architecture

```
Browser (Vue.js SPA)
   │
   ▼ (REST API calls)
┌─────────────────────────┐
│  Spring Boot Backend    │ ──► Docker Containers ──► Code Execution
│  (Java 17+)            │     (Isolated Sandboxes)    (Compile/Run)
├─────────────────────────┤
│  PostgreSQL Database    │
│  (User data, snippets)  │
└─────────────────────────┘
```

### Technology Stack

**Frontend:** Vue.js 3 + TypeScript + Monaco Editor + Vite  
**Backend:** Spring Boot 3 + Spring Security + JWT + JPA  
**Database:** PostgreSQL (production) / H2 (development)  
**Execution:** Docker containers with security hardening  
**AI Integration:** Google Gemini API for code analysis  

## 🔒 Security Features

- **Container Isolation**: Each execution in separate Docker container
- **Resource Limits**: CPU, memory, and execution time constraints
- **Network Security**: No external network access for code execution
- **Input Validation**: All user inputs sanitized and validated
- **Rate Limiting**: API rate limits to prevent abuse
- **JWT Security**: Secure token-based authentication
- **CORS Protection**: Properly configured cross-origin policies

## 🚀 Deployment

### Development
```bash
# Start backend
cd backend && ./mvnw spring-boot:run

# Start frontend (separate terminal)
cd frontend && npm run dev
```

### Production
- **Frontend**: Deploy to Vercel, Netlify, or Cloudflare Pages
- **Backend**: Deploy to Render, Railway, or AWS ECS
- **Database**: PostgreSQL on AWS RDS or Google Cloud SQL
- **Monitoring**: Built-in health checks and metrics

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## 📧 Support

- **Documentation**: [doc.md](doc.md) for technical details
- **Issues**: GitHub Issues for bug reports and feature requests
- **Security**: Report security issues privately
