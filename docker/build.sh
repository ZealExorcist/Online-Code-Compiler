#!/bin/bash
# Build script for all Docker images

echo "Building Docker images for Online Compiler..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker and try again."
    exit 1
fi

# Build images using Docker Compose
echo "Building all language containers..."
docker-compose build

# Verify images were built
echo "Verifying built images..."
docker images | grep "online-compiler"

echo "Docker image build complete!"
echo ""
echo "Available images:"
echo "- online-compiler/python:latest"
echo "- online-compiler/java:latest"
echo "- online-compiler/cpp:latest"
echo "- online-compiler/javascript:latest"
echo "- online-compiler/go:latest"
