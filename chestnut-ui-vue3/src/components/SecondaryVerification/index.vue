<template>
  <el-dialog
    v-model="visible"
    :title="$t('Common.SecondaryVerification')"
    width="420px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleCancel"
    append-to-body
    destroy-on-close
  >
    <el-tabs v-model="activeType">
      <el-tab-pane
        v-for="item in typeOptions"
        :key="item.value"
        :label="item.label"
        :name="item.value"
      />
    </el-tabs>

    <el-form
      v-if="activeType === 'password'"
      ref="formRef"
      :model="formData"
      :rules="rules"
      @submit.prevent="handleSubmit"
    >
      <el-form-item prop="password">
        <el-input
          v-model="formData.password"
          type="password"
          :placeholder="$t('Common.InputPassword')"
          show-password
          @keyup.enter="handleSubmit"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleCancel">{{ $t('Common.Cancel') }}</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ $t('Common.Confirm') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { getVerificationTypes,verify } from '@/api/system/secondaryVerification'

const visible = ref(false)
const loading = ref(false)
const activeType = ref('password')
const formRef = ref(null)
const typeOptions = ref([])

const formData = reactive({
  password: ''
})

const rules = {
  password: [{ required: true, message: () => proxy.$t('Common.Required'), trigger: 'blur' }]
}

let resolveCallback = null
let rejectCallback = null

onMounted(() => {
  getVerificationTypes().then(res => {
    typeOptions.value = res.data
    if (typeOptions.value.length > 0) {
      activeType.value = typeOptions.value[0].value
    }
  })
})

function open(verifyTypes) {
  formData.password = ''
  visible.value = true

  return new Promise((resolve, reject) => {
    resolveCallback = resolve
    rejectCallback = reject
  })
}

async function handleSubmit() {
  if (activeType.value === 'password') {
    try {
      await formRef.value.validate()
    } catch {
      return
    }
  }

  loading.value = true
  try {
    const res = await verify({
      type: activeType.value,
      params: activeType.value === 'password' ? { password: formData.password } : {}
    })
    visible.value = false
    if (resolveCallback) {
      resolveCallback(res.data)
    }
  } catch {
    // verify API failure is handled by global interceptor
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  visible.value = false
  if (rejectCallback) {
    rejectCallback(new Error('cancelled'))
  }
}

defineExpose({ open })
</script>
