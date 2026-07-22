<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import hljs from 'highlight.js'
import MarkdownIt from 'markdown-it'
import 'highlight.js/styles/github.css'
import {
  CloudUploadOutlined,
  DownloadOutlined,
  EditOutlined,
  RocketOutlined,
} from '@ant-design/icons-vue'
import { deployApp, downloadAppCode, getAppVoById } from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import PromptComposer from '@/components/app/PromptComposer.vue'
import { getAppPreviewUrl, getCodeGenTypeLabel } from '@/config/domain'
import request from '@/request'
import { useLoginUserStore } from '@/stores/LoginUser'
import { appendElementInfoToPrompt, VisualEditor, type ElementInfo } from '@/utils/visualEditor'

type ChatMessage = {
  role: 'user' | 'assistant'
  content: string
  renderedContent?: string
  loading?: boolean
}

type SseChunkPayload = {
  d?: string
}

type BusinessErrorPayload = {
  message?: string
}

const DEFAULT_USER_AVATAR = '/default-user-avatar.png'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const appId = computed(() => {
  const id = route.params.id
  return Array.isArray(id) ? id[0] : id
})
const appInfo = ref<API.AppVO>()
const messages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const generating = ref(false)
const deploying = ref(false)
const downloading = ref(false)
const previewReady = ref(false)
const previewIframeRef = ref<HTMLIFrameElement>()
const isEditMode = ref(false)
const selectedElementInfo = ref<ElementInfo | null>(null)
const messageListRef = ref<HTMLElement>()
const historyLoading = ref(false)
const hasMoreHistory = ref(false)
const lastCreateTime = ref<string>()
const userAvatarLoadFailed = ref(false)
let eventSource: EventSource | undefined
let pendingAssistantContent = ''
let pendingAssistantMessage: ChatMessage | undefined
let renderTimer: ReturnType<typeof window.setTimeout> | undefined
let previewRequestSeq = 0
const visualEditor = new VisualEditor({
  onElementSelected: (elementInfo) => {
    selectedElementInfo.value = elementInfo
  },
})
const PREVIEW_READY_RETRY_COUNT = 30
const PREVIEW_READY_RETRY_DELAY = 1000

const escapeHtml = (value: string) =>
  value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')

const markdown: MarkdownIt = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  highlight(code: string, language: string): string {
    const normalizedLanguage = language?.trim()
    if (normalizedLanguage && hljs.getLanguage(normalizedLanguage)) {
      return `<pre class="hljs"><code>${hljs.highlight(code, { language: normalizedLanguage }).value}</code></pre>`
    }
    return `<pre class="hljs"><code>${escapeHtml(code)}</code></pre>`
  },
})

const appName = computed(() => appInfo.value?.appName || appInfo.value?.initPrompt || '未命名应用')

const isOwnApp = computed(() => {
  const userId = loginUserStore.loginUser.id
  const appUserId = appInfo.value?.userId
  return Boolean(userId && appUserId && String(userId) === String(appUserId))
})
const canChat = computed(() => Boolean(appInfo.value && isOwnApp.value))
const selectedElementClassNames = computed(() =>
  selectedElementInfo.value?.className
    .split(/\s+/)
    .filter((className) => className && !className.startsWith('edit-'))
    .join('.') || '',
)
const userAvatar = computed(() =>
  userAvatarLoadFailed.value ? DEFAULT_USER_AVATAR : loginUserStore.loginUser.userAvatar || DEFAULT_USER_AVATAR,
)

const previewUrl = computed(() => {
  if (!appInfo.value?.id || !appInfo.value?.codeGenType) {
    return ''
  }
  return getAppPreviewUrl(appInfo.value.codeGenType, appInfo.value.id)
})

const renderMarkdown = (content: string) => markdown.render(content)

const sleep = (ms: number) => new Promise((resolve) => window.setTimeout(resolve, ms))

const safeDecodeURIComponent = (value: string) => {
  try {
    return decodeURIComponent(value)
  } catch {
    return value
  }
}

const parseDownloadFileName = (contentDisposition?: string) => {
  if (!contentDisposition) {
    return ''
  }
  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    return safeDecodeURIComponent(utf8Match[1])
  }
  const fileNameMatch = contentDisposition.match(/filename="?([^";]+)"?/i)
  return fileNameMatch?.[1] ? safeDecodeURIComponent(fileNameMatch[1]) : ''
}

const saveBlob = (blob: Blob, fileName: string) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

const checkPreviewAvailable = async (url: string) => {
  try {
    const res = await fetch(url, {
      method: 'GET',
      cache: 'no-store',
      credentials: 'include',
    })
    return res.ok
  } catch {
    return false
  }
}

const refreshPreviewWhenAvailable = async () => {
  const url = previewUrl.value
  const requestSeq = ++previewRequestSeq
  previewReady.value = false
  if (!url) {
    return
  }
  for (let i = 0; i < PREVIEW_READY_RETRY_COUNT; i += 1) {
    if (requestSeq !== previewRequestSeq) {
      return
    }
    if (await checkPreviewAvailable(url)) {
      if (requestSeq === previewRequestSeq) {
        previewReady.value = true
      }
      return
    }
    await sleep(PREVIEW_READY_RETRY_DELAY)
  }
}

const handleUserAvatarError = () => {
  userAvatarLoadFailed.value = true
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const clearRenderTimer = () => {
  if (renderTimer) {
    window.clearTimeout(renderTimer)
    renderTimer = undefined
  }
}

const flushAssistantMessage = async (shouldScroll = true) => {
  if (!pendingAssistantMessage) {
    return
  }
  pendingAssistantMessage.content = pendingAssistantContent
  pendingAssistantMessage.renderedContent = renderMarkdown(pendingAssistantContent)
  if (shouldScroll && !document.hidden) {
    await scrollToBottom()
  }
}

const scheduleAssistantRender = () => {
  if (!pendingAssistantMessage || renderTimer || document.hidden) {
    return
  }
  renderTimer = window.setTimeout(() => {
    renderTimer = undefined
    flushAssistantMessage()
  }, 120)
}

const handleVisibilityChange = () => {
  if (!document.hidden) {
    clearRenderTimer()
    flushAssistantMessage()
  }
}

const fetchApp = async () => {
  if (!appId.value) {
    return
  }
  const res = await getAppVoById({ id: appId.value })
  if (res.data.code === 0 && res.data.data) {
    appInfo.value = res.data.data
  } else {
    message.error(`获取应用失败，${res.data.message ?? '请稍后重试'}`)
  }
}

const isUserMessage = (type?: string) => ['user', 'USER'].includes(type ?? '')

const toChatMessage = (record: API.ChatHistory): ChatMessage => {
  const role = isUserMessage(record.messageType) ? 'user' : 'assistant'
  const content = record.message ?? ''
  return {
    role,
    content,
    renderedContent: role === 'assistant' ? renderMarkdown(content) : undefined,
  }
}

const fetchHistory = async (loadMore = false) => {
  if (!appId.value || historyLoading.value) {
    return
  }
  historyLoading.value = true
  const oldScrollHeight = messageListRef.value?.scrollHeight ?? 0
  const oldScrollTop = messageListRef.value?.scrollTop ?? 0
  try {
    const res = await listAppChatHistory({
      appId: appId.value,
      pageSize: 10,
      lastCreateTime: loadMore ? lastCreateTime.value : undefined,
    })
    if (res.data.code === 0 && res.data.data) {
      const records = (res.data.data.records ?? [])
        .filter((item) => item.message)
        .sort((a, b) => String(a.createTime ?? '').localeCompare(String(b.createTime ?? '')))
      const historyMessages = records.map(toChatMessage)
      messages.value = loadMore ? [...historyMessages, ...messages.value] : historyMessages
      if (records.length > 0) {
        lastCreateTime.value = records[0]?.createTime
      }
      const total = res.data.data.totalRow ?? 0
      hasMoreHistory.value = total > messages.value.length
      await nextTick()
      if (loadMore && messageListRef.value) {
        messageListRef.value.scrollTop = messageListRef.value.scrollHeight - oldScrollHeight + oldScrollTop
      } else {
        await scrollToBottom()
      }
    } else {
      message.error(`获取对话历史失败，${res.data.message ?? '请稍后重试'}`)
    }
  } finally {
    historyLoading.value = false
  }
}

const closeEventSource = () => {
  eventSource?.close()
  eventSource = undefined
}

const parseSseChunk = (rawData: string) => {
  if (!rawData) {
    return ''
  }
  try {
    const payload = JSON.parse(rawData) as SseChunkPayload
    return typeof payload.d === 'string' ? payload.d : ''
  } catch {
    return rawData
  }
}

const handleVisualEditorMessage = (event: MessageEvent) => {
  visualEditor.handleIframeMessage(event)
}

const handlePreviewIframeLoad = () => {
  const iframe = previewIframeRef.value
  if (!iframe) {
    return
  }
  visualEditor.init(iframe)
  visualEditor.onIframeLoad()
}

const clearSelectedElement = () => {
  selectedElementInfo.value = null
  visualEditor.clearSelection()
}

const resetVisualEditState = () => {
  selectedElementInfo.value = null
  isEditMode.value = false
  visualEditor.disableEditMode()
}

const toggleEditMode = () => {
  const iframe = previewIframeRef.value
  if (!previewReady.value || !iframe) {
    message.warning('请等待页面加载完成')
    return
  }

  visualEditor.init(iframe)
  isEditMode.value = visualEditor.toggleEditMode()
}

const finishGenerating = async () => {
  closeEventSource()
  clearRenderTimer()
  await flushAssistantMessage()
  pendingAssistantMessage = undefined
  pendingAssistantContent = ''
  generating.value = false
  await fetchApp()
  refreshPreviewWhenAvailable()
  messages.value = messages.value.map((item) => {
    if (!item.loading) {
      return item
    }
    const content = item.content || '生成完成。'
    return {
      ...item,
      loading: false,
      content,
      renderedContent: item.renderedContent || renderMarkdown(content),
    }
  })
}

const sendMessage = async (text?: string) => {
  const rawContent = (text ?? inputMessage.value).trim()
  if (!rawContent || !appId.value || generating.value || !canChat.value) {
    return
  }
  const content = appendElementInfoToPrompt(rawContent, selectedElementInfo.value)
  inputMessage.value = ''
  resetVisualEditState()
  previewReady.value = false
  messages.value.push({ role: 'user', content })

  const assistantMessage = reactive<ChatMessage>({
    role: 'assistant',
    content: '',
    renderedContent: '',
    loading: true,
  })
  pendingAssistantMessage = assistantMessage
  pendingAssistantContent = ''
  messages.value.push(assistantMessage)
  generating.value = true
  await scrollToBottom()

  closeEventSource()
  const params = new URLSearchParams({
    appId: String(appId.value),
    message: content,
  })
  eventSource = new EventSource(`${request.defaults.baseURL || '/api'}/app/chat/generate/code?${params}`, {
    withCredentials: true,
  })
  let streamCompleted = false

  eventSource.onmessage = (event) => {
    if (streamCompleted) {
      return
    }
    const chunk = parseSseChunk(event.data)
    if (!chunk) {
      return
    }
    pendingAssistantContent += chunk
    scheduleAssistantRender()
  }

  eventSource.addEventListener('done', () => {
    if (streamCompleted) {
      return
    }
    streamCompleted = true
    finishGenerating()
  })

  eventSource.addEventListener('business-error', (event: Event) => {
    if (streamCompleted) {
      return
    }
    streamCompleted = true

    const rawData = (event as MessageEvent<string>).data
    let errorMessage = '生成过程中出现错误'

    try {
      const errorData = JSON.parse(rawData) as BusinessErrorPayload
      console.error('SSE业务错误事件:', errorData)
      errorMessage = errorData.message || errorMessage
    } catch (parseError) {
      console.error('解析错误事件失败:', parseError, '原始数据:', rawData)
      errorMessage = '服务器返回错误'
    }

    clearRenderTimer()
    pendingAssistantMessage = undefined
    pendingAssistantContent = ''
    assistantMessage.content = `❌ ${errorMessage}`
    assistantMessage.renderedContent = renderMarkdown(assistantMessage.content)
    assistantMessage.loading = false
    message.error(errorMessage)
    generating.value = false
    closeEventSource()
  })

  eventSource.onerror = () => {
    if (streamCompleted) {
      return
    }
    streamCompleted = true
    if (pendingAssistantContent || assistantMessage.content) {
      finishGenerating()
    } else {
      clearRenderTimer()
      pendingAssistantMessage = undefined
      pendingAssistantContent = ''
      generating.value = false
      assistantMessage.loading = false
      assistantMessage.content = '连接中断了，请稍后再试。'
      assistantMessage.renderedContent = renderMarkdown(assistantMessage.content)
      closeEventSource()
    }
  }
}

const doDownload = async () => {
  if (!appId.value || downloading.value) {
    return
  }
  downloading.value = true
  try {
    const res = await downloadAppCode(
      { appId: appId.value } as unknown as API.downloadAppCodeParams,
      { responseType: 'blob' },
    )
    const fileName = parseDownloadFileName(res.headers['content-disposition']) || `${appName.value}.zip`
    saveBlob(res.data, fileName)
    message.success('下载成功')
  } catch {
    message.error('下载失败，请稍后重试')
  } finally {
    downloading.value = false
  }
}

const doDeploy = async () => {
  if (!appId.value) {
    return
  }
  deploying.value = true
  try {
    const res = await deployApp({ appId: appId.value })
    if (res.data.code === 0 && res.data.data) {
      Modal.success({
        title: '部署成功',
        content: res.data.data,
        okText: '打开网站',
        onOk: () => {
          window.open(res.data.data, '_blank')
        },
      })
      await fetchApp()
    } else {
      message.error(`部署失败，${res.data.message ?? '请稍后重试'}`)
    }
  } finally {
    deploying.value = false
  }
}

onMounted(async () => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
  window.addEventListener('message', handleVisualEditorMessage)
  if (!loginUserStore.loginUser.id) {
    await loginUserStore.fetchLoginUser().catch(() => {})
  }
  await fetchApp()
  await fetchHistory()
  if (previewUrl.value && messages.value.length >= 2) {
    refreshPreviewWhenAvailable()
  } else {
    previewReady.value = false
  }
  const autoPrompt = typeof route.query.prompt === 'string' ? route.query.prompt : ''
  if (isOwnApp.value && messages.value.length === 0 && (autoPrompt || appInfo.value?.initPrompt)) {
    sendMessage(autoPrompt || appInfo.value?.initPrompt)
  }
})

onBeforeUnmount(() => {
  previewRequestSeq += 1
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('message', handleVisualEditorMessage)
  resetVisualEditState()
  clearRenderTimer()
  closeEventSource()
})
</script>

<template>
  <main class="chat-page">
    <header class="chat-topbar">
      <a-space class="app-title" @click="router.push(`/app/edit/${appId}`)">
        <a-avatar src="/favicon.ico" :size="36" />
        <span>{{ appName }}</span>
        <a-tag v-if="appInfo?.codeGenType" color="blue" class="code-gen-type-tag">
          {{ getCodeGenTypeLabel(appInfo.codeGenType) }}
        </a-tag>
        <EditOutlined />
      </a-space>
      <a-space>
        <a-button :loading="downloading" @click="doDownload">
          <template #icon><DownloadOutlined /></template>
          下载代码
        </a-button>
        <a-button type="primary" :loading="deploying" @click="doDeploy">
          <template #icon><RocketOutlined /></template>
          部署
        </a-button>
      </a-space>
    </header>

    <section class="chat-workspace">
      <aside class="conversation-panel">
        <div ref="messageListRef" class="message-list">
          <div v-if="hasMoreHistory" class="load-more-history">
            <a-button type="link" :loading="historyLoading" @click="fetchHistory(true)">
              加载更多
            </a-button>
          </div>
          <div
            v-for="(item, index) in messages"
            :key="index"
            class="message-row"
            :class="item.role"
          >
            <a-avatar
              v-if="item.role === 'assistant'"
              class="message-avatar"
              src="/favicon.ico"
              :size="32"
            />
            <div class="message-bubble">
              <a-spin v-if="item.loading && !item.content" size="small" />
              <div
                v-else-if="item.role === 'assistant'"
                class="markdown-body"
                v-html="item.renderedContent"
              />
              <pre v-else>{{ item.content }}</pre>
            </div>
            <a-avatar
              v-if="item.role === 'user'"
              class="message-avatar"
              :src="userAvatar"
              :size="32"
              @error="handleUserAvatarError"
            />
          </div>
        </div>

        <div
          class="input-box"
          :class="{ disabled: !canChat }"
          :title="canChat ? '' : '无法在别人的作品下对话哦~'"
        >
          <a-alert
            v-if="selectedElementInfo"
            class="selected-element-alert"
            type="info"
            show-icon
            closable
            @close="clearSelectedElement"
          >
            <template #message>
              <div class="selected-element-info">
                <div class="element-header">
                  <span class="element-label">选中元素：</span>
                  <span class="element-tag">{{ selectedElementInfo.tagName.toLowerCase() }}</span>
                  <span v-if="selectedElementInfo.id" class="element-id">
                    #{{ selectedElementInfo.id }}
                  </span>
                  <span v-if="selectedElementClassNames" class="element-class">
                    .{{ selectedElementClassNames }}
                  </span>
                </div>
                <div class="element-details">
                  <div v-if="selectedElementInfo.textContent" class="element-item">
                    内容：{{ selectedElementInfo.textContent.substring(0, 50) }}{{
                      selectedElementInfo.textContent.length > 50 ? '...' : ''
                    }}
                  </div>
                  <div v-if="selectedElementInfo.pagePath" class="element-item">
                    页面路径：{{ selectedElementInfo.pagePath }}
                  </div>
                  <div class="element-item">
                    选择器：
                    <code class="element-selector-code">{{ selectedElementInfo.selector }}</code>
                  </div>
                </div>
              </div>
            </template>
          </a-alert>
          <PromptComposer
            v-model:value="inputMessage"
            :disabled="!canChat"
            :loading="generating"
            :edit-active="isEditMode"
            show-edit
            placeholder="描述越详细，页面越具体，可以一步一步完善生成效果"
            @edit="toggleEditMode"
            @submit="sendMessage()"
          />
        </div>
      </aside>

      <section class="preview-panel" :class="{ editing: isEditMode }">
        <iframe
          v-if="previewReady && previewUrl"
          ref="previewIframeRef"
          :src="previewUrl"
          title="应用预览"
          @load="handlePreviewIframeLoad"
        />
        <div v-else class="preview-empty">
          <CloudUploadOutlined />
          <h2>{{ generating ? '正在生成网页' : '网页将在生成完成后展示' }}</h2>
          <p>左侧对话完成后，这里会自动加载最新效果。</p>
        </div>
      </section>
    </section>
  </main>
</template>

<style scoped>
.chat-page {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  background: #fff;
}

.chat-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 18px;
  background: #fff;
  border-bottom: 1px solid #edf0f4;
}

.app-title {
  color: #111827;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

.code-gen-type-tag {
  margin-inline-end: 0;
  font-weight: 500;
}

.chat-workspace {
  display: grid;
  flex: 1 1 auto;
  grid-template-columns: 36% 64%;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.conversation-panel {
  display: flex;
  flex: 1 1 auto;
  min-height: 0;
  flex-direction: column;
  min-width: 360px;
  overflow: hidden;
  border-right: 1px solid #edf0f4;
  background: #fff;
}

.message-list {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding: 18px 18px 140px;
}

.load-more-history {
  margin-bottom: 14px;
  text-align: center;
}

.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-avatar {
  flex: 0 0 auto;
}

.message-bubble {
  max-width: min(86%, 620px);
  padding: 14px 16px;
  background: #f5f6f8;
  border-radius: 12px;
  color: #111827;
  line-height: 1.7;
}

.message-row.user .message-bubble {
  background: #f3f4f6;
}

.message-bubble pre {
  margin: 0;
  font-family: inherit;
  white-space: pre-wrap;
  word-break: break-word;
}

.markdown-body :deep(*) {
  max-width: 100%;
}

.markdown-body :deep(p),
.markdown-body :deep(ul),
.markdown-body :deep(ol),
.markdown-body :deep(pre),
.markdown-body :deep(blockquote) {
  margin: 0 0 12px;
}

.markdown-body :deep(p:last-child),
.markdown-body :deep(ul:last-child),
.markdown-body :deep(ol:last-child),
.markdown-body :deep(pre:last-child),
.markdown-body :deep(blockquote:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(code) {
  padding: 0.15em 0.35em;
  background: rgba(15, 23, 42, 0.08);
  border-radius: 6px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  font-size: 0.92em;
}

.markdown-body :deep(pre) {
  overflow-x: auto;
  padding: 14px 16px;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
}

.markdown-body :deep(pre code) {
  padding: 0;
  background: transparent;
  border-radius: 0;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 20px;
}

.markdown-body :deep(blockquote) {
  padding-left: 12px;
  color: #667085;
  border-left: 3px solid #d0d5dd;
}

.markdown-body :deep(a) {
  color: #1677ff;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.input-box {
  position: sticky;
  bottom: 0;
  margin: 0 14px 14px;
  padding: 14px;
  background: #fff;
  border: 1px solid #edf0f4;
  border-radius: 14px;
  box-shadow: 0 -12px 40px rgba(15, 23, 42, 0.06);
}

.input-box.disabled {
  cursor: not-allowed;
  background: #f8fafc;
}

.input-box.disabled :deep(textarea) {
  cursor: not-allowed;
}

.selected-element-alert {
  margin-bottom: 12px;
}

.selected-element-info {
  line-height: 1.5;
}

.element-header {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
  margin-bottom: 6px;
}

.element-label {
  color: #475467;
}

.element-tag,
.element-selector-code {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
}

.element-tag {
  color: #1677ff;
  font-weight: 600;
}

.element-id {
  color: #16a34a;
}

.element-class {
  color: #d97706;
}

.element-item {
  color: #475467;
  font-size: 13px;
  word-break: break-word;
}

.element-selector-code {
  padding: 2px 5px;
  color: #d4380d;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 4px;
}

.preview-panel {
  display: flex;
  flex: 1 1 auto;
  min-height: 0;
  min-width: 0;
  flex-direction: column;
  overflow: hidden;
  padding: 14px;
  background: #f8fafc;
}

.preview-panel iframe {
  display: block;
  flex: 1 1 auto;
  min-height: 0;
  width: 100%;
  height: 100%;
  background: #fff;
  border: 1px solid #edf0f4;
  border-radius: 16px;
}

.preview-panel.editing iframe {
  border-color: #1677ff;
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.12);
}

.preview-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #667085;
  background: #fff;
  border: 1px dashed #d9dee7;
  border-radius: 16px;
}

.preview-empty :deep(.anticon) {
  color: #94a3b8;
  font-size: 44px;
}

.preview-empty h2 {
  margin: 18px 0 8px;
  color: #111827;
  font-size: 22px;
}

.preview-empty p {
  margin: 0;
}

@media (max-width: 920px) {
  .chat-page {
    height: auto;
    min-height: auto;
    overflow: visible;
  }

  .chat-workspace {
    display: block;
    height: auto;
    min-height: auto;
    overflow: visible;
  }

  .conversation-panel {
    min-width: 0;
    height: 640px;
    overflow: hidden;
    border-right: none;
  }

  .preview-panel {
    height: 680px;
    overflow: hidden;
  }
}
</style>
