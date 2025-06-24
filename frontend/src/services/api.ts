import axios from 'axios'
import authService from './auth'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL: API_BASE_URL + '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add request interceptor to include auth headers
api.interceptors.request.use(
  (config) => {
    const authHeaders = authService.getAuthHeaders()
    Object.assign(config.headers, authHeaders)
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Add response interceptor to handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Only logout if we have a token/apiKey and it's actually invalid
      // Don't logout for anonymous users trying to access protected endpoints
      const authHeaders = authService.getAuthHeaders()
      if (authHeaders.Authorization || authHeaders['X-API-Key']) {
        // We have auth credentials but got 401, so they're invalid
        authService.logout()
        window.location.href = '/login'
      }
      // For anonymous users (no auth headers), just reject without logout
    }
    return Promise.reject(error)
  }
)

export async function executeCode(code: string, language: string, input: string = '') {
  try {
    const response = await api.post('/execute', {
      code,
      language,
      input
    })
    return response.data
  } catch (error: any) {
    if (error.response?.status === 429) {
      throw new Error('Rate limit exceeded. Please wait before trying again.')
    }
    throw new Error(error.response?.data?.error || error.response?.data?.message || 'Execution failed')
  }
}

export async function shareSnippet(code: string, language: string, input: string = '', title: string = '') {
  try {
    const response = await api.post('/share', {
      code,
      language,
      input,
      title
    })
    return response.data
  } catch (error: any) {
    throw new Error(error.response?.data?.error || error.response?.data?.message || 'Failed to create share link')
  }
}

export async function loadSharedCode(shareId: string) {
  try {
    const response = await api.get(`/load/${shareId}`)
    return response.data
  } catch (error: any) {
    throw new Error(error.response?.data?.error || error.response?.data?.message || 'Failed to load shared code')
  }
}

export async function getSupportedLanguages() {
  try {
    const response = await api.get('/languages')
    return response.data
  } catch (error) {
    throw new Error('Failed to load supported languages')
  }
}

export async function saveSnippet(title: string, code: string, language: string, input: string = '', isPublic: boolean = false) {
  try {
    const response = await api.post('/snippets', {
      title,
      code,
      language,
      input,
      isPublic
    })
    return response.data
  } catch (error: any) {
    throw new Error(error.response?.data?.error || error.response?.data?.message || 'Failed to save snippet')
  }
}

export async function getUserProfile() {
  try {
    const response = await api.get('/user/profile')
    return response.data
  } catch (error: any) {
    throw new Error(error.response?.data?.error || error.response?.data?.message || 'Failed to get user profile')
  }
}

export async function installPackages(language: string, packages: string[], commands: string[]) {
  try {
    const response = await api.post('/install-packages', {
      language,
      packages,
      commands
    })
    return response.data
  } catch (error: any) {
    if (error.response?.status === 429) {
      throw new Error('Rate limit exceeded. Please wait before trying again.')
    }
    throw new Error(error.response?.data?.error || error.response?.data?.message || 'Package installation failed')
  }
}

export async function executeCustomCommands(language: string, commands: string[]) {
  try {
    const response = await api.post('/execute-commands', {
      language,
      commands
    })
    return response.data
  } catch (error: any) {
    if (error.response?.status === 429) {
      throw new Error('Rate limit exceeded. Please wait before trying again.')
    }
    if (error.response?.status === 403) {
      throw new Error('Custom commands require Advanced or Master tier subscription.')
    }
    throw new Error(error.response?.data?.error || error.response?.data?.message || 'Command execution failed')
  }
}

export async function getAIInsights(code: string, language: string, userApiKey?: string) {
  console.log('🔍 getAIInsights called with:', {
    codeLength: code.length,
    language,
    hasUserApiKey: !!userApiKey,
    userApiKeyLength: userApiKey?.length || 0,
    userApiKeyPreview: userApiKey ? `${userApiKey.substring(0, 10)}...` : 'NONE'
  })
  
  try {
    const requestData = {
      code,
      language,
      userApiKey
    }
    
    console.log('📡 Making POST request to /ai-insights with:', {
      ...requestData,
      userApiKey: requestData.userApiKey ? `${requestData.userApiKey.substring(0, 10)}...` : 'NONE'
    })
    
    const response = await api.post('/ai-insights', requestData)
    
    console.log('✅ AI Insights API response:', {
      status: response.status,
      data: response.data
    })
    
    return response.data
  } catch (error: any) {
    console.error('❌ AI Insights API error:', {
      error,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      headers: error.response?.headers,
      config: error.config
    })
    
    if (error.response?.status === 429) {
      throw new Error('AI Insights rate limit exceeded. Please wait before trying again.')
    }
    if (error.response?.status === 400) {
      throw new Error('AI analysis temporarily unavailable. Please check your API key and try again later.')
    }
    if (error.response?.status === 401) {
      throw new Error('Invalid API key. Please check your Gemini API key in Settings.')
    }
    throw new Error(error.response?.data?.message || error.response?.data || 'AI Insights service temporarily unavailable')
  }
}

export async function getShareLimits() {
  try {
    const response = await api.get('/share/limits')
    return response.data
  } catch (error: any) {
    throw new Error(error.response?.data?.error || error.response?.data?.message || 'Failed to get share limits')
  }
}

export default api