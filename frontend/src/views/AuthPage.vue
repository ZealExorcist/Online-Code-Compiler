<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <div class="logo-container">
          <img v-if="logoUrl" :src="logoUrl" alt="Logo" class="auth-logo" />
          <h1 class="auth-title">Online Compiler</h1>
        </div>
        <p class="auth-subtitle">{{ isLogin ? 'Sign in to your account' : 'Create a new account' }}</p>
      </div>

      <form @submit.prevent="handleSubmit" class="auth-form">
        <div class="form-group">
          <label for="username">Username</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            required
            :disabled="loading"
            class="form-input"
            placeholder="Enter your username"
          />
        </div>

        <div v-if="!isLogin" class="form-group">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            required
            :disabled="loading"
            class="form-input"
            placeholder="Enter your email"
          />
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            required
            :disabled="loading"
            class="form-input"
            placeholder="Enter your password"
          />
        </div>

        <div v-if="!isLogin" class="form-group">
          <label for="confirmPassword">Confirm Password</label>
          <input
            id="confirmPassword"
            v-model="form.confirmPassword"
            type="password"
            required
            :disabled="loading"
            class="form-input"
            placeholder="Confirm your password"
          />
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>        <button type="submit" :disabled="loading" class="auth-submit-button">
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else class="button-content">
            <span class="button-icon">{{ isLogin ? '🚀' : '✨' }}</span>
            {{ isLogin ? 'Sign In' : 'Sign Up' }}
          </span>
        </button>
      </form>

      <div class="auth-switch">        <p>
          {{ isLogin ? "Don't have an account?" : 'Already have an account?' }}
          <button @click="toggleMode" class="switch-button">
            <span class="switch-icon">{{ isLogin ? '➕' : '👤' }}</span>
            {{ isLogin ? 'Sign Up' : 'Sign In' }}
          </button>
        </p>
      </div>

      <div class="auth-divider">
        <span>or</span>
      </div>      <div class="guest-access">
        <button @click="continueAsGuest" class="guest-button">
          <span class="guest-icon">👻</span>
          Continue as Guest
        </button>
        <p class="guest-note">
          🔒 Limited functionality • ⚡ No rate limits • 🎯 Quick start
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import authService from '../services/auth'

export default {
  name: 'AuthPage',
  data() {
    return {
      isLogin: true,
      loading: false,
      error: '',
      form: {
        username: '',
        email: '',
        password: '',
        confirmPassword: ''
      },
      logoUrl: '/logo.png' // You can customize this
    }
  },
  methods: {
    async handleSubmit() {
      this.error = ''
      
      if (!this.validateForm()) {
        return
      }

      this.loading = true

      try {        if (this.isLogin) {
          await authService.login(this.form.username, this.form.password)
        } else {
          await authService.register(this.form.username, this.form.email, this.form.password, this.form.confirmPassword)
        }
        
        this.$router.push('/')
      } catch (error) {
        this.error = error.message
      } finally {
        this.loading = false
      }
    },

    validateForm() {
      if (!this.form.username.trim()) {
        this.error = 'Username is required'
        return false
      }

      if (!this.form.password.trim()) {
        this.error = 'Password is required'
        return false
      }

      if (!this.isLogin) {
        if (!this.form.email.trim()) {
          this.error = 'Email is required'
          return false
        }

        if (this.form.password !== this.form.confirmPassword) {
          this.error = 'Passwords do not match'
          return false
        }

        if (this.form.password.length < 6) {
          this.error = 'Password must be at least 6 characters'
          return false
        }
      }

      return true
    },

    toggleMode() {
      this.isLogin = !this.isLogin
      this.error = ''
      this.form.confirmPassword = ''
    },

    continueAsGuest() {
      this.$router.push('/')
    }
  },
  mounted() {
    // Check if user is already authenticated
    if (authService.isAuthenticated()) {
      this.$router.push('/')
      return
    }
    
    // Check URL parameters for signup mode
    const urlParams = new URLSearchParams(window.location.search)
    if (urlParams.get('mode') === 'signup') {
      this.isLogin = false
    }
  }
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.auth-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 420px;
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.auth-logo {
  width: 60px;
  height: 60px;
  border-radius: 8px;
}

.auth-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
}

.auth-subtitle {
  color: #718096;
  margin: 10px 0 0 0;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #2d3748;
  font-size: 14px;
}

.form-input {
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
}

.form-input:disabled {
  background-color: #f7fafc;
  cursor: not-allowed;
}

.error-message {
  color: #e53e3e;
  font-size: 14px;
  text-align: center;
  padding: 8px;
  background-color: #fed7d7;
  border-radius: 6px;
}

.auth-submit-button {
  padding: 16px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  min-height: 56px;
}

.auth-submit-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s;
}

.auth-submit-button:hover::before {
  left: 100%;
}

.auth-submit-button:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 12px 35px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}

.auth-submit-button:active:not(:disabled) {
  transform: translateY(-1px);
}

.auth-submit-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  background: linear-gradient(135deg, #a0a0a0 0%, #808080 100%);
}

.button-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.button-icon {
  font-size: 18px;
  transition: transform 0.3s ease;
}

.auth-submit-button:hover .button-icon {
  transform: scale(1.1) rotate(10deg);
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.auth-switch {
  text-align: center;
  margin: 24px 0;
}

.auth-switch p {
  color: #718096;
  font-size: 14px;
  margin: 0;
}

.switch-button {
  background: none;
  border: none;
  color: #667eea;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  margin-left: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  position: relative;
  overflow: hidden;
}

.switch-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 20px;
}

.switch-button:hover::before {
  opacity: 1;
}

.switch-button:hover {
  color: #5a6fd8;
  transform: translateY(-1px);
}

.switch-icon {
  font-size: 14px;
  transition: transform 0.3s ease;
}

.switch-button:hover .switch-icon {
  transform: scale(1.2);
}

.auth-divider {
  text-align: center;
  margin: 20px 0;
  position: relative;
}

.auth-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e2e8f0;
}

.auth-divider span {
  background: white;
  padding: 0 15px;
  color: #718096;
  font-size: 14px;
}

.guest-access {
  text-align: center;
}

.guest-button {
  padding: 14px 28px;
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  color: #2d3748;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.guest-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(45, 55, 72, 0.05), transparent);
  transition: left 0.5s;
}

.guest-button:hover::before {
  left: 100%;
}

.guest-button:hover {
  background: linear-gradient(135deg, #edf2f7 0%, #e2e8f0 100%);
  border-color: #cbd5e0;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  color: #1a202c;
}

.guest-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.guest-button:hover .guest-icon {
  transform: scale(1.1) rotate(-10deg);
}

.guest-note {
  font-size: 12px;
  color: #718096;
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
