<template>
  <el-dialog
    v-model="visible"
    :title="$t('AccountStatus.LockTitle')"
    width="500px"
    append-to-body
    :close-on-click-modal="false"
    :close-on-press-escape="!submitting"
    :show-close="!submitting"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
      <el-form-item :label="$t('AccountStatus.Account')">
        <el-text>{{ account }}</el-text>
      </el-form-item>
      <el-form-item :label="$t('AccountStatus.LockEndTime')" prop="lockEndTime">
        <el-date-picker
          v-model="form.lockEndTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          :placeholder="$t('AccountStatus.LockEndTimePlaceholder')"
          :disabled="submitting"
          style="width: 100%"
        />
        <el-text type="info" size="small">{{ $t('AccountStatus.ExpiredTimeHint') }}</el-text>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ $t('Common.Confirm') }}</el-button>
      <el-button :disabled="submitting" @click="visible = false">{{ $t('Common.Cancel') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="AccountStatusDialog">
import { getCurrentInstance, nextTick, reactive, ref, watch } from 'vue'
import { isValidLockEndTime } from '@/utils/accountStatus'

const props = defineProps({
  account: { type: String, default: '' },
  lockEndTime: { type: String, default: '' },
  submit: { type: Function, required: true }
})
const visible = defineModel('open', { type: Boolean, default: false })
const { proxy } = getCurrentInstance()
const formRef = ref()
const form = reactive({ lockEndTime: '' })
const submitting = ref(false)
const rules = {
  lockEndTime: [{
    required: true,
    validator: (_rule, value, callback) => callback(isValidLockEndTime(value)
      ? undefined : new Error(proxy.$t('AccountStatus.ValidTimeRequired'))),
    trigger: ['change', 'blur']
  }]
}

watch(visible, async open => {
  if (open) {
    form.lockEndTime = isValidLockEndTime(props.lockEndTime) ? props.lockEndTime : ''
    await nextTick()
    formRef.value?.clearValidate()
  }
})

async function handleSubmit() {
  if (submitting.value) return
  submitting.value = true
  try {
    if (await formRef.value.validate().catch(() => false)) {
      await props.submit(form.lockEndTime)
    }
  } finally {
    submitting.value = false
  }
}
</script>
