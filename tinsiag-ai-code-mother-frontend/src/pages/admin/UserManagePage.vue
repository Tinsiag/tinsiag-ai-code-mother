<template>
  <main id="userManagePage">
    <a-card :bordered="false">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="账号">
          <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" allow-clear />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input v-model:value="searchParams.userName" placeholder="输入用户名" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>
      <a-divider />
      <a-table
        row-key="id"
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        @change="doTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'userAvatar'">
            <a-avatar v-if="record.userAvatar" :src="record.userAvatar" :size="48" />
            <span v-else>-</span>
          </template>

          <template v-if="column.dataIndex === 'userRole'">
            <a-tag :color="record.userRole === 'admin' ? 'green' : 'blue'">
              {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
            </a-tag>
          </template>

          <template v-if="column.dataIndex === 'createTime'">
            {{ record.createTime ? dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
          <template v-if="column.key === 'action'">
            <a-popconfirm title="确定删除该用户？" @confirm="doDelete(record.id)">
              <a-button danger>删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>
  </main>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { deleteUser, listUserVoByPage } from '@/api/userController'

const columns = [
  { title: 'ID', dataIndex: 'id' },
  { title: '账号', dataIndex: 'userAccount' },
  { title: '用户名', dataIndex: 'userName' },
  { title: '头像', dataIndex: 'userAvatar' },
  { title: '简介', dataIndex: 'userProfile' },
  { title: '用户角色', dataIndex: 'userRole' },
  { title: '创建时间', dataIndex: 'createTime' },
  { title: '操作', key: 'action' },
]

const data = ref<API.UserVO[]>([])
const total = ref(0)
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

const fetchData = async () => {
  const res = await listUserVoByPage({ ...searchParams })
  if (res.data.code === 0 && res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error(`获取数据失败，${res.data.message ?? '请稍后重试'}`)
  }
}

const pagination = computed(() => ({
  current: searchParams.pageNum ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (value: number) => `共 ${value} 条数据`,
}))

const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

const doDelete = async (id?: number) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0 && res.data.data) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error(`删除失败，${res.data.message ?? '请稍后重试'}`)
  }
}

onMounted(fetchData)
</script>

<style scoped>
#userManagePage {
  width: 100%;
  min-height: calc(100vh - 64px);
  padding: 24px;
  background: #f6f8fb;
}
</style>
