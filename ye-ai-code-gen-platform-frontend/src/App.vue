<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { healthCheck } from '@/api/healthController.ts'

healthCheck().then((res) => {
  console.log(res)
})


const route = useRoute()
const isWelcomePage = computed(() => route.path === '/')
</script>

<template>
  <BasicLayout v-if="!isWelcomePage" />
  <RouterView v-else />
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
