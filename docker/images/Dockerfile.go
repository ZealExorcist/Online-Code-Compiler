FROM golang:1.21-bookworm

# Set working directory
WORKDIR /workspace

# Set Go environment for optimized execution
ENV GOMAXPROCS=1
ENV GODEBUG=madvdontneed=1
ENV CGO_ENABLED=0

# Install necessary packages and clean up
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    git \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Create non-root user for security
RUN groupadd -r coderunner && useradd -r -g coderunner coderunner

# Create home directory and set permissions
RUN mkdir -p /home/coderunner/.cache/go-build && \
    chown -R coderunner:coderunner /home/coderunner

# Set resource limits
RUN echo "coderunner soft nproc 50" >> /etc/security/limits.conf && \
    echo "coderunner hard nproc 100" >> /etc/security/limits.conf && \
    echo "coderunner soft nofile 100" >> /etc/security/limits.conf && \
    echo "coderunner hard nofile 200" >> /etc/security/limits.conf

# Set proper permissions
RUN chown -R coderunner:coderunner /workspace

# Switch to non-root user
USER coderunner

# Default command
CMD ["go"]
