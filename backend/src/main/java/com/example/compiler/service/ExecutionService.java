package com.example.compiler.service;

import com.example.compiler.model.ExecuteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${app.execution.mode:docker}")
    private String executionMode;
      // Supported languages and their Docker configurations
    private static final Map<String, LanguageConfig> LANGUAGE_CONFIGS;
    
    static {
        Map<String, LanguageConfig> configs = new HashMap<>();
        configs.put("python", new LanguageConfig("online-compiler/python:latest", "python3 main.py", "main.py"));
        configs.put("java", new LanguageConfig("online-compiler/java:latest", "javac Main.java && java Main", "Main.java"));        configs.put("cpp", new LanguageConfig("online-compiler/cpp:latest", "g++ -o main main.cpp && ./main", "main.cpp"));
        configs.put("c", new LanguageConfig("online-compiler/cpp:latest", "gcc -o main main.c && ./main", "main.c"));
        configs.put("javascript", new LanguageConfig("online-compiler/javascript:latest", "node main.js", "main.js"));
        configs.put("typescript", new LanguageConfig("online-compiler/typescript:latest", "tsc main.ts && node main.js", "main.ts"));
        configs.put("rust", new LanguageConfig("online-compiler/rust:latest", "rustc main.rs && ./main", "main.rs"));        configs.put("ruby", new LanguageConfig("online-compiler/ruby:latest", "ruby main.rb", "main.rb"));        configs.put("r", new LanguageConfig("online-compiler/r:latest", "Rscript main.R", "main.R"));
        configs.put("csharp", new LanguageConfig("online-compiler/csharp:latest", "mcs Program.cs && mono Program.exe", "Program.cs"));
        configs.put("go", new LanguageConfig("online-compiler/go:latest", "go run main.go", "main.go"));
        LANGUAGE_CONFIGS = Collections.unmodifiableMap(configs);    }
    
    private static final int TIMEOUT_SECONDS = 10;
    private static final String MEMORY_LIMIT = "128m";
    private static final String CPU_LIMIT = "0.5";
    
    // Get timeout for specific language
    private int getTimeoutForLanguage(String language) {
        // Go needs more time for compilation and initialization
        if ("go".equals(language.toLowerCase())) {
            return 20; // 20 seconds for Go
        }
        return TIMEOUT_SECONDS; // 10 seconds for others
    }
      public ExecuteResponse executeCode(String code, String language, String input) {
        return executeCode(code, language, input, null);
    }
    
    public ExecuteResponse executeCode(String code, String language, String input, String userId) {
        long startTime = System.currentTimeMillis();
        
        // Log execution with user info
        if (userId != null) {
            logger.info("Executing {} code for user: {}", language, userId);
        } else {
            logger.info("Executing {} code for anonymous user", language);
        }
        
        try {
            // Check execution mode
            if ("mock".equals(executionMode)) {
                return executeMockCode(code, language, input, startTime);
            }
            
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
                ExecuteResponse response = runInDockerContainer(tempDir, config, inputFile != null, language);
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
    
    private ExecuteResponse executeMockCode(String code, String language, String input, long startTime) {
        logger.info("Executing code in mock mode for language: {}", language);
        
        // Simulate execution delay
        try {
            Thread.sleep(500 + (long)(Math.random() * 1000)); // 0.5-1.5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Generate mock output based on language and code
        String mockOutput = generateMockOutput(code, language, input);
        long executionTime = System.currentTimeMillis() - startTime;
        
        return new ExecuteResponse(mockOutput, "", 0, executionTime);
    }
    
    private String generateMockOutput(String code, String language, String input) {
        // Simple mock output generation
        if (code.toLowerCase().contains("hello")) {
            return "Hello, World!";
        }
        
        switch (language.toLowerCase()) {
            case "python":
                if (code.contains("print")) {
                    return "Mock Python output: Code executed successfully!";
                }
                break;
            case "java":
                if (code.contains("System.out.println")) {
                    return "Mock Java output: Code compiled and executed!";
                }
                break;
            case "cpp":
            case "c":
                if (code.contains("cout") || code.contains("printf")) {
                    return "Mock C/C++ output: Code compiled and executed!";
                }
                break;
            case "javascript":
                if (code.contains("console.log")) {
                    return "Mock JavaScript output: Code executed successfully!";
                }
                break;
            default:
                return "Mock " + language + " output: Code executed successfully!";
        }
          return "Mock execution completed. Docker mode disabled for development.";
    }
    
    private ExecuteResponse runInDockerContainer(Path workDir, LanguageConfig config, boolean hasInput, String language) {
        try {
            List<String> dockerCommand = new ArrayList<>();
            dockerCommand.addAll(Arrays.asList(
                "docker", "run",
                "--rm",
                "--memory=" + MEMORY_LIMIT,                "--cpus=" + CPU_LIMIT,
                "--network=none"            ));
            
            // Add process limits
            dockerCommand.add("--pids-limit=50");
            
            dockerCommand.addAll(Arrays.asList(
                "--tmpfs=/tmp:exec",
                "-v", workDir.toString() + ":/workspace",
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
            int timeout = getTimeoutForLanguage(language);
            boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                return ExecuteResponse.error("Execution timeout after " + timeout + " seconds");
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
