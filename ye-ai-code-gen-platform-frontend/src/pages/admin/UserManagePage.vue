<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { listUserVoByPage, deleteUser, updateUser } from '@/api/userController.ts'
import type { ColumnsType } from 'ant-design-vue/es/table'
import type { FormInstance } from 'ant-design-vue'

// 数据
const data = ref<API.UserVO[]>([])
const total = ref(0)
const loading = ref(false)

// 编辑相关
const editingKey = ref<string | number>('')
const formRef = ref<FormInstance>()
const formModel = reactive<Record<string, any>>({})

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 表格列定义
const columns: ColumnsType<API.UserVO> = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    key: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 开始编辑
const edit = (record: Partial<API.UserVO>) => {
  editingKey.value = record.id!
  formModel[record.id!] = { ...record }
}

// 取消编辑
const cancel = () => {
  editingKey.value = ''
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listUserVoByPage({
      ...searchParams,
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error) {
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

// 保存编辑
const save = async (key: string | number) => {
  try {
    await formRef.value?.validateFields()
    const row = formModel[key] as API.UserUpdateRequest
    Modal.confirm({
      title: '确认保存',
      content: '您确定要保存修改吗？',
      okText: '确认',
      okType: 'primary',
      cancelText: '取消',
      onOk: async () => {
        try {
          const index = data.value.findIndex((item) => key === item.id)
          const item = data.value[index]
          const res = await updateUser({
            id: item.id,
            userName: row.userName || item.userName,
            userAvatar: row.userAvatar || item.userAvatar,
            userProfile: row.userProfile || item.userProfile,
            userRole: row.userRole || item.userRole,
          })
          if (res.data.code === 0) {
            message.success('保存成功')
            editingKey.value = ''
            fetchData()
          } else {
            message.error('保存失败，' + res.data.message)
          }
        } catch (error) {
          message.error('保存失败')
        }
      },
    })
  } catch (errInfo) {
    console.log('Validate Failed:', errInfo)
  }
}

// 删除数据
const doDelete = (id: string) => {
  if (!id) {
    return
  }
  Modal.confirm({
    title: '确认删除',
    content: '您确定要删除该用户吗？此操作不可恢复。',
    okText: '确认',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteUser({ id })
        if (res.data.code === 0) {
          message.success('删除成功')
          fetchData()
        } else {
          message.error('删除失败，' + res.data.message)
        }
      } catch (error) {
        message.error('删除失败')
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
  <div id="userManagePage" class="user-manage-page">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
    <a-form ref="formRef" :model="formModel">
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
          showTotal: (total) => `共 ${total} 条`,
          pageSizeOptions: ['10', '20', '50', '100'],
        }"
        @change="handlePageChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userAvatar'">
            <template v-if="editingKey === record.id">
              <a-form-item
                :name="[record.id as string, 'userAvatar']"
                style="margin-bottom: 0;"
              >
                <a-input v-model:value="formModel[record.id].userAvatar" />
              </a-form-item>
            </template>
            <template v-else>
              <img v-if="record.userAvatar" :src="record.userAvatar" style="width: 40px; height: 40px; border-radius: 50%;" />
            </template>
          </template>
          <template v-else-if="column.key === 'action'">
            <template v-if="editingKey === record.id">
              <a-space>
                <a-button type="primary" size="small" style="cursor: pointer;" @click="save(record.id)">
                  保存
                </a-button>
                <a-button size="small" style="cursor: pointer;" @click="cancel">
                  取消
                </a-button>
              </a-space>
            </template>
            <template v-else>
              <a-space>
                <a-button type="link" size="small" style="cursor: pointer;" @click="edit(record)">
                  编辑
                </a-button>
                <a-button
                  type="primary"
                  danger
                  size="small"
                  style="cursor: pointer; background-color: #ff4d4f; border-color: #ff4d4f;"
                  @click="doDelete(record.id as string)"
                >
                  删除
                </a-button>
              </a-space>
            </template>
          </template>
          <template v-else-if="['userName', 'userProfile', 'userRole'].includes(column.dataIndex)">
            <template v-if="editingKey === record.id">
              <a-form-item
                :name="[record.id as string, column.dataIndex]"
                :rules="column.dataIndex === 'userRole' ? [{ required: true, message: '必填项' }] : []"
                style="margin-bottom: 0;"
              >
                <a-input v-if="column.dataIndex !== 'userRole'" v-model:value="formModel[record.id][column.dataIndex]" />
                <a-select v-else v-model:value="formModel[record.id][column.dataIndex]" style="width: 100%;">
                  <a-select-option value="user">普通用户</a-select-option>
                  <a-select-option value="admin">管理员</a-select-option>
                </a-select>
              </a-form-item>
            </template>
            <template v-else>
              {{ (record as any)[column.dataIndex] }}
            </template>
          </template>
        </template>
      </a-table>
    </a-form>
  </div>
</template>

<style scoped>
.user-manage-page {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
}
</style>
