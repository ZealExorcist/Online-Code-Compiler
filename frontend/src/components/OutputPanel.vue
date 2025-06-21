<template>
  <div class="output-panel">
    <div class="output-header">
      <h3>Output</h3>      <div class="output-controls">
        <button @click="toggleInputPanel" class="btn btn-input" :class="{ active: showInputPanel }">
          ⌨️ Input
        </button>
        <button @click="clearOutput" class="btn btn-clear" :disabled="!output">
          🗑️ Clear
        </button>
      </div>
    </div>
      <div class="output-content">
      <div v-if="isLoading" class="loading">
        <div class="spinner"></div>
        <p>Executing code...</p>
      </div>
      
      <div v-else-if="!output" class="empty-state">
        <p>👨‍💻 Click "Run" to execute your code</p>
        <div class="shortcuts">
          <small>Shortcuts: Ctrl+Enter to run, Ctrl+S to share</small>
        </div>
      </div>
      
      <div v-else class="output-result">
        <!-- Success Output -->
        <div v-if="output.stdout && !output.error" class="output-section success">
          <div class="section-header">
            <span class="icon">✅</span>
            <span class="label">Output</span>
            <span class="execution-time" v-if="output.executionTime">
              ({{ output.executionTime }}ms)
            </span>
          </div>
          <pre class="output-text">{{ output.stdout }}</pre>
        </div>
        
        <!-- Error Output -->
        <div v-if="output.stderr || output.error" class="output-section error">
          <div class="section-header">
            <span class="icon">❌</span>
            <span class="label">{{ output.error ? 'Error' : 'Standard Error' }}</span>
            <span class="exit-code" v-if="output.exitCode !== undefined">
              Exit Code: {{ output.exitCode }}
            </span>
          </div>
          <pre class="output-text">{{ output.error || output.stderr }}</pre>
        </div>
        
        <!-- Empty but successful execution -->
        <div v-if="!output.stdout && !output.stderr && !output.error && output.exitCode === 0" class="output-section success">
          <div class="section-header">
            <span class="icon">✅</span>
            <span class="label">Success</span>
            <span class="execution-time" v-if="output.executionTime">
              ({{ output.executionTime }}ms)
            </span>
          </div>
          <p class="no-output">Program executed successfully with no output.</p>
        </div>
      </div>
      
      <!-- Input Section -->
      <div v-if="needsInput || showInputPanel" class="input-section">
        <div class="input-header">
          <span class="icon">⌨️</span>
          <span class="label">Program Input</span>
          <button @click="clearInput" class="clear-input-btn" title="Clear Input">
            🗑️
          </button>
        </div>
        <div class="input-area">
          <textarea 
            v-model="userInput"
            placeholder="Enter input for your program here..."
            class="input-textarea"
            @keydown.ctrl.enter="sendInput"
            @keydown.meta.enter="sendInput"
          ></textarea>
          <div class="input-actions">
            <button @click="sendInput" :disabled="!userInput.trim()" class="send-input-btn">
              Send Input (Ctrl+Enter)
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OutputPanel',  props: {
    output: {
      type: Object,
      default: null
    },
    isLoading: {
      type: Boolean,
      default: false
    },
    needsInput: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      showInputPanel: false,
      userInput: ''
    }
  },  methods: {
    clearOutput() {
      this.$emit('clear-output')
    },
    toggleInputPanel() {
      this.showInputPanel = !this.showInputPanel
    },
    sendInput() {
      if (this.userInput.trim()) {
        this.$emit('send-input', this.userInput)
        this.userInput = ''
      }
    },
    clearInput() {
      this.userInput = ''
    }
  }
}
</script>

<style scoped>
.output-panel {
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #1e1e1e;
  border-left: 1px solid #3c3c3c;
}

.output-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: #252526;
  border-bottom: 1px solid #3c3c3c;
}

.output-header h3 {
  margin: 0;
  color: #d4d4d4;
  font-size: 1.1rem;
}

.output-controls {
  display: flex;
  gap: 0.5rem;
}

.btn {
  padding: 0.4rem 0.8rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-clear {
  background-color: #6c757d;
  color: white;
}

.btn-clear:hover:not(:disabled) {
  background-color: #5a6268;
}

.output-content {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
  min-height: 0; /* Allow content to shrink */
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #d4d4d4;
}

.spinner {
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

.empty-state {
  text-align: center;
  color: #858585;
  padding: 2rem;
}

.empty-state p {
  font-size: 1.1rem;
  margin-bottom: 1rem;
}

.shortcuts {
  padding: 1rem;
  background-color: #252526;
  border-radius: 8px;
  border: 1px solid #3c3c3c;
}

.output-result {
  color: #d4d4d4;
}

.output-section {
  margin-bottom: 1rem;
  border-radius: 8px;
  overflow: hidden;
}

.output-section.success {
  border: 1px solid #28a745;
}

.output-section.error {
  border: 1px solid #dc3545;
}

.section-header {
  background-color: #252526;
  padding: 0.5rem 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  font-size: 0.9rem;
}

.success .section-header {
  background-color: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.error .section-header {
  background-color: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.execution-time,
.exit-code {
  margin-left: auto;
  font-size: 0.8rem;
  opacity: 0.8;
}

.output-text {
  background-color: #1e1e1e;
  padding: 1rem;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9rem;
  line-height: 1.4;
  max-height: 300px;
  overflow-y: auto;
}

.no-output {
  padding: 1rem;
  margin: 0;
  font-style: italic;
  color: #858585;
}

.input-section {
  background-color: #252526;
  border-top: 1px solid #3c3c3c;
  border-radius: 0 0 6px 6px;
}

.input-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background-color: #2d2d30;
  border-bottom: 1px solid #3c3c3c;
}

.input-header .label {
  flex: 1;
  font-size: 0.9rem;
  font-weight: 500;
  color: #d4d4d4;
}

.clear-input-btn {
  background: none;
  border: none;
  color: #d4d4d4;
  cursor: pointer;
  padding: 0.2rem;
  border-radius: 3px;
  transition: background-color 0.2s;
}

.clear-input-btn:hover {
  background-color: #3c3c3c;
}

.input-area {
  padding: 1rem;
}

.input-textarea {
  width: 100%;
  min-height: 80px;
  background-color: #1e1e1e;
  color: #d4d4d4;
  border: 1px solid #3c3c3c;
  border-radius: 4px;
  padding: 0.5rem;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.9rem;
  resize: vertical;
}

.input-textarea:focus {
  outline: none;
  border-color: #007acc;
}

.input-actions {
  margin-top: 0.5rem;
  display: flex;
  justify-content: flex-end;
}

.send-input-btn {
  background-color: #007acc;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.2s;
}

.send-input-btn:hover:not(:disabled) {
  background-color: #005a9e;
}

.send-input-btn:disabled {
  background-color: #555;
  cursor: not-allowed;
}

.btn-input {
  background-color: #3c3c3c;
  color: #d4d4d4;
}

.btn-input.active {
  background-color: #007acc;
  color: white;
}

.btn-input:hover {
  background-color: #4a4a4a;
}

.btn-input.active:hover {
  background-color: #005a9e;
}

@media (max-width: 768px) {
  .output-panel {
    width: 100%;
    border-left: none;
    border-top: 1px solid #3c3c3c;
  }
}
</style>
