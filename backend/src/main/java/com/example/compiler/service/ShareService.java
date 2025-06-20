package com.example.compiler.service;

import com.example.compiler.model.ShareRequest;
import com.example.compiler.model.ShareResponse;
import com.example.compiler.model.LoadResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Service
public class ShareService {
    
    private static final Logger logger = LoggerFactory.getLogger(ShareService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;
      public ShareResponse createShareUrl(ShareRequest request) {
        try {
            // Convert request to JSON
            String json = objectMapper.writeValueAsString(request);
            logger.info("Original JSON size: {} bytes", json.length());
            
            // Compress the JSON
            byte[] compressed = compress(json);
            logger.info("Compressed size: {} bytes", compressed.length);
              // Encode to Base64 URL-safe
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(compressed);
            logger.info("Base64 encoded size: {} characters", encoded.length());
            
            // Create URLs - both use the same encoded data
            String shareUrl = baseUrl + "/share/" + encoded;
            String shortUrl = baseUrl + "/s/" + encoded;  // Use encoded data for consistency
            
            logger.info("Generated share URL with {} characters", shareUrl.length());
            
            return new ShareResponse(shareUrl, shortUrl, encoded);  // Return encoded as shareId
            
        } catch (Exception e) {
            logger.error("Error creating share URL", e);
            throw new RuntimeException("Failed to create share URL: " + e.getMessage());
        }
    }
    
    public LoadResponse loadFromShareId(String shareId) {
        try {
            // Decode from Base64
            byte[] compressed = Base64.getUrlDecoder().decode(shareId);
            
            // Decompress
            String json = decompress(compressed);
            
            // Parse JSON back to ShareRequest
            ShareRequest request = objectMapper.readValue(json, ShareRequest.class);
            
            return new LoadResponse(
                request.getCode(),
                request.getLanguage(),
                request.getInput(),
                request.getTitle()
            );
            
        } catch (Exception e) {
            logger.error("Error loading from share ID: {}", shareId, e);
            return new LoadResponse("Invalid or corrupted share link");
        }
    }
    
    private byte[] compress(String data) throws IOException {
        byte[] input = data.getBytes(StandardCharsets.UTF_8);
        
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(input);
        deflater.finish();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        
        deflater.end();
        return outputStream.toByteArray();
    }
    
    private String decompress(byte[] compressed) throws Exception {
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
          inflater.end();
        return outputStream.toString(StandardCharsets.UTF_8);
    }
}
