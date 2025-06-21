// Authentication service for managing user auth
import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

interface LoginResponse {
  token: string
  apiKey: string
  username: string
  email: string
}

interface UserData {
  username: string | null
  email: string | null
  token: string | null
  apiKey: string | null
}

export class AuthService {
  private token: string | null = null
  private apiKey: string | null = null

  constructor() {
    this.refreshTokensFromStorage()
  }

  // Refresh tokens from localStorage
  refreshTokensFromStorage(): void {
    this.token = localStorage.getItem('auth_token')
    this.apiKey = localStorage.getItem('api_key')
  }

  // Set auth headers for axios
  getAuthHeaders(): Record<string, string> {
    const headers: Record<string, string> = {}
    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`
    } else if (this.apiKey) {
      headers['X-API-Key'] = this.apiKey
    }
    return headers
  }

  // Login user
  async login(username: string, password: string): Promise<LoginResponse> {
    try {
      const response = await axios.post<LoginResponse>(`${API_BASE_URL}/api/auth/login`, {
        username,
        password
      })
      
      const { token, apiKey, username: userName, email } = response.data
      
      this.token = token
      this.apiKey = apiKey
      
      localStorage.setItem('auth_token', token)
      localStorage.setItem('api_key', apiKey)
      localStorage.setItem('username', userName)
      localStorage.setItem('email', email)
      
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Login failed')
    }
  }

  // Register user
  async register(username: string, email: string, password: string): Promise<LoginResponse> {
    try {
      const response = await axios.post<LoginResponse>(`${API_BASE_URL}/api/auth/register`, {
        username,
        email,
        password
      })
      
      const { token, apiKey, username: userName } = response.data
      
      this.token = token
      this.apiKey = apiKey
      
      localStorage.setItem('auth_token', token)
      localStorage.setItem('api_key', apiKey)
      localStorage.setItem('username', userName)
      localStorage.setItem('email', email)
      
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Registration failed')
    }
  }

  // Logout user
  logout(): void {
    this.token = null
    this.apiKey = null
    
    localStorage.removeItem('auth_token')
    localStorage.removeItem('api_key')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
  }

  // Check if user is authenticated
  isAuthenticated(): boolean {
    return !!(this.token || this.apiKey)
  }

  // Get current user info
  async getCurrentUser(): Promise<any> {
    try {
      const headers = this.getAuthHeaders()
      
      const response = await axios.get(`${API_BASE_URL}/api/auth/me`, {
        headers: headers
      })
      return response.data
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to get user info')
    }
  }

  // Refresh API key
  async refreshApiKey(): Promise<string> {
    try {
      const response = await axios.post<{ apiKey: string }>(`${API_BASE_URL}/api/auth/refresh-api-key`, {}, {
        headers: this.getAuthHeaders()
      })
      
      this.apiKey = response.data.apiKey
      localStorage.setItem('api_key', this.apiKey)
      
      return response.data.apiKey
    } catch (error: any) {
      throw new Error(error.response?.data?.message || 'Failed to refresh API key')
    }
  }

  // Validate current authentication
  async validateAuth(): Promise<boolean> {
    try {
      const response = await axios.get<{ valid: boolean }>(`${API_BASE_URL}/api/auth/validate`, {
        headers: this.getAuthHeaders()
      })
      return response.data.valid
    } catch (error) {
      return false
    }
  }

  // Get stored user data
  getStoredUserData(): UserData {
    return {
      username: localStorage.getItem('username'),
      email: localStorage.getItem('email'),
      token: this.token,
      apiKey: this.apiKey
    }
  }
}

export default new AuthService()
