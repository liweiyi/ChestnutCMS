<template>
  <el-dialog :model-value="modelValue" :title="$t(record ? 'MemberSubscription.Adjust' : 'MemberSubscription.AddRecord')" width="min(650px, 94vw)" append-to-body :close-on-click-modal="false" :show-close="!isSaving" :close-on-press-escape="!isSaving" @update:model-value="handleVisible" @closed="handleClosed">
    <el-alert class="mb8" type="info" :closable="false" :title="$t('MemberSubscription.AdjustNotice')" show-icon />
    <el-form ref="formRef" :model="form" :rules="rules" :disabled="isSaving" label-position="top" @submit.prevent>
      <el-form-item :label="$t('MemberSubscription.MemberId')" prop="memberId">
        <el-input v-model.trim="form.memberId" :disabled="Boolean(record)" maxlength="19"><template v-if="!record" #append><el-button v-hasPermi="['member:view']" @click="isMemberSelectorOpen = true">{{ $t('MemberSubscription.SelectMember') }}</el-button></template></el-input>
      </el-form-item>
      <el-form-item :label="$t('MemberSubscription.DefinitionTitle')" prop="subscriptionId">
        <member-subscription-definition-select v-model="form.subscriptionId" :selected-label="record?.subscriptionName ?? ''" :disabled="Boolean(record) || isSaving" :clearable="!record" />
      </el-form-item>
      <el-form-item v-if="record" :label="$t('MemberSubscription.ExpireTime')">{{ formatSubscriptionTime(record.expireTime, 'seconds') }}</el-form-item>
      <el-form-item :label="$t('MemberSubscription.DeltaSeconds')" prop="deltaSeconds">
        <div class="member-subscription-adjust__duration">
          <el-input v-model.trim="form.deltaSeconds" maxlength="20" inputmode="text" />
          <el-space class="member-subscription-adjust__shortcuts" :size="6" wrap>
            <el-button v-for="shortcut in durationShortcuts" :key="shortcut.seconds" size="small" plain
              @click="applyDurationShortcut(shortcut.seconds)">
              {{ $t(`MemberSubscription.DurationShortcuts.${shortcut.label}`) }}
            </el-button>
          </el-space>
        </div>
      </el-form-item>
      <el-text v-if="!record" type="info" size="small">{{ $t('MemberSubscription.FirstNotice') }}</el-text>
      <el-form-item :label="$t('MemberSubscription.Reason')" prop="reason"><el-input v-model.trim="form.reason" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
    </el-form>
    <el-text type="info" size="small">{{ $t('MemberSubscription.RetryNotice') }}</el-text>
    <template #footer><el-button v-if="canAdjust" type="primary" :loading="isSaving" @click="handleSave">{{ $t('Common.Confirm') }}</el-button><el-button :disabled="isSaving" @click="handleVisible(false)">{{ $t('Common.Cancel') }}</el-button></template>
  </el-dialog>
  <member-selector v-if="isMemberSelectorOpen" v-model:open="isMemberSelectorOpen" @ok="handleMembers" />
</template>

<script setup name="MemberSubscriptionAdjustDialog">
import { adjustMemberSubscription } from '@/api/member/subscription'
import { checkPermi } from '@/utils/permission'
import MemberSelector from '@/views/member/memberSelector.vue'
import MemberSubscriptionDefinitionSelect from '@/views/components/MemberSubscriptionDefinitionSelect/index.vue'
import { isSubscriptionSeconds, isSubscriptionMemberId, formatSubscriptionTime, createSubscriptionRequestId, buildSubscriptionAdjustment } from '../formValues'

const defaults = () => ({ memberId: '', subscriptionId: '', deltaSeconds: '0', reason: '' })
const props = defineProps({ modelValue: { type: Boolean, default: false }, record: { type: Object, default: null } })
const emit = defineEmits(['update:modelValue', 'success'])
const { proxy } = getCurrentInstance()
const durationShortcuts = [
  { label: 'OneHour', seconds: '3600' },
  { label: 'TwelveHours', seconds: '43200' },
  { label: 'OneDay', seconds: '86400' },
  { label: 'SevenDays', seconds: '604800' },
  { label: 'ThirtyDays', seconds: '2592000' },
  { label: 'Days366', seconds: '31622400' }
]
const formRef = ref()
const form = reactive(defaults())
const isSaving = ref(false)
const isMemberSelectorOpen = ref(false)
const canAdjust = computed(() => checkPermi(['member:subscription:adjust']))
const rules = computed(() => ({
  memberId: [{ validator: (rule, value, callback) => isSubscriptionMemberId(value) ? callback() : callback(new Error(proxy.$t('MemberSubscription.MemberRule'))), trigger: 'blur' }],
  subscriptionId: [{ required: true, message: proxy.$t('MemberSubscription.SelectDefinition'), trigger: 'change' }],
  deltaSeconds: [{ validator: (rule, value, callback) => isSubscriptionSeconds(value) ? callback() : callback(new Error(proxy.$t('MemberSubscription.SecondsRule'))), trigger: 'blur' }],
  reason: [{ required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' }]
}))
let prepared = null

watch([() => props.modelValue, () => props.record], async ([open]) => {
  if (!open) return
  prepared = null
  Object.assign(form, defaults(), props.record ? { memberId: String(props.record.memberId), subscriptionId: String(props.record.subscriptionId) } : {})
  await nextTick()
  formRef.value?.clearValidate()
}, { immediate: true })

function handleMembers(members) {
  if (!Array.isArray(members) || members.length !== 1) { proxy.$modal.msgWarning(proxy.$t('MemberSubscription.OneMember')); return }
  form.memberId = String(members[0])
}
function applyDurationShortcut(seconds) {
  if (isSaving.value) return
  form.deltaSeconds = seconds
  formRef.value?.clearValidate('deltaSeconds')
}
async function handleSave() {
  if (!canAdjust.value || isSaving.value || !(await formRef.value?.validate().catch(() => false))) return
  const content = buildSubscriptionAdjustment(form, undefined)
  const signature = JSON.stringify(content)
  if (!prepared || prepared.signature !== signature) prepared = { signature, requestId: createSubscriptionRequestId() }
  isSaving.value = true
  try {
    await adjustMemberSubscription({ ...content, requestId: prepared.requestId })
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'))
    emit('update:modelValue', false)
    emit('success')
  } finally { isSaving.value = false }
}
function handleVisible(value) { if (!isSaving.value) emit('update:modelValue', value) }
function handleClosed() { Object.assign(form, defaults()); prepared = null; isMemberSelectorOpen.value = false; formRef.value?.clearValidate() }
</script>

<style scoped>
.member-subscription-adjust__duration { width: 100%; }
.member-subscription-adjust__shortcuts { margin-top: 8px; }
</style>
