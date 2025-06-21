package com.example.compiler.service;

import com.example.compiler.model.UserTier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class RateLimitService {
    
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<LocalDateTime>> userRequests = new ConcurrentHashMap<>();
    
    public boolean isRateLimited(String userId, UserTier tier) {
        ConcurrentLinkedQueue<LocalDateTime> requests = userRequests.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>());
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        
        // Remove requests older than 1 hour
        requests.removeIf(timestamp -> timestamp.isBefore(oneHourAgo));
        
        // Check if user has exceeded rate limit
        if (requests.size() >= tier.getRequestsPerHour()) {
            return true;
        }
        
        // Add current request
        requests.offer(now);
        return false;
    }
    
    public int getRemainingRequests(String userId, UserTier tier) {
        ConcurrentLinkedQueue<LocalDateTime> requests = userRequests.get(userId);
        if (requests == null) {
            return tier.getRequestsPerHour();
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        
        // Count requests in the last hour
        long recentRequests = requests.stream()
            .filter(timestamp -> timestamp.isAfter(oneHourAgo))
            .count();
            
        return Math.max(0, tier.getRequestsPerHour() - (int) recentRequests);
    }
}
