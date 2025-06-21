package com.example.compiler.model;

public class SignupResponse {
    private boolean success;
    private String message;
    private String userId;
    private String username;
    private String email;
    
    // Constructors
    public SignupResponse() {}
    
    public SignupResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public SignupResponse(boolean success, String message, String userId, String username, String email) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
    
    // Static factory methods
    public static SignupResponse success(String userId, String username, String email) {
        return new SignupResponse(true, "User registered successfully", userId, username, email);
    }
    
    public static SignupResponse error(String message) {
        return new SignupResponse(false, message);
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
