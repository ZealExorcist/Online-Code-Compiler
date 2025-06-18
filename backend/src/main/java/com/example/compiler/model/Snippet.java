package com.example.compiler.model;

import java.time.LocalDateTime;

public class Snippet {
    private String id;
    private String code;
    private String language;
    private LocalDateTime createdAt;
    
    public Snippet() {}
    
    public Snippet(String id, String code, String language) {
        this.id = id;
        this.code = code;
        this.language = language;
        this.createdAt = LocalDateTime.now();
    }
    
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
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
