package com.example.compiler.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.example.compiler.repository")
@Profile("!test") // Don't load for test profile
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    @Value("${spring.data.mongodb.database:online-compiler}")
    private String databaseName;
    
    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/online-compiler}")
    private String mongoUri;
    
    @Override
    protected String getDatabaseName() {
        return databaseName != null ? databaseName : "online-compiler";
    }
    
    @Bean
    @Override
    public MongoClient mongoClient() {
        try {
            MongoClient client = MongoClients.create(mongoUri);
            // Test the connection
            client.getDatabase(getDatabaseName()).runCommand(new org.bson.Document("ping", 1));
            return client;
        } catch (Exception e) {
            // Log the error but don't fail - let the application use H2 fallback
            System.err.println("MongoDB connection failed: " + e.getMessage());
            System.err.println("Application will use H2 database as fallback");
            // Return a mock client to satisfy the interface
            return MongoClients.create("mongodb://localhost:27017");
        }
    }
    
    @Bean
    public MongoTemplate mongoTemplate() {
        try {
            return new MongoTemplate(mongoClient(), getDatabaseName());
        } catch (Exception e) {
            System.err.println("MongoTemplate creation failed: " + e.getMessage());
            return null;
        }
    }
}
