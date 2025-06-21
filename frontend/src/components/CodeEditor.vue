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
        <div class="editor-settings">
          <button @click="toggleWordWrap" :class="['setting-btn', { active: editorSettings.wordWrap }]" title="Toggle Word Wrap">
            📝 Wrap
          </button>
          <button @click="toggleMinimap" :class="['setting-btn', { active: editorSettings.minimap }]" title="Toggle Minimap">
            🗺️ Map
          </button>
          <button @click="toggleInsertSpaces" :class="['setting-btn', { active: editorSettings.insertSpaces }]" title="Toggle Insert Spaces">
            {{ editorSettings.insertSpaces ? '␣' : '→' }} {{ editorSettings.insertSpaces ? 'Spaces' : 'Tabs' }}
          </button>
        </div>
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
    }
  },  data() {
    return {
      editor: null,
      editorReady: false,
      currentLanguage: this.language,
      isChangingLanguage: false,
      languageChangeTimer: null,
      editorSettings: {
        wordWrap: false,
        minimap: true,
        insertSpaces: true,
        tabSize: 4
      },
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
    this.initEditor()
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
    }
  },
  methods: {
    initEditor() {
      try {
        this.editor = createEditor(this.$refs.editorContainer, {
          doc: this.code,
          language: this.currentLanguage,
          wordWrap: this.editorSettings.wordWrap,
          minimap: this.editorSettings.minimap,
          insertSpaces: this.editorSettings.insertSpaces,
          tabSize: this.editorSettings.tabSize,
          onChange: (value) => {
            this.$emit('code-change', value)
          },
          onRun: () => {
            this.runCode()
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
    toggleWordWrap() {
      this.editorSettings.wordWrap = !this.editorSettings.wordWrap
      if (this.editor) {
        this.editor.updateWordWrap(this.editorSettings.wordWrap)
      }
    },
    toggleMinimap() {
      this.editorSettings.minimap = !this.editorSettings.minimap
      if (this.editor) {
        this.editor.updateMinimap(this.editorSettings.minimap)
      }
    },
    toggleInsertSpaces() {
      this.editorSettings.insertSpaces = !this.editorSettings.insertSpaces
      if (this.editor) {
        this.editor.updateIndentConfig(this.editorSettings.insertSpaces, this.editorSettings.tabSize)
      }
    },
  }
}
</script>

<style scoped>
.code-editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #1e1e1e;
  border-right: 1px solid #3c3c3c;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  background-color: #252526;
  border-bottom: 1px solid #3c3c3c;
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

.editor-settings {
  display: flex;
  gap: 0.5rem;
}

.setting-btn {
  padding: 0.4rem 0.8rem;
  border: 1px solid #5a5a5a;
  border-radius: 4px;
  background-color: #3c3c3c;
  color: #d4d4d4;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 500;
  transition: all 0.2s;
}

.setting-btn:hover {
  background-color: #4a4a4a;
}

.setting-btn.active {
  background-color: #007acc;
  border-color: #007acc;
  color: white;
}

.toolbar-right {
  display: flex;
  gap: 0.5rem;
}

.language-selector {
  background-color: #3c3c3c;
  color: #d4d4d4;
  border: 1px solid #5a5a5a;
  border-radius: 4px;
  padding: 0.5rem;
  font-size: 0.9rem;
  min-width: 120px;
}

.language-selector:focus {
  outline: none;
  border-color: #007acc;
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
  background-color: #28a745;
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
}

.codemirror-editor {
  height: 100%;
  width: 100%;
}

.editor-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #d4d4d4;
}

.editor-loading .spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #3c3c3c;
  border-top: 4px solid #007acc;
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
