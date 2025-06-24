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
                <label>Theme</label>                <select v-model="settings.theme">
                  <option value="dark">Dark</option>
                  <option value="light">Light</option>
                </select>
              </div>              <div class="setting-group">
                <label>Editor Color Scheme</label>                <select v-model="settings.colorScheme">
                  <option value="oneDark">One Dark</option>
                  <option value="oneLight">One Light</option>
                  <option value="githubLight">GitHub Light</option>
                  <option value="monokai">Monokai</option>
                  <option value="vscodeDark">VS Code Dark</option>
                  <option value="vscodeLight">VS Code Light</option>
                </select>
              </div>              <div class="setting-group">
                <label>Font Size</label>                <select v-model="settings.fontSize">
                  <option value="12px">12px</option>
                  <option value="14px">14px</option>
                  <option value="16px">16px</option>
                  <option value="18px">18px</option>
                  <option value="20px">20px</option>
                </select>
              </div>              <div class="setting-group">
                <label>Tab Size</label>                <select v-model="settings.tabSize">
                  <option :value="2">2 spaces</option>
                  <option :value="4">4 spaces</option>
                  <option :value="8">8 spaces</option>
                </select>
              </div>

              <div class="setting-group checkbox-group">
                <label>                  <input 
                    type="checkbox" 
                    v-model="settings.enableErrorHighlighting"
                  />
                  Enable Error Highlighting
                </label>                <small class="setting-help">Show syntax errors and warnings in real-time</small>
              </div>
              
              <!-- Save Button for Editor Settings -->
              <div class="save-section">
                <button @click="saveEditorSettings" class="save-btn" :disabled="isSaving">
                  {{ isSaving ? 'Saving...' : 'Save Editor Settings' }}
                </button>
              </div>
            </div>

            <!-- Execution Settings -->
            <div v-if="activeTab === 'execution'" class="settings-section">
              <h4>Execution Settings</h4>
              
              <div class="setting-group">
                <label>Max Execution Time (seconds)</label>                <select v-model="settings.maxExecutionTime">
                  <option :value="10">10 seconds</option>
                  <option :value="30">30 seconds</option>
                  <option :value="60">1 minute</option>
                  <option :value="120">2 minutes</option>
                </select>
              </div>

              <div class="setting-group">
                <label>Max Output Size (KB)</label>                <select v-model="settings.maxOutputSize">
                  <option :value="1024">1 KB</option>
                  <option :value="5120">5 KB</option>
                  <option :value="10240">10 KB</option>
                  <option :value="20480">20 KB</option>
                </select>
              </div>

              <div class="setting-group checkbox-group">
                <label>                  <input 
                    type="checkbox" 
                    v-model="settings.enableInput"
                  />Enable Input Panel
                </label>
              </div>
              
              <!-- Save Button for Execution Settings -->
              <div class="save-section">
                <button @click="saveExecutionSettings" class="save-btn" :disabled="isSaving">
                  {{ isSaving ? 'Saving...' : 'Save Execution Settings' }}
                </button>
              </div>
            </div><!-- Account Settings -->
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
              </div>              <div class="api-key-section">
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

              <div class="gemini-api-section">
                <h5>Gemini API Key</h5>
                <div class="gemini-api-display">
                  <input 
                    :type="showGeminiApiKey ? 'text' : 'password'"                    v-model="settings.geminiApiKey"
                    placeholder="Enter your Gemini API key for AI Insights"
                    class="gemini-api-input"
                  />
                  <button @click="toggleGeminiApiKeyVisibility" class="toggle-visibility-btn">
                    {{ showGeminiApiKey ? 'Hide' : 'Show' }}
                  </button>
                </div>
                <small class="setting-help">
                  Required for AI Insights feature. Get your API key from Google AI Studio.
                </small>
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
                </div>                <button @click="changePassword" class="change-password-btn" :disabled="isChangingPassword">
                  {{ isChangingPassword ? 'Changing...' : 'Change Password' }}
                </button>
              </div>
              
              <!-- Save Button for Account Settings -->
              <div class="save-section">
                <button @click="saveAccountSettings" class="save-btn" :disabled="isSaving">
                  {{ isSaving ? 'Saving...' : 'Save Account Settings' }}
                </button>
              </div>
            </div>

            <!-- Privacy Settings -->
            <div v-if="activeTab === 'privacy'" class="settings-section">
              <h4>Privacy Settings</h4>
              
              <div class="setting-group checkbox-group">
                <label>                  <input 
                    type="checkbox" 
                    v-model="settings.publicSnippets"
                  />
                  Make my snippets public by default
                </label>
              </div>

              <div class="setting-group checkbox-group">
                <label>                  <input 
                    type="checkbox" 
                    v-model="settings.shareByDefault"
                  />Enable sharing by default
                </label>
              </div>
              
              <!-- Save Button for Privacy Settings -->
              <div class="save-section">
                <button @click="savePrivacySettings" class="save-btn" :disabled="isSaving">
                  {{ isSaving ? 'Saving...' : 'Save Privacy Settings' }}
                </button>
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
        shareByDefault: false,
        enableErrorHighlighting: true,
        geminiApiKey: ''
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
      ],      copied: false,
      isRegenerating: false,
      isChangingPassword: false,
      isDeleting: false,
      isSaving: false,
      showGeminiApiKey: false,
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
      console.log('💾 SettingsComponent: Updating settings:', {
        currentSettings: this.settings,
        geminiApiKey: this.settings.geminiApiKey ? `${this.settings.geminiApiKey.substring(0, 10)}...` : 'NONE',
        geminiApiKeyLength: this.settings.geminiApiKey?.length || 0
      })
      
      try {
        // Always cache settings locally for immediate feedback
        localStorage.setItem('user_settings', JSON.stringify(this.settings))
        console.log('💾 Settings cached locally')
        
        // Try to save to server if authenticated
        if (authService.isAuthenticated()) {
          console.log('🔐 User is authenticated, saving to server...')
          await updateUserSettings(this.settings)
          this.showSuccess('Settings updated successfully!')
          console.log('✅ Settings saved to server successfully')
        } else {
          console.log('👤 User not authenticated, settings saved locally only')
          this.showSuccess('Settings saved locally!')
        }
        
        console.log('📡 Emitting settings-updated event:', this.settings)
        this.$emit('settings-updated', this.settings)
        // Don't close modal when settings are updated
      } catch (error) {
        console.error('❌ Settings update error:', error)
        // Even if server update fails, keep local settings
        this.showError('Settings saved locally but server update failed: ' + error.message)
        this.$emit('settings-updated', this.settings)
      }
    },

    async saveEditorSettings() {
      this.isSaving = true
      try {
        await this.updateSettings()
        this.showSuccess('Editor settings saved successfully!')
      } catch (error) {
        this.showError('Failed to save editor settings: ' + error.message)
      } finally {
        this.isSaving = false
      }
    },

    async saveExecutionSettings() {
      this.isSaving = true
      try {
        await this.updateSettings()
        this.showSuccess('Execution settings saved successfully!')
      } catch (error) {
        this.showError('Failed to save execution settings: ' + error.message)
      } finally {
        this.isSaving = false
      }
    },

    async saveAccountSettings() {
      this.isSaving = true
      try {
        await this.updateSettings()
        this.showSuccess('Account settings saved successfully!')
      } catch (error) {
        this.showError('Failed to save account settings: ' + error.message)
      } finally {
        this.isSaving = false
      }
    },

    async savePrivacySettings() {
      this.isSaving = true
      try {
        await this.updateSettings()
        this.showSuccess('Privacy settings saved successfully!')
      } catch (error) {
        this.showError('Failed to save privacy settings: ' + error.message)
      } finally {
        this.isSaving = false
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
      return new Date(dateString).toLocaleDateString()    },

    toggleGeminiApiKeyVisibility() {
      this.showGeminiApiKey = !this.showGeminiApiKey
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
  background: var(--accent-color);
  color: white;
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
  filter: brightness(1.1);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--overlay-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--bg-secondary);
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
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-tertiary);
  flex-shrink: 0;
}

.modal-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 20px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--text-muted);
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
  background: var(--bg-primary);
}

.modal-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.settings-nav {
  min-width: 200px;
  width: 200px;
  background: var(--bg-tertiary);
  border-right: 1px solid var(--border-color);
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
  color: var(--text-muted);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-button:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.nav-button.active {
  background: var(--accent-color);
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
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}

.save-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: flex-end;
}

.save-btn {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.save-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.4);
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.setting-group {
  margin-bottom: 20px;
}

.setting-group label {
  display: block;
  margin-bottom: 6px;
  color: var(--text-primary);
  font-weight: 500;
  font-size: 14px;
}

.setting-group select,
.setting-group input[type="text"],
.setting-group input[type="password"] {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 14px;
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: border-color 0.2s;
}

.setting-group select:focus,
.setting-group input:focus {
  outline: none;
  border-color: var(--accent-color);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.setting-group input::placeholder {
  color: var(--text-muted);
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
  border: 2px solid var(--border-color);
  border-radius: 4px;
  background: var(--bg-primary);
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.checkbox-group input[type="checkbox"]:checked {
  background: var(--accent-color);
  border-color: var(--accent-color);
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
  border-color: var(--accent-color);
}

.setting-help {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
  font-style: italic;
  line-height: 1.4;
}

/* User Info Section */
.user-info {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-color);
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  color: var(--text-muted);
  font-weight: 500;
}

.info-row .value {
  color: var(--text-primary);
}

.tier-badge {
  background: var(--accent-color);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
}

/* API Key Sections */
.api-key-section,
.gemini-api-section {
  margin-bottom: 24px;
}

.api-key-section h5,
.gemini-api-section h5 {
  margin: 0 0 8px 0;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 600;
}

.api-key-display,
.gemini-api-display {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.api-key-input,
.gemini-api-input {
  flex: 1;
  font-family: monospace;
  font-size: 12px;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 8px 12px;
  border-radius: 6px;
}

.copy-btn,
.toggle-visibility-btn,
.regenerate-btn,
.change-password-btn {
  padding: 8px 12px;
  background: var(--accent-color);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  white-space: nowrap;
  transition: all 0.2s;
}

.copy-btn:hover,
.toggle-visibility-btn:hover,
.regenerate-btn:hover,
.change-password-btn:hover {
  filter: brightness(1.1);
}

.regenerate-btn {
  background: var(--success-color);
}

.change-password-btn {
  margin-top: 12px;
}

/* Password Section */
.password-section {
  margin-bottom: 24px;
}

.password-section h5 {
  margin: 0 0 12px 0;
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 600;
}

/* Success/Error Messages */
.success-message,
.error-message {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 500;
  z-index: 1001;
  min-width: 250px;
}

.success-message {
  background: var(--success-color);
  color: white;
  border: 1px solid var(--success-color);
}

.error-message {
  background: var(--error-color);
  color: white;
  border: 1px solid var(--error-color);
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
    border-bottom: 1px solid var(--border-color);
    padding: 12px 0;
  }
  
  .nav-button {
    padding: 8px 16px;
    display: inline-block;
    width: auto;
    margin: 0 4px;
    border-radius: 6px;
  }
  
  .api-key-display,
  .gemini-api-display {
    flex-direction: column;
  }
}
</style>
