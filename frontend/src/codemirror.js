// CodeMirror 6 configuration
import { EditorView, keymap, lineNumbers } from '@codemirror/view'
import { EditorState, Compartment } from '@codemirror/state'
import { defaultKeymap, history, historyKeymap, indentWithTab } from '@codemirror/commands'
import { autocompletion, closeBrackets, acceptCompletion, completionKeymap, completionStatus } from '@codemirror/autocomplete'
import { searchKeymap, highlightSelectionMatches } from '@codemirror/search'
import { foldGutter, bracketMatching } from '@codemirror/language'
import { linter, lintGutter } from '@codemirror/lint'
import { oneDark } from '@codemirror/theme-one-dark'
import { githubLight } from '@uiw/codemirror-theme-github'
import { monokai } from '@uiw/codemirror-theme-monokai'
import { vscodeDark, vscodeLight } from '@uiw/codemirror-theme-vscode'
import { python } from '@codemirror/lang-python'
import { java } from '@codemirror/lang-java'
import { javascript } from '@codemirror/lang-javascript'
import { cpp } from '@codemirror/lang-cpp'
import { go } from '@codemirror/lang-go'
import { rust } from '@codemirror/lang-rust'
import { errorDetector } from './services/errorDetector.js'

// Configuration compartments for dynamic changes
const languageConf = new Compartment()
const themeConf = new Compartment()
const fontConf = new Compartment()
const linterConf = new Compartment()

// Get theme extension for a given color scheme (independent of app theme)
export function getThemeExtension(colorScheme) {
  switch (colorScheme) {
    case 'oneLight':
    case 'githubLight':
      return githubLight
    case 'vscodeLight':
      return vscodeLight
    case 'oneDark':
      return oneDark
    case 'monokai':
      return monokai
    case 'vscodeDark':
      return vscodeDark
    default:
      return oneDark // Default to oneDark
  }
}

// Get font theme extension
export function getFontTheme(fontSize = '14px', tabSize = 4) {
  return EditorView.theme({
    '&': {
      fontSize: fontSize,
      height: '100%'
    },
    '&.cm-focused': {
      outline: 'none'
    },
    '.cm-content': {
      padding: '10px',
      minHeight: '300px',
      tabSize: tabSize.toString(),
      fontSize: `${fontSize} !important`
    },
    '.cm-editor': {
      height: '100%',
      fontSize: `${fontSize} !important`,
      border: 'none',
      borderRadius: '0'
    },
    '.cm-editor.cm-focused': {
      outline: 'none'
    },
    '.cm-scroller': {
      height: '100%',
      fontSize: `${fontSize} !important`
    },
    '.cm-gutter': {
      border: 'none'
    },
    '.cm-gutters': {
      border: 'none',
      borderRadius: '0'
    },
    '.cm-focused': {
      outline: 'none'
    }
  })
}

// Get language extension for a given language ID
export function getLanguageExtension(lang) {
  switch (lang) {
    case 'python':
      return python()
    case 'java':
      return java()
    case 'javascript':
    case 'typescript':
      return javascript()
    case 'cpp':
    case 'c':
      return cpp()
    case 'go':
      return go()
    case 'rust':
      return rust()
    case 'ruby':
      return javascript() // Use JavaScript highlighting for Ruby
    case 'r':
      return javascript() // Use JavaScript highlighting for R
    case 'csharp':
      return java() // Use Java highlighting for C#
    default:
      return javascript() // Default fallback
  }
}

// Create linter extension for a given language
export function createLinterExtension(language) {
  return linter((view) => {
    const code = view.state.doc.toString()
    const errors = errorDetector.detectErrors(code, language)
    
    return errors.map(error => ({
      from: error.from,
      to: error.to,
      severity: error.severity,
      message: error.message
    }))
  }, {
    delay: 300 // Debounce linting by 300ms
  })
}

// Create CodeMirror editor
export function createEditor(parent, options = {}) {
  const {
    doc = '',
    language = 'python',
    colorScheme = 'oneDark',
    fontSize = '14px',
    tabSize = 4,
    onChange = () => {},
    onRun = () => {},
    onSave = () => {},
    enableLinting = true
  } = options
  
  const extensions = [
    // Basic editor functionality
    lineNumbers(),
    foldGutter(),
    history(),
    bracketMatching(),
    closeBrackets(),
    autocompletion({
      activateOnTyping: true,
      override: [],
      closeOnBlur: true,
      maxRenderedOptions: 10
    }),
    highlightSelectionMatches(),
    
    // Dynamic configurations
    languageConf.of(getLanguageExtension(language)),
    themeConf.of(getThemeExtension(colorScheme)),
    fontConf.of(getFontTheme(fontSize, tabSize)),
    
    // Add linting if enabled
    ...(enableLinting ? [
      lintGutter(),
      linterConf.of(createLinterExtension(language))
    ] : []),
    
    // Keymaps - order matters! Custom shortcuts must come first to override defaults
    keymap.of([
      // Custom shortcuts with highest priority
      {
        key: 'Ctrl-s',
        preventDefault: true,
        run: () => {
          onSave()
          return true
        }
      },
      {
        key: 'Cmd-s',
        preventDefault: true,
        run: () => {
          onSave()
          return true
        }
      },
      {
        key: 'Ctrl-Enter',
        preventDefault: true,
        run: () => {
          onRun()
          return true
        }
      },
      {
        key: 'Cmd-Enter',
        preventDefault: true,
        run: () => {
          onRun()
          return true
        }
      },
      {
        key: 'Tab',
        run: (view) => {
          // Check if there's an active completion
          const status = completionStatus(view.state)
          if (status === 'active') {
            return acceptCompletion(view)
          }
          // Otherwise, indent using the indentWithTab command
          return indentWithTab(view)
        }
      },
      // Default keymaps after custom shortcuts
      ...completionKeymap.filter(binding => binding.key !== 'Tab' && binding.key !== 'Ctrl-Enter' && binding.key !== 'Cmd-Enter'), // Remove conflicting keys
      ...defaultKeymap.filter(binding => binding.key !== 'Ctrl-Enter' && binding.key !== 'Cmd-Enter'), // Remove conflicting keys
      ...historyKeymap,
      ...searchKeymap
    ]),
    
    // Update listener for content changes
    EditorView.updateListener.of((update) => {
      if (update.docChanged) {
        onChange(update.state.doc.toString())
      }
    })
  ]

  const state = EditorState.create({
    doc,
    extensions
  })

  const view = new EditorView({
    state,
    parent
  })

  return {
    view,
    updateLanguage(newLang) {
      const effects = [languageConf.reconfigure(getLanguageExtension(newLang))]
      
      // Update linter for new language if linting is enabled
      if (options.enableLinting !== false) {
        effects.push(linterConf.reconfigure(createLinterExtension(newLang)))
      }
      
      view.dispatch({ effects })
    },
    updateTheme(colorScheme = 'oneDark') {
      view.dispatch({
        effects: themeConf.reconfigure(getThemeExtension(colorScheme))
      })
    },
    updateFont(fontSize, tabSize) {
      view.dispatch({
        effects: fontConf.reconfigure(getFontTheme(fontSize, tabSize))
      })
    },
    updateSettings(settings) {
      const effects = []
      
      if (settings.colorScheme) {
        const themeExtension = getThemeExtension(settings.colorScheme)
        effects.push(themeConf.reconfigure(themeExtension))
      }
      
      if (settings.fontSize || settings.tabSize) {
        const fontSize = settings.fontSize || '14px'
        const tabSize = settings.tabSize || 4
        effects.push(fontConf.reconfigure(getFontTheme(fontSize, tabSize)))
      }
      
      if (effects.length > 0) {
        view.dispatch({ effects })
      }
    },
    setValue(value) {
      view.dispatch({
        changes: {
          from: 0,
          to: view.state.doc.length,
          insert: value
        }
      })
    },
    getValue() {
      return view.state.doc.toString()
    },
    destroy() {
      view.destroy()
    }
  }
}
