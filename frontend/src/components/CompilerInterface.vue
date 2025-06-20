<template>
  <div class="compiler-interface">
    <Header />
    
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
      <div class="right-actions">
        <ShareComponent 
          :code="currentCode" 
          :language="selectedLanguage" 
          :input="currentInput"
        />
      </div>
    </div>
    
    <div class="main-content">
      <CodeEditor 
        :code="currentCode" 
        :language="selectedLanguage"
        @code-change="handleCodeChange"
        @language-change="handleLanguageChange"
        @run-code="executeCode"
        :isLoading="isExecuting"
      />
      <OutputPanel 
        :output="executionOutput"
        :isLoading="isExecuting"
        @clear-output="clearOutput"
      />
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
import { executeCode, loadSharedCode } from '../services/api'

export default {
  name: 'CompilerInterface',
  components: {
    Header,
    CodeEditor,
    OutputPanel,
    Footer,
    ShareComponent,
    LoadComponent
  },
  props: {
    snippetId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      currentCode: `print("Hello, World!")`,
      selectedLanguage: 'python',
      currentInput: '',
      executionOutput: null,
      isExecuting: false,
      isLoadingSnippet: false
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
    }
  }
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
.right-actions {
  display: flex;
  gap: 10px;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    gap: 10px;
  }
  
  .left-actions,
  .right-actions {
    width: 100%;
    justify-content: center;
  }
  
  .main-content {
    flex-direction: column;
  }
}
</style>
