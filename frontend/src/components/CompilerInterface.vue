<template>
  <div class="compiler-interface">
    <Header @load-snippet="handleLoadSnippet" />
    
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
        <UpgradeComponent @tier-updated="handleTierUpdated" />
      </div>
      <div class="right-actions">
        <ShareComponent 
          :code="currentCode" 
          :language="selectedLanguage" 
          :input="currentInput"
        />
      </div>    </div>
    
    <div class="main-content">
      <div class="editor-section" :style="{ width: editorWidth + '%' }">
        <CodeEditor 
          :code="currentCode" 
          :language="selectedLanguage"
          @code-change="handleCodeChange"
          @language-change="handleLanguageChange"
          @run-code="executeCode"
          :isLoading="isExecuting"
        />
        <div class="editor-actions">
          <button @click="saveSnippet" class="save-snippet-btn" :disabled="!currentCode.trim()">
            <i class="icon">💾</i>
            Save Snippet
          </button>
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
import UpgradeComponent from './UpgradeComponent.vue'
import { executeCode, loadSharedCode } from '../services/api'

export default {
  name: 'CompilerInterface',  components: {
    Header,
    CodeEditor,
    OutputPanel,
    Footer,
    ShareComponent,
    LoadComponent,
    UpgradeComponent
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
      isExecuting: false,      isLoadingSnippet: false,
      showSaveModal: false,
      isSaving: false,
      saveModalData: {
        title: ''
      },
      editorWidth: 50, // Percentage width for the editor
      isResizing: false
    }
  },
  async mounted() {
    if (this.snippetId) {
      await this.loadSnippet()
    }
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
          console.log(`Loaded shared code: ${title} (${data.language})`)
        })
      } catch (error) {
        console.error('Failed to load snippet:', error)
        alert(`Failed to load shared code: ${error.message}`)
      } finally {
        this.isLoadingSnippet = false
      }
    },
    handleCodeChange(newCode) {
      this.currentCode = newCode
    },handleLanguageChange(newLanguage) {
      this.selectedLanguage = newLanguage
      this.currentCode = this.getDefaultCode(newLanguage)
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
    handleTierUpdated(newTier) {
      console.log(`Tier updated to: ${newTier}`)
      // You can add any additional logic here, like refreshing user info
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
      // You can implement a toast notification system here
      alert(message) // Temporary fallback
    },

    showErrorNotification(message) {
      // You can implement a toast notification system here
      alert(message) // Temporary fallback
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
}

.snippet-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e9ecef;
  border-top: 4px solid #007bff;
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
  color: #6c757d;
  font-size: 14px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
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
  background: #4a5568;
  cursor: col-resize;
  transition: background-color 0.2s;
  flex-shrink: 0;
}

.resizer:hover {
  background: #667eea;
}

.output-section {
  display: flex;
  flex-direction: column;
  min-width: 0; /* Allow shrinking */
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  padding: 8px 20px;
  background: #2d3748;
  border-bottom: 1px solid #4a5568;
  gap: 10px;
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

.save-snippet-btn .icon {
  font-size: 16px;
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
  color: #374151;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #48bb78;
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.1);
}

.snippet-preview {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
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
  color: #6b7280;
}

.code-preview {
  background: #1f2937;
  color: #f9fafb;
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
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-cancel:hover {
  background: #e5e7eb;
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
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: white;
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
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
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
