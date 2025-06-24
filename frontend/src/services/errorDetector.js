// Error detection service for different programming languages
// This provides basic syntax error detection that can be extended

export class ErrorDetector {
  constructor() {
    this.errorCheckers = {
      python: this.checkPythonErrors.bind(this),
      javascript: this.checkJavaScriptErrors.bind(this),
      typescript: this.checkTypeScriptErrors.bind(this),
      java: this.checkJavaErrors.bind(this),
      cpp: this.checkCppErrors.bind(this),
      c: this.checkCppErrors.bind(this), // Use same checker as C++
      csharp: this.checkCSharpErrors.bind(this),
      go: this.checkGoErrors.bind(this),
      rust: this.checkRustErrors.bind(this),
      ruby: this.checkRubyErrors.bind(this),
      r: this.checkRErrors.bind(this)
    }
  }

  // Main method to detect errors in code
  detectErrors(code, language) {
    const checker = this.errorCheckers[language] || this.checkGenericErrors.bind(this)
    return checker(code)
  }

  // Python error detection
  checkPythonErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const lineNum = index + 1
      const trimmed = line.trim()

      // Check for indentation errors (basic)
      if (line.length > 0 && !line.startsWith(' ') && !line.startsWith('\t')) {
        // Check if line should be indented (after colons)
        if (index > 0) {
          const prevLine = lines[index - 1].trim()
          if (prevLine.endsWith(':') && trimmed && !trimmed.startsWith('#')) {
            errors.push({
              from: this.getLineStart(lines, index),
              to: this.getLineEnd(lines, index),
              severity: 'error',
              message: 'IndentationError: expected an indented block'
            })
          }
        }
      }

      // Check for unmatched parentheses, brackets, braces
      const brackets = this.checkUnmatchedBrackets(line)
      if (brackets.length > 0) {
        brackets.forEach(bracket => {
          errors.push({
            from: this.getLineStart(lines, index) + bracket.pos,
            to: this.getLineStart(lines, index) + bracket.pos + 1,
            severity: 'error',
            message: `SyntaxError: unmatched '${bracket.char}'`
          })
        })
      }

      // Check for undefined variables (basic patterns)
      if (trimmed.includes('=') && !trimmed.startsWith('#')) {
        const invalidPatterns = [
          /\b(\d+\w+)/g, // Variables starting with numbers
          /\bdef\s+(\d+\w*)/g, // Function names starting with numbers
          /\bclass\s+(\d+\w*)/g // Class names starting with numbers
        ]

        invalidPatterns.forEach(pattern => {
          const matches = [...line.matchAll(pattern)]
          matches.forEach(match => {
            errors.push({
              from: this.getLineStart(lines, index) + match.index,
              to: this.getLineStart(lines, index) + match.index + match[0].length,
              severity: 'error',
              message: `SyntaxError: invalid syntax`
            })
          })
        })
      }
    })

    return errors
  }

  // JavaScript/TypeScript error detection
  checkJavaScriptErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const lineNum = index + 1
      const trimmed = line.trim()

      // Check for unmatched brackets
      const brackets = this.checkUnmatchedBrackets(line)
      if (brackets.length > 0) {
        brackets.forEach(bracket => {
          errors.push({
            from: this.getLineStart(lines, index) + bracket.pos,
            to: this.getLineStart(lines, index) + bracket.pos + 1,
            severity: 'error',
            message: `SyntaxError: Unexpected token '${bracket.char}'`
          })
        })
      }

      // Check for missing semicolons (simple heuristic)
      if (trimmed && !trimmed.endsWith(';') && !trimmed.endsWith('{') && 
          !trimmed.endsWith('}') && !trimmed.startsWith('//') && 
          !trimmed.startsWith('if') && !trimmed.startsWith('for') && 
          !trimmed.startsWith('while') && !trimmed.startsWith('function') &&
          !trimmed.startsWith('class') && !trimmed.includes('//') &&
          (trimmed.includes('let ') || trimmed.includes('const ') || trimmed.includes('var ') ||
           trimmed.includes('return ') || trimmed.includes('= '))) {
        errors.push({
          from: this.getLineEnd(lines, index) - 1,
          to: this.getLineEnd(lines, index),
          severity: 'warning',
          message: 'Missing semicolon'
        })
      }

      // Check for undefined variables (basic)
      const undefinedVarPattern = /\b([a-zA-Z_$][a-zA-Z0-9_$]*)\s*(?=\()/g
      const matches = [...line.matchAll(undefinedVarPattern)]
      matches.forEach(match => {
        const varName = match[1]
        if (!this.isBuiltinFunction(varName) && !code.includes(`function ${varName}`) && 
            !code.includes(`const ${varName}`) && !code.includes(`let ${varName}`) && 
            !code.includes(`var ${varName}`)) {
          errors.push({
            from: this.getLineStart(lines, index) + match.index,
            to: this.getLineStart(lines, index) + match.index + varName.length,
            severity: 'warning',
            message: `'${varName}' is not defined`
          })
        }
      })
    })

    return errors
  }

  checkTypeScriptErrors(code) {
    // Use JavaScript checker as base, can be extended for TypeScript-specific features
    return this.checkJavaScriptErrors(code)
  }

  // Java error detection
  checkJavaErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Check for unmatched brackets
      const brackets = this.checkUnmatchedBrackets(line)
      if (brackets.length > 0) {
        brackets.forEach(bracket => {
          errors.push({
            from: this.getLineStart(lines, index) + bracket.pos,
            to: this.getLineStart(lines, index) + bracket.pos + 1,
            severity: 'error',
            message: `Syntax error: unmatched '${bracket.char}'`
          })
        })
      }

      // Check for missing semicolons
      if (trimmed && !trimmed.endsWith(';') && !trimmed.endsWith('{') && 
          !trimmed.endsWith('}') && !trimmed.startsWith('//') && 
          !trimmed.startsWith('import') && !trimmed.startsWith('package') &&
          !trimmed.includes('if ') && !trimmed.includes('for ') && 
          !trimmed.includes('while ') && !trimmed.includes('class ') &&
          (trimmed.includes('= ') || trimmed.includes('return ') || 
           trimmed.includes('System.') || trimmed.includes('new '))) {
        errors.push({
          from: this.getLineEnd(lines, index) - 1,
          to: this.getLineEnd(lines, index),
          severity: 'error',
          message: 'Missing semicolon'
        })
      }
    })

    return errors
  }

  // C/C++ error detection
  checkCppErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Check for unmatched brackets
      const brackets = this.checkUnmatchedBrackets(line)
      if (brackets.length > 0) {
        brackets.forEach(bracket => {
          errors.push({
            from: this.getLineStart(lines, index) + bracket.pos,
            to: this.getLineStart(lines, index) + bracket.pos + 1,
            severity: 'error',
            message: `Syntax error: unmatched '${bracket.char}'`
          })
        })
      }

      // Check for missing semicolons
      if (trimmed && !trimmed.endsWith(';') && !trimmed.endsWith('{') && 
          !trimmed.endsWith('}') && !trimmed.startsWith('//') && 
          !trimmed.startsWith('#') && !trimmed.includes('if ') && 
          !trimmed.includes('for ') && !trimmed.includes('while ') &&
          (trimmed.includes('= ') || trimmed.includes('return ') || 
           trimmed.includes('cout') || trimmed.includes('printf'))) {
        errors.push({
          from: this.getLineEnd(lines, index) - 1,
          to: this.getLineEnd(lines, index),
          severity: 'error',
          message: 'Missing semicolon'
        })
      }
    })

    return errors
  }

  // C# error detection
  checkCSharpErrors(code) {
    // Similar to Java, with some C#-specific additions
    return this.checkJavaErrors(code)
  }

  // Go error detection
  checkGoErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Check for unmatched brackets
      const brackets = this.checkUnmatchedBrackets(line)
      if (brackets.length > 0) {
        brackets.forEach(bracket => {
          errors.push({
            from: this.getLineStart(lines, index) + bracket.pos,
            to: this.getLineStart(lines, index) + bracket.pos + 1,
            severity: 'error',
            message: `Syntax error: unmatched '${bracket.char}'`
          })
        })
      }
    })

    return errors
  }

  // Rust error detection
  checkRustErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Check for unmatched brackets
      const brackets = this.checkUnmatchedBrackets(line)
      if (brackets.length > 0) {
        brackets.forEach(bracket => {
          errors.push({
            from: this.getLineStart(lines, index) + bracket.pos,
            to: this.getLineStart(lines, index) + bracket.pos + 1,
            severity: 'error',
            message: `Syntax error: unmatched '${bracket.char}'`
          })
        })
      }
    })

    return errors
  }

  // Ruby error detection
  checkRubyErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Check for unmatched brackets
      const brackets = this.checkUnmatchedBrackets(line)
      if (brackets.length > 0) {
        brackets.forEach(bracket => {
          errors.push({
            from: this.getLineStart(lines, index) + bracket.pos,
            to: this.getLineStart(lines, index) + bracket.pos + 1,
            severity: 'error',
            message: `Syntax error: unmatched '${bracket.char}'`
          })
        })
      }
    })

    return errors
  }

  // R error detection
  checkRErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Check for unmatched brackets
      const brackets = this.checkUnmatchedBrackets(line)
      if (brackets.length > 0) {
        brackets.forEach(bracket => {
          errors.push({
            from: this.getLineStart(lines, index) + bracket.pos,
            to: this.getLineStart(lines, index) + bracket.pos + 1,
            severity: 'error',
            message: `Syntax error: unmatched '${bracket.char}'`
          })
        })
      }
    })

    return errors
  }

  // Generic error detection for unsupported languages
  checkGenericErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      // Only check for unmatched brackets for unknown languages
      const brackets = this.checkUnmatchedBrackets(line)
      if (brackets.length > 0) {
        brackets.forEach(bracket => {
          errors.push({
            from: this.getLineStart(lines, index) + bracket.pos,
            to: this.getLineStart(lines, index) + bracket.pos + 1,
            severity: 'warning',
            message: `Unmatched '${bracket.char}'`
          })
        })
      }
    })

    return errors
  }

  // Helper method to check for unmatched brackets
  checkUnmatchedBrackets(line) {
    const brackets = []
    const stack = []
    const pairs = { '(': ')', '[': ']', '{': '}' }
    const openBrackets = Object.keys(pairs)
    const closeBrackets = Object.values(pairs)

    for (let i = 0; i < line.length; i++) {
      const char = line[i]
      
      if (openBrackets.includes(char)) {
        stack.push({ char, pos: i })
      } else if (closeBrackets.includes(char)) {
        if (stack.length === 0) {
          brackets.push({ char, pos: i })
        } else {
          const last = stack.pop()
          if (pairs[last.char] !== char) {
            brackets.push({ char, pos: i })
          }
        }
      }
    }

    // Add unmatched opening brackets
    stack.forEach(item => {
      brackets.push(item)
    })

    return brackets
  }

  // Helper to get line start position
  getLineStart(lines, lineIndex) {
    let pos = 0
    for (let i = 0; i < lineIndex; i++) {
      pos += lines[i].length + 1 // +1 for newline
    }
    return pos
  }

  // Helper to get line end position
  getLineEnd(lines, lineIndex) {
    return this.getLineStart(lines, lineIndex) + lines[lineIndex].length
  }

  // Check if a function name is a built-in function
  isBuiltinFunction(name) {
    const builtins = [
      'console', 'alert', 'confirm', 'prompt', 'setTimeout', 'setInterval',
      'parseInt', 'parseFloat', 'isNaN', 'isFinite', 'encodeURIComponent',
      'decodeURIComponent', 'Math', 'Date', 'Array', 'Object', 'String',
      'Number', 'Boolean', 'RegExp', 'Error', 'JSON'
    ]
    return builtins.includes(name)
  }
}

// Export singleton instance
export const errorDetector = new ErrorDetector()
