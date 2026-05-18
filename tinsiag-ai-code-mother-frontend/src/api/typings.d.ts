declare namespace API {
  type AppId = string | number

  type AppAddRequest = {
    initPrompt?: string
  }

  type AppAdminUpdateRequest = {
    id?: AppId
    appName?: string
    cover?: string
    priority?: number
  }

  type AppDeployRequest = {
    appId?: AppId
  }

  type AppQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: AppId
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
    userId?: number
  }

  type AppUpdateRequest = {
    id?: AppId
    appName?: string
  }

  type AppVO = {
    id?: AppId
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: AppId
    createTime?: string
    updateTime?: string
    user?: UserVO
  }

  type BaseResponseAppVO = {
    code?: number
    message?: string
    data?: AppVO
  }

  type BaseResponseBoolean = {
    code?: number
    message?: string
    data?: boolean
  }

  type BaseResponseLoginUserVO = {
    code?: number
    message?: string
    data?: LoginUserVO
  }

  type BaseResponseLong = {
    code?: number
    message?: string
    data?: AppId
  }

  type BaseResponsePageAppVO = {
    code?: number
    message?: string
    data?: PageAppVO
  }

  type BaseResponsePageUserVO = {
    code?: number
    message?: string
    data?: PageUserVO
  }

  type BaseResponseString = {
    code?: number
    message?: string
    data?: string
  }

  type BaseResponseUser = {
    code?: number
    message?: string
    data?: User
  }

  type BaseResponseUserVO = {
    code?: number
    message?: string
    data?: UserVO
  }

  type Chat2GenCodeParams = {
    appId: AppId
    message: string
  }

  type DeleteRequest = {
    id?: string | number
  }

  type getAppVOByIdByAdminParams = {
    id: AppId
  }

  type getAppVOByIdParams = {
    id: AppId
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfils?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type ServerSentEventString = true

  type serveStaticResourceParams = {
    deployKey: string
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }
}
