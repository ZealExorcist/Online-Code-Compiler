FROM node:18-slim

# Set working directory
WORKDIR /workspace

# Create non-root user for security
RUN groupadd -r coderunner && useradd -r -g coderunner coderunner

# Install necessary packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Install common Node.js packages globally
RUN npm install -g --unsafe-perm=true --allow-root \
    axios \
    lodash

# Set resource limits
RUN echo "coderunner soft nproc 50" >> /etc/security/limits.conf && \
    echo "coderunner hard nproc 100" >> /etc/security/limits.conf && \
    echo "coderunner soft nofile 100" >> /etc/security/limits.conf && \
    echo "coderunner hard nofile 200" >> /etc/security/limits.conf

# Switch to non-root user
USER coderunner

# Default command
CMD ["node"]
