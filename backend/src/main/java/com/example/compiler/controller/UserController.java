package com.example.compiler.controller;

import com.example.compiler.model.User;
import com.example.compiler.model.UserTier;
import com.example.compiler.model.Snippet;
import com.example.compiler.security.UserPrincipal;
import com.example.compiler.service.UserService;
import com.example.compiler.service.SnippetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SnippetService snippetService;

    @GetMapping("/settings")
    public ResponseEntity<?> getUserSettings(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.badRequest().body(createErrorResponse("Authentication required"));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();
        
        return ResponseEntity.ok(user.getSettings());
    }
    
    @PutMapping("/settings")
    public ResponseEntity<?> updateUserSettings(@Valid @RequestBody User.UserSettings settings, 
                                               Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.badRequest().body(createErrorResponse("Authentication required"));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        try {
            User updatedUser = userService.updateUserSettings(userPrincipal.getUserId(), settings);
            logger.info("Settings updated for user: {}", updatedUser.getUsername());
            return ResponseEntity.ok(updatedUser.getSettings());
        } catch (Exception e) {
            logger.error("Failed to update settings for user: {}", userPrincipal.getUsername(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("Failed to update settings"));
        }
    }
    
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData,
                                          Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.badRequest().body(createErrorResponse("Authentication required"));
        }
        
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(createErrorResponse("Both old and new passwords are required"));
        }
        
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(createErrorResponse("New password must be at least 6 characters"));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        try {
            userService.updatePassword(userPrincipal.getUserId(), oldPassword, newPassword);
            logger.info("Password changed for user: {}", userPrincipal.getUsername());
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (Exception e) {
            logger.error("Failed to change password for user: {}", userPrincipal.getUsername(), e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.badRequest().body(createErrorResponse("Authentication required"));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());        profile.put("email", user.getEmail());
        profile.put("tier", user.getTier());
        profile.put("apiKey", user.getApiKey());
        profile.put("createdAt", user.getCreatedAt());
        profile.put("lastLoginAt", user.getLastLoginAt());
        profile.put("snippetCount", user.getSnippetIds().size());
        profile.put("settings", user.getSettings());
        
        return ResponseEntity.ok(profile);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<?> getUserStatistics(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.badRequest().body(createErrorResponse("Authentication required"));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSnippets", user.getSnippetIds().size());
        stats.put("memberSince", user.getCreatedAt());
        stats.put("lastActive", user.getLastLoginAt());
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/tiers")
    public ResponseEntity<?> getAvailableTiers() {
        Map<String, Object> tierInfo = new HashMap<>();
        for (UserTier tier : UserTier.values()) {
            Map<String, Object> info = new HashMap<>();
            info.put("description", tier.getDescription());
            info.put("requestsPerHour", tier.getRequestsPerHour());
            info.put("delayMs", tier.getDelayMs());
            tierInfo.put(tier.name(), info);
        }
        return ResponseEntity.ok(tierInfo);
    }
    
    @PostMapping("/upgrade")
    public ResponseEntity<?> upgradeTier(@RequestBody Map<String, String> request, 
                                        Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.badRequest().body(createErrorResponse("Authentication required"));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String newTier = request.get("tier");
        
        if (newTier == null || newTier.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Tier is required"));
        }
        
        try {
            UserTier.valueOf(newTier); // Validate tier exists
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Invalid tier: " + newTier));
        }
        
        try {
            User updatedUser = userService.upgradeTier(userPrincipal.getUserId(), newTier);
            logger.info("User {} upgraded to tier: {}", updatedUser.getUsername(), newTier);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tier upgraded successfully");
            response.put("newTier", newTier);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to upgrade tier for user: {}", userPrincipal.getUsername(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Failed to upgrade tier"));
        }
    }
    
    @PostMapping("/generate-api-key")
    public ResponseEntity<?> generateNewApiKey(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.badRequest().body(createErrorResponse("Authentication required"));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        try {
            String newApiKey = userService.generateNewApiKey(userPrincipal.getUserId());
            logger.info("Generated new API key for user: {}", userPrincipal.getUsername());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "API key generated successfully");
            response.put("apiKey", newApiKey);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to generate API key for user: {}", userPrincipal.getUsername(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Failed to generate API key"));
        }
    }
    
    @GetMapping("/snippets")
    public ResponseEntity<?> getUserSnippets(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.badRequest().body(createErrorResponse("Authentication required"));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        try {
            List<Snippet> snippets = snippetService.getUserSnippets(userPrincipal.getUserId());
            return ResponseEntity.ok(snippets);
        } catch (Exception e) {
            logger.error("Failed to get snippets for user: {}", userPrincipal.getUsername(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Failed to retrieve snippets"));
        }
    }
    
    @PostMapping("/snippets")
    public ResponseEntity<?> saveUserSnippet(@RequestBody Map<String, String> request, 
                                            Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.badRequest().body(createErrorResponse("Authentication required"));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        String title = request.get("title");
        String code = request.get("code");
        String language = request.get("language");
        String input = request.get("input");
        
        // Validate request
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Code is required"));
        }
        
        if (language == null || language.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Language is required"));
        }
        
        // Limit code length
        if (code.length() > 50000) { // 50KB limit
            return ResponseEntity.badRequest().body(createErrorResponse("Code too long (max 50KB)"));
        }
        
        try {
            Snippet snippet = snippetService.saveUserSnippet(
                userPrincipal.getUserId(), 
                title != null ? title : "Untitled Snippet", 
                code, 
                language,
                input
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Snippet saved successfully");
            response.put("snippet", snippet);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to save snippet for user: {}", userPrincipal.getUsername(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Failed to save snippet"));
        }
    }
    
    @DeleteMapping("/snippets/{snippetId}")
    public ResponseEntity<?> deleteUserSnippet(@PathVariable String snippetId, 
                                              Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.badRequest().body(createErrorResponse("Authentication required"));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        try {
            boolean deleted = snippetService.deleteUserSnippet(userPrincipal.getUserId(), snippetId);
            
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Snippet deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to delete snippet {} for user: {}", snippetId, userPrincipal.getUsername(), e);
            return ResponseEntity.internalServerError()
                .body(createErrorResponse("Failed to delete snippet"));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}
