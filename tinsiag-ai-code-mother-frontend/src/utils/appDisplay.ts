import { isRef, type Ref } from 'vue'

export const DEFAULT_APP_COVER = '/default.png'
export const DEFAULT_APP_NAME = '未命名应用'
export const DEFAULT_APP_AUTHOR = 'NoCode 官方'

export type LoginUserLike = Pick<API.LoginUserVO, 'id' | 'userName' | 'userAvatar'>

const resolveLoginUser = (loginUser?: LoginUserLike | Ref<LoginUserLike>) =>
  isRef(loginUser) ? loginUser.value : loginUser

export const getAppCover = (app?: API.AppVO) => app?.cover || DEFAULT_APP_COVER

export const getAppTitle = (app?: API.AppVO) => app?.appName || app?.initPrompt || DEFAULT_APP_NAME

export const getAppUserName = (app?: API.AppVO, loginUser?: LoginUserLike | Ref<LoginUserLike>) => {
  const currentUser = resolveLoginUser(loginUser)
  const isCurrentUser = app?.userId && currentUser?.id && String(app.userId) === String(currentUser.id)

  return app?.user?.userName || (isCurrentUser ? currentUser?.userName : '') || DEFAULT_APP_AUTHOR
}

export const getAppUserAvatar = (
  app?: API.AppVO,
  loginUser?: LoginUserLike | Ref<LoginUserLike>,
) => {
  const currentUser = resolveLoginUser(loginUser)
  const isCurrentUser = app?.userId && currentUser?.id && String(app.userId) === String(currentUser.id)

  return app?.user?.userAvatar || (isCurrentUser ? currentUser?.userAvatar : '')
}

export const getAppUserInitial = (app?: API.AppVO, loginUser?: LoginUserLike | Ref<LoginUserLike>) =>
  getAppUserName(app, loginUser).trim()[0] || '官'
