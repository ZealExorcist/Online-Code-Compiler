// User settings service
import axios from 'axios'
import authService from './auth'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

// Types for settings
export interface UserSettings {
  theme: string
  fontSize: string
  enableAutoComplete: boolean
  enableLineNumbers: boolean
  enableFolding: boolean
  defaultLanguage: string
  enableKeyboardShortcuts: boolean
  tabSize: number
  colorScheme: string
  maxExecutionTime: number
  maxOutputSize: number
  enableInput: boolean
  publicSnippets: boolean
  shareByDefault: boolean
}

export interface UserProfile {
  id: string
  username: string
  email: string
  tier: string
  createdAt: string
  lastLoginAt: string
}

export interface UserStatistics {
  totalExecutions: number
  totalSnippets: number
  favoriteLanguage: string
  executionsThisMonth: number
  avgExecutionTime: number
}

export interface Tier {
  name: string
  displayName: string
  features: string[]
  maxExecutions: number
  maxExecutionTime: number
  priority: number
  price?: number
}

export class SettingsService {
  // Get user settings
  async getUserSettings(): Promise<UserSettings> {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/user/settings`, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to get settings')
    }
  }

  // Update user settings
  async updateUserSettings(settings: Partial<UserSettings>): Promise<UserSettings> {
    try {
      const response = await axios.put(`${API_BASE_URL}/api/user/settings`, settings, {
        headers: authService.getAuthHeaders()
      })
      
      // Cache settings locally
      localStorage.setItem('user_settings', JSON.stringify(response.data))
      
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to update settings')
    }
  }

  // Get user profile
  async getUserProfile(): Promise<UserProfile> {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/user/profile`, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to get profile')
    }
  }

  // Change password
  async changePassword(oldPassword: string, newPassword: string): Promise<{ success: boolean; message: string }> {
    try {
      const response = await axios.put(`${API_BASE_URL}/api/user/password`, {
        oldPassword,
        newPassword
      }, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to change password')
    }
  }

  // Generate new API key
  async generateNewApiKey(): Promise<{ apiKey: string }> {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/user/generate-api-key`, {}, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to generate API key')
    }
  }

  // Get user statistics
  async getUserStatistics(): Promise<UserStatistics> {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/user/statistics`, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to get statistics')
    }
  }

  // Get available tiers
  async getAvailableTiers(): Promise<Tier[]> {
    try {
      const response = await axios.get(`${API_BASE_URL}/api/user/tiers`, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to get tiers')
    }
  }

  // Upgrade tier
  async upgradeTier(tier: string): Promise<{ success: boolean; message: string; newTier: string }> {
    try {
      const response = await axios.post(`${API_BASE_URL}/api/user/upgrade`, { tier }, {
        headers: authService.getAuthHeaders()
      })
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to upgrade tier')
    }
  }

  // Get cached settings from localStorage
  getCachedSettings(): UserSettings {
    const cached = localStorage.getItem('user_settings')
    if (cached) {
      try {
        return JSON.parse(cached)
      } catch (e) {
        console.error('Failed to parse cached settings:', e)
      }
    }
    return this.getDefaultSettings()  }

  // Get default settings
  getDefaultSettings(): UserSettings {    return {
      theme: 'dark',
      fontSize: '14px',
      enableAutoComplete: true,
      enableLineNumbers: true,
      enableFolding: true,
      defaultLanguage: 'python',
      enableKeyboardShortcuts: true,
      tabSize: 4,
      colorScheme: 'oneDark',
      maxExecutionTime: 30,
      maxOutputSize: 10240,
      enableInput: true,
      publicSnippets: false,
      shareByDefault: false
    }
  }

  // Apply settings to editor
  applySettingsToEditor(editorInstance: any, settings: UserSettings): void {
    if (!editorInstance || !settings) return

    try {
      // Apply theme
      if (settings.theme && editorInstance.updateTheme) {
        editorInstance.updateTheme(settings.theme)
      }      // Apply font settings
      if (settings.fontSize) {
        const editorElement = editorInstance.view?.dom
        if (editorElement) {
          if (settings.fontSize) {
            editorElement.style.fontSize = settings.fontSize
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

const settingsService = new SettingsService()
export default settingsService

// Export individual functions for easier import
export const getUserSettings = () => settingsService.getUserSettings()
export const updateUserSettings = (settings: Partial<UserSettings>) => settingsService.updateUserSettings(settings)
export const getUserProfile = () => settingsService.getUserProfile()
export const changePassword = (oldPassword: string, newPassword: string) => settingsService.changePassword(oldPassword, newPassword)
export const generateNewApiKey = () => settingsService.generateNewApiKey()
export const getAvailableTiers = () => settingsService.getAvailableTiers()
export const upgradeTier = (tier: string) => settingsService.upgradeTier(tier)
