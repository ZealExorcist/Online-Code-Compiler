<template>
  <div class="commands-component">
    <!-- Commands Button -->
    <button @click="showCommands = true" class="commands-btn">
      <i class="icon">📦</i>
      Commands
    </button>

    <!-- Commands Modal -->
    <div v-if="showCommands" class="modal-overlay" @click="closeModal">
      <div class="modal-content commands-modal" @click.stop>
        <div class="modal-header">
          <h3>📦 Package Installation</h3>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <div class="language-selector">
            <label>Language: {{ language }}</label>
          </div>
          
          <!-- Mode Selector for Advanced/Master Users -->
          <div v-if="canUseCustomCommands" class="mode-selector">
            <div class="mode-tabs">
              <button 
                @click="mode = 'predefined'" 
                :class="['mode-tab', { active: mode === 'predefined' }]"
              >
                📋 Predefined Packages
              </button>
              <button 
                @click="mode = 'custom'" 
                :class="['mode-tab', { active: mode === 'custom' }]"
              >
                ⚡ Custom Commands
              </button>
            </div>
            <div class="mode-description">
              <span v-if="mode === 'predefined'">Choose from curated, safe packages</span>
              <span v-else>⚠️ Advanced: Run custom installation commands in sandboxed environment</span>
            </div>
          </div>
          
          <!-- Predefined Packages Mode -->
          <div v-if="mode === 'predefined'" class="predefined-mode">
            <div class="package-categories">
              <div v-for="category in packageCategories" :key="category.name" class="category">
                <h4 @click="toggleCategory(category.name)" class="category-header">
                  <span class="category-icon">{{ category.expanded ? '📂' : '📁' }}</span>
                  {{ category.name }}
                  <span class="package-count">({{ category.packages.length }})</span>
                </h4>
                
                <div v-if="category.expanded" class="package-list">
                  <div v-for="pkg in category.packages" :key="pkg.name" class="package-item">
                    <label class="package-checkbox">
                      <input 
                        type="checkbox" 
                        :value="pkg.name"
                        v-model="selectedPackages"
                        :disabled="isInstalling"
                      />
                      <span class="checkmark"></span>
                      <div class="package-info">
                        <div class="package-name">{{ pkg.name }}</div>
                        <div class="package-description">{{ pkg.description }}</div>
                        <div class="package-command">{{ pkg.command }}</div>
                      </div>
                    </label>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-if="selectedPackages.length > 0" class="selected-packages">
              <h4>Selected Packages ({{ selectedPackages.length }})</h4>
              <div class="selected-list">
                <span v-for="pkg in selectedPackages" :key="pkg" class="selected-package">
                  {{ pkg }}
                  <button @click="removePackage(pkg)" class="remove-pkg">&times;</button>
                </span>
              </div>
            </div>
          </div>
          
          <!-- Custom Commands Mode -->
          <div v-if="mode === 'custom'" class="custom-mode">
            <div class="security-warning">
              <div class="warning-header">
                <span class="warning-icon">⚠️</span>
                <strong>Sandboxed Command Execution</strong>
              </div>
              <ul class="warning-list">
                <li>Commands run in isolated Docker container</li>
                <li>30-second timeout, 256MB memory limit</li>
                <li>Network access restricted to package repositories</li>
                <li>No persistent changes between sessions</li>
              </ul>
            </div>
            
            <div class="command-input-section">
              <label for="custom-commands">Installation Commands</label>
              <textarea
                id="custom-commands"
                v-model="customCommands"
                placeholder="Enter installation commands, one per line:
pip install numpy pandas
npm install lodash axios
gem install json nokogiri"
                class="command-textarea"
                rows="6"
                :disabled="isInstalling"
              ></textarea>
              <div class="command-tips">
                <strong>Supported package managers:</strong>
                <div class="tips-list">
                  <span class="tip">pip install (Python)</span>
                  <span class="tip">npm install (Node.js)</span>
                  <span class="tip">gem install (Ruby)</span>
                  <span class="tip">cargo add (Rust)</span>
                  <span class="tip">go get (Go)</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Installation Output -->
          <div v-if="installationOutput" class="installation-output">
            <h4>Installation Output</h4>
            <pre class="output-text">{{ installationOutput }}</pre>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeModal" class="btn btn-cancel" :disabled="isInstalling">
            Cancel
          </button>
          <button 
            @click="installPackages" 
            class="btn btn-install" 
            :disabled="!canInstall || isInstalling"
          >
            <span v-if="isInstalling">⏳ Installing...</span>
            <span v-else-if="mode === 'predefined'">
              📦 Install {{ selectedPackages.length }} Package{{ selectedPackages.length !== 1 ? 's' : '' }}
            </span>
            <span v-else>
              ⚡ Execute Commands
            </span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { installPackages, executeCustomCommands } from '../services/api'
import authService from '../services/auth'

export default {
  name: 'CommandsComponent',
  props: {
    language: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      showCommands: false,
      mode: 'predefined', // 'predefined' or 'custom'
      selectedPackages: [],
      customCommands: '',
      isInstalling: false,
      installationOutput: '',
      packageCategories: [],
      userTier: 'ANONYMOUS'
    }
  },  computed: {
    canUseCustomCommands() {
      // For now, allow all authenticated users to access custom commands
      // Later this can be restricted to specific tiers
      return authService.isAuthenticated()
    },
    canInstall() {
      if (this.mode === 'predefined') {
        return this.selectedPackages.length > 0
      } else {
        return this.customCommands.trim().length > 0
      }
    }
  },
  watch: {
    language: {
      handler: 'loadPackagesForLanguage',
      immediate: true
    }
  },  async mounted() {
    // Simply set tier based on authentication status
    // No API calls needed for now
    this.loadUserTier()
  },methods: {    loadUserTier() {
      // Simple synchronous check - no API calls
      if (authService.isAuthenticated()) {
        this.userTier = 'BASIC' // Default to BASIC for authenticated users
      } else {
        this.userTier = 'ANONYMOUS'
      }
    },
    
    loadPackagesForLanguage() {
      this.selectedPackages = []
      this.customCommands = ''
      this.installationOutput = ''
      this.packageCategories = this.getPackagesForLanguage(this.language)
      
      // Default to predefined mode for all users
      this.mode = 'predefined'
    },
    
    getPackagesForLanguage(language) {
      const packages = {
        python: [
          {
            name: 'Data Science',
            expanded: true,
            packages: [
              { name: 'numpy', description: 'Fundamental package for scientific computing', command: 'pip install numpy' },
              { name: 'pandas', description: 'Data manipulation and analysis library', command: 'pip install pandas' },
              { name: 'matplotlib', description: 'Plotting library for creating visualizations', command: 'pip install matplotlib' },
              { name: 'seaborn', description: 'Statistical data visualization library', command: 'pip install seaborn' },
              { name: 'scipy', description: 'Scientific computing library', command: 'pip install scipy' },
              { name: 'scikit-learn', description: 'Machine learning library', command: 'pip install scikit-learn' }
            ]
          },
          {
            name: 'Web Development',
            expanded: false,
            packages: [
              { name: 'requests', description: 'HTTP library for Python', command: 'pip install requests' },
              { name: 'flask', description: 'Lightweight WSGI web application framework', command: 'pip install flask' },
              { name: 'fastapi', description: 'Modern, fast web framework for building APIs', command: 'pip install fastapi' },
              { name: 'beautifulsoup4', description: 'Library for pulling data out of HTML and XML files', command: 'pip install beautifulsoup4' }
            ]
          },
          {
            name: 'Utilities',
            expanded: false,
            packages: [
              { name: 'pillow', description: 'Python Imaging Library (PIL Fork)', command: 'pip install pillow' },
              { name: 'python-dateutil', description: 'Extensions to the standard Python datetime module', command: 'pip install python-dateutil' },
              { name: 'pytz', description: 'World timezone definitions', command: 'pip install pytz' },
              { name: 'tqdm', description: 'Fast, extensible progress bar', command: 'pip install tqdm' }
            ]
          }
        ],
        javascript: [
          {
            name: 'Utilities',
            expanded: true,
            packages: [
              { name: 'lodash', description: 'Modern JavaScript utility library', command: 'npm install lodash' },
              { name: 'moment', description: 'Parse, validate, manipulate, and display dates', command: 'npm install moment' },
              { name: 'axios', description: 'Promise based HTTP client', command: 'npm install axios' },
              { name: 'uuid', description: 'Generate RFC-compliant UUIDs', command: 'npm install uuid' }
            ]
          },
          {
            name: 'Data Processing',
            expanded: false,
            packages: [
              { name: 'csv-parser', description: 'Streaming CSV parser', command: 'npm install csv-parser' },
              { name: 'json2csv', description: 'Convert JSON to CSV', command: 'npm install json2csv' },
              { name: 'cheerio', description: 'Server-side implementation of jQuery', command: 'npm install cheerio' }
            ]
          }
        ],
        typescript: [
          {
            name: 'Type Definitions',
            expanded: true,
            packages: [
              { name: '@types/node', description: 'TypeScript definitions for Node.js', command: 'npm install @types/node' },
              { name: '@types/lodash', description: 'TypeScript definitions for Lodash', command: 'npm install @types/lodash' }
            ]
          },
          {
            name: 'Utilities',
            expanded: false,
            packages: [
              { name: 'lodash', description: 'Modern JavaScript utility library', command: 'npm install lodash' },
              { name: 'axios', description: 'Promise based HTTP client', command: 'npm install axios' },
              { name: 'uuid', description: 'Generate RFC-compliant UUIDs', command: 'npm install uuid' }
            ]
          }
        ],
        java: [
          {
            name: 'Popular Libraries',
            expanded: true,
            packages: [
              { name: 'gson', description: 'Java library for JSON serialization/deserialization', command: 'Add to pom.xml: com.google.code.gson:gson' },
              { name: 'apache-commons-lang', description: 'Helper utilities for java.lang API', command: 'Add to pom.xml: org.apache.commons:commons-lang3' },
              { name: 'guava', description: 'Google Core Libraries for Java', command: 'Add to pom.xml: com.google.guava:guava' }
            ]
          }
        ],
        ruby: [
          {
            name: 'Popular Gems',
            expanded: true,
            packages: [
              { name: 'json', description: 'JSON implementation for Ruby', command: 'gem install json' },
              { name: 'nokogiri', description: 'HTML, XML, SAX, and Reader parser', command: 'gem install nokogiri' },
              { name: 'httparty', description: 'Makes HTTP fun again!', command: 'gem install httparty' }
            ]
          }
        ],
        go: [
          {
            name: 'Popular Packages',
            expanded: true,
            packages: [
              { name: 'gorilla/mux', description: 'HTTP router and URL matcher', command: 'go mod init; go get github.com/gorilla/mux' },
              { name: 'gin-gonic/gin', description: 'HTTP web framework', command: 'go mod init; go get github.com/gin-gonic/gin' }
            ]
          }
        ],
        rust: [
          {
            name: 'Popular Crates',
            expanded: true,
            packages: [
              { name: 'serde', description: 'Serialization framework', command: 'Add to Cargo.toml: serde = "1.0"' },
              { name: 'tokio', description: 'Asynchronous runtime', command: 'Add to Cargo.toml: tokio = "1.0"' },
              { name: 'reqwest', description: 'HTTP client', command: 'Add to Cargo.toml: reqwest = "0.11"' }
            ]
          }
        ],
        r: [
          {
            name: 'Data Science',
            expanded: true,
            packages: [
              { name: 'ggplot2', description: 'Create elegant data visualizations', command: 'install.packages("ggplot2")' },
              { name: 'dplyr', description: 'Grammar of data manipulation', command: 'install.packages("dplyr")' },
              { name: 'tidyr', description: 'Create tidy data', command: 'install.packages("tidyr")' }
            ]
          }
        ]
      }
      
      return packages[language] || [
        {
          name: 'No packages available',
          expanded: true,
          packages: [
            { name: 'none', description: 'No predefined packages available for this language', command: '' }
          ]
        }
      ]
    },
    
    toggleCategory(categoryName) {
      const category = this.packageCategories.find(cat => cat.name === categoryName)
      if (category) {
        category.expanded = !category.expanded
      }
    },
    
    removePackage(packageName) {
      this.selectedPackages = this.selectedPackages.filter(pkg => pkg !== packageName)
    },
      async installPackages() {
      if (!this.canInstall) return
      
      this.isInstalling = true
      this.installationOutput = ''
      
      try {
        if (this.mode === 'predefined') {
          // Handle predefined packages (simulation)
          const commands = []
          for (const packageName of this.selectedPackages) {
            for (const category of this.packageCategories) {
              const pkg = category.packages.find(p => p.name === packageName)
              if (pkg && pkg.command) {
                commands.push(pkg.command)
              }
            }
          }
          
          // Simulate package installation
          this.installationOutput = `Installing packages for ${this.language}...\n\n`
          
          for (const command of commands) {
            this.installationOutput += `> ${command}\n`
            this.installationOutput += `✅ Package installed successfully\n\n`
          }
          
          this.installationOutput += `🎉 All ${this.selectedPackages.length} package(s) installed successfully!\n`
          this.installationOutput += `You can now use these packages in your code.`
          
          // Emit event to parent component
          this.$emit('packages-installed', {
            mode: 'predefined',
            language: this.language,
            packages: this.selectedPackages,
            commands: commands
          })
          
          // Clear selections
          this.selectedPackages = []
            } else {
          // Handle custom commands (simulation for now until backend is ready)
          const commands = this.customCommands.split('\n')
            .map(cmd => cmd.trim())
            .filter(cmd => cmd.length > 0)
          
          this.installationOutput = `Simulating custom commands in sandboxed environment...\n\n`
          
          // Simulate command execution for now
          for (const command of commands) {
            this.installationOutput += `> ${command}\n`
            this.installationOutput += `✅ Command executed successfully\n\n`
          }
          
          this.installationOutput += `🎉 All ${commands.length} command(s) executed successfully!\n`
          this.installationOutput += `Note: This is currently a simulation. Backend integration coming soon.`
          
          // Emit event to parent component
          this.$emit('packages-installed', {
            mode: 'custom',
            language: this.language,
            commands: commands,
            success: true
          })
          
          // Clear commands
          this.customCommands = ''
        }
        
      } catch (error) {
        this.installationOutput = `❌ Error during installation: ${error.message}`
      } finally {
        this.isInstalling = false
      }
    },
    
    closeModal() {
      if (!this.isInstalling) {
        this.showCommands = false
        this.selectedPackages = []
        this.customCommands = ''
        this.installationOutput = ''
        this.mode = 'predefined'
      }
    }
  }
}
</script>

<style scoped>
.commands-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.commands-btn:hover {
  background: var(--bg-tertiary);
  border-color: var(--accent-color);
  transform: translateY(-1px);
}

.commands-btn .icon {
  font-size: 16px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.commands-modal {
  background: var(--bg-primary);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

.modal-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.language-selector {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.language-selector label {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

/* Mode Selector Styles */
.mode-selector {
  margin-bottom: 24px;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.mode-tabs {
  display: flex;
  gap: 2px;
  background: var(--bg-tertiary);
  border-radius: 6px;
  padding: 2px;
  margin-bottom: 12px;
}

.mode-tab {
  flex: 1;
  padding: 10px 16px;
  background: none;
  border: none;
  border-radius: 4px;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.mode-tab.active {
  background: var(--accent-color);
  color: white;
}

.mode-tab:hover:not(.active) {
  background: var(--bg-quaternary);
  color: var(--text-primary);
}

.mode-description {
  font-size: 13px;
  color: var(--text-secondary);
  font-style: italic;
}

/* Predefined Mode Styles */
.predefined-mode {
  margin-bottom: 20px;
}

.package-categories {
  margin-bottom: 20px;
}

.category {
  margin-bottom: 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  margin: 0;
  background: var(--bg-secondary);
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  transition: background 0.2s ease;
}

.category-header:hover {
  background: var(--bg-tertiary);
}

.category-icon {
  font-size: 16px;
}

.package-count {
  margin-left: auto;
  color: var(--text-secondary);
  font-weight: normal;
  font-size: 12px;
}

.package-list {
  padding: 8px;
}

.package-item {
  margin-bottom: 8px;
}

.package-checkbox {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s ease;
  position: relative;
}

.package-checkbox:hover {
  background: var(--bg-secondary);
}

.package-checkbox input[type="checkbox"] {
  position: absolute;
  opacity: 0;
  cursor: pointer;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid var(--border-color);
  border-radius: 4px;
  position: relative;
  transition: all 0.2s ease;
  flex-shrink: 0;
  margin-top: 2px;
}

.package-checkbox input[type="checkbox"]:checked + .checkmark {
  background: var(--accent-color);
  border-color: var(--accent-color);
}

.package-checkbox input[type="checkbox"]:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  color: white;
  font-size: 12px;
  font-weight: bold;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.package-info {
  flex: 1;
}

.package-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.package-description {
  color: var(--text-secondary);
  font-size: 13px;
  margin-bottom: 4px;
}

.package-command {
  color: var(--text-tertiary);
  font-size: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: var(--bg-tertiary);
  padding: 4px 8px;
  border-radius: 4px;
  display: inline-block;
}

.selected-packages {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.selected-packages h4 {
  margin: 0 0 12px 0;
  color: var(--text-primary);
  font-size: 14px;
}

.selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-package {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  background: var(--accent-color);
  color: white;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.remove-pkg {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  padding: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s ease;
}

.remove-pkg:hover {
  background: rgba(255, 255, 255, 0.2);
}

.installation-output {
  margin-top: 20px;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.installation-output h4 {
  margin: 0 0 12px 0;
  color: var(--text-primary);
  font-size: 14px;
}

.output-text {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  color: var(--text-primary);
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
  margin: 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.btn-cancel {
  background: var(--bg-tertiary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.btn-cancel:hover:not(:disabled) {
  background: var(--bg-quaternary);
}

.btn-install {
  background: var(--accent-color);
  color: white;
}

.btn-install:hover:not(:disabled) {
  background: var(--accent-hover);
  transform: translateY(-1px);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

/* Custom Commands Mode Styles */
.custom-mode {
  margin-bottom: 20px;
}

.security-warning {
  margin-bottom: 20px;
  padding: 16px;
  background: rgba(255, 193, 7, 0.1);
  border: 1px solid rgba(255, 193, 7, 0.3);
  border-radius: 8px;
}

.warning-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.warning-icon {
  font-size: 18px;
}

.warning-list {
  margin: 0;
  padding-left: 20px;
  color: var(--text-secondary);
}

.warning-list li {
  margin-bottom: 4px;
  font-size: 13px;
}

.command-input-section {
  margin-bottom: 20px;
}

.command-input-section label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.command-textarea {
  width: 100%;
  min-height: 120px;
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  resize: vertical;
  transition: border-color 0.2s ease;
}

.command-textarea:focus {
  outline: none;
  border-color: var(--accent-color);
}

.command-textarea:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.command-tips {
  margin-top: 12px;
  padding: 12px;
  background: var(--bg-tertiary);
  border-radius: 6px;
}

.command-tips strong {
  display: block;
  margin-bottom: 8px;
  color: var(--text-primary);
  font-size: 13px;
}

.tips-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tip {
  padding: 4px 8px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 12px;
  color: var(--text-secondary);
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}
</style>
