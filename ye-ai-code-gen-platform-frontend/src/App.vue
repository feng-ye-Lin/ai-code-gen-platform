<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { healthCheck } from '@/api/healthController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import zhCN from 'ant-design-vue/es/locale/zh_CN'

healthCheck().then((res) => {
  console.log(res)
})

const route = useRoute()
const isWelcomePage = computed(() => route.path === '/')

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()
</script>

<template>
  <a-config-provider :locale="zhCN">
    <BasicLayout v-if="!isWelcomePage" />
    <RouterView v-else />
  </a-config-provider>
</template>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
}
</style>
