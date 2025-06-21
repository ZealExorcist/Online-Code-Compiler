package com.example.compiler.service;

import com.example.compiler.model.Snippet;
import com.example.compiler.model.User;
import com.example.compiler.repository.SnippetRepository;
import com.example.compiler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SnippetService {
    
    @Autowired
    private SnippetRepository snippetRepository;
    
    @Autowired
    private UserRepository userRepository;
    
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
      public List<Snippet> getUserSnippets(String userId) {
        return snippetRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }
    
    public Snippet saveUserSnippet(String userId, String title, String code, String language, String input) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        String id = generateUniqueId();
        
        Snippet snippet = new Snippet();
        snippet.setId(id);
        snippet.setTitle(title);
        snippet.setCode(code);
        snippet.setLanguage(language);
        snippet.setInput(input);
        snippet.setUserId(userId);
        snippet.setUsername(user.getUsername());
        snippet.setCreatedAt(LocalDateTime.now());
        snippet.setUpdatedAt(LocalDateTime.now());
        
        // Add snippet ID to user's snippet list
        user.getSnippetIds().add(id);
        userRepository.save(user);
        
        return snippetRepository.save(snippet);
    }
    
    public boolean deleteUserSnippet(String userId, String snippetId) {
        Optional<Snippet> snippetOpt = snippetRepository.findById(snippetId);
        if (snippetOpt.isEmpty()) {
            return false;
        }
        
        Snippet snippet = snippetOpt.get();
        if (!userId.equals(snippet.getUserId())) {
            return false; // User doesn't own this snippet
        }
        
        // Remove snippet ID from user's snippet list
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.getSnippetIds().remove(snippetId);
            userRepository.save(user);
        }
        
        snippetRepository.deleteById(snippetId);
        return true;
    }
}
