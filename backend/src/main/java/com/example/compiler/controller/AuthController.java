package com.example.compiler.controller;

import com.example.compiler.model.AuthRequest;
import com.example.compiler.model.AuthResponse;
import com.example.compiler.model.SignupRequest;
import com.example.compiler.model.SignupResponse;
import com.example.compiler.model.User;
import com.example.compiler.security.UserPrincipal;
import com.example.compiler.service.AuthService;
import com.example.compiler.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            logger.info("User {} logged in successfully", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.warn("Login failed for user: {}", request.getUsername());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid username or password"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.register(request);
            logger.info("User {} registered successfully", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Registration failed for user: {}", request.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request, BindingResult bindingResult) {
        logger.info("Signup request received for username: {}", request != null ? request.getUsername() : "null");
        
        // Check for null request
        if (request == null) {
            logger.warn("Signup failed: request is null");
            return ResponseEntity.badRequest().body(SignupResponse.error("Invalid request"));
        }
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .findFirst()
                    .orElse("Invalid input");
            logger.warn("Signup validation failed: {}", errorMessage);
            return ResponseEntity.badRequest().body(SignupResponse.error(errorMessage));
        }
        
        // Check if passwords match
        if (!request.isPasswordMatching()) {
            logger.warn("Signup failed: passwords do not match for user {}", request.getUsername());
            return ResponseEntity.badRequest().body(SignupResponse.error("Passwords do not match"));
        }
        
        try {
            // Check if username already exists
            if (userService.existsByUsername(request.getUsername())) {
                logger.warn("Signup failed: username {} already exists", request.getUsername());
                return ResponseEntity.badRequest().body(SignupResponse.error("Username is already taken"));
            }
            
            // Check if email already exists
            if (userService.existsByEmail(request.getEmail())) {
                logger.warn("Signup failed: email {} already exists", request.getEmail());
                return ResponseEntity.badRequest().body(SignupResponse.error("Email is already registered"));
            }
            
            // Create new user
            User newUser = userService.createUser(request.getUsername(), request.getEmail(), request.getPassword());
            
            logger.info("New user registered: {} ({})", newUser.getUsername(), newUser.getEmail());
            
            return ResponseEntity.ok(SignupResponse.success(
                newUser.getId(),
                newUser.getUsername(),
                newUser.getEmail()
            ));
            
        } catch (Exception e) {
            logger.error("Error during user registration for {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(SignupResponse.error("Registration failed. Please try again."));
        }
    }    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Authentication required"));
        }
        
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Authentication required"));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("apiKey", user.getApiKey());
        userInfo.put("roles", user.getRoles());
        userInfo.put("tier", user.getTier());
        userInfo.put("settings", user.getSettings());
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("lastLoginAt", user.getLastLoginAt());
        userInfo.put("snippetCount", user.getSnippetIds().size());
        
        return ResponseEntity.ok(userInfo);
    }
    
    @PostMapping("/refresh-api-key")
    public ResponseEntity<?> refreshApiKey(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Authentication required"));
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        try {
            String newApiKey = userService.regenerateApiKey(userPrincipal.getUserId());
            Map<String, String> response = new HashMap<>();
            response.put("apiKey", newApiKey);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Failed to refresh API key"));
        }
    }
    
    @GetMapping("/validate")
    public ResponseEntity<?> validateAuth(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok(Map.of("valid", false));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("username", authentication.getName());
        
        if (authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            response.put("userId", userPrincipal.getUserId());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/debug-token")
    public ResponseEntity<?> debugToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> debug = new HashMap<>();
        
        if (authHeader == null) {
            debug.put("error", "No Authorization header");
            return ResponseEntity.ok(debug);
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            debug.put("error", "Invalid Authorization header format");
            return ResponseEntity.ok(debug);
        }
        
        String token = authHeader.substring(7);
        debug.put("tokenPresent", true);
        debug.put("tokenLength", token.length());
        
        try {
            boolean isValid = authService.validateToken(token);
            debug.put("isValidToken", isValid);
            
            if (isValid) {
                String username = authService.getUsernameFromToken(token);
                String userId = authService.getUserIdFromToken(token);
                debug.put("username", username);
                debug.put("userId", userId);
                
                Optional<User> userOpt = userService.findByUsername(username);
                debug.put("userFound", userOpt.isPresent());
                if (userOpt.isPresent()) {
                    debug.put("userEnabled", userOpt.get().isEnabled());
                    debug.put("userRoles", userOpt.get().getRoles());
                }
            }
        } catch (Exception e) {
            debug.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(debug);
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}
