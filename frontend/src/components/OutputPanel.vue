<template>
  <div class="output-panel">
    <div class="output-header">
      <h3>Output</h3>
      <div class="output-controls">
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
    </div>
  </div>
</template>

<script>
export default {
  name: 'OutputPanel',
  props: {
    output: {
      type: Object,
      default: null
    },
    isLoading: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    clearOutput() {
      this.$emit('clear-output')
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
  background-color: var(--editor-bg);
  border-left: 1px solid var(--border-color);
}

.output-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background-color: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.output-header h3 {
  margin: 0;
  color: var(--text-primary);
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
  color: var(--text-primary);
}

.spinner {
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

.empty-state {
  text-align: center;
  color: var(--text-muted);
  padding: 2rem;
}

.empty-state p {
  font-size: 1.1rem;
  margin-bottom: 1rem;
}

.shortcuts {
  padding: 1rem;
  background-color: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.output-result {
  color: var(--text-primary);
}

.output-section {
  margin-bottom: 1rem;
  border-radius: 8px;
  overflow: hidden;
}

.output-section.success {
  border: 1px solid var(--success-color);
}

.output-section.error {
  border: 1px solid var(--error-color);
}

.section-header {
  background-color: var(--bg-secondary);
  padding: 0.5rem 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  font-size: 0.9rem;
}

.success .section-header {
  background-color: rgba(40, 167, 69, 0.1);
  color: var(--success-color);
}

.error .section-header {
  background-color: rgba(220, 53, 69, 0.1);
  color: var(--error-color);
}

.execution-time,
.exit-code {
  margin-left: auto;
  font-size: 0.8rem;
  opacity: 0.8;
}

.output-text {
  background-color: var(--editor-bg);
  color: var(--editor-text);
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
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .output-panel {
    width: 100%;
    border-left: none;
    border-top: 1px solid var(--border-color);
  }
}
</style>
