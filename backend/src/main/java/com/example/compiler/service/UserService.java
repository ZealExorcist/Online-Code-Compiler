package com.example.compiler.service;

import com.example.compiler.model.User;
import com.example.compiler.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User createUser(String username, String email, String password) {
        // Check if user already exists
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new user
        User user = new User(username, email, passwordEncoder.encode(password));
        user.setApiKey(generateApiKey());
        
        logger.info("Creating new user: {}", username);
        return userRepository.save(user);
    }
      public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey);
    }
    
    public User updateUserSettings(String userId, User.UserSettings settings) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setSettings(settings);
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }
    
    public String regenerateApiKey(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String newApiKey = generateApiKey();
            user.setApiKey(newApiKey);
            userRepository.save(user);
            return newApiKey;
        }
        throw new RuntimeException("User not found");
    }
    
    public boolean validatePassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }
    
    public User updatePassword(String userId, String oldPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (validatePassword(user, oldPassword)) {
                user.setPassword(passwordEncoder.encode(newPassword));
                return userRepository.save(user);
            } else {
                throw new RuntimeException("Invalid old password");
            }
        }
        throw new RuntimeException("User not found");
    }
    
    public void updateLastLogin(String userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setLastLoginAt(java.time.LocalDateTime.now());
            userRepository.save(user);
        }
    }
    
    private String generateApiKey() {
        String apiKey;
        do {
            apiKey = "oc_" + UUID.randomUUID().toString().replace("-", "");
        } while (userRepository.existsByApiKey(apiKey));
        return apiKey;
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public User upgradeTier(String userId, String newTier) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        user.setTier(newTier);
        
        logger.info("Upgrading user {} to tier: {}", user.getUsername(), newTier);
        return userRepository.save(user);
    }
    
    public String generateNewApiKey(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        String newApiKey = generateApiKey();
        user.setApiKey(newApiKey);
        
        logger.info("Generated new API key for user: {}", user.getUsername());
        userRepository.save(user);
        return newApiKey;
    }
}
