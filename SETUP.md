# Online Compiler - Quick Setup

Automated setup scripts to install all dependencies and build the project.

## Windows Setup

Run the Windows batch script:

```cmd
setup.bat
```

## Linux/macOS Setup

Run the shell script:

```bash
chmod +x setup.sh
./setup.sh
```

## What the scripts do:

1. **Check Prerequisites:**
   - Java 17+ (required)
   - Node.js 18+ (required) 
   - Docker (required)
   - Git (recommended)

2. **Setup Backend:**
   - Create .env file from .env.example
   - Install Maven dependencies
   - Compile Java code

3. **Setup Frontend:**
   - Create .env file from .env.example
   - Install npm dependencies
   - Build frontend assets

4. **Setup Docker:**
   - Build language execution containers
   - Verify Docker images

## Manual Setup

If the automated scripts don't work for your environment, see:
- [doc.md](doc.md) - Complete setup instructions
- [README.md](README.md) - Quick start guide

## After Setup

1. Start backend: `cd backend && ./mvnw spring-boot:run`
2. Start frontend: `cd frontend && npm run dev`
3. Open: http://localhost:3000

## Troubleshooting

- **Java not found:** Install from https://adoptium.net/
- **Node.js not found:** Install from https://nodejs.org/
- **Docker not found:** Install from https://docker.com/
- **Permission errors:** Run as administrator/sudo
- **Build failures:** Check network connection and try again

For detailed troubleshooting, see [doc.md](doc.md#troubleshooting).
