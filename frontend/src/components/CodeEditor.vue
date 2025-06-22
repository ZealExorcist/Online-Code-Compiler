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
  </div>
</template>

<script>
import { createEditor } from '../codemirror.js'

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
  },
  beforeUnmount() {
    if (this.languageChangeTimer) {
      clearTimeout(this.languageChangeTimer)
    }
    if (this.editor) {
      this.editor.destroy()
    }
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
        }
      },
      deep: true
    }
  },
  methods: {
    initEditor() {
      try {
        this.editor = createEditor(this.$refs.editorContainer, {
          doc: this.code,
          language: this.currentLanguage,
          colorScheme: this.settings?.colorScheme || 'oneDark',
          fontSize: this.settings?.fontSize || '14px',
          tabSize: this.settings?.tabSize || 4,
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
</style>
