// CodeMirror 6 configuration
import { EditorView, keymap, lineNumbers } from '@codemirror/view'
import { EditorState, Compartment } from '@codemirror/state'
import { defaultKeymap, history, historyKeymap, indentWithTab } from '@codemirror/commands'
import { autocompletion, closeBrackets } from '@codemirror/autocomplete'
import { searchKeymap, highlightSelectionMatches } from '@codemirror/search'
import { foldGutter, bracketMatching } from '@codemirror/language'
import { oneDark } from '@codemirror/theme-one-dark'
import { python } from '@codemirror/lang-python'
import { java } from '@codemirror/lang-java'
import { javascript } from '@codemirror/lang-javascript'
import { cpp } from '@codemirror/lang-cpp'
import { go } from '@codemirror/lang-go'
import { rust } from '@codemirror/lang-rust'

// Language configuration compartment for dynamic changes
const languageConf = new Compartment()

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

// Create CodeMirror editor
export function createEditor(parent, options = {}) {
  const {
    doc = '',
    language = 'python',
    onChange = () => {},
    onRun = () => {}
  } = options
  const extensions = [
    // Basic editor functionality
    lineNumbers(),
    foldGutter(),
    history(),
    bracketMatching(),
    closeBrackets(),
    autocompletion(),
    highlightSelectionMatches(),
    
    // Theme and language
    oneDark,
    languageConf.of(getLanguageExtension(language)),
    
    // Keymaps
    keymap.of([
      ...defaultKeymap,
      ...historyKeymap,
      ...searchKeymap,
      indentWithTab,
      {
        key: 'Ctrl-Enter',
        run: () => {
          onRun()
          return true
        }
      },
      {
        key: 'Cmd-Enter',
        run: () => {
          onRun()
          return true
        }
      }
    ]),
    
    // Update listener for content changes
    EditorView.updateListener.of((update) => {
      if (update.docChanged) {
        onChange(update.state.doc.toString())
      }
    }),
    
    // Custom styling
    EditorView.theme({
      '&': {
        fontSize: '14px',
        height: '100%'
      },
      '.cm-content': {
        padding: '16px',
        minHeight: '300px'
      },
      '.cm-focused': {
        outline: 'none'
      },
      '.cm-editor': {
        height: '100%'
      },
      '.cm-scroller': {
        height: '100%'
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
      view.dispatch({
        effects: languageConf.reconfigure(getLanguageExtension(newLang))
      })
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
