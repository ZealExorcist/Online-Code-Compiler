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
    
    // Use block-based checking for brace-based languages
    if (this.isBlockBasedLanguage(language)) {
      return this.checkBlockBasedErrors(code, language)
    } else {
      return checker(code)
    }
  }
  
  // Check if language is block-based (uses braces for structure)
  isBlockBasedLanguage(language) {
    const blockBasedLanguages = ['java', 'javascript', 'typescript', 'cpp', 'c', 'csharp', 'go', 'rust']
    return blockBasedLanguages.includes(language)
  }
  
  // Block-based error checking for languages that use braces
  checkBlockBasedErrors(code, language) {
    const errors = []
    
    // Check overall bracket balance across the entire code
    const bracketErrors = this.checkGlobalBracketBalance(code)
    errors.push(...bracketErrors)
    
    // Then do line-by-line checks for other issues
    const lineErrors = this.errorCheckers[language] ? this.errorCheckers[language](code) : []
    
    // Filter out individual line bracket errors since we handle them globally
    const filteredLineErrors = lineErrors.filter(error => 
      !error.message.includes('unmatched') || !error.message.includes("'"))
    
    errors.push(...filteredLineErrors)
    
    return errors
  }
  
  // Global bracket balance checking for block-based languages
  checkGlobalBracketBalance(code) {
    const errors = []
    const lines = code.split('\n')
    const bracketStack = []
    const pairs = { '(': ')', '[': ']', '{': '}' }
    const openBrackets = Object.keys(pairs)
    const closeBrackets = Object.values(pairs)
    
    let inString = false
    let inComment = false
    let inMultilineComment = false
    let stringChar = ''
    let globalPos = 0
    
    for (let lineIndex = 0; lineIndex < lines.length; lineIndex++) {
      const line = lines[lineIndex]
      const lineStart = globalPos
      
      for (let i = 0; i < line.length; i++) {
        const char = line[i]
        const nextChar = i < line.length - 1 ? line[i + 1] : ''
        const prevChar = i > 0 ? line[i - 1] : ''
        
        // Handle multi-line comments
        if (!inString && !inComment && char === '/' && nextChar === '*') {
          inMultilineComment = true
          i++ // Skip the *
          continue
        }
        if (inMultilineComment && char === '*' && nextChar === '/') {
          inMultilineComment = false
          i++ // Skip the /
          continue
        }
        
        if (inMultilineComment) {
          continue
        }
        
        // Handle single-line comments
        if (!inString && char === '/' && nextChar === '/') {
          inComment = true
          break // Rest of line is comment
        }
        
        if (inComment) {
          break
        }
        
        // Handle strings
        if (!inString && (char === '"' || char === "'" || char === '`')) {
          inString = true
          stringChar = char
          continue
        } else if (inString && char === stringChar && prevChar !== '\\') {
          inString = false
          stringChar = ''
          continue
        }
        
        if (inString) {
          continue
        }
        
        // Check brackets
        if (openBrackets.includes(char)) {
          bracketStack.push({ 
            char, 
            pos: globalPos + i, 
            line: lineIndex,
            col: i
          })
        } else if (closeBrackets.includes(char)) {
          if (bracketStack.length === 0) {
            errors.push({
              from: globalPos + i,
              to: globalPos + i + 1,
              severity: 'error',
              message: `Unexpected closing '${char}'`
            })
          } else {
            const last = bracketStack.pop()
            if (pairs[last.char] !== char) {
              errors.push({
                from: globalPos + i,
                to: globalPos + i + 1,
                severity: 'error',
                message: `Expected '${pairs[last.char]}' but found '${char}'`
              })
            }
          }
        }
      }
      
      // Reset comment state at end of line
      inComment = false
      globalPos += line.length + 1 // +1 for newline
    }
    
    // Report unclosed brackets
    bracketStack.forEach(bracket => {
      errors.push({
        from: bracket.pos,
        to: bracket.pos + 1,
        severity: 'error',
        message: `Unclosed '${bracket.char}'`
      })
    })
    
    return errors
  }
  // Python error detection (improved)
  checkPythonErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const lineNum = index + 1
      const trimmed = line.trim()

      // Skip empty lines and comments
      if (!trimmed || trimmed.startsWith('#')) {
        return
      }

      // Check for basic indentation errors (more conservative)
      if (this.shouldCheckPythonIndentation(line, lines, index)) {
        errors.push({
          from: this.getLineStart(lines, index),
          to: this.getLineEnd(lines, index),
          severity: 'error',
          message: 'IndentationError: expected an indented block'
        })
      }

      // Check for unmatched parentheses, brackets, braces (improved)
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

      // Check for obvious syntax errors (more conservative)
      if (this.hasObviousPythonSyntaxError(trimmed)) {
        const match = trimmed.match(/(\d+\w+)/)
        if (match) {
          const pos = line.indexOf(match[1])
          errors.push({
            from: this.getLineStart(lines, index) + pos,
            to: this.getLineStart(lines, index) + pos + match[1].length,
            severity: 'error',
            message: `SyntaxError: invalid syntax`
          })
        }
      }
    })

    return errors
  }
  
  // Improved Python indentation checking
  shouldCheckPythonIndentation(line, lines, index) {
    if (index === 0) return false
    
    const currentLine = line.trim()
    const prevLine = lines[index - 1].trim()
    
    // Only check if previous line ends with colon and current line has content
    if (!prevLine.endsWith(':') || !currentLine || currentLine.startsWith('#')) {
      return false
    }
    
    // Check if current line should be indented
    const currentIndent = line.length - line.trimStart().length
    const prevIndent = lines[index - 1].length - lines[index - 1].trimStart().length
    
    // Only flag if there's clearly no indentation when there should be
    return currentIndent <= prevIndent && !currentLine.startsWith('class ') && 
           !currentLine.startsWith('def ') && !currentLine.startsWith('if ') &&
           !currentLine.startsWith('elif ') && !currentLine.startsWith('else:') &&
           !currentLine.startsWith('except ') && !currentLine.startsWith('finally:')
  }
  
  // Check for obvious Python syntax errors
  hasObviousPythonSyntaxError(trimmed) {
    // Only flag very obvious errors
    return trimmed.match(/^\s*\d+\w+/) && !trimmed.includes('#')  // Variables starting with numbers
  }
  // JavaScript/TypeScript error detection (block-based)
  checkJavaScriptErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const lineNum = index + 1
      const trimmed = line.trim()

      // Skip empty lines and comments
      if (!trimmed || trimmed.startsWith('//') || trimmed.startsWith('/*') || trimmed.startsWith('*')) {
        return
      }

      // Note: Bracket checking is handled globally for block-based languages// Check for missing semicolons (improved heuristic)
      if (this.shouldCheckSemicolon(trimmed, lines, index)) {
        errors.push({
          from: this.getLineEnd(lines, index) - 1,
          to: this.getLineEnd(lines, index),
          severity: 'warning',
          message: 'Missing semicolon'
        })
      }      // Check for undefined variables (more conservative)
      if (this.shouldCheckUndefinedVars(trimmed)) {
        const undefinedVarPattern = /\b([a-zA-Z_$][a-zA-Z0-9_$]*)\s*(?=\()/g
        const matches = [...line.matchAll(undefinedVarPattern)]
        matches.forEach(match => {
          const varName = match[1]
          if (this.isLikelyUndefinedFunction(varName, code)) {
            errors.push({
              from: this.getLineStart(lines, index) + match.index,
              to: this.getLineStart(lines, index) + match.index + varName.length,
              severity: 'info', // Downgrade to info instead of warning
              message: `'${varName}' might not be defined`
            })
          }
        })
      }
    })

    return errors
  }
  checkTypeScriptErrors(code) {
    // TypeScript is block-based, so use JavaScript checker without bracket checking
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Skip empty lines and comments
      if (!trimmed || trimmed.startsWith('//') || trimmed.includes('//')) {
        return
      }

      // Only check semicolons and undefined variables (brackets handled globally)
      if (this.shouldCheckSemicolon(trimmed, lines, index)) {
        errors.push({
          from: this.getLineEnd(lines, index) - 1,
          to: this.getLineEnd(lines, index),
          severity: 'warning',
          message: 'Missing semicolon'
        })
      }

      // Check for undefined variables (more conservative)
      if (this.shouldCheckUndefinedVars(trimmed)) {
        const undefinedVarPattern = /\b([a-zA-Z_$][a-zA-Z0-9_$]*)\s*(?=\()/g
        const matches = [...line.matchAll(undefinedVarPattern)]
        matches.forEach(match => {
          const varName = match[1]
          if (this.isLikelyUndefinedFunction(varName, code)) {
            errors.push({
              from: this.getLineStart(lines, index) + match.index,
              to: this.getLineStart(lines, index) + match.index + varName.length,
              severity: 'info',
              message: `'${varName}' might not be defined`
            })
          }
        })
      }
    })

    return errors
  }
  // Java error detection (improved)
  checkJavaErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()      // Skip empty lines and comments
      if (!trimmed || trimmed.startsWith('//') || trimmed.startsWith('/*') || trimmed.startsWith('*')) {
        return
      }

      // Note: Bracket checking is handled globally for block-based languages

      // Improved semicolon checking for Java
      if (this.shouldCheckJavaSemicolon(trimmed, lines, index)) {
        errors.push({
          from: this.getLineEnd(lines, index) - 1,
          to: this.getLineEnd(lines, index),
          severity: 'warning',
          message: 'Missing semicolon'
        })
      }
    })

    return errors
  }
  
  // Improved Java semicolon checking
  shouldCheckJavaSemicolon(trimmed, lines, index) {
    if (!trimmed || trimmed.length === 0) return false
    
    // Don't check if line already ends with proper punctuation
    if (trimmed.endsWith(';') || trimmed.endsWith('{') || trimmed.endsWith('}') || 
        trimmed.startsWith('//') || trimmed.startsWith('import') || 
        trimmed.startsWith('package') || trimmed.startsWith('@')) {
      return false
    }
    
    // Don't check control structures and declarations
    if (trimmed.match(/^\s*(if|for|while|class|interface|enum|try|catch|else|switch|case|default|public|private|protected|static|final)\b/)) {
      return false
    }
    
    // Don't check method signatures
    if (trimmed.includes('(') && trimmed.includes(')') && !trimmed.includes('=') && !trimmed.includes('new ')) {
      return false
    }
    
    // Only check lines that clearly should have semicolons in Java
    const shouldHaveSemicolon = 
      trimmed.match(/^\s*\w+\s+\w+\s*=/) ||           // Variable declarations with assignment
      trimmed.match(/^\s*return\s+/) ||               // Return statements
      trimmed.match(/^\s*\w+\s*=/) ||                 // Assignments
      trimmed.match(/^\s*System\./) ||                // System calls
      trimmed.match(/^\s*throw\s+/) ||                // Throw statements
      trimmed.match(/^\s*\w+\.\w+\s*\([^)]*\)\s*$/)   // Method calls
    
    return shouldHaveSemicolon !== null
  }  // C/C++ error detection (improved - no individual bracket checking)
  checkCppErrors(code) {
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Skip empty lines, comments, and preprocessor directives
      if (!trimmed || trimmed.startsWith('//') || trimmed.startsWith('/*') || 
          trimmed.startsWith('*') || trimmed.startsWith('#')) {
        return
      }

      // Only check for missing semicolons (brackets handled globally for block-based languages)
      if (this.shouldCheckCppSemicolon(trimmed, lines, index)) {
        errors.push({
          from: this.getLineEnd(lines, index) - 1,
          to: this.getLineEnd(lines, index),
          severity: 'warning',
          message: 'Missing semicolon'
        })
      }
    })

    return errors
  }
  
  // Improved C++ semicolon checking
  shouldCheckCppSemicolon(trimmed, lines, index) {
    if (!trimmed || trimmed.length === 0) return false
    
    // Don't check if line already ends with proper punctuation
    if (trimmed.endsWith(';') || trimmed.endsWith('{') || trimmed.endsWith('}') || 
        trimmed.startsWith('//') || trimmed.startsWith('#')) {
      return false
    }
    
    // Don't check control structures, function definitions, and declarations
    if (trimmed.match(/^\s*(if|for|while|class|struct|enum|namespace|try|catch|else|switch|case|default)\b/) ||
        trimmed.match(/^\s*(public|private|protected):\s*$/) ||
        trimmed.includes('(') && trimmed.includes(')') && !trimmed.includes('=')) {
      return false
    }
    
    // Only check lines that clearly should have semicolons in C++
    const shouldHaveSemicolon = 
      trimmed.match(/^\s*\w+\s+\w+\s*=/) ||           // Variable declarations with assignment
      trimmed.match(/^\s*return\s+/) ||               // Return statements
      trimmed.match(/^\s*\w+\s*=/) ||                 // Assignments
      trimmed.match(/^\s*(cout|printf)/) ||           // Output statements
      trimmed.match(/^\s*delete\s+/) ||               // Delete statements
      trimmed.match(/^\s*throw\s+/)                   // Throw statements
    
    return shouldHaveSemicolon !== null
  }
  // C# error detection (Java-compatible, no individual bracket checking)
  checkCSharpErrors(code) {
    // Use Java checker but without bracket checking (handled globally)
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Skip empty lines and comments
      if (!trimmed || trimmed.startsWith('//') || trimmed.startsWith('/*') || 
          trimmed.startsWith('*') || trimmed.startsWith('@')) {
        return
      }

      // Only check for missing semicolons (brackets handled globally)
      if (this.shouldCheckJavaSemicolon(trimmed, lines, index)) {
        errors.push({
          from: this.getLineEnd(lines, index) - 1,
          to: this.getLineEnd(lines, index),
          severity: 'warning',
          message: 'Missing semicolon'
        })
      }
    })

    return errors
  }
  // Go error detection (block-based language)
  checkGoErrors(code) {
    // Go is block-based, so minimal checking - brackets handled globally
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Skip empty lines and comments
      if (!trimmed || trimmed.startsWith('//') || trimmed.startsWith('/*')) {
        return
      }

      // Go doesn't use semicolons typically, so very minimal checking
      // Only check for obviously malformed lines
      if (this.hasObviousGoSyntaxError(trimmed)) {
        errors.push({
          from: this.getLineStart(lines, index),
          to: this.getLineEnd(lines, index),
          severity: 'warning',
          message: 'Potential syntax issue'
        })
      }
    })

    return errors
  }
  
  // Check for obvious Go syntax errors
  hasObviousGoSyntaxError(trimmed) {
    // Very conservative - only flag obviously wrong patterns
    return false // Disable for now - Go syntax is complex
  }
  // Rust error detection (block-based language)
  checkRustErrors(code) {
    // Rust is block-based, so minimal checking - brackets handled globally
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Skip empty lines and comments
      if (!trimmed || trimmed.startsWith('//') || trimmed.startsWith('/*')) {
        return
      }

      // Rust has complex syntax, so very minimal checking
      // Only check for obviously malformed lines
      if (this.hasObviousRustSyntaxError(trimmed)) {
        errors.push({
          from: this.getLineStart(lines, index),
          to: this.getLineEnd(lines, index),
          severity: 'warning',
          message: 'Potential syntax issue'
        })
      }
    })

    return errors
  }
  
  // Check for obvious Rust syntax errors
  hasObviousRustSyntaxError(trimmed) {
    // Very conservative - Rust syntax is complex
    return false // Disable for now
  }
  // Ruby error detection (more flexible)
  checkRubyErrors(code) {
    // Ruby has flexible syntax, so minimal checking
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Skip empty lines and comments
      if (!trimmed || trimmed.startsWith('#')) {
        return
      }

      // Ruby doesn't require semicolons and has flexible syntax
      // Only check for very obvious errors
      if (this.hasObviousRubySyntaxError(trimmed)) {
        errors.push({
          from: this.getLineStart(lines, index),
          to: this.getLineEnd(lines, index),
          severity: 'warning',
          message: 'Potential syntax issue'
        })
      }
    })

    return errors
  }
  
  // Check for obvious Ruby syntax errors
  hasObviousRubySyntaxError(trimmed) {
    // Very conservative - Ruby is very flexible
    return false // Disable for now
  }
  // R error detection (statistical language)
  checkRErrors(code) {
    // R has unique syntax, so minimal checking
    const errors = []
    const lines = code.split('\n')

    lines.forEach((line, index) => {
      const trimmed = line.trim()

      // Skip empty lines and comments
      if (!trimmed || trimmed.startsWith('#')) {
        return
      }

      // R has unique syntax patterns, so very minimal checking
      if (this.hasObviousRSyntaxError(trimmed)) {
        errors.push({
          from: this.getLineStart(lines, index),
          to: this.getLineEnd(lines, index),
          severity: 'warning',
          message: 'Potential syntax issue'
        })
      }
    })

    return errors
  }
  
  // Check for obvious R syntax errors
  hasObviousRSyntaxError(trimmed) {
    // Very conservative - R has unique syntax
    return false // Disable for now
  }
  // Generic error detection for unsupported languages
  checkGenericErrors(code) {
    // For unknown languages, only do very basic checking
    // Global bracket checking will handle most bracket issues
    return [] // Minimal checking for unknown languages
  }
  // Helper method to check for unmatched brackets (improved version)
  checkUnmatchedBrackets(line) {
    const brackets = []
    const stack = []
    const pairs = { '(': ')', '[': ']', '{': '}' }
    const openBrackets = Object.keys(pairs)
    const closeBrackets = Object.values(pairs)
    
    let inString = false
    let inComment = false
    let stringChar = ''
    
    for (let i = 0; i < line.length; i++) {
      const char = line[i]
      const nextChar = i < line.length - 1 ? line[i + 1] : ''
      const prevChar = i > 0 ? line[i - 1] : ''
      
      // Handle comments
      if (!inString && char === '/' && nextChar === '/') {
        inComment = true
        continue
      }
      
      if (inComment) {
        continue // Skip everything in comments
      }
      
      // Handle strings
      if (!inString && (char === '"' || char === "'" || char === '`')) {
        inString = true
        stringChar = char
        continue
      } else if (inString && char === stringChar && prevChar !== '\\') {
        inString = false
        stringChar = ''
        continue
      }
      
      if (inString) {
        continue // Skip everything in strings
      }
      
      // Now check brackets only if not in string or comment
      if (openBrackets.includes(char)) {
        stack.push({ char, pos: i })
      } else if (closeBrackets.includes(char)) {
        if (stack.length === 0) {
          // Only report as error if it's clearly wrong (not in typical code patterns)
          const beforeContext = line.substring(Math.max(0, i - 5), i)
          const afterContext = line.substring(i + 1, Math.min(line.length, i + 6))
          
          // Don't report errors for common valid patterns
          if (!this.isValidBracketContext(beforeContext, afterContext, char)) {
            brackets.push({ char, pos: i })
          }
        } else {
          const last = stack.pop()
          if (pairs[last.char] !== char) {
            brackets.push({ char, pos: i })
          }
        }
      }
    }

    // Only report unmatched opening brackets if they seem problematic
    stack.forEach(item => {
      const context = line.substring(Math.max(0, item.pos - 5), Math.min(line.length, item.pos + 10))
      if (!this.isValidOpenBracketContext(context, item.char)) {
        brackets.push(item)
      }
    })

    return brackets
  }
  
  // Helper to check if bracket context is likely valid
  isValidBracketContext(before, after, bracket) {
    // Common valid patterns where brackets might appear
    const validPatterns = [
      /System\.out\.println/,
      /console\.log/,
      /printf/,
      /cout/,
      /\w+\s*\(/,  // Function calls
      /if\s*\(/,   // Control structures
      /for\s*\(/,
      /while\s*\(/,
      /\[\s*\d+\s*\]/,  // Array indexing
    ]
    
    const fullContext = before + bracket + after
    return validPatterns.some(pattern => pattern.test(fullContext))
  }
  
  // Helper to check if open bracket context is valid
  isValidOpenBracketContext(context, bracket) {
    // Don't report errors for common valid patterns
    if (bracket === '{') {
      return context.includes('class') || context.includes('if') || 
             context.includes('for') || context.includes('while') ||
             context.includes('function') || context.includes('try') ||
             context.includes('else') || context.includes('switch')
    }
    if (bracket === '(') {
      return context.includes('if') || context.includes('for') || 
             context.includes('while') || context.includes('switch') ||
             /\w+\s*\($/.test(context)  // Function calls
    }
    return true // Be permissive for other brackets
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

  // Improved semicolon checking
  shouldCheckSemicolon(trimmed, lines, index) {
    if (!trimmed || trimmed.length === 0) return false
    
    // Don't check if line already ends with semicolon, braces, or is a comment
    if (trimmed.endsWith(';') || trimmed.endsWith('{') || trimmed.endsWith('}') || 
        trimmed.startsWith('//') || trimmed.includes('//')) {
      return false
    }
    
    // Don't check control structures
    if (trimmed.match(/^\s*(if|for|while|function|class|try|catch|else|switch|case|default)\b/)) {
      return false
    }
    
    // Don't check object/array literals that might span multiple lines
    if (trimmed.endsWith(',') || trimmed.endsWith('[') || trimmed.endsWith('(')) {
      return false
    }
    
    // Only check lines that clearly should have semicolons
    const shouldHaveSemicolon = 
      trimmed.match(/^\s*(let|const|var)\s+\w+/) ||  // Variable declarations
      trimmed.match(/^\s*return\s+/) ||              // Return statements
      trimmed.match(/^\s*\w+\s*=/) ||                // Assignments
      trimmed.match(/^\s*\w+\.\w+\s*\(/) ||          // Method calls
      trimmed.match(/^\s*console\./) ||              // Console calls
      trimmed.match(/^\s*throw\s+/)                  // Throw statements
    
    return shouldHaveSemicolon !== null
  }
  
  // Improved undefined variable checking
  shouldCheckUndefinedVars(trimmed) {
    // Be more conservative - only check in very specific contexts
    return trimmed.includes('(') && !trimmed.startsWith('//') && 
           !trimmed.includes('function') && !trimmed.includes('class')
  }
  
  // More accurate undefined function detection
  isLikelyUndefinedFunction(varName, code) {
    if (this.isBuiltinFunction(varName)) return false
    
    // Don't flag common patterns
    const commonPatterns = [
      'console', 'window', 'document', 'Math', 'Date', 'Array', 'Object',
      'JSON', 'parseInt', 'parseFloat', 'setTimeout', 'setInterval',
      'require', 'module', 'exports', 'process', '__dirname', '__filename'
    ]
    
    if (commonPatterns.includes(varName)) return false
    
    // Check if function is defined in the code (more thorough)
    const definitionPatterns = [
      new RegExp(`function\\s+${varName}\\s*\\(`),
      new RegExp(`const\\s+${varName}\\s*=`),
      new RegExp(`let\\s+${varName}\\s*=`),
      new RegExp(`var\\s+${varName}\\s*=`),
      new RegExp(`${varName}\\s*:\\s*function`),  // Object methods
      new RegExp(`${varName}\\s*=\\s*\\(`),       // Arrow functions
    ]
    
    return !definitionPatterns.some(pattern => pattern.test(code))
  }
}

// Export singleton instance
export const errorDetector = new ErrorDetector()
