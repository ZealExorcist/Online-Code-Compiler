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
            // Apply rate limiting for AI insights (with a more restrictive bucket for AI calls)
            String clientIp = getClientIp(httpRequest);
            String aiRateLimitKey = "ai-insights-" + clientIp;
            
            if (!rateLimitingService.tryConsume(aiRateLimitKey)) {
                return ResponseEntity.status(429).body("AI Insights rate limit exceeded. Please wait before trying again.");
            }
            
            AIInsightResponse response = aiInsightService.analyzeCodeSecurity(
                request.getCode(), 
                request.getLanguage(), 
                request.getUserApiKey()
            );
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("AI Insights service temporarily unavailable.");
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
