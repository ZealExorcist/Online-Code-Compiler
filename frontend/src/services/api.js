import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export async function executeCode(code, language, input = '') {
  try {
    const response = await api.post('/execute', {
      code,
      language,
      input
    })
    return response.data
  } catch (error) {
    throw new Error(error.response?.data?.error || 'Execution failed')
  }
}

export async function shareSnippet(code, language, input = '', title = '') {
  try {
    const response = await api.post('/share', {
      code,
      language,
      input,
      title
    })
    return response.data
  } catch (error) {
    throw new Error(error.response?.data?.error || 'Failed to create share link')
  }
}

export async function loadSharedCode(shareId) {
  try {
    const response = await api.get(`/load/${shareId}`)
    return response.data
  } catch (error) {
    throw new Error(error.response?.data?.error || 'Failed to load shared code')
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
