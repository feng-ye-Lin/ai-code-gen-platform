<script setup lang="ts">
import GlobalHeader from '@/components/layout/GlobalHeader.vue'
import GlobalFooter from '@/components/layout/GlobalFooter.vue'
import type { MenuItem } from '@/components/layout/GlobalHeader.vue'

const menuItems: MenuItem[] = [
  {
    key: '/',
    label: '首页',
    path: '/',
  },
  {
    key: '/home',
    label: '工作台',
    path: '/home',
  },
  {
    key: '/about',
    label: '关于',
    path: '/about',
  },
]
</script>

<template>
  <a-layout class="basic-layout">
    <GlobalHeader :menu-items="menuItems" />
    <a-layout-content class="basic-layout-content">
      <RouterView />
    </a-layout-content>
    <GlobalFooter />
  </a-layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.basic-layout-content {
  flex: 1;
  padding: 8px;
  background: #f5f5f5;
}
</style>