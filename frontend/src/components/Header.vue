<template>
  <header class="header">
    <div class="header-content">
      <div class="logo">
        <img v-if="logoUrl" :src="logoUrl" alt="Logo" class="logo-image" />
        <div class="logo-text">
          <h1>Online Compiler</h1>
          <span class="subtitle">Code, Compile, Execute</span>
        </div>
      </div>
      
      <div class="header-center">
        <span class="language-count">{{ languageCount }}+ Languages Supported</span>
      </div>

      <div class="header-actions">
        <div v-if="isAuthenticated" class="user-menu">
          <div class="user-info" @click="toggleUserMenu">
            <span class="username">{{ username }}</span>
            <div class="avatar">{{ userInitial }}</div>
            <span class="dropdown-arrow">▼</span>
          </div>            <div v-if="showUserMenu" class="user-dropdown">
            <SettingsComponent 
              @settings-updated="handleSettingsUpdate"
              @load-snippet="handleLoadSnippet" 
            />
            <div class="dropdown-item" @click="logout">
              🚪 Logout
            </div>
          </div>
        </div>

        <div v-else class="auth-buttons">
          <router-link to="/login" class="auth-button login-button">
            Sign In
          </router-link>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
import authService from '../services/auth'
import SettingsComponent from './SettingsComponent.vue'

export default {
  name: 'Header',
  components: {
    SettingsComponent
  },
  data() {
    return {
      languageCount: 10,
      showUserMenu: false,
      logoUrl: '/logo.png' // You can add your logo here
    }
  },
  computed: {
    isAuthenticated() {
      return authService.isAuthenticated()
    },
    username() {
      return authService.getStoredUserData().username || 'User'
    },
    userInitial() {
      return this.username.charAt(0).toUpperCase()
    }
  },
  methods: {
    toggleUserMenu() {
      this.showUserMenu = !this.showUserMenu
    },    logout() {
      authService.logout()
      this.showUserMenu = false
      // Reload the page to reset all state
      window.location.reload()
    },    handleSettingsUpdate(settings) {
      console.log('Settings updated:', settings)
      // Don't close user menu when settings are updated to prevent modal from closing
      // this.showUserMenu = false
    },
    handleLoadSnippet(snippet) {
      this.$emit('load-snippet', snippet)
      this.showUserMenu = false
    }
  },
  mounted() {
    // Close user menu when clicking outside
    document.addEventListener('click', (e) => {
      if (!this.$el.contains(e.target)) {
        this.showUserMenu = false
      }
    })
  }
}
</script>

<style scoped>
.header {
  background: #1e1e2f;
  color: white;
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: relative;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
}

.logo-text h1 {
  font-size: 1.8rem;
  font-weight: bold;
  margin: 0;
}

.subtitle {
  font-size: 0.9rem;
  color: #a0aec0;
  display: block;
}

.header-center {
  display: flex;
  align-items: center;
}

.language-count {
  font-size: 0.9rem;
  color: #a0aec0;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.5rem 1rem;
  border-radius: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-menu {
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.1);
}

.username {
  font-weight: 500;
}

.avatar {
  width: 32px;
  height: 32px;
  background: #667eea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.dropdown-arrow {
  font-size: 10px;
  transition: transform 0.2s;
}

.user-info:hover .dropdown-arrow {
  transform: rotate(180deg);
}

.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  min-width: 200px;
  padding: 8px 0;
  margin-top: 8px;
  z-index: 1000;
}

.dropdown-item {
  display: block;
  padding: 12px 16px;
  color: #2d3748;
  text-decoration: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.dropdown-item:hover {
  background: #f7fafc;
}

/* Settings component in dropdown */
.user-dropdown .settings-component {
  padding: 8px 16px;
}

.user-dropdown .settings-btn {
  width: 100%;
  justify-content: flex-start;
  background: none !important;
  color: #2d3748 !important;
  padding: 8px 0;
  font-weight: normal;
  box-shadow: none !important;
}

.user-dropdown .settings-btn:hover {
  background: #f7fafc !important;
  transform: none !important;
  box-shadow: none !important;
}

.auth-buttons {
  display: flex;
  gap: 12px;
}

.auth-button {
  padding: 8px 16px;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s;
}

.login-button {
  background: #667eea;
  color: white;
}

.login-button:hover {
  background: #5a67d8;
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
  }
  
  .header-center {
    order: -1;
  }
  
  .language-count {
    font-size: 0.8rem;
    padding: 0.3rem 0.8rem;
  }
  
  .logo-text h1 {
    font-size: 1.5rem;
  }
  
  .subtitle {
    font-size: 0.8rem;
  }
}
</style>
