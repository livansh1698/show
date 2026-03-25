<template>
  <div class="app-container">
    <div class="app-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.app-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: var(--radius-window);
  //border: 1px solid rgba(0, 0, 0, 0.1);
  background: var(--bg-primary);
}

.app-content {
  flex: 1;
  overflow: hidden;
  position: relative;
}
</style>
