Architecture Overview
The application is a two-tier web system with a Vue.js frontend and a Java backend, connected via REST APIs. Users open a web page with a code editor, select a language, write code, and click Run or Share. The frontend sends the code and language to the backend. The Java backend then launches a Docker-based sandbox to compile/run the code and returns the output (stdout/stderr) to the frontend. For sharing, the backend stores the code (in a database or file) with a unique ID and returns a shareable link. When someone opens that link, the frontend loads the code by calling the backend’s GET endpoint and displays it in the editor. An example high-level flow is:
Browser (Vue SPA) 
   │   (1) API calls
   ▼        ┌─────────────────────────┐
[Code Editor] ──► [Java Backend REST API] ──► [Docker Sandbox] ──► (Compile/Run Code)
                     ▲    │      │
                     │    │      └─ returns output (stdout, stderr)
                     │    │
                     │    └─► [Snippet Store (DB or File System)] (Save/Retrieve code)
                     └─► (returns snippet ID or code content)
All user interactions (editing, running, and sharing code) are single-user and do not require authentication. Every new “share” creates a distinct URL (with an embedded unique ID) that retrieves the stored snippet
stackoverflow.com
. Containers isolate each code execution, ensuring the user code cannot directly affect the host system
dev.to
.
Technologies and Libraries
Backend: Java (e.g. Java 11+) using Spring Boot for rapid REST API development. Use Maven or Gradle as a build tool.
Libraries: Spring Web (REST), Jackson (JSON), possibly docker-java (a Java client for Docker) or simply invoking docker via ProcessBuilder.
Database: a lightweight embedded database (H2) or file-based storage to save code snippets. (Even a simple JSON file or Map keyed by unique ID can suffice if persistence isn’t critical.)
Frontend: Vue.js 3 (via Vue CLI or Vite) for SPA.
Libraries: Vue Router (for URL-based snippet loading), Axios or Fetch API (for HTTP calls), and a code editor component like Monaco Editor or CodeMirror to enable language syntax highlighting and editing.
UI Framework (optional): a minimal CSS framework or component library (e.g. Vuetify, BootstrapVue) for buttons and layout, or custom CSS for simplicity.
Execution Sandbox: Docker (free community edition) as the container runtime to isolate untrusted code execution
dev.to
. Each supported language uses a suitable Docker image (see table below). Docker will be invoked with strict resource limits (CPU, memory, no network, --rm, etc.) to sandbox the code.
Supported Languages: Python, C++, C, Java, Ruby, R, Go, JavaScript, Rust, C#, plus HTML/CSS editing. (HTML/CSS can be handled specially via an iframe preview instead of a backend run.) The backend will accept code in any of these languages and select the appropriate Docker image for execution
dev.to
.
Backend Project Structure
backend/
├── src/
│   └── main/java/com/example/compiler/
│       ├── CompilerApplication.java         # Spring Boot entry point
│       ├── controller/
│       │   ├── ExecuteController.java       # Handles /api/execute
│       │   └── SnippetController.java       # Handles /api/snippets
│       ├── model/
│       │   ├── ExecuteRequest.java          # {code, language, input?}
│       │   ├── ExecuteResponse.java         # {stdout, stderr, exitCode}
│       │   ├── Snippet.java                 # {id, code, language}
│       │   └── SnippetResponse.java         # {id, url}
│       └── service/
│           ├── ExecutionService.java        # Runs code in Docker
│           └── SnippetService.java          # Creates and retrieves snippets
├── src/main/resources/
│   └── application.yml                     # e.g. server port, storage config
├── pom.xml                                 # Maven config (or build.gradle)
├── Dockerfile                              # (for backend container, if needed)
└── README.md
Controller Layer: Defines REST endpoints. ExecuteController listens for POST /api/execute calls (code+lang in JSON), invokes ExecutionService, and returns the output. SnippetController handles POST /api/snippets (create snippet) and GET /api/snippets/{id} (retrieve code).
Service Layer:
ExecutionService writes the submitted code to a temporary workspace, pulls or reuses a Docker image for the language, then runs docker run with strict flags (e.g. --memory, --cpus, --pids-limit, --net=none, --read-only) to compile/run the code. It captures the container’s stdout/stderr and exit code. For compiled languages (C, C++, Java, C#), it typically runs a compile command then an execute command. For scripting languages (Python, Ruby, R, JS), it runs the interpreter directly. For HTML/CSS, the frontend can render an iframe instead of using the backend. After execution, it removes the container (--rm). Memory/CPU limits are set to prevent abuse
docs.docker.com
.
SnippetService generates a unique identifier (e.g. a UUID or short random string) for each new code snippet
stackoverflow.com
, saves the code and language under that ID (in H2/SQLite or as a file), and returns the ID (or share URL). On GET, it looks up the snippet by ID and returns its contents.
Endpoints
Method	Path	Request Body	Response	Description
GET	/api/languages	–	["Python","Java","C++",...]	(Optional) List supported languages
POST	/api/execute	{ code: "...", language: "Python", input: "..." }<br/>(JSON)	{ stdout: "...", stderr: "...", exitCode: 0 }	Compile & run the code, return output/errors
POST	/api/snippets	{ code: "...", language: "Java" }	{ id: "abc123", url: "https://app/snippets/abc123" }	Store code and return a shareable link
GET	/api/snippets/{id}	–	{ code: "...", language: "Java" }	Retrieve a previously shared snippet
(Optionally, you can include a parameter for custom input on /api/execute, but as per requirements only code output is needed.)
Frontend Project Structure
frontend/
├── public/
│   └── index.html               # Main HTML page
├── src/
│   ├── assets/                  # Static assets (logo, CSS)
│   ├── components/
│   │   ├── CodeEditor.vue       # Wraps Monaco/CodeMirror editor + language select
│   │   ├── OutputConsole.vue    # Displays run output (stdout/stderr)
│   │   └── ShareLink.vue        # Modal or box showing the shareable URL
│   ├── views/
│   │   ├── EditorView.vue       # Main view: code editor + run/share buttons
│   │   └── ShareView.vue        # (Optional) Loads code from URL param, shows in editor
│   ├── App.vue                  # Root component
│   ├── router.js                # Vue Router setup (routes for `/`, `/snippets/:id`)
│   └── main.js                  # App initialization, mounting, global libs
├── package.json
└── README.md
Vue Router: Define routes such as / (blank editor) and /snippets/:id (loads a snippet).
CodeEditor.vue: Embeds the code editor component. It binds to a text model and a <select> for language. This component emits the code and selected language on Run/Share.
OutputConsole.vue: Simple component that shows output lines in a scrollable area.
ShareLink.vue: After the backend returns an ID, this component formats a URL (e.g. https://yourapp.com/snippets/abc123) and lets the user copy it.
EditorView.vue: Composes CodeEditor, Run and Share buttons, and OutputConsole. On Run, it sends the code/lang to /api/execute via Axios and displays results. On Share, it sends code/lang to /api/snippets, then shows the ShareLink component with the returned URL.
ShareView.vue: On mount, reads this.$route.params.id, calls /api/snippets/{id}, and pre-fills the code editor with the retrieved code. It can reuse the same EditorView UI to allow editing/running the shared snippet.
The UI should remain simple and responsive: a large editor area, language selector, Run and Share buttons, and an output panel below.
Execution Sandbox (Docker Integration)
The backend relies on Docker to safely execute code. For each request to /api/execute, the service will:
Prepare code: Write the submitted code to a temp file (e.g. Main.java, script.py, etc.) in a dedicated workspace directory.
Choose image & command: Based on language, pick a base image. For example:
Python: python:3.11-slim with command python script.py
C: gcc:latest with gcc code.c -o a.out && ./a.out
C++: gcc:latest (g++ included) or gcc:latest and g++ code.cpp -o a.out && ./a.out
Java: openjdk:17-jdk-slim with javac Main.java && java Main
JavaScript: node:18-slim with node script.js
Go: golang:1.20 with go run main.go
Ruby: ruby:3.0 with ruby code.rb
R: r-base:latest with Rscript code.R
Rust: rust:1.67 (or rust:slim) with rustc main.rs && ./main
C#: .NET SDK e.g. mcr.microsoft.com/dotnet/sdk:6.0 with dotnet run (requires project setup, or csc if available)
HTML/CSS: No backend execution; these can be previewed in an <iframe> on the frontend.
These images contain the compilers/interpreters needed.
Run container: Execute a Docker command such as:
docker run --rm \
  --net=none \
  --cpu-quota=200000 \     # e.g. limit CPU
  --memory=256m \
  -v /host/tmp/workdir:/workspace \
  python:3.11-slim \
  /bin/sh -c "timeout 5s python /workspace/script.py"
Flags like --net=none disable networking.
--memory and --cpu-quota constrain resources
docs.docker.com
.
Using timeout inside the container or Docker’s --stop-timeout to enforce a time limit (e.g. 5 sec).
Mounting the code file via -v or baking it into the image ensures the code executes inside container.
The container runs as a non-root user (many base images like Python/Rust have a default user, or we can specify user: 1000).
Capture output: The backend reads the container’s stdout and stderr streams and the exit code. These are packaged into ExecuteResponse JSON and sent back to the frontend.
Cleanup: Containers started with --rm auto-delete. The backend also deletes any temp files.
This design ensures isolation of untrusted code
dev.to
. Each execution happens in a fresh container, with limited privileges. (For improved performance, images can be pre-pulled or cached.) <table> <thead><tr><th>Language</th><th>Docker Image (example)</th><th>Run Command</th></tr></thead> <tbody> <tr><td>Python</td><td><code>python:3.11-slim</code></td><td><code>python script.py</code></td></tr> <tr><td>C</td><td><code>gcc:latest</code></td><td><code>gcc code.c -o a.out && ./a.out</code></td></tr> <tr><td>C++</td><td><code>gcc:latest</code> (g++)</td><td><code>g++ code.cpp -o a.out && ./a.out</code></td></tr> <tr><td>Java</td><td><code>openjdk:17-jdk-slim</code></td><td><code>javac Main.java && java Main</code></td></tr> <tr><td>JavaScript</td><td><code>node:18-slim</code></td><td><code>node script.js</code></td></tr> <tr><td>Go</td><td><code>golang:1.20</code></td><td><code>go run main.go</code></td></tr> <tr><td>Ruby</td><td><code>ruby:3.0</code></td><td><code>ruby code.rb</code></td></tr> <tr><td>R</td><td><code>r-base:latest</code></td><td><code>Rscript code.R</code></td></tr> <tr><td>Rust</td><td><code>rust:1.67</code></td><td><code>rustc main.rs && ./main</code></td></tr> <tr><td>C#</td><td><code>mcr.microsoft.com/dotnet/sdk:6.0</code></td><td><code>dotnet run</code> (requires .csproj)</td></tr> </tbody> </table> (For HTML/CSS, no Docker is needed – the code editor can output these directly into a sandboxed <iframe> for preview.)
Sharing Snippets (No Authentication)
Users share code via unique links without logging in. When a user clicks Share, the frontend sends {code, language} to POST /api/snippets. The backend’s SnippetService generates a unique ID (e.g. a short UUID or hash) for the snippet
stackoverflow.com
 and stores the code and language under that ID (in a database table or a file named by ID). It then returns a JSON {id, url}, where url is something like https://yourapp.com/snippets/{id}. The frontend displays this URL to the user. When someone visits that URL, the Vue router loads the EditorView with the route param, the frontend calls GET /api/snippets/{id}, and fills the editor with the stored code and language. Because there is no login, all snippets are publicly retrievable by anyone with the link; however, editing is still single-user (there is no real-time collaboration or editing locks). This approach mirrors services like JSFiddle or Pastebin, which “generate a unique ID embedded in the URL and associate it with data in some back end storage”
stackoverflow.com
. No cookies or sessions are needed, and the user’s browser simply navigates to the share URL.
Deployment
Local Development:
Backend: Require Java 11+ and Docker. Run via mvn spring-boot:run or similar. Ensure Docker daemon is running and the Java process has permissions to invoke Docker (e.g. the docker CLI or Docker API). Alternatively, provide a docker-compose.yml that spins up the backend (Spring Boot) service and mounts the host Docker socket (/var/run/docker.sock) so the app can launch containers.
Frontend: Require Node.js. Run npm install and npm run serve (Vue CLI) or npm run dev (Vite). The frontend config (e.g. in vue.config.js or vite.config.js) should proxy API calls to localhost:8080 (Spring Boot) so CORS isn’t an issue.
(Optionally) Use docker-compose to start both services together. For example, a Compose file might define one service for the Java backend (exposing port 8080) and one for the Vue frontend (exposing port 3000).
Production (Cloud) – Render/Vercel:
Frontend: Build the Vue app (npm run build). Deploy static assets to Vercel or Netlify as a static site (they integrate easily via GitHub). Vercel/Netlify will serve the compiled JS/CSS on a global CDN. Set the build output to target the correct path (root).
Backend: Deploy to Render (or Heroku). Render supports Java/Spring Boot via either a Dockerfile or built-in Spring service. In Render, add an environment variable for the port (use $PORT from Render). The backend app should listen on 0.0.0.0 and the given port. Note: Some free-tier hosts may not allow running Docker inside the containerized app. If Render’s free tier forbids Docker-in-Docker, an alternative is to use an external execution service (or a lightweight VM-based sandbox), but assuming Docker is allowed, you can include Docker CLI in the Render environment.
CORS: On cloud, ensure CORS is configured or use a proxy. If frontend and backend are on different domains, the backend must allow cross-origin requests from the frontend origin.
CI/CD: Set up GitHub Actions or Render deploy hooks to automatically build and deploy both parts on commit.
Free-Tier Notes: Render’s free plan sleeps after inactivity; warm starts may incur delay. Vercel free is ideal for static frontends. Make sure to use minimal resource limits (e.g. 256MB RAM) so you stay within free quotas.
Security Considerations
Container Isolation: Docker provides some sandboxing by isolating the process and filesystem. However, containers share the host’s kernel, so they are not as foolproof as full VMs
security.stackexchange.com
. A malicious user with a kernel exploit could potentially escape the container. To mitigate:
Run containers with --read-only and minimal privileges (--cap-drop=ALL, --user nobody) to reduce attack surface.
Do not mount sensitive host directories. Only mount the temp workspace (or better, use Docker’s -v to an ephemeral temp directory).
Disable network with --net=none so code can’t call external services.
Drop dangerous Linux capabilities (SYS_ADMIN, etc.) with --cap-drop.
Use small, language-specific images (e.g. slim images) to minimize included software.
Resource Limits: Prevent denial-of-service by limiting container resources
docs.docker.com
. Always run docker run with --memory (e.g. 128–256 MB) and --cpus limits (e.g. 0.5 CPUs). Use Docker’s --pids-limit to prevent fork bombs. Also set a time limit (timeout or Docker’s --stop-timeout) so infinite loops are killed. Without limits, a user’s code could crash the host (out-of-memory kills) or hog the CPU indefinitely.
Code Injection: Carefully build Docker commands to avoid shell injection. For example, when inserting the user’s filename or command, do not concatenate untrusted strings into shell commands. Use Java’s ProcessBuilder with separate arguments or properly escape. Validate or whitelist languages to avoid arbitrary image names.
Output Handling: The user-submitted code might print large data. Cap the output size returned (e.g. truncate after 100KB). Sanitize any output before displaying in HTML to avoid breaking the page (especially if showing HTML/CSS code). If previewing user-written HTML/CSS in an <iframe>, use the sandbox attribute to prevent script execution and escape insertion of <script> tags in the page.
No Login, No Privacy: Since there is no authentication, all saved snippets are public. Warn users that anyone with the link can see the code. Also, rate-limit the endpoints (especially code execution) to prevent abuse by bots.
Updates and Patches: Keep Docker, the OS, and language runtimes up-to-date to patch known vulnerabilities. Remove unused packages from sandbox images to shrink attack surface.
Alternative Isolation (If Needed): For stricter security, one could use lightweight VMs (Firecracker, gVisor) instead of Docker, as containers “do not guarantee complete isolation”
security.stackexchange.com
security.stackexchange.com
. However, Docker with best practices is usually acceptable for a hobby project on a free tier.
By combining container-based isolation
dev.to
, strict resource caps
docs.docker.com
, and careful handling of inputs/outputs, the system can safely run user code in multiple languages. Always privilege the sandbox – for example, validate language names against an allowed list, and never run untrusted code directly on the host. Sources: Guidance on using containers for code execution
dev.to
, best practices for unique snippet links
stackoverflow.com
, and Docker security/limits
security.stackexchange.com
docs.docker.com
 were used to inform this design.
Citations
Favicon
php - How can you save and share the data WITHOUT logging in - Stack Overflow

https://stackoverflow.com/questions/25985994/how-can-you-save-and-share-the-data-without-logging-in
Favicon
How to develop an online code compiler using Java and Docker. - DEV Community

https://dev.to/zakariamaaraki/how-to-develop-an-online-code-compiler-using-java-and-docker-2oe8
Favicon
How to develop an online code compiler using Java and Docker. - DEV Community

https://dev.to/zakariamaaraki/how-to-develop-an-online-code-compiler-using-java-and-docker-2oe8
Favicon
Resource constraints | Docker Docs

https://docs.docker.com/engine/containers/resource_constraints/
Favicon
Docker as a sandbox for untrusted code - Information Security Stack Exchange

https://security.stackexchange.com/questions/107850/docker-as-a-sandbox-for-untrusted-code
Favicon
Docker as a sandbox for untrusted code - Information Security Stack Exchange

https://security.stackexchange.com/questions/107850/docker-as-a-sandbox-for-untrusted-code