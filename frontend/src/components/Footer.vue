<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-left">
        <p>&copy; 2025 Online Code Compiler.</p>
      </div>
      <div class="footer-right">
        <a href="https://github.com" target="https://github.com/ZealExorcist/Online-Code-Compiler" class="footer-link">GitHub</a>
        <span class="separator">|</span>
        <a href="https://zealexorcist.gitbook.io/zealexorcist-blog/project-docs/online-code-compiler" class="footer-link">Documentation</a>
        <span class="separator">|</span>
        <span class="status">
          <span class="status-dot" :class="{ 'online': isOnline }"></span>
          {{ isOnline ? 'Online' : 'Offline' }}
        </span>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'Footer',
  data() {
    return {
      isOnline: true
    }
  },
  mounted() {
    // Simple connectivity check
    this.checkConnection()
    setInterval(this.checkConnection, 30000) // Check every 30 seconds
  },  methods: {
    async checkConnection() {
      try {
        // Create AbortController for timeout
        const controller = new AbortController()
        const timeoutId = setTimeout(() => controller.abort(), 3000)
        
        const response = await fetch('http://localhost:8080/api/public/health', {
          method: 'GET',
          signal: controller.signal,
          headers: {
            'Content-Type': 'application/json'
          }
        })
        
        clearTimeout(timeoutId)
        
        if (response.ok) {
          const data = await response.json()
          this.isOnline = data.status === 'online'
        } else {
          this.isOnline = false
        }      } catch (error) {
        // Silent connection check failure
        this.isOnline = false
      }
    }
  }
}
</script>

<style scoped>
.footer {
  background-color: #252526;
  color: #d4d4d4;
  padding: 1rem 0;
  border-top: 1px solid #3c3c3c;
  font-size: 0.9rem;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-left p {
  margin: 0;
  color: #858585;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.footer-link {
  color: #d4d4d4;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-link:hover {
  color: #007acc;
}

.separator {
  color: #858585;
}

.status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #858585;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #dc3545;
}

.status-dot.online {
  background-color: #28a745;
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    gap: 0.5rem;
    text-align: center;
  }
  
  .footer-right {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
