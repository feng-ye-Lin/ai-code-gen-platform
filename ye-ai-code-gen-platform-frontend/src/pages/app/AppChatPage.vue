<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { getAppById, deployApp, chatToGenCode } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = computed(() => route.params.appId as string)
const isViewMode = computed(() => route.query.view === '1')
const app = ref<API.AppVO | null>(null)
const loadingApp = ref(false)
const loadingDeploy = ref(false)

// 是否是自己的作品
const isMyApp = computed(() => {
  if (!app.value?.userId) return true
  return app.value.userId === loginUserStore.loginUser.id
})

// 对话消息
interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  timestamp?: number
}

const messages = ref<ChatMessage[]>([])
const userInput = ref('')
const loadingSend = ref(false)
const eventSource = ref<EventSource | null>(null)
const isGenerating = ref(false)

// 网站预览
const websiteUrl = ref('')
const showWebsite = ref(false)

// 加载应用信息
const loadApp = async () => {
  if (!appId.value) return
  loadingApp.value = true
  try {
    const res = await getAppById({ id: appId.value })
    if (res.data.code === 0 && res.data.data) {
      app.value = res.data.data
      // 仅在非查看模式下，自动发送第一条消息
      if (!isViewMode.value && app.value.initPrompt && messages.value.length === 0) {
        await nextTick()
        sendMessage(app.value.initPrompt, true)
      }
      // 在查看模式下，自动显示网站预览
      if (isViewMode.value) {
        updateWebsiteUrl()
        showWebsite.value = true
      }
    } else {
      message.error(res.data.message ?? '获取应用信息失败')
      router.back()
    }
  } catch {
    message.error('获取应用信息失败')
    router.back()
  } finally {
    loadingApp.value = false
  }
}

// 发送消息
const sendMessage = async (prompt?: string, isAuto = false) => {
  const content = prompt || userInput.value.trim()
  if (!content || isGenerating.value) return

  if (!isAuto) {
    userInput.value = ''
  }

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: content,
    timestamp: Date.now(),
  })

  // 添加空的 AI 消息，用于流式更新（直接 push 到 messages 数组，后续通过数组索引修改）
  messages.value.push({
    role: 'assistant',
    content: '',
    timestamp: Date.now(),
  })
  const aiMsgIndex = messages.value.length - 1

  isGenerating.value = true
  loadingSend.value = true

  // 使用 chatToGenCode 接口路径和参数（注意：chatToGenCode 基于 axios，不支持流式，所以用 fetch 实现 SSE）
  const params: API.chatToGenCodeParams = {
    appId: appId.value,
    message: content,
  }
  // 从 request.ts 的 baseURL 拼接完整 URL
  const baseUrl = 'http://localhost:8531/api'
  const queryString = new URLSearchParams(params as unknown as Record<string, string>).toString()
  const url = `${baseUrl}/app/chat/gen/code?${queryString}`

  const updateAiContent = (chunk: string) => {
    // 直接修改 messages 数组内对象的属性，确保响应式系统能检测到变更
    messages.value[aiMsgIndex].content += chunk
    scrollToBottom()
  }

  try {
    // 使用原生 EventSource 实现 SSE（支持 withCredentials 携带 cookie）
    eventSource.value = new EventSource(url, { withCredentials: true })

    // 处理默认消息事件（后端发送的普通 data 事件）
    eventSource.value.onmessage = (event) => {
      try {
        // 后端返回的是 JSON: {"d": "内容"}
        const parsed = JSON.parse(event.data)
        if (parsed && typeof parsed.d === 'string') {
          updateAiContent(parsed.d)
        }
      } catch {
        // 如果不是 JSON，直接追加原始内容
        updateAiContent(event.data)
      }
    }

    // 监听自定义 done 事件（后端发送的结束事件）
    eventSource.value.addEventListener('done', () => {
      eventSource.value?.close()
      eventSource.value = null
      isGenerating.value = false
      loadingSend.value = false
      showWebsite.value = true
      updateWebsiteUrl()
    })

    // 处理错误
    eventSource.value.onerror = (err) => {
      eventSource.value?.close()
      eventSource.value = null
      isGenerating.value = false
      loadingSend.value = false
      message.error('发送消息失败')
    }
  } catch {
    message.error('发送消息失败')
    isGenerating.value = false
    loadingSend.value = false
  }
}

// 更新网站预览 URL
const updateWebsiteUrl = () => {
  if (!app.value) return
  const baseUrl = 'http://localhost:8531/api'
  websiteUrl.value = `${baseUrl}/static/${app.value.codeGenType}_${app.value.id}/`
}

// 部署应用
const handleDeploy = async () => {
  if (!app.value) return
  loadingDeploy.value = true
  try {
    const res = await deployApp({ appId: app.value.id })
    if (res.data.code === 0 && res.data.data) {
      message.success('部署成功')
      Modal.success({
        title: '部署成功',
        content: `您的应用已成功部署！\n访问地址：${res.data.data}`,
      })
    } else {
      message.error(res.data.message ?? '部署失败')
    }
  } catch {
    message.error('部署失败')
  } finally {
    loadingDeploy.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    const container = document.querySelector('.chat-messages')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })
}

// 停止生成
const stopGenerate = () => {
  eventSource.value?.close()
  eventSource.value = null
  isGenerating.value = false
  loadingSend.value = false
  showWebsite.value = true
  updateWebsiteUrl()
}

// 跳转到编辑页面
const goToEdit = () => {
  if (!app.value) return
  router.push(`/app/edit/${app.value.id}`)
}

// 查看部署的作品
const viewWork = () => {
  if (!app.value || !app.value.deployKey) return
  window.open(`http://localhost/${app.value.deployKey}`, '_blank')
}

onMounted(() => {
  loadApp()
})
</script>

<template>
  <div class="app-chat-page">
    <!-- 顶部栏 -->
    <div class="chat-header">
      <div class="header-left">
        <a-button type="text" @click="router.back()">返回</a-button>
        <div class="app-info-header">
          <h1 class="app-name">{{ app?.appName || '加载中...' }}</h1>
        </div>
      </div>
      <div class="header-right">
        <a-button
          type="default"
          @click="viewWork"
          v-if="app && app.deployKey"
        >查看作品</a-button>
        <a-button type="default" @click="goToEdit" v-if="app && isMyApp">编辑</a-button>
        <a-button
          type="primary"
          :loading="loadingDeploy"
          @click="handleDeploy"
          v-if="app && isMyApp"
        >部署</a-button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="chat-main">
      <!-- 左侧对话区 -->
      <div class="chat-panel">
        <div class="chat-messages" v-loading="loadingApp">
          <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
            <div :class="['message', msg.role]">
              <div class="message-avatar">
                <template v-if="msg.role === 'user'">
                  <a-avatar :src="loginUserStore.loginUser.userAvatar" size="small" />
                </template>
                <template v-else>
                  <a-avatar size="small" style="background: #1976d2">AI</a-avatar>
                </template>
              </div>
              <div class="message-content">
                <template v-if="msg.role === 'assistant' && isGenerating && index === messages.length - 1 && !msg.content">
                  <!-- 正在生成且内容为空时，显示 typing 指示器 -->
                  <div class="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </template>
                <template v-else>
                  <div class="message-text">{{ msg.content }}</div>
                </template>
              </div>
            </div>
          </div>
        </div>
        <div class="chat-input-wrapper">
          <div class="chat-input-container">
            <a-tooltip v-if="!isMyApp" title="无法在别人的作品下对话哦~" placement="top">
              <a-textarea
                v-model:value="userInput"
                placeholder="无法在别人的作品下对话哦~"
                :rows="3"
                class="chat-input"
                disabled
              />
            </a-tooltip>
            <template v-else>
              <a-textarea
                v-model:value="userInput"
                placeholder="输入您的需求，继续优化应用..."
                :rows="3"
                @keydown.enter.prevent="!isGenerating && sendMessage()"
                class="chat-input"
                :disabled="isGenerating"
              />
            </template>
            <div class="input-actions">
            <a-button
              v-if="isMyApp && isGenerating"
              type="default"
              danger
              @click="stopGenerate"
            >
              停止
            </a-button>
            <a-button
              v-else-if="isMyApp"
              type="primary"
              :loading="loadingSend"
              @click="sendMessage()"
              :disabled="!userInput.trim()"
            >
              发送
            </a-button>
          </div>
          </div>
        </div>
      </div>

      <!-- 右侧预览区 -->
      <div class="preview-panel">
        <div class="preview-header">
          <span class="preview-title">网站预览</span>
        </div>
        <div class="preview-content">
          <template v-if="showWebsite && websiteUrl">
            <iframe :src="websiteUrl" class="preview-iframe" title="网站预览" />
          </template>
          <template v-else>
            <div class="preview-placeholder">
              <a-empty description="网站生成后将在这里显示" />
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>



<style scoped>
.app-chat-page {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f5f5;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-info-header {
  display: flex;
  flex-direction: column;
}

.app-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-main {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e8e8e8;
  background: #fff;
  min-height: 0;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.message-wrapper {
  margin-bottom: 24px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 90%;
}

.message.user {
  margin-left: auto;
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 100%;
}

.message-text {
  display: inline-block;
  max-width: 100%;
  padding: 12px 16px;
  border-radius: 12px;
  background: #f5f5f5;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.message.user {
  text-align: right;
}

.message.user .message-text {
  background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
  color: #fff;
  text-align: left;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #1976d2;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

.chat-input-wrapper {
  padding: 16px 24px;
  border-top: 1px solid #e8e8e8;
  background: #fff;
}

.chat-input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input {
  flex: 1;
}

.chat-input :deep(.ant-input) {
  border-radius: 20px;
  padding: 10px 16px;
  max-height: 120px;
  overflow-y: auto;
  resize: none;
}

.input-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.preview-panel {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;
}

.preview-header {
  padding: 12px 24px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
}

.preview-title {
  font-size: 14px;
  font-weight: 600;
  color: #666;
}

.preview-content {
  flex: 1;
  overflow: hidden;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

@media (max-width: 992px) {
  .chat-main {
    flex-direction: column;
  }

  .chat-panel {
    border-right: none;
    border-bottom: 1px solid #e8e8e8;
  }

  .chat-panel,
  .preview-panel {
    flex: none;
    height: 50%;
  }
}
</style>
