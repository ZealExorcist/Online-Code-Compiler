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
      // Token expired or invalid, logout user
      authService.logout()
      window.location.href = '/login'
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
