package com.example.compiler.service;

import com.example.compiler.model.Snippet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class SnippetService {
    
    private final Map<String, Snippet> snippetStore = new ConcurrentHashMap<>();
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    public String saveSnippet(String code, String language) {
        String id = generateUniqueId();
        Snippet snippet = new Snippet(id, code, language);
        snippetStore.put(id, snippet);
        return id;
    }
    
    public Snippet getSnippet(String id) {
        return snippetStore.get(id);
    }
    
    public String generateShareUrl(String id) {
        return baseUrl + "/snippets/" + id;
    }
    
    private String generateUniqueId() {
        // Generate a shorter, more user-friendly ID
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    public boolean snippetExists(String id) {
        return snippetStore.containsKey(id);
    }
    
    public int getSnippetCount() {
        return snippetStore.size();
    }
}
