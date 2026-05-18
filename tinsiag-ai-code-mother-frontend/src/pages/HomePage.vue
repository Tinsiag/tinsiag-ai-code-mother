<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowUpOutlined,
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  PaperClipOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons-vue'
import { addApp, deleteApp, listGoodAppVoByPage, listMyAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/LoginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const prompt = ref('')
const creating = ref(false)
const myApps = ref<API.AppVO[]>([])
const goodApps = ref<API.AppVO[]>([])
const myTotal = ref(0)
const goodTotal = ref(0)
const myLoading = ref(false)
const goodLoading = ref(false)

const mySearch = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const goodSearch = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: 6,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const examples = ['波普风电商页面', '企业网站', '电商运营后台', '暗黑话题社区']



const appCover = (app: API.AppVO) => app.cover || 'public/default.png'

const formatTime = (value?: string) => {
  if (!value) {
    return '刚刚'
  }
  const diff = Date.now() - new Date(value).getTime()
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  if (diff < hour) {
    return `${Math.max(1, Math.floor(diff / minute))} 分钟前`
  }
  if (diff < day) {
    return `${Math.floor(diff / hour)} 小时前`
  }
  return `${Math.floor(diff / day)} 天前`
}

const fetchMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    myApps.value = []
    myTotal.value = 0
    return
  }
  myLoading.value = true
  try {
    const res = await listMyAppVoByPage({
      ...mySearch,
      pageSize: Math.min(mySearch.pageSize ?? 6, 20),
    })
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records ?? []
      myTotal.value = res.data.data.totalRow ?? 0
    }
  } finally {
    myLoading.value = false
  }
}

const fetchGoodApps = async () => {
  goodLoading.value = true
  try {
    const res = await listGoodAppVoByPage({
      ...goodSearch,
      pageSize: Math.min(goodSearch.pageSize ?? 6, 20),
    })
    if (res.data.code === 0 && res.data.data) {
      goodApps.value = res.data.data.records ?? []
      goodTotal.value = res.data.data.totalRow ?? 0
    }
  } finally {
    goodLoading.value = false
  }
}

const createApp = async () => {
  const initPrompt = prompt.value.trim()
  if (!initPrompt) {
    message.warning('先描述你想创建的应用')
    return
  }
  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录后再创建应用')
    await router.push(`/user/login?redirect=${encodeURIComponent('/')}`)
    return
  }
  creating.value = true
  try {
    const res = await addApp({ initPrompt })
    if (res.data.code === 0 && res.data.data) {
      await router.push({
        path: `/app/chat/${res.data.data}`,
        query: { prompt: initPrompt, auto: '1' },
      })
    } else {
      message.error(`创建失败，${res.data.message ?? '请稍后重试'}`)
    }
  } finally {
    creating.value = false
  }
}

const openApp = (app: API.AppVO) => {
  if (app.id) {
    router.push(`/app/chat/${app.id}`)
  }
}

const editApp = (app: API.AppVO) => {
  if (app.id) {
    router.push(`/app/edit/${app.id}`)
  }
}

const removeApp = async (app: API.AppVO) => {
  if (!app.id) {
    return
  }
  const res = await deleteApp({ id: app.id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchMyApps()
  } else {
    message.error(`删除失败，${res.data.message ?? '请稍后重试'}`)
  }
}

const paginationFactory = (
  total: typeof myTotal,
  params: API.AppQueryRequest,
  fetcher: () => void,
) =>
  computed(() => ({
    current: params.pageNum,
    pageSize: params.pageSize,
    total: total.value,
    showSizeChanger: false,
    onChange: (page: number) => {
      params.pageNum = page
      fetcher()
    },
  }))

const myPagination = paginationFactory(myTotal, mySearch, fetchMyApps)
const goodPagination = paginationFactory(goodTotal, goodSearch, fetchGoodApps)

onMounted(() => {
  fetchMyApps()
  fetchGoodApps()
})
</script>

<template>
  <main class="home-page">
    <section class="hero-section">
      <div class="brand-title">
        <span>一句话</span>
        <img src="/favicon.ico" alt="logo" />
        <span>呈所想</span>
      </div>
      <p class="subtitle">与 AI 对话轻松创建应用和网站</p>

      <div class="prompt-box">
        <a-textarea
          v-model:value="prompt"
          :bordered="false"
          :auto-size="{ minRows: 4, maxRows: 6 }"
          placeholder="使用 NoCode 创建一个高效的小工具，帮我计算......"
          @pressEnter.ctrl="createApp"
        />
        <div class="prompt-actions">
          <a-space>
            <a-button shape="round">
              <template #icon><PaperClipOutlined /></template>
              上传
            </a-button>
            <a-button shape="round" disabled>
              <template #icon><ThunderboltOutlined /></template>
              优化
            </a-button>
          </a-space>
          <a-button
            type="primary"
            shape="circle"
            size="large"
            :loading="creating"
            @click="createApp"
          >
            <template #icon><ArrowUpOutlined /></template>
          </a-button>
        </div>
      </div>

      <a-space class="example-tags" wrap>
        <a-button v-for="item in examples" :key="item" shape="round" @click="prompt = item">
          {{ item }}
        </a-button>
      </a-space>
    </section>

    <section class="gallery-panel">
      <div class="section-head">
        <h2>我的作品</h2>
        <a-input-search
          v-model:value="mySearch.appName"
          class="search-input"
          placeholder="搜索我的应用"
          allow-clear
          @search="
            () => {
              mySearch.pageNum = 1
              fetchMyApps()
            }
          "
        />
      </div>
      <a-empty v-if="!loginUserStore.loginUser.id" description="登录后查看我的作品" />
      <a-list
        v-else
        :loading="myLoading"
        :data-source="myApps"
        :grid="{ gutter: 28, xs: 1, sm: 1, md: 2, lg: 3, xl: 3 }"
        :pagination="myPagination"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <article class="app-card" @click="openApp(item)">
              <div class="cover-wrap">
                <img v-if="appCover(item)" :src="appCover(item)" :alt="item.appName" />
                <div v-else class="cover-placeholder">
                  <img src="/favicon.ico" alt="" />
                </div>
                <div class="card-tools" @click.stop>
                  <a-tooltip title="查看">
                    <a-button shape="circle" @click="openApp(item)">
                      <template #icon><EyeOutlined /></template>
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="编辑">
                    <a-button shape="circle" @click="editApp(item)">
                      <template #icon><EditOutlined /></template>
                    </a-button>
                  </a-tooltip>
                  <a-popconfirm title="确定删除该应用？" @confirm="removeApp(item)">
                    <a-button danger shape="circle">
                      <template #icon><DeleteOutlined /></template>
                    </a-button>
                  </a-popconfirm>
                </div>
              </div>
              <h3>{{ item.appName || item.initPrompt || '未命名应用' }}</h3>
              <p>创建于 {{ formatTime(item.createTime) }}</p>
            </article>
          </a-list-item>
        </template>
      </a-list>

      <div class="section-head featured-head">
        <h2>精选案例</h2>
        <a-input-search
          v-model:value="goodSearch.appName"
          class="search-input"
          placeholder="搜索精选应用"
          allow-clear
          @search="
            () => {
              goodSearch.pageNum = 1
              fetchGoodApps()
            }
          "
        />
      </div>
      <a-list
        :loading="goodLoading"
        :data-source="goodApps"
        :grid="{ gutter: 28, xs: 1, sm: 1, md: 2, lg: 3, xl: 3 }"
        :pagination="goodPagination"
      >
        <template #renderItem="{ item }">
          <a-list-item>
            <article class="app-card featured-card" @click="openApp(item)">
              <div class="cover-wrap">
                <img v-if="appCover(item)" :src="appCover(item)" :alt="item.appName" />
                <div v-else class="cover-placeholder">
                  <img src="/favicon.ico" alt="" />
                </div>
              </div>
              <div class="featured-meta">
                <a-avatar :src="item.user?.userAvatar">
                  {{ item.user?.userName?.[0] ?? '官' }}
                </a-avatar>
                <div>
                  <h3>{{ item.appName || '精选应用' }}</h3>
                  <p>{{ item.user?.userName ?? 'NoCode 官方' }}</p>
                </div>
              </div>
            </article>
          </a-list-item>
        </template>
      </a-list>
    </section>
  </main>
</template>

<style scoped>
.home-page {
  min-height: calc(100vh - 64px);
  padding-bottom: 56px;
  background:
    radial-gradient(circle at 80% 18%, rgba(172, 245, 233, 0.8), transparent 28%),
    linear-gradient(155deg, #fffdf8 0%, #effcf6 42%, #75d5f3 100%);
}

.hero-section {
  max-width: 1120px;
  margin: 0 auto;
  padding: 92px 24px 72px;
  text-align: center;
}

.brand-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  color: #111827;
  font-size: 48px;
  font-weight: 900;
}

.brand-title img {
  width: 64px;
  height: 64px;
  border-radius: 50%;
}

.subtitle {
  margin: 22px 0 46px;
  color: #667085;
  font-size: 20px;
}

.prompt-box {
  max-width: 920px;
  min-height: 184px;
  margin: 0 auto;
  padding: 22px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 28px;
  box-shadow: 0 24px 80px rgba(25, 121, 148, 0.12);
}

.prompt-box :deep(textarea) {
  color: #111827;
  font-size: 18px;
}

.prompt-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
}

.example-tags {
  margin-top: 28px;
}

.gallery-panel {
  width: min(1440px, calc(100% - 64px));
  margin: 0 auto;
  padding: 56px;
  background: #fff;
  border-radius: 32px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
}

.featured-head {
  margin-top: 56px;
}

.section-head h2 {
  margin: 0;
  color: #111827;
  font-size: 32px;
  font-weight: 800;
}

.search-input {
  width: 260px;
}

.app-card {
  cursor: pointer;
}

.cover-wrap {
  position: relative;
  height: 230px;
  overflow: hidden;
  background: #f7f8fa;
  border: 1px solid #eef0f3;
  border-radius: 16px;
}

.cover-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: top center;
  transition: transform 0.25s ease;
}

.app-card:hover .cover-wrap img {
  transform: scale(1.02);
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: linear-gradient(135deg, #f8fafc, #edf7f4);
}

.cover-placeholder img {
  width: 76px;
  height: 76px;
  opacity: 0.55;
}

.card-tools {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.app-card:hover .card-tools {
  opacity: 1;
}

.app-card h3 {
  margin: 18px 12px 4px;
  color: #111827;
  font-size: 20px;
  font-weight: 700;
}

.app-card p {
  margin: 0 12px;
  color: #667085;
  font-size: 15px;
}

.featured-meta {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-top: 18px;
}

.featured-meta h3,
.featured-meta p {
  margin-left: 0;
}

@media (max-width: 768px) {
  .brand-title {
    gap: 14px;
    font-size: 34px;
  }

  .brand-title img {
    width: 48px;
    height: 48px;
  }

  .gallery-panel {
    width: calc(100% - 24px);
    padding: 28px 18px;
    border-radius: 24px;
  }

  .section-head {
    align-items: stretch;
    flex-direction: column;
  }

  .search-input {
    width: 100%;
  }
}
</style>
