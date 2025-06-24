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
          </div>          <!-- Share Options -->
          <div v-if="!isLoading && !shareResult" class="share-options">
            <!-- Share Limits Info -->
            <div v-if="shareLimits && !shareLimits.unlimited" class="share-limits-info">
              <div class="limits-display">
                <span class="limits-icon">📊</span>
                <span class="limits-text">{{ shareStatusText }}</span>
              </div>
              <div v-if="!canShareMore" class="limits-warning">
                <span class="warning-icon">⚠️</span>
                <span>Daily share limit reached. Upgrade for unlimited sharing!</span>
              </div>
            </div>
            <div v-else-if="shareLimits && shareLimits.unlimited" class="share-limits-info unlimited">
              <div class="limits-display">
                <span class="limits-icon">♾️</span>
                <span class="limits-text">Unlimited shares available</span>
              </div>
            </div>            
            <button 
              @click="generateShareLink" 
              class="generate-btn"
              :disabled="!canShareMore"
              :class="{ 'btn-disabled': !canShareMore }"
            >
              <span v-if="!canShareMore">⚠️ Share Limit Reached</span>
              <span v-else>Generate Share Link</span>
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
    </div>    <!-- Copy Success Toast -->
    <div v-if="showCopyToast" class="copy-toast">
      Link copied to clipboard! 📋
    </div>
    
    <!-- Share Limit Upgrade Modal -->
    <div v-if="showUpgradeModal" class="modal-overlay" @click="closeUpgradeModal">
      <div class="modal-content upgrade-modal" @click.stop>
        <div class="modal-header">
          <h3>🔒 Share Limit Reached</h3>
          <button @click="closeUpgradeModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="upgrade-info">
            <div class="feature-icon">📤</div>
            <h4>Daily Share Limit Exceeded</h4>
            <p>You've reached your daily limit of <strong>3 shares</strong>.</p>
            
            <div v-if="shareLimits" class="current-usage">
              <span class="usage-label">Today's usage:</span>
              <span class="usage-stats">{{ shareLimits.usedShares }}/3 shares used</span>
            </div>
            
            <p class="upgrade-benefits">Upgrade to <strong>Advanced</strong> or <strong>Master</strong> tier for:</p>
            <ul class="benefits-list">
              <li>♾️ Unlimited code sharing</li>
              <li>📥 Download code files to your computer</li>
              <li>⚡ Custom package installation commands</li>
              <li>🚀 Faster execution with reduced delays</li>
              <li>📊 Higher rate limits</li>
            </ul>
          </div>        </div>        <div class="modal-footer">
          <button @click="closeUpgradeModal" class="btn-secondary">Maybe Later</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { shareSnippet, getShareLimits } from '../services/api'
import authService from '../services/auth.ts'
import { eventBus } from '../services/eventBus.ts'

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
      showCopyToast: false,
      shareLimits: null,
      showUpgradeModal: false
    }
  },  computed: {
    codeSample() {
      // Show first 3 lines or 150 characters, whichever is shorter
      const lines = this.code.split('\n').slice(0, 3)
      const sample = lines.join('\n')
      return sample.length > 150 ? sample.substring(0, 150) + '...' : sample
    },
    
    isAuthenticated() {
      return authService.isAuthenticated()
    },
    
    canShareMore() {
      return !this.shareLimits || this.shareLimits.canShare
    },
    
    shareStatusText() {
      if (!this.shareLimits) return ''
      
      if (this.shareLimits.unlimited) {
        return 'Unlimited shares available'
      } else {
        const remaining = this.shareLimits.remainingShares
        const used = this.shareLimits.usedShares
        return `${remaining} of 3 daily shares remaining (${used} used)`
      }
    }
  },  methods: {
    async loadShareLimits() {
      try {
        this.shareLimits = await getShareLimits()
      } catch (error) {
        console.warn('Failed to load share limits:', error)
        // Set default limits for anonymous users
        this.shareLimits = {
          canShare: true,
          remainingShares: 3,
          usedShares: 0,
          tier: 'ANONYMOUS',
          unlimited: false
        }
      }
    },
    
    async generateShareLink() {
      // Check limits before attempting to share
      if (!this.canShareMore) {
        this.showUpgradeInfo()
        return
      }
      
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
        
        // Refresh share limits after successful share
        await this.loadShareLimits()
        
      } catch (error) {
        // Handle share limit errors
        if (error.message.includes('Daily share limit reached') || error.message.includes('share limit')) {
          this.showUpgradeInfo()
        } else {
          this.errorMessage = error.message
        }
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
    },    closeModal() {
      this.showShareModal = false
      this.resetShare()
      this.shareTitle = ''
    },
    
    showUpgradeInfo() {
      this.showUpgradeModal = true
    },
    
    closeUpgradeModal() {
      this.showUpgradeModal = false
    },
    
    openUpgradeOptions() {
      this.closeUpgradeModal()
      // Emit event to show upgrade modal
      eventBus.emit('show-upgrade-modal')
    }
  },
  
  async mounted() {
    await this.loadShareLimits()
    
    // Listen for tier updates to refresh share limits
    eventBus.on('tier-updated', this.loadShareLimits)
  },
  
  beforeUnmount() {
    eventBus.off('tier-updated', this.loadShareLimits)
  }
}
</script>

<style scoped>
.share-component {
  position: relative;
}

.share-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  min-width: 120px;
  justify-content: center;
}

.share-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.share-btn:disabled {
  background: #4a5568;
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
  box-shadow: none;
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
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: var(--bg-primary, white);
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  color: var(--text-primary, #1f2937);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f9fafb);
}

.modal-header h3 {
  margin: 0;
  color: var(--text-primary, #1f2937);
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s;
}

.close-btn:hover {
  background: #e5e7eb;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--border-color, #e5e7eb);
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  background: var(--bg-secondary, #f9fafb);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--text-primary, #1f2937);
  font-size: 14px;
}

.title-input {
  width: 100%;
  padding: 12px;
  border: 2px solid var(--border-color, #e5e7eb);
  border-radius: 6px;
  font-size: 14px;
  background: var(--bg-primary, white);
  color: var(--text-primary, #1f2937);
  transition: all 0.2s ease;
}

.title-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 16px;
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
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

/* Share Limits Info */
.share-limits-info {
  background: var(--bg-secondary, #f8f9fa);
  border: 1px solid var(--border-color, #e9ecef);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.share-limits-info.unlimited {
  background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
  border-color: #28a745;
}

.limits-display {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.limits-icon {
  font-size: 16px;
}

.limits-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary, #333);
}

.limits-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 6px;
  color: #856404;
  font-size: 13px;
}

.warning-icon {
  font-size: 14px;
}

.generate-btn {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.generate-btn:hover:not(.btn-disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.4);
}

.generate-btn.btn-disabled {
  background: #6c757d !important;
  cursor: not-allowed !important;
  opacity: 0.7;
}

.generate-btn.btn-disabled:hover {
  background: #6c757d !important;
  transform: none !important;
}


/* Share Limit Upgrade Modal */
.upgrade-modal {
  max-width: 500px;
}

.upgrade-info {
  text-align: center;
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.upgrade-info h4 {
  margin: 0 0 16px 0;
  color: var(--text-primary, #1f2937);
  font-size: 20px;
  font-weight: 600;
}

.upgrade-info p {
  margin: 0 0 16px 0;
  color: var(--text-secondary, #6b7280);
  line-height: 1.5;
}

.current-usage {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 16px 0;
  padding: 12px;
  background: var(--bg-secondary, #f9fafb);
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 8px;
}

.usage-label {
  color: var(--text-secondary, #6b7280);
  font-size: 14px;
}

.usage-stats {
  font-weight: 600;
  color: var(--text-primary, #1f2937);
  font-size: 14px;
}

.upgrade-benefits {
  font-weight: 500;
  margin: 20px 0 8px 0 !important;
}

.benefits-list {
  text-align: left;
  margin: 0;
  padding-left: 20px;
}

.benefits-list li {
  margin: 8px 0;
  color: var(--text-secondary, #6b7280);
}

/* Button Styles */
.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: transparent;
  color: var(--text-secondary, #6b7280);
  border: 1px solid var(--border-color, #e5e7eb);
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background: var(--bg-secondary, #f9fafb);
  border-color: #d1d5db;
}

/* Dark mode support */
[data-theme="dark"] .share-limits-info {
  background: #374151;
  border-color: #4b5563;
}

[data-theme="dark"] .share-limits-info.unlimited {
  background: linear-gradient(135deg, #065f46 0%, #047857 100%);
  border-color: #10b981;
}

[data-theme="dark"] .limits-text {
  color: #f9fafb;
}

[data-theme="dark"] .limits-warning {
  background: #451a03;
  border-color: #92400e;
  color: #fbbf24;
}

[data-theme="dark"] .current-usage {
  background: #374151;
  border-color: #4b5563;
}

[data-theme="dark"] .usage-label {
  color: #9ca3af;
}

[data-theme="dark"] .usage-stats {
  color: #f9fafb;
}

[data-theme="dark"] .upgrade-info h4 {
  color: #f9fafb;
}

[data-theme="dark"] .upgrade-info p {
  color: #d1d5db;
}

[data-theme="dark"] .benefits-list li {
  color: #9ca3af;
}

[data-theme="dark"] .btn-secondary {
  color: #d1d5db;
  border-color: #4b5563;
}

[data-theme="dark"] .btn-secondary:hover {
  background: #4b5563;
  border-color: #6b7280;
}

.copy-btn {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  min-width: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.copy-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.4);
}

.share-url {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid var(--border-color, #ddd);
  border-radius: 4px;
  background: var(--bg-primary, white);
  color: var(--text-primary, #333);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  margin-right: 8px;
}

.share-url:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.link-container {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.link-group {
  margin-bottom: 16px;
}

.link-group label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
  color: var(--text-primary, #333);
  font-size: 14px;
}

.share-links {
  margin: 20px 0;
}

.share-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  justify-content: center;
}

.open-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.open-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.new-share-btn {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.new-share-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(237, 137, 54, 0.4);
}

.success-message {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #28a745;
  font-weight: 500;
  margin-bottom: 16px;
}

.success-icon {
  font-size: 18px;
}

.error-state {
  text-align: center;
  color: #dc3545;
}

.error-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.retry-btn {
  background: linear-gradient(135deg, #e53e3e 0%, #c53030 100%);
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  margin-top: 12px;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(229, 62, 62, 0.4);
}

.loading-state {
  text-align: center;
  color: var(--text-secondary, #666);
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.copy-toast {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background: #28a745;
  color: white;
  padding: 12px 16px;
  border-radius: 6px;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* Dark mode support for original styles */
[data-theme="dark"] .modal-content {
  background: #2d3748;
  color: #e2e8f0;
}

[data-theme="dark"] .modal-header {
  border-bottom-color: #4a5568;
}

[data-theme="dark"] .modal-header h3 {
  color: #e2e8f0;
}

[data-theme="dark"] .close-btn {
  color: #a0aec0;
}

[data-theme="dark"] .close-btn:hover {
  color: #e2e8f0;
}

[data-theme="dark"] .title-input {
  background: #4a5568;
  border-color: #2d3748;
  color: #e2e8f0;
}

[data-theme="dark"] .title-input:focus {
  border-color: #4299e1;
  box-shadow: 0 0 0 2px rgba(66, 153, 225, 0.25);
}

[data-theme="dark"] .code-preview {
  background: #2d3748;
  border-color: #4a5568;
}

[data-theme="dark"] .preview-header {
  background: #4a5568;
  border-bottom-color: #2d3748;
}

[data-theme="dark"] .code-length {
  color: #a0aec0;
}

[data-theme="dark"] .code-sample {
  color: #e2e8f0;
}

[data-theme="dark"] .share-url {
  background: #4a5568;
  border-color: #2d3748;
  color: #e2e8f0;
}

[data-theme="dark"] .share-url:focus {
  border-color: #4299e1;
  box-shadow: 0 0 0 2px rgba(66, 153, 225, 0.25);
}

[data-theme="dark"] .link-group label {
  color: #e2e8f0;
}

[data-theme="dark"] .loading-state {
  color: #a0aec0;
}

[data-theme="dark"] .spinner {
  border-color: #4a5568;
  border-top-color: #4299e1;
}

[data-theme="dark"] .error-state {
  color: #f56565;
}

/* ...rest of existing dark mode styles... */
</style>
