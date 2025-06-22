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
    },
    handleShowNotification(message, type) {
      // This would need to be passed to a global notification system
      // For now, let's create a simple implementation
      console.log('Notification:', message, type)
    }
  },
  async mounted() {
    // Load user's theme preference
    try {
      const settings = await getUserSettings()
      this.currentTheme = settings.theme || 'dark'
      // Apply theme to document root for global CSS variables
      document.documentElement.setAttribute('data-theme', this.currentTheme)
    } catch (error) {
      console.log('Using default theme (dark)')
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
