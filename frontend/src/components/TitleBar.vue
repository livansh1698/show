<template>
  <div class="titlebar" @mousedown="onMouseDown">
    <div class="titlebar-left">
      <div class="app-icon">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polygon points="12 2 22 8.5 22 15.5 12 22 2 15.5 2 8.5 12 2"/>
          <line x1="12" y1="22" x2="12" y2="15.5"/>
          <polyline points="22 8.5 12 15.5 2 8.5"/>
          <polyline points="2 15.5 12 8.5 22 15.5"/>
        </svg>
      </div>
      <span class="app-title">Show</span>
    </div>
    <div class="titlebar-controls">
      <button class="ctrl-btn minimize-btn" @click.stop="doMinimize" title="最小化">
        <svg width="12" height="12" viewBox="0 0 12 12">
          <rect x="2" y="5.5" width="8" height="1" fill="currentColor"/>
        </svg>
      </button>
      <button class="ctrl-btn maximize-btn" @click.stop="doMaximize" title="最大化">
        <svg width="12" height="12" viewBox="0 0 12 12">
          <rect x="2" y="2" width="8" height="8" fill="none" stroke="currentColor" stroke-width="1.2"/>
        </svg>
      </button>
      <button class="ctrl-btn close-btn" @click.stop="doClose" title="关闭">
        <svg width="12" height="12" viewBox="0 0 12 12">
          <line x1="2.5" y1="2.5" x2="9.5" y2="9.5" stroke="currentColor" stroke-width="1.3"/>
          <line x1="9.5" y1="2.5" x2="2.5" y2="9.5" stroke="currentColor" stroke-width="1.3"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
let isDragging = false

function onMouseDown(e) {
  // 不在按钮上时才拖动
  if (e.target.closest('.ctrl-btn')) return

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

function doMinimize() {
  if (window.ddmo) window.ddmo.minimize()
}

function doMaximize() {
  if (window.ddmo) window.ddmo.maximize()
}

function doClose() {
  if (window.ddmo) window.ddmo.close()
}
</script>

<style scoped>
.titlebar {
  height: var(--titlebar-height);
  min-height: var(--titlebar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px 0 14px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border);
  cursor: default;
  -webkit-app-region: drag;
  z-index: 1000;
}

.titlebar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent);
}

.app-title {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

.titlebar-controls {
  display: flex;
  align-items: center;
  height: 100%;
  -webkit-app-region: no-drag;
}

.ctrl-btn {
  width: 40px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  background: transparent;
  transition: all var(--transition-fast);
  border-radius: 0;
}

.ctrl-btn:hover {
  background: rgba(0, 0, 0, 0.06);
  color: var(--text-primary);
}

.close-btn:hover {
  background: var(--danger);
  color: white;
}
</style>
