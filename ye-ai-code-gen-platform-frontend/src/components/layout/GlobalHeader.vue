<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { ItemType } from 'ant-design-vue'
import Logo from '@/components/Logo.vue'

export interface MenuItem {
  key: string
  label: string
  path?: string
  children?: MenuItem[]
}

const props = withDefaults(
  defineProps<{
    menuItems?: MenuItem[]
    title?: string
  }>(),
  {
    menuItems: () => [],
    title: 'YeCode 零代码应用生成平台',
  }
)

const router = useRouter()
const route = useRoute()

const selectedKeys = ref<string[]>([route.path])

watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
  }
)

function handleMenuClick({ key }: { key: string }) {
  router.push(key)
}

function convertToAntdMenu(items: MenuItem[]): ItemType[] {
  return items.map((item) => {
    const menuItem: ItemType = {
      key: item.path || item.key,
      label: item.label,
    }
    if (item.children && item.children.length > 0) {
      ;(menuItem as any).children = convertToAntdMenu(item.children)
    }
    return menuItem
  })
}
</script>

<template>
  <a-layout-header class="global-header">
    <div class="header-left">
      <div class="header-logo">
        <Logo />
      </div>
      <span class="header-title">{{ title }}</span>
    </div>

    <a-menu
      v-model:selectedKeys="selectedKeys"
      mode="horizontal"
      theme="light"
      :items="convertToAntdMenu(menuItems)"
      class="header-menu"
      @click="handleMenuClick"
    />

    <div class="header-right">
      <a-button type="primary" size="small" class="login-btn">登录</a-button>
    </div>
  </a-layout-header>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  background: #fff;
  height: 64px;
  line-height: 64px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.header-logo {
  display: flex;
  align-items: center;
}

.header-logo :deep(.logo-svg) {
  width: 40px;
  height: 40px;
}

.header-logo :deep(.logo-text) {
  display: none;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  background: linear-gradient(135deg, #D32F2F 0%, #FF7043 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
  white-space: nowrap;
}

.header-menu {
  flex: 1;
  display: flex;
  justify-content: flex-start;
  background: transparent;
  border-bottom: none;
  min-width: 0;
  margin-left: 48px;
}

.header-menu :deep(.ant-menu-item) {
  font-weight: 500;
  color: #333;
}

.header-menu :deep(.ant-menu-item-selected) {
  color: #D32F2F;
}

.header-menu :deep(.ant-menu-item-selected::after) {
  border-bottom-color: #D32F2F;
}

.header-right {
  flex-shrink: 0;
}

.login-btn {
  background: linear-gradient(135deg, #D32F2F 0%, #FF7043 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(211, 47, 47, 0.25);
  border-radius: 16px;
}

.login-btn:hover {
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.35);
}

@media (max-width: 768px) {
  .global-header {
    padding: 0 16px;
  }

  .header-title {
    display: none;
  }

  .header-menu {
    margin-left: 16px;
  }
}
</style>