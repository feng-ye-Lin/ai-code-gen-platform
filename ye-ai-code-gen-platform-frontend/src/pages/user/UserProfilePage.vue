<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { updateUser } from '@/api/userController.ts'
import type { FormInstance } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()
const formRef = ref<FormInstance>()

const formModel = reactive({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const loading = ref(false)

onMounted(() => {
  formModel.userName = loginUserStore.loginUser.userName || ''
  formModel.userAvatar = loginUserStore.loginUser.userAvatar || ''
  formModel.userProfile = loginUserStore.loginUser.userProfile || ''
})

const handleSave = async () => {
  try {
    await formRef.value?.validateFields()
    Modal.confirm({
      title: '确认保存',
      content: '您确定要保存个人信息吗？',
      okText: '确认',
      okType: 'primary',
      cancelText: '取消',
      onOk: async () => {
        try {
          loading.value = true
          const res = await updateUser({
            id: loginUserStore.loginUser.id,
            userName: formModel.userName,
            userAvatar: formModel.userAvatar,
            userProfile: formModel.userProfile,
          })
          if (res.data.code === 0) {
            message.success('保存成功')
            // 更新 store 中的用户信息
            loginUserStore.setLoginUser({
              ...loginUserStore.loginUser,
              userName: formModel.userName,
              userAvatar: formModel.userAvatar,
              userProfile: formModel.userProfile,
            })
          } else {
            message.error('保存失败，' + res.data.message)
          }
        } catch (error) {
          message.error('保存失败')
        } finally {
          loading.value = false
        }
      },
    })
  } catch (errInfo) {
    console.log('Validate Failed:', errInfo)
  }
}
</script>

<template>
  <div class="user-profile-page">
    <a-card title="个人中心" :bordered="false" class="profile-card">
      <a-form
        ref="formRef"
        :model="formModel"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item
          label="用户账号"
        >
          <span>{{ loginUserStore.loginUser.userAccount }}</span>
        </a-form-item>
        <a-form-item
          label="用户名称"
          name="userName"
          :rules="[{ required: true, message: '请输入用户名称' }]"
        >
          <a-input v-model:value="formModel.userName" placeholder="请输入用户名称" />
        </a-form-item>
        <a-form-item
          label="用户头像"
          name="userAvatar"
        >
          <a-input v-model:value="formModel.userAvatar" placeholder="请输入头像URL" />
        </a-form-item>
        <a-form-item
          label="用户简介"
          name="userProfile"
        >
          <a-textarea v-model:value="formModel.userProfile" placeholder="请输入用户简介" :rows="4" />
        </a-form-item>
        <a-form-item
          label="用户角色"
        >
          <span>{{ loginUserStore.loginUser.userRole === 'admin' ? '管理员' : '普通用户' }}</span>
        </a-form-item>
        <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
          <a-button type="primary" @click="handleSave" :loading="loading">保存</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<style scoped>
.user-profile-page {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 8px;
}
</style>
