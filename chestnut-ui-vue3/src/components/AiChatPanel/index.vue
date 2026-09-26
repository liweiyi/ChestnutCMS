<template>
  <el-dialog
    ref="dialogRef"
    v-model="visible"
    class="ai-chat-dialog"
    :width="dialogWidth"
    :close-on-click-modal="false"
    append-to-body
    draggable
    @closed="resetDialogPosition"
    @opened="scrollToBottom"
  >
    <template #header>
      <div class="ai-chat-title-wrap">
        <span class="ai-chat-title-icon"><svg-icon icon-class="ai-chat" /></span>
        <div>
          <div class="ai-chat-title">{{ $t('AI.Chat.Title') }}</div>
          <div class="ai-chat-subtitle">{{ $t('AI.Chat.StatelessTip') }}</div>
        </div>
      </div>
    </template>

    <div v-loading="modelsLoading" class="ai-chat-panel">
      <div class="ai-chat-model-toolbar">
        <el-select
          v-model="selectedModelId"
          :placeholder="$t('AI.Chat.SelectModel')"
          :loading="modelsLoading"
          :disabled="sending"
          filterable
        >
          <el-option
            v-for="model in modelOptions"
            :key="model.modelId"
            :value="model.modelId"
            :label="modelOptionLabel(model)"
          >
            <div class="ai-chat-model-option">
              <span>{{ model.name }}</span>
              <el-tag v-if="model.defaultModel" size="small" type="success">
                {{ $t('AI.Model.IsDefault') }}
              </el-tag>
              <span class="ai-chat-model-name">{{ model.modelName }}</span>
            </div>
          </el-option>
        </el-select>
        <el-button icon="Refresh" :disabled="sending" @click="startNewConversation">
          {{ $t('AI.Chat.NewConversation') }}
        </el-button>
      </div>

      <div ref="messageContainerRef" class="ai-chat-message-container">
        <div v-if="modelOptions.length === 0 && !modelsLoading" class="ai-chat-empty-state">
          <div class="ai-chat-empty-icon"><svg-icon icon-class="ai-chat" /></div>
          <h3>{{ $t('AI.Chat.NoModels') }}</h3>
          <p>{{ $t('AI.Chat.NoModelsTip') }}</p>
        </div>
        <div v-else-if="messages.length === 0" class="ai-chat-empty-state">
          <div class="ai-chat-empty-icon"><svg-icon icon-class="ai-chat" /></div>
          <h3>{{ $t('AI.Chat.EmptyTitle') }}</h3>
          <p>{{ $t('AI.Chat.EmptyDescription') }}</p>
        </div>

        <div
          v-for="message in messages"
          :key="message.id"
          class="ai-chat-message-row"
          :class="message.role === 'USER' ? 'ai-chat-message-user' : 'ai-chat-message-assistant'"
        >
          <div class="ai-chat-message-avatar">
            <el-icon v-if="message.role === 'USER'"><User /></el-icon>
            <svg-icon v-else icon-class="ai-chat" />
          </div>
          <div class="ai-chat-message-body">
            <div class="ai-chat-message-meta">
              <span>{{ message.role === 'USER' ? $t('AI.Chat.User') : $t('AI.Chat.Assistant') }}</span>
              <el-button
                v-if="message.role === 'ASSISTANT' && message.content && !message.pending"
                link
                type="primary"
                size="small"
                @click="copyMessage(message.content)"
              >{{ $t('AI.Chat.Copy') }}</el-button>
            </div>
            <div class="ai-chat-message-bubble" :class="{ 'ai-chat-message-error': message.error }">
              <span v-if="message.content">{{ message.content }}</span>
              <span v-else-if="message.pending" class="ai-chat-typing-indicator">
                <i></i><i></i><i></i>
              </span>
              <span v-if="message.error" class="ai-chat-error-text">{{ message.error }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="ai-chat-composer">
        <el-input
          v-model="inputMessage"
          type="textarea"
          resize="none"
          :autosize="{ minRows: 3, maxRows: 8 }"
          maxlength="20000"
          show-word-limit
          :disabled="modelOptions.length === 0"
          :placeholder="$t('AI.Chat.InputPlaceholder')"
          @keydown="handleComposerKeydown"
        />
        <div class="ai-chat-composer-footer">
          <span class="ai-chat-composer-tip">{{ $t('AI.Chat.SendTip') }}</span>
          <el-button v-if="sending" type="danger" plain icon="VideoPause" @click="stopGeneration">
            {{ $t('AI.Chat.Stop') }}
          </el-button>
          <el-button
            v-else
            type="primary"
            icon="Promotion"
            :disabled="!canSend"
            @click="sendMessage"
          >{{ $t('AI.Chat.Send') }}</el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup name="AiChatPanel">
import { useWindowSize } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import { getAiChatModels, streamAiChat } from '@/api/ai/chat'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:open'])
const { proxy } = getCurrentInstance()
const { width } = useWindowSize()

const modelOptions = ref([])
const selectedModelId = ref(undefined)
const modelsLoading = ref(false)
const messages = ref([])
const inputMessage = ref('')
const sending = ref(false)
const dialogRef = ref()
const messageContainerRef = ref()
const conversationId = ref(createConversationId())
let currentController
let messageSequence = 0
let modelLoadSequence = 0

const visible = computed({
  get: () => props.open,
  set: value => emit('update:open', value)
})
const dialogWidth = computed(() => width.value < 768 ? 'calc(100% - 24px)' : '30%')
const canSend = computed(() => selectedModelId.value
  && inputMessage.value.trim().length > 0
  && !sending.value)

watch(() => props.open, open => {
  if (open) {
    loadModels()
    scrollToBottom()
  } else {
    currentController?.abort()
  }
}, { immediate: true })

onBeforeUnmount(() => {
  currentController?.abort()
})

function loadModels() {
  const loadSequence = ++modelLoadSequence
  modelsLoading.value = true
  getAiChatModels()
    .then(response => {
      if (loadSequence !== modelLoadSequence) return
      modelOptions.value = response.data || []
      const selectedModel = modelOptions.value.find(item => item.modelId === selectedModelId.value)
      const defaultModel = modelOptions.value.find(item => item.defaultModel)
      selectedModelId.value = selectedModel?.modelId || defaultModel?.modelId || modelOptions.value[0]?.modelId
    })
    .catch(() => {
      if (loadSequence !== modelLoadSequence) return
      modelOptions.value = []
      selectedModelId.value = undefined
    })
    .finally(() => {
      if (loadSequence === modelLoadSequence) modelsLoading.value = false
    })
}

function modelOptionLabel(model) {
  return `${model.name} (${model.modelName})`
}

function startNewConversation() {
  currentController?.abort()
  currentController = undefined
  sending.value = false
  messages.value = []
  inputMessage.value = ''
  conversationId.value = createConversationId()
}

async function sendMessage() {
  const content = inputMessage.value.trim()
  if (!selectedModelId.value || !content || sending.value) return

  messages.value.push(createMessage('USER', content))
  inputMessage.value = ''

  const requestMessages = messages.value
    .filter(message => !message.pending && !message.error)
    .slice(-20)
    .map(message => ({ role: message.role, content: message.content }))
  const assistantMessage = createMessage('ASSISTANT', '', true)
  const controller = new AbortController()
  messages.value.push(assistantMessage)
  sending.value = true
  currentController = controller
  await scrollToBottom()

  try {
    await streamAiChat({
      conversationId: conversationId.value,
      modelId: selectedModelId.value,
      messages: requestMessages
    }, {
      signal: controller.signal,
      onEvent: (eventName, event) => handleStreamEvent(eventName, event, assistantMessage)
    })
    assistantMessage.pending = false
  } catch (error) {
    assistantMessage.pending = false
    if (error?.name === 'AbortError' && !assistantMessage.content) {
      messages.value = messages.value.filter(message => message.id !== assistantMessage.id)
    } else if (error?.name !== 'AbortError') {
      assistantMessage.error = error?.message || proxy.$t('AI.Chat.RequestFailed')
    }
  } finally {
    if (currentController === controller) {
      sending.value = false
      currentController = undefined
    }
    await scrollToBottom()
  }
}

function handleStreamEvent(eventName, event, assistantMessage) {
  const type = event?.type || eventName
  if (type === 'delta') {
    assistantMessage.content += event.content || ''
  } else if (type === 'complete') {
    assistantMessage.pending = false
  } else if (type === 'error') {
    assistantMessage.pending = false
    assistantMessage.error = event.message || proxy.$t('AI.Chat.RequestFailed')
  }
  scrollToBottom()
}

function stopGeneration() {
  currentController?.abort()
}

function handleComposerKeydown(event) {
  if (event.key !== 'Enter' || event.shiftKey || event.isComposing) return
  event.preventDefault()
  sendMessage()
}

function createMessage(role, content, pending = false) {
  return reactive({
    id: `${Date.now()}-${++messageSequence}`,
    role,
    content,
    pending,
    error: ''
  })
}

async function copyMessage(content) {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success(proxy.$t('AI.Chat.Copied'))
  } catch {
    ElMessage.error(proxy.$t('AI.Chat.CopyFailed'))
  }
}

async function scrollToBottom() {
  await nextTick()
  const container = messageContainerRef.value
  if (container) container.scrollTop = container.scrollHeight
}

function createConversationId() {
  if (globalThis.crypto?.randomUUID) return globalThis.crypto.randomUUID()
  return `${Date.now()}-${Math.random().toString(36).slice(2)}`
}

function resetDialogPosition() {
  dialogRef.value?.resetPosition()
}
</script>

<style lang="scss">
.ai-chat-dialog {
  margin-right: 0;
  margin-left: auto;

  .el-dialog__header {
    padding: 18px 20px;
    margin-bottom: 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .el-dialog__body {
    display: flex;
    height: min(680px, calc(88vh - 78px));
    min-height: 0;
    padding: 0;
    overflow: hidden;
  }
}

.ai-chat-title-wrap,
.ai-chat-model-toolbar,
.ai-chat-model-option,
.ai-chat-composer-footer {
  display: flex;
  align-items: center;
}

.ai-chat-title-wrap {
  min-width: 0;
  gap: 12px;
}

.ai-chat-title-icon,
.ai-chat-empty-icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  border-radius: 50%;
}

.ai-chat-title-icon {
  width: 40px;
  height: 40px;
  font-size: 21px;
}

.ai-chat-title {
  color: var(--el-text-color-primary);
  font-size: 17px;
  font-weight: 600;
}

.ai-chat-subtitle {
  margin-top: 4px;
  overflow: hidden;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ai-chat-panel {
  display: flex;
  width: 100%;
  min-height: 0;
  flex: 1;
  flex-direction: column;
}

.ai-chat-model-toolbar {
  flex: 0 0 auto;
  padding: 14px 16px;
  gap: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  .el-select {
    min-width: 0;
    flex: 1;
  }
}

.ai-chat-model-option {
  width: 100%;
  gap: 8px;
}

.ai-chat-model-name {
  margin-left: auto;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.ai-chat-message-container {
  min-height: 0;
  flex: 1;
  padding: 20px 16px;
  overflow-y: auto;
  background: var(--el-fill-color-lighter);
}

.ai-chat-empty-state {
  display: flex;
  height: 100%;
  min-height: 240px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: var(--el-text-color-secondary);
  text-align: center;

  h3 {
    margin: 16px 0 8px;
    color: var(--el-text-color-primary);
    font-size: 18px;
  }

  p {
    max-width: 430px;
    margin: 0;
    line-height: 1.7;
  }
}

.ai-chat-empty-icon {
  width: 68px;
  height: 68px;
  font-size: 34px;
}

.ai-chat-message-row {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
}

.ai-chat-message-user {
  flex-direction: row-reverse;

  .ai-chat-message-meta {
    justify-content: flex-end;
  }

  .ai-chat-message-bubble {
    color: var(--el-color-white);
    background: var(--el-color-primary);
    border-top-right-radius: 4px;
  }
}

.ai-chat-message-assistant .ai-chat-message-bubble {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-top-left-radius: 4px;
}

.ai-chat-message-avatar {
  display: flex;
  width: 34px;
  height: 34px;
  flex: 0 0 34px;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 50%;
}

.ai-chat-message-user .ai-chat-message-avatar {
  color: var(--el-color-white);
  background: var(--el-color-primary);
}

.ai-chat-message-body {
  max-width: calc(100% - 44px);
}

.ai-chat-message-meta {
  display: flex;
  min-height: 24px;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.ai-chat-message-bubble {
  min-width: 52px;
  padding: 10px 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  border-radius: 12px;
}

.ai-chat-message-error {
  border-color: var(--el-color-danger-light-5) !important;
}

.ai-chat-error-text {
  display: block;
  margin-top: 8px;
  color: var(--el-color-danger);
  font-size: 12px;
}

.ai-chat-typing-indicator {
  display: inline-flex;
  padding: 6px 2px;
  gap: 5px;

  i {
    width: 6px;
    height: 6px;
    background: var(--el-text-color-secondary);
    border-radius: 50%;
    animation: ai-chat-typing 1.2s infinite ease-in-out;
  }

  i:nth-child(2) { animation-delay: 0.15s; }
  i:nth-child(3) { animation-delay: 0.3s; }
}

.ai-chat-composer {
  flex: 0 0 auto;
  padding: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
}

.ai-chat-composer-footer {
  justify-content: space-between;
  margin-top: 10px;
  gap: 12px;
}

.ai-chat-composer-tip {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}

@keyframes ai-chat-typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-4px); opacity: 1; }
}

@media (max-width: 767px) {
  .ai-chat-dialog {
    margin-top: 12px !important;

    .el-dialog__body {
      height: calc(100vh - 104px);
    }
  }

  .ai-chat-subtitle {
    max-width: calc(100vw - 130px);
  }

  .ai-chat-model-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .ai-chat-composer-tip {
    display: none;
  }

  .ai-chat-composer-footer {
    justify-content: flex-end;
  }
}
</style>
