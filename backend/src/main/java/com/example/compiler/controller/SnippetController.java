package com.example.compiler.controller;

import com.example.compiler.model.Snippet;
import com.example.compiler.model.SnippetResponse;
import com.example.compiler.service.SnippetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // For development - should be configured properly in production
public class SnippetController {
    
    private static final Logger logger = LoggerFactory.getLogger(SnippetController.class);
    
    @Autowired
    private SnippetService snippetService;
    
    @PostMapping("/snippets")
    public ResponseEntity<SnippetResponse> createSnippet(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String language = request.get("language");
        
        logger.info("Creating snippet for language: {}", language);
        
        // Validate request
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        if (language == null || language.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Limit code length
        if (code.length() > 50000) { // 50KB limit for storage
            return ResponseEntity.badRequest().build();
        }
        
        try {
            String snippetId = snippetService.saveSnippet(code, language);
            String shareUrl = snippetService.generateShareUrl(snippetId);
            
            SnippetResponse response = new SnippetResponse(snippetId, shareUrl);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error creating snippet", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/snippets/{id}")
    public ResponseEntity<Snippet> getSnippet(@PathVariable String id) {
        logger.info("Retrieving snippet with ID: {}", id);
        
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Snippet snippet = snippetService.getSnippet(id);
            
            if (snippet == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(snippet);
            
        } catch (Exception e) {
            logger.error("Error retrieving snippet", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
