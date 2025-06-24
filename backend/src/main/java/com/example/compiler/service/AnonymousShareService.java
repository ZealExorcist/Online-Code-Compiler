package com.example.compiler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnonymousShareService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnonymousShareService.class);
    private static final int DAILY_SHARE_LIMIT = 3;
    
    // Simple in-memory storage for anonymous share tracking
    // In production, this should be moved to Redis or database
    private final Map<String, ShareTracker> ipShareTracking = new ConcurrentHashMap<>();
    
    public boolean canShare(String clientIp) {
        ShareTracker tracker = getOrCreateTracker(clientIp);
        tracker.resetIfNewDay();
        return tracker.shareCount < DAILY_SHARE_LIMIT;
    }
    
    public void recordShare(String clientIp) {
        ShareTracker tracker = getOrCreateTracker(clientIp);
        tracker.resetIfNewDay();
        tracker.shareCount++;
        logger.info("Anonymous IP {} has used {} shares today", clientIp, tracker.shareCount);
    }
    
    public int getRemainingShares(String clientIp) {
        ShareTracker tracker = getOrCreateTracker(clientIp);
        tracker.resetIfNewDay();
        return Math.max(0, DAILY_SHARE_LIMIT - tracker.shareCount);
    }
    
    public int getUsedShares(String clientIp) {
        ShareTracker tracker = getOrCreateTracker(clientIp);
        tracker.resetIfNewDay();
        return tracker.shareCount;
    }
    
    private ShareTracker getOrCreateTracker(String clientIp) {
        return ipShareTracking.computeIfAbsent(clientIp, k -> new ShareTracker());
    }
    
    private static class ShareTracker {
        int shareCount = 0;
        LocalDateTime lastResetDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        void resetIfNewDay() {
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            if (lastResetDate.isBefore(today)) {
                shareCount = 0;
                lastResetDate = today;
            }
        }
    }
}
