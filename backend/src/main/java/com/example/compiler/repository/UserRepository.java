package com.example.compiler.repository;

import com.example.compiler.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByApiKey(String apiKey);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByApiKey(String apiKey);
}
