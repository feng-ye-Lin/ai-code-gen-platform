<script setup lang="ts">
import { ref, onMounted, nextTick, computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { getAppById, deployApp, chatToGenCode, deleteApp } from '@/api/appController'
import { listChatHistoryByPage } from '@/api/chatHistoryController'
import { useLoginUserStore } from '@/stores/loginUser'
import { renderMarkdown } from '@/utils/markdown'
import { CODE_GEN_TYPE_OPTIONS, CodeGenTypeEnum, getCodeGenTypeLabel } from '@/constants/codeGenType'
import request from '@/request'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = computed(() => route.params.appId as string)
const isViewMode = computed(() => route.query.view === '1')
const app = ref<API.AppVO | null>(null)
const loadingApp = ref(false)
const loadingDeploy = ref(false)
const loadingDownload = ref(false)

// 已加载的历史消息（按 createTime 升序排列的历史消息）
const historyMessages = ref<ChatMessage[]>([])
// 游标：下一页使用的 cursor（最早一条历史记录的 id 作为游标）
const nextCursor = ref<string>('')
// 是否还有更多历史消息
const hasMoreHistory = ref(false)
const loadingHistory = ref(false)
// 每页加载的历史消息条数
const HISTORY_PAGE_SIZE = 10

// 是否是自己的作品
const isMyApp = computed(() => {
  if (!app.value?.userId) return true
  return app.value.userId === loginUserStore.loginUser.id
})

// 是否是管理员
const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin'
})

// 是否可操作（本人或管理员）
const canOperate = computed(() => {
  return isMyApp.value || isAdmin.value
})

// 应用详情弹窗
const showDetail = ref(false)

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

// 将后端 ChatHistory 转为前端 ChatMessage
const chatHistoryToMessage = (item: API.ChatHistory): ChatMessage => {
  const msgType = item.messageType ?? ''
  const isUser = msgType.toLowerCase().includes('user')
  return {
    role: isUser ? 'user' : 'assistant',
    content: item.message ?? '',
    timestamp: item.createTime ? new Date(item.createTime).getTime() : undefined,
  }
}

// 加载对话历史
// - 目标展示顺序：上方老消息，下方新消息（old→new）。
// - 请求策略：统一按 createTime desc 请求后端分页接口；
//   首次加载不带 cursor，获取最新一页，前端反转为 old→new。
//   加载「更早的历史」时带 cursor = 当前已加载中最早一条消息的 createTime，
//   期望后端返回比该时间更早的下一页（同样是新→旧顺序），前端反转为 old→new 后插入列表顶部。
// - hasMoreHistory：以 totalRow 为总量上限，只要已加载数量 < 总数就允许继续翻页。
// - 注意：必须在 app.value 已有值之后才执行，避免预览 URL 依赖 app.id / app.codeGenType。
const loadHistory = async (cursor?: string) => {
  if (!appId.value) return
  loadingHistory.value = true
  try {
    const res = await listChatHistoryByPage({
      appId: appId.value,
      pageSize: HISTORY_PAGE_SIZE,
      sortField: 'createTime',
      sortOrder: 'desc',
      cursor: cursor || undefined,
    })
    if (res.data.code === 0 && res.data.data) {
      const rawRecords = res.data.data.records ?? []
      // 后端返回顺序为 new→old（desc），在前端反转为 old→new 的展示顺序
      const orderedRaw = [...rawRecords].reverse()
      const records = orderedRaw.map(chatHistoryToMessage)

      if (cursor) {
        // 更早的历史：插到现有列表顶部
        historyMessages.value = [...records, ...historyMessages.value]
      } else {
        // 首次加载
        historyMessages.value = records
      }

      const total = res.data.data.totalRow ?? 0
      hasMoreHistory.value = historyMessages.value.length < total

      // 下一页游标：当前已加载的最早一条消息的 createTime（rawRecords 最后一个即是本页最老的记录）
      if (rawRecords.length > 0) {
        nextCursor.value = rawRecords[rawRecords.length - 1]?.createTime ?? ''
      }

      // 已有至少 2 条对话历史时展示预览。
      // 注意：调用方需确保调用 loadHistory 时保证 app.value 已就绪，以避免 updateWebsiteUrl 中 app.value 为 null。
      if (historyMessages.value.length >= 2 && app.value) {
        updateWebsiteUrl()
        showWebsite.value = true
      }

      // 首次加载完成后滚动到底部
      if (!cursor) {
        nextTick(() => {
          scrollToBottom()
        })
      }
    } else {
      message.error(res.data.message ?? '加载对话历史失败')
    }
  } catch {
    message.error('加载对话历史失败')
  } finally {
    loadingHistory.value = false
  }
}

// 加载更早的历史消息（按钮位于消息列表顶部）
const loadMoreHistory = async () => {
  if (loadingHistory.value || !hasMoreHistory.value) return
  await loadHistory(nextCursor.value)
}

// 加载应用信息
// 执行顺序：先拉取应用详情 -> 再拉取对话历史 -> 保证处理历史消息时 app.value 已就绪，避免预览 URL 构建时 app 为 null。
const loadApp = async () => {
  if (!appId.value) return
  loadingApp.value = true
  
  try {
    const appRes = await getAppById({ id: appId.value })
    if (appRes.data.code === 0 && appRes.data.data) {
      app.value = appRes.data.data
      // 先保证 app.value 已就绪后再加载历史
      await loadHistory()
      // 再判断是否自动触发 initPrompt 对话：仅当是自己的应用且没有对话历史时
      if (isMyApp.value && app.value.initPrompt && historyMessages.value.length === 0) {
        await nextTick()
        sendMessage(app.value.initPrompt, true)
      }
      // 在查看模式下，自动显示网站预览
      if (isViewMode.value) {
        updateWebsiteUrl()
        showWebsite.value = true
      }
    } else {
      message.error(appRes.data.message ?? '获取应用信息失败')
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
  const baseUrl = import.meta.env.VITE_API_BASE_URL
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
  const previewDomain = import.meta.env.VITE_PREVIEW_DOMAIN
  websiteUrl.value = `${previewDomain}/static/${app.value.codeGenType}_${app.value.id}/`
  if (app.value.codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
    websiteUrl.value += 'dist/index.html'
  }
}

// 从 Content-Disposition 响应头解析文件名
const parseFilenameFromHeader = (contentDisposition: string): string => {
  if (!contentDisposition) return ''
  // 兼容 filename="xxx.zip" 和 filename*=UTF-8''xxx 等形式
  const patterns = [
    /filename\*\s*=\s*UTF-8''([^;]+)/i,
    /filename\s*=\s*"([^"]+)"/i,
    /filename\s*=\s*([^;]+)/i,
  ]
  for (const re of patterns) {
    const match = contentDisposition.match(re)
    if (match && match[1]) {
      const raw = match[1].trim()
      try {
        return decodeURIComponent(raw)
      } catch {
        return raw
      }
    }
  }
  return ''
}

// 从响应头中获取 content-disposition（兼容大小写、Axios Headers 多种形式）
const getContentDisposition = (headers: Record<string, unknown> | undefined): string => {
  if (!headers) return ''
  // 直接索引访问（覆盖普通对象）
  const direct = (headers as Record<string, string>)['content-disposition']
  if (direct) return direct
  // 大小写不敏感查找
  for (const [key, value] of Object.entries(headers as Record<string, string>)) {
    if (key.toLowerCase() === 'content-disposition') return value
  }
  // AxiosHeaders.get 方法兜底
  try {
    const h = headers as { get?: (k: string) => string }
    if (typeof h.get === 'function') {
      return h.get('content-disposition') as string
    }
  } catch {
    /* ignore */
  }
  return ''
}

// 下载应用代码
const handleDownloadCode = async () => {
  if (!app.value) return
  if (!isMyApp.value) {
    message.warning('仅应用所有者可下载代码')
    return
  }
  loadingDownload.value = true
  try {
    const res = await request.get(`/app/download/${app.value.id}`, {
      responseType: 'blob',
    })
    const blob = res.data as Blob
    const contentDisposition = getContentDisposition(res.headers as Record<string, unknown>)
    let filename = parseFilenameFromHeader(contentDisposition) || `${app.value.id || 'app'}.zip`

    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'app_' + filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    message.success('下载成功')
  } catch {
    message.error('下载失败')
  } finally {
    loadingDownload.value = false
  }
}

// 部署应用
const handleDeploy = async () => {
  if (!app.value) return
  loadingDeploy.value = true
  try {
    const res = await deployApp({ appId: app.value.id })
    if (res.data.code === 0 && res.data.data) {
      message.success('部署成功')
      const deployUrl = res.data.data as string
      let modalInstance: ReturnType<typeof Modal.success>
      modalInstance = Modal.success({
        title: '',
        okText: '',
        width: 440,
        icon: null,
        closable: true,
        content: () => {
          return h('div', { style: 'padding: 8px 0 16px;' }, [
            h('div', { style: 'text-align: center; margin-bottom: 24px;' }, [
              h('div', { style: 'width: 60px; height: 60px; margin: 0 auto 18px; border-radius: 50%; background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%); display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 12px rgba(82,196,26,0.25);' }, [
                h('svg', { width: '30', height: '30', viewBox: '0 0 24 24', fill: 'none' }, [
                  h('path', { d: 'M5 13l4 4L19 7', stroke: '#fff', 'stroke-width': '2.5', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' }),
                ]),
              ]),
              h('div', { style: 'font-size: 17px; font-weight: 600; color: #1a1a1a;' }, '网站部署成功！'),
            ]),
            h('div', { style: 'font-size: 14px; color: #888; text-align: center; margin-bottom: 22px;' }, '您的网站已成功部署，可以通过以下链接访问'),
            h('div', { style: 'display: flex; align-items: center; gap: 10px; background: #fafafa; padding: 11px 15px; border-radius: 8px; border: 1px solid #eee;' }, [
              h('span', { style: 'flex: 1; font-size: 13px; word-break: break-all; color: #D32F2F; line-height: 1.5;' }, deployUrl),
              h('button', {
                style: 'flex-shrink: 0; padding: 3px 8px; border: none; border-radius: 6px; background: transparent; cursor: pointer; font-size: 16px; color: #bbb; transition: all 0.2s;',
                onClick: () => { navigator.clipboard.writeText(deployUrl); message.success('已复制到剪贴板') },
                onMouseenter: (e: MouseEvent) => { const el = e.target as HTMLElement; el.style.color = '#D32F2F'; el.style.background = '#FFF5F5'; },
                onMouseleave: (e: MouseEvent) => { const el = e.target as HTMLElement; el.style.color = '#bbb'; el.style.background = 'transparent'; },
              }, '\uD83D\uDCCB'),
            ]),
          ])
        },
        footer: () => {
          return h('div', { style: 'display: flex; justify-content: center; gap: 12px; padding-top: 20px;' }, [
            h('button', {
              style: 'min-width: 90px; padding: 7px 26px; border: none; border-radius: 6px; background: linear-gradient(135deg, #D32F2F 0%, #FF7043 100%); color: #fff; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.25s;',
              onClick: () => { window.open(deployUrl, '_blank'); modalInstance?.destroy() },
              onMouseenter: (e: MouseEvent) => { (e.target as HTMLElement).style.boxShadow = '0 4px 12px rgba(211,47,47,0.35)'; (e.target as HTMLElement).style.transform = 'translateY(-1px)' },
              onMouseleave: (e: MouseEvent) => { (e.target as HTMLElement).style.boxShadow = ''; (e.target as HTMLElement).style.transform = '' },
            }, '访问网站'),
            h('button', {
              style: 'min-width: 70px; padding: 7px 22px; border: 1px solid #ddd; border-radius: 6px; background: #fff; color: #555; font-size: 14px; cursor: pointer; transition: all 0.25s;',
              onClick: () => { modalInstance?.destroy() },
              onMouseenter: (e: MouseEvent) => { const el = e.target as HTMLElement; el.style.borderColor = '#D32F2F'; el.style.color = '#D32F2F'; el.style.background = '#FFF5F5' },
              onMouseleave: (e: MouseEvent) => { const el = e.target as HTMLElement; el.style.borderColor = '#ddd'; el.style.color = '#555'; el.style.background = '#fff' },
            }, '关闭'),
          ])
        },
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
  const deployDomain = import.meta.env.VITE_DEPLOY_DOMAIN
  window.open(`${deployDomain}/${app.value.deployKey}/`, '_blank')
}

// 删除应用
const handleDelete = () => {
  if (!app.value) return
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用「${app.value.appName}」吗？删除后不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteApp({ id: app.value!.id })
        if (res.data.code === 0) {
          message.success('删除成功')
          router.push('/')
        } else {
          message.error(res.data.message ?? '删除失败')
        }
      } catch {
        message.error('删除失败')
      }
    },
  })
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
          <div class="app-name-row">
            <h1 class="app-name">{{ app?.appName || '加载中...' }}</h1>
            <a-tag v-if="app?.codeGenType" color="blue" class="app-type-tag">
              {{ getCodeGenTypeLabel(app.codeGenType) }}
            </a-tag>
          </div>
        </div>
      </div>
      <div class="header-right">
        <a-button
          type="default"
          @click="viewWork"
          v-if="app && app.deployKey"
        >查看作品</a-button>
        <a-button
          type="default"
          @click="showDetail = true"
          v-if="app"
        >应用详情</a-button>
        <a-button type="default" @click="goToEdit" v-if="app && isMyApp">编辑</a-button>
        <a-tooltip title="下载代码" placement="top" v-if="app && isMyApp">
          <a-button
            type="default"
            :disabled="!isMyApp"
            :loading="loadingDownload"
            @click="handleDownloadCode"
          >
            <template #icon>
              <svg viewBox="64 64 896 896" width="1em" height="1em" fill="currentColor" focusable="false" aria-hidden="true">
                <path d="M505.7 661a8 8 0 0 0 12.6 0l112-141.7c4.1-5.2.4-12.9-6.3-12.9h-74.1V168c0-4.4-3.6-8-8-8h-60c-4.4 0-8 3.6-8 8v338.3H400c-6.7 0-10.4 7.7-6.3 12.9l112 141.8zM878 626h-60c-4.4 0-8 3.6-8 8v154H214V634c0-4.4-3.6-8-8-8h-60c-4.4 0-8 3.6-8 8v198c0 17.7 14.3 32 32 32h684c17.7 0 32-14.3 32-32V634c0-4.4-3.6-8-8-8z" />
              </svg>
            </template>
          </a-button>
        </a-tooltip>
        <a-button
          type="default"
          :disabled="true"
          v-else-if="app && !isMyApp"
        >
          <template #icon>
            <svg viewBox="64 64 896 896" width="1em" height="1em" fill="currentColor" focusable="false" aria-hidden="true">
              <path d="M505.7 661a8 8 0 0 0 12.6 0l112-141.7c4.1-5.2.4-12.9-6.3-12.9h-74.1V168c0-4.4-3.6-8-8-8h-60c-4.4 0-8 3.6-8 8v338.3H400c-6.7 0-10.4 7.7-6.3 12.9l112 141.8zM878 626h-60c-4.4 0-8 3.6-8 8v154H214V634c0-4.4-3.6-8-8-8h-60c-4.4 0-8 3.6-8 8v198c0 17.7 14.3 32 32 32h684c17.7 0 32-14.3 32-32V634c0-4.4-3.6-8-8-8z" />
            </svg>
          </template>
        </a-button>
        <a-button
          type="primary"
          :loading="loadingDeploy"
          @click="handleDeploy"
          v-if="app && isMyApp"
        >部署</a-button>
      </div>
    </div>

    <!-- 应用详情弹窗 -->
    <a-modal
      v-model:open="showDetail"
      title="应用详情"
      :footer="null"
      centered
      width="400px"
    >
      <div class="app-detail-panel" v-if="app">
        <div class="detail-section">
          <div class="detail-creator">
            <a-avatar :src="app.user?.userAvatar" size="small" />
            <span class="creator-name">{{ app.user?.userName || '未知用户' }}</span>
          </div>
          <div class="detail-time">{{ app.createTime }}</div>
        </div>
        <div class="detail-section" v-if="app.codeGenType">
          <div class="detail-item">
            <span class="detail-label">生成类型</span>
            <a-tag color="blue" class="detail-value">
              {{ getCodeGenTypeLabel(app.codeGenType) }}
            </a-tag>
          </div>
        </div>
        <div class="detail-actions" v-if="canOperate">
          <a-button type="default" @click="goToEdit(); showDetail = false">
            修改
          </a-button>
          <a-button type="primary" danger @click="handleDelete(); showDetail = false">
            删除
          </a-button>
        </div>
      </div>
    </a-modal>

    <!-- 主内容区 -->
    <div class="chat-main">
      <!-- 左侧对话区 -->
      <div class="chat-panel">
        <div class="chat-messages" v-loading="loadingApp">
          <!-- 加载更多历史消息按钮 -->
          <div class="load-more-wrapper" v-if="historyMessages.length > 0 || hasMoreHistory">
            <a-button
              v-if="hasMoreHistory"
              type="link"
              size="small"
              :loading="loadingHistory"
              @click="loadMoreHistory"
            >加载更多历史消息</a-button>
            <span v-else-if="historyMessages.length > 0" class="no-more-text">暂无更早的对话</span>
          </div>
          <div v-for="(msg, index) in historyMessages" :key="'h-' + index" class="message-wrapper">
            <div :class="['message', msg.role]">
              <div class="message-avatar">
                <template v-if="msg.role === 'user'">
                  <a-avatar :src="loginUserStore.loginUser.userAvatar" size="small" />
                </template>
                <template v-else>
                  <a-avatar size="small" class="ai-avatar">
                    <template #icon>
                      <svg viewBox="0 0 200 200" width="20" height="20">
                        <defs>
                          <linearGradient id="aiAvatarGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" style="stop-color:#D32F2F" />
                            <stop offset="100%" style="stop-color:#FF7043" />
                          </linearGradient>
                        </defs>
                        <path d="M100 30 L120 50 L145 45 L135 70 L160 80 L140 100 L160 120 L135 130 L145 155 L120 150 L100 170 L80 150 L55 155 L65 130 L40 120 L60 100 L40 80 L65 70 L55 45 L80 50 Z" fill="url(#aiAvatarGrad)" stroke="#C62828" stroke-width="2"/>
                      </svg>
                    </template>
                  </a-avatar>
                </template>
              </div>
              <div class="message-content">
                <div
                  v-if="msg.role === 'assistant'"
                  class="message-text markdown-body"
                  v-html="renderMarkdown(msg.content)"
                ></div>
                <div v-else class="message-text">{{ msg.content }}</div>
              </div>
            </div>
          </div>
          <div v-for="(msg, index) in messages" :key="'c-' + index" class="message-wrapper">
            <div :class="['message', msg.role]">
              <div class="message-avatar">
                <template v-if="msg.role === 'user'">
                  <a-avatar :src="loginUserStore.loginUser.userAvatar" size="small" />
                </template>
                <template v-else>
                  <a-avatar size="small" class="ai-avatar">
                    <template #icon>
                      <svg viewBox="0 0 200 200" width="20" height="20">
                        <defs>
                          <linearGradient id="aiAvatarGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" style="stop-color:#D32F2F" />
                            <stop offset="100%" style="stop-color:#FF7043" />
                          </linearGradient>
                        </defs>
                        <path d="M100 30 L120 50 L145 45 L135 70 L160 80 L140 100 L160 120 L135 130 L145 155 L120 150 L100 170 L80 150 L55 155 L65 130 L40 120 L60 100 L40 80 L65 70 L55 45 L80 50 Z" fill="url(#aiAvatarGrad)" stroke="#C62828" stroke-width="2"/>
                      </svg>
                    </template>
                  </a-avatar>
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
                  <div
                    v-if="msg.role === 'assistant'"
                    class="message-text markdown-body"
                    v-html="renderMarkdown(msg.content)"
                  ></div>
                  <div v-else class="message-text">{{ msg.content }}</div>
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
                placeholder="请描述你想生成的网站，越详细效果越好哦"
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

.app-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.app-type-tag {
  font-size: 12px;
  font-weight: 500;
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
  flex: 2;
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
  padding: 16px;
}

.load-more-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.no-more-text {
  font-size: 12px;
  color: #999;
}

.message-wrapper {
  margin-bottom: 16px;
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

.ai-avatar {
  background: linear-gradient(135deg, #D32F2F 0%, #FF7043 100%) !important;
  display: flex;
  align-items: center;
  justify-content: center;
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
  background: linear-gradient(135deg, #D32F2F 0%, #FF7043 100%);
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
  background: #D32F2F;
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
  padding: 12px 16px;
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
  flex: 3;
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

<style>
/* 部署成功弹窗 - 修正左右对称 */
.ant-modal .ant-modal-body {
  padding: 24px !important;
}

.ant-modal .ant-modal-footer {
  padding: 0 24px 20px !important;
}

.app-detail-panel {
  padding: 8px 0;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-creator {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.creator-name {
  font-size: 15px;
  font-weight: 500;
  color: #1a1a1a;
}

.detail-time {
  font-size: 13px;
  color: #999;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-label {
  font-size: 14px;
  color: #666;
}

.detail-value {
  font-size: 13px;
  font-weight: 500;
}

.detail-actions {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* Markdown 渲染样式 */
.markdown-body {
  font-size: 14px;
  line-height: 1.7;
  color: #333;
  word-break: break-word;
}

.markdown-body p {
  margin: 0 0 10px;
}

.markdown-body p:last-child {
  margin-bottom: 0;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4 {
  margin: 16px 0 8px;
  font-weight: 600;
  line-height: 1.4;
}

.markdown-body h1 { font-size: 20px; }
.markdown-body h2 { font-size: 18px; }
.markdown-body h3 { font-size: 16px; }
.markdown-body h4 { font-size: 15px; }

.markdown-body ul,
.markdown-body ol {
  margin: 8px 0;
  padding-left: 24px;
}

.markdown-body li {
  margin: 4px 0;
}

.markdown-body blockquote {
  margin: 8px 0;
  padding: 8px 16px;
  border-left: 4px solid #D32F2F;
  background: #FFF5F5;
  color: #555;
  border-radius: 0 6px 6px 0;
}

.markdown-body code {
  padding: 2px 6px;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  color: #d63384;
}

.markdown-body pre {
  margin: 10px 0;
  padding: 0;
  background: #1e1e2e;
  border-radius: 8px;
  overflow: hidden;
}

.markdown-body pre code {
  display: block;
  padding: 14px 18px;
  background: transparent;
  color: #cdd6f4;
  font-size: 13px;
  line-height: 1.6;
  overflow-x: auto;
  border-radius: 0;
}

.markdown-body table {
  margin: 10px 0;
  border-collapse: collapse;
  width: 100%;
}

.markdown-body th,
.markdown-body td {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  text-align: left;
}

.markdown-body th {
  background: #f5f5f5;
  font-weight: 600;
}

.markdown-body hr {
  margin: 16px 0;
  border: none;
  border-top: 1px solid #e8e8e8;
}

.markdown-body a {
  color: #D32F2F;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}

.markdown-body img {
  max-width: 100%;
  border-radius: 6px;
}

/* highlight.js 代码高亮 - Catppuccin Mocha 风格 */
.markdown-body pre code .hljs-keyword { color: #cba6f7; }
.markdown-body pre code .hljs-built_in { color: #f38ba8; }
.markdown-body pre code .hljs-type { color: #fab387; }
.markdown-body pre code .hljs-literal { color: #fab387; }
.markdown-body pre code .hljs-number { color: #fab387; }
.markdown-body pre code .hljs-string { color: #a6e3a1; }
.markdown-body pre code .hljs-regexp { color: #f5e0dc; }
.markdown-body pre code .hljs-symbol { color: #f5e0dc; }
.markdown-body pre code .hljs-class { color: #f9e2af; }
.markdown-body pre code .hljs-function { color: #89b4fa; }
.markdown-body pre code .hljs-title { color: #89b4fa; }
.markdown-body pre code .hljs-params { color: #cdd6f4; }
.markdown-body pre code .hljs-comment { color: #6c7086; font-style: italic; }
.markdown-body pre code .hljs-doctag { color: #f38ba8; }
.markdown-body pre code .hljs-meta { color: #fab387; }
.markdown-body pre code .hljs-section { color: #89b4fa; }
.markdown-body pre code .hljs-tag { color: #cba6f7; }
.markdown-body pre code .hljs-name { color: #cba6f7; }
.markdown-body pre code .hljs-attr { color: #89b4fa; }
.markdown-body pre code .hljs-attribute { color: #a6e3a1; }
.markdown-body pre code .hljs-variable { color: #cdd6f4; }
.markdown-body pre code .hljs-bullet { color: #f5e0dc; }
.markdown-body pre code .hljs-code { color: #a6e3a1; }
.markdown-body pre code .hljs-emphasis { font-style: italic; }
.markdown-body pre code .hljs-strong { font-weight: bold; }
.markdown-body pre code .hljs-formula { color: #cdd6f4; }
.markdown-body pre code .hljs-link { color: #89b4fa; text-decoration: underline; }
.markdown-body pre code .hljs-quote { color: #a6e3a1; }
.markdown-body pre code .hljs-selector-tag { color: #f38ba8; }
.markdown-body pre code .hljs-selector-id { color: #89b4fa; }
.markdown-body pre code .hljs-selector-class { color: #a6e3a1; }
.markdown-body pre code .hljs-selector-attr { color: #cba6f7; }
.markdown-body pre code .hljs-selector-pseudo { color: #f5e0dc; }
.markdown-body pre code .hljs-template-tag { color: #cba6f7; }
.markdown-body pre code .hljs-template-variable { color: #cdd6f4; }
.markdown-body pre code .hljs-addition { color: #a6e3a1; background: rgba(166, 227, 161, 0.1); }
.markdown-body pre code .hljs-deletion { color: #f38ba8; background: rgba(243, 139, 168, 0.1); }
</style>
