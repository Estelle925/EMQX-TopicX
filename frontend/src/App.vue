<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
// App.vue 作为根组件，只需要渲染路由视图
</script>

<style>
#app {
  min-height: 100vh;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  position: relative;
  overflow-x: hidden;
}



/* 路由视图容器 */
.router-view {
  width: 100%;
  height: 100%;
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
}

::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: var(--radius-sm);
}

/* 选择文本样式 */
::selection {
  background: rgba(0, 212, 170, 0.2);
  color: var(--text-primary);
}

/* 焦点样式 */
:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
  border-radius: var(--radius-sm);
}


</style>