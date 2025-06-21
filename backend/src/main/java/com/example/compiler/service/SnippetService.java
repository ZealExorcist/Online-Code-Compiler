package com.example.compiler.service;

import com.example.compiler.model.Snippet;
import com.example.compiler.repository.SnippetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class SnippetService {
    
    @Autowired
    private SnippetRepository snippetRepository;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    public String saveSnippet(String code, String language) {
        String id = generateUniqueId();
        Snippet snippet = new Snippet(code, language);
        snippet.setId(id);
        snippetRepository.save(snippet);
        return id;
    }
    
    public String saveSnippet(String code, String language, String userId, String username) {
        String id = generateUniqueId();
        Snippet snippet = new Snippet(code, language, userId, username);
        snippet.setId(id);
        snippetRepository.save(snippet);
        return id;
    }
    
    public Snippet getSnippet(String id) {
        Optional<Snippet> snippet = snippetRepository.findById(id);
        return snippet.orElse(null);
    }
    
    public String generateShareUrl(String id) {
        return baseUrl + "/snippets/" + id;
    }
    
    private String generateUniqueId() {
        // Generate a shorter, more user-friendly ID
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    public boolean snippetExists(String id) {
        return snippetRepository.existsById(id);
    }
    
    public long getSnippetCount() {
        return snippetRepository.count();
    }
}
