package com.example.compiler.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExecuteResponse {
    private String stdout;
    private String stderr;
    private int exitCode;
    private String error; // For compilation or runtime errors
    private long executionTime; // In milliseconds
    
    public ExecuteResponse() {}
    
    public ExecuteResponse(String stdout, String stderr, int exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }
    
    public ExecuteResponse(String stdout, String stderr, int exitCode, long executionTime) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
        this.executionTime = executionTime;
    }
    
    // Static method for error responses
    public static ExecuteResponse error(String error) {
        ExecuteResponse response = new ExecuteResponse();
        response.setError(error);
        response.setExitCode(-1);
        return response;
    }
    
    public String getStdout() {
        return stdout;
    }
    
    public void setStdout(String stdout) {
        this.stdout = stdout;
    }
    
    public String getStderr() {
        return stderr;
    }
    
    public void setStderr(String stderr) {
        this.stderr = stderr;
    }
    
    public int getExitCode() {
        return exitCode;
    }
    
    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
}
