package com.example.compiler.config;

import com.example.compiler.model.User;
import com.example.compiler.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UserService userService;
    
    @Value("${app.admin.username:admin}")
    private String adminUsername;
    
    @Value("${app.admin.password:admin123}")
    private String adminPassword;
    
    @Value("${app.admin.email:admin@compiler.com}")
    private String adminEmail;
    
    @Override
    public void run(String... args) throws Exception {
        createDefaultAdmin();
    }
      private void createDefaultAdmin() {
        try {
            if (!userService.existsByUsername(adminUsername)) {
                User admin = userService.createUser(adminUsername, adminEmail, adminPassword);
                admin.addRole("ADMIN");
                admin.addRole("USER");
                
                // Save the user again to persist the roles
                userService.saveUser(admin);
                
                logger.info("Default admin user created: {}", adminUsername);
                logger.info("Admin API Key: {}", admin.getApiKey());
            } else {
                logger.info("Admin user already exists: {}", adminUsername);
            }
        } catch (Exception e) {
            logger.error("Failed to create default admin user", e);
        }
    }
}
