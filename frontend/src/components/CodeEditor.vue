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
      <div class="toolbar-right">
        <button @click="runCode" :disabled="isLoading" class="btn btn-run">
          <span v-if="isLoading">⏳ Running...</span>
          <span v-else>▶️ Run</span>
        </button>
        <button @click="shareCode" class="btn btn-share">
          📤 Share
        </button>
      </div>
    </div>    <div class="editor-wrapper">
      <div v-if="!editorReady" class="editor-loading">
        <div class="spinner"></div>
        <p>Loading Monaco Editor...</p>
      </div>
      <div ref="editorContainer" class="monaco-editor" :style="{ display: editorReady ? 'block' : 'none' }"></div>
    </div>
  </div>
</template>

<script>
import { monaco } from '../monaco.js'

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
      languages: [
        { id: 'python', name: 'Python' },
        { id: 'java', name: 'Java' },
        { id: 'cpp', name: 'C++' },
        { id: 'c', name: 'C' },
        { id: 'javascript', name: 'JavaScript' },
        { id: 'go', name: 'Go' },
        { id: 'rust', name: 'Rust' },
        { id: 'ruby', name: 'Ruby' },
        { id: 'r', name: 'R' },
        { id: 'csharp', name: 'C#' }
      ]
    }
  },
  mounted() {
    this.initMonaco()
  },
  beforeUnmount() {
    if (this.editor) {
      this.editor.dispose()
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
        monaco.editor.setModelLanguage(this.editor.getModel(), this.getMonacoLanguage(newLanguage))
      }
    }
  },  methods: {
    async initMonaco() {
      try {
        // Configure Monaco Editor
        monaco.editor.defineTheme('dark-theme', {
          base: 'vs-dark',
          inherit: true,
          rules: [],
          colors: {
            'editor.background': '#1e1e1e',
            'editor.foreground': '#d4d4d4',
            'editorLineNumber.foreground': '#858585',
            'editor.selectionBackground': '#264f78',
            'editor.inactiveSelectionBackground': '#3a3d41'
          }
        })

        this.editor = monaco.editor.create(this.$refs.editorContainer, {
          value: this.code,
          language: this.getMonacoLanguage(this.currentLanguage),
          theme: 'dark-theme',
          fontSize: 14,
          lineNumbers: 'on',
          roundedSelection: false,
          scrollBeyondLastLine: false,
          readOnly: false,
          minimap: { enabled: true },
          automaticLayout: true,
          wordWrap: 'on',
          tabSize: 4,
          insertSpaces: true
        })

        // Listen for content changes
        this.editor.onDidChangeModelContent(() => {
          const value = this.editor.getValue()
          this.$emit('code-change', value)
        })

        // Keyboard shortcuts
        this.editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter, () => {
          this.runCode()
        })

        this.editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => {
          this.shareCode()
        })

        this.editorReady = true
      } catch (error) {
        console.error('Failed to initialize Monaco Editor:', error)
      }
    },
    getMonacoLanguage(lang) {
      const mappings = {
        'python': 'python',
        'java': 'java',
        'cpp': 'cpp',
        'c': 'c',
        'javascript': 'javascript',
        'go': 'go',
        'rust': 'rust',
        'ruby': 'ruby',
        'r': 'r',
        'csharp': 'csharp'
      }
      return mappings[lang] || 'plaintext'
    },
    onLanguageChange() {
      this.$emit('language-change', this.currentLanguage)
      if (this.editor) {
        monaco.editor.setModelLanguage(this.editor.getModel(), this.getMonacoLanguage(this.currentLanguage))
      }
    },
    runCode() {
      this.$emit('run-code')
    },
    shareCode() {
      this.$emit('share-code')
    }
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

.monaco-editor {
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
