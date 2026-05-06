<template>
  <div id="userLoginPage">
    <h2 class="title">小新乁のAI应用生成-用户登录</h2>
    <div class="desc">不写一行代码，一键生成完整应用</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>

      <div class="tips">没有注册？ <RouterLink to="/user/register">点击注册</RouterLink></div>
      <div class="submit">
        <a-form-item >
          <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
        </a-form-item>
      </div>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/LoginUser.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const loginUserStore = useLoginUserStore();
const router = useRouter();
/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const res = await userLogin(values);
  if (res.data.code=== 0 && res.data.data){
    await loginUserStore.fetchLoginUser();
    message.success("登录成功");
    router.push({
      path: '/',
      replace: true
    })
  }else {
    message.error("登录失败"+res.data.message);
  }
}
</script>
<style>
#userLoginPage {
  max-width: 300px;
  margin: 0 auto;

}
.title {
  margin-bottom: 16px;
  text-align: center;
}
.desc {
  text-align: center;
  margin-bottom: 16px;
  color: #888;
}
.tips {
  text-align: right;
  margin-bottom: 16px;
  color: #888;
  font-size: 13px;
}
.submit{
  margin: 0 auto;
  text-align: center;
}
</style>
