# Online Compiler - Enhanced Security & User Management

A secure, multi-language online code compiler with Docker-based execution, user management, and comprehensive security features.

## 🚀 New Features (Phase 6 Complete)

### 🔐 Authentication & Security
- **JWT + API Key Authentication**: Dual authentication system for maximum flexibility
- **User Registration/Login**: Secure user account management
- **Rate Limiting**: Configurable request limits to prevent abuse
- **Password Security**: BCrypt hashing with secure policies
- **Role-based Access**: User and admin roles with proper permissions

### 👤 User Management
- **MongoDB Integration**: Scalable user data storage
- **User Profiles**: Complete user information management
- **API Key Management**: Generate and refresh API keys for programmatic access
- **Account Settings**: Password changes and profile management

### ⚙️ Comprehensive Settings
- **Editor Preferences**: Theme, font, language defaults, and editor behavior
- **Execution Settings**: Timeout limits, output size controls
- **Privacy Controls**: Public snippets and sharing preferences
- **Real-time Updates**: Settings applied instantly across the interface

### 🎨 Enhanced UI/UX
- **Modern Authentication Pages**: Beautiful login/registration interface
- **Settings Dashboard**: Intuitive settings management with tabbed interface
- **User Menu**: Header integration with user status and quick actions
- **Logo Support**: Customizable branding capabilities
- **Mobile Responsive**: Optimized for all device sizes

### 🔒 Advanced Security
- **Container Isolation**: Docker-based code execution with security constraints
- **Input Validation**: Comprehensive validation and sanitization
- **CORS Configuration**: Proper cross-origin resource sharing setup
- **Security Headers**: Enhanced HTTP security headers
- **Guest Mode**: Limited functionality for unauthenticated users

## 🛠️ Technology Stack

### Backend
- **Java 17** with Spring Boot 3.x
- **MongoDB** for user data and settings
- **H2 Database** for backward compatibility
- **Spring Security** for authentication and authorization
- **JWT** for secure token-based authentication
- **Bucket4j** for advanced rate limiting
- **Docker** for secure code execution

### Frontend
- **Vue.js 3** with Composition API
- **CodeMirror 6** for advanced code editing
- **Axios** for HTTP requests with auth interceptors
- **Vue Router** with authentication guards
- **Responsive CSS** for mobile compatibility

## 🏃‍♂️ Quick Start

### Prerequisites
- Java 17+
- Node.js 16+
- Docker
- MongoDB (optional - H2 fallback available)

### Backend Setup
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

### Environment Variables
```bash
# Backend (.env or application.yml)
MONGODB_URI=mongodb://localhost:27017/online-compiler
JWT_SECRET=your-secret-key-here
RATE_LIMIT_REQUESTS_PER_MINUTE=10
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123

# Frontend (.env)
VITE_API_URL=http://localhost:8080
```

## 🔑 Authentication

### Using JWT Tokens
```javascript
// Login and get token
const response = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'user', password: 'pass' })
});
const { token } = await response.json();

// Use token in requests
fetch('/api/execute', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

### Using API Keys
```bash
# Get your API key from settings page, then:
curl -X POST http://localhost:8080/api/execute \
  -H "X-API-Key: oc_your-api-key-here" \
  -H "Content-Type: application/json" \
  -d '{"code": "print(\"Hello World\")", "language": "python"}'
```

## 🎛️ User Settings

Users can customize:

### Editor Settings
- **Theme**: Dark/Light modes
- **Color Scheme**: Multiple syntax highlighting themes
- **Font**: Size, family, and spacing preferences
- **Behavior**: Auto-complete, line numbers, folding, word wrap
- **Default Language**: Preferred programming language

### Execution Settings
- **Timeout**: Maximum execution time (5-60 seconds)
- **Output Limit**: Maximum output size (1KB-100KB)
- **Input Support**: Enable/disable stdin input

### Privacy Settings
- **Public Snippets**: Make snippets discoverable
- **Auto-sharing**: Generate share links by default

## 🚦 Rate Limiting

- **Default**: 10 requests per minute per user/IP
- **Burst Capacity**: 20 requests for peak usage
- **Headers**: `X-Rate-Limit-Remaining` and `X-Rate-Limit-Burst` in responses
- **Configurable**: Adjust limits via environment variables

## 🔐 Security Features

### Container Security
- Non-root user execution
- Network isolation (`--net=none`)
- Resource limits (CPU, memory, time)
- Read-only filesystem where possible
- Minimal base images

### API Security
- JWT token validation
- API key authentication
- Input length limits
- SQL injection prevention
- XSS protection
- CORS configuration

### User Security
- BCrypt password hashing
- Secure session management
- Rate limiting per user
- Audit logging

## 🎯 Supported Languages

- **Python** (3.11+)
- **JavaScript** (Node.js 18+)
- **Java** (OpenJDK 17+)
- **C/C++** (GCC latest)
- **Go** (1.20+)
- **Rust** (latest stable)
- **Ruby** (3.0+)
- **R** (latest)
- **C#** (.NET 6+)
- **TypeScript** (via Node.js)

## 📱 Mobile Support

The interface is fully responsive and optimized for:
- Mobile phones (320px+)
- Tablets (768px+)
- Desktops (1024px+)
- Large screens (1440px+)

## 🚀 Deployment

### Production Environment Variables
```bash
# Backend
MONGODB_URI=mongodb://your-production-db/online-compiler
JWT_SECRET=your-production-secret-key
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
ADMIN_PASSWORD=your-secure-admin-password

# Frontend
VITE_API_URL=https://your-backend-domain.com
```

### Docker Deployment
```bash
# Backend
docker build -t online-compiler-backend ./backend
docker run -p 8080:8080 online-compiler-backend

# Frontend
docker build -t online-compiler-frontend ./frontend
docker run -p 3000:3000 online-compiler-frontend
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

- **Issues**: GitHub Issues
- **Documentation**: See `/docs` folder
- **API Reference**: Available at `/api/docs` when running

## 🔄 Changelog

### v2.0.0 - Phase 6 Complete
- ✅ User authentication and management
- ✅ MongoDB integration
- ✅ Advanced settings system
- ✅ Enhanced security features
- ✅ Rate limiting
- ✅ Modern UI/UX improvements
- ✅ API key authentication
- ✅ Mobile responsiveness

### v1.0.0 - Initial Release
- ✅ Multi-language code execution
- ✅ Docker-based sandboxing
- ✅ Code sharing functionality
- ✅ Basic security measures
