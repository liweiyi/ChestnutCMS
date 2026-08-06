<template>
  <div class="msg-inner-event-selector-container">
    <el-dialog 
      :title="$t('Message.Event.InnerEventSelectorTitle')"
      v-model="visible"
      width="800px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form 
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        class="el-form-search">
        <el-form-item prop="query">
          <el-input v-model="queryParams.query" :placeholder="$t('Message.Event.InnerEventSelectorQueryPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button 
              type="primary"
              @click="handleQuery">
              <Search />
              {{ $t("Common.Search") }}
            </el-button>
            <el-button 
              @click="resetQuery">
              <Refresh />
              {{ $t("Common.Reset") }}
            </el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
      <el-table 
        v-loading="loading"
        :height="435"
        :data="eventList"
        highlight-current-row
        @cell-dblclick="handleDbClick"
        @current-change="handleSelectionChange">
        <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="50" />
        <el-table-column :label="$t('Message.Event.Key')" align="left" prop="value" />
        <el-table-column :label="$t('Message.Event.Name')" align="left" prop="label" />
      </el-table>
      <template #footer>
        <el-button type="primary" :disabled="okBtnDisabled" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="MessageInnerEventSelector">
const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  }
})

const emit = defineEmits(['update:open', 'ok', 'cancel'])

const { proxy } = getCurrentInstance()
const { MessageInnerEvent } = proxy.useDict("MessageInnerEvent")

const loading = ref(false)
const visible = ref(props.open)
const selected = ref(undefined)
const eventList = ref(MessageInnerEvent.value)
const objects = reactive({
  queryParams: {
    query: undefined
  }
})
const { queryParams } = toRefs(objects)

const okBtnDisabled = computed(() => {
  return selected.value == undefined
})

watch(() => props.open, (newVal) => {
  visible.value = newVal
})

watch(visible, (newVal) => {
  if (!newVal) {
    handleCancel()
  }
})

function handleSelectionChange(selection) {
  if (selection) {
    selected.value = selection
  } else {
    selected.value = undefined
  }
}

function handleDbClick(row) {
  emit("ok", row)
}

function handleOk() {
  emit("ok", selected.value)
}

function handleCancel() {
  emit("update:open", false)
  emit("cancel")
  queryParams.value.query = undefined
}

function handleQuery() {
  eventList.value = MessageInnerEvent.value.filter(item => item.value.toLowerCase().includes(queryParams.value.query.toLowerCase()) || item.label.toLowerCase().includes(queryParams.value.query.toLowerCase()))
}

function resetQuery() {
  proxy.resetForm("queryFormRef")
  queryParams.value.query = undefined
  handleQuery()
}
</script>