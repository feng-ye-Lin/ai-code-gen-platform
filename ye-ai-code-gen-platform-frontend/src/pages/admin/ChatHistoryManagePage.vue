<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  listChatHistoryByAdmin,
  deleteByAppIdByAdmin,
} from '@/api/chatHistoryController'
import type { ColumnsType } from 'ant-design-vue/es/table'

const router = useRouter()

// 数据
const data = ref<API.ChatHistory[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索条件
const searchParams = reactive<API.ChatHistoryQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'desc',
})

// 表格列定义
const columns: ColumnsType<API.ChatHistory> = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '应用 ID',
    dataIndex: 'appId',
    width: 100,
  },
  {
    title: '用户 ID',
    dataIndex: 'userId',
    width: 100,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    width: 120,
  },
  {
    title: '消息内容',
    dataIndex: 'message',
    ellipsis: true,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
  },
]

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listChatHistoryByAdmin({
      ...searchParams,
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch {
    message.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 搜索
const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  searchParams.appId = undefined
  searchParams.userId = undefined
  searchParams.messageType = undefined
  searchParams.pageNum = 1
  fetchData()
}

// 查看应用对话详情
const goToDetail = (record: API.ChatHistory) => {
  if (record.appId) {
    router.push(`/app/chat/${record.appId}`)
  }
}

// 删除（按 appId 删除全部对话记录）
const doDeleteByAppId = (record: API.ChatHistory) => {
  if (!record.appId) return
  Modal.confirm({
    title: '确认删除',
    content: `您确定要删除应用「${record.appId}」的全部对话记录吗？此操作不可恢复。`,
    okText: '确认',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteByAppIdByAdmin({ appId: record.appId as number })
        if (res.data.code === 0) {
          message.success('删除成功')
          fetchData()
        } else {
          message.error('删除失败，' + res.data.message)
        }
      } catch {
        message.error('删除失败')
      }
    },
  })
}

// 分页变化
const handlePageChange = (pagination: any) => {
  searchParams.pageNum = pagination.current
  searchParams.pageSize = pagination.pageSize
  fetchData()
}
</script>

<template>
  <div class="chat-history-manage-page">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
      <a-form-item label="应用 ID">
        <a-input-number
          v-model:value="searchParams.appId"
          placeholder="输入应用 ID"
          :min="1"
          style="width: 160px"
        />
      </a-form-item>
      <a-form-item label="用户 ID">
        <a-input-number
          v-model:value="searchParams.userId"
          placeholder="输入用户 ID"
          :min="1"
          style="width: 160px"
        />
      </a-form-item>
      <a-form-item label="消息类型">
        <a-input
          v-model:value="searchParams.messageType"
          placeholder="输入消息类型"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit">搜索</a-button>
          <a-button @click="resetSearch">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :loading="loading"
      :pagination="{
        current: searchParams.pageNum,
        pageSize: searchParams.pageSize,
        total: total,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total: number) => `共 ${total} 条`,
        pageSizeOptions: ['10', '20', '50', '100'],
      }"
      @change="handlePageChange"
      row-key="id"
      :scroll="{ x: 1200 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="goToDetail(record)">
              查看对话
            </a-button>
            <a-popconfirm
              title="确认删除该应用的全部对话记录？"
              ok-text="确认"
              cancel-text="取消"
              @confirm="doDeleteByAppId(record)"
            >
              <a-button type="link" size="small" danger>
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped>
.chat-history-manage-page {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
}

.search-form {
  align-items: flex-end;
}
</style>