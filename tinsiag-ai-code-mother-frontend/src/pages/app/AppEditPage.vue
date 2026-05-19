<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVoById, getAppVoByIdByAdmin, updateApp, updateAppByAdmin } from '@/api/appController'
import { useLoginUserStore } from '@/stores/LoginUser'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const appId = computed(() => String(route.params.id ?? ''))
const loading = ref(false)
const saving = ref(false)
const appInfo = ref<API.AppVO>()

const formState = reactive<API.AppAdminUpdateRequest>({
  id: undefined,
  appName: '',
  cover: '',
  priority: 0,
})

const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')

const fetchApp = async () => {
  if (!appId.value) {
    return
  }
  loading.value = true
  try {
    const res = isAdmin.value
      ? await getAppVoByIdByAdmin({ id: appId.value })
      : await getAppVoById({ id: appId.value })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data
      formState.id = res.data.data.id
      formState.appName = res.data.data.appName || ''
      formState.cover = res.data.data.cover || ''
      formState.priority = res.data.data.priority ?? 0
    } else {
      message.error(`获取应用失败，${res.data.message ?? '请稍后重试'}`)
    }
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!formState.id) {
    return
  }
  saving.value = true
  try {
    const res = isAdmin.value
      ? await updateAppByAdmin({ ...formState })
      : await updateApp({ id: formState.id, appName: formState.appName })
    if (res.data.code === 0 && res.data.data) {
      message.success('保存成功')
      router.push(isAdmin.value ? '/admin/appManage' : `/app/chat/${formState.id}`)
    } else {
      message.error(`保存失败，${res.data.message ?? '请稍后重试'}`)
    }
  } finally {
    saving.value = false
  }
}

onMounted(fetchApp)
</script>

<template>
  <main class="edit-page">
    <a-card :bordered="false" class="edit-card">
      <template #title>应用信息</template>
      <a-spin :spinning="loading">
        <a-form :model="formState" layout="vertical" @finish="submit">
          <a-form-item
            label="应用名称"
            name="appName"
            :rules="[{ required: true, message: '请输入应用名称' }]"
          >
            <a-input v-model:value="formState.appName" placeholder="请输入应用名称" />
          </a-form-item>
          <a-form-item v-if="isAdmin" label="应用封面" name="cover">
            <a-input v-model:value="formState.cover" placeholder="请输入封面 URL" />
          </a-form-item>
          <a-form-item v-if="isAdmin" label="优先级" name="priority">
            <a-input-number
              v-model:value="formState.priority"
              :min="0"
              :max="99"
              class="full-input"
            />
          </a-form-item>
          <a-descriptions v-if="appInfo" size="small" :column="1" class="app-desc">
            <a-descriptions-item label="应用 ID">{{ appInfo.id }}</a-descriptions-item>
            <a-descriptions-item label="创建人">{{
              appInfo.user?.userName ?? appInfo.userId
            }}</a-descriptions-item>
            <a-descriptions-item label="生成类型">{{
              appInfo.codeGenType || '-'
            }}</a-descriptions-item>
          </a-descriptions>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="saving">保存</a-button>
            <a-button @click="router.back()">取消</a-button>
          </a-space>
        </a-form>
      </a-spin>
    </a-card>
  </main>
</template>

<style scoped>
.edit-page {
  min-height: calc(100vh - 64px);
  padding: 32px;
  background: #f6f8fb;
}

.edit-card {
  max-width: 720px;
  margin: 0 auto;
  border-radius: 12px;
}

.full-input {
  width: 100%;
}

.app-desc {
  margin: 8px 0 24px;
}
</style>
