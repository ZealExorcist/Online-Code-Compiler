<template>
  <div class="share-component">
    <!-- Share Button -->
    <button 
      @click="showShareModal = true" 
      class="share-btn"
      :disabled="!code.trim()"
    >
      <i class="share-icon">🔗</i>
      Share Code
    </button>

    <!-- Share Modal -->
    <div v-if="showShareModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Share Your Code</h3>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>

        <div class="modal-body">
          <!-- Title Input -->
          <div class="form-group">
            <label for="title">Title (optional):</label>
            <input 
              id="title"
              v-model="shareTitle" 
              type="text" 
              placeholder="Enter a title for your code..."
              class="title-input"
            />
          </div>

          <!-- Code Preview -->
          <div class="code-preview">
            <div class="preview-header">
              <span class="language-tag">{{ language }}</span>
              <span class="code-length">{{ code.length }} characters</span>
            </div>
            <pre class="code-sample">{{ codeSample }}</pre>
          </div>

          <!-- Share Options -->
          <div v-if="!isLoading && !shareResult" class="share-options">
            <button @click="generateShareLink" class="generate-btn">
              Generate Share Link
            </button>
          </div>

          <!-- Loading State -->
          <div v-if="isLoading" class="loading-state">
            <div class="spinner"></div>
            <p>Generating share link...</p>
          </div>

          <!-- Share Result -->
          <div v-if="shareResult" class="share-result">
            <div class="success-message">
              <i class="success-icon">✓</i>
              Share link generated successfully!
            </div>

            <div class="share-links">
              <div class="link-group">
                <label>Full Share URL:</label>
                <div class="link-container">
                  <input 
                    :value="shareResult.shareUrl" 
                    readonly 
                    class="share-url"
                    ref="fullUrlInput"
                  />
                  <button @click="copyToClipboard(shareResult.shareUrl, 'fullUrlInput')" class="copy-btn">
                    Copy
                  </button>
                </div>
              </div>

              <div class="link-group">
                <label>Short URL:</label>
                <div class="link-container">
                  <input 
                    :value="shareResult.shortUrl" 
                    readonly 
                    class="share-url"
                    ref="shortUrlInput"
                  />
                  <button @click="copyToClipboard(shareResult.shortUrl, 'shortUrlInput')" class="copy-btn">
                    Copy
                  </button>
                </div>
              </div>
            </div>

            <div class="share-actions">
              <button @click="openInNewTab(shareResult.shareUrl)" class="open-btn">
                Open in New Tab
              </button>
              <button @click="resetShare" class="new-share-btn">
                Generate New Link
              </button>
            </div>
          </div>

          <!-- Error State -->
          <div v-if="errorMessage" class="error-state">
            <i class="error-icon">⚠️</i>
            <p>{{ errorMessage }}</p>
            <button @click="resetShare" class="retry-btn">Try Again</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Copy Success Toast -->
    <div v-if="showCopyToast" class="copy-toast">
      Link copied to clipboard! 📋
    </div>
  </div>
</template>

<script>
import { shareSnippet } from '../services/api'

export default {
  name: 'ShareComponent',
  props: {
    code: {
      type: String,
      required: true
    },
    language: {
      type: String,
      required: true
    },
    input: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      showShareModal: false,
      shareTitle: '',
      isLoading: false,
      shareResult: null,
      errorMessage: '',
      showCopyToast: false
    }
  },
  computed: {
    codeSample() {
      // Show first 3 lines or 150 characters, whichever is shorter
      const lines = this.code.split('\n').slice(0, 3)
      const sample = lines.join('\n')
      return sample.length > 150 ? sample.substring(0, 150) + '...' : sample
    }
  },
  methods: {
    async generateShareLink() {
      this.isLoading = true
      this.errorMessage = ''
      
      try {
        const result = await shareSnippet(
          this.code, 
          this.language, 
          this.input, 
          this.shareTitle
        )
        
        this.shareResult = result
      } catch (error) {
        this.errorMessage = error.message
      } finally {
        this.isLoading = false
      }
    },

    async copyToClipboard(text, inputRef) {
      try {
        await navigator.clipboard.writeText(text)
        this.showCopySuccess()
        
        // Select the input text for visual feedback
        if (this.$refs[inputRef]) {
          this.$refs[inputRef].select()
        }
      } catch (err) {
        // Fallback for older browsers
        if (this.$refs[inputRef]) {
          this.$refs[inputRef].select()
          document.execCommand('copy')
          this.showCopySuccess()
        }
      }
    },

    showCopySuccess() {
      this.showCopyToast = true
      setTimeout(() => {
        this.showCopyToast = false
      }, 2000)
    },

    openInNewTab(url) {
      window.open(url, '_blank')
    },

    resetShare() {
      this.shareResult = null
      this.errorMessage = ''
      this.isLoading = false
    },

    closeModal() {
      this.showShareModal = false
      this.resetShare()
      this.shareTitle = ''
    }
  }
}
</script>

<style scoped>
.share-component {
  position: relative;
}

.share-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.share-btn:hover:not(:disabled) {
  background: #0056b3;
  transform: translateY(-1px);
}

.share-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.share-icon {
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

.title-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.title-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.code-preview {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  margin-bottom: 20px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #e9ecef;
  border-bottom: 1px solid #dee2e6;
  font-size: 12px;
}

.language-tag {
  background: #007bff;
  color: white;
  padding: 2px 8px;
  border-radius: 3px;
  font-weight: 500;
}

.code-length {
  color: #666;
}

.code-sample {
  padding: 12px;
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  color: #333;
  max-height: 120px;
  overflow-y: auto;
}

.share-options {
  text-align: center;
}

.generate-btn {
  background: #28a745;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.generate-btn:hover {
  background: #218838;
}

.loading-state {
  text-align: center;
  padding: 20px;
}

.spinner {
  border: 3px solid #f3f3f3;
  border-top: 3px solid #007bff;
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

.share-result {
  text-align: center;
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

.share-links {
  margin-bottom: 20px;
}

.link-group {
  margin-bottom: 15px;
  text-align: left;
}

.link-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.link-container {
  display: flex;
  gap: 8px;
}

.share-url {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
  background: #f8f9fa;
}

.copy-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.2s;
}

.copy-btn:hover {
  background: #5a6268;
}

.share-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.open-btn, .new-share-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.open-btn {
  background: #007bff;
  color: white;
}

.open-btn:hover {
  background: #0056b3;
}

.new-share-btn {
  background: #6c757d;
  color: white;
}

.new-share-btn:hover {
  background: #5a6268;
}

.error-state {
  text-align: center;
  color: #dc3545;
}

.error-icon {
  font-size: 24px;
  margin-bottom: 10px;
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

.copy-toast {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: #28a745;
  color: white;
  padding: 12px 20px;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  animation: slideInUp 0.3s ease-out;
}

@keyframes slideInUp {
  from {
    transform: translateY(100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
