# Online Compiler API Documentation & Developer Guide

> **A comprehensive guide to integrating with the Online Compiler REST API**

[![API Version](https://img.shields.io/badge/API-v1.0-blue)](https://api.compiler.example.com)
[![Authentication](https://img.shields.io/badge/Auth-JWT-green)](https://jwt.io)
[![Rate Limit](https://img.shields.io/badge/Rate%20Limit-Tiered-orange)](https://developer.compiler.example.com/rate-limits)

## 🚀 Getting Started

The Online Compiler API provides programmatic access to our secure, multi-language code execution platform. Execute code in 10+ programming languages with enterprise-grade security and reliability.

### Base URL
```
Production: https://api.compiler.example.com
Development: http://localhost:8080
```

### Quick Start Example

```javascript
// Execute Python code
const response = await fetch('https://api.compiler.example.com/api/execute', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_JWT_TOKEN'
  },
  body: JSON.stringify({
    code: 'print("Hello, World!")',
    language: 'python',
    input: ''
  })
});

const result = await response.json();
console.log(result.output); // "Hello, World!"
```

## 🔐 Authentication

The API uses JWT (JSON Web Tokens) for authentication. All protected endpoints require a valid JWT token in the Authorization header.

### Registration

Create a new user account to get API access.

**Endpoint:** `POST /api/auth/register`

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "developer",
  "email": "dev@example.com",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "developer",
  "email": "dev@example.com",
  "apiKey": "oc_1234567890abcdef",
  "expiresIn": 86400000
}
```

**Password Requirements:**
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character (@$!%*?&)

### Login

Authenticate existing users to get access tokens.

**Endpoint:** `POST /api/auth/login`

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "developer",
  "password": "SecurePass123!"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "developer",
  "email": "dev@example.com",
  "apiKey": "oc_1234567890abcdef",
  "expiresIn": 86400000
}
```

### Using API Keys

For server-to-server authentication, you can use API keys instead of JWT tokens:

```http
GET /api/user/profile
X-API-Key: oc_1234567890abcdef
```

### Token Management

JWT tokens expire after 24 hours. Implement token refresh logic in your applications:

```javascript
// Check token expiration
function isTokenExpired(token) {
  const payload = JSON.parse(atob(token.split('.')[1]));
  return Date.now() >= payload.exp * 1000;
}

// Refresh token before expiration
if (isTokenExpired(currentToken)) {
  const newToken = await refreshToken();
  localStorage.setItem('token', newToken);
}
```

## 💻 Code Execution API

Execute code in multiple programming languages with secure sandboxing.

### Execute Code

**Endpoint:** `POST /api/execute`

Execute code in a secure Docker container with resource limits and timeout protection.

```http
POST /api/execute
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "code": "def fibonacci(n):\n    if n <= 1:\n        return n\n    return fibonacci(n-1) + fibonacci(n-2)\n\nprint(fibonacci(10))",
  "language": "python",
  "input": "optional input for stdin"
}
```

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `code` | string | Yes | Source code to execute (max 10KB) |
| `language` | string | Yes | Programming language (see supported languages) |
| `input` | string | No | Input data for stdin (max 1KB) |

**Response:**
```json
{
  "output": "55\n",
  "error": "",
  "exitCode": 0,
  "executionTime": 245,
  "metadata": {
    "tier": "BASIC",
    "tierDescription": "Basic user with 50 executions per hour",
    "remainingRequests": 47
  }
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `output` | string | Standard output from the program |
| `error` | string | Standard error output |
| `exitCode` | number | Program exit code (0 = success) |
| `executionTime` | number | Execution time in milliseconds |
| `metadata` | object | Additional execution metadata |

### Supported Languages

**Endpoint:** `GET /api/languages`

Get a list of all supported programming languages.

```http
GET /api/languages
```

**Response:**
```json
[
  "python",
  "java", 
  "cpp",
  "c",
  "javascript",
  "typescript",
  "go",
  "rust",
  "ruby",
  "r",
  "csharp"
]
```

### Language-Specific Examples

#### Python
```json
{
  "code": "import math\nprint(f'Pi is approximately {math.pi:.2f}')",
  "language": "python"
}
```

#### Java
```json
{
  "code": "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello from Java!\");\n    }\n}",
  "language": "java"
}
```

#### C++
```json
{
  "code": "#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << \"Hello from C++!\" << endl;\n    return 0;\n}",
  "language": "cpp"
}
```

#### JavaScript
```json
{
  "code": "const message = 'Hello from Node.js!';\nconsole.log(message);",
  "language": "javascript"
}
```

#### Go
```json
{
  "code": "package main\n\nimport \"fmt\"\n\nfunc main() {\n    fmt.Println(\"Hello from Go!\")\n}",
  "language": "go"
}
```

## 📄 Snippet Management API

Save, share, and manage code snippets with version control.

### Create Snippet

**Endpoint:** `POST /api/snippets`

Save a code snippet for future reference or sharing.

```http
POST /api/snippets
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "title": "Fibonacci Calculator",
  "code": "def fibonacci(n):\n    if n <= 1:\n        return n\n    return fibonacci(n-1) + fibonacci(n-2)\n\nprint(fibonacci(10))",
  "language": "python",
  "isPublic": true,
  "description": "Recursive implementation of Fibonacci sequence"
}
```

**Response:**
```json
{
  "id": "snippet_abc123def456",
  "title": "Fibonacci Calculator",
  "code": "def fibonacci(n):\n    if n <= 1:\n        return n\n    return fibonacci(n-1) + fibonacci(n-2)\n\nprint(fibonacci(10))",
  "language": "python",
  "isPublic": true,
  "description": "Recursive implementation of Fibonacci sequence",
  "createdAt": "2025-06-21T10:30:00Z",
  "updatedAt": "2025-06-21T10:30:00Z",
  "author": "developer",
  "shareUrl": "https://compiler.example.com/snippet/abc123def456"
}
```

### Load Snippet

**Endpoint:** `GET /api/snippets/{snippetId}`

Load a saved snippet by its unique ID.

```http
GET /api/snippets/abc123def456
```

**Response:**
```json
{
  "id": "snippet_abc123def456",
  "title": "Fibonacci Calculator",
  "code": "def fibonacci(n):\n    if n <= 1:\n        return n\n    return fibonacci(n-1) + fibonacci(n-2)\n\nprint(fibonacci(10))",
  "language": "python",
  "isPublic": true,
  "description": "Recursive implementation of Fibonacci sequence",
  "createdAt": "2025-06-21T10:30:00Z",
  "updatedAt": "2025-06-21T10:30:00Z",
  "author": "developer"
}
```

### List User Snippets

**Endpoint:** `GET /api/snippets`

Get all snippets created by the authenticated user.

```http
GET /api/snippets
Authorization: Bearer {jwt_token}
```

**Query Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | number | Page number (default: 1) |
| `limit` | number | Items per page (default: 20, max: 100) |
| `language` | string | Filter by programming language |
| `isPublic` | boolean | Filter by visibility |

**Response:**
```json
{
  "snippets": [
    {
      "id": "snippet_abc123def456",
      "title": "Fibonacci Calculator",
      "language": "python",
      "isPublic": true,
      "createdAt": "2025-06-21T10:30:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 1,
    "pages": 1
  }
}
```

### Update Snippet

**Endpoint:** `PUT /api/snippets/{snippetId}`

Update an existing snippet (owner only).

```http
PUT /api/snippets/abc123def456
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "title": "Optimized Fibonacci Calculator",
  "code": "def fibonacci(n, memo={}):\n    if n in memo:\n        return memo[n]\n    if n <= 1:\n        return n\n    memo[n] = fibonacci(n-1, memo) + fibonacci(n-2, memo)\n    return memo[n]\n\nprint(fibonacci(10))",
  "description": "Memoized version for better performance"
}
```

### Delete Snippet

**Endpoint:** `DELETE /api/snippets/{snippetId}`

Delete a snippet (owner only).

```http
DELETE /api/snippets/abc123def456
Authorization: Bearer {jwt_token}
```

## 👤 User Management API

Manage user profiles, settings, and API keys.

### Get User Profile

**Endpoint:** `GET /api/user/profile`

Get the authenticated user's profile information.

```http
GET /api/user/profile
Authorization: Bearer {jwt_token}
```

**Response:**
```json
{
  "id": "user_123456789",
  "username": "developer",
  "email": "dev@example.com",
  "tier": "BASIC",
  "apiKey": "oc_1234567890abcdef",
  "createdAt": "2025-06-01T12:00:00Z",
  "lastLoginAt": "2025-06-21T10:00:00Z",
  "settings": {
    "defaultLanguage": "python",
    "theme": "dark",
    "fontSize": 14
  },
  "usage": {
    "executionsThisHour": 3,
    "executionsToday": 15,
    "totalExecutions": 247
  }
}
```

### Update User Settings

**Endpoint:** `PUT /api/user/settings`

Update user preferences and settings.

```http
PUT /api/user/settings
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "defaultLanguage": "javascript",
  "theme": "light",
  "fontSize": 16,
  "notifications": {
    "email": true,
    "browser": false
  }
}
```

### Change Password

**Endpoint:** `PUT /api/user/password`

Update the user's password with validation.

```http
PUT /api/user/password
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "oldPassword": "CurrentPass123!",
  "newPassword": "NewSecurePass456@"
}
```

### Regenerate API Key

**Endpoint:** `POST /api/user/api-key/regenerate`

Generate a new API key (invalidates the old one).

```http
POST /api/user/api-key/regenerate
Authorization: Bearer {jwt_token}
```

**Response:**
```json
{
  "apiKey": "oc_newkey9876543210fedcba",
  "generatedAt": "2025-06-21T11:00:00Z"
}
```

## 🎯 Rate Limiting & User Tiers

The API implements tiered rate limiting based on user accounts.

### Rate Limit Headers

All API responses include rate limit information:

```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 50
X-RateLimit-Remaining: 47
X-RateLimit-Reset: 1624276800
X-RateLimit-Tier: BASIC
```

### User Tiers

| Tier | Executions/Hour | Rate Limit/Min | Features |
|------|----------------|----------------|----------|
| Anonymous | 10 | 10 | Basic execution only |
| Basic | 50 | 20 | User account, save snippets |
| Premium | 200 | 50 | Priority execution, larger files |
| Pro | 1000 | 100 | API access, team features |

### Rate Limit Exceeded Response

```json
{
  "error": "Rate limit exceeded",
  "message": "BASIC tier allows 50 executions per hour. Remaining: 0",
  "retryAfter": 3600,
  "tier": "BASIC"
}
```

## ⚠️ Error Handling

The API uses standard HTTP status codes and returns detailed error information.

### Error Response Format

```json
{
  "error": "Validation failed",
  "message": "Code exceeds maximum length of 10KB",
  "code": "VALIDATION_ERROR",
  "timestamp": "2025-06-21T10:30:00Z",
  "path": "/api/execute"
}
```

### Common HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Invalid or missing authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server error |

### Validation Errors

```json
{
  "error": "Validation failed",
  "message": "Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character",
  "code": "INVALID_PASSWORD",
  "field": "password"
}
```

### Execution Errors

```json
{
  "output": "",
  "error": "SyntaxError: invalid syntax\n  File \"main.py\", line 1\n    print(\"Hello World\"\n                        ^\nSyntaxError: invalid syntax",
  "exitCode": 1,
  "executionTime": 123
}
```

## 🔒 Security Considerations

### Input Validation

All code input is validated for:
- Length limits (10KB for code, 1KB for input)
- Malicious patterns (system commands, network requests)
- Language compatibility
- Character encoding

### Execution Security

- **Docker Sandboxing**: Each execution runs in an isolated container
- **Resource Limits**: Memory (128MB), CPU (0.5 cores), processes (50)
- **Network Isolation**: No external network access
- **Timeout Protection**: Language-specific execution timeouts
- **File System**: Temporary, isolated file systems

### API Security

- **HTTPS Only**: All production traffic encrypted
- **Rate Limiting**: Prevents abuse and DoS attacks
- **Input Sanitization**: SQL injection and XSS protection
- **Error Handling**: No sensitive information in error messages
- **Authentication**: JWT tokens with expiration

## 📚 SDK & Libraries

### JavaScript/TypeScript SDK

```bash
npm install @compiler/sdk
```

```javascript
import { CompilerClient } from '@compiler/sdk';

const client = new CompilerClient({
  apiUrl: 'https://api.compiler.example.com',
  token: 'your-jwt-token'
});

const result = await client.execute({
  code: 'print("Hello from SDK!")',
  language: 'python'
});

console.log(result.output);
```

### Python SDK

```bash
pip install compiler-sdk
```

```python
from compiler_sdk import CompilerClient

client = CompilerClient(
    api_url='https://api.compiler.example.com',
    token='your-jwt-token'
)

result = client.execute(
    code='print("Hello from Python SDK!")',
    language='python'
)

print(result.output)
```

### cURL Examples

#### Execute Python Code
```bash
curl -X POST "https://api.compiler.example.com/api/execute" \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "print(\"Hello from cURL!\")",
    "language": "python"
  }'
```

#### Get User Profile
```bash
curl -X GET "https://api.compiler.example.com/api/user/profile" \
  -H "Authorization: Bearer your-jwt-token"
```

## 🚀 Advanced Usage

### Batch Execution

Execute multiple code snippets efficiently:

```javascript
const codes = [
  { code: 'print("Hello")', language: 'python' },
  { code: 'console.log("World")', language: 'javascript' },
  { code: 'puts "!"', language: 'ruby' }
];

const results = await Promise.all(
  codes.map(item => client.execute(item))
);
```

### Error Handling Best Practices

```javascript
try {
  const result = await client.execute({
    code: 'invalid python code',
    language: 'python'
  });
  
  if (result.exitCode !== 0) {
    console.error('Execution failed:', result.error);
  } else {
    console.log('Output:', result.output);
  }
} catch (error) {
  if (error.status === 429) {
    console.log('Rate limited. Retry after:', error.retryAfter);
  } else {
    console.error('API Error:', error.message);
  }
}
```

### Streaming Output (WebSocket)

For long-running executions, use WebSocket for real-time output:

```javascript
const ws = new WebSocket('wss://api.compiler.example.com/ws/execute');

ws.onopen = () => {
  ws.send(JSON.stringify({
    token: 'your-jwt-token',
    code: 'for i in range(10): print(f"Line {i}")',
    language: 'python'
  }));
};

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  if (data.type === 'output') {
    console.log('Streaming output:', data.content);
  }
};
```

## 🤝 Support & Community

### Documentation
- **API Reference**: [https://docs.compiler.example.com](https://docs.compiler.example.com)
- **SDK Documentation**: [https://sdk.compiler.example.com](https://sdk.compiler.example.com)
- **Tutorials**: [https://learn.compiler.example.com](https://learn.compiler.example.com)

### Community
- **Discord**: [https://discord.gg/compiler](https://discord.gg/compiler)
- **GitHub**: [https://github.com/compiler/api](https://github.com/compiler/api)
- **Stack Overflow**: Tag `online-compiler-api`

### Support
- **Email**: api-support@compiler.example.com
- **Status Page**: [https://status.compiler.example.com](https://status.compiler.example.com)
- **Feature Requests**: [https://feedback.compiler.example.com](https://feedback.compiler.example.com)

---

**Happy Coding! 🎉**

*Built with ❤️ for developers who want to execute code anywhere, anytime.*
