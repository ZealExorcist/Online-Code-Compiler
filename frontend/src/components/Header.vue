<template>
  <header class="header">
    <div class="header-content">
      <div class="logo">
        <h1>Online Compiler</h1>
        <span class="subtitle">Code, Compile, Execute</span>
      </div>
      <div class="header-info">
        <span class="language-count">{{ languageCount }}+ Languages Supported</span>
      </div>
    </div>
  </header>
</template>

<script>
export default {
  name: 'Header',
  data() {
    return {
      languageCount: 10
    }
  }
}
</script>

<style scoped>
.header {
  background: #1e1e2f;
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo h1 {
  font-size: 1.8rem;
  font-weight: bold;
  margin: 0;
}

.subtitle {
  font-size: 0.9rem;
  opacity: 0.9;
  font-style: italic;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.language-count {
  background: rgba(255,255,255,0.2);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
  }
  
  .logo h1 {
    font-size: 1.5rem;
  }
}
</style>
