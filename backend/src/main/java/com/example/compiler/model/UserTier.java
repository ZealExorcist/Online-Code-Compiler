package com.example.compiler.model;

public enum UserTier {
    ANONYMOUS(60000, "Anonymous access - Sign up for higher limits", 5),
    BASIC(30000, "Basic Plan - 30 second compilation delay", 10),
    ADVANCED(15000, "Advanced Plan - 15 second compilation delay", 50),
    MASTER(0, "Master Plan - Instant compilation", 200);
    
    private final int delayMs;
    private final String description;
    private final int requestsPerHour;
    
    UserTier(int delayMs, String description, int requestsPerHour) {
        this.delayMs = delayMs;
        this.description = description;
        this.requestsPerHour = requestsPerHour;
    }
    
    public int getDelayMs() {
        return delayMs;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getRequestsPerHour() {
        return requestsPerHour;
    }
}
