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
            <div class="dropdown-item" @click="showSettingsModal = true">
              ⚙️ Settings
            </div>
            <SettingsComponent 
              v-if="showSettingsModal"
              @settings-updated="handleSettingsUpdate"
              @load-snippet="handleLoadSnippet" 
              @close="showSettingsModal = false"
            />
            <div class="dropdown-item" @click="showSnippetsModal">
              📄 My Snippets
            </div>
            <div class="dropdown-item" @click="showUpgradeModal = true">
              ⭐ Upgrade Plan
            </div>
            <UpgradeComponent 
              v-if="showUpgradeModal"
              @tier-updated="handleTierUpdated"
              @close="showUpgradeModal = false"
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
    
    <!-- My Snippets Modal -->
    <div v-if="showSnippetsModalOpen" class="modal-overlay" @click="closeSnippetsModal">
      <div class="modal-content snippets-modal" @click.stop>
        <div class="modal-header">
          <h3>📄 My Snippets</h3>
          <button @click="closeSnippetsModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <div v-if="isLoadingSnippets" class="loading-state">
            <div class="spinner"></div>
            <p>Loading snippets...</p>
          </div>
          
          <div v-else-if="snippets.length === 0" class="empty-state">
            <p>No snippets saved yet. Create and save your first snippet!</p>
          </div>
          
          <div v-else class="snippets-list">
            <div 
              v-for="snippet in snippets" 
              :key="snippet.id"
              class="snippet-item"
            >
              <div class="snippet-header">
                <h6>{{ snippet.title || 'Untitled Snippet' }}</h6>
                <span class="snippet-language">{{ snippet.language }}</span>
              </div>
              <div class="snippet-meta">
                <span class="snippet-date">{{ formatDate(snippet.createdAt) }}</span>
                <span class="snippet-length">{{ snippet.code.length }} chars</span>
              </div>
              <div class="snippet-preview">
                <pre>{{ snippet.code.substring(0, 150) }}{{ snippet.code.length > 150 ? '...' : '' }}</pre>
              </div>
              <div class="snippet-actions">
                <button @click="loadSnippet(snippet)" class="load-snippet-btn">
                  Load
                </button>
                <button @click="shareSnippet(snippet)" class="share-snippet-btn">
                  Share
                </button>
                <button @click="deleteSnippet(snippet.id)" class="delete-snippet-btn">
                  Delete
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay delete-modal-overlay" @click="closeDeleteModal">
      <div class="modal-content delete-modal-content" @click.stop>
        <div class="modal-header delete-modal-header">
          <h3>🗑️ Delete Snippet</h3>
          <button @click="closeDeleteModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body delete-modal-body">
          <div class="warning-icon">⚠️</div>
          <div class="warning-content">
            <h4>Are you sure you want to delete this snippet?</h4>
            <p v-if="snippetToDelete">
              <strong>"{{ snippetToDelete.title || 'Untitled Snippet' }}"</strong>
            </p>
            <p class="warning-text">This action cannot be undone. The snippet will be permanently removed from your account.</p>
          </div>
        </div>
        <div class="modal-footer delete-modal-footer">
          <button @click="closeDeleteModal" class="btn btn-cancel">
            Cancel
          </button>
          <button @click="confirmDeleteSnippet" class="btn btn-delete" :disabled="isDeleting">
            <span v-if="isDeleting">⏳ Deleting...</span>
            <span v-else>🗑️ Delete Snippet</span>
          </button>
        </div>
      </div>
    </div>
    
    <!-- Toast Notifications -->
    <div class="toast-container">
      <div 
        v-for="toast in toasts" 
        :key="toast.id"
        :class="['toast', `toast-${toast.type}`, { 'toast-leaving': toast.leaving }]"
      >
        <div class="toast-icon">
          <span v-if="toast.type === 'success'">✅</span>
          <span v-else-if="toast.type === 'error'">❌</span>
          <span v-else-if="toast.type === 'warning'">⚠️</span>
          <span v-else>ℹ️</span>
        </div>
        <div class="toast-content">
          <div class="toast-title">{{ toast.title }}</div>
          <div v-if="toast.message" class="toast-message">{{ toast.message }}</div>
        </div>
        <button @click="removeToast(toast.id)" class="toast-close">&times;</button>
      </div>
    </div>
  </header>
</template>

<script>
import authService from '../services/auth'
import SettingsComponent from './SettingsComponent.vue'
import UpgradeComponent from './UpgradeComponent.vue'

export default {
  name: 'Header',
  components: {
    SettingsComponent,
    UpgradeComponent
  },
  data() {
    return {
      languageCount: 10,
      showUserMenu: false,
      showSettingsModal: false,
      showUpgradeModal: false,
      logoUrl: '/logo.png', // You can add your logo here
      showSnippetsModalOpen: false,
      snippets: [],
      isLoadingSnippets: false,
      showDeleteModal: false,
      snippetToDelete: null,
      isDeleting: false,
      toasts: [],
      toastId: 0
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
      // Emit theme change globally when settings are updated
      if (settings.theme) {
        this.$emit('theme-changed', settings.theme)
      }
      // Emit settings update to parent
      this.$emit('settings-updated', settings)
      // Don't close user menu when settings are updated to prevent modal from closing
      // this.showUserMenu = false
    },
    handleLoadSnippet(snippet) {
      this.$emit('load-snippet', snippet)
      this.showUserMenu = false
    },
    
    async showSnippetsModal() {
      this.showSnippetsModalOpen = true
      this.showUserMenu = false
      await this.loadSnippets()
    },
    
    closeSnippetsModal() {
      this.showSnippetsModalOpen = false
    },
    
    async loadSnippets() {
      this.isLoadingSnippets = true
      try {
        const response = await fetch('/api/user/snippets', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('auth_token')}`,
            'Content-Type': 'application/json'
          }
        })
        if (response.ok) {
          this.snippets = await response.json()
        } else {
          console.error('Failed to load snippets:', response.statusText)
        }
      } catch (error) {
        console.error('Failed to load snippets:', error)
      } finally {
        this.isLoadingSnippets = false
      }
    },

    loadSnippet(snippet) {
      this.$emit('load-snippet', snippet)
      this.closeSnippetsModal()
    },

    async shareSnippet(snippet) {
      try {
        const response = await fetch('/api/share', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('auth_token')}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            code: snippet.code,
            language: snippet.language,
            title: snippet.title
          })
        })
        if (response.ok) {
          const result = await response.json()
          navigator.clipboard.writeText(result.shareUrl)
          this.showNotification('Share link copied to clipboard!', 'success', '🔗')
        }
      } catch (error) {
        this.showNotification('Failed to share snippet', 'error', '❌')
      }
    },

    deleteSnippet(snippetId) {
      const snippet = this.snippets.find(s => s.id === snippetId)
      this.snippetToDelete = snippet
      this.showDeleteModal = true
    },

    closeDeleteModal() {
      this.showDeleteModal = false
      this.snippetToDelete = null
      this.isDeleting = false
    },

    async confirmDeleteSnippet() {
      if (!this.snippetToDelete) return
      
      this.isDeleting = true
      
      try {
        const response = await fetch(`/api/user/snippets/${this.snippetToDelete.id}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
          }
        })
        if (response.ok) {
          this.snippets = this.snippets.filter(s => s.id !== this.snippetToDelete.id)
          this.showNotification('Snippet deleted successfully!', 'success', '🗑️')
          this.closeDeleteModal()
        } else {
          const errorData = await response.json()
          this.showNotification('Failed to delete snippet: ' + (errorData.message || 'Server error'), 'error', '❌')
        }
      } catch (error) {
        console.error('Failed to delete snippet:', error)
        this.showNotification('Failed to delete snippet', 'error', '❌')
      } finally {
        this.isDeleting = false
      }
    },
    
    formatDate(dateString) {
      if (!dateString) return 'Unknown'
      return new Date(dateString).toLocaleDateString()
    },
    
    showNotification(message, type = 'success', icon = '✅') {
      this.showToast(message, type)
    },
    
    hideNotification() {
      // Legacy method for compatibility
    },
    
    showToast(title, type = 'success', message = '', duration = 4000) {
      const id = ++this.toastId
      const toast = {
        id,
        title,
        message,
        type,
        leaving: false
      }
      
      this.toasts.push(toast)
      
      // Auto-remove after duration
      setTimeout(() => {
        this.removeToast(id)
      }, duration)
    },
    
    removeToast(id) {
      const toastIndex = this.toasts.findIndex(t => t.id === id)
      if (toastIndex > -1) {
        this.toasts[toastIndex].leaving = true
        setTimeout(() => {
          this.toasts = this.toasts.filter(t => t.id !== id)
        }, 300) // Wait for animation
      }
    },
    
    handleTierUpdated(newTier) {
      // Handle tier update - could refresh user info, emit event, etc.
      this.showUpgradeModal = false
      this.showUserMenu = false
    },
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
  background: var(--bg-primary);
  color: var(--text-primary);
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: relative;
  z-index: 100;
  transition: background-color 0.3s ease, color 0.3s ease;
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
  color: var(--text-muted);
  background: var(--bg-secondary);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  transition: background-color 0.3s ease, color 0.3s ease;
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
  background: var(--dropdown-bg);
  border-radius: 8px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  min-width: 200px;
  padding: 8px 0;
  margin-top: 8px;
  z-index: 1000;
  transition: background-color 0.3s ease;
}

.dropdown-item {
  display: block;
  padding: 12px 16px;
  color: var(--dropdown-text);
  text-decoration: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.dropdown-item:hover {
  background: var(--dropdown-hover);
}

/* Settings component in dropdown */
.user-dropdown .settings-component {
  padding: 0;
  margin: 0;
  background: transparent;
  display: block;
}

/* Snippets Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  background: var(--modal-bg);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  max-height: 90vh;
  overflow-y: auto;
  transition: background-color 0.3s ease;
}

.snippets-modal {
  max-width: 800px;
  width: 90vw;
}

.modal-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.modal-body {
  padding: 20px;
  max-height: 500px;
  overflow-y: auto;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #6c757d;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e9ecef;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #6c757d;
}

.snippets-list {
  display: grid;
  gap: 20px;
}

.snippet-item {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  background: #f8f9fa;
  transition: box-shadow 0.2s;
}

.snippet-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.snippet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.snippet-header h6 {
  margin: 0;
  font-size: 1.1rem;
  color: #2d3748;
}

.snippet-language {
  background: #667eea;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

.snippet-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 0.9rem;
  color: #6c757d;
}

.snippet-preview {
  background: #1e1e1e;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
}

.snippet-preview pre {
  margin: 0;
  color: #e9ecef;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.85rem;
  line-height: 1.4;
  white-space: pre-wrap;
  word-break: break-word;
}

.snippet-actions {
  display: flex;
  gap: 8px;
}

.load-snippet-btn,
.share-snippet-btn,
.delete-snippet-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 500;
  transition: all 0.2s;
}

.load-snippet-btn {
  background: #28a745;
  color: white;
}

.load-snippet-btn:hover {
  background: #218838;
}

.share-snippet-btn {
  background: #17a2b8;
  color: white;
}

.share-snippet-btn:hover {
  background: #138496;
}

.delete-snippet-btn {
  background: #dc3545;
  color: white;
}

.delete-snippet-btn:hover {
  background: #c82333;
}

/* Delete Modal Styles */
.delete-modal-overlay {
  z-index: 10000;
}

.delete-modal-content {
  max-width: 450px;
  width: 90vw;
}

.delete-modal-header {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
}

.delete-modal-body {
  padding: 30px;
  text-align: center;
}

.warning-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.warning-content h4 {
  color: #2d3748;
  margin-bottom: 16px;
}

.warning-text {
  color: #6c757d;
  font-size: 0.9rem;
  line-height: 1.5;
  margin: 0;
}

.delete-modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 20px 30px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-cancel {
  background: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background: #5a6268;
}

.btn-delete {
  background: #dc3545;
  color: white;
}

.btn-delete:hover:not(:disabled) {
  background: #c82333;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Toast Notifications */
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 400px;
}

.toast {
  background: var(--toast-bg);
  color: var(--toast-text);
  border: 1px solid var(--toast-border);
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 4px 12px var(--toast-shadow);
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 300px;
  animation: toast-slide-in 0.3s ease-out;
  transition: all 0.3s ease;
}

.toast.toast-leaving {
  animation: toast-slide-out 0.3s ease-out;
  opacity: 0;
  transform: translateX(100%);
}

.toast-success {
  border-left: 4px solid var(--success-color);
}

.toast-error {
  border-left: 4px solid var(--error-color);
}

.toast-warning {
  border-left: 4px solid var(--warning-color);
}

.toast-info {
  border-left: 4px solid var(--info-color);
}

.toast-icon {
  font-size: 20px;
  flex-shrink: 0;
  margin-top: 2px;
}

.toast-content {
  flex: 1;
}

.toast-title {
  font-weight: 600;
  margin-bottom: 4px;
  line-height: 1.4;
}

.toast-message {
  font-size: 14px;
  color: var(--text-muted);
  line-height: 1.4;
}

.toast-close {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  flex-shrink: 0;
  transition: all 0.2s;
}

.toast-close:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

@keyframes toast-slide-in {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes toast-slide-out {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(100%);
  }
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
  
  .toast-container {
    top: 10px;
    right: 10px;
    left: 10px;
    max-width: none;
  }
  
  .toast {
    min-width: auto;
  }
}
</style>
