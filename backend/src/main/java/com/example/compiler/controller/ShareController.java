package com.example.compiler.controller;

import com.example.compiler.model.ShareRequest;
import com.example.compiler.model.ShareResponse;
import com.example.compiler.model.LoadResponse;
import com.example.compiler.model.User;
import com.example.compiler.repository.UserRepository;
import com.example.compiler.security.UserPrincipal;
import com.example.compiler.service.ShareService;
import com.example.compiler.service.AnonymousShareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ShareController {
    
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);    @Autowired
    private ShareService shareService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AnonymousShareService anonymousShareService;    @PostMapping("/share")
    public ResponseEntity<ShareResponse> createShare(@RequestBody ShareRequest request, Authentication authentication, HttpServletRequest httpRequest) {
        try {
            logger.info("Creating share for language: {}, title: {}", request.getLanguage(), request.getTitle());
            
            // Validate request
            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Get user ID if authenticated
            String userId = null;
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                userId = userPrincipal.getUserId();
                logger.info("Authenticated user {} creating share", userPrincipal.getUsername());
            } else {
                logger.info("Anonymous user creating share");
            }
            
            // Get client IP for anonymous users
            String clientIp = getClientIpAddress(httpRequest);
            
            ShareResponse response = shareService.createShareUrl(request, userId, clientIp);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            // Handle share limit errors specifically
            if (e.getMessage().contains("Daily share limit reached")) {
                logger.warn("Share limit reached: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ShareResponse(null, null, e.getMessage()));
            }
            logger.error("Error creating share", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Error creating share", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/load/{shareId}")
    public ResponseEntity<LoadResponse> loadShare(@PathVariable String shareId) {
        try {
            logger.info("Loading share with ID: {}", shareId);
            
            LoadResponse response = shareService.loadFromShareId(shareId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error loading share", e);
            LoadResponse errorResponse = new LoadResponse("Failed to load shared code");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/share/info")
    public ResponseEntity<String> getShareInfo() {
        return ResponseEntity.ok("URL-based code sharing service. No data is stored on the server.");
    }
      @GetMapping("/share/limits")
    public ResponseEntity<Map<String, Object>> getShareLimits(Authentication authentication, HttpServletRequest httpRequest) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                // Get user from database to check current share count
                Optional<User> userOptional = userRepository.findById(userPrincipal.getUserId());
                
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    user.resetShareCountIfNeeded(); // Reset if new day
                    
                    response.put("canShare", user.canShare());
                    response.put("remainingShares", user.getRemainingShares());
                    response.put("usedShares", user.getShareCount());
                    response.put("tier", user.getTier());
                    response.put("unlimited", "ADVANCED".equals(user.getTier()) || "MASTER".equals(user.getTier()));
                } else {
                    response.put("canShare", false);
                    response.put("remainingShares", 0);
                    response.put("usedShares", 0);
                    response.put("tier", "BASIC");
                    response.put("unlimited", false);
                }
            } else {
                // Anonymous user - check IP-based limits
                String clientIp = getClientIpAddress(httpRequest);
                
                response.put("canShare", anonymousShareService.canShare(clientIp));
                response.put("remainingShares", anonymousShareService.getRemainingShares(clientIp));
                response.put("usedShares", anonymousShareService.getUsedShares(clientIp));
                response.put("tier", "ANONYMOUS");
                response.put("unlimited", false);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting share limits", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
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
