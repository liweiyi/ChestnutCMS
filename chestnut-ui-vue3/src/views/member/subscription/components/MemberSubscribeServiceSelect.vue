<template>
  <div class="member-subscribe-service-select">
    <el-select :model-value="modelValue" multiple filterable remote clearable :multiple-limit="100"
      :remote-method="loadOptions" :loading="isLoading" :disabled="disabled" style="width: 100%"
      :placeholder="$t('MemberSubscribeService.SelectServices')"
      :no-data-text="$t(hasFailed ? 'MemberSubscription.LoadFailed' : 'MemberSubscription.NoItems')"
      @update:model-value="handleChange">
      <el-option v-for="option in displayedOptions" :key="option.value" :value="option.value" :label="option.label">
        <span>{{ option.label }}</span>
        <el-text type="info" class="member-subscribe-service-select__id" size="small">{{ option.value }}</el-text>
      </el-option>
    </el-select>
    <el-text v-if="hasFailed" type="danger" size="small">
      {{ $t('MemberSubscription.LoadFailed') }}
      <el-button link type="primary" @click="handleRetry">{{ $t('Common.Refresh') }}</el-button>
    </el-text>
  </div>
</template>

<script setup name="MemberSubscribeServiceSelect">
import { listMemberSubscribeServiceOptions } from '@/api/member/subscribeService'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  disabled: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])
const options = ref([])
const optionCache = reactive(new Map())
const isLoading = ref(false)
const hasOptionsFailed = ref(false)
const hasSelectionFailed = ref(false)
const hasFailed = computed(() => hasOptionsFailed.value || hasSelectionFailed.value)
const displayedOptions = computed(() => {
  const selected = props.modelValue.map(value => optionCache.get(value) ?? { value, label: value })
  return [...new Map([...selected, ...options.value].map(option => [option.value, option])).values()]
})
let requestVersion = 0
let selectionVersion = 0
let isAlive = true

watch(() => [...props.modelValue], loadSelected, { immediate: true })
onMounted(() => loadOptions(''))
onBeforeUnmount(() => { isAlive = false; requestVersion += 1; selectionVersion += 1 })

async function loadSelected() {
  const version = ++selectionVersion
  hasSelectionFailed.value = false
  const ids = props.modelValue.filter(id => !optionCache.has(id))
  if (ids.length === 0) return
  try {
    const response = await listMemberSubscribeServiceOptions({ ids })
    if (!isAlive || version !== selectionVersion) return
    for (const option of response.data ?? []) optionCache.set(option.value, option)
  } catch {
    if (isAlive && version === selectionVersion) hasSelectionFailed.value = true
  }
}

async function loadOptions(keyword) {
  const version = ++requestVersion
  isLoading.value = true
  hasOptionsFailed.value = false
  try {
    const response = await listMemberSubscribeServiceOptions({ keyword })
    if (!isAlive || version !== requestVersion) return
    options.value = response.data ?? []
    for (const option of options.value) optionCache.set(option.value, option)
  } catch {
    if (isAlive && version === requestVersion) {
      options.value = []
      hasOptionsFailed.value = true
    }
  } finally {
    if (isAlive && version === requestVersion) isLoading.value = false
  }
}

function handleChange(value) {
  emit('update:modelValue', [...value])
}

function handleRetry() {
  return Promise.all([loadOptions(''), loadSelected()])
}
</script>

<style scoped>
.member-subscribe-service-select { width: 100%; }
.member-subscribe-service-select__id { margin-left: 8px; }
</style>
