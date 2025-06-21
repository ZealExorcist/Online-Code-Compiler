package com.example.compiler.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Document(collection = "snippets")
public class Snippet {
    @Id
    private String id;
    
    private String code;
    private String language;
    private String input;
    private String title;
    private String description;
    
    @Indexed
    private String userId; // Owner of the snippet
    
    @Indexed
    private String username; // For easy display
    
    private boolean publicSnippet = false;
    private boolean shared = false;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime lastExecutedAt;
    
    private int executionCount = 0;
    private int viewCount = 0;
    
    private Map<String, Object> metadata = new HashMap<>();
    
    // Constructors
    public Snippet() {}
    
    public Snippet(String code, String language) {
        this.code = code;
        this.language = language;
    }
    
    public Snippet(String code, String language, String userId, String username) {
        this.code = code;
        this.language = language;
        this.userId = userId;
        this.username = username;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getInput() {
        return input;
    }
    
    public void setInput(String input) {
        this.input = input;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public boolean isPublicSnippet() {
        return publicSnippet;
    }
    
    public void setPublicSnippet(boolean publicSnippet) {
        this.publicSnippet = publicSnippet;
    }
    
    public boolean isShared() {
        return shared;
    }
    
    public void setShared(boolean shared) {
        this.shared = shared;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastExecutedAt() {
        return lastExecutedAt;
    }
    
    public void setLastExecutedAt(LocalDateTime lastExecutedAt) {
        this.lastExecutedAt = lastExecutedAt;
    }
    
    public int getExecutionCount() {
        return executionCount;
    }
    
    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
    }
    
    public int getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    // Helper methods
    public void incrementExecutionCount() {
        this.executionCount++;
        this.lastExecutedAt = LocalDateTime.now();
    }
    
    public void incrementViewCount() {
        this.viewCount++;
    }
      public boolean isOwnedBy(String userId) {
        return this.userId != null && this.userId.equals(userId);
    }
    
    public boolean canBeViewedBy(String userId) {
        return this.publicSnippet || this.shared || this.isOwnedBy(userId);
    }
}
