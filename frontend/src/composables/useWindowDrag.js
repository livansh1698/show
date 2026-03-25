export function useWindowDrag() {
  let isDragging = false

  const onMouseDown = (e) => {
    // 如果点击的是按钮或输入框，不触发拖动
    if (e.target.closest('button, input, a, .no-drag')) return

    isDragging = true
    if (window.ddmo) {
      window.ddmo.startDrag(e.screenX, e.screenY)
    }

    const onMouseMove = (ev) => {
      if (isDragging && window.ddmo) {
        window.ddmo.drag(ev.screenX, ev.screenY)
      }
    }

    const onMouseUp = () => {
      isDragging = false
      document.removeEventListener('mousemove', onMouseMove)
      document.removeEventListener('mouseup', onMouseUp)
    }

    document.addEventListener('mousemove', onMouseMove)
    document.addEventListener('mouseup', onMouseUp)
  }

  return {
    onMouseDown
  }
}
