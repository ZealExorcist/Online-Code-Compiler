package com.example.compiler.model;

public class ShareRequest {
    private String code;
    private String language;
    private String input;
    private String title;

    // Default constructor
    public ShareRequest() {}

    // Constructor with parameters
    public ShareRequest(String code, String language, String input, String title) {
        this.code = code;
        this.language = language;
        this.input = input;
        this.title = title;
    }

    // Getters and setters
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
}
