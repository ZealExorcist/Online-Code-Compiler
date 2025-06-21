# Testing Guide: Status Dot and API Key Fixes

## Issues Fixed

### 1. Status Dot (Footer Connection Indicator)
**Problem**: Status dot wasn't functioning properly
**Solution**: 
- Fixed timeout handling using AbortController
- Changed endpoint from `/api/languages` to `/api/public/health`
- Added proper error handling
- Created dedicated health endpoint

### 2. API Key Generation
**Problem**: API key generation not working
**Solution**:
- Fixed signup flow to auto-login after successful registration
- Added better error handling and debugging
- Ensured API key is properly generated and returned

## Testing Steps

### Test 1: Backend Health Check
1. Start the backend:
   ```bash
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

2. Test the health endpoint directly:
   ```bash
   curl http://localhost:8080/api/public/health
   ```
   
   Expected response:
   ```json
   {
     "status": "online",
     "timestamp": "2025-06-20T15:18:49",
     "message": "Online Compiler Backend is running"
   }
   ```

### Test 2: Status Dot
1. Start the frontend:
   ```bash
   cd frontend
   npm run dev
   ```

2. Open browser to `http://localhost:3000`

3. Look at the footer - you should see:
   - Green dot with "Online" text when backend is running
   - Red dot with "Offline" text when backend is stopped

4. Test by stopping and starting the backend

### Test 3: User Registration and API Key
1. Open browser developer tools (F12) -> Console tab

2. Navigate to `http://localhost:3000`

3. Click "Sign Up" (or go to `/login` and switch to signup)

4. Fill in the form:
   - Username: `testuser`
   - Email: `test@example.com`
   - Password: `password123`
   - Confirm Password: `password123`

5. Submit the form

6. Watch the console for debug messages:
   - "Loading user info..."
   - "User info loaded: {object with apiKey}"

7. If successful, you should be redirected to the home page

### Test 4: API Key in Settings
1. After successful signup/login, click on your username in the header

2. Click "Settings"

3. Go to "Account" tab

4. Look for the "API Key" section

5. You should see a generated API key (format: `oc_xxxxxxxxxx`)

6. Click "Refresh" button to test API key regeneration

7. Watch console for debug messages:
   - "Refreshing API key..."
   - "New API key received: oc_xxxxxxxxxx"

### Test 5: Manual API Testing
1. Test signup endpoint directly:
   ```bash
   # Windows PowerShell
   .\test_signup.ps1
   
   # Or using curl
   curl -X POST http://localhost:8080/api/auth/signup \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser2",
       "email": "test2@example.com",
       "password": "password123",
       "confirmPassword": "password123"
     }'
   ```

2. Test login endpoint:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser2",
       "password": "password123"
     }'
   ```

3. Test profile endpoint with JWT token:
   ```bash
   curl -X GET http://localhost:8080/api/auth/profile \
     -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
   ```

## Debugging Tips

### If Status Dot Still Shows Offline:
1. Check browser console for errors
2. Verify backend is running on port 8080
3. Test health endpoint directly: `http://localhost:8080/api/public/health`
4. Check for CORS issues in browser network tab

### If API Key is Missing:
1. Check browser console for error messages
2. Look for "User info loaded:" message in console
3. Test `/api/auth/profile` endpoint directly
4. Check if MongoDB is connected (look for connection errors in backend logs)

### If Signup Fails:
1. Check backend logs for validation errors
2. Look at browser Network tab for request/response details
3. Verify all required fields are filled
4. Check if username/email already exists

## Expected Log Messages

### Backend Logs (Successful Signup):
```
2025-06-20 15:18:49 - Signup request received for username: testuser
2025-06-20 15:18:49 - Creating new user: testuser
2025-06-20 15:18:49 - New user registered: testuser (test@example.com)
```

### Frontend Console (Successful Flow):
```
Loading user info...
User info loaded: {username: "testuser", email: "test@example.com", apiKey: "oc_abc123..."}
```

### API Key Refresh:
```
Refreshing API key...
New API key received: oc_def456...
```

## Troubleshooting Common Issues

1. **MongoDB Connection Error**: 
   - Check MongoDB Atlas connection string
   - Verify internet connection
   - App will fall back to H2 database

2. **CORS Errors**:
   - Verify backend CORS configuration
   - Check if both frontend and backend are running

3. **JWT Token Issues**:
   - Check if token is properly stored in localStorage
   - Verify token isn't expired

4. **API Key Not Showing**:
   - Check if user is properly authenticated
   - Verify userService.generateApiKey() is being called
   - Check MongoDB user document for apiKey field

## Success Criteria

✅ Status dot shows green "Online" when backend is running
✅ Status dot shows red "Offline" when backend is stopped
✅ User can successfully sign up
✅ API key is generated and displayed in settings
✅ API key can be refreshed
✅ No console errors during normal operation
