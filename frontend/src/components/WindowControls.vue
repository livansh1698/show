<template>
  <div class="window-controls no-drag">
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
</template>

<script setup>
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
.window-controls {
  display: flex;
  align-items: center;
  height: 32px;
  -webkit-app-region: no-drag;
}

.ctrl-btn {
  width: 44px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: inherit;
  background: transparent;
  transition: all var(--transition-fast);
  border-radius: 0;
  opacity: 0.7;
}

.ctrl-btn:hover {
  background: rgba(0, 0, 0, 0.1);
  opacity: 1;
}

.close-btn:hover {
  background: #ef4444 !important;
  color: white !important;
}

/* 适配深色背景 */
:global(.dark-controls) .ctrl-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}
</style>
