package com.example.compiler.model;

public class LoadResponse {
    private String code;
    private String language;
    private String input;
    private String title;
    private boolean success;
    private String error;

    // Default constructor
    public LoadResponse() {}

    // Constructor for success
    public LoadResponse(String code, String language, String input, String title) {
        this.code = code;
        this.language = language;
        this.input = input;
        this.title = title;
        this.success = true;
    }

    // Constructor for error
    public LoadResponse(String error) {
        this.error = error;
        this.success = false;
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
