package com.example.compiler.model;

public class ExecuteRequest {
    private String code;
    private String language;
    private String input; // Optional input for the program
    
    public ExecuteRequest() {}
    
    public ExecuteRequest(String code, String language) {
        this.code = code;
        this.language = language;
    }
    
    public ExecuteRequest(String code, String language, String input) {
        this.code = code;
        this.language = language;
        this.input = input;
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
    
    public String getInput() {
        return input;
    }
    
    public void setInput(String input) {
        this.input = input;
    }
}
