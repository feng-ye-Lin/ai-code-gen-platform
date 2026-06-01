<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import type { ItemType } from 'ant-design-vue'
import Logo from '@/components/Logo.vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'

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
  },
)

const router = useRouter()
const route = useRoute()

const selectedKeys = ref<string[]>([route.path])

const loginUserStore = useLoginUserStore()

watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
  },
)

function handleMenuClick({ key }: { key: string }) {
  router.push(key)
}

function handleLogin() {
  router.push('/user/login')
}

async function handleLogout() {
  try {
    const res = await userLogout()
    if (res.data.code === 0) {
      message.success('退出登录成功')
      loginUserStore.clearLoginUser()
      router.push('/user/login')
    } else {
      message.error(res.data.message ?? '退出登录失败')
    }
  } catch (e: unknown) {
    const errorMessage = e instanceof Error ? e.message : '退出登录失败'
    message.error(errorMessage)
  }
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
      <div v-if="loginUserStore.loginUser.id">
        <a-dropdown>
          <a-space class="user-info" style="cursor: pointer">
            <a-avatar :src="loginUserStore.loginUser.userAvatar" />
            <span>{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
          </a-space>
          <template #overlay>
            <a-menu>
              <a-menu-item key="logout" @click="handleLogout">
                <span>退出登录</span>
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
      <div v-else>
        <a-button type="primary" size="small" class="login-btn" @click="handleLogin">登录</a-button>
      </div>
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
  background: linear-gradient(135deg, #d32f2f 0%, #ff7043 100%);
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
  color: #d32f2f;
}

.header-menu :deep(.ant-menu-item-selected::after) {
  border-bottom-color: #d32f2f;
}

.header-right {
  flex-shrink: 0;
}

.user-info {
  transition: opacity 0.2s;
}

.user-info:hover {
  opacity: 0.7;
}

.login-btn {
  background: linear-gradient(135deg, #d32f2f 0%, #ff7043 100%);
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
