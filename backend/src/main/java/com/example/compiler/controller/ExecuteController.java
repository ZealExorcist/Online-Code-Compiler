package com.example.compiler.controller;

import com.example.compiler.model.ExecuteRequest;
import com.example.compiler.model.ExecuteResponse;
import com.example.compiler.model.User;
import com.example.compiler.model.UserTier;
import com.example.compiler.security.UserPrincipal;
import com.example.compiler.service.ExecutionService;
import com.example.compiler.service.RateLimitService;
import com.example.compiler.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // For development - should be configured properly in production
public class ExecuteController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExecuteController.class);
    
    @Autowired
    private ExecutionService executionService;
    
    @Autowired
    private RateLimitService rateLimitService;
    
    @Autowired
    private UserService userService;
    
    @Value("${spring.profiles.active:}")
    private String activeProfile;    @PostMapping("/execute")
    public ResponseEntity<ExecuteResponse> executeCode(@RequestBody ExecuteRequest request, 
                                                      Authentication authentication,
                                                      HttpServletRequest httpRequest) {
        String userId = null;
        String username = "anonymous";
        UserTier userTier = UserTier.BASIC; // Default tier for anonymous users
        String rateLimitKey; // Key used for rate limiting
          // Get user info if authenticated
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            userId = userPrincipal.getUserId();
            username = userPrincipal.getUsername();
            rateLimitKey = userId; // Use user ID for authenticated users
            
            // Get user's tier
            User user = userService.getUserByUsername(username);
            if (user != null) {
                try {
                    userTier = UserTier.valueOf(user.getTier());
                } catch (IllegalArgumentException e) {
                    userTier = UserTier.BASIC;
                }
            }
        } else {
            // For anonymous users, use IP address for rate limiting
            rateLimitKey = "anon_" + getClientIpAddress(httpRequest);
            // Anonymous users get more restrictive limits
            userTier = UserTier.ANONYMOUS;
        }

        // Check rate limiting (applies to both authenticated and anonymous users)
        if (rateLimitService.isRateLimited(rateLimitKey, userTier)) {
            int remaining = rateLimitService.getRemainingRequests(rateLimitKey, userTier);
            String message = userId != null ? 
                "Rate limit exceeded. " + userTier.getDescription() + ". Remaining requests this hour: " + remaining :
                "Rate limit exceeded for anonymous users. Please wait or sign up for higher limits. Remaining requests this hour: " + remaining;
            return ResponseEntity.status(429)
                .body(ExecuteResponse.error(message));
        }

        logger.info("Received execution request for language: {} from user: {} (tier: {}, key: {})", 
                   request.getLanguage(), username, userTier, rateLimitKey);
        
        // Validate request
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ExecuteResponse.error("Code cannot be empty"));
        }
        
        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ExecuteResponse.error("Language must be specified"));
        }
          // Limit code length (e.g., 10KB)
        if (request.getCode().length() > 10240) {
            return ResponseEntity.badRequest()
                .body(ExecuteResponse.error("Code exceeds maximum length of 10KB"));
        }

        try {
            // Apply tier-based delay in development mode
            if ("dev".equals(activeProfile) && userTier.getDelayMs() > 0) {
                logger.info("Applying tier-based delay: {}ms for tier: {}", userTier.getDelayMs(), userTier);
                Thread.sleep(userTier.getDelayMs());
            }
            
            ExecuteResponse response = executionService.executeCode(
                request.getCode(),
                request.getLanguage(),
                request.getInput(),
                userId
            );
              // Add tier information to response
            if (userId != null) {
                int remaining = rateLimitService.getRemainingRequests(rateLimitKey, userTier);
                response.setMetadata("tier", userTier.name());
                response.setMetadata("tierDescription", userTier.getDescription());
                response.setMetadata("remainingRequests", remaining);
                response.setMetadata("authenticated", true);
            } else {
                // For anonymous users, also show rate limit info
                int remaining = rateLimitService.getRemainingRequests(rateLimitKey, userTier);
                response.setMetadata("tier", "ANONYMOUS");
                response.setMetadata("tierDescription", "Anonymous access - Sign up for higher limits");
                response.setMetadata("remainingRequests", remaining);
                response.setMetadata("authenticated", false);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Execution interrupted", e);
            return ResponseEntity.internalServerError()
                .body(ExecuteResponse.error("Execution interrupted"));
        } catch (Exception e) {
            logger.error("Error executing code", e);
            return ResponseEntity.internalServerError()
                .body(ExecuteResponse.error("Internal server error"));
        }
    }
      @GetMapping("/languages")
    public ResponseEntity<Set<String>> getSupportedLanguages() {
        Set<String> languages = executionService.getSupportedLanguages();
        return ResponseEntity.ok(languages);
    }
    
    /**
     * Get the client's IP address, considering various proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        // Fallback to remote address
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }
}
