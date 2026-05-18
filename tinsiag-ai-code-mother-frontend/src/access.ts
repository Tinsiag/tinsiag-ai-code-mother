import router from '@/router'
import { useLoginUserStore } from '@/stores/LoginUser'
import { message } from 'ant-design-vue'

let firstFetchLoginUser = true

router.beforeEach(async (to) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser

  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false
  }

  if (to.fullPath.startsWith('/admin') && loginUser.userRole !== 'admin') {
    message.error('没有权限')
    return `/user/login?redirect=${encodeURIComponent(to.fullPath)}`
  }

  return true
})
