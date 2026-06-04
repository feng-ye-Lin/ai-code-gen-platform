<script setup lang="ts">
import { computed } from 'vue'
import GlobalHeader from '@/components/layout/GlobalHeader.vue'
import GlobalFooter from '@/components/layout/GlobalFooter.vue'
import type { MenuItem } from '@/components/layout/GlobalHeader.vue'
import { useLoginUserStore } from '@/stores/loginUser'

const loginUserStore = useLoginUserStore()

const menuItems = computed<MenuItem[]>(() => {
  const items: MenuItem[] = [
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
  // 仅当用户角色为 admin 时显示管理菜单
  if (loginUserStore.loginUser.userRole === 'admin') {
    items.push({
      key: '/admin/userManage',
      label: '用户管理',
      path: '/admin/userManage',
    })
    items.push({
      key: '/admin/appManage',
      label: '应用管理',
      path: '/admin/appManage',
    })
  }
  return items
})
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
  height: 100vh;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.basic-layout-content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 8px;
  background: #f5f5f5;
  position: relative;
}
</style>
