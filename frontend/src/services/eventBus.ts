// Simple event bus for cross-component communication
import { reactive } from 'vue'

interface EventBusState {
  userTier: string | null
  isAuthenticated: boolean
}

class EventBus {
  private state: EventBusState = reactive({
    userTier: null,
    isAuthenticated: false
  })

  private listeners: Record<string, Function[]> = {}

  // Get current state
  getState(): EventBusState {
    return this.state
  }

  // Update user tier
  updateUserTier(tier: string) {
    this.state.userTier = tier
    this.emit('tier-updated', tier)
  }

  // Update authentication status
  updateAuthStatus(isAuthenticated: boolean) {
    this.state.isAuthenticated = isAuthenticated
    this.emit('auth-updated', isAuthenticated)
  }

  // Emit an event
  emit(event: string, data?: any) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => callback(data))
    }
  }

  // Listen to an event
  on(event: string, callback: Function) {
    if (!this.listeners[event]) {
      this.listeners[event] = []
    }
    this.listeners[event].push(callback)
  }

  // Remove event listener
  off(event: string, callback: Function) {
    if (this.listeners[event]) {
      this.listeners[event] = this.listeners[event].filter(cb => cb !== callback)
    }
  }

  // Initialize state from localStorage/API
  async initialize() {
    try {
      // Check if user is authenticated
      const token = localStorage.getItem('auth_token')
      const apiKey = localStorage.getItem('api_key')
      this.state.isAuthenticated = !!(token || apiKey)

      // If authenticated, get user tier from API
      if (this.state.isAuthenticated) {
        const { getUserProfile } = await import('./api')
        const profile = await getUserProfile()
        this.state.userTier = profile.tier
      }
    } catch (error) {
      console.warn('Failed to initialize event bus state:', error)
    }
  }
}

export const eventBus = new EventBus()
export default eventBus
