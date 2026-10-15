<template>
  <div class="login">
    <div class="login-form">
      <h3 class="title">{{ $t("APP.TITLE") }}</h3>
      <el-radio-group
        v-if="smsLoginEnabled"
        v-model="loginMode"
        :disabled="loading || smsLoggingIn"
        class="login-mode-switch"
      >
        <el-radio-button value="password">{{ $t('Login.PasswordLogin') }}</el-radio-button>
        <el-radio-button value="sms">{{ $t('Login.SmsLogin') }}</el-radio-button>
      </el-radio-group>
      <el-form v-show="loginMode === 'password'" ref="loginRef" :model="loginForm" :rules="loginRules">
      <el-form-item prop="username">
        <el-input
          ref="usernameRef"
          v-model="loginForm.username"
          :disabled="loading"
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
          :disabled="loading"
          type="password"
          size="large"
          auto-complete="off"
          :placeholder="$t('Login.Password')"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
        <el-button v-if="loginConfig.demoMode" :disabled="loading" link type="success" @click="loginForm.username='demo';loginForm.password='a123456'">演示账号：demo / a123456</el-button>
      </el-form-item>
      <el-form-item v-if="loginConfig.captcha.enabled">
        <captcha-text
          v-if="loginConfig.captcha.type === 'Text'"
          ref="TextCaptchaRef"
          v-model="captchaData"
          :token="captchaToken"
          :expires="loginConfig.captcha.expires"
          @loading-change="captchaLoading = $event"
        />
        <captcha-math
          v-if="loginConfig.captcha.type === 'Math'"
          ref="MathCaptchaRef"
          v-model="captchaData"
          :token="captchaToken"
          :expires="loginConfig.captcha.expires"
          @loading-change="captchaLoading = $event"
        />
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">{{ $t('Login.RememberMe') }}</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          :disabled="captchaLoading"
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
      </el-form>
      <sms-login
        v-if="smsLoginEnabled"
        v-show="loginMode === 'sms'"
        :active="loginMode === 'sms' && smsLoginEnabled"
        @success="onLoginSuccess"
        @loading-change="smsLoggingIn = $event"
      />
      <div class="login-methods">
        <el-row justify="space-between" style="width: 100%;">
          <el-col :span="12">
            <language-select id="lang-select" :arrow="true" class="right-menu-item hover-effect" />
          </el-col>
          <el-col v-if="thirdLoginOptions.length" :span="12" class="third-login">
            <el-dropdown trigger="hover" placement="bottom-end" persistent>
              <el-button text>
                {{ $t('Login.OtherMethods') }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <template v-for="third in thirdLoginOptions" :key="`${third.id}`">
                    <wechat-login v-if="third.type === 'wechat'" :configId="third.id" :name="third.name" />
                    <feishu-login v-else-if="third.type === 'feishu'" :configId="third.id" :name="third.name" />
                  </template>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-col>
        </el-row>
      </div>
    </div>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>{{ $t('APP.Copyright') }}</span>
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
import SmsLogin from './components/SmsLogin'
import useUserStore from '@/store/modules/user'
import { isSmsLoginEnabled, resolveLoginMode } from '@/utils/loginTypes'

const { proxy } = getCurrentInstance()

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const loginMode = ref('password')
const smsLoggingIn = ref(false)
// 验证码配置
const captchaData = ref(null)
const captchaLoading = ref(false)
const usernameRef = ref()
const captchaToken = ref("")
// 注册开关
const register = ref(false)
const redirect = ref(undefined)

const loginForm = ref({
  username: "",
  password: "",
  rememberMe: false,
})

const normalizedUsername = computed(() => loginForm.value.username.trim())
let usernameVersion = 0
let usernameRequest
let configRequest
let disposed = false
const configLoaded = ref(false)
const loginConfig = ref({
  captcha: {
    enabled: false
  },
  thirds: [],
  loginTypes: {},
});
const smsLoginEnabled = computed(() => isSmsLoginEnabled(loginConfig.value))
const thirdLoginOptions = computed(() => (loginConfig.value.thirds || [])
  .filter(third => ['wechat', 'feishu'].includes(third.type) && third.id))

watch(smsLoginEnabled, enabled => {
  loginMode.value = resolveLoginMode(loginMode.value, loginConfig.value)
  if (!enabled) smsLoggingIn.value = false
}, { flush: 'sync' })

watch(loginMode, async mode => {
  await nextTick()
  if (disposed) return
  proxy.$refs.loginRef?.clearValidate()
  if (mode === 'password' && loginMode.value === 'password') usernameRef.value?.focus()
})

function captchaComponent() {
  return proxy.$refs[`${loginConfig.value.captcha.type}CaptchaRef`]
}

function loadLoginConfig() {
  if (configLoaded.value) return Promise.resolve()
  if (!configRequest) {
    configRequest = getLoginConfig().then(res => {
      if (!disposed) {
        loginConfig.value = res.data
        configLoaded.value = true
      }
    }).finally(() => { configRequest = undefined })
  }
  return configRequest
}

async function prepareToken() {
  const username = normalizedUsername.value
  const version = usernameVersion
  await loadLoginConfig()
  const isCurrent = () => !disposed && version === usernameVersion && username === normalizedUsername.value
  if (!isCurrent()) throw new Error(proxy.$t('Login.CaptchaAccountChanged'))
  if (!loginConfig.value.captcha.enabled) return ''
  if (!username) throw new Error(proxy.$t('Login.AccountRuleTip'))
  if (captchaToken.value === username) return username
  if (usernameRequest?.username === username && usernameRequest.version === version) {
    return usernameRequest.promise
  }
  const request = {
    username, version,
    promise: checkUsername(username).then(() => {
      if (!isCurrent()) throw new Error(proxy.$t('Login.CaptchaAccountChanged'))
      captchaToken.value = username
      return username
    })
  }
  usernameRequest = request
  try {
    return await request.promise
  } finally {
    if (usernameRequest === request) usernameRequest = undefined
  }
}

function onUserNameBlur() {
  if (normalizedUsername.value) prepareToken().catch(() => {})
}

watch(normalizedUsername, () => {
  usernameVersion++
  usernameRequest = undefined
  captchaComponent()?.resetCaptcha()
  captchaToken.value = ''
  captchaData.value = null
  captchaLoading.value = false
}, { flush: 'sync' })

const loginRules = {
  username: [{ required: true, whitespace: true, trigger: "blur", message: proxy.$t('Common.RuleTips.NotEmpty') }],
  password: [{ required: true, trigger: "blur", message: proxy.$t('Login.PasswordRuleTip') }]
}

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

onMounted(async () => {
  await nextTick()
  usernameRef.value?.focus()
  try {
    await loadLoginConfig()
    if (!disposed && normalizedUsername.value) await prepareToken()
  } catch {
    // Request errors are reported by the shared request interceptor; blur can retry.
  }
})

onBeforeUnmount(() => {
  disposed = true
  usernameVersion++
})

async function handleLogin() {
  if (disposed || loading.value || smsLoggingIn.value || loginMode.value !== 'password') return
  loading.value = true
  try {
    await loadLoginConfig()
    const valid = await proxy.$refs.loginRef.validate().catch(() => false)
    if (!valid) return
    if (loginConfig.value.captcha.enabled && (
      captchaLoading.value || captchaData.value?.token !== normalizedUsername.value ||
      !captchaComponent()?.isValid()
    )) {
      proxy.$modal.msgError(proxy.$t('Login.CaptchaRequired'))
      return
    }
    const loginData = {
      ...loginForm.value,
      username: normalizedUsername.value,
      captcha: loginConfig.value.captcha.enabled ? JSON.parse(JSON.stringify(captchaData.value)) : null
    }
    if (loginData.rememberMe) {
      Cookies.set("username", loginData.username, { expires: 30 })
      Cookies.set("password", encrypt(loginData.password), { expires: 30 })
      Cookies.set("rememberMe", loginData.rememberMe, { expires: 30 })
    } else {
      Cookies.remove("username")
      Cookies.remove("password")
      Cookies.remove("rememberMe")
    }
    try {
      await userStore.login(loginData)
      onLoginSuccess()
    } catch {
      if (loginConfig.value.captcha.enabled) {
        captchaComponent()?.resetCaptcha()
        if (['Text', 'Math'].includes(loginConfig.value.captcha.type)) {
          captchaComponent()?.reloadCaptcha()
        }
      }
    }
  } catch {
    // Configuration/request failures have already been reported.
  } finally {
    loading.value = false
  }
}

function onLoginSuccess() {
  const otherQueryParams = Object.keys(route.query).reduce((acc, key) => {
    if (key !== 'redirect') acc[key] = route.query[key]
    return acc
  }, {})
  router.push({ path: redirect.value || '/', query: otherQueryParams })
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

.login-mode-switch {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
  width: 100%;
}

.login-methods {
  margin-bottom: 22px;
}

.third-login {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
.third-login-icon {
  width: 18px;
  height: 18px;
  margin-right: 8px;
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