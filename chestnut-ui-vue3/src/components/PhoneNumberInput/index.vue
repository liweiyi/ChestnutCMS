<template>
  <el-input
    ref="inputRef"
    v-bind="$attrs"
    :model-value="nationalNumber"
    :disabled="disabled"
    :size="size"
    type="tel"
    inputmode="tel"
    autocomplete="tel-national"
    @update:model-value="updateNationalNumber"
    @change="emit('change', nationalNumber)"
    @blur="emit('blur', $event)"
  >
    <template #prefix>
      <el-select
        v-model="countryCode"
        class="phone-country-select"
        :disabled="disabled"
        :size="size"
        :aria-label="$t('Common.CountryCallingCode')"
        :popper-style="{ width: 'min(420px, calc(100vw - 32px))' }"
        filterable
        @change="updateCallingCode"
      >
        <template #label>{{ countryCode }}</template>
        <el-option
          v-for="country in countryOptions"
          :key="country.code"
          :label="`${country.name} ${country.code}`"
          :value="country.code"
        >
          <span class="phone-country-option" :title="`${country.name} ${country.code}`">
            <span class="phone-country-code">{{ country.code }}</span>
            <span class="phone-country-name">{{ country.name }}</span>
          </span>
        </el-option>
      </el-select>
    </template>
  </el-input>
</template>

<script setup name="PhoneNumberInput">
import { computed, ref, watch } from 'vue'
import { PHONE_COUNTRY_CODES } from '@/utils/phoneCountryCodes'
import { normalizePhoneParts } from '@/utils/phoneNumber'

defineOptions({ inheritAttrs: false })
const props = defineProps({
  modelValue: { type: String, default: '' },
  phoneCallingCode: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  size: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue', 'update:phoneCallingCode', 'change', 'blur'])
const inputRef = ref()
const countryCode = ref('')
const nationalNumber = ref('')
const countryOptions = computed(() => !countryCode.value || PHONE_COUNTRY_CODES.some(item => item.code === countryCode.value)
  ? PHONE_COUNTRY_CODES : [{ code: countryCode.value, name: countryCode.value }, ...PHONE_COUNTRY_CODES])

function emitValue() {
  const parts = nationalNumber.value.trim().startsWith('+') ? normalizePhoneParts(nationalNumber.value)
    : normalizePhoneParts(nationalNumber.value, countryCode.value)
  countryCode.value = parts.phoneCallingCode
  nationalNumber.value = parts.phoneNumber
  emit('update:phoneCallingCode', parts.phoneCallingCode)
  emit('update:modelValue', parts.phoneNumber)
}

function updateNationalNumber(value) {
  nationalNumber.value = value
  emitValue()
}

function updateCallingCode() {
  emit('update:phoneCallingCode', countryCode.value)
  emit('change', nationalNumber.value)
}

watch(() => [props.modelValue, props.phoneCallingCode], ([value, code]) => {
  const parts = normalizePhoneParts(value, code)
  countryCode.value = parts.phoneCallingCode
  nationalNumber.value = parts.phoneNumber
}, { immediate: true })

defineExpose({ focus: () => inputRef.value?.focus() })
</script>

<style scoped>
.phone-country-select { width: 70px; }
.phone-country-option { display: flex; gap: 8px; }
.phone-country-code { flex: 0 0 48px; }
.phone-country-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.phone-country-select :deep(.el-select__wrapper) {
  padding-left: 0;
  /* 区号作为输入框前缀，不单独显示表单校验边框。 */
  box-shadow: none !important;
  background: transparent;
}
</style>
