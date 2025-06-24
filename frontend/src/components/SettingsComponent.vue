<template>  <div class="settings-component" :data-theme="currentTheme">
    <!-- Settings Modal -->
    <div class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Settings</h3>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>

        <div class="modal-body">
          <div class="settings-nav">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              @click="activeTab = tab.id"
              :class="['nav-button', { active: activeTab === tab.id }]"
            >
              {{ tab.name }}
            </button>
          </div>

          <div class="settings-panel">
            <!-- Editor Settings -->
            <div v-if="activeTab === 'editor'" class="settings-section">
              <h4>Editor Settings</h4>
              
              <div class="setting-group">
                <label>Theme</label>
                <select v-model="settings.theme" @change="updateSettings">
                  <option value="dark">Dark</option>
                  <option value="light">Light</option>
                </select>
              </div>              <div class="setting-group">
                <label>Editor Color Scheme</label>
                <select v-model="settings.colorScheme" @change="updateSettings">
                  <option value="oneDark">One Dark</option>
                  <option value="oneLight">One Light</option>
                  <option value="githubLight">GitHub Light</option>
                  <option value="monokai">Monokai</option>
                  <option value="vscodeDark">VS Code Dark</option>
                  <option value="vscodeLight">VS Code Light</option>
                </select>
              </div>              <div class="setting-group">
                <label>Font Size</label>
                <select v-model="settings.fontSize" @change="updateSettings">
                  <option value="12px">12px</option>
                  <option value="14px">14px</option>
                  <option value="16px">16px</option>
                  <option value="18px">18px</option>
                  <option value="20px">20px</option>
                </select>
              </div>              <div class="setting-group">
                <label>Tab Size</label>
                <select v-model="settings.tabSize" @change="updateSettings">
                  <option :value="2">2 spaces</option>
                  <option :value="4">4 spaces</option>
                  <option :value="8">8 spaces</option>
                </select>
              </div>

              <div class="setting-group checkbox-group">
                <label>
                  <input 
                    type="checkbox" 
                    v-model="settings.enableErrorHighlighting" 
                    @change="updateSettings"
                  />
                  Enable Error Highlighting
                </label>
                <small class="setting-help">Show syntax errors and warnings in real-time</small>
              </div>
            </div>

            <!-- Execution Settings -->
            <div v-if="activeTab === 'execution'" class="settings-section">
              <h4>Execution Settings</h4>
              
              <div class="setting-group">
                <label>Max Execution Time (seconds)</label>
                <select v-model="settings.maxExecutionTime" @change="updateSettings">
                  <option :value="10">10 seconds</option>
                  <option :value="30">30 seconds</option>
                  <option :value="60">1 minute</option>
                  <option :value="120">2 minutes</option>
                </select>
              </div>

              <div class="setting-group">
                <label>Max Output Size (KB)</label>
                <select v-model="settings.maxOutputSize" @change="updateSettings">
                  <option :value="1024">1 KB</option>
                  <option :value="5120">5 KB</option>
                  <option :value="10240">10 KB</option>
                  <option :value="20480">20 KB</option>
                </select>
              </div>

              <div class="setting-group checkbox-group">
                <label>
                  <input 
                    type="checkbox" 
                    v-model="settings.enableInput" 
                    @change="updateSettings"
                  />
                  Enable Input Panel
                </label>
              </div>            </div>            <!-- Account Settings -->
            <div v-if="activeTab === 'account'" class="settings-section">
              <h4>Account Settings</h4>
              
              <div class="user-info" v-if="userProfile">
                <div class="info-row">
                  <span class="label">Username:</span>
                  <span class="value">{{ userProfile.username }}</span>
                </div>
                <div class="info-row">
                  <span class="label">Email:</span>
                  <span class="value">{{ userProfile.email }}</span>
                </div>
                <div class="info-row">
                  <span class="label">Plan:</span>
                  <span class="value tier-badge">{{ userProfile.tier || 'BASIC' }}</span>
                </div>
                <div class="info-row">
                  <span class="label">Member Since:</span>
                  <span class="value">{{ formatDate(userProfile.createdAt) }}</span>
                </div>
              </div>

              <div class="api-key-section">
                <h5>API Key</h5>
                <div class="api-key-display">
                  <input 
                    type="text" 
                    :value="userProfile?.apiKey || 'Loading...'" 
                    readonly 
                    class="api-key-input"
                    ref="apiKeyInput"
                  />
                  <button @click="copyApiKey" class="copy-btn">
                    {{ copied ? 'Copied!' : 'Copy' }}
                  </button>
                </div>
                <button @click="regenerateApiKey" class="regenerate-btn" :disabled="isRegenerating">
                  {{ isRegenerating ? 'Regenerating...' : 'Regenerate API Key' }}
                </button>
              </div>

              <div class="password-section">
                <h5>Change Password</h5>
                <div class="setting-group">
                  <label>Current Password</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.oldPassword" 
                    placeholder="Enter current password"
                  />
                </div>
                <div class="setting-group">
                  <label>New Password</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.newPassword" 
                    placeholder="Enter new password"
                  />
                </div>
                <div class="setting-group">
                  <label>Confirm New Password</label>
                  <input 
                    type="password" 
                    v-model="passwordForm.confirmPassword" 
                    placeholder="Confirm new password"
                  />
                </div>
                <button @click="changePassword" class="change-password-btn" :disabled="isChangingPassword">
                  {{ isChangingPassword ? 'Changing...' : 'Change Password' }}
                </button>
              </div>
            </div>

            <!-- Privacy Settings -->
            <div v-if="activeTab === 'privacy'" class="settings-section">
              <h4>Privacy Settings</h4>
              
              <div class="setting-group checkbox-group">
                <label>
                  <input 
                    type="checkbox" 
                    v-model="settings.publicSnippets" 
                    @change="updateSettings"
                  />
                  Make my snippets public by default
                </label>
              </div>

              <div class="setting-group checkbox-group">
                <label>
                  <input 
                    type="checkbox" 
                    v-model="settings.shareByDefault" 
                    @change="updateSettings"
                  />
                  Enable sharing by default
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- Success/Error Messages -->
        <div v-if="successMessage" class="success-message">
          <p>✅ {{ successMessage }}</p>
        </div>        <div v-if="errorMessage" class="error-message">
          <p>❌ {{ errorMessage }}</p>
        </div>
      </div>
    </div>
    </div>
</template>

<script>
import { getUserSettings, updateUserSettings, getUserProfile, changePassword, generateNewApiKey } from '../services/settings'
import authService from '../services/auth'

export default {
  name: 'SettingsComponent',  data() {
    return {
      activeTab: 'editor',      settings: {
        theme: 'dark',
        colorScheme: 'oneDark',
        fontSize: '14px',
        tabSize: 4,
        maxExecutionTime: 30,
        maxOutputSize: 10240,
        enableInput: true,
        publicSnippets: false,
        shareByDefault: false
      },userProfile: null,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },      tabs: [
        { id: 'editor', name: 'Editor' },
        { id: 'execution', name: 'Execution' },
        { id: 'account', name: 'Account' },
        { id: 'privacy', name: 'Privacy' }
      ],copied: false,
      isRegenerating: false,      isChangingPassword: false,
      isDeleting: false,
      successMessage: '',
      errorMessage: ''
    }
  },
  computed: {
    currentTheme() {
      return this.settings.theme || 'light'
    }
  },
  watch: {
    'settings.theme'(newTheme, oldTheme) {
      // Prevent modal from closing when theme changes
      // Just update the data-theme attribute
      if (newTheme !== oldTheme) {
        this.$nextTick(() => {
          document.documentElement.setAttribute('data-theme', newTheme)
        })
      }
    }  },
  mounted() {
    this.loadSettings()
  },
  methods: {    async loadSettings() {
      try {
        if (authService.isAuthenticated()) {
          const settings = await getUserSettings()
          this.settings = { ...this.settings, ...settings }
        } else {
          // Load from localStorage for non-authenticated users
          const cached = localStorage.getItem('user_settings')
          if (cached) {
            const cachedSettings = JSON.parse(cached)
            this.settings = { ...this.settings, ...cachedSettings }
          }
        }
      } catch (error) {
        console.error('Failed to load settings:', error)
        // Fall back to localStorage
        const cached = localStorage.getItem('user_settings')
        if (cached) {
          try {
            const cachedSettings = JSON.parse(cached)
            this.settings = { ...this.settings, ...cachedSettings }
          } catch (parseError) {
            console.error('Failed to parse cached settings:', parseError)
          }
        }
      }
    },

    async loadUserProfile() {
      try {        this.userProfile = await getUserProfile()
      } catch (error) {
        console.error('Failed to load user profile:', error)
      }
    },    async updateSettings() {
      try {
        // Always cache settings locally for immediate feedback
        localStorage.setItem('user_settings', JSON.stringify(this.settings))
        
        // Try to save to server if authenticated
        if (authService.isAuthenticated()) {
          await updateUserSettings(this.settings)
          this.showSuccess('Settings updated successfully!')
        } else {
          this.showSuccess('Settings saved locally!')
        }
        
        this.$emit('settings-updated', this.settings)
        // Don't close modal when settings are updated
      } catch (error) {
        console.error('Settings update error:', error)
        // Even if server update fails, keep local settings
        this.showError('Settings saved locally but server update failed: ' + error.message)
        this.$emit('settings-updated', this.settings)
      }
    },

    async copyApiKey() {
      try {
        await navigator.clipboard.writeText(this.userProfile.apiKey)
        this.copied = true
        setTimeout(() => {
          this.copied = false
        }, 2000)
      } catch (err) {
        // Fallback for older browsers
        this.$refs.apiKeyInput.select()
        document.execCommand('copy')
        this.copied = true
        setTimeout(() => {
          this.copied = false
        }, 2000)
      }
    },

    async regenerateApiKey() {
      this.isRegenerating = true
      try {
        const response = await generateNewApiKey()
        this.userProfile.apiKey = response.apiKey
        this.showSuccess('API key regenerated successfully!')
      } catch (error) {
        this.showError('Failed to regenerate API key: ' + error.message)
      } finally {
        this.isRegenerating = false
      }
    },    async changePassword() {
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.showError('New passwords do not match')
        return
      }

      this.isChangingPassword = true
      try {
        await changePassword(this.passwordForm.oldPassword, this.passwordForm.newPassword)
        this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
        this.showSuccess('Password changed successfully!')
      } catch (error) {
        this.showError('Failed to change password: ' + error.message)
      } finally {
        this.isChangingPassword = false
      }
    },

    formatDate(dateString) {
      if (!dateString) return 'Unknown'
      return new Date(dateString).toLocaleDateString()
    },

    showSuccess(message) {
      this.successMessage = message
      this.errorMessage = ''
      setTimeout(() => {
        this.successMessage = ''
      }, 3000)
    },

    showError(message) {
      this.errorMessage = message
      this.successMessage = ''
      setTimeout(() => {
        this.errorMessage = ''
      }, 5000)
    },    closeModal() {
      this.activeTab = 'editor'
      this.successMessage = ''
      this.errorMessage = ''
      this.$emit('close')
    }
  },

  async mounted() {    await this.loadSettings()
    await this.loadUserProfile()
  }
}
</script>

<style scoped>
.settings-component {
  position: relative;
}

.settings-btn {
  background: var(--btn-bg, linear-gradient(135deg, #6c757d 0%, #495057 100%));
  color: var(--btn-text, white);
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.settings-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(108, 117, 125, 0.4);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--modal-bg, white);
  border-radius: 12px;
  padding: 0;
  max-width: 900px;
  width: 90vw;
  height: 80vh;
  min-height: 600px;
  max-height: 800px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  color: var(--text-color, #1f2937);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color, #e5e7eb);
  background: var(--header-bg, #f9fafb);
  flex-shrink: 0;
}

.modal-header h3 {
  margin: 0;
  color: var(--text-color, #1f2937);
  font-size: 20px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s;
}

.close-btn:hover {
  background: #e5e7eb;
}

.modal-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.settings-nav {
  min-width: 200px;
  width: 200px;
  background: var(--nav-bg, #f9fafb);
  border-right: 1px solid var(--border-color, #e5e7eb);
  padding: 20px 0;
  flex-shrink: 0;
}

.nav-button {
  display: block;
  width: 100%;
  padding: 12px 24px;
  border: none;
  background: none;
  text-align: left;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-button:hover {
  background: #e5e7eb;
  color: #374151;
}

.nav-button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 500;
}

.settings-panel {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  min-height: 0;
}

.settings-section h4 {
  margin: 0 0 20px 0;
  color: #1f2937;
  font-size: 18px;
  font-weight: 600;
}

.setting-group {
  margin-bottom: 20px;
}

.setting-group label {
  display: block;
  margin-bottom: 6px;
  color: #374151;
  font-weight: 500;
  font-size: 14px;
}

.setting-group select,
.setting-group input[type="text"],
.setting-group input[type="password"] {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.setting-group select:focus,
.setting-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.checkbox-group label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 0;
}

.checkbox-group input[type="checkbox"] {
  width: 20px;
  height: 20px;
  margin: 0;
  appearance: none;
  border: 2px solid #d1d5db;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.checkbox-group input[type="checkbox"]:checked {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
}

.checkbox-group input[type="checkbox"]:checked::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 14px;
  font-weight: bold;
}

.checkbox-group input[type="checkbox"]:hover {
  border-color: #667eea;
}

.setting-help {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-secondary, #6b7280);
  font-style: italic;
  line-height: 1.4;
}

.settings-component[data-theme="dark"] .setting-help {
  color: #9ca3af;
}

.settings-component[data-theme="dark"] {
  --btn-bg: linear-gradient(135deg, #4b5563 0%, #374151 100%);
  --btn-text: #f9fafb;
}

.settings-component[data-theme="dark"] .modal-content {
  --modal-bg: #1f2937;
  --text-color: #f9fafb;
  --header-bg: #374151;
  --nav-bg: #374151;
  --border-color: #4b5563;
}

.settings-component[data-theme="light"] {
  --btn-bg: linear-gradient(135deg, #6c757d 0%, #495057 100%);
  --btn-text: white;
}

.settings-component[data-theme="light"] .modal-content {
  --modal-bg: white;
  --text-color: #1f2937;
  --header-bg: #f9fafb;
  --nav-bg: #f9fafb;
  --border-color: #e5e7eb;
}

.settings-component[data-theme="dark"] .nav-button {
  color: #d1d5db;
}

.settings-component[data-theme="dark"] .nav-button:hover {
  background: #4b5563;
  color: #f9fafb;
}

.settings-component[data-theme="dark"] .nav-button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.settings-component[data-theme="dark"] .setting-group label {
  color: #d1d5db;
}

.settings-component[data-theme="dark"] .setting-group select,
.settings-component[data-theme="dark"] .setting-group input[type="text"],
.settings-component[data-theme="dark"] .setting-group input[type="password"] {
  background: #374151;
  border-color: #4b5563;
  color: #f9fafb;
}

.settings-component[data-theme="dark"] .user-info {
  background: #374151;
  border-color: #4b5563;
}

.settings-component[data-theme="dark"] .info-row .label {
  color: #9ca3af;
}

.settings-component[data-theme="dark"] .info-row .value {
  color: #f9fafb;
}

.settings-component[data-theme="dark"] .close-btn:hover {
  background: #4b5563;
}

.settings-component[data-theme="dark"] .snippet-item {
  --snippet-bg: #374151;
  --code-bg: #1f2937;
  --code-color: #d1d5db;
  --meta-color: #9ca3af;
}

@media (max-width: 768px) {
  .modal-content {
    width: 95vw;
    height: 95vh;
  }
  
  .modal-body {
    flex-direction: column;
  }
  
  .settings-nav {
    min-width: auto;
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
    padding: 12px 0;
  }
  
  .nav-button {
    padding: 8px 16px;
    display: inline-block;
    width: auto;
    margin: 0 4px;
    border-radius: 6px;
  }
  
  .api-key-display {
    flex-direction: column;
  }
}
</style>
