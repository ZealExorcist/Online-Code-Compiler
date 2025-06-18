package com.example.compiler.controller;

import com.example.compiler.model.ExecuteRequest;
import com.example.compiler.model.ExecuteResponse;
import com.example.compiler.service.ExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // For development - should be configured properly in production
public class ExecuteController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExecuteController.class);
    
    @Autowired
    private ExecutionService executionService;
    
    @PostMapping("/execute")
    public ResponseEntity<ExecuteResponse> executeCode(@RequestBody ExecuteRequest request) {
        logger.info("Received execution request for language: {}", request.getLanguage());
        
        // Validate request
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ExecuteResponse.error("Code cannot be empty"));
        }
        
        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ExecuteResponse.error("Language must be specified"));
        }
        
        // Limit code length (e.g., 10KB)
        if (request.getCode().length() > 10240) {
            return ResponseEntity.badRequest()
                .body(ExecuteResponse.error("Code exceeds maximum length of 10KB"));
        }
        
        try {
            ExecuteResponse response = executionService.executeCode(
                request.getCode(),
                request.getLanguage(),
                request.getInput()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error executing code", e);
            return ResponseEntity.internalServerError()
                .body(ExecuteResponse.error("Internal server error"));
        }
    }
    
    @GetMapping("/languages")
    public ResponseEntity<Set<String>> getSupportedLanguages() {
        Set<String> languages = executionService.getSupportedLanguages();
        return ResponseEntity.ok(languages);
    }
}
