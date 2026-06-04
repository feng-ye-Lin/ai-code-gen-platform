<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { getAppById, updateApp, updateAppByAdmin, deleteApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = computed(() => route.params.appId as string)
const loading = ref(false)
const saving = ref(false)

// 表单数据
const formRef = ref()
const formData = reactive<{
  appName?: string
  cover?: string
  priority?: number
}>({
  appName: '',
  cover: '',
  priority: 0,
})

// 是否为管理员
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')
// 是否为自己的应用
const isOwner = ref(false)

// 加载应用信息
const loadApp = async () => {
  if (!appId.value) return
  loading.value = true
  try {
    const api = isAdmin.value ? getAppById : getAppById
    const res = await api({ id: appId.value })
    if (res.data.code === 0 && res.data.data) {
      const app = res.data.data
      formData.appName = app.appName
      formData.cover = app.cover || ''
      formData.priority = app.priority || 0
      isOwner.value = app.userId === loginUserStore.loginUser.id
      // 检查权限
      if (!isAdmin.value && !isOwner.value) {
        message.error('您没有权限编辑此应用')
        router.back()
      }
    } else {
      message.error(res.data.message ?? '获取应用信息失败')
      router.back()
    }
  } catch {
    message.error('获取应用信息失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 保存
const handleSave = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    let res
    if (isAdmin.value) {
      // 管理员使用管理接口
      res = await updateAppByAdmin({
        id: appId.value,
        appName: formData.appName,
        cover: formData.cover,
        priority: formData.priority,
      })
    } else {
      // 普通用户只更新名称
      res = await updateApp({
        id: appId.value,
        appName: formData.appName,
      })
    }
    if (res.data.code === 0) {
      message.success('保存成功')
      router.back()
    } else {
      message.error(res.data.message ?? '保存失败')
    }
  } catch (err) {
    if ((err as { errorFields?: unknown })?.errorFields) {
      // 表单验证错误，不处理
      return
    }
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 删除
const handleDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: '您确定要删除此应用吗？此操作不可恢复。',
    okText: '确认',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        saving.value = true
        const res = await deleteApp({ id: appId.value })
        if (res.data.code === 0) {
          message.success('删除成功')
          router.push('/home')
        } else {
          message.error(res.data.message ?? '删除失败')
        }
      } catch {
        message.error('删除失败')
      } finally {
        saving.value = false
      }
    },
  })
}

onMounted(() => {
  loadApp()
})
</script>

<template>
  <div class="app-edit-page">
    <a-card title="编辑应用" :loading="loading">
      <a-form
        ref="formRef"
        :model="formData"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 12 }"
        style="max-width: 800px; margin: 0 auto;"
      >
        <a-form-item
          label="应用名称"
          name="appName"
          :rules="[{ required: true, message: '请输入应用名称' }]"
        >
          <a-input v-model:value="formData.appName" placeholder="请输入应用名称" />
        </a-form-item>

        <a-form-item label="封面图片" v-if="isAdmin">
          <a-input v-model:value="formData.cover" placeholder="请输入封面图片 URL" />
          <div v-if="formData.cover" style="margin-top: 8px;">
            <img :src="formData.cover" style="width: 120px; height: 120px; object-fit: cover; border-radius: 8px;" />
          </div>
        </a-form-item>

        <a-form-item label="优先级" v-if="isAdmin">
          <a-input-number
            v-model:value="formData.priority"
            :min="0"
            :max="99"
            placeholder="请输入优先级"
            style="width: 100%;"
          />
          <div style="color: #999; font-size: 12px; margin-top: 4px;">优先级越高，在精选列表越靠前，设为 99 即为精选</div>
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 6, span: 12 }">
          <a-space>
            <a-button type="primary" :loading="saving" @click="handleSave">
              保存
            </a-button>
            <a-button @click="router.back()">取消</a-button>
            <a-button danger @click="handleDelete" v-if="isOwner || isAdmin">
              删除应用
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>



<style scoped>
.app-edit-page {
  padding: 24px;
}
</style>
