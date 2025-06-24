<template>
  <div class="compiler-interface">
    <Header @load-snippet="handleLoadSnippet" @theme-changed="$emit('theme-changed', $event)" @settings-updated="handleSettingsUpdate" />
    
    <!-- Loading State for Snippet -->
    <div v-if="isLoadingSnippet" class="snippet-loading">
      <div class="loading-spinner"></div>
      <p>Loading shared code...</p>
    </div>
    
    <!-- Action Bar -->
    <div class="action-bar">
      <div class="left-actions">
        <LoadComponent @code-loaded="handleCodeLoaded" />
      </div>
      <div class="center-actions">
        <CommandsComponent 
          :language="selectedLanguage" 
          @packages-installed="handlePackagesInstalled"
        />
        <button @click="toggleInputSection" :class="['input-toggle-btn', { active: showInputSection }]">
          <i class="icon">{{ showInputSection ? '📥' : '📤' }}</i>
          {{ showInputSection ? 'Hide Input' : 'Show Input' }}
        </button>
      </div>
      <div class="right-actions">
        <ShareComponent 
          :code="currentCode" 
          :language="selectedLanguage" 
          :input="currentInput"
        />
      </div>
    </div>
    
    <div class="main-content">
      <div class="editor-section" :style="{ width: editorWidth + '%' }">
        <CodeEditor 
          :code="currentCode" 
          :language="selectedLanguage"
          :settings="settings"
          @code-change="handleCodeChange"
          @language-change="handleLanguageChange"
          @run-code="executeCode"
          @save-snippet="saveSnippet"
          @file-downloaded="handleFileDownloaded"
          @show-upgrade="handleShowUpgrade"
          :isLoading="isExecuting"
        />
        
        <!-- Input Section -->
        <div v-if="showInputSection" class="input-section">
          <div class="input-header">
            <label for="program-input">Program Input</label>
            <span class="input-info">💡 Provide input for your program (if needed)</span>
          </div>
          <textarea
            id="program-input"
            v-model="currentInput"
            placeholder="Enter input for your program here... (one line per input)"
            class="input-textarea"
            rows="3"
          ></textarea>
        </div>
      </div>
      <div class="resizer" @mousedown="startResize"></div>
      <div class="output-section" :style="{ width: (100 - editorWidth) + '%' }">
        <OutputPanel 
          :output="executionOutput"
          :isLoading="isExecuting"
          @clear-output="clearOutput"
        />
      </div>
    </div>
    
    <!-- Save Snippet Modal -->
    <div v-if="showSaveModal" class="modal-overlay" @click="closeSaveModal">
      <div class="modal-content save-snippet-modal" @click.stop>
        <div class="modal-header">
          <h3>💾 Save Code Snippet</h3>
          <button @click="closeSaveModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label for="snippet-title">Snippet Title</label>
            <input 
              type="text" 
              id="snippet-title"
              v-model="saveModalData.title" 
              placeholder="Enter a descriptive title for your snippet..."
              class="form-input"
              @keyup.enter="confirmSaveSnippet"
            />
          </div>
          <div class="snippet-preview">
            <div class="preview-header">
              <span class="language-tag">{{ selectedLanguage }}</span>
              <span class="code-length">{{ currentCode.length }} characters</span>
            </div>
            <pre class="code-preview">{{ currentCode.substring(0, 200) }}{{ currentCode.length > 200 ? '...' : '' }}</pre>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeSaveModal" class="btn btn-cancel">
            Cancel
          </button>
          <button @click="confirmSaveSnippet" class="btn btn-save" :disabled="isSaving">
            <span v-if="isSaving">⏳ Saving...</span>
            <span v-else>💾 Save Snippet</span>
          </button>
        </div>
      </div>
    </div>
    
    <Footer />
  </div>
</template>

<script>
import Header from './Header.vue'
import CodeEditor from './CodeEditor.vue'
import OutputPanel from './OutputPanel.vue'
import Footer from './Footer.vue'
import ShareComponent from './ShareComponent.vue'
import LoadComponent from './LoadComponent.vue'
import CommandsComponent from './CommandsComponent.vue'
import { executeCode, loadSharedCode, saveSnippet } from '../services/api'
import settingsService from '../services/settings'
import authService from '../services/auth'
import { eventBus } from '../services/eventBus.ts'

export default {
  name: 'CompilerInterface',  components: {
    Header,
    CodeEditor,
    OutputPanel,
    Footer,
    ShareComponent,
    LoadComponent,
    CommandsComponent
  },
  props: {
    snippetId: {
      type: String,
      default: null
    }
  },  data() {
    return {
      currentCode: `print("Hello, World!")`,
      selectedLanguage: 'python',
      currentInput: '',
      executionOutput: null,
      isExecuting: false,
      isLoadingSnippet: false,
      showSaveModal: false,
      isSaving: false,
      saveModalData: {
        title: ''
      },
      editorWidth: 50, // Percentage width for the editor
      isResizing: false,
      showInputSection: false, // Input section is hidden by default
      settings: {
        theme: 'dark',
        colorScheme: 'oneDark',
        fontSize: '14px',
        fontFamily: "'Monaco', 'Menlo', 'Ubuntu Mono', monospace",
        tabSize: 4
      } // Initialize with default settings
    }
  },
  async mounted() {
    if (this.snippetId) {
      await this.loadSnippet()
    }
    // Load user settings if authenticated
    await this.loadUserSettings()
  },
  watch: {
    snippetId: {
      handler: 'loadSnippet',
      immediate: true
    }
  },  methods: {
    async loadSnippet() {
      if (!this.snippetId) return
      
      this.isLoadingSnippet = true
      try {
        const data = await loadSharedCode(this.snippetId)
        this.currentCode = data.code
        this.selectedLanguage = data.language
        this.currentInput = data.input || ''
        
        // Show success message
        this.$nextTick(() => {
          const title = data.title || 'Untitled'
          // Snippet loaded successfully
        })
      } catch (error) {
        console.error('Failed to load snippet:', error)
        alert(`Failed to load shared code: ${error.message}`)
      } finally {
        this.isLoadingSnippet = false
      }
    },    handleCodeChange(newCode) {
      this.currentCode = newCode
    },    handleLanguageChange(newLanguage) {
      this.selectedLanguage = newLanguage
      this.currentCode = this.getDefaultCode(newLanguage)
    },
    
    handleSettingsUpdate(newSettings) {
      this.settings = { ...this.settings, ...newSettings }
    },
    
    async loadUserSettings() {
      try {
        if (authService.isAuthenticated()) {
          const userSettings = await settingsService.getUserSettings()
          this.settings = { ...this.settings, ...userSettings }
        } else {
          // For anonymous users, only use cached settings - no API call
          const cachedSettings = settingsService.getCachedSettings()
          this.settings = { ...this.settings, ...cachedSettings }
        }
      } catch (error) {
        console.warn('Failed to load user settings, using cached settings:', error)
        const cachedSettings = settingsService.getCachedSettings()
        this.settings = { ...this.settings, ...cachedSettings }
      }
    },
    handleCodeLoaded(loadedData) {
      this.currentCode = loadedData.code
      this.selectedLanguage = loadedData.language
      this.currentInput = loadedData.input || ''
      
      // Show a success message
      this.$nextTick(() => {
        alert(`Code loaded successfully!\nLanguage: ${loadedData.language}\nTitle: ${loadedData.title || 'Untitled'}`)
      })
    },
    
    handleFileDownloaded(filename) {
      // File download success is now handled by CodeEditor's modal
      // This can be removed or used for additional logging if needed
    },
    
    handleShowUpgrade() {
      // Emit event to show upgrade modal via event bus
      eventBus.emit('show-upgrade-modal')
    },
    async executeCode() {
      if (!this.currentCode.trim()) {
        alert('Please enter some code to execute.')
        return
      }

      this.isExecuting = true
      this.executionOutput = null

      try {
        const result = await executeCode(this.currentCode, this.selectedLanguage, this.currentInput)
        this.executionOutput = result
      } catch (error) {
        this.executionOutput = {
          error: 'Failed to execute code: ' + error.message,
          exitCode: -1
        }
      } finally {
        this.isExecuting = false
      }
    },
    clearOutput() {
      this.executionOutput = null
    },
    getDefaultCode(language) {
      const defaults = {
        python: `print("Hello, World!")`,
        java: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}`,
        cpp: `#include <iostream>
using namespace std;

int main() {
    cout << "Hello, World!" << endl;
    return 0;
}`,
        c: `#include <stdio.h>

int main() {
    printf("Hello, World!\\n");
    return 0;
}`,
        javascript: `console.log("Hello, World!");`,
        typescript: `function greet(name: string): string {
    return "Hello, " + name + "!";
}

console.log(greet("TypeScript"));`,
        go: `package main

import "fmt"

func main() {
    fmt.Println("Hello, World!")
}`,
        rust: `fn main() {
    println!("Hello, World!");
}`,
        ruby: `puts "Hello, World!"`,
        r: `print("Hello, World!")`,
        csharp: `using System;

class Program {
    static void Main() {
        Console.WriteLine("Hello, World!");
    }
}`
      }
      return defaults[language] || `// ${language} code here`
    },
    
    handlePackagesInstalled(installationData) {
      // Show success notification
      const { packages, commands, language, mode } = installationData
      
      // Handle different modes - predefined mode has packages, custom mode has commands
      let items = []
      if (mode === 'predefined' && packages && Array.isArray(packages)) {
        items = packages
      } else if (mode === 'custom' && commands && Array.isArray(commands)) {
        items = commands
      }
      
      const itemList = items.join(', ')
      
      // TODO: Show success notification here
      
      // Optionally, you could add a comment to the code editor about the installed packages
      if (items.length > 0) {
        const comment = this.getCommentSyntax(language)
        const itemComment = `${comment} Installed ${mode === 'predefined' ? 'packages' : 'commands'}: ${itemList}\n`
        
        // Add comment at the top of the code if user wants
        // this.currentCode = itemComment + this.currentCode
      }
    },
    
    getCommentSyntax(language) {
      const commentSyntax = {
        python: '#',
        javascript: '//',
        typescript: '//',
        java: '//',
        cpp: '//',
        c: '//',
        go: '//',
        rust: '//',
        csharp: '//',
        ruby: '#',
        r: '#'
      }
      return commentSyntax[language] || '#'
    },    async saveSnippet() {
      if (!this.currentCode.trim()) return
      
      this.saveModalData.title = ''
      this.showSaveModal = true
    },

    closeSaveModal() {
      this.showSaveModal = false
      this.saveModalData.title = ''
      this.isSaving = false
    },

    async confirmSaveSnippet() {
      if (!this.currentCode.trim()) return
      
      this.isSaving = true
      
      try {
        const response = await fetch('/api/user/snippets', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('auth_token')}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            title: this.saveModalData.title || 'Untitled Snippet',
            code: this.currentCode,
            language: this.selectedLanguage,
            input: this.currentInput
          })
        })
        
        if (response.ok) {
          const data = await response.json()
          if (data.success) {
            this.showSuccessNotification('Snippet saved successfully!')
            this.closeSaveModal()
          } else {
            this.showErrorNotification('Failed to save snippet: ' + (data.message || 'Unknown error'))
          }
        } else {
          const errorData = await response.json()
          this.showErrorNotification('Failed to save snippet: ' + (errorData.message || 'Server error'))
        }
      } catch (error) {
        console.error('Failed to save snippet:', error)
        this.showErrorNotification('Failed to save snippet. Please try again.')
      } finally {
        this.isSaving = false
      }
    },

    showSuccessNotification(message) {
      // Create a simple styled notification that matches the theme
      this.createNotification(message, 'success')
    },

    showErrorNotification(message) {
      // Create a simple styled notification that matches the theme
      this.createNotification(message, 'error')
    },
    
    createNotification(message, type) {
      const notification = document.createElement('div')
      notification.className = `simple-notification simple-notification-${type}`
      notification.innerHTML = `
        <div class="notification-content">
          <span class="notification-icon">${type === 'success' ? '✅' : '❌'}</span>
          <span class="notification-message">${message}</span>
        </div>
      `
      
      // Add styles
      const style = document.createElement('style')
      style.textContent = `
        .simple-notification {
          position: fixed;
          top: 20px;
          right: 20px;
          z-index: 10000;
          background: var(--toast-bg);
          color: var(--toast-text);
          border: 1px solid var(--toast-border);
          border-radius: 8px;
          padding: 16px;
          box-shadow: 0 4px 12px var(--toast-shadow);
          animation: slideInRight 0.3s ease-out;
          max-width: 400px;
        }
        .simple-notification-success {
          border-left: 4px solid var(--success-color);
        }
        .simple-notification-error {
          border-left: 4px solid var(--error-color);
        }
        .notification-content {
          display: flex;
          align-items: center;
          gap: 12px;
        }
        .notification-icon {
          font-size: 20px;
          flex-shrink: 0;
        }
        .notification-message {
          font-weight: 500;
        }
        @keyframes slideInRight {
          from { transform: translateX(100%); opacity: 0; }
          to { transform: translateX(0); opacity: 1; }
        }
      `
      
      document.head.appendChild(style)
      document.body.appendChild(notification)
      
      // Remove notification after 4 seconds
      setTimeout(() => {
        notification.style.animation = 'slideInRight 0.3s ease-out reverse'
        setTimeout(() => {
          if (notification.parentNode) {
            notification.parentNode.removeChild(notification)
          }
          if (style.parentNode) {
            style.parentNode.removeChild(style)
          }
        }, 300)
      }, 4000)
    },
    handleLoadSnippet(snippet) {
      this.currentCode = snippet.code || ''
      this.selectedLanguage = snippet.language || 'javascript'
      this.currentInput = snippet.input || ''
      // Trigger the editor to update
      this.$nextTick(() => {
        if (this.$refs.editor) {
          this.$refs.editor.updateCode(this.currentCode)
          this.$refs.editor.updateLanguage(this.selectedLanguage)
        }
      })
    },
    startResize(event) {
      this.isResizing = true
      document.addEventListener('mousemove', this.handleResize)
      document.addEventListener('mouseup', this.stopResize)
      event.preventDefault()
    },

    handleResize(event) {
      if (!this.isResizing) return
      
      const container = this.$el.querySelector('.main-content')
      const containerRect = container.getBoundingClientRect()
      const newWidth = ((event.clientX - containerRect.left) / containerRect.width) * 100
      
      // Constrain between 20% and 80%
      this.editorWidth = Math.max(20, Math.min(80, newWidth))
    },

    stopResize() {
      this.isResizing = false
      document.removeEventListener('mousemove', this.handleResize)
      document.removeEventListener('mouseup', this.stopResize)
    },
    toggleInputSection() {
      this.showInputSection = !this.showInputSection
    }
  },
  beforeUnmount() {
    // Clean up resize listeners
    document.removeEventListener('mousemove', this.handleResize)
    document.removeEventListener('mouseup', this.stopResize)
  },
}
</script>

<style scoped>
.compiler-interface {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.snippet-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border-color);
  border-top: 4px solid var(--accent-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.snippet-loading p {
  margin: 0;
  color: var(--text-muted);
  font-size: 14px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  gap: 10px;
}

.left-actions,
.center-actions,
.right-actions {
  display: flex;
  gap: 10px;
}

.center-actions {
  flex: 1;
  justify-content: center;
}

.input-toggle-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.input-toggle-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.input-toggle-btn.active {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
}

.input-toggle-btn.active:hover {
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.4);
}

.input-toggle-btn .icon {
  font-size: 16px;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.editor-section {
  display: flex;
  flex-direction: column;
  min-width: 0; /* Allow shrinking */
}

.resizer {
  width: 4px;
  background: var(--border-color);
  cursor: col-resize;
  transition: background-color 0.2s;
  flex-shrink: 0;
}

.resizer:hover {
  background: var(--accent-color);
}

.output-section {
  display: flex;
  flex-direction: column;
  min-width: 0; /* Allow shrinking */
}

/* Input Section Styles */
.input-section {
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  padding: 12px 20px;
}

.input-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.input-header label {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 14px;
}

.input-info {
  color: var(--text-muted);
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.input-textarea {
  width: 100%;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 10px;
  color: var(--text-primary);
  font-family: 'Fira Code', Consolas, 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.4;
  resize: vertical;
  min-height: 60px;
  max-height: 200px;
}

.input-textarea:focus {
  outline: none;
  border-color: var(--accent-color);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-textarea::placeholder {
  color: var(--text-muted);
}

/* Save Snippet Modal Styles */
.save-snippet-modal {
  max-width: 600px;
  width: 90vw;
}

.save-snippet-modal .modal-header {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  border-radius: 12px 12px 0 0;
}

.save-snippet-modal .modal-header h3 {
  color: white;
}

.save-snippet-modal .close-btn {
  color: white;
}

.save-snippet-modal .close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--modal-text);
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid var(--border-color);
  border-radius: 8px;
  font-size: 14px;
  background: var(--modal-bg);
  color: var(--modal-text);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: var(--success-color);
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.1);
}

.snippet-preview {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.language-tag {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.code-length {
  font-size: 12px;
  color: var(--text-muted);
}

.code-preview {
  background: var(--editor-bg);
  color: var(--editor-text);
  padding: 12px;
  border-radius: 6px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 0 0 12px 12px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-cancel {
  background: var(--bg-secondary);
  color: var(--modal-text);
  border: 1px solid var(--border-color);
}

.btn-cancel:hover {
  background: var(--bg-tertiary);
}

.btn-save {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
}

.btn-save:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.4);
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* Modal Base Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--overlay-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: var(--modal-bg);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--modal-text);
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--text-muted);
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
  background: var(--bg-secondary);
}

.modal-body {
  padding: 24px;
}

@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    gap: 10px;
  }
  
  .left-actions,
  .center-actions,
  .right-actions {
    width: 100%;
    justify-content: center;
  }
  
  .editor-actions {
    justify-content: center;
  }
  
  .main-content {
    flex-direction: column;
  }
  
  .editor-section {
    width: 100% !important;
    border-bottom: 1px solid #4a5568;
  }
  
  .resizer {
    display: none;
  }
  
  .output-section {
    width: 100% !important;
  }
}
</style>
