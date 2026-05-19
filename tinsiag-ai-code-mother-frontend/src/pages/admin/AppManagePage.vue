<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { deleteAppByAdmin, listAppVoByPageByAdmin, updateAppByAdmin } from '@/api/appController'
import AppDetailModal from '@/components/app/AppDetailModal.vue'
import { getAppPreviewUrl } from '@/config/domain'
import { getAppCover } from '@/utils/appDisplay'

const router = useRouter()
const data = ref<API.AppVO[]>([])
const total = ref(0)
const loading = ref(false)
const detailOpen = ref(false)
const detailApp = ref<API.AppVO>()

const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const columns = [
  { title: 'ID', dataIndex: 'id', width: 90 },
  { title: '应用名称', dataIndex: 'appName' },
  { title: '封面', dataIndex: 'cover', width: 140 },
  { title: '生成类型', dataIndex: 'codeGenType', width: 120 },
  { title: '优先级', dataIndex: 'priority', width: 100 },
  { title: '创建用户', dataIndex: 'user', width: 140 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 260 },
]

const staticUrlOf = (record: API.AppVO) => {
  if (!record.id || !record.codeGenType) {
    return ''
  }
  return getAppPreviewUrl(record.codeGenType, record.id)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin({ ...searchParams })
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
  searchParams.appName = undefined
  searchParams.userId = undefined
  searchParams.priority = undefined
  doSearch()
}

const doDelete = async (id?: API.AppId) => {
  if (!id) {
    return
  }
  const res = await deleteAppByAdmin({ id })
  if (res.data.code === 0 && res.data.data) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error(`删除失败：${res.data.message ?? '请稍后重试'}`)
  }
}

const toggleGood = async (record: API.AppVO) => {
  if (!record.id) {
    return
  }
  const isGood = record.priority === 99
  const res = await updateAppByAdmin({
    id: record.id,
    appName: record.appName,
    cover: record.cover,
    priority: isGood ? 0 : 99,
  })
  if (res.data.code === 0 && res.data.data) {
    message.success(isGood ? '已取消精选' : '已设为精选')
    fetchData()
  } else {
    message.error(`设置失败：${res.data.message ?? '请稍后重试'}`)
  }
}

const showDetail = (record: API.AppVO) => {
  detailApp.value = record
  detailOpen.value = true
}

onMounted(fetchData)
</script>

<template>
  <main id="appManagePage">
    <a-card :bordered="false">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="ID">
          <a-input-number v-model:value="searchParams.id" placeholder="应用 ID" />
        </a-form-item>
        <a-form-item label="名称">
          <a-input v-model:value="searchParams.appName" placeholder="应用名称" allow-clear />
        </a-form-item>
        <a-form-item label="用户 ID">
          <a-input-number v-model:value="searchParams.userId" placeholder="用户 ID" />
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number v-model:value="searchParams.priority" placeholder="优先级" />
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
          <template v-if="column.dataIndex === 'cover'">
            <a-image
              :src="record.cover || staticUrlOf(record) || getAppCover(record)"
              :width="108"
              :height="64"
              class="cover-image"
            />
          </template>
          <template v-else-if="column.dataIndex === 'priority'">
            <a-tag :color="record.priority === 99 ? 'gold' : 'default'">
              {{ record.priority ?? 0 }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'user'">
            {{ record.user?.userName ?? record.userId ?? '-' }}
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            {{ record.createTime ? dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="showDetail(record)">详情</a-button>
              <a-button type="link" @click="router.push(`/app/edit/${record.id}`)">编辑</a-button>
              <a-button type="link" @click="toggleGood(record)">
                {{ record.priority === 99 ? '取消精选' : '精选' }}
              </a-button>
              <a-popconfirm title="确定删除该应用？" @confirm="doDelete(record.id)">
                <a-button type="link" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <AppDetailModal
      v-model:open="detailOpen"
      :app="detailApp"
      @edit="(app) => router.push(`/app/edit/${app.id}`)"
    />
  </main>
</template>

<style scoped>
#appManagePage {
  width: 100%;
  min-height: calc(100vh - 64px);
  padding: 24px;
  background: #f6f8fb;
}

.cover-image {
  overflow: hidden;
  object-fit: cover;
  border-radius: 6px;
}
</style>
