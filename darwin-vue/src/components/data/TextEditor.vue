<script setup>
import { computed, onMounted, onUnmounted, ref, useTemplateRef, watch } from 'vue'
import { basicSetup } from 'codemirror'
import { indentWithTab } from '@codemirror/commands'
import { Compartment, EditorState } from '@codemirror/state'
import { EditorView, keymap } from '@codemirror/view'
import { indentUnit } from '@codemirror/language'
import { java } from '@codemirror/lang-java'
import { javascript } from '@codemirror/lang-javascript'
import { html } from '@codemirror/lang-html'
import { xml } from '@codemirror/lang-xml'
import { css } from '@codemirror/lang-css'
import { autocompletion, closeBracketsKeymap, completionKeymap } from '@codemirror/autocomplete'
import { oneDark } from '@codemirror/theme-one-dark'
import { dracula } from '@uiw/codemirror-theme-dracula'
import { githubDark } from '@uiw/codemirror-theme-github'

const props = defineProps({
  title: { default: '文本编辑器' },
  lang: { type: String, default: '' },
  height: { type: Number, default: 550 },
  refresh: { type: Number },
  readOnly: { default: false }
})
const text = defineModel()
const editorView = ref()
const editorRef = useTemplateRef('editorRef')
const editorLang = new Compartment()
const editorIndentSize = new Compartment()
const editorTheme = new Compartment()

const language = computed(() => {
  const lang = props.lang ? props.lang.toLowerCase() : ''
  if (lang === 'groovy') return java()
  else if (lang === 'java') return java()
  else if (lang === 'javascript') return javascript({ jsx: true })
  else if (lang === 'xml') return xml()
  else if (lang === 'html') return html()
  else if (lang === 'css') return css()
  return undefined
})

const indentSize = computed(() => {
  const lang = props.lang ? props.lang.toLowerCase() : ''
  if (lang === 'groovy' || lang === 'java') return indentUnit.of(' '.repeat(4))
  else if (lang === 'javascript' || lang === 'html' || lang === 'xml' || lang === 'css') {
    return indentUnit.of(' '.repeat(2))
  }
  return indentUnit.of(' '.repeat(2))
})

const theme = computed(() => {
  const lang = props.lang ? props.lang.toLowerCase() : ''
  if (lang === 'groovy' || lang === 'java') return oneDark
  else if (lang === 'javascript') return dracula
  else if (lang === 'html' || lang === 'xml' || lang === 'css') return githubDark
  return oneDark
})

const initEditor = () => {
  destroyEditor()
  const state = EditorState.create({
    doc: text.value,
    extensions: language.value ? [
      basicSetup,
      keymap.of([ indentWithTab, ... closeBracketsKeymap, ... completionKeymap ]),
      editorTheme.of(theme.value),
      editorIndentSize.of(indentSize.value),
      editorLang.of(language.value),
      autocompletion(),
      EditorState.readOnly.of(props.readOnly),
      EditorView.updateListener.of(update => text.value = update.state.doc.toString())
    ] : [
      basicSetup,
      keymap.of([ indentWithTab, ... closeBracketsKeymap, ... completionKeymap ]),
      editorTheme.of(theme.value),
      editorIndentSize.of(indentSize.value),
      autocompletion(),
      EditorState.readOnly.of(props.readOnly),
      EditorView.updateListener.of(update => text.value = update.state.doc.toString())
    ]
  })
  if (editorRef) {
    editorView.value = new EditorView({
      state: state,
      parent: editorRef.value
    })
  }
}

const destroyEditor = () => {
  if (editorView.value) editorView.value.destroy()
}

watch(() => props.refresh, () => initEditor())
watch(() => props.lang, () => {
  if (editorView.value) {
    if (language.value) editorView.value.dispatch({ effects: editorLang.reconfigure(language.value) })
    editorView.value.dispatch({ effects: editorIndentSize.reconfigure(indentSize.value) })
    editorView.value.dispatch({ effects: editorTheme.reconfigure(theme.value) })
  }
})
onMounted(() => initEditor())
onUnmounted(() => destroyEditor())
</script>

<template>
  <div class="text-editor">
    <div class="text-editor-title">{{ props.title }}</div>
    <div ref="editorRef" class="text-editor-body"
         :style="{ 'height': height + 'px', 'max-height': height + 'px' }">
    </div>
  </div>
</template>

<style scoped>
.text-editor {
  --bs-card-box-shadow: rgba(145, 158, 171, 0.2) 0px 0px 2px 0px, rgba(145, 158, 171, 0.12) 0px 12px 24px -4px;
  width: 100%;
}
.text-editor-title {
  color: #d1cccc;
  display: flex;
  align-content: center;
  align-items: center;
  height: 40px;
  font-size: 15px;
  font-weight: 600;
  background-color: #3D3D3D;
  padding: 15px;
  border: 0 solid #ebf1f6;
  border-top-left-radius: 7px;
  border-top-right-radius: 7px;
  box-shadow: var(--bs-card-box-shadow);
}
.text-editor-body {
  background-color: #292C34;
  overflow: scroll;
  border: 0 solid #ebf1f6;
  border-bottom-left-radius: 7px;
  border-bottom-right-radius: 7px;
  box-shadow: var(--bs-card-box-shadow)
}
</style>