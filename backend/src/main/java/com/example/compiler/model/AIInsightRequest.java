package com.example.compiler.model;

public class AIInsightRequest {
    private String code;
    private String language;
    private String userApiKey;

    public AIInsightRequest() {}

    public AIInsightRequest(String code, String language) {
        this.code = code;
        this.language = language;
    }

    public AIInsightRequest(String code, String language, String userApiKey) {
        this.code = code;
        this.language = language;
        this.userApiKey = userApiKey;
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

    public String getUserApiKey() {
        return userApiKey;
    }

    public void setUserApiKey(String userApiKey) {
        this.userApiKey = userApiKey;
    }
}
