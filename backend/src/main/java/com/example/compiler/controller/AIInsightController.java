package com.example.compiler.controller;

import com.example.compiler.model.AIInsightRequest;
import com.example.compiler.model.AIInsightResponse;
import com.example.compiler.service.AIInsightService;
import com.example.compiler.service.RateLimitingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AIInsightController {
    
    @Autowired
    private AIInsightService aiInsightService;
    
    @Autowired
    private RateLimitingService rateLimitingService;    @PostMapping("/ai-insights")
    public ResponseEntity<?> getAIInsights(@RequestBody AIInsightRequest request, HttpServletRequest httpRequest) {
        try {
            System.out.println("🔍 AI Insights request received:");
            System.out.println("  - Code length: " + (request.getCode() != null ? request.getCode().length() : 0));
            System.out.println("  - Language: " + request.getLanguage());
            System.out.println("  - Has user API key: " + (request.getUserApiKey() != null && !request.getUserApiKey().isEmpty()));
            System.out.println("  - User API key length: " + (request.getUserApiKey() != null ? request.getUserApiKey().length() : 0));
            if (request.getUserApiKey() != null && request.getUserApiKey().length() > 10) {
                System.out.println("  - User API key preview: " + request.getUserApiKey().substring(0, 10) + "...");
            }
            
            // Apply rate limiting for AI insights (with a more restrictive bucket for AI calls)
            String clientIp = getClientIp(httpRequest);
            String aiRateLimitKey = "ai-insights-" + clientIp;
            
            System.out.println("  - Client IP: " + clientIp);
            System.out.println("  - Rate limit key: " + aiRateLimitKey);
            
            if (!rateLimitingService.tryConsume(aiRateLimitKey)) {
                System.out.println("❌ Rate limit exceeded for: " + aiRateLimitKey);
                return ResponseEntity.status(429).body("AI Insights rate limit exceeded. Please wait before trying again.");
            }
            
            System.out.println("✅ Rate limit passed, calling AI service...");
            AIInsightResponse response = aiInsightService.analyzeCodeSecurity(
                request.getCode(), 
                request.getLanguage(), 
                request.getUserApiKey()
            );
              System.out.println("✅ AI service completed successfully");
            System.out.println("  - Response has insight: " + (response.getInsight() != null));
            System.out.println("  - Response category: " + response.getCategory());
            System.out.println("  - Response suggestions count: " + (response.getSuggestions() != null ? response.getSuggestions().size() : 0));
            System.out.println("  - Response has error: " + (response.getError() != null));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ AI Insights error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("AI Insights service temporarily unavailable: " + e.getMessage());
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
