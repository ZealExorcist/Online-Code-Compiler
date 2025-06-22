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
        
        <!-- Rate Limit and User Info -->
        <div v-if="output.metadata" class="output-section info">
          <div class="section-header">
            <span class="icon">ℹ️</span>
            <span class="label">Execution Info</span>
          </div>
          <div class="metadata-info">
            <div class="metadata-row" v-if="output.metadata.authenticated !== undefined">
              <span class="metadata-label">Status:</span>
              <span class="metadata-value" :class="{ 'authenticated': output.metadata.authenticated, 'anonymous': !output.metadata.authenticated }">
                {{ output.metadata.authenticated ? '🔐 Authenticated' : '🌐 Anonymous' }}
              </span>
            </div>
            <div class="metadata-row" v-if="output.metadata.tier">
              <span class="metadata-label">Tier:</span>
              <span class="metadata-value tier-badge" :class="'tier-' + output.metadata.tier.toLowerCase()">
                {{ output.metadata.tier }}
              </span>
            </div>
            <div class="metadata-row" v-if="output.metadata.tierDescription">
              <span class="metadata-label">Plan:</span>
              <span class="metadata-value">{{ output.metadata.tierDescription }}</span>
            </div>
            <div class="metadata-row" v-if="output.metadata.remainingRequests !== undefined">
              <span class="metadata-label">Remaining Requests:</span>
              <span class="metadata-value" :class="{ 'low-requests': output.metadata.remainingRequests <= 2 }">
                {{ output.metadata.remainingRequests }} this hour
              </span>
            </div>
            <div v-if="!output.metadata.authenticated && output.metadata.remainingRequests <= 2" class="upgrade-hint">
              💡 <strong>Sign up</strong> to get higher limits and faster execution!
            </div>
          </div>
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

.output-section.info {
  border: 1px solid var(--info-color, #17a2b8);
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

.info .section-header {
  background-color: rgba(23, 162, 184, 0.1);
  color: var(--info-color, #17a2b8);
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

.metadata-info {
  padding: 1rem;
  background-color: var(--bg-secondary);
}

.metadata-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.metadata-row:last-child {
  margin-bottom: 0;
}

.metadata-label {
  color: var(--text-muted);
  font-weight: 500;
}

.metadata-value {
  color: var(--text-primary);
  font-weight: 600;
}

.metadata-value.authenticated {
  color: var(--success-color);
}

.metadata-value.anonymous {
  color: var(--warning-color, #ffc107);
}

.metadata-value.low-requests {
  color: var(--error-color);
  font-weight: bold;
}

.tier-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: bold;
  text-transform: uppercase;
}

.tier-badge.tier-anonymous {
  background-color: rgba(255, 193, 7, 0.2);
  color: var(--warning-color, #ffc107);
}

.tier-badge.tier-basic {
  background-color: rgba(108, 117, 125, 0.2);
  color: #6c757d;
}

.tier-badge.tier-advanced {
  background-color: rgba(23, 162, 184, 0.2);
  color: var(--info-color, #17a2b8);
}

.tier-badge.tier-master {
  background-color: rgba(123, 63, 228, 0.2);
  color: #7b3fe4;
}

.upgrade-hint {
  margin-top: 0.75rem;
  padding: 0.75rem;
  background-color: rgba(255, 193, 7, 0.1);
  border: 1px solid var(--warning-color, #ffc107);
  border-radius: 6px;
  color: var(--text-primary);
  font-size: 0.9rem;
  text-align: center;
}

@media (max-width: 768px) {
  .output-panel {
    width: 100%;
    border-left: none;
    border-top: 1px solid var(--border-color);
  }
}
</style>
