<script setup>
import { computed, onMounted, onUnmounted, ref, useTemplateRef, watch } from 'vue'
import { basicSetup } from 'codemirror'
import { indentWithTab } from '@codemirror/commands'
import { Compartment, EditorState } from '@codemirror/state'
import { EditorView, keymap } from '@codemirror/view'
import { indentUnit } from '@codemirror/language'
import { java } from '@codemirror/lang-java'
import { javascript } from '@codemirror/lang-javascript'
import { autocompletion, closeBracketsKeymap, completionKeymap } from '@codemirror/autocomplete'
import { oneDark } from '@codemirror/theme-one-dark'
import { dracula } from '@uiw/codemirror-theme-dracula'

const props = defineProps({
  code: { required: true },
  lang: { type: String, default: 'groovy' },
  refresh: { type: Number },
  readOnly: { default: false }
})
const emits = defineEmits(['change'])
const editorView = ref()
const editorRef = useTemplateRef('editorRef')
const editorLang = new Compartment()
const editorIndentSize = new Compartment()
const editorTheme = new Compartment()

const language = computed(() => {
  const lang = props.lang.toLowerCase()
  if (lang === 'groovy') return java()
  else if (lang === 'java') return java()
  else if (lang === 'javascript') return javascript()
  return java()
})

const indentSize = computed(() => {
  const lang = props.lang.toLowerCase()
  if (lang === 'groovy' || lang === 'java') return indentUnit.of(' '.repeat(4))
  else if (lang === 'javascript') return indentUnit.of(' '.repeat(2))
  return indentUnit.of(' '.repeat(4))
})

const theme = computed(() => {
  const lang = props.lang.toLowerCase()
  if (lang === 'groovy' || lang === 'java') return oneDark
  else if (lang === 'javascript') return dracula
  return oneDark
})

const initEditor = () => {
  destroyEditor()
  const state = EditorState.create({
    doc: props.code,
    extensions: [
      basicSetup,
      keymap.of([ indentWithTab, ... closeBracketsKeymap, ... completionKeymap ]),
      editorTheme.of(theme.value),
      editorIndentSize.of(indentSize.value),
      editorLang.of(language.value),
      autocompletion(),
      EditorState.readOnly.of(props.readOnly),
      EditorView.updateListener.of(update => emits('change', update.state.doc.toString()))
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
    editorView.value.dispatch({ effects: editorLang.reconfigure(language.value) })
    editorView.value.dispatch({ effects: editorIndentSize.reconfigure(indentSize.value) })
    editorView.value.dispatch({ effects: editorTheme.reconfigure(theme.value) })
  }
})
onMounted(() => initEditor())
onUnmounted(() => destroyEditor())
</script>

<template>
  <div ref="editorRef" class="code-editor"></div>
</template>

<style scoped>
.code-editor {
  width: 100%;
  max-width: 980px;
  max-height: 550px;
  height: 550px;
  background-color: #292C34;
  overflow: scroll;
}
</style>