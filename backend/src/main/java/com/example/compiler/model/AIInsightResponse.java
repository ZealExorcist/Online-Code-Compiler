package com.example.compiler.model;

import java.util.List;

public class AIInsightResponse {
    private String insight;
    private String category;
    private List<String> suggestions;
    private boolean hasSecurityIssues;
    private String error;

    public AIInsightResponse() {}

    public AIInsightResponse(String insight, String category, List<String> suggestions, boolean hasSecurityIssues) {
        this.insight = insight;
        this.category = category;
        this.suggestions = suggestions;
        this.hasSecurityIssues = hasSecurityIssues;
    }

    public AIInsightResponse(String error) {
        this.error = error;
    }

    public String getInsight() {
        return insight;
    }

    public void setInsight(String insight) {
        this.insight = insight;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public boolean isHasSecurityIssues() {
        return hasSecurityIssues;
    }

    public void setHasSecurityIssues(boolean hasSecurityIssues) {
        this.hasSecurityIssues = hasSecurityIssues;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
