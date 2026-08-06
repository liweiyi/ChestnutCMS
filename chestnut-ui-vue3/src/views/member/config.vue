<template>
  <div class="app-container">
    <el-card v-loading="loading" shadow="never">
      <template #header>
        <span>{{ $t('Member.Config.Title') }}</span>
      </template>

      <el-alert
        class="mb20"
        :title="$t('Member.Config.Tip')"
        type="info"
        :closable="false"
        show-icon
      />

      <el-form :model="configs" label-width="190px" style="max-width: 1100px">
        <el-form-item
          v-for="definition in definitions"
          :key="definition.id"
          :label="definition.name"
        >
          <div v-if="definition.controlType === 'MESSAGE'" class="message-cascade">
            <div class="message-selector">
              <el-select
                v-model="configs[definition.id][0]"
                :loading="configLoading[definition.messageType]"
                :placeholder="$t('Member.Config.ConfigPlaceholder')"
                clearable
                filterable
                style="width: 100%"
                @change="value => handleMessageConfigChange(definition, value)"
              >
                <el-option
                  v-for="option in getConfigOptions(definition)"
                  :key="option.configId"
                  :label="option.name"
                  :value="option.configId"
                  :disabled="option.missing"
                />
              </el-select>
            </div>
            <div class="message-selector">
              <el-select
                v-model="configs[definition.id][1]"
                :disabled="!selectedMessageConfig(definition)"
                :loading="isTemplateLoading(definition)"
                :placeholder="selectedMessageConfig(definition)
                  ? $t('Member.Config.TemplatePlaceholder')
                  : $t('Member.Config.SelectConfigFirst')"
                clearable
                filterable
                style="width: 100%"
                @change="value => handleMessageTemplateChange(definition, value)"
              >
                <el-option
                  v-for="option in getTemplateOptions(definition)"
                  :key="option.templateId"
                  :label="option.name"
                  :value="option.templateId"
                  :disabled="option.missing"
                />
              </el-select>
            </div>
          </div>
          <el-switch
            v-else-if="definition.controlType === 'SWITCH'"
            v-model="configs[definition.id]"
          />
          <el-input-number
            v-else-if="definition.controlType === 'INPUT_NUMBER'"
            v-model="configs[definition.id]"
            style="width: 100%"
          />
          <el-input v-else v-model="configs[definition.id]" />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            icon="Check"
            :loading="saving"
            v-hasPermi="['member:config:edit']"
            @click="saveConfig"
          >
            {{ $t('Common.Save') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup name="MemberConfig">
import * as memberConfigApi from '@/api/member/config'
import * as messageConfigApi from '@/api/tool/message/config'
import * as messageTemplateApi from '@/api/tool/message/template'

const { proxy } = getCurrentInstance()

const MESSAGE_CONTROL_TYPE = 'MESSAGE'
const MESSAGE_CONFIG_INDEX = 0
const MESSAGE_TEMPLATE_INDEX = 1

const loading = ref(false)
const saving = ref(false)
const definitions = ref([])
const configs = reactive({})
const configOptionsByType = reactive({})
const templateOptionsByType = reactive({})
const configLoading = reactive({})
const templateLoading = reactive({})

async function loadConfig() {
  loading.value = true
  try {
    const [definitionResponse, configResponse] = await Promise.all([
      memberConfigApi.getMemberConfigDefinitions(),
      memberConfigApi.getMemberConfig()
    ])
    definitions.value = definitionResponse.data || []
    Object.keys(configs).forEach(key => delete configs[key])
    Object.assign(configs, configResponse.data?.configs || {})
    definitions.value.forEach(definition => {
      if (configs[definition.id] === undefined || configs[definition.id] === null) {
        configs[definition.id] = definition.defaultValue
      }
      if (definition.controlType === MESSAGE_CONTROL_TYPE) {
        configs[definition.id] = normalizeMessageValue(configs[definition.id])
      }
    })
    await loadMessageOptions()
  } finally {
    loading.value = false
  }
}

async function loadMessageOptions() {
  clearOptionState()
  const messageDefinitions = definitions.value
    .filter(definition => definition.controlType === MESSAGE_CONTROL_TYPE)
  const configTypes = [...new Set(messageDefinitions
    .map(definition => definition.messageType)
    .filter(Boolean))]
  await Promise.all(configTypes.map(loadConfigOptions))

  const selectedTypes = [...new Set(messageDefinitions
    .map(selectedMessageConfig)
    .filter(Boolean)
    .map(config => config.type))]
  await Promise.all(selectedTypes.map(loadTemplateOptions))
}

function clearOptionState() {
  for (const state of [configOptionsByType, templateOptionsByType, configLoading, templateLoading]) {
    Object.keys(state).forEach(key => delete state[key])
  }
}

async function loadConfigOptions(type) {
  if (!type || configOptionsByType[type]) {
    return
  }
  configLoading[type] = true
  try {
    const response = await messageConfigApi.getConfigList({ page: 1, size: 1000, type })
    configOptionsByType[type] = response.data?.rows || []
  } finally {
    configLoading[type] = false
  }
}

async function loadTemplateOptions(type) {
  if (!type || templateOptionsByType[type]) {
    return
  }
  templateLoading[type] = true
  try {
    const response = await messageTemplateApi.getTemplateList({ page: 1, size: 1000, type })
    templateOptionsByType[type] = response.data?.rows || []
  } finally {
    templateLoading[type] = false
  }
}

function getConfigOptions(definition) {
  const options = configOptionsByType[definition.messageType] || []
  const selectedId = configs[definition.id]?.[MESSAGE_CONFIG_INDEX]
  if (hasId(selectedId) && !options.some(option => sameId(option.configId, selectedId))) {
    return options.concat({
      configId: selectedId,
      name: proxy.$t('Member.Config.MissingConfig'),
      missing: true
    })
  }
  return options
}

function selectedMessageConfig(definition) {
  const selectedId = configs[definition.id]?.[MESSAGE_CONFIG_INDEX]
  if (!hasId(selectedId)) {
    return undefined
  }
  return (configOptionsByType[definition.messageType] || [])
    .find(option => sameId(option.configId, selectedId))
}

function getTemplateOptions(definition) {
  const selectedConfig = selectedMessageConfig(definition)
  const options = selectedConfig ? (templateOptionsByType[selectedConfig.type] || []) : []
  const selectedId = configs[definition.id]?.[MESSAGE_TEMPLATE_INDEX]
  if (hasId(selectedId) && !options.some(option => sameId(option.templateId, selectedId))) {
    return options.concat({
      templateId: selectedId,
      name: proxy.$t('Member.Config.MissingTemplate'),
      missing: true
    })
  }
  return options
}

function isTemplateLoading(definition) {
  const selectedConfig = selectedMessageConfig(definition)
  return selectedConfig ? templateLoading[selectedConfig.type] : false
}

async function handleMessageConfigChange(definition, configId) {
  configs[definition.id][MESSAGE_CONFIG_INDEX] = configId || undefined
  configs[definition.id][MESSAGE_TEMPLATE_INDEX] = undefined
  const selectedConfig = selectedMessageConfig(definition)
  if (selectedConfig) {
    await loadTemplateOptions(selectedConfig.type)
  }
}

function handleMessageTemplateChange(definition, templateId) {
  configs[definition.id][MESSAGE_TEMPLATE_INDEX] = templateId || undefined
}

function normalizeMessageValue(value) {
  const values = Array.isArray(value) ? value : []
  return [
    hasId(values[MESSAGE_CONFIG_INDEX]) ? values[MESSAGE_CONFIG_INDEX] : undefined,
    hasId(values[MESSAGE_TEMPLATE_INDEX]) ? values[MESSAGE_TEMPLATE_INDEX] : undefined
  ]
}

function buildConfigPayload() {
  const values = { ...configs }
  definitions.value
    .filter(definition => definition.controlType === MESSAGE_CONTROL_TYPE)
    .forEach(definition => {
      const messageValue = configs[definition.id] || []
      values[definition.id] = [
        hasId(messageValue[MESSAGE_CONFIG_INDEX]) ? messageValue[MESSAGE_CONFIG_INDEX] : 0,
        hasId(messageValue[MESSAGE_TEMPLATE_INDEX]) ? messageValue[MESSAGE_TEMPLATE_INDEX] : 0
      ]
    })
  return values
}

function hasId(value) {
  return value !== undefined && value !== null && String(value) !== '' && String(value) !== '0'
}

function sameId(left, right) {
  return String(left) === String(right)
}

function saveConfig() {
  saving.value = true
  memberConfigApi.updateMemberConfig({ configs: buildConfigPayload() }).then(() => {
    proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'))
    loadConfig()
  }).finally(() => {
    saving.value = false
  })
}

loadConfig()
</script>

<style scoped>
.message-cascade {
  display: flex;
  width: 100%;
  gap: 16px;
}

.message-selector {
  flex: 1;
  min-width: 240px;
}

@media (max-width: 900px) {
  .message-cascade {
    flex-direction: column;
    gap: 12px;
  }

  .message-selector {
    min-width: 0;
  }
}
</style>
