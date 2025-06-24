# AI Insights Error Debugging Instructions

## Current Status
From the debug logs, we can see:
- ✅ Frontend is working correctly (API key present, HTTP request successful)
- ✅ Backend receives the request and returns HTTP 200
- ❌ Backend service is returning an error: "AI analysis temporarily unavailable"

## Next Steps to Debug

### 1. Start the Backend Server
```bash
cd d:\Projects\compiler\backend
.\mvnw.cmd spring-boot:run
```

### 2. Check Backend Configuration
Open browser and visit: `http://localhost:8080/api/debug/gemini-config`

This will show:
- Whether Gemini API URL is configured correctly
- Whether system API key is set
- Configuration status

### 3. Test AI Service Directly
Use the debug endpoint to test the AI service:

**POST** `http://localhost:8080/api/debug/test-ai`
```json
{
  "code": "print('Hello, World!')",
  "language": "python",
  "userApiKey": "YOUR_GEMINI_API_KEY_HERE"
}
```

### 4. Review Backend Console Logs
When you run the backend, look for these debug messages:
- 🔍 AI Insights request received
- 📝 Building security prompt
- 📡 Calling Gemini API
- ✅/❌ Success or failure messages

### 5. Expected Issues and Solutions

#### Issue: Missing System API Key
**Symptom**: Backend shows "systemApiKeyConfigured: false"
**Solution**: Create `.env` file in backend root:
```bash
GEMINI_API_KEY=your-system-api-key-here
```

#### Issue: Invalid API Key
**Symptom**: "Authentication failed" or "Invalid API key"
**Solution**: 
1. Verify API key is correct from Google AI Studio
2. Check API key has proper permissions
3. Ensure no extra spaces or characters

#### Issue: API URL Configuration
**Symptom**: "Gemini API URL not configured"
**Solution**: Verify `application.yml` has correct URL

#### Issue: Network/Firewall
**Symptom**: Connection timeout or network errors
**Solution**: Check internet connection and firewall settings

#### Issue: Quota Exceeded
**Symptom**: "quota exceeded" or "limit" in error message
**Solution**: Wait for quota reset or upgrade Gemini API plan

### 6. Manual API Test (Optional)
You can test the Gemini API directly using curl:

```bash
curl -X POST \
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "contents": [{
      "parts": [{"text": "Say hello"}]
    }]
  }'
```

## Debug Flow Summary

1. **Start backend** with debug logging
2. **Check configuration** via debug endpoint
3. **Test AI service** with known good API key
4. **Review console logs** for specific error details
5. **Apply appropriate fix** based on error type

## Common Fixes

### Fix 1: Add System API Key
Create `d:\Projects\compiler\backend\.env`:
```
GEMINI_API_KEY=your-actual-gemini-api-key
```

### Fix 2: Verify User API Key
- Ensure API key is 39 characters long
- Starts with "AIzaSy"
- Has proper permissions in Google AI Studio

### Fix 3: Check Rate Limits
- User may have exceeded rate limits
- Try with a different API key
- Wait for rate limit reset

The enhanced error messages in the backend will now provide much more specific information about what's going wrong!
