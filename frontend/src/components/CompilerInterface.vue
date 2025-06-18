<template>
  <div class="compiler-interface">
    <Header />
    <div class="main-content">
      <CodeEditor 
        :code="currentCode" 
        :language="selectedLanguage"
        @code-change="handleCodeChange"
        @language-change="handleLanguageChange"
        @run-code="executeCode"
        @share-code="shareCode"
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
import { executeCode, shareSnippet, getSnippet } from '../services/api'

export default {
  name: 'CompilerInterface',
  components: {
    Header,
    CodeEditor,
    OutputPanel,
    Footer
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
      executionOutput: null,
      isExecuting: false
    }
  },
  async mounted() {
    if (this.snippetId) {
      await this.loadSnippet(this.snippetId)
    }
  },
  watch: {
    snippetId(newId) {
      if (newId) {
        this.loadSnippet(newId)
      }
    }
  },
  methods: {
    handleCodeChange(newCode) {
      this.currentCode = newCode
    },
    handleLanguageChange(newLanguage) {
      this.selectedLanguage = newLanguage
      this.currentCode = this.getDefaultCode(newLanguage)
    },
    async executeCode() {
      if (!this.currentCode.trim()) {
        alert('Please enter some code to execute.')
        return
      }

      this.isExecuting = true
      this.executionOutput = null

      try {
        const result = await executeCode(this.currentCode, this.selectedLanguage)
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
    async shareCode() {
      if (!this.currentCode.trim()) {
        alert('Please enter some code to share.')
        return
      }

      try {
        const result = await shareSnippet(this.currentCode, this.selectedLanguage)
        const shareUrl = `${window.location.origin}/snippets/${result.id}`
        
        // Copy to clipboard if available
        if (navigator.clipboard) {
          await navigator.clipboard.writeText(shareUrl)
          alert(`Snippet saved! URL copied to clipboard:\n${shareUrl}`)
        } else {
          alert(`Snippet saved! Share this URL:\n${shareUrl}`)
        }
        
        // Navigate to the snippet URL
        this.$router.push(`/snippets/${result.id}`)
      } catch (error) {
        alert('Failed to share code: ' + error.message)
      }
    },
    async loadSnippet(id) {
      try {
        const snippet = await getSnippet(id)
        this.currentCode = snippet.code
        this.selectedLanguage = snippet.language
      } catch (error) {
        alert('Failed to load snippet: ' + error.message)
        // Redirect to home on error
        this.$router.push('/')
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

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
}
</style>
