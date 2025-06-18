package com.example.compiler.service;

import com.example.compiler.model.ExecuteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ExecutionService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExecutionService.class);
    
    // Supported languages and their Docker configurations
    private static final Map<String, LanguageConfig> LANGUAGE_CONFIGS = Map.of(
        "python", new LanguageConfig("python:3.9-slim", "python", "main.py"),
        "java", new LanguageConfig("openjdk:11-slim", "javac Main.java && java Main", "Main.java"),
        "cpp", new LanguageConfig("gcc:latest", "g++ -o main main.cpp && ./main", "main.cpp"),
        "c", new LanguageConfig("gcc:latest", "gcc -o main main.c && ./main", "main.c"),
        "javascript", new LanguageConfig("node:16-slim", "node", "main.js"),
        "go", new LanguageConfig("golang:1.19-alpine", "go run main.go", "main.go"),
        "rust", new LanguageConfig("rust:1.65-slim", "rustc main.rs && ./main", "main.rs"),
        "ruby", new LanguageConfig("ruby:3.0-slim", "ruby", "main.rb"),
        "r", new LanguageConfig("r-base:latest", "Rscript", "main.R"),
        "csharp", new LanguageConfig("mcr.microsoft.com/dotnet/sdk:6.0", "dotnet run", "Program.cs")
    );
    
    private static final int TIMEOUT_SECONDS = 10;
    private static final String MEMORY_LIMIT = "128m";
    private static final String CPU_LIMIT = "0.5";
    
    public ExecuteResponse executeCode(String code, String language, String input) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate language
            LanguageConfig config = LANGUAGE_CONFIGS.get(language.toLowerCase());
            if (config == null) {
                return ExecuteResponse.error("Unsupported language: " + language);
            }
            
            // Create temporary directory for execution
            Path tempDir = Files.createTempDirectory("code_execution_");
            logger.info("Created temporary directory: {}", tempDir);
            
            try {
                // Write code to file
                Path codeFile = tempDir.resolve(config.fileName);
                Files.write(codeFile, code.getBytes(), StandardOpenOption.CREATE);
                
                // Write input to file if provided
                Path inputFile = null;
                if (input != null && !input.isEmpty()) {
                    inputFile = tempDir.resolve("input.txt");
                    Files.write(inputFile, input.getBytes(), StandardOpenOption.CREATE);
                }
                
                // Execute in Docker container
                ExecuteResponse response = runInDockerContainer(tempDir, config, inputFile != null);
                response.setExecutionTime(System.currentTimeMillis() - startTime);
                
                return response;
                
            } finally {
                // Clean up temporary directory
                deleteDirectory(tempDir.toFile());
            }
            
        } catch (Exception e) {
            logger.error("Error executing code", e);
            return ExecuteResponse.error("Internal server error: " + e.getMessage());
        }
    }
    
    private ExecuteResponse runInDockerContainer(Path workDir, LanguageConfig config, boolean hasInput) {
        try {
            List<String> dockerCommand = new ArrayList<>();
            dockerCommand.addAll(Arrays.asList(
                "docker", "run",
                "--rm",
                "--memory=" + MEMORY_LIMIT,
                "--cpus=" + CPU_LIMIT,
                "--network=none",
                "--pids-limit=50",
                "--read-only",
                "--tmpfs=/tmp:exec",
                "-v", workDir.toString() + ":/workspace:ro",
                "-w", "/workspace"
            ));
            
            if (hasInput) {
                dockerCommand.addAll(Arrays.asList("--interactive"));
            }
            
            dockerCommand.add(config.dockerImage);
            dockerCommand.addAll(Arrays.asList("sh", "-c", config.command));
            
            logger.info("Executing Docker command: {}", String.join(" ", dockerCommand));
            
            ProcessBuilder pb = new ProcessBuilder(dockerCommand);
            pb.directory(workDir.toFile());
            
            Process process = pb.start();
            
            // Handle input if provided
            if (hasInput) {
                try (PrintWriter writer = new PrintWriter(process.getOutputStream())) {
                    Path inputFile = workDir.resolve("input.txt");
                    if (Files.exists(inputFile)) {
                        String inputContent = Files.readString(inputFile);
                        writer.print(inputContent);
                        writer.flush();
                    }
                }
            }
            
            // Wait for completion with timeout
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                return ExecuteResponse.error("Execution timeout after " + TIMEOUT_SECONDS + " seconds");
            }
            
            // Read output
            String stdout = readStream(process.getInputStream());
            String stderr = readStream(process.getErrorStream());
            int exitCode = process.exitValue();
            
            logger.info("Execution completed with exit code: {}", exitCode);
            
            return new ExecuteResponse(stdout, stderr, exitCode);
            
        } catch (Exception e) {
            logger.error("Error running Docker container", e);
            return ExecuteResponse.error("Docker execution error: " + e.getMessage());
        }
    }
    
    private String readStream(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
    
    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
    
    public Set<String> getSupportedLanguages() {
        return LANGUAGE_CONFIGS.keySet();
    }
    
    private static class LanguageConfig {
        final String dockerImage;
        final String command;
        final String fileName;
        
        LanguageConfig(String dockerImage, String command, String fileName) {
            this.dockerImage = dockerImage;
            this.command = command;
            this.fileName = fileName;
        }
    }
}
