package com.example.compiler.repository;

import com.example.compiler.model.Snippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SnippetRepository extends MongoRepository<Snippet, String> {
    List<Snippet> findByUserId(String userId);
    List<Snippet> findByUserIdOrderByUpdatedAtDesc(String userId);
    Page<Snippet> findByUserId(String userId, Pageable pageable);
    
    List<Snippet> findByPublicSnippetTrue();
    Page<Snippet> findByPublicSnippetTrue(Pageable pageable);
    
    List<Snippet> findBySharedTrue();
    Page<Snippet> findBySharedTrue(Pageable pageable);
    
    Optional<Snippet> findByIdAndUserId(String id, String userId);
    
    @Query("{ '$or': [ { 'publicSnippet': true }, { 'shared': true }, { 'userId': ?0 } ] }")
    Page<Snippet> findAccessibleSnippets(String userId, Pageable pageable);
    
    @Query("{ 'language': ?0, '$or': [ { 'publicSnippet': true }, { 'shared': true }, { 'userId': ?1 } ] }")
    Page<Snippet> findByLanguageAndAccessible(String language, String userId, Pageable pageable);
    
    long countByUserId(String userId);
    long countByPublicSnippetTrue();
}
