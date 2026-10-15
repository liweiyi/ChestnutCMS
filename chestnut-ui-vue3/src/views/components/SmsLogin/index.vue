<template>
  <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
    <el-form-item prop="phoneNumber">
      <phone-number-input
        ref="phoneRef"
        v-model="form.phoneNumber"
        v-model:phone-calling-code="form.phoneCallingCode"
        :disabled="!active || loggingIn"
        :placeholder="$t('Login.PhoneNumber')"
        size="large"
      />
    </el-form-item>
    <el-form-item prop="code">
      <el-input
        v-model="form.code"
        :disabled="!active || loggingIn"
        :placeholder="$t('Login.SmsCode')"
        type="text"
        inputmode="numeric"
        autocomplete="one-time-code"
        maxlength="6"
        size="large"
        @keyup.enter="handleLogin"
      >
        <template #prefix><svg-icon icon-class="validCode" class="input-icon" /></template>
        <template #append>
          <el-button
            :loading="sending"
            :disabled="!active || loggingIn || isRefreshDisabled || !validSmsPhoneNumber(form.phoneNumber, form.phoneCallingCode)"
            @click="handleSendCode"
          >{{ sendButtonText }}</el-button>
        </template>
      </el-input>
    </el-form-item>
    <el-form-item>
      <el-button :loading="loggingIn" :disabled="!active" size="large" type="primary" class="login-button" @click="handleLogin">
        {{ $t(loggingIn ? 'Login.Logining' : 'Login.Login') }}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup name="SmsLogin">
import { sendSmsCode } from '@/api/login'
import useUserStore from '@/store/modules/user'
import useCaptchaCountdown from '../captcha/useCaptchaCountdown'
import { createSmsCodeRequests, validSmsCode, validSmsPhoneNumber } from './smsCodeRequests'
import PhoneNumberInput from '@/components/PhoneNumberInput'
import { normalizePhoneNumber, normalizePhoneParts } from '@/utils/phoneNumber'

const props = defineProps({
  active: { type: Boolean, default: false }
})
const emit = defineEmits(['success', 'loading-change'])
const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const formRef = ref()
const phoneRef = ref()
const form = reactive({ phoneCallingCode: '+86', phoneNumber: '', code: '' })
const normalizedPhone = computed(() => normalizePhoneNumber(form.phoneNumber, form.phoneCallingCode))
const sending = ref(false)
const loggingIn = ref(false)
const sent = ref(false)
const { leftSeconds, isRefreshDisabled, startCountdown, selectToken } = useCaptchaCountdown(sending)
let disposed = false

const requests = createSmsCodeRequests({
  sendCode: sendSmsCode,
  onSent(data, phoneNumber) {
    startCountdown(data.retryDuration ?? 0, phoneNumber)
  },
  onPendingChange() {
    sending.value = requests.isPending()
  }
})

const rules = {
  phoneNumber: [{
    validator: (_rule, value, callback) => callback(validSmsPhoneNumber(value, form.phoneCallingCode)
      ? undefined : new Error(proxy.$t('Login.PhoneNumberRuleTip'))),
    trigger: 'blur'
  }],
  code: [{
    validator: (_rule, value, callback) => callback(validSmsCode(value.trim())
      ? undefined : new Error(proxy.$t('Login.SmsCodeRuleTip'))),
    trigger: 'blur'
  }]
}

const sendButtonText = computed(() => {
  if (sending.value) return proxy.$t('Login.SendingSmsCode')
  if (leftSeconds.value > 0) return proxy.$t('Login.SmsResendWaiting', [leftSeconds.value])
  return proxy.$t(sent.value ? 'Login.ResendSmsCode' : 'Login.SendSmsCode')
})

watch(() => [form.phoneCallingCode, form.phoneNumber], () => {
  form.code = ''
  sent.value = false
  requests.selectPhone(form.phoneNumber, form.phoneCallingCode)
  selectToken(normalizedPhone.value)
  formRef.value?.clearValidate('code')
}, { immediate: true, flush: 'sync' })

watch(() => props.active, async active => {
  if (!active) form.code = ''
  await nextTick()
  if (disposed) return
  formRef.value?.clearValidate()
  if (active) {
    if (props.active) phoneRef.value?.focus()
  }
}, { immediate: true })
watch(loggingIn, value => emit('loading-change', value), { flush: 'sync' })

async function handleSendCode() {
  if (!props.active || loggingIn.value || isRefreshDisabled.value) return
  const valid = await formRef.value.validateField('phoneNumber').catch(() => false)
  if (!valid || disposed || !props.active || loggingIn.value || isRefreshDisabled.value) return
  try {
    const result = await requests.send()
    if (!result?.current || disposed) return
    sent.value = true
    if (props.active) proxy.$modal.msgSuccess(proxy.$t('Login.SmsCodeSent'))
  } catch {
    // The shared request interceptor reports sending failures.
  }
}

async function handleLogin() {
  if (!props.active || disposed || loggingIn.value) return
  loggingIn.value = true
  try {
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid || disposed || !props.active) return
    await userStore.smsLogin({ ...normalizePhoneParts(form.phoneNumber, form.phoneCallingCode), code: form.code.trim() })
    if (!disposed) emit('success')
  } catch {
    // The shared request interceptor reports login failures.
  } finally {
    loggingIn.value = false
  }
}

onBeforeUnmount(() => {
  disposed = true
  requests.dispose()
})
</script>

<style scoped>
.login-button { width: 100%; }
.input-icon { width: 1rem; height: 1rem; }
</style>
