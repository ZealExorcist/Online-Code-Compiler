<template>
  <div class="code-editor-container">
    <div class="editor-toolbar">
      <div class="toolbar-left">
        <select v-model="currentLanguage" @change="onLanguageChange" class="language-selector">
          <option v-for="lang in languages" :key="lang.id" :value="lang.id">
            {{ lang.name }}
          </option>
        </select>
      </div>
      <div class="toolbar-center">
        <button @click="saveSnippet" class="save-snippet-btn" :disabled="!code.trim()">
          <i class="icon">💾</i>
          Save Snippet
        </button>
        <button 
          v-if="canDownloadCode" 
          @click="downloadCode" 
          class="download-btn" 
          :disabled="!code.trim()"
          :title="`Download code to your computer (${userTier || 'Premium'} feature)`"
        >
          <i class="icon">📥</i>
          Download
        </button>
        <button 
          v-else-if="isAuthenticated && userTier && userTier !== 'ADVANCED' && userTier !== 'MASTER'" 
          @click="showUpgradeInfo" 
          class="upgrade-hint-btn" 
          title="Upgrade to Advanced or Master tier to download code files"
        >
          <i class="icon">📥</i>
          Download 🔒
        </button>
      </div>
      <div class="toolbar-right">
        <button @click="runCode" :disabled="isLoading" class="btn btn-run">
          <span v-if="isLoading">⏳ Running...</span>
          <span v-else>▶️ Run</span>
        </button>
      </div>
    </div>
    <div class="editor-wrapper">
      <div v-if="!editorReady" class="editor-loading">
        <div class="spinner"></div>
        <p>Loading Code Editor...</p>
      </div>
      <div ref="editorContainer" class="codemirror-editor" :style="{ display: editorReady ? 'block' : 'none' }"></div>
    </div>
    
    <!-- Upgrade Info Modal -->
    <div v-if="showUpgradeModal" class="modal-overlay" @click="closeUpgradeModal">
      <div class="modal-content upgrade-modal" @click.stop>
        <div class="modal-header">
          <h3>🔒 Premium Feature</h3>
          <button @click="closeUpgradeModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="upgrade-info">
            <div class="feature-icon">📥</div>
            <h4>File Download Feature</h4>
            <p>This feature is available for <strong>Advanced</strong> and <strong>Master</strong> tier users.</p>
            <div class="current-tier">
              <span class="tier-label">Your current tier:</span>
              <span class="tier-badge" :class="userTier?.toLowerCase()">{{ userTier || 'Basic' }}</span>
            </div>
            <p class="upgrade-benefits">Upgrade your account to unlock:</p>
            <ul class="benefits-list">
              <li>📥 Download code files to your computer</li>
              <li>⚡ Custom package installation commands</li>
              <li>🚀 Faster execution with reduced delays</li>
              <li>📊 Higher rate limits</li>
            </ul>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeUpgradeModal" class="btn-primary">Got it!</button>
        </div>
      </div>
    </div>

    <!-- Filename Input Modal -->
    <div v-if="showFilenameModal" class="modal-overlay" @click="closeFilenameModal">
      <div class="modal-content filename-modal" @click.stop>
        <div class="modal-header">
          <h3>📥 Download Code File</h3>
          <button @click="closeFilenameModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="filename-input-section">
            <div class="input-icon">📁</div>
            <h4>Choose a filename</h4>
            <p>Enter a name for your code file:</p>
            <div class="form-group">
              <label for="filename-input">Filename</label>
              <input 
                type="text" 
                id="filename-input"
                v-model="customFileName" 
                placeholder="Enter filename..."
                class="form-input filename-input"
                @keyup.enter="confirmDownload"
                @focus="$event.target.select()"
                ref="filenameInput"
              />
              <div class="filename-help">
                <span class="help-text">Include file extension (e.g., .py, .js, .cpp)</span>
              </div>
            </div>
            <div class="language-info">
              <span class="language-tag">{{ currentLanguage }}</span>
              <span class="code-stats">{{ code.length }} characters</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeFilenameModal" class="btn-secondary">Cancel</button>
          <button @click="confirmDownload" class="btn-primary" :disabled="!customFileName.trim()">
            Download File
          </button>
        </div>
      </div>
    </div>

    <!-- Download Success Modal -->
    <div v-if="showDownloadSuccess" class="modal-overlay" @click="closeDownloadSuccess">
      <div class="modal-content download-success-modal" @click.stop>
        <div class="modal-header">
          <h3>✅ Download Successful</h3>
          <button @click="closeDownloadSuccess" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="success-info">
            <div class="success-icon">📥</div>
            <h4>File Downloaded Successfully!</h4>
            <p>Your code has been saved to your computer as:</p>
            <div class="file-info">
              <span class="filename">{{ downloadedFileName }}</span>
            </div>
            <p class="download-location">Check your Downloads folder or the location you specified.</p>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeDownloadSuccess" class="btn-primary">Got it!</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { createEditor } from '../codemirror.js'
import authService from '../services/auth.ts'
import { getUserProfile } from '../services/api.ts'
import { eventBus } from '../services/eventBus.ts'

export default {
  name: 'CodeEditor',
  props: {
    code: {
      type: String,
      default: ''
    },
    language: {
      type: String,
      default: 'python'
    },
    isLoading: {
      type: Boolean,
      default: false
    },
    settings: {
      type: Object,
      default: () => ({})
    }
  },  data() {
    return {
      editor: null,
      editorReady: false,
      currentLanguage: this.language,
      isChangingLanguage: false,
      languageChangeTimer: null,
      canDownloadCode: false,
      userTier: null,
      showUpgradeModal: false,
      showDownloadSuccess: false,
      showFilenameModal: false,
      downloadedFileName: '',
      customFileName: '',
      languages: [
        { id: 'python', name: 'Python' },
        { id: 'java', name: 'Java' },
        { id: 'cpp', name: 'C++' },
        { id: 'c', name: 'C' },
        { id: 'javascript', name: 'JavaScript' },
        { id: 'typescript', name: 'TypeScript' },
        { id: 'go', name: 'Go' },
        { id: 'rust', name: 'Rust' },
        { id: 'ruby', name: 'Ruby' },
        { id: 'r', name: 'R' },
        { id: 'csharp', name: 'C#' }
      ]
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.initEditor()
    })
    this.checkUserDownloadPermission()
    
    // Listen for tier updates
    eventBus.on('tier-updated', this.handleTierUpdate)
  },
  beforeUnmount() {
    if (this.languageChangeTimer) {
      clearTimeout(this.languageChangeTimer)
    }
    if (this.editor) {
      this.editor.destroy()
    }
    
    // Remove event listeners
    eventBus.off('tier-updated', this.handleTierUpdate)
  },
  watch: {
    code(newCode) {
      if (this.editor && this.editor.getValue() !== newCode) {
        this.editor.setValue(newCode)
      }
    },
    language(newLanguage) {
      this.currentLanguage = newLanguage
      if (this.editor) {
        this.editor.updateLanguage(newLanguage)
      }
    },
    settings: {
      handler(newSettings, oldSettings) {
        if (this.editor && newSettings) {
          this.editor.updateSettings(newSettings)
          
          // Check if error highlighting setting changed
          if (oldSettings && newSettings.enableErrorHighlighting !== oldSettings.enableErrorHighlighting) {
            // Recreate editor with new linting setting
            if (this.editor) {
              this.editor.destroy()
            }
            this.$nextTick(() => {
              this.initEditor()
            })
          }
        }
      },
      deep: true
    },
    showFilenameModal(isVisible) {
      if (isVisible) {
        this.$nextTick(() => {
          if (this.$refs.filenameInput) {
            this.$refs.filenameInput.focus()
            this.$refs.filenameInput.select()
          }
        })
      }
    }
  },
  created() {
    // Check download permission when component is created
    this.checkUserDownloadPermission()
  },
  computed: {
    isAuthenticated() {
      return authService.isAuthenticated()
    }
  },
  methods: {
    async checkUserDownloadPermission() {
      // Check if user is authenticated first
      if (!authService.isAuthenticated()) {
        this.canDownloadCode = false
        return
      }
      
      try {
        // Get user profile to check tier
        const profile = await getUserProfile()
        this.userTier = profile.tier
        
        // Allow download for Advanced and Master users only
        this.canDownloadCode = profile.tier === 'ADVANCED' || profile.tier === 'MASTER'
      } catch (error) {
        console.warn('Failed to check user tier, disabling download:', error)
        this.canDownloadCode = false
      }
    },
    
    handleTierUpdate(newTier) {
      // Update user tier and re-check download permission
      this.userTier = newTier
      this.canDownloadCode = newTier === 'ADVANCED' || newTier === 'MASTER'
    },
    initEditor() {
      try {
        this.editor = createEditor(this.$refs.editorContainer, {
          doc: this.code,
          language: this.currentLanguage,
          colorScheme: this.settings?.colorScheme || 'oneDark',
          fontSize: this.settings?.fontSize || '14px',
          tabSize: this.settings?.tabSize || 4,
          enableLinting: this.settings?.enableErrorHighlighting !== false, // Default to true
          onChange: (value) => {
            this.$emit('code-change', value)
          },
          onRun: () => {
            this.runCode()
          },
          onSave: () => {
            this.saveSnippet()
          }
        })

        this.editorReady = true
      } catch (error) {
        console.error('Failed to initialize CodeMirror Editor:', error)
      }
    },
    onLanguageChange() {
      if (this.isChangingLanguage) return
      
      this.isChangingLanguage = true
      
      // Clear any pending language change
      if (this.languageChangeTimer) {
        clearTimeout(this.languageChangeTimer)
      }
      
      // Debounce language changes to prevent issues
      this.languageChangeTimer = setTimeout(() => {
        try {
          this.$emit('language-change', this.currentLanguage)
          
          if (this.editor) {
            this.editor.updateLanguage(this.currentLanguage)
          }
        } catch (error) {
          console.error('Error changing language:', error)
        } finally {
          this.isChangingLanguage = false
        }
      }, 100) // Shorter debounce since CodeMirror is faster
    },
    runCode() {
      this.$emit('run-code')
    },
    saveSnippet() {
      this.$emit('save-snippet')
    },
    downloadCode() {
      if (!this.code.trim()) {
        return
      }

      // Get default filename with extension based on language
      const fileExtensions = {
        python: 'py',
        java: 'java',
        cpp: 'cpp',
        c: 'c',
        javascript: 'js',
        typescript: 'ts',
        go: 'go',
        rust: 'rs',
        ruby: 'rb',
        r: 'r',
        csharp: 'cs'
      }

      const extension = fileExtensions[this.currentLanguage] || 'txt'
      this.customFileName = `code.${extension}`
      
      // Show filename input modal
      this.showFilenameModal = true
    },
    
    confirmDownload() {
      if (!this.customFileName.trim()) {
        return
      }

      const filename = this.customFileName.trim()

      // Create blob and download
      const blob = new Blob([this.code], { type: 'text/plain' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = filename
      link.style.display = 'none'
      
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      // Clean up the URL object
      window.URL.revokeObjectURL(url)

      // Close filename modal and show success message
      this.showFilenameModal = false
      this.downloadedFileName = filename
      this.showDownloadSuccess = true
    },
    
    closeFilenameModal() {
      this.showFilenameModal = false
      this.customFileName = ''
    },
    
    showUpgradeInfo() {
      // Show upgrade modal instead of alert
      this.showUpgradeModal = true
    },
    
    closeUpgradeModal() {
      this.showUpgradeModal = false
    },
    
    closeDownloadSuccess() {
      this.showDownloadSuccess = false
      this.downloadedFileName = ''
    },
    
    openUpgradeOptions() {
      // Close current modal first
      this.closeUpgradeModal()
      
      console.log('CodeEditor: Emitting show-upgrade-modal event')
      
      // Emit event via eventBus to show standalone upgrade modal
      eventBus.emit('show-upgrade-modal')
    }
  }
}
</script>

<style scoped>
.code-editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--border-color);
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  background-color: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.toolbar-center {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.save-snippet-btn {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  min-width: 120px;
  justify-content: center;
}

.save-snippet-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.4);
}

.save-snippet-btn:disabled {
  background: #4a5568;
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
  box-shadow: none;
}

.download-btn {
  background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  min-width: 120px;
  justify-content: center;
}

.download-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(66, 153, 225, 0.4);
}

.download-btn:disabled {
  background: #4a5568;
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
  box-shadow: none;
}

.upgrade-hint-btn {
  background: linear-gradient(135deg, #a0aec0 0%, #718096 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  min-width: 120px;
  justify-content: center;
  position: relative;
}

.upgrade-hint-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(160, 174, 192, 0.4);
  background: linear-gradient(135deg, #cbd5e0 0%, #a0aec0 100%);
}

.save-snippet-btn .icon,
.download-btn .icon,
.upgrade-hint-btn .icon {
  font-size: 16px;
}

.toolbar-right {
  display: flex;
  gap: 0.5rem;
}

.language-selector {
  background-color: var(--bg-tertiary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 0.5rem;
  font-size: 0.9rem;
  min-width: 120px;
}

.language-selector:focus {
  outline: none;
  border-color: var(--accent-color);
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-run {
  background-color: var(--success-color);
  color: white;
}

.btn-run:hover:not(:disabled) {
  background-color: #218838;
}

.btn-share {
  background-color: #17a2b8;
  color: white;
}

.btn-share:hover {
  background-color: #138496;
}

.editor-wrapper {
  flex: 1;
  position: relative;
  height: 100%;
}

.codemirror-editor {
  height: 100%;
  width: 100%;
  border: none;
  border-radius: 0;
  overflow: hidden;
}

.codemirror-editor .cm-editor {
  border: none !important;
  border-radius: 0 !important;
}

.codemirror-editor .cm-gutters {
  border: none !important;
  border-right: none !important;
}

.codemirror-editor .cm-scroller {
  border: none !important;
}

.editor-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--text-primary);
}

.editor-loading .spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border-color);
  border-top: 4px solid var(--accent-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .editor-toolbar {
    flex-direction: column;
    gap: 0.5rem;
    align-items: stretch;
  }
  
  .toolbar-left,
  .toolbar-right {
    justify-content: center;
  }
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--bg-primary, white);
  border-radius: 12px;
  padding: 0;
  max-width: 500px;
  width: 90vw;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
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
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--border-color, #e5e7eb);
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* Upgrade Modal Specific Styles */
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

.current-tier {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 16px 0;
}

.tier-label {
  color: var(--text-secondary, #6b7280);
  font-size: 14px;
}

.tier-badge {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  background: #e5e7eb;
  color: #1f2937;
}

.tier-badge.basic {
  background: #dbeafe;
  color: #1e40af;
}

.tier-badge.advanced {
  background: #fef3c7;
  color: #92400e;
}

.tier-badge.master {
  background: #d1fae5;
  color: #065f46;
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

/* Download Success Modal Styles */
.success-info {
  text-align: center;
}

.success-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.success-info h4 {
  margin: 0 0 16px 0;
  color: var(--text-primary, #1f2937);
  font-size: 20px;
  font-weight: 600;
}

.file-info {
  background: var(--bg-secondary, #f9fafb);
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 8px;
  padding: 12px;
  margin: 16px 0;
}

.filename {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #1f2937);
}

.download-location {
  font-size: 14px;
  color: var(--text-secondary, #6b7280);
  margin: 16px 0 0 0 !important;
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
[data-theme="dark"] .modal-content {
  --bg-primary: #1f2937;
  --bg-secondary: #374151;
  --text-primary: #f9fafb;
  --text-secondary: #d1d5db;
  --border-color: #4b5563;
}

[data-theme="dark"] .close-btn:hover {
  background: #4b5563;
}

[data-theme="dark"] .btn-secondary {
  color: #d1d5db;
  border-color: #4b5563;
}

[data-theme="dark"] .btn-secondary:hover {
  background: #4b5563;
  border-color: #6b7280;
}

/* Filename Input Modal */
.filename-modal {
  max-width: 450px;
}

.filename-input-section {
  text-align: center;
}

.input-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.filename-input-section h4 {
  margin: 0 0 0.5rem 0;
  color: var(--text-primary, #1f2937);
  font-size: 1.25rem;
}

.filename-input-section p {
  margin: 0 0 1.5rem 0;
  color: var(--text-secondary, #6b7280);
}

.form-group {
  text-align: left;
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-primary, #1f2937);
  font-weight: 500;
  font-size: 0.9rem;
}

.filename-input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid var(--border-color, #e5e7eb);
  border-radius: 6px;
  font-size: 1rem;
  background: var(--bg-primary, white);
  color: var(--text-primary, #1f2937);
  transition: border-color 0.2s;
}

.filename-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.filename-help {
  margin-top: 0.5rem;
}

.help-text {
  font-size: 0.8rem;
  color: var(--text-secondary, #6b7280);
  font-style: italic;
}

.language-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg-secondary, #f9fafb);
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 6px;
  padding: 0.75rem;
  margin-top: 1rem;
}

.language-tag {
  background: #667eea;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
  text-transform: uppercase;
}

.code-stats {
  color: var(--text-secondary, #6b7280);
  font-size: 0.9rem;
}

/* Dark mode for filename modal */
[data-theme="dark"] .filename-input-section h4 {
  color: #f9fafb;
}

[data-theme="dark"] .filename-input-section p {
  color: #d1d5db;
}

[data-theme="dark"] .form-group label {
  color: #f9fafb;
}

[data-theme="dark"] .filename-input {
  background: #374151;
  border-color: #4b5563;
  color: #f9fafb;
}

[data-theme="dark"] .filename-input:focus {
  border-color: #667eea;
}

[data-theme="dark"] .help-text {
  color: #9ca3af;
}

[data-theme="dark"] .language-info {
  background: #374151;
  border-color: #4b5563;
}

[data-theme="dark"] .code-stats {
  color: #9ca3af;
}

/* Error highlighting styles */
.codemirror-editor .cm-diagnostic {
  padding: 3px 6px 3px 8px;
  margin-left: -1px;
  display: block;
  white-space: pre-wrap;
}

.codemirror-editor .cm-diagnostic-error {
  border-left: 3px solid #d73a49;
  background-color: rgba(215, 58, 73, 0.1);
  color: #d73a49;
}

.codemirror-editor .cm-diagnostic-warning {
  border-left: 3px solid #f66a0a;
  background-color: rgba(246, 106, 10, 0.1);
  color: #f66a0a;
}

.codemirror-editor .cm-diagnostic-info {
  border-left: 3px solid #0969da;
  background-color: rgba(9, 105, 218, 0.1);
  color: #0969da;
}

.codemirror-editor .cm-diagnosticText {
  font-size: 12px;
  font-family: var(--font-family-mono, 'Monaco', 'Menlo', 'Ubuntu Mono', monospace);
}

/* Error gutter styles */
.codemirror-editor .cm-lint-marker {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 4px;
  position: relative;
  top: 2px;
}

.codemirror-editor .cm-lint-marker-error {
  background: #d73a49;
  color: white;
  text-align: center;
  font-size: 10px;
  line-height: 16px;
}

.codemirror-editor .cm-lint-marker-warning {
  background: #f66a0a;
  color: white;
  text-align: center;
  font-size: 10px;
  line-height: 16px;
}

.codemirror-editor .cm-lint-marker-info {
  background: #0969da;
  color: white;
  text-align: center;
  font-size: 10px;
  line-height: 16px;
}

/* Error underlines */
.codemirror-editor .cm-lintRange-error {
  background-image: url("data:image/svg+xml,%3csvg width='6' height='3' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='m0 3 l3 -3 l1 0 l1 3 l1 0' stroke='%23d73a49' fill='none' stroke-width='.7'/%3e%3c/svg%3e");
  background-repeat: repeat-x;
  background-position: bottom;
  padding-bottom: 2px;
}

.codemirror-editor .cm-lintRange-warning {
  background-image: url("data:image/svg+xml,%3csvg width='6' height='3' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='m0 3 l3 -3 l1 0 l1 3 l1 0' stroke='%23f66a0a' fill='none' stroke-width='.7'/%3e%3c/svg%3e");
  background-repeat: repeat-x;
  background-position: bottom;
  padding-bottom: 2px;
}

.codemirror-editor .cm-lintRange-info {
  background-image: url("data:image/svg+xml,%3csvg width='6' height='3' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='m0 3 l3 -3 l1 0 l1 3 l1 0' stroke='%230969da' fill='none' stroke-width='.7'/%3e%3c/svg%3e");
  background-repeat: repeat-x;
  background-position: bottom;
  padding-bottom: 2px;
}

/* Dark mode error highlighting */
[data-theme="dark"] .codemirror-editor .cm-diagnostic-error {
  border-left-color: #f85149;
  background-color: rgba(248, 81, 73, 0.15);
  color: #f85149;
}

[data-theme="dark"] .codemirror-editor .cm-diagnostic-warning {
  border-left-color: #ff8c42;
  background-color: rgba(255, 140, 66, 0.15);
  color: #ff8c42;
}

[data-theme="dark"] .codemirror-editor .cm-diagnostic-info {
  border-left-color: #58a6ff;
  background-color: rgba(88, 166, 255, 0.15);
  color: #58a6ff;
}

[data-theme="dark"] .codemirror-editor .cm-lint-marker-error {
  background: #f85149;
}

[data-theme="dark"] .codemirror-editor .cm-lint-marker-warning {
  background: #ff8c42;
}

[data-theme="dark"] .codemirror-editor .cm-lint-marker-info {
  background: #58a6ff;
}

[data-theme="dark"] .codemirror-editor .cm-lintRange-error {
  background-image: url("data:image/svg+xml,%3csvg width='6' height='3' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='m0 3 l3 -3 l1 0 l1 3 l1 0' stroke='%23f85149' fill='none' stroke-width='.7'/%3e%3c/svg%3e");
}

[data-theme="dark"] .codemirror-editor .cm-lintRange-warning {
  background-image: url("data:image/svg+xml,%3csvg width='6' height='3' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='m0 3 l3 -3 l1 0 l1 3 l1 0' stroke='%23ff8c42' fill='none' stroke-width='.7'/%3e%3c/svg%3e");
}

[data-theme="dark"] .codemirror-editor .cm-lintRange-info {
  background-image: url("data:image/svg+xml,%3csvg width='6' height='3' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='m0 3 l3 -3 l1 0 l1 3 l1 0' stroke='%23058a6ff' fill='none' stroke-width='.7'/%3e%3c/svg%3e");
}

/* Tooltip styles for error messages */
.codemirror-editor .cm-tooltip.cm-tooltip-lint {
  background: var(--bg-primary, white);
  border: 1px solid var(--border-color, #e1e4e8);
  border-radius: 6px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  color: var(--text-primary, #24292e);
  font-size: 12px;
  max-width: 300px;
  padding: 8px 12px;
  z-index: 1000;
}

[data-theme="dark"] .codemirror-editor .cm-tooltip.cm-tooltip-lint {
  background: #21262d;
  border-color: #30363d;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  color: #c9d1d9;
}
</style>
