# AI Insights Feature

## Overview
The AI Insights feature analyzes user code for security vulnerabilities, best practices, and code quality improvements using Google's Gemini AI API. It provides intelligent feedback only when there are clear, actionable insights to share.

## Features
- **Security Analysis**: Identifies potential security vulnerabilities in code
- **Best Practices**: Suggests improvements following industry standards  
- **Code Quality**: Analyzes code structure and maintainability
- **Smart Filtering**: Only displays insights when they are clear and actionable (not ambiguous)
- **Multi-language Support**: Works with all supported programming languages
- **Rate Limited**: Prevents abuse with built-in rate limiting

## How It Works

### User Experience
1. **Write Code**: User writes code in the editor
2. **Run Code**: After execution, the "AI Insights" section appears
3. **Analyze**: User clicks "🔍 Analyze Code" button
4. **Results**: AI provides categorized feedback with specific suggestions

### Analysis Categories
- **🔒 Security**: Security vulnerabilities and risks
- **✅ Good**: Code follows good practices
- **⭐ Quality**: Code quality improvements
- **🏆 Best Practices**: Industry best practice recommendations
- **📋 General**: Other insights and observations

### Sample Output
```
Category: Security
Insight: This code contains a potential SQL injection vulnerability in the database query.
Suggestions:
- Use parameterized queries instead of string concatenation
- Validate and sanitize all user inputs
- Consider using an ORM for database operations
Security Issue Detected: true
```

## Technical Implementation

### Backend Components
- **AIInsightController**: REST endpoint for AI analysis requests
- **AIInsightService**: Core service handling Gemini API integration
- **AIInsightRequest/Response**: Data models for API communication
- **Rate Limiting**: Built-in protection against abuse

### Frontend Components
- **OutputPanel**: Displays AI insights section in output panel
- **API Integration**: Handles communication with backend
- **UI Components**: Loading states, error handling, and results display

### Configuration
```yaml
# application.yml
gemini:
  api:
    key: ${GEMINI_API_KEY:}
    url: ${GEMINI_API_URL:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}
```

## Setup Instructions

### 1. Get Gemini API Key
1. Go to [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Create a new API key
3. Copy the key for configuration

### 2. Configure Backend
Add to your `.env` file:
```bash
GEMINI_API_KEY=your-actual-api-key-here
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent
```

### 3. Test Feature
1. Start the backend application
2. Write some code in the editor
3. Run the code
4. Click "🔍 Analyze Code" in the AI Insights section
5. View the analysis results

## Rate Limiting
- AI Insights uses the same rate limiting as other API endpoints
- Prevents abuse while allowing reasonable usage
- Rate limits are applied per IP address

## Error Handling
- **API Key Missing**: Shows configuration message
- **Rate Limit Exceeded**: Shows rate limit message  
- **Service Unavailable**: Shows temporary unavailability message
- **Invalid Response**: Falls back to raw text display

## Privacy & Security
- Code is sent to Google's Gemini API for analysis
- No code is stored permanently by the AI service
- Rate limiting prevents excessive API usage
- Users should be aware that code is processed by external AI service

## Customization
The AI prompt can be customized in `AIInsightService.java`:
```java
private String buildSecurityPrompt(String code, String language) {
    // Customize the prompt for different analysis focus
}
```

## Future Enhancements
- User preference for AI analysis types
- Integration with additional AI models
- Cached results for identical code
- User-specific analysis history
- Custom security rule sets

## Troubleshooting

### Common Issues
1. **"AI Insights feature is not configured"**
   - Check GEMINI_API_KEY is set in environment
   - Verify API key is valid

2. **"Rate limit exceeded"** 
   - Wait before trying again
   - Check rate limiting configuration

3. **"Service temporarily unavailable"**
   - Check internet connectivity
   - Verify Gemini API service status
   - Check API key permissions

### Logs
Check application logs for detailed error messages:
```bash
# Check backend logs
tail -f logs/application.log
```

## Dependencies
- Google Gemini AI API
- Spring Boot RestTemplate
- Jackson JSON processing
- Frontend: Axios for API calls
