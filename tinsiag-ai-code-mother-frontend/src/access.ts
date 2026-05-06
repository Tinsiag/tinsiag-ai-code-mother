import router from '@/router'
import { useLoginUserStore } from '@/stores/LoginUser.ts'
import { message } from 'ant-design-vue'

let firstFetchLoginUser = true

router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser

  // 首次拉取登录用户信息
  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser // 正确获取
    firstFetchLoginUser = false
  }

  const toUrl = to.fullPath

  if (toUrl.startsWith('/admin')) {
    if (!loginUser || loginUser.userRole !== 'admin') {
      message.error('没有权限')
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
  }

  next() // 必须调用，继续路由导航
})
