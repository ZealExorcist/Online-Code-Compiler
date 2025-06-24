package com.example.compiler.service;

import com.example.compiler.model.AIInsightResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIInsightService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIInsightService.class);
    
    @Value("${gemini.api.key:}")
    private String geminiApiKey;
    
    @Value("${gemini.api.url:}")
    private String geminiApiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
      public AIInsightResponse analyzeCodeSecurity(String code, String language, String userApiKey) {
        String apiKeyToUse = userApiKey;
        
        // Use user's API key if provided, otherwise fall back to system API key
        if (apiKeyToUse == null || apiKeyToUse.trim().isEmpty()) {
            apiKeyToUse = geminiApiKey;
        }
        
        if (apiKeyToUse == null || apiKeyToUse.trim().isEmpty()) {
            return new AIInsightResponse("AI Insights feature requires a Gemini API key. Please add your API key in Settings > Account > Gemini AI API Key, or contact administrator to configure the system key.");
        }
        
        if (code == null || code.trim().isEmpty()) {
            return new AIInsightResponse("No code provided for analysis.");
        }
        
        try {
            String prompt = buildSecurityPrompt(code, language);
            String geminiResponse = callGeminiAPI(prompt, apiKeyToUse);
            return parseGeminiResponse(geminiResponse);
        } catch (Exception e) {
            logger.error("Error analyzing code with AI: ", e);
            return new AIInsightResponse("AI analysis temporarily unavailable. Please check your API key and try again later.");
        }
    }
    
    private String buildSecurityPrompt(String code, String language) {
        return String.format(
            "Analyze the following %s code for security vulnerabilities, best practices, and potential improvements. " +
            "Focus only on clear, definitive security issues and obvious code quality problems. " +
            "If the code appears safe and follows good practices, say so clearly. " +
            "Provide your response in this exact JSON format: " +
            "{\"insight\": \"your main insight here\", \"category\": \"Security|Quality|Best Practices|Good\", " +
            "\"suggestions\": [\"suggestion1\", \"suggestion2\"], \"hasSecurityIssues\": true/false}\n\n" +
            "Code to analyze:\n```%s\n%s\n```",
            language, language, code
        );
    }
      private String callGeminiAPI(String prompt, String apiKey) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = new HashMap<>();
        
        // Create the contents array with parts
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", Arrays.asList(part));
        requestBody.put("contents", Arrays.asList(content));
        
        // Add generation config for more reliable JSON responses
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.1);
        generationConfig.put("topK", 1);
        generationConfig.put("topP", 0.8);
        generationConfig.put("maxOutputTokens", 1000);
        requestBody.put("generationConfig", generationConfig);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        String url = geminiApiUrl + "?key=" + apiKey;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Gemini API returned status: " + response.getStatusCode());
        }
        
        return response.getBody();
    }
    
    private AIInsightResponse parseGeminiResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.get("candidates");
            
            if (candidates == null || candidates.size() == 0) {
                return new AIInsightResponse("No insights available for this code.");
            }
            
            JsonNode content = candidates.get(0).get("content");
            if (content == null) {
                return new AIInsightResponse("Unable to analyze code at this time.");
            }
            
            JsonNode parts = content.get("parts");
            if (parts == null || parts.size() == 0) {
                return new AIInsightResponse("No analysis results available.");
            }
            
            String text = parts.get(0).get("text").asText();
            
            // Try to extract JSON from the response
            return extractJsonFromResponse(text);
            
        } catch (Exception e) {
            logger.error("Error parsing Gemini response: ", e);
            return new AIInsightResponse("Error processing AI analysis results.");
        }
    }
    
    private AIInsightResponse extractJsonFromResponse(String text) {
        try {
            // Look for JSON content in the response
            int jsonStart = text.indexOf("{");
            int jsonEnd = text.lastIndexOf("}");
            
            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                String jsonStr = text.substring(jsonStart, jsonEnd + 1);
                JsonNode json = objectMapper.readTree(jsonStr);
                
                String insight = json.has("insight") ? json.get("insight").asText() : "Code analysis completed.";
                String category = json.has("category") ? json.get("category").asText() : "General";
                boolean hasSecurityIssues = json.has("hasSecurityIssues") ? json.get("hasSecurityIssues").asBoolean() : false;
                  List<String> suggestions = Arrays.asList();
                if (json.has("suggestions") && json.get("suggestions").isArray()) {
                    List<String> tempSuggestions = new ArrayList<>();
                    for (JsonNode suggestion : json.get("suggestions")) {
                        tempSuggestions.add(suggestion.asText());
                    }
                    suggestions = tempSuggestions;
                }
                
                return new AIInsightResponse(insight, category, suggestions, hasSecurityIssues);
            }
            
            // If no JSON found, return the raw text as insight
            return new AIInsightResponse(text.trim(), "General", Arrays.asList(), false);
            
        } catch (Exception e) {
            logger.error("Error extracting JSON from AI response: ", e);
            // Return raw text if JSON parsing fails
            return new AIInsightResponse(text.trim(), "General", Arrays.asList(), false);
        }
    }
}
