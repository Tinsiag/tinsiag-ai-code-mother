<script setup lang="ts">
import { ref, computed, watch, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { LoginOutlined, LogoutOutlined, HomeOutlined, AppstoreOutlined, CodeOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/LoginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { message } from 'ant-design-vue'

// 获取登录用户状态
const loginUserStore = useLoginUserStore()

withDefaults(
  defineProps<{
    title?: string
  }>(),
  {
    title: '小新乁のAI零代码生成平台',
  },
)

// 菜单配置项
const originItems = [
  { key: '/', icon: () => h(HomeOutlined), label: '首页' },
  { key: '/apps', icon: () => h(AppstoreOutlined), label: '应用' },
  { key: '/generate', icon: () => h(CodeOutlined), label: '代码生成' },
  { key: '/admin/usermanage', icon: () => h(AppstoreOutlined), label: '系统管理' },
]

// 过滤菜单项
const filterMenus = (menus = originItems) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed(() => filterMenus(originItems))

const router = useRouter()
const route = useRoute()

const selectedKeys = ref<string[]>([])

// 根据当前路由同步选中菜单
const currentMenuKey = computed(() => {
  const matched = menuItems.value.find((item) => item.key === route.path)
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
//退出登录
const doLogout = async () =>{
  const res = await userLogout();
  if(res.data.code ===0 && res.data.data){
    message.success('退出成功');
    loginUserStore.setLoginUser(
      {
        userName: '未登录'
      }
    )
    await router.push('/user/login')
  }else {
    message.error('退出失败'+res.data.message)
  }
}


</script>

<template>
  <a-layout-header class="global-header">
    <div class="header-left">
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
      <div v-if="loginUserStore.loginUser.id">
        <a-dropdown>
          <a-space>
            <a-avatar :src="loginUserStore.loginUser.userAvatar" />
            {{ loginUserStore.loginUser.userName ?? '无名' }}
          </a-space>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="doLogout">
                <LogoutOutlined/>
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
      <div v-else>
        <a-button type="primary" ghost  href="/user/login">
          <template #icon><LoginOutlined /></template>
          登录
        </a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: rgb(255, 255, 255);
}

.header-left {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-right: 24px;
}

.header-logo {
  width: 32px;
  height: 32px;
  margin-right: 12px;
}

.header-title {
  color: rgba(0, 0, 0, 0.88);
  font-size: 18px;
  font-weight: 600;
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

@media (max-width: 576px) {
  .global-header {
    padding: 0 12px;
  }

  .header-title {
    display: none;
  }

  .header-left {
    margin-right: 12px;
  }
}
</style>
