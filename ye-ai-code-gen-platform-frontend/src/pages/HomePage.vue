<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 提示词输入
const promptInput = ref('')
const loadingCreate = ref(false)

// 我的应用列表
const myApps = ref<API.AppVO[]>([])
const myAppsTotal = ref(0)
const myAppsLoading = ref(false)
const myAppsSearch = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6,
  sortField: 'createTime',
  sortOrder: 'desc',
})

// 精选应用列表
const goodApps = ref<API.AppVO[]>([])
const goodAppsTotal = ref(0)
const goodAppsLoading = ref(false)
const goodAppsSearch = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6,
  sortField: 'createTime',
  sortOrder: 'desc',
})

// 创建应用
const handleCreateApp = async () => {
  if (!promptInput.value.trim()) {
    message.warning('请输入提示词')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  loadingCreate.value = true
  try {
    const res = await addApp({
      appName: '我的应用',
      initPrompt: promptInput.value.trim(),
      codeGenType: 'web',
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('创建成功')
      router.push(`/app/chat/${res.data.data}`)
    } else {
      message.error(res.data.message ?? '创建失败')
    }
  } catch {
    message.error('创建失败')
  } finally {
    loadingCreate.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  myAppsLoading.value = true
  try {
    const res = await listMyAppVoByPage(myAppsSearch)
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records ?? []
      myAppsTotal.value = res.data.data.totalRow ?? 0
    }
  } catch {
    console.error('加载我的应用失败', error)
  } finally {
    myAppsLoading.value = false
  }
}

// 加载精选应用
const loadGoodApps = async () => {
  goodAppsLoading.value = true
  try {
    const res = await listGoodAppVoByPage(goodAppsSearch)
    if (res.data.code === 0 && res.data.data) {
      goodApps.value = res.data.data.records ?? []
      goodAppsTotal.value = res.data.data.totalRow ?? 0
    }
  } catch {
    console.error('加载精选应用失败', error)
  } finally {
    goodAppsLoading.value = false
  }
}

// 进入应用对话页面（查看模式）
const goToApp = (appId: string) => {
  router.push(`/app/chat/${appId}?view=1`)
}

// 查看部署的作品
const viewWork = (app: API.AppVO) => {
  if (!app.deployKey) return
  window.open(`http://localhost/${app.deployKey}`, '_blank')
}

// 我的应用分页
const handleMyAppsPageChange = (page: number) => {
  myAppsSearch.pageNum = page
  loadMyApps()
}

// 精选应用分页
const handleGoodAppsPageChange = (page: number) => {
  goodAppsSearch.pageNum = page
  loadGoodApps()
}

onMounted(() => {
  if (loginUserStore.loginUser.id) {
    loadMyApps()
  }
  loadGoodApps()
})
</script>

<template>
  <div class="home-page">
    <!-- 标题区域 -->
    <div class="hero-section">
      <h1 class="hero-title">一句话，生成你的应用</h1>
      <p class="hero-subtitle">与 AI 对话，快速创建和部署你的 Web 应用</p>

      <!-- 输入框区域 -->
      <div class="input-section">
        <a-textarea
          v-model:value="promptInput"
          placeholder="输入你想要的应用描述，例如：创建一个待办事项应用"
          :rows="4"
          :auto-size="{ minRows: 4, maxRows: 8 }"
          class="prompt-input"
        />
        <div class="input-footer">
          <a-button
            type="primary"
            size="large"
            :loading="loadingCreate"
            @click="handleCreateApp"
            class="create-btn"
          >
            开始创建
          </a-button>
        </div>
      </div>
    </div>

    <div class="content-section">
      <!-- 我的应用 -->
      <section class="app-section" v-if="loginUserStore.loginUser.id">
        <div class="section-header">
          <h2 class="section-title">我的应用</h2>
        </div>
        <div v-if="myAppsLoading" class="loading-wrapper">
          <a-spin size="large" />
        </div>
        <div v-else-if="myApps.length > 0" class="app-grid">
          <div
            v-for="app in myApps"
            :key="app.id"
            class="app-card"
          >
            <div class="app-cover">
              <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
              <div v-else class="app-cover-placeholder">
                <template-icon />
              </div>
              <div class="app-actions">
                <button
                  v-if="app.deployKey"
                  class="action-btn action-btn-view"
                  @click.stop="viewWork(app)"
                >
                  查看作品
                </button>
                <button
                  class="action-btn action-btn-chat"
                  @click.stop="goToApp(app.id as string)"
                >
                  查看对话
                </button>
              </div>
            </div>
            <div class="app-info">
              <h3 class="app-name">{{ app.appName }}</h3>
              <p class="app-time">创建于 {{ app.createTime }}</p>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <a-empty description="暂无应用，输入提示词开始创建吧" />
        </div>
        <div v-if="myAppsTotal > (myAppsSearch.pageSize || 12)" class="pagination-wrapper">
          <a-pagination
            :current="myAppsSearch.pageNum"
            :total="myAppsTotal"
            :page-size="myAppsSearch.pageSize || 12"
            @change="handleMyAppsPageChange"
            :show-less-items="true"
          />
        </div>
      </section>

      <!-- 精选应用 -->
      <section class="app-section">
        <div class="section-header">
          <h2 class="section-title">精选应用</h2>
        </div>
        <div v-if="goodAppsLoading" class="loading-wrapper">
          <a-spin size="large" />
        </div>
        <div v-else-if="goodApps.length > 0" class="app-grid">
          <div
            v-for="app in goodApps"
            :key="app.id"
            class="app-card"
          >
            <div class="app-cover">
              <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
              <div v-else class="app-cover-placeholder">
                <template-icon />
              </div>
              <div class="app-actions">
                <button
                  v-if="app.deployKey"
                  class="action-btn action-btn-view"
                  @click.stop="viewWork(app)"
                >
                  查看作品
                </button>
                <button
                  class="action-btn action-btn-chat"
                  @click.stop="goToApp(app.id as string)"
                >
                  查看对话
                </button>
              </div>
            </div>
            <div class="app-info">
              <h3 class="app-name">{{ app.appName }}</h3>
              <div class="app-meta">
                <span v-if="app.user?.userName" class="app-author">
                  {{ app.user.userName }}
                </span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <a-empty description="暂无精选应用" />
        </div>
        <div v-if="goodAppsTotal > (goodAppsSearch.pageSize || 12)" class="pagination-wrapper">
          <a-pagination
            :current="goodAppsSearch.pageNum"
            :total="goodAppsTotal"
            :page-size="goodAppsSearch.pageSize || 12"
            @change="handleGoodAppsPageChange"
            :show-less-items="true"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, h } from 'vue'
export default defineComponent({
  components: {
    TemplateIcon: () =>
      h(
        'svg',
        {
          width: '48',
          height: '48',
          viewBox: '0 0 48 48',
          fill: 'none',
        },
        [
          h('rect', {
            width: '48',
            height: '48',
            rx: '8',
            fill: '#f5f5f5',
          }),
          h('path', {
            d: 'M14 14h20v20H14V14z',
            stroke: '#d9d9d9',
            'stroke-width': '2',
            'stroke-linecap': 'round',
            'stroke-linejoin': 'round',
          }),
          h('path', {
            d: 'M14 20h20M20 14v20',
            stroke: '#d9d9d9',
            'stroke-width': '2',
            'stroke-linecap': 'round',
          }),
        ],
      ),
  },
})
</script>

<style scoped>
.home-page {
  min-height: 100%;
}

.hero-section {
  text-align: center;
  padding: 64px 24px;
  background: linear-gradient(135deg, #fff5f5 0%, #fff 50%, #f0f9ff 100%);
}

.hero-title {
  font-size: 40px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #d32f2f 0%, #1976d2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 18px;
  color: #666;
  margin-bottom: 40px;
}

.input-section {
  max-width: 800px;
  margin: 0 auto;
}

.prompt-input {
  margin-bottom: 16px;
  font-size: 16px;
  border-radius: 12px;
}

.prompt-input :deep(.ant-input) {
  font-size: 16px;
  padding: 16px;
}

.input-footer {
  display: flex;
  justify-content: flex-end;
}

.create-btn {
  min-width: 160px;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
  background: linear-gradient(135deg, #d32f2f 0%, #ff7043 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(211, 47, 47, 0.3);
}

.create-btn:hover {
  box-shadow: 0 6px 16px rgba(211, 47, 47, 0.4);
}

.content-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

.app-section {
  margin-bottom: 48px;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  padding: 48px 0;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.app-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.app-cover {
  position: relative;
  width: 100%;
  height: 160px;
  background: #f5f5f5;
  overflow: hidden;
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-actions {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  gap: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.app-card:hover .app-actions {
  opacity: 1;
}

.action-btn {
  padding: 8px 24px;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.action-btn-view {
  background: #1a1a1a;
  color: #fff;
}

.action-btn-view:hover {
  background: #333;
}

.action-btn-chat {
  background: #fff;
  color: #1a1a1a;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.action-btn-chat:hover {
  background: #f5f5f5;
}

.app-info {
  padding: 16px;
}

.app-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-time {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.app-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-author {
  font-size: 14px;
  color: #666;
}

.empty-state {
  padding: 48px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .app-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}
</style>
