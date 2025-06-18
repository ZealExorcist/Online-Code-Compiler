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

export async function shareSnippet(code, language) {
  try {
    const response = await api.post('/snippets', {
      code,
      language
    })
    return response.data
  } catch (error) {
    throw new Error(error.response?.data?.error || 'Failed to share snippet')
  }
}

export async function getSnippet(id) {
  try {
    const response = await api.get(`/snippets/${id}`)
    return response.data
  } catch (error) {
    throw new Error(error.response?.data?.error || 'Failed to load snippet')
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
