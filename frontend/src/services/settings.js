// User settings service
import axios from 'axios'
import authService from './auth'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export class SettingsService {
  // Get user settings
  async getUserSettings() {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/user/settings`, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to get settings')
    }
  }

  // Update user settings
  async updateUserSettings(settings) {
    try {
      const response = await axios.put(`${API_BASE_URL}/api/user/settings`, settings, {
        headers: authService.getAuthHeaders()
      })
      
      // Cache settings locally
      localStorage.setItem('user_settings', JSON.stringify(response.data))
      
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to update settings')
    }
  }

  // Get user profile
  async getUserProfile() {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/user/profile`, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to get profile')    }
  }

  // Change password
  async changePassword(oldPassword, newPassword) {
    try {
      const response = await axios.put(`${API_BASE_URL}/api/user/password`, {
        oldPassword,
        newPassword
      }, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to change password')
    }
  }

  // Generate new API key
  async generateNewApiKey() {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/user/generate-api-key`, {}, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to generate API key')
    }
  }

  // Get user statistics
  async getUserStatistics() {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/user/statistics`, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to get statistics')
    }
  }

  // Get available tiers
  async getAvailableTiers() {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/user/tiers`, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to get tiers')
    }
  }

  // Upgrade tier
  async upgradeTier(tier) {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/user/upgrade`, { tier }, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Failed to upgrade tier')
    }
  }

  // Get cached settings from localStorage
  getCachedSettings() {
    const cached = localStorage.getItem('user_settings')
    if (cached) {
      try {
        return JSON.parse(cached)
      } catch (e) {
        console.error('Failed to parse cached settings:', e)
      }
    }
    return this.getDefaultSettings()
  }

  // Get default settings
  getDefaultSettings() {
    return {
      theme: 'dark',
      fontSize: '14px',
      fontFamily: 'JetBrains Mono, Monaco, "Courier New", monospace',
      enableAutoComplete: true,
      enableLineNumbers: true,
      enableFolding: true,
      defaultLanguage: 'python',
      enableKeyboardShortcuts: true,
      tabSize: 4,
      insertSpaces: true,
      wordWrap: false,
      minimap: false,
      colorScheme: 'oneDark',
      maxExecutionTime: 30,
      maxOutputSize: 10240,
      enableInput: true,
      publicSnippets: false,
      shareByDefault: false
    }
  }

  // Apply settings to editor
  applySettingsToEditor(editorInstance, settings) {
    if (!editorInstance || !settings) return

    try {
      // Apply theme
      if (settings.theme && editorInstance.updateTheme) {
        editorInstance.updateTheme(settings.theme)
      }

      // Apply font settings
      if (settings.fontSize || settings.fontFamily) {
        const editorElement = editorInstance.view?.dom
        if (editorElement) {
          if (settings.fontSize) {
            editorElement.style.fontSize = settings.fontSize
          }
          if (settings.fontFamily) {
            editorElement.style.fontFamily = settings.fontFamily
          }
        }
      }

      // Apply tab settings
      if (settings.tabSize && editorInstance.updateTabSize) {
        editorInstance.updateTabSize(settings.tabSize)
      }
    } catch (error) {
      console.error('Failed to apply settings to editor:', error)
    }
  }
}

export default new SettingsService()

// Export individual functions for easier import
export const getUserSettings = () => new SettingsService().getUserSettings()
export const updateUserSettings = (settings) => new SettingsService().updateUserSettings(settings)
export const getUserProfile = () => new SettingsService().getUserProfile()
export const changePassword = (oldPassword, newPassword) => new SettingsService().changePassword(oldPassword, newPassword)
export const generateNewApiKey = () => new SettingsService().generateNewApiKey()
export const getAvailableTiers = () => new SettingsService().getAvailableTiers()
export const upgradeTier = (tier) => new SettingsService().upgradeTier(tier)
