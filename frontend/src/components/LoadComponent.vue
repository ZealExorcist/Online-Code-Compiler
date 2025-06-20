<template>
  <div class="load-component">
    <!-- Load Button -->
    <button @click="showLoadModal = true" class="load-btn">
      <i class="load-icon">📁</i>
      Load Shared Code
    </button>

    <!-- Load Modal -->
    <div v-if="showLoadModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Load Shared Code</h3>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>

        <div class="modal-body">
          <!-- URL Input -->
          <div class="form-group">
            <label for="shareUrl">Paste share URL or ID:</label>
            <input 
              id="shareUrl"
              v-model="shareUrl" 
              type="text" 
              placeholder="https://... or share ID"
              class="url-input"
              @paste="handlePaste"
            />
            <div class="input-help">
              You can paste either the full share URL or just the share ID
            </div>
          </div>

          <!-- Load Actions -->
          <div v-if="!isLoading && !loadResult && !errorMessage" class="load-actions">
            <button @click="loadSharedCode" :disabled="!shareUrl.trim()" class="load-code-btn">
              Load Code
            </button>
          </div>

          <!-- Loading State -->
          <div v-if="isLoading" class="loading-state">
            <div class="spinner"></div>
            <p>Loading shared code...</p>
          </div>

          <!-- Load Result -->
          <div v-if="loadResult" class="load-result">
            <div class="success-message">
              <i class="success-icon">✓</i>
              Code loaded successfully!
            </div>

            <div class="code-info">
              <div class="info-row" v-if="loadResult.title">
                <label>Title:</label>
                <span>{{ loadResult.title }}</span>
              </div>
              <div class="info-row">
                <label>Language:</label>
                <span class="language-tag">{{ loadResult.language }}</span>
              </div>
              <div class="info-row">
                <label>Code length:</label>
                <span>{{ loadResult.code.length }} characters</span>
              </div>
              <div class="info-row" v-if="loadResult.input">
                <label>Has input:</label>
                <span>Yes ({{ loadResult.input.length }} characters)</span>
              </div>
            </div>

            <div class="code-preview">
              <div class="preview-header">
                <span>Code Preview:</span>
              </div>
              <pre class="code-sample">{{ codeSample }}</pre>
            </div>

            <div class="load-actions">
              <button @click="applyLoadedCode" class="apply-btn">
                Load into Editor
              </button>
              <button @click="resetLoad" class="cancel-btn">
                Cancel
              </button>
            </div>
          </div>

          <!-- Error State -->
          <div v-if="errorMessage" class="error-state">
            <i class="error-icon">⚠️</i>
            <p>{{ errorMessage }}</p>
            <button @click="resetLoad" class="retry-btn">Try Again</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { loadSharedCode } from '../services/api'

export default {
  name: 'LoadComponent',
  emits: ['code-loaded'],
  data() {
    return {
      showLoadModal: false,
      shareUrl: '',
      isLoading: false,
      loadResult: null,
      errorMessage: ''
    }
  },
  computed: {
    codeSample() {
      if (!this.loadResult) return ''
      
      // Show first 10 lines or 300 characters, whichever is shorter
      const lines = this.loadResult.code.split('\n').slice(0, 10)
      const sample = lines.join('\n')
      return sample.length > 300 ? sample.substring(0, 300) + '...' : sample
    }
  },
  methods: {    async loadSharedCode() {
      this.isLoading = true
      this.errorMessage = ''
      
      try {
        // Extract share ID from URL if needed
        const shareId = this.extractShareId(this.shareUrl)
        
        const result = await loadSharedCode(shareId)
        
        // Backend returns the data directly or throws an error
        this.loadResult = result
      } catch (error) {
        this.errorMessage = error.message
      } finally {
        this.isLoading = false
      }
    },

    extractShareId(input) {
      const trimmed = input.trim()
      
      // If it's already just an ID (no protocol), return as is
      if (!trimmed.includes('://')) {
        return trimmed
      }
      
      // Extract from full URL
      try {
        const url = new URL(trimmed)
        const pathSegments = url.pathname.split('/').filter(segment => segment)
        
        // Handle /share/{id} or /s/{id} patterns
        if (pathSegments.length >= 2) {
          return pathSegments[pathSegments.length - 1]
        }
        
        // Fallback: try to extract from hash or query
        if (url.hash) {
          return url.hash.substring(1)
        }
        
        const shareParam = url.searchParams.get('share') || url.searchParams.get('id')
        if (shareParam) {
          return shareParam
        }
        
        throw new Error('Could not extract share ID from URL')
      } catch (error) {
        throw new Error('Invalid share URL format')
      }
    },

    applyLoadedCode() {
      if (this.loadResult) {
        this.$emit('code-loaded', {
          code: this.loadResult.code,
          language: this.loadResult.language,
          input: this.loadResult.input || '',
          title: this.loadResult.title || ''
        })
        this.closeModal()
      }
    },

    handlePaste(event) {
      // Auto-trigger load after a short delay when pasting
      setTimeout(() => {
        if (this.shareUrl.trim()) {
          this.loadSharedCode()
        }
      }, 100)
    },

    resetLoad() {
      this.loadResult = null
      this.errorMessage = ''
      this.isLoading = false
    },

    closeModal() {
      this.showLoadModal = false
      this.resetLoad()
      this.shareUrl = ''
    }
  },
    // Handle loading from URL on component mount
  mounted() {
    // Only auto-load if explicitly requested via URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const shareParam = urlParams.get('share') || urlParams.get('load');
    
    if (shareParam) {
      try {
        this.shareUrl = shareParam;
        this.showLoadModal = true;
        // Delay auto-load to allow UI to render
        this.$nextTick(() => {
          this.loadSharedCode();
        });
      } catch (error) {
        console.warn('Could not auto-load from URL parameter:', error.message);
      }
    }
  }
}
</script>

<style scoped>
.load-component {
  position: relative;
}

.load-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.load-btn:hover {
  background: #218838;
  transform: translateY(-1px);
}

.load-icon {
  font-size: 16px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.url-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: monospace;
}

.url-input:focus {
  outline: none;
  border-color: #28a745;
  box-shadow: 0 0 0 2px rgba(40, 167, 69, 0.25);
}

.input-help {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.load-actions {
  text-align: center;
  margin-top: 20px;
}

.load-code-btn {
  background: #28a745;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.load-code-btn:hover:not(:disabled) {
  background: #218838;
}

.load-code-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.loading-state {
  text-align: center;
  padding: 20px;
}

.spinner {
  border: 3px solid #f3f3f3;
  border-top: 3px solid #28a745;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
  margin: 0 auto 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.load-result {
  text-align: left;
}

.success-message {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #28a745;
  font-weight: 500;
  margin-bottom: 20px;
}

.success-icon {
  background: #28a745;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.code-info {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row label {
  font-weight: 500;
  color: #495057;
  width: 120px;
  flex-shrink: 0;
}

.info-row span {
  color: #333;
}

.language-tag {
  background: #007bff;
  color: white;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 500;
}

.code-preview {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  margin-bottom: 20px;
}

.preview-header {
  padding: 10px 12px;
  background: #e9ecef;
  border-bottom: 1px solid #dee2e6;
  font-size: 14px;
  font-weight: 500;
  color: #495057;
}

.code-sample {
  padding: 12px;
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  color: #333;
  max-height: 200px;
  overflow-y: auto;
}

.apply-btn, .cancel-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  margin: 0 5px;
  transition: background-color 0.2s;
}

.apply-btn {
  background: #28a745;
  color: white;
}

.apply-btn:hover {
  background: #218838;
}

.cancel-btn {
  background: #6c757d;
  color: white;
}

.cancel-btn:hover {
  background: #5a6268;
}

.error-state {
  text-align: center;
  color: #dc3545;
  padding: 20px;
}

.error-icon {
  font-size: 24px;
  margin-bottom: 10px;
  display: block;
}

.retry-btn {
  background: #dc3545;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 10px;
}

.retry-btn:hover {
  background: #c82333;
}
</style>
