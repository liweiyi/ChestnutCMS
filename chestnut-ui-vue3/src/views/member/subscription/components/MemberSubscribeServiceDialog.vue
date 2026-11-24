<template>
  <el-dialog :model-value="modelValue" :title="$t(isEditing ? 'MemberSubscribeService.Edit' : 'MemberSubscribeService.Add')"
    width="min(600px, 94vw)" append-to-body :close-on-click-modal="false" :close-on-press-escape="!isSaving"
    :show-close="!isSaving" @update:model-value="handleVisible" @closed="handleClosed">
    <el-alert v-if="hasFailed" class="mb8" type="error" :closable="false" :title="$t('MemberSubscription.LoadFailed')" />
    <el-form ref="formRef" v-loading="isLoading" :model="form" :rules="rules" label-position="top"
      :disabled="isSaving || isLoading" :validate-on-rule-change="false" @submit.prevent>
      <el-form-item :label="$t('MemberSubscribeService.Type')">
        <el-text v-if="isEditing">{{ $t(isBuiltIn ? 'MemberSubscribeService.BuiltIn' : 'MemberSubscribeService.Custom') }}</el-text>
        <el-radio-group v-else v-model="form.sourceType" @change="handleTypeChange">
          <el-radio value="builtIn">{{ $t('MemberSubscribeService.BuiltIn') }}</el-radio>
          <el-radio value="custom">{{ $t('MemberSubscribeService.Custom') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="isBuiltIn && !isEditing" :label="$t('MemberSubscribeService.BuiltInItem')" prop="builtInId">
        <el-select v-model="form.builtInId" filterable clearable style="width: 100%" :placeholder="$t('MemberSubscribeService.SelectBuiltIn')">
          <el-option v-for="item in builtInItems" :key="item.id" :label="`${item.name} (${item.id})`" :value="item.id" />
        </el-select>
      </el-form-item>
      <template v-else>
        <el-form-item :label="$t('MemberSubscribeService.Id')" prop="serviceId">
          <el-text v-if="isEditing">{{ form.serviceId }}</el-text>
          <el-input v-else v-model="form.serviceId" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item :label="$t('MemberSubscribeService.Name')" prop="name">
          <el-text v-if="isBuiltIn">{{ form.name }}</el-text>
          <el-input v-else v-model.trim="form.name" maxlength="100" show-word-limit />
        </el-form-item>
      </template>
      <el-form-item :label="$t('Common.Remark')" prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="400" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-if="canSave" type="primary" :loading="isSaving" :disabled="!hasLoaded || isLoading" @click="handleSave">{{ $t('Common.Save') }}</el-button>
      <el-button :disabled="isSaving" @click="handleVisible(false)">{{ $t('Common.Cancel') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup name="MemberSubscribeServiceDialog">
import { getMemberSubscribeService, addMemberSubscribeService, updateMemberSubscribeService } from '@/api/member/subscribeService'
import { listMemberSubscriptionServiceItems } from '@/api/member/subscription'
import { checkPermi } from '@/utils/permission'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  serviceId: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue', 'success'])
const { proxy } = getCurrentInstance()
const defaults = () => ({ sourceType: 'builtIn', builtInId: '', serviceId: '', name: '', remark: '' })
const formRef = ref()
const form = reactive(defaults())
const builtInItems = ref([])
const isLoading = ref(false)
const isSaving = ref(false)
const hasLoaded = ref(false)
const hasFailed = ref(false)
const isEditing = computed(() => Boolean(props.serviceId))
const isBuiltIn = computed(() => form.sourceType === 'builtIn')
const canSave = computed(() => checkPermi([isEditing.value ? 'member:subscription:definition:edit' : 'member:subscription:definition:add']))
const rules = computed(() => ({
  builtInId: isBuiltIn.value && !isEditing.value
    ? [{ required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'change' }] : [],
  serviceId: !isBuiltIn.value && !isEditing.value
    ? [{ required: true, pattern: /^[A-Za-z0-9_]{1,100}$/, message: proxy.$t('MemberSubscribeService.IdRule'), trigger: 'blur' }] : [],
  name: !isBuiltIn.value ? [
    { required: true, whitespace: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [100]), trigger: 'blur' }
  ] : [],
  remark: [{ max: 400, message: proxy.$t('Common.RuleTips.MaxLength', [400]), trigger: 'blur' }]
}))
let requestVersion = 0

watch([() => props.modelValue, () => props.serviceId], ([open]) => {
  if (open) return loadForm()
  requestVersion += 1
  isLoading.value = false
}, { immediate: true })
onBeforeUnmount(() => { requestVersion += 1 })

async function loadForm() {
  const version = ++requestVersion
  Object.assign(form, defaults())
  builtInItems.value = []
  hasLoaded.value = false
  hasFailed.value = false
  isLoading.value = true
  try {
    const [items, detail] = await Promise.all([
      listMemberSubscriptionServiceItems(),
      isEditing.value ? getMemberSubscribeService(props.serviceId) : Promise.resolve({ data: {} })
    ])
    if (version !== requestVersion || !props.modelValue) return
    builtInItems.value = items.data ?? []
    if (isEditing.value) {
      const entity = detail.data
      Object.assign(form, {
        serviceId: entity.serviceId,
        name: entity.name,
        remark: entity.remark ?? '',
        sourceType: builtInItems.value.some(item => item.id === entity.serviceId) ? 'builtIn' : 'custom'
      })
    }
    hasLoaded.value = true
    await nextTick()
    formRef.value?.clearValidate()
  } catch {
    if (version === requestVersion) hasFailed.value = true
  } finally {
    if (version === requestVersion) isLoading.value = false
  }
}

async function handleTypeChange() {
  Object.assign(form, { builtInId: '', serviceId: '', name: '' })
  await nextTick()
  formRef.value?.clearValidate()
}

async function handleSave() {
  if (!canSave.value || isSaving.value || !hasLoaded.value) return
  if (!await formRef.value?.validate().catch(() => false)) return
  const data = {
    serviceId: isEditing.value ? props.serviceId : isBuiltIn.value ? form.builtInId : form.serviceId,
    remark: form.remark,
    ...(!isBuiltIn.value ? { name: form.name.trim() } : {})
  }
  isSaving.value = true
  try {
    if (isEditing.value) await updateMemberSubscribeService(data)
    else await addMemberSubscribeService(data)
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
