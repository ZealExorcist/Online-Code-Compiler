package com.example.compiler.security;

import com.example.compiler.model.User;
import com.example.compiler.service.AuthService;
import com.example.compiler.service.RateLimitingService;
import com.example.compiler.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RateLimitingService rateLimitingService;
      @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check rate limiting for authenticated endpoints
        String clientIdentifier = getClientIdentifier(request);
        if (!rateLimitingService.tryConsume(clientIdentifier)) {
            logger.warn("Rate limit exceeded for client: {}", clientIdentifier);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}");
            
            // Add rate limit headers
            RateLimitingService.RateLimitInfo info = rateLimitingService.getRateLimitInfo(clientIdentifier);
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(info.getAvailableTokens()));
            response.setHeader("X-Rate-Limit-Burst", String.valueOf(info.getBurstCapacity()));
            return;
        }        // Try JWT authentication first
        String token = extractJwtFromHeader(request);
        if (token != null) {
            if (authService.validateToken(token)) {
                authenticateWithJwt(request, token);
            }
        } else {
            // Try API key authentication
            String apiKey = extractApiKeyFromHeader(request);
            if (apiKey != null) {
                if (authService.validateApiKey(apiKey)) {
                    authenticateWithApiKey(request, apiKey);
                }
            }
        }
        
        // Add rate limit headers to response
        RateLimitingService.RateLimitInfo info = rateLimitingService.getRateLimitInfo(clientIdentifier);
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(info.getAvailableTokens()));
        response.setHeader("X-Rate-Limit-Burst", String.valueOf(info.getBurstCapacity()));
        
        filterChain.doFilter(request, response);
    }
      private boolean isPublicEndpoint(String path) {
        return path.equals("/api/auth/login") ||
               path.equals("/api/auth/register") ||
               path.equals("/api/auth/signup") ||
               path.equals("/api/auth/debug-token") ||
               path.startsWith("/api/public/") ||
               path.equals("/actuator/health") ||
               path.startsWith("/h2-console/");
    }
    
    private String getClientIdentifier(HttpServletRequest request) {
        // Use authenticated user ID if available, otherwise use IP address
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return "user:" + principal.getUserId();
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return "ip:" + xForwardedFor.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }
    
    private String extractJwtFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
    
    private String extractApiKeyFromHeader(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = request.getParameter("api_key");
        }
        return apiKey;
    }    private void authenticateWithJwt(HttpServletRequest request, String token) {
        try {
            String username = authService.getUsernameFromToken(token);
            String userId = authService.getUserIdFromToken(token);
            
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                if (user.isEnabled()) {
                    UserPrincipal userPrincipal = new UserPrincipal(user);
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userPrincipal, 
                            null, 
                            user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList())
                        );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication from JWT: {}", e.getMessage());
        }
    }
    
    private void authenticateWithApiKey(HttpServletRequest request, String apiKey) {
        try {
            Optional<User> userOptional = authService.getUserByApiKey(apiKey);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (user.isEnabled()) {
                    UserPrincipal userPrincipal = new UserPrincipal(user);
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userPrincipal, 
                            null, 
                            user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList())
                        );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication from API key: {}", e.getMessage());
        }
    }
}
