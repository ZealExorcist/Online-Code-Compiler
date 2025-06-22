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
              </div>

              <div class="setting-group">
                <label>Tab Size</label>
                <select v-model="settings.tabSize" @change="updateSettings">
                  <option :value="2">2 spaces</option>
                  <option :value="4">4 spaces</option>
                  <option :value="8">8 spaces</option>
                </select>
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

.settings-component[data-theme="dark"] .checkbox-group input[type="checkbox"] {
  background: #374151;
  border-color: #4b5563;
}

.settings-component[data-theme="dark"] .checkbox-group input[type="checkbox"]:checked {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
}

.user-info {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row .label {
  color: #6b7280;
  font-size: 14px;
}

.info-row .value {
  color: #1f2937;
  font-weight: 500;
  font-size: 14px;
}

.tier-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.api-key-section,
.password-section {
  margin-top: 24px;
}

.api-key-section h5,
.password-section h5 {
  margin: 0 0 12px 0;
  color: #374151;
  font-size: 16px;
  font-weight: 600;
}

.api-key-display {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.api-key-input {
  flex: 1;
  font-family: monospace;
  font-size: 12px;
}

.copy-btn,
.regenerate-btn,
.change-password-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.copy-btn:hover,
.regenerate-btn:hover,
.change-password-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.copy-btn:disabled,
.regenerate-btn:disabled,
.change-password-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.regenerate-btn {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  margin-top: 8px;
}

.change-password-btn {
  margin-top: 16px;
}

.success-message,
.error-message {
  position: absolute;
  bottom: 20px;
  right: 20px;
  padding: 12px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  z-index: 1001;
}

.success-message {
  background: #10b981;
  color: white;
}

.error-message {
  background: #ef4444;
  color: white;
}

.snippets-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.snippet-item {
  background: var(--snippet-bg, #f9fafb);
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 8px;
  padding: 16px;
  transition: all 0.2s;
}

.snippet-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.snippet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.snippet-header h6 {
  margin: 0;
  color: var(--text-color, #1f2937);
  font-size: 16px;
  font-weight: 600;
}

.snippet-language {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.snippet-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 14px;
  color: var(--meta-color, #6b7280);
}

.snippet-preview {
  margin-bottom: 12px;
}

.snippet-preview pre {
  background: var(--code-bg, #f3f4f6);
  border: 1px solid var(--border-color, #d1d5db);
  border-radius: 4px;
  padding: 8px;
  font-size: 12px;
  color: var(--code-color, #374151);
  overflow: hidden;
  white-space: pre-wrap;
  margin: 0;
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
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.load-snippet-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.share-snippet-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
}

.delete-snippet-btn {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

.load-snippet-btn:hover,
.share-snippet-btn:hover,
.delete-snippet-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--meta-color, #6b7280);
}

/* Delete Modal Styles */
.delete-modal-overlay {
  backdrop-filter: blur(4px);
}

.delete-modal-content {
  max-width: 500px;
  width: 90vw;
}

.delete-modal-header {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  border-radius: 12px 12px 0 0;
}

.delete-modal-header h3 {
  color: white;
}

.delete-modal-header .close-btn {
  color: white;
}

.delete-modal-header .close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.delete-modal-body {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 24px;
}

.warning-icon {
  font-size: 48px;
  color: #f59e0b;
  flex-shrink: 0;
}

.warning-content h4 {
  margin: 0 0 12px 0;
  color: #1f2937;
  font-size: 18px;
  font-weight: 600;
}

.warning-content p {
  margin: 0 0 8px 0;
  color: #374151;
  line-height: 1.5;
}

.warning-text {
  color: #6b7280 !important;
  font-size: 14px;
}

.delete-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 0 0 12px 12px;
}

.delete-modal-footer .btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.delete-modal-footer .btn-cancel {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.delete-modal-footer .btn-cancel:hover {
  background: #e5e7eb;
}

.delete-modal-footer .btn-delete {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

.delete-modal-footer .btn-delete:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}

.delete-modal-footer .btn-delete:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Dark mode support for delete modal */
.settings-component[data-theme="dark"] .delete-modal-content {
  background: #1f2937;
  color: #f9fafb;
}

.settings-component[data-theme="dark"] .delete-modal-body {
  background: #1f2937;
}

.settings-component[data-theme="dark"] .warning-content h4 {
  color: #f9fafb;
}

.settings-component[data-theme="dark"] .warning-content p {
  color: #d1d5db;
}

.settings-component[data-theme="dark"] .delete-modal-footer {
  background: #374151;
  border-color: #4b5563;
}

.settings-component[data-theme="dark"] .delete-modal-footer .btn-cancel {
  background: #374151;
  color: #d1d5db;
  border-color: #4b5563;
}

.settings-component[data-theme="dark"] .delete-modal-footer .btn-cancel:hover {
  background: #4b5563;
}

/* Dark mode support */
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
