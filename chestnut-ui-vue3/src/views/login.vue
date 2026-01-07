<template>
  <div class="login">
    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{ $t("APP.TITLE") }}</h3>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          size="large"
          auto-complete="off"
          :placeholder="$t('Login.Account')"
          @blur="onUserNameBlur"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          size="large"
          auto-complete="off"
          :placeholder="$t('Login.Password')"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item v-if="loginConfig.captcha.enabled">
        <captcha-text v-if="loginConfig.captcha.type=='Text'" ref="TextCaptchaRef" v-model="captchaData" v-model:token="captchaToken"></captcha-text>
        <captcha-math v-if="loginConfig.captcha.type=='Math'" ref="MathCaptchaRef" v-model="captchaData" v-model:token="captchaToken"></captcha-math>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">{{ $t('Login.RememberMe') }}</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          style="width:100%;"
          @click.prevent="handleLogin"
        >
          <span v-if="!loading">{{ $t('Login.Login') }}</span>
          <span v-else>{{ $t('Login.Logining') }}</span>
        </el-button>
        <div style="float: right;" v-if="register">
          <router-link class="link-type" :to="'/register'">{{ $t('Login.Register') }}</router-link>
        </div>
      </el-form-item>
      <el-form-item>
        <el-row justify="space-between" style="width: 100%;">
          <el-col :span="12">
            <language-select id="lang-select" :arrow="true" class="right-menu-item hover-effect" />
          </el-col>
          <el-col :span="12">
            <div v-for="third in loginConfig.thirds" style="width: 100%;display: flex; justify-content: flex-end; align-items: center;">
              <wechat-login v-if="third.type == 'wechat'" :configId="third.id" />
            </div>
          </el-col>
        </el-row>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>{{ copyright }}</span>
    </div>
  </div>
</template>

<script setup>
import { checkUsername, getLoginConfig } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import LanguageSelect from '@/components/LanguageSelect'
import CaptchaMath from './components/captcha/math'
import CaptchaText from './components/captcha/text'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()

const copyright = import.meta.env.VITE_APP_COPYRIGHT
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const loading = ref(false)
// 验证码配置
const captchaData = ref({});
const captchaToken = ref("")
// 注册开关
const register = ref(false)
const redirect = ref(undefined)

const loginForm = ref({
  username: "",
  password: "",
  rememberMe: false,
})

const oldUsername = ref('');
const loginConfig = ref({
  captcha: {
    enabled: false
  },
  thirdLogin: [],
});

function loadLoginConfig() {
  getLoginConfig().then(res => {
    loginConfig.value = res.data;
  });
}

function onUserNameBlur() {
  if (proxy.$tools.isEmpty(loginForm.value.username) || !loginConfig.value.captcha.enabled) {
    return;
  }
  if (oldUsername.value == loginForm.value.username) {
    return;
  }
  oldUsername.value = loginForm.value.username;
  checkUsername(loginForm.value.username).then(res => {
    captchaToken.value = loginForm.value.username;
    nextTick(() => {
      proxy.$refs[`${loginConfig.value.captcha.type}CaptchaRef`].reloadCaptcha();
    })
  })
}
const loginRules = {
  username: [{ required: true, trigger: "blur", message: proxy.$t('Common.RuleTips.NotEmpty') }],
  password: [{ required: true, trigger: "blur", message: proxy.$t('Login.PasswordRuleTip') }]
}

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

onMounted(() => {
  loadLoginConfig()
  setTimeout(() => {
    onUserNameBlur();
  }, 1000);
})

function wxLogin() {
  wxLoginObj.value = new WxLogin({
    self_redirect: true,
    id:"login-container", 
    appid: "wx82da3cc8f5c503e4", 
    scope: "snsapi_login", 
    redirect_uri: "http://localhost:89/admin/",
    state: "123456",
    style: "white",
    fast_login: true,
    onReady: function(isReady){
      console.log(isReady);
      if (!isReady) {
        proxy.$modal.msgError("Load wechat login failed.");
      }
    },
    onQRCodeReady: function(){
      console.log("QRCode ready.");
    }
  });
  console.log(wxLoginObj.value);
}

function handleLogin() {
  if (loginConfig.value.captcha.enabled) {
    loginForm.value.captcha = captchaData.value;
    if (!loginForm.value.captcha) {
      proxy.$modal.msgError(proxy.$t('Login.CaptchaRuleTip'));
      return;
    }
  }
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || '/', query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        // 重新获取验证码
        if (loginConfig.value.captcha.enabled) {
          proxy.$refs[`${loginConfig.value.captcha.type}CaptchaRef`].reloadCaptcha();
        }
      })
    }
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
    captcha: ""
  }
}

getCookie()
</script>
<style lang='scss' scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("@/assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  z-index: 1;
  .el-input {
    height: 40px;
    input {
      height: 40px;
    }
  }
  .input-icon {
    height: 1rem;
    width: 1rem;
    margin-left: 0px;
  }
}
.el-login-footer {
  height: 1.5rem;
  line-height: 1.5;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
</style>