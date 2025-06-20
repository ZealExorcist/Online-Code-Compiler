FROM r-base:4.3.2

# Set working directory
WORKDIR /workspace

# Create non-root user for security
RUN groupadd -r coderunner && useradd -r -g coderunner coderunner

# Install common R packages
RUN R -e "install.packages(c('ggplot2', 'dplyr', 'readr', 'tidyr', 'stringr'), repos='https://cran.rstudio.com/')"

# Set resource limits
RUN echo "coderunner soft nproc 50" >> /etc/security/limits.conf && \
    echo "coderunner hard nproc 100" >> /etc/security/limits.conf && \
    echo "coderunner soft nofile 100" >> /etc/security/limits.conf && \
    echo "coderunner hard nofile 200" >> /etc/security/limits.conf

# Switch to non-root user
USER coderunner

# Default command
CMD ["R"]
