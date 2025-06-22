<template>
  <div id="app" :data-theme="currentTheme">
    <router-view 
      @theme-changed="handleThemeChange" 
      @show-notification="handleShowNotification"
    />
  </div>
</template>

<script>
import { getUserSettings } from './services/settings'
import authService from './services/auth'

export default {
  name: 'App',
  data() {
    return {
      currentTheme: 'dark'
    }
  },
  methods: {
    handleThemeChange(newTheme) {
      this.currentTheme = newTheme
      document.documentElement.setAttribute('data-theme', newTheme)
      // Cache theme for anonymous users
      if (!authService.isAuthenticated()) {
        localStorage.setItem('compiler-theme', newTheme)
      }
    },
    handleShowNotification(message, type) {
      // This would need to be passed to a global notification system
      // For now, let's create a simple implementation
      // TODO: Implement proper toast notification system
    }
  },
  async mounted() {
    // Load user's theme preference
    try {
      if (authService.isAuthenticated()) {
        const settings = await getUserSettings()
        this.currentTheme = settings.theme || 'dark'
      } else {
        // For anonymous users, try to get cached theme from localStorage
        const cachedTheme = localStorage.getItem('compiler-theme')
        this.currentTheme = cachedTheme || 'dark'
      }
      // Apply theme to document root for global CSS variables
      document.documentElement.setAttribute('data-theme', this.currentTheme)
    } catch (error) {
      // Silent fallback to default theme
      this.currentTheme = 'dark'
      document.documentElement.setAttribute('data-theme', 'dark')
    }
  }
}
</script>

<style>
#app {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  margin: 0;
  background-color: var(--bg-primary);
  transition: background-color 0.3s ease;
}
</style>
