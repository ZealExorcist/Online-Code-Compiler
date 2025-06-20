package com.example.compiler.controller;

import com.example.compiler.model.ShareRequest;
import com.example.compiler.model.ShareResponse;
import com.example.compiler.model.LoadResponse;
import com.example.compiler.service.ShareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ShareController {
    
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);
    
    @Autowired
    private ShareService shareService;
    
    @PostMapping("/share")
    public ResponseEntity<ShareResponse> createShare(@RequestBody ShareRequest request) {
        try {
            logger.info("Creating share for language: {}, title: {}", request.getLanguage(), request.getTitle());
            
            // Validate request
            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            ShareResponse response = shareService.createShareUrl(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error creating share", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/load/{shareId}")
    public ResponseEntity<LoadResponse> loadShare(@PathVariable String shareId) {
        try {
            logger.info("Loading share with ID: {}", shareId);
            
            LoadResponse response = shareService.loadFromShareId(shareId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error loading share", e);
            LoadResponse errorResponse = new LoadResponse("Failed to load shared code");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/share/info")
    public ResponseEntity<String> getShareInfo() {
        return ResponseEntity.ok("URL-based code sharing service. No data is stored on the server.");
    }
}
