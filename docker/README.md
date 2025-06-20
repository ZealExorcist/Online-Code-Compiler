# Docker Setup for Online Compiler

This directory contains Docker configurations for secure code execution environments.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose available

## Building Images

### Windows
```bash
cd docker
build.bat
```

### Linux/Mac
```bash
cd docker
chmod +x build.sh
./build.sh
```

### Manual Build
```bash
cd docker
docker-compose build
```

## Available Languages & Images

| Language   | Docker Image                     | Base Image        |
|------------|----------------------------------|-------------------|
| Python     | online-compiler/python:latest    | python:3.11-slim  |
| Java       | online-compiler/java:latest      | openjdk:17-slim   |
| C++        | online-compiler/cpp:latest       | gcc:latest        |
| C          | online-compiler/cpp:latest       | gcc:latest        |
| JavaScript | online-compiler/javascript:latest | node:18-slim     |
| Go         | online-compiler/go:latest        | golang:1.21-alpine |
| Rust       | online-compiler/rust:latest      | rust:1.70-slim    |
| Ruby       | online-compiler/ruby:latest      | ruby:3.2-slim     |

## Security Features

- **Non-root execution**: All code runs as non-privileged user `coderunner`
- **Resource limits**: Memory, CPU, and process limits enforced
- **Network isolation**: Containers run with `--network=none`
- **Filesystem isolation**: Read-only filesystem with tmpfs for temporary files
- **Process limits**: Maximum 50 processes per container
- **File descriptor limits**: Limited file handles

## Container Execution

The ExecutionService automatically:
1. Creates temporary directory with user code
2. Mounts directory as read-only volume
3. Runs container with security restrictions:
   - `--memory=128m` (128MB RAM limit)
   - `--cpus=0.5` (50% CPU limit)
   - `--network=none` (No network access)
   - `--pids-limit=50` (Max 50 processes)
   - `--read-only` (Read-only filesystem)
   - `--tmpfs=/tmp:exec` (Executable temporary directory)
4. Enforces 10-second execution timeout
5. Captures stdout/stderr
6. Cleans up temporary files

## Testing Docker Setup

1. Build images: `docker-compose build`
2. Test Python: `docker run --rm online-compiler/python:latest python3 -c "print('Hello Docker!')"`
3. Test Java: `docker run --rm online-compiler/java:latest java -version`
4. List images: `docker images | grep online-compiler`

## Troubleshooting

### Docker Not Running
- Ensure Docker Desktop is started
- Check Docker daemon status: `docker info`

### Build Failures
- Check Docker logs: `docker-compose logs`
- Rebuild specific image: `docker-compose build python`
- Clean build: `docker system prune -a`

### Permission Issues
- On Linux: Add user to docker group: `sudo usermod -aG docker $USER`
- Restart shell after group changes

## Monitoring

View running containers:
```bash
docker ps
```

Monitor resource usage:
```bash
docker stats
```

Clean up stopped containers:
```bash
docker container prune
```
