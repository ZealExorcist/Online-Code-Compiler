package com.example.compiler.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String username;
    
    @Indexed(unique = true)
    private String email;
    
    private String password;
    
    @Indexed(unique = true)
    private String apiKey;
      private List<String> roles = new ArrayList<>();
    
    private boolean enabled = true;
    
    private String tier = "BASIC"; // BASIC, ADVANCED, MASTER
    
    private UserSettings settings = new UserSettings();
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime lastLoginAt;
      private List<String> snippetIds = new ArrayList<>();
    
    private int shareCount = 0; // Track number of shares created
    private LocalDateTime shareCountResetDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0); // Daily reset
    
    private Map<String, Object> metadata = new HashMap<>();
    
    // Constructors
    public User() {}
    
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles.add("USER");
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getTier() {
        return tier;
    }
    
    public void setTier(String tier) {
        this.tier = tier;
    }
    
    public UserSettings getSettings() {
        return settings;
    }
    
    public void setSettings(UserSettings settings) {
        this.settings = settings;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public List<String> getSnippetIds() {
        return snippetIds;
    }
    
    public void setSnippetIds(List<String> snippetIds) {
        this.snippetIds = snippetIds;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    // Helper methods
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
    
    public void addRole(String role) {
        if (!roles.contains(role)) {
            roles.add(role);
        }
    }
      public void addSnippetId(String snippetId) {
        if (!snippetIds.contains(snippetId)) {
            snippetIds.add(snippetId);
        }
    }
    
    public int getShareCount() {
        return shareCount;
    }
    
    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }
    
    public LocalDateTime getShareCountResetDate() {
        return shareCountResetDate;
    }
    
    public void setShareCountResetDate(LocalDateTime shareCountResetDate) {
        this.shareCountResetDate = shareCountResetDate;
    }
    
    public void incrementShareCount() {
        this.shareCount++;
    }
    
    public void resetShareCountIfNeeded() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (shareCountResetDate.isBefore(today)) {
            this.shareCount = 0;
            this.shareCountResetDate = today;
        }
    }
    
    public boolean canShare() {
        resetShareCountIfNeeded();
        
        // Premium and advanced users have unlimited shares
        if ("ADVANCED".equals(tier) || "MASTER".equals(tier)) {
            return true;
        }
        
        // Basic users have daily limit of 3 shares
        return shareCount < 3;
    }
    
    public int getRemainingShares() {
        resetShareCountIfNeeded();
        
        // Premium and advanced users have unlimited shares
        if ("ADVANCED".equals(tier) || "MASTER".equals(tier)) {
            return -1; // Indicate unlimited
        }
        
        // Basic users have daily limit of 3 shares
        return Math.max(0, 3 - shareCount);
    }
    
    public static class UserSettings {
        private String theme = "dark"; // dark, light
        private String fontSize = "14px";
        private String fontFamily = "JetBrains Mono, Monaco, 'Courier New', monospace";
        private boolean enableAutoComplete = true;
        private boolean enableLineNumbers = true;
        private boolean enableFolding = true;
        private String defaultLanguage = "python";
        private boolean enableKeyboardShortcuts = true;
        private int tabSize = 4;
        private boolean insertSpaces = true;
        private boolean wordWrap = false;
        private boolean minimap = false;
        private String colorScheme = "oneDark";
        
        // Execution Settings
        private int maxExecutionTime = 30; // seconds
        private int maxOutputSize = 1024 * 10; // 10KB
        private boolean enableInput = true;
        
        // Privacy Settings
        private boolean publicSnippets = false;
        private boolean shareByDefault = false;
        
        // Getters and Setters
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        
        public String getFontSize() { return fontSize; }
        public void setFontSize(String fontSize) { this.fontSize = fontSize; }
        
        public String getFontFamily() { return fontFamily; }
        public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
        
        public boolean isEnableAutoComplete() { return enableAutoComplete; }
        public void setEnableAutoComplete(boolean enableAutoComplete) { this.enableAutoComplete = enableAutoComplete; }
        
        public boolean isEnableLineNumbers() { return enableLineNumbers; }
        public void setEnableLineNumbers(boolean enableLineNumbers) { this.enableLineNumbers = enableLineNumbers; }
        
        public boolean isEnableFolding() { return enableFolding; }
        public void setEnableFolding(boolean enableFolding) { this.enableFolding = enableFolding; }
        
        public String getDefaultLanguage() { return defaultLanguage; }
        public void setDefaultLanguage(String defaultLanguage) { this.defaultLanguage = defaultLanguage; }
        
        public boolean isEnableKeyboardShortcuts() { return enableKeyboardShortcuts; }
        public void setEnableKeyboardShortcuts(boolean enableKeyboardShortcuts) { this.enableKeyboardShortcuts = enableKeyboardShortcuts; }
        
        public int getTabSize() { return tabSize; }
        public void setTabSize(int tabSize) { this.tabSize = tabSize; }
        
        public boolean isInsertSpaces() { return insertSpaces; }
        public void setInsertSpaces(boolean insertSpaces) { this.insertSpaces = insertSpaces; }
        
        public boolean isWordWrap() { return wordWrap; }
        public void setWordWrap(boolean wordWrap) { this.wordWrap = wordWrap; }
        
        public boolean isMinimap() { return minimap; }
        public void setMinimap(boolean minimap) { this.minimap = minimap; }
        
        public String getColorScheme() { return colorScheme; }
        public void setColorScheme(String colorScheme) { this.colorScheme = colorScheme; }
        
        public int getMaxExecutionTime() { return maxExecutionTime; }
        public void setMaxExecutionTime(int maxExecutionTime) { this.maxExecutionTime = maxExecutionTime; }
        
        public int getMaxOutputSize() { return maxOutputSize; }
        public void setMaxOutputSize(int maxOutputSize) { this.maxOutputSize = maxOutputSize; }
        
        public boolean isEnableInput() { return enableInput; }
        public void setEnableInput(boolean enableInput) { this.enableInput = enableInput; }
        
        public boolean isPublicSnippets() { return publicSnippets; }
        public void setPublicSnippets(boolean publicSnippets) { this.publicSnippets = publicSnippets; }
        
        public boolean isShareByDefault() { return shareByDefault; }
        public void setShareByDefault(boolean shareByDefault) { this.shareByDefault = shareByDefault; }
    }
}
