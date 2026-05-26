<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController'

const data = ref<API.ChatHistory[]>([])
const total = ref(0)
const loading = ref(false)

const searchParams = reactive<API.ChatHistoryQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const columns = [
  { title: 'ID', dataIndex: 'id', width: 90 },
  { title: '消息内容', dataIndex: 'message' },
  { title: '消息类型', dataIndex: 'messageType', width: 120 },
  { title: '应用 ID', dataIndex: 'appId', width: 120 },
  { title: '用户 ID', dataIndex: 'userId', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAllChatHistoryByPageForAdmin({ ...searchParams })
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error(`获取数据失败：${res.data.message ?? '请稍后重试'}`)
    }
  } finally {
    loading.value = false
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

const resetSearch = () => {
  searchParams.id = undefined
  searchParams.message = undefined
  searchParams.messageType = undefined
  searchParams.appId = undefined
  searchParams.userId = undefined
  doSearch()
}

const isUserMessage = (type?: string) => ['user', 'USER'].includes(type ?? '')
const isAiMessage = (type?: string) => ['ai', 'AI', 'assistant', 'ASSISTANT'].includes(type ?? '')

const getMessageTypeLabel = (type?: string) => {
  if (isUserMessage(type)) {
    return '用户'
  }
  if (isAiMessage(type)) {
    return 'AI'
  }
  return type ?? '-'
}

onMounted(fetchData)
</script>

<template>
  <main id="chatHistoryManagePage">
    <a-card :bordered="false">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="ID">
          <a-input-number v-model:value="searchParams.id" placeholder="对话 ID" />
        </a-form-item>
        <a-form-item label="消息内容">
          <a-input v-model:value="searchParams.message" placeholder="消息内容" allow-clear />
        </a-form-item>
        <a-form-item label="消息类型">
          <a-select
            v-model:value="searchParams.messageType"
            placeholder="消息类型"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="user">用户</a-select-option>
            <a-select-option value="ai">AI</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="应用 ID">
          <a-input v-model:value="searchParams.appId" placeholder="应用 ID" allow-clear />
        </a-form-item>
        <a-form-item label="用户 ID">
          <a-input-number v-model:value="searchParams.userId" placeholder="用户 ID" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">搜索</a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <a-divider />
      <a-table
        row-key="id"
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        :loading="loading"
        @change="doTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'message'">
            <a-typography-paragraph
              class="message-cell"
              :ellipsis="{ rows: 2, expandable: true, symbol: '展开' }"
              :content="record.message || '-'"
            />
          </template>
          <template v-else-if="column.dataIndex === 'messageType'">
            <a-tag :color="isUserMessage(record.messageType) ? 'blue' : 'green'">
              {{ getMessageTypeLabel(record.messageType) }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            {{ record.createTime ? dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
        </template>
      </a-table>
    </a-card>
  </main>
</template>

<style scoped>
#chatHistoryManagePage {
  width: 100%;
  min-height: calc(100vh - 64px);
  padding: 24px;
  background: #f6f8fb;
}

.message-cell {
  max-width: 520px;
  margin-bottom: 0;
  white-space: pre-wrap;
}
</style>
