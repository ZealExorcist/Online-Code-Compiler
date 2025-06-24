package com.example.compiler.controller;

import com.example.compiler.service.AIInsightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DebugController {
    
    @Autowired
    private AIInsightService aiInsightService;
    
    @Value("${gemini.api.url:}")
    private String geminiApiUrl;
    
    @Value("${gemini.api.key:}")
    private String geminiApiKey;
      @GetMapping("/gemini-config")
    public ResponseEntity<?> checkGeminiConfig() {
        return ResponseEntity.ok(java.util.Map.of(
            "geminiApiUrlConfigured", geminiApiUrl != null && !geminiApiUrl.trim().isEmpty(),
            "geminiApiUrl", geminiApiUrl != null ? geminiApiUrl : "NOT SET",
            "systemApiKeyConfigured", geminiApiKey != null && !geminiApiKey.trim().isEmpty(),
            "systemApiKeyLength", geminiApiKey != null ? geminiApiKey.length() : 0,
            "recommendedUrl", "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent",
            "currentModel", "gemini-2.0-flash"
        ));
    }
    
    @PostMapping("/test-ai")
    public ResponseEntity<?> testAI(@RequestBody java.util.Map<String, String> request) {
        try {
            String code = request.getOrDefault("code", "print('Hello, World!')");
            String language = request.getOrDefault("language", "python");
            String userApiKey = request.get("userApiKey");
            
            System.out.println("🧪 Debug test AI analysis:");
            System.out.println("  - Code: " + code);
            System.out.println("  - Language: " + language);
            System.out.println("  - User API key provided: " + (userApiKey != null && !userApiKey.trim().isEmpty()));
            
            var result = aiInsightService.analyzeCodeSecurity(code, language, userApiKey);
            
            return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "result", result,
                "hasError", result.getError() != null,
                "error", result.getError() != null ? result.getError() : "none"
            ));
        } catch (Exception e) {
            System.err.println("🧪 Debug test failed: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.ok(java.util.Map.of(
                "success", false,
                "error", e.getMessage(),
                "stackTrace", java.util.Arrays.toString(e.getStackTrace())
            ));
        }
    }
}
