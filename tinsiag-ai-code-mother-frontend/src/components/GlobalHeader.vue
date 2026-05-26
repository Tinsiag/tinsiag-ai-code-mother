<script setup lang="ts">
import { computed, h, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  AppstoreOutlined,
  CommentOutlined,
  HomeOutlined,
  LoginOutlined,
  LogoutOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { userLogout } from '@/api/userController'
import { useLoginUserStore } from '@/stores/LoginUser'

withDefaults(
  defineProps<{
    title?: string
  }>(),
  {
    title: '小新乁の零代码生成平台',
  },
)

const loginUserStore = useLoginUserStore()
const router = useRouter()
const route = useRoute()
const selectedKeys = ref<string[]>([])

const originItems = [
  { key: '/', icon: () => h(HomeOutlined), label: '首页' },
  { key: '/admin/appManage', icon: () => h(AppstoreOutlined), label: '应用管理', admin: true },
  { key: '/admin/chatHistoryManage', icon: () => h(CommentOutlined), label: '对话管理', admin: true },
  { key: '/admin/userManage', icon: () => h(UserOutlined), label: '用户管理', admin: true },
]

const menuItems = computed(() =>
  originItems.filter((item) => !item.admin || loginUserStore.loginUser.userRole === 'admin'),
)

const currentMenuKey = computed(() => {
  const matched = menuItems.value.find((item) => route.path === item.key)
  return matched ? [matched.key] : []
})

watch(
  currentMenuKey,
  (keys) => {
    selectedKeys.value = keys
  },
  { immediate: true },
)

function onMenuClick({ key }: { key: string }) {
  router.push(key)
}

const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0 && res.data.data) {
    message.success('退出成功')
    loginUserStore.setLoginUser({ userName: '未登录' })
    await router.push('/user/login')
  } else {
    message.error(`退出失败，${res.data.message ?? '请稍后重试'}`)
  }
}
</script>

<template>
  <a-layout-header class="global-header">
    <div class="header-left" @click="router.push('/')">
      <img src="/favicon.ico" alt="logo" class="header-logo" />
      <span class="header-title">{{ title }}</span>
    </div>
    <a-menu
      v-model:selectedKeys="selectedKeys"
      mode="horizontal"
      theme="light"
      class="header-menu"
      @click="onMenuClick"
    >
      <a-menu-item v-for="item in menuItems" :key="item.key">
        <component :is="item.icon" v-if="item.icon" />
        <span>{{ item.label }}</span>
      </a-menu-item>
    </a-menu>
    <div class="header-right">
      <a-dropdown v-if="loginUserStore.loginUser.id">
        <a-space class="user-entry">
          <a-avatar :src="loginUserStore.loginUser.userAvatar">
            {{ loginUserStore.loginUser.userName?.[0] ?? '用' }}
          </a-avatar>
          {{ loginUserStore.loginUser.userName ?? '无名用户' }}
        </a-space>
        <template #overlay>
          <a-menu>
            <a-menu-item @click="doLogout">
              <LogoutOutlined />
              退出登录
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
      <a-button v-else type="primary" ghost @click="router.push('/user/login')">
        <template #icon><LoginOutlined /></template>
        登录
      </a-button>
    </div>
  </a-layout-header>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-right: 24px;
  cursor: pointer;
}

.header-logo {
  width: 34px;
  height: 34px;
  margin-right: 10px;
  border-radius: 50%;
}

.header-title {
  color: #111827;
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
}

.header-menu {
  flex: 1;
  min-width: 0;
  border-bottom: none;
}

.header-right {
  flex-shrink: 0;
  margin-left: 16px;
}

.user-entry {
  cursor: pointer;
}

@media (max-width: 576px) {
  .global-header {
    padding: 0 12px;
  }

  .header-title {
    display: none;
  }

  .header-left {
    margin-right: 8px;
  }
}
</style>
