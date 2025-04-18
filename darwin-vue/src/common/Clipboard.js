export const writeClipboard = async text => {
  if (navigator.clipboard && window.isSecureContext) {
    await navigator.clipboard.writeText(text)
    return
  }
  const input = document.createElement('input')
  input.style.position = 'fixed'
  input.style.top = '-10000px'
  input.style.zIndex = '-999'
  document.body.appendChild(input)
  input.value = text
  input.focus()
  input.select()
  try {
    document.execCommand('copy')
  } finally {
    document.body.removeChild(input)
  }
}