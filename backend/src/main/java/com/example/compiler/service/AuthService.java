package com.example.compiler.service;

import com.example.compiler.model.AuthRequest;
import com.example.compiler.model.AuthResponse;
import com.example.compiler.model.User;
import com.example.compiler.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthResponse login(AuthRequest request) {
        Optional<User> optionalUser = userService.findByUsername(request.getUsername());
        
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            if (userService.validatePassword(user, request.getPassword())) {
                if (!user.isEnabled()) {
                    throw new RuntimeException("Account is disabled");
                }
                
                // Update last login
                userService.updateLastLogin(user.getId());
                
                // Generate JWT token
                String token = jwtUtil.generateToken(user.getUsername(), user.getId());
                
                logger.info("User {} logged in successfully", user.getUsername());
                
                return new AuthResponse(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getApiKey(),
                    jwtUtil.getExpirationTime()
                );
            }
        }
        
        logger.warn("Failed login attempt for username: {}", request.getUsername());
        throw new RuntimeException("Invalid username or password");
    }
    
    public AuthResponse register(AuthRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required for registration");
        }
        
        try {
            User user = userService.createUser(
                request.getUsername().trim(),
                request.getEmail().trim(),
                request.getPassword()
            );
            
            // Generate JWT token for immediate login
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());
            
            logger.info("User {} registered successfully", user.getUsername());
            
            return new AuthResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getApiKey(),
                jwtUtil.getExpirationTime()
            );
            
        } catch (Exception e) {
            logger.error("Registration failed for username: {}", request.getUsername(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }
    
    public boolean validateApiKey(String apiKey) {
        return userService.findByApiKey(apiKey).isPresent();
    }
    
    public Optional<User> getUserByApiKey(String apiKey) {
        return userService.findByApiKey(apiKey);
    }
    
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
    
    public String getUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }
    
    public String getUserIdFromToken(String token) {
        return jwtUtil.getUserIdFromToken(token);
    }
}
