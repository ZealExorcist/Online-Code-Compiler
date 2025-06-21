Task List for Building the Online Compiler Application
Project Initialization
Set up development environment: Install JDK 17+ (required for Spring Boot 3.x
spring.io
), Node.js (LTS), npm/Yarn, Docker engine, and a suitable IDE (e.g. IntelliJ, VS Code).
Initialize version control: Create a Git repository (e.g. on GitHub) and add a README describing the project and stack. Set up .gitignore and, if desired, CI config (e.g. GitHub Actions) to run tests and builds.
Backend scaffold: Use Spring Initializr (https://start.spring.io) to bootstrap a new Spring Boot project
spring.io
. Choose Maven or Gradle, Java, and add dependencies like Spring Web, Spring Data, and DevTools. Configure the build tool and Java version (17+).
Frontend scaffold: Initialize a Vue.js project (with Vite or Vue CLI). For example, use npm init vue@latest or vue create. Configure package.json scripts, linting (ESLint/Prettier) and basic folder structure (components, views).
Define project structure: Decide on mono-repo vs. separate repos. Typically have one folder/repo for backend and one for frontend. Ensure clear directory layout (e.g. backend/, frontend/).
Configure database/storage: Choose storage for code snippets (e.g. PostgreSQL, SQLite, or an in-memory DB). Set up database config placeholders in Spring (e.g. application.properties). If using an external DB, document local vs. production settings.
Create initial Dockerfiles: Write a multi-stage Dockerfile for the backend (see example – build JAR then run from minimal image
medium.com
). Also create .dockerignore. These will be used later for containerization.
Configure environment files: Set up configuration for local development. For backend, prepare application-dev.properties. For frontend, set up environment variables (e.g. VITE_API_URL) for API endpoints.
Documentation and tracking: Write initial docs or comments (e.g. API design, UML if needed). Set up issue tracking (Kanban, backlog) to plan tasks.
Backend Development
Design data model and services: Define a Snippet entity (fields: id, code, language, createdAt, etc.) and a repository/service to save and retrieve snippets. Ensure each snippet has a unique identifier (UUID or nanoid). No user accounts are needed.
Implement snippet storage API: Create REST endpoints such as POST /api/snippets (to save code and return an ID) and GET /api/snippets/{id} (to retrieve code by ID). Use Spring Web MVC (@RestController) and, if using a database, Spring Data JPA (or similar) for persistence.
Implement code execution API: Add an endpoint like POST /api/execute that accepts { code: "...", language: "python" }. In this service, write the received code to a temporary directory and launch a Docker container to run it
stackoverflow.com
. For example, use ProcessBuilder or a Java Docker client to execute docker run with the code mounted.
Execute code in Docker: For each execution request, spawn a new container for the specified language with the code directory mounted
stackoverflow.com
. The container’s entrypoint should compile/run the code (e.g. gcc, python, java, etc.). Capture stdout and stderr streams. Enforce timeouts: kill the container if it runs too long (e.g. 5–10 seconds). After completion or timeout, remove the container. This follows the approach described in sandbox examples
stackoverflow.com
.
Resource limiting and security: Apply limits on container resources (CPU, memory) and use --network=none to disable networking. Create/ensure a non-root user inside the container for code execution
medium.com
. Apply Docker best-practices (multi-stage images, minimal base) so the runner images are small and secure
medium.com
medium.com
.
REST API details: Return JSON with execution results (output, errors, exit code). Handle compilation failures or runtime errors gracefully. Implement error handling in controllers (e.g. return 4xx/5xx codes as appropriate).
Health and utility endpoints: Add simple endpoints for health checks (e.g. /actuator/health if using Spring Actuator) and a GET /api/languages that lists supported languages.
Testing and logging: Write unit tests for controllers/services (JUnit). Add logging for each request (timestamp, IP, language). Consider a thread pool or async task executor for running code executions concurrently.
Logging and cleanup: Log execution details but avoid logging user code. Clean up temp files/directories after execution.
Documentation: Update API docs or Swagger/OpenAPI definitions. Document how to use the endpoints.
Frontend Development
Build UI layout: Design a single-page UI with a code editor panel (using e.g. Monaco Editor or CodeMirror), a language selector dropdown, a “Run” button, and an output console panel. Optionally add a “Share” button. Ensure only one user edits at a time (no real-time collaboration needed).
Integrate code editor: Install and configure the chosen code editor component. Enable syntax highlighting and language modes for all target languages. Ensure editor settings (tab size, font) are user-friendly.
Language selection: Populate a dropdown with supported languages. When the user selects a language, adjust the editor’s syntax mode and prepare to send the correct language to the backend.
Run code flow: On clicking “Run”, send the editor content and selected language to the backend execution API. Show a loading indicator, then display the output or errors in the output panel. Handle asynchronous responses properly.
Share link flow: Implement code sharing: when “Share” (or “Save”) is clicked, POST the current code and language to the backend snippet API. Receive a unique snippet ID and generate a shareable URL (e.g. https://yourapp.com/?id=<snippetId>). Display/copy this link to the user. (This mimics GitHub Gist’s approach of sharing code snippets via unique URLs
lazypandatech.com
.)
Load snippet by ID: On page load, check for a snippet ID in the URL parameters. If present, GET the snippet from the backend and populate the editor with its code and language. This enables anyone with the link to view (and re-run) the code.
State management: Use Vue’s state (Vuex or composables) or local state to manage editor content and output. Ensure that copying a link or reloading maintains or clears state appropriately.
Error handling: Show user-friendly messages if execution fails (e.g. “Compilation error” or “Server timeout”). Validate user input (e.g. non-empty code). Enforce sensible limits on code length before sending to backend.
CORS and config: Configure Axios or Fetch with the correct API base URL (from an env var). Set up CORS on the backend to allow the frontend origin. For local dev, configure a Vite dev server proxy to bypass CORS.
Local development: Set up npm run dev to hot-reload the Vue app. Ensure it integrates with the backend (using proxy or backend running on different port).
Testing: Write basic component tests (Jest or Vue Test Utils). Manually test flows: writing code, running it, saving, and loading via shared link.
Deployment prep: Build the static files (e.g. npm run build). Ensure the build script outputs to a dist folder ready for Vercel deployment.
Sandbox Container Setup
Language-specific images: For each language, either create or choose a Docker image with the required toolchain:
Python: python:latest (or slim) with necessary packages.
C/C++: gcc:latest (or Alpine with build tools) for compiling.
Java: openjdk:xx for javac and java.
Ruby: ruby:latest.
R: rocker/r-base:latest or similar with Rscript.
Go: golang:latest.
JavaScript: node:latest to run JS (or Deno if desired).
HTML/CSS: (no execution needed; could be a no-op or preview in frontend).
Rust: rust:latest (with rustc).
C#: use .NET SDK image (e.g. mcr.microsoft.com/dotnet/sdk) or Mono image with mcs/csc.
Entrypoint scripts: In each container, have an entrypoint (or a script) that checks the code file and runs the appropriate commands. For compiled languages, use multi-stage builds: compile in one stage and run in a minimal second stage
medium.com
medium.com
. This keeps final images small.
Non-root users: Ensure containers run code as a non-root user
medium.com
. Add a user in the Dockerfile and use USER to switch.
Security restrictions: Run containers with minimal privileges: no extra capabilities, no network (--network=none), and bind-mount only necessary volume (read/write code). Use Docker options like --cpus and -m to limit CPU and memory.
Ephemeral execution: Use docker run --rm or programmatic cleanup so containers and volumes are removed after execution. Handle timeouts (e.g. docker run --stop-timeout or externally kill the container).
Testing images: Build and test each container locally by running sample code. Verify that long loops are killed and no file writes or network calls can escape the container. Ensure minimal attack surface by starting from slim/base images
medium.com
.
Frontend–Backend Integration
CORS configuration: Enable CORS in Spring Boot (e.g. via @CrossOrigin on controllers or a global CORS config) to allow requests from the frontend host.
API endpoints: In Vue, call the backend’s REST endpoints (/execute, /snippets, /snippets/{id}, etc.) using Axios or Fetch. Ensure JSON formats match (fields like code and language).
Environment variables: Use different configurations for local vs production API URLs. In Vercel, set environment var VITE_API_URL to the Render backend URL; in local dev, point to localhost.
Local proxy (optional): Configure Vite’s proxy so that API calls during local development automatically route to the backend port (avoiding CORS issues).
Data flow testing: Verify end-to-end that typing code in the editor, clicking Run, and displaying output works. Also test saving a snippet and then retrieving it by reloading the page with the snippet ID.
Error and state handling: Ensure that UI handles errors (network failures, 5xx from backend) gracefully. Maintain application state properly when switching between editing and viewing shared code.
Real-time output (optional): For long-running code, consider implementing streaming output (e.g. via Server-Sent Events as in the CodeCube example)
stackoverflow.com
. Otherwise, use synchronous requests.
Security Hardening
Container security: Follow Docker hardening practices. Use minimal base images (e.g. Alpine) to reduce vulnerabilities
medium.com
. Run containers with non-root users
medium.com
. Disable networking (--network=none) and drop Linux capabilities.
Resource limits: Enforce strict CPU, memory, and execution-time limits on code containers. Use Docker flags (-m, --cpus, --pids-limit, etc.) and kill runaway processes to prevent abuse.
API rate limiting: Implement rate limiting on API endpoints to throttle requests per IP or snippet ID. For example, use a filter or a library like Bucket4j/Resilience4J in Spring to cap calls (e.g. 10 runs/min per IP)
geeksforgeeks.org
. This helps protect against DoS attacks.
Input validation: Sanitize and validate all inputs. Limit the size of code snippets and reject any unsupported language requests. Do not trust user input when constructing file paths or commands.
Dependency and image updates: Regularly update Docker base images and dependencies. Use fixed tags (not latest) for reproducibility, but rebuild images often to pick up security patches
medium.com
.
HTTPS and secrets: In production, serve over HTTPS. Store any secrets (if used) as environment variables, not in code. Lock down backend origins (CORS) to only the frontend domain.
Logging and monitoring: Log suspicious activities (e.g. repeated failures). Consider monitoring container exits for errors. Remove any debug or verbose logs before production.
Security testing: Conduct penetration tests on the sandbox (try to escape containers or access unauthorized resources). Verify that SQL (if used) is not injectable (use prepared statements/JPA).
Deployment
Local deployment: Verify local setup by running mvn spring-boot:run (or ./mvnw spring-boot:run) for the backend and npm run dev for the frontend. Optionally use docker-compose to start backend plus an in-memory DB. Test full functionality locally.
Continuous Integration (optional): Configure a CI pipeline to build and test the backend and frontend on commits. Build Docker images for the sandbox if desired.
Frontend cloud deployment: Use Vercel to host the Vue app. Connect the frontend repo/Git branch in Vercel. Vercel will auto-detect Vue and build the site
vercel.com
. Set the environment variable for the API URL in Vercel’s project settings. Vercel will provide a .vercel.app domain (or add a custom domain).
Backend cloud deployment: Deploy the Spring Boot backend (with Docker) on Render (or a similar service). Create a Web Service in Render, link the GitHub repo, and choose “Docker” as the runtime
medium.com
. Render will build the Dockerfile and run the container. Set required environment variables (e.g. DB URL, any secrets). Render’s free tier can include a managed PostgreSQL database if needed.
Verify deployments: After deployment, test the live API and frontend on their URLs. Ensure CORS is configured to allow the Vercel domain. Monitor logs in Render/Vercel for errors.
SSL and domains: Vercel and Render automatically provide HTTPS. Configure a custom domain on Vercel if desired.
Scaling and backups: (Optional) Configure auto-scaling options if high traffic is expected. Set up backups for the database storing snippets.
Final QA and Testing
Unit tests: Ensure thorough unit tests for backend (controllers, services) and frontend components. Mock Docker execution in unit tests (or use a test container).
Integration/E2E tests: Perform end-to-end tests of the full workflow: write code in the UI, run it, save a snippet, and retrieve it via its link. Use tools like Cypress or Selenium for UI tests, and Postman or RestAssured for API tests.
Cross-language testing: For each supported language, test sample code (including edge cases, errors, infinite loops). Verify the container kills long-running code and isolates properly.
Security testing: Test rate limiting (send many requests quickly), injection attempts, and container breakout attempts. Confirm that limits and validations prevent these.
Performance testing: Simulate concurrent users submitting code to test how the backend scales. Monitor memory/CPU usage.
Usability testing: Check the UI for usability issues. Verify code editor behaves correctly (indentation, tabbing) and share links work on different browsers.
Accessibility checks: Ensure the frontend is usable (e.g. accessible labels for screen readers).
Bug fixes and iterations: Fix any bugs found and re-test. Review and refactor code as needed.
Documentation: Finalize the README and any user guide, including how to run locally and API usage.
Go-live: Once QA passes, launch the app. Monitor logs and metrics post-deployment to catch any issues early.
Sources: Guidance on Spring Boot project setup
spring.io
, containerized code execution patterns
stackoverflow.com
stackoverflow.com
, shareable code snippet examples
lazypandatech.com
, Docker security best practices
medium.com
medium.com
, API rate limiting techniques
geeksforgeeks.org
, and deployment steps for Vue and Spring Boot
vercel.com
medium.com
.
Citations
Favicon
Getting Started | Building an Application with Spring Boot

https://spring.io/guides/gs/spring-boot/
Favicon
Getting Started | Building an Application with Spring Boot

https://spring.io/guides/gs/spring-boot/
Favicon
Deploying a Spring Boot Application on Render | by Parth Manaktala | Medium

https://medium.com/@pmanaktala/deploying-a-spring-boot-application-on-render-4e757dfe92ed
Favicon
Sandbox command execution with docker via Ajax - Stack Overflow

https://stackoverflow.com/questions/28086406/sandbox-command-execution-with-docker-via-ajax
Favicon
Hardening Container Images: Best Practices and Examples for Docker | by Fabien Soulis | Medium

https://medium.com/@SecurityArchitect/hardening-container-images-best-practices-and-examples-for-docker-e941263cab13
Favicon
Hardening Container Images: Best Practices and Examples for Docker | by Fabien Soulis | Medium

https://medium.com/@SecurityArchitect/hardening-container-images-best-practices-and-examples-for-docker-e941263cab13
Favicon
How to create and add GitHub gist file to your blog

https://lazypandatech.com/blog/Miscellaneous/27/How-to-create-and-add-GitHub-gist-file-to-your-blog/
Favicon
Implementing Rate Limiting in a Spring Boot Application - GeeksforGeeks

https://www.geeksforgeeks.org/advance-java/implementing-rate-limiting-in-a-spring-boot-application/
Favicon
Hardening Container Images: Best Practices and Examples for Docker | by Fabien Soulis | Medium

https://medium.com/@SecurityArchitect/hardening-container-images-best-practices-and-examples-for-docker-e941263cab13
Favicon
How to Deploy a Vue.js Site with Vercel

https://vercel.com/guides/deploying-vuejs-to-vercel
Favicon
Deploying a Spring Boot Application on Render | by Parth Manaktala | Medium

https://medium.com/@pmanaktala/deploying-a-spring-boot-application-on-render-4e757dfe92ed
Favicon
Sandbox command execution with docker via Ajax - Stack Overflow

https://stackoverflow.com/questions/28086406/sandbox-command-execution-with-docker-via-ajax
All Sources
Favicon
spring
Favicon
medium
Favicon
stackoverflow
Favicon
lazypandatech
Favicon
geeksforgeeks
Favicon
Building the Online Compiler Application - Task List

## Phase 6 Enhanced: Security Hardening and User Management - ✅ COMPLETED

### User Management System
✅ **MongoDB Integration**: Added MongoDB support for user data storage alongside existing H2 database
✅ **User Model**: Created comprehensive User model with settings, API keys, and snippet ownership
✅ **User Registration/Login**: Implemented full authentication system with JWT tokens
✅ **User Settings**: Comprehensive user preferences for editor, execution, and privacy settings
✅ **API Key Authentication**: Dual authentication system supporting both JWT tokens and API keys

### Backend Security Features
✅ **JWT Authentication**: Secure token-based authentication with configurable expiration
✅ **API Rate Limiting**: Bucket4j-based rate limiting to prevent abuse (configurable requests per minute)
✅ **Password Security**: BCrypt password hashing with secure password policies
✅ **CORS Configuration**: Proper CORS setup for production deployment
✅ **Security Headers**: Added security headers and authentication middleware
✅ **Input Validation**: Enhanced validation for all user inputs and API endpoints

### Authentication & Authorization
✅ **JWT Security Filter**: Custom authentication filter supporting both JWT and API key auth
✅ **Role-based Access**: User roles (USER, ADMIN) with proper authorization
✅ **Protected Endpoints**: All execution and snippet APIs now require authentication
✅ **Guest Access**: Limited functionality available for unauthenticated users
✅ **Default Admin User**: Automatic creation of admin user on first startup

### Frontend User Interface
✅ **Authentication Pages**: Beautiful login/registration page with proper validation
✅ **Settings Dashboard**: Comprehensive settings page with multiple configuration sections:
   - Editor Settings (theme, font, language preferences)
   - Execution Settings (timeouts, output limits)
   - Privacy Settings (public snippets, sharing preferences)
   - Account Management (password change, API key refresh)
✅ **User Header**: Updated header with user menu, authentication status, and navigation
✅ **Logo Support**: Added logo display capabilities for branding
✅ **Route Guards**: Navigation guards to protect authenticated routes

### Enhanced API Features
✅ **User Context**: All operations now include user context for ownership and permissions
✅ **Settings API**: Full CRUD operations for user settings with real-time updates
✅ **Authentication API**: Complete auth endpoints (login, register, profile, validation)
✅ **Rate Limit Headers**: Response headers showing rate limit status
✅ **Error Handling**: Improved error responses with proper HTTP status codes

### Configuration Management
✅ **Environment Variables**: Proper configuration for JWT secrets, MongoDB URIs, and rate limits
✅ **Development Setup**: MongoDB configuration with fallback to H2 for backward compatibility
✅ **Security Configuration**: Spring Security configuration with proper endpoint protection
✅ **Data Initialization**: Automatic admin user creation with default settings

### Code Quality & Security
✅ **Security Best Practices**: Non-root container execution, input sanitization, SQL injection prevention
✅ **Dependency Updates**: Updated to latest security-focused dependencies
✅ **Service Layer**: Proper separation of concerns with dedicated services for auth, users, and settings
✅ **Error Logging**: Comprehensive logging for security events and authentication attempts

### Additional Features Implemented
✅ **User Snippet Ownership**: Snippets now belong to users with proper access controls
✅ **Settings Persistence**: User preferences stored in MongoDB and cached locally
✅ **API Documentation**: Enhanced API with proper authentication documentation
✅ **Mobile Responsive**: All new UI components are mobile-friendly
✅ **Guest Mode**: Continue-as-guest option for users who don't want to register

### Security Testing
✅ **Authentication Flow**: Login, registration, and logout functionality
✅ **Rate Limiting**: Verified rate limiting works correctly
✅ **API Security**: All protected endpoints require proper authentication
✅ **Input Validation**: SQL injection and XSS prevention measures
✅ **Session Management**: Proper token expiration and refresh handling

This enhanced Phase 6 provides enterprise-level security and user management while maintaining the simplicity of the original compiler interface. Users can now securely manage their code snippets, customize their development environment, and maintain their coding preferences across sessions.
