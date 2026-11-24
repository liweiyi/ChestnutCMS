<template>
  <div class="member-subscription-definition-select">
    <el-select :model-value="modelValue || undefined" :disabled="disabled" :clearable="clearable" filterable remote :remote-method="loadOptions" :loading="isLoading" :placeholder="$t('MemberSubscription.SelectDefinition')" :no-data-text="$t(hasFailed ? 'MemberSubscription.LoadFailed' : 'MemberSubscription.NoDefinitions')" style="width: 100%" @update:model-value="handleChange" @visible-change="handleVisible">
      <el-option v-for="option in displayedOptions" :key="option.value" :value="option.value" :label="option.label" />
    </el-select>
    <el-text v-if="hasFailed" type="danger" size="small">{{ $t('MemberSubscription.LoadFailed') }} <el-button link type="primary" @click="loadOptions('')">{{ $t('Common.Refresh') }}</el-button></el-text>
  </div>
</template>

<script setup name="MemberSubscriptionDefinitionSelect">
import { listMemberSubscriptionOptions } from '@/api/member/subscription'

const props = defineProps({
  modelValue: { type: String, default: '' },
  selectedLabel: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  clearable: { type: Boolean, default: true }
})
const emit = defineEmits(['update:modelValue'])
const options = ref([])
const selectedOption = ref(null)
const isLoading = ref(false)
const hasFailed = ref(false)
const displayedOptions = computed(() => {
  if (!props.modelValue || options.value.some(option => option.value === props.modelValue)) return options.value
  return [{ value: props.modelValue, label: props.selectedLabel || selectedOption.value?.label || props.modelValue }, ...options.value]
})
let requestVersion = 0
let selectedVersion = 0
let isAlive = true

watch(() => props.modelValue, async value => {
  const version = ++selectedVersion
  selectedOption.value = options.value.find(option => option.value === value) ?? null
  if (!value || selectedOption.value || props.selectedLabel) return
  try {
    const response = await listMemberSubscriptionOptions({ ids: [value] })
    if (isAlive && version === selectedVersion && value === props.modelValue) selectedOption.value = response.data?.find(option => option.value === value) ?? null
  } catch { if (isAlive && version === selectedVersion) hasFailed.value = true }
}, { immediate: true })
onMounted(() => { if (!props.disabled) loadOptions('') })
onBeforeUnmount(() => { isAlive = false; requestVersion += 1; selectedVersion += 1 })

async function loadOptions(keyword) {
  const version = ++requestVersion
  isLoading.value = true
  hasFailed.value = false
  try {
    const response = await listMemberSubscriptionOptions({ keyword })
    if (!isAlive || version !== requestVersion) return
    options.value = response.data ?? []
    const current = options.value.find(option => option.value === props.modelValue)
    if (current) selectedOption.value = current
  } catch {
    if (isAlive && version === requestVersion) { options.value = []; hasFailed.value = true }
  } finally { if (isAlive && version === requestVersion) isLoading.value = false }
}
function handleChange(value) {
  selectedOption.value = options.value.find(option => option.value === value) ?? null
  emit('update:modelValue', value || '')
}
function handleVisible(open) { if (open) return loadOptions('') }
</script>

<style scoped>
.member-subscription-definition-select { width: 100%; }
</style>
