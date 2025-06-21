# MongoDB Setup Guide for Online Compiler

## Prerequisites
- MongoDB installed on your system
- Node.js and npm installed
- Java 17+ installed

## MongoDB Installation

### Option 1: Local MongoDB Installation

1. **Download MongoDB Community Server**
   - Visit https://www.mongodb.com/try/download/community
   - Download for Windows/macOS/Linux
   - Install following the installer instructions

2. **Start MongoDB Service**
   ```bash
   # Windows (as Administrator)
   net start MongoDB
   
   # macOS/Linux
   sudo systemctl start mongod
   # or
   brew services start mongodb-community
   ```

3. **Verify Installation**
   ```bash
   mongo --version
   # or for newer versions
   mongosh --version
   ```

### Option 2: MongoDB Docker Container

1. **Pull and run MongoDB container**
   ```bash
   docker run -d --name mongodb -p 27017:27017 mongo:latest
   ```

2. **Connect to MongoDB**
   ```bash
   docker exec -it mongodb mongosh
   ```

### Option 3: MongoDB Atlas (Cloud)

1. **Sign up for MongoDB Atlas**
   - Visit https://www.mongodb.com/cloud/atlas
   - Create a free account

2. **Create a cluster**
   - Follow the setup wizard
   - Choose the free tier (M0)

3. **Get connection string**
   - Replace `<username>`, `<password>`, and `<cluster-url>` in:
   ```
   mongodb+srv://<username>:<password>@<cluster-url>/online-compiler?retryWrites=true&w=majority
   ```

## Application Configuration

### Backend Configuration

1. **Update application-dev.yml**
   ```yaml
   spring:
     data:
       mongodb:
         # For local MongoDB
         uri: mongodb://localhost:27017/online-compiler-dev
         database: online-compiler-dev
         
         # For MongoDB Atlas
         # uri: mongodb+srv://username:password@cluster.mongodb.net/online-compiler-dev
         # database: online-compiler-dev
   ```

2. **Environment Variables (Optional)**
   ```bash
   # Set environment variables
   export MONGODB_URI=mongodb://localhost:27017/online-compiler
   export MONGODB_DATABASE=online-compiler
   ```

### Frontend Configuration

1. **Update environment file**
   ```bash
   # Create .env file in frontend directory
   VITE_API_URL=http://localhost:8080
   ```

## Running the Application

### 1. Start MongoDB
```bash
# Local installation
mongod

# Docker
docker start mongodb
```

### 2. Start Backend
```bash
cd backend
./mvnw spring-boot:run
```

### 3. Start Frontend
```bash
cd frontend
npm install
npm run dev
```

## Verify Setup

1. **Check MongoDB Connection**
   - Backend logs should show successful MongoDB connection
   - No "Connection refused" errors

2. **Test User Registration**
   - Navigate to http://localhost:3000
   - Click "Sign Up"
   - Create a new account
   - Check MongoDB for user data

3. **Test Code Execution**
   - Login with created account
   - Write and execute code
   - Verify snippets are saved to MongoDB

## Troubleshooting

### MongoDB Connection Issues

1. **Connection Refused**
   ```
   Error: Connection refused: no further information
   ```
   - Solution: Ensure MongoDB service is running
   - Check if port 27017 is available

2. **Authentication Failed**
   ```
   Error: Authentication failed
   ```
   - Solution: Check username/password in connection string
   - Ensure user has proper permissions

3. **Database Access Denied**
   ```
   Error: not authorized on database
   ```
   - Solution: Grant read/write permissions to user
   - Check database name in configuration

### Application Fallback

If MongoDB is not available, the application will:
1. Log connection errors
2. Fall back to H2 database
3. Continue functioning with limited features

## Development vs Production

### Development
- Use local MongoDB or Docker
- Enable H2 console for debugging
- Use development-specific database names

### Production
- Use MongoDB Atlas or dedicated server
- Disable H2 console
- Set proper authentication and SSL
- Use environment variables for secrets

## Database Collections

The application creates these collections:
- `users` - User accounts and settings
- `snippets` - Code snippets and sharing data

## API Endpoints

### Authentication
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/profile` - Get user profile

### Code Execution
- `POST /api/execute` - Execute code
- `GET /api/languages` - Get supported languages

### User Management
- `GET /api/user/settings` - Get user settings
- `PUT /api/user/settings` - Update user settings
- `POST /api/user/change-password` - Change password

## Security Notes

1. **Change Default Passwords**
   - Update admin password in production
   - Use strong JWT secrets

2. **Enable Authentication**
   - All execution endpoints require authentication
   - API keys provide alternative access

3. **Rate Limiting**
   - Configured to prevent abuse
   - Adjustable per environment

## Monitoring

1. **MongoDB Metrics**
   - Use MongoDB Compass for GUI
   - Monitor connection pool usage

2. **Application Logs**
   - Check for authentication errors
   - Monitor code execution patterns

3. **Performance**
   - Index frequently queried fields
   - Monitor database response times
