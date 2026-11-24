<template>
  <el-dialog :model-value="modelValue" :title="$t(isEditing ? 'MemberSubscription.EditDefinition' : 'MemberSubscription.AddDefinition')" width="min(720px, 94vw)" append-to-body :close-on-click-modal="false" :close-on-press-escape="!isSaving" :show-close="!isSaving" @update:model-value="handleVisible" @closed="handleClosed">
    <el-alert class="mb8" type="info" :closable="false" show-icon :title="$t('MemberSubscription.ScopeNotice')" />
    <el-alert v-if="hasFailed" class="mb8" type="error" :closable="false" :title="$t('MemberSubscription.LoadFailed')" />
    <el-form v-loading="isLoading" ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="isSaving || isLoading" @submit.prevent>
      <el-form-item v-if="isEditing" :label="$t('MemberSubscription.DefinitionId')">
        <el-text>{{ form.subscriptionId }}</el-text>
      </el-form-item>
      <el-form-item :label="$t('MemberSubscription.Name')" prop="subscriptionName"><el-input v-model.trim="form.subscriptionName" maxlength="128" show-word-limit /></el-form-item>
      <el-form-item :label="$t('MemberSubscription.Description')" prop="description"><el-input v-model="form.description" type="textarea" :rows="3" maxlength="1000" show-word-limit /></el-form-item>
      <el-form-item :label="$t('Common.Remark')"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" show-word-limit /></el-form-item>
      <el-form-item :label="$t('MemberSubscription.Items')" prop="serviceIds">
        <member-subscribe-service-select v-if="hasLoaded" v-model="form.serviceIds" :disabled="isSaving || isLoading" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-if="canSave" type="primary" :loading="isSaving" :disabled="!hasLoaded || isLoading" @click="handleSave">{{ $t('Common.Save') }}</el-button>
      <el-button :disabled="isSaving" @click="handleVisible(false)">{{ $t('Common.Cancel') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="MemberSubscriptionDefinitionDialog">
import {
  getMemberSubscriptionDefinition,
  addMemberSubscriptionDefinition,
  updateMemberSubscriptionDefinition
} from '@/api/member/subscription'
import {
  checkPermi
} from '@/utils/permission'
import { buildSubscriptionDefinition } from '../formValues'
import MemberSubscribeServiceSelect from './MemberSubscribeServiceSelect.vue'

const defaults = () => ({
  subscriptionId: '',
  subscriptionName: '',
  description: '',
  remark: '',
  serviceIds: []
})
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  subscriptionId: {
    type: String,
    default: ''
  }
})
const emit = defineEmits(['update:modelValue', 'success'])
const {
  proxy
} = getCurrentInstance()
const formRef = ref()
const form = reactive(defaults())
const isLoading = ref(false)
const isSaving = ref(false)
const hasLoaded = ref(false)
const hasFailed = ref(false)
const isEditing = computed(() => Boolean(props.subscriptionId))
const canSave = computed(() => checkPermi([isEditing.value ? 'member:subscription:definition:edit' :
  'member:subscription:definition:add'
]))
const rules = computed(() => ({
  serviceIds: [{ type: 'array', max: 100, message: proxy.$t('MemberSubscribeService.SelectionLimit'), trigger: 'change' }],
  subscriptionName: [{
    required: true,
    message: proxy.$t('Common.RuleTips.NotEmpty'),
    trigger: 'blur'
  }, {
    max: 128,
    message: proxy.$t('Common.RuleTips.MaxLength', [128]),
    trigger: 'blur'
  }]
}))
let requestVersion = 0

watch([() => props.modelValue, () => props.subscriptionId], ([open]) => {
  if (open) return loadForm()
  requestVersion += 1
  isLoading.value = false
}, {
  immediate: true
})
onBeforeUnmount(() => {
  requestVersion += 1
})

async function loadForm() {
  const version = ++requestVersion
  Object.assign(form, defaults())
  hasLoaded.value = false
  hasFailed.value = false
  isLoading.value = true
  try {
    const detail = isEditing.value ? await getMemberSubscriptionDefinition(props.subscriptionId) : { data: defaults() }
    if (version !== requestVersion || !props.modelValue) return
    Object.assign(form, Object.fromEntries(Object.entries(defaults()).map(([key, fallback]) => [key, detail.data[key] ?? fallback])), {
      subscriptionId: detail.data.definitionId ?? '',
      serviceIds: [...(detail.data.serviceIds ?? [])]
    })
    hasLoaded.value = true
    await nextTick()
    formRef.value?.clearValidate()
  } catch (error) {
    if (version === requestVersion) hasFailed.value = true
  } finally {
    if (version === requestVersion) isLoading.value = false
  }
}
async function handleSave() {
  if (!canSave.value || isSaving.value || !hasLoaded.value) return
  if (!(await formRef.value?.validate().catch(() => false))) return
  isSaving.value = true
  try {
    const data = buildSubscriptionDefinition(form, isEditing.value)
    if (isEditing.value) await updateMemberSubscriptionDefinition(data)
    else await addMemberSubscriptionDefinition(data)
    proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'))
    emit('update:modelValue', false)
    emit('success')
  } finally {
    isSaving.value = false
  }
}

function handleVisible(value) {
  if (!isSaving.value) emit('update:modelValue', value)
}

function handleClosed() {
  Object.assign(form, defaults())
  hasLoaded.value = false
  formRef.value?.clearValidate()
}
</script>
