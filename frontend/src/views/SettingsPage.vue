<template>
  <div class="settings-container">
    <div class="settings-header">
      <h1>Settings</h1>
      <button @click="goBack" class="back-button">← Back</button>
    </div>

    <div class="settings-content">
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
          <h2>Editor Settings</h2>
          
          <div class="setting-group">
            <label>Theme</label>
            <select v-model="settings.theme" @change="updateSettings">
              <option value="dark">Dark</option>
              <option value="light">Light</option>
            </select>
          </div>

          <div class="setting-group">
            <label>Color Scheme</label>
            <select v-model="settings.colorScheme" @change="updateSettings">
              <option value="oneDark">One Dark</option>
              <option value="oneLight">One Light</option>
              <option value="monokai">Monokai</option>
              <option value="solarizedDark">Solarized Dark</option>
              <option value="solarizedLight">Solarized Light</option>
            </select>
          </div>

          <div class="setting-group">
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
            <label>Font Family</label>
            <select v-model="settings.fontFamily" @change="updateSettings">
              <option value="JetBrains Mono, Monaco, 'Courier New', monospace">JetBrains Mono</option>
              <option value="Monaco, 'Courier New', monospace">Monaco</option>
              <option value="'Courier New', monospace">Courier New</option>
              <option value="'Ubuntu Mono', Monaco, monospace">Ubuntu Mono</option>
              <option value="'Fira Code', Monaco, monospace">Fira Code</option>
            </select>
          </div>

          <div class="setting-group">
            <label>Tab Size</label>
            <select v-model.number="settings.tabSize" @change="updateSettings">
              <option :value="2">2</option>
              <option :value="4">4</option>
              <option :value="8">8</option>
            </select>
          </div>

          <div class="setting-group">
            <label>Default Language</label>
            <select v-model="settings.defaultLanguage" @change="updateSettings">
              <option value="python">Python</option>
              <option value="javascript">JavaScript</option>
              <option value="java">Java</option>
              <option value="cpp">C++</option>
              <option value="c">C</option>
              <option value="go">Go</option>
              <option value="rust">Rust</option>
              <option value="ruby">Ruby</option>
              <option value="r">R</option>
              <option value="csharp">C#</option>
            </select>
          </div>

          <div class="setting-group">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="settings.enableLineNumbers" 
                @change="updateSettings"
              />
              Show Line Numbers
            </label>
          </div>

          <div class="setting-group">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="settings.enableFolding" 
                @change="updateSettings"
              />
              Enable Code Folding
            </label>
          </div>

          <div class="setting-group">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="settings.enableAutoComplete" 
                @change="updateSettings"
              />
              Enable Auto-Complete
            </label>
          </div>

          <div class="setting-group">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="settings.wordWrap" 
                @change="updateSettings"
              />
              Word Wrap
            </label>
          </div>

          <div class="setting-group">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="settings.minimap" 
                @change="updateSettings"
              />
              Show Minimap
            </label>
          </div>
        </div>

        <!-- Execution Settings -->
        <div v-if="activeTab === 'execution'" class="settings-section">
          <h2>Execution Settings</h2>
          
          <div class="setting-group">
            <label>Max Execution Time (seconds)</label>
            <input 
              type="number" 
              v-model.number="settings.maxExecutionTime" 
              @change="updateSettings"
              min="5"
              max="60"
            />
          </div>

          <div class="setting-group">
            <label>Max Output Size (bytes)</label>
            <input 
              type="number" 
              v-model.number="settings.maxOutputSize" 
              @change="updateSettings"
              min="1024"
              max="102400"
            />
          </div>

          <div class="setting-group">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="settings.enableInput" 
                @change="updateSettings"
              />
              Enable Input Support
            </label>
          </div>
        </div>

        <!-- Privacy Settings -->
        <div v-if="activeTab === 'privacy'" class="settings-section">
          <h2>Privacy Settings</h2>
          
          <div class="setting-group">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="settings.publicSnippets" 
                @change="updateSettings"
              />
              Make my snippets public by default
            </label>
            <p class="setting-description">
              Public snippets can be discovered and viewed by other users
            </p>
          </div>

          <div class="setting-group">
            <label class="checkbox-label">
              <input 
                type="checkbox" 
                v-model="settings.shareByDefault" 
                @change="updateSettings"
              />
              Generate share links by default
            </label>
            <p class="setting-description">
              Automatically create shareable links when saving snippets
            </p>
          </div>
        </div>

        <!-- Account Settings -->
        <div v-if="activeTab === 'account'" class="settings-section">
          <h2>Account Settings</h2>
          
          <div class="setting-group">
            <label>Username</label>
            <input type="text" :value="userInfo.username" readonly />
          </div>

          <div class="setting-group">
            <label>Email</label>
            <input type="email" :value="userInfo.email" readonly />
          </div>

          <div class="setting-group">
            <label>API Key</label>
            <div class="api-key-section">
              <input 
                type="text" 
                :value="userInfo.apiKey" 
                readonly 
                class="api-key-input"
              />
              <button @click="refreshApiKey" class="refresh-button">
                Refresh
              </button>
            </div>
            <p class="setting-description">
              Use this API key to authenticate API requests
            </p>
          </div>

          <div class="setting-group">
            <label>Gemini API Key</label>
            <div class="gemini-api-key-section">
              <input 
                :type="showGeminiApiKey ? 'text' : 'password'"
                v-model="settings.geminiApiKey"
                @change="updateSettings"
                placeholder="Enter your Gemini API key for AI Insights"
                class="gemini-api-key-input"
              />
              <button @click="toggleGeminiApiKeyVisibility" class="toggle-visibility-button">
                {{ showGeminiApiKey ? 'Hide' : 'Show' }}
              </button>
            </div>
            <p class="setting-description">
              Required for AI Insights feature. Get your API key from Google AI Studio.
            </p>
          </div>

          <div class="setting-group">
            <label>Change Password</label>
            <div class="password-section">
              <input 
                type="password" 
                v-model="passwordData.oldPassword"
                placeholder="Current password"
              />
              <input 
                type="password" 
                v-model="passwordData.newPassword"
                placeholder="New password"
              />
              <button @click="changePassword" class="change-password-button">
                Change Password
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="message" :class="['message', messageType]">
      {{ message }}
    </div>
  </div>
</template>

<script>
import settingsService from '../services/settings'
import authService from '../services/auth'

export default {
  name: 'SettingsPage',
  data() {
    return {
      activeTab: 'editor',
      settings: {},
      userInfo: {},
      passwordData: {
        oldPassword: '',
        newPassword: ''
      },
      showGeminiApiKey: false,
      message: '',
      messageType: 'success',
      tabs: [
        { id: 'editor', name: 'Editor' },
        { id: 'execution', name: 'Execution' },
        { id: 'privacy', name: 'Privacy' },
        { id: 'account', name: 'Account' }
      ]
    }
  },
  async mounted() {
    if (!authService.isAuthenticated()) {
      this.$router.push('/login')
      return
    }

    await this.loadSettings()
    await this.loadUserInfo()
  },
  methods: {    async loadSettings() {
      try {
        if (authService.isAuthenticated()) {
          this.settings = await settingsService.getUserSettings()
        } else {
          console.warn('Cannot load settings for unauthenticated user')
          this.settings = settingsService.getDefaultSettings()
        }
      } catch (error) {
        console.error('Failed to load settings:', error)
        this.settings = settingsService.getDefaultSettings()
      }
    },    async loadUserInfo() {
      try {
        this.userInfo = await authService.getCurrentUser()
        
        if (!this.userInfo.apiKey) {
          console.warn('No API key found in user info')
        }
      } catch (error) {
        console.error('Failed to load user info:', error)
        this.showMessage('Failed to load user information', 'error')
      }
    },

    async updateSettings() {
      try {
        await settingsService.updateUserSettings(this.settings)
        this.showMessage('Settings updated successfully', 'success')
        
        // Emit event to update editor if on main page
        this.$emit('settings-updated', this.settings)
      } catch (error) {
        console.error('Failed to update settings:', error)
        this.showMessage('Failed to update settings', 'error')
      }
    },    async refreshApiKey() {      try {
        const newApiKey = await authService.refreshApiKey()
        
        this.userInfo.apiKey = newApiKey
        this.showMessage('API key refreshed successfully', 'success')
      } catch (error) {
        console.error('Failed to refresh API key:', error)
        this.showMessage(`Failed to refresh API key: ${error.message}`, 'error')
      }
    },

    async changePassword() {
      if (!this.passwordData.oldPassword || !this.passwordData.newPassword) {
        this.showMessage('Please fill in both password fields', 'error')
        return
      }

      if (this.passwordData.newPassword.length < 6) {
        this.showMessage('New password must be at least 6 characters', 'error')
        return
      }

      try {
        await settingsService.changePassword(
          this.passwordData.oldPassword,
          this.passwordData.newPassword
        )
        this.passwordData.oldPassword = ''
        this.passwordData.newPassword = ''
        this.showMessage('Password changed successfully', 'success')
      } catch (error) {
        console.error('Failed to change password:', error)
        this.showMessage(error.message, 'error')
      }
    },

    toggleGeminiApiKeyVisibility() {
      this.showGeminiApiKey = !this.showGeminiApiKey
    },

    showMessage(text, type) {
      this.message = text
      this.messageType = type
      setTimeout(() => {
        this.message = ''
      }, 3000)
    },

    goBack() {
      this.$router.go(-1)
    }
  }
}
</script>

<style scoped>
.settings-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: var(--bg-primary);
  min-height: 100vh;
  color: var(--text-primary);
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: var(--bg-secondary);
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
}

.settings-header h1 {
  margin: 0;
  color: var(--text-primary);
}

.back-button {
  padding: 8px 16px;
  background: var(--accent-color);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.back-button:hover {
  filter: brightness(1.1);
}

.settings-content {
  display: flex;
  gap: 20px;
  background: var(--bg-secondary);
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.settings-nav {
  min-width: 200px;
  background: var(--bg-tertiary);
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--border-color);
}

.nav-button {
  padding: 15px 20px;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  font-size: 16px;
  color: var(--text-secondary);
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
}

.nav-button:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.nav-button.active {
  background: var(--accent-color);
  color: white;
  border-left-color: #4c51bf;
}

.settings-panel {
  flex: 1;
  padding: 20px;
}

.settings-section h2 {
  margin: 0 0 20px 0;
  color: var(--text-primary);
  font-size: 20px;
}

.setting-group {
  margin-bottom: 20px;
}

.setting-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: var(--text-primary);
}

.checkbox-label {
  display: flex !important;
  align-items: center;
  gap: 8px;
}

.checkbox-label input[type="checkbox"] {
  margin: 0;
  accent-color: var(--accent-color);
}

.setting-group input,
.setting-group select {
  width: 100%;
  padding: 10px 12px;
  border: 2px solid var(--border-color);
  border-radius: 6px;
  font-size: 14px;
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: border-color 0.2s ease;
}

.setting-group input:focus,
.setting-group select:focus {
  outline: none;
  border-color: var(--accent-color);
}

.setting-group input::placeholder {
  color: var(--text-muted);
}

.setting-description {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.api-key-section {
  display: flex;
  gap: 10px;
  align-items: center;
}

.api-key-input {
  flex: 1;
  font-family: monospace;
  font-size: 12px;
}

.gemini-api-key-section {
  display: flex;
  gap: 10px;
  align-items: center;
}

.gemini-api-key-input {
  flex: 1;
  font-family: monospace;
  font-size: 12px;
}

.refresh-button,
.toggle-visibility-button {
  padding: 10px 16px;
  background: var(--success-color);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s ease;
  white-space: nowrap;
}

.refresh-button:hover,
.toggle-visibility-button:hover {
  filter: brightness(1.1);
}

.password-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.change-password-button {
  padding: 10px 16px;
  background: var(--accent-color);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.change-password-button:hover {
  filter: brightness(1.1);
}

.message {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 500;
  z-index: 1000;
}

.message.success {
  background: var(--success-color);
  color: white;
  border: 1px solid var(--success-color);
}

.message.error {
  background: var(--error-color);
  color: white;
  border: 1px solid var(--error-color);
}

/* Theme specific adjustments for better contrast */
[data-theme="light"] .message.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

[data-theme="light"] .message.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

[data-theme="dark"] .message.success {
  background: rgba(72, 187, 120, 0.2);
  color: #68d391;
  border: 1px solid #68d391;
}

[data-theme="dark"] .message.error {
  background: rgba(245, 101, 101, 0.2);
  color: #f56565;
  border: 1px solid #f56565;
}

@media (max-width: 768px) {
  .settings-content {
    flex-direction: column;
  }
  
  .settings-nav {
    min-width: auto;
    flex-direction: row;
    overflow-x: auto;
    border-right: none;
    border-bottom: 1px solid var(--border-color);
  }
  
  .nav-button {
    white-space: nowrap;
    border-left: none;
    border-bottom: 3px solid transparent;
  }
  
  .nav-button.active {
    border-left: none;
    border-bottom-color: #4c51bf;
  }
}
</style>
