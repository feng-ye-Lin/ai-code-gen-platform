<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  listAppVoByPageByAdmin,
  deleteAppByAdmin,
  updateAppByAdmin,
} from '@/api/appController'
import type { ColumnsType } from 'ant-design-vue/es/table'

const router = useRouter()

// 数据
const data = ref<API.AppVO[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索条件
const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'desc',
})

// 表格列定义
const columns: ColumnsType<API.AppVO> = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
  },
  {
    title: '封面',
    dataIndex: 'cover',
    width: 100,
    key: 'cover',
  },
  {
    title: '类型',
    dataIndex: 'codeGenType',
    width: 100,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 100,
  },
  {
    title: '创建者',
    dataIndex: ['user', 'userName'],
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 220,
    fixed: 'right',
  },
]

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin(searchParams)
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
  searchParams.appName = undefined
  searchParams.pageNum = 1
  fetchData()
}

// 编辑
const goToEdit = (record: API.AppVO) => {
  router.push(`/app/edit/${record.id}`)
}

// 查看详情
const goToDetail = (record: API.AppVO) => {
  router.push(`/app/chat/${record.id}`)
}

// 删除
const doDelete = (record: API.AppVO) => {
  Modal.confirm({
    title: '确认删除',
    content: `您确定要删除应用「${record.appName}」吗？此操作不可恢复。`,
    okText: '确认',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteAppByAdmin({ id: record.id })
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

// 设为精选
const setFeatured = async (record: API.AppVO) => {
  Modal.confirm({
    title: '设为精选',
    content: `您确定要将应用「${record.appName}」设为精选吗？`,
    okText: '确认',
    okType: 'primary',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await updateAppByAdmin({
          id: record.id,
          priority: 99,
        })
        if (res.data.code === 0) {
          message.success('设置成功')
          fetchData()
        } else {
          message.error('设置失败，' + res.data.message)
        }
      } catch {
        message.error('设置失败')
      }
    },
  })
}

// 取消精选
const cancelFeatured = async (record: API.AppVO) => {
  Modal.confirm({
    title: '取消精选',
    content: `您确定要取消应用「${record.appName}」的精选状态吗？`,
    okText: '确认',
    okType: 'primary',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await updateAppByAdmin({
          id: record.id,
          priority: 0,
        })
        if (res.data.code === 0) {
          message.success('取消成功')
          fetchData()
        } else {
          message.error('取消失败，' + res.data.message)
        }
      } catch {
        message.error('取消失败')
      }
    },
  })
}

// 分页变化
const handlePageChange = (page: number, pageSize: number) => {
  searchParams.pageNum = page
  searchParams.pageSize = pageSize
  fetchData()
}
</script>

<template>
  <div class="app-manage-page">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
      <a-form-item label="应用名称">
        <a-input
          v-model:value="searchParams.appName"
          placeholder="输入应用名称"
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
        <template v-if="column.key === 'cover'">
          <img v-if="record.cover" :src="record.cover" style="width: 48px; height: 48px; object-fit: cover; border-radius: 4px;" />
          <span v-else>-</span>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="goToDetail(record)">
              查看
            </a-button>
            <a-button type="link" size="small" @click="goToEdit(record)">
              编辑
            </a-button>
            <template v-if="record.priority === 99">
              <a-button type="link" size="small" @click="cancelFeatured(record)">
                取消精选
              </a-button>
            </template>
            <template v-else>
              <a-button type="link" size="small" @click="setFeatured(record)">
                设为精选
              </a-button>
            </template>
            <a-popconfirm
              title="确认删除？"
              ok-text="确认"
              cancel-text="取消"
              @confirm="doDelete(record)"
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
.app-manage-page {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
}

.search-form {
  align-items: flex-end;
}
</style>
