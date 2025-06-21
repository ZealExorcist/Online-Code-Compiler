package com.example.compiler.service;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {
    
    @Value("${app.rate-limit.requests-per-minute:10}")
    private int requestsPerMinute;
    
    @Value("${app.rate-limit.burst-capacity:20}")
    private int burstCapacity;
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    public Bucket getBucket(String identifier) {
        return buckets.computeIfAbsent(identifier, this::createBucket);
    }
    
    private Bucket createBucket(String identifier) {
        // Allow requestsPerMinute requests per minute with burst capacity
        Bandwidth limit = Bandwidth.classic(burstCapacity, Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    public boolean tryConsume(String identifier) {
        return getBucket(identifier).tryConsume(1);
    }
    
    public boolean tryConsume(String identifier, long tokens) {
        return getBucket(identifier).tryConsume(tokens);
    }
    
    public long getAvailableTokens(String identifier) {
        return getBucket(identifier).getAvailableTokens();
    }
    
    // Clear bucket for testing or admin purposes
    public void clearBucket(String identifier) {
        buckets.remove(identifier);
    }
    
    // Get rate limit info
    public RateLimitInfo getRateLimitInfo(String identifier) {
        Bucket bucket = getBucket(identifier);
        return new RateLimitInfo(
            bucket.getAvailableTokens(),
            burstCapacity,
            requestsPerMinute
        );
    }
    
    public static class RateLimitInfo {
        private final long availableTokens;
        private final int burstCapacity;
        private final int requestsPerMinute;
        
        public RateLimitInfo(long availableTokens, int burstCapacity, int requestsPerMinute) {
            this.availableTokens = availableTokens;
            this.burstCapacity = burstCapacity;
            this.requestsPerMinute = requestsPerMinute;
        }
        
        public long getAvailableTokens() { return availableTokens; }
        public int getBurstCapacity() { return burstCapacity; }
        public int getRequestsPerMinute() { return requestsPerMinute; }
    }
}
