<template>
  <div class="i18n-field-editor">
    <el-link :underline="false" @click="handleOpen">
      <svg-icon icon-class="language" />
    </el-link>
    <el-dialog 
      :title="title" 
      v-model="open" 
      width="500px" 
      :close-on-click-modal="false" 
      append-to-body
    >
      <el-form ref="i18nForm" label-width="80px">
        <el-form-item 
          :label="lang.langTag" 
          v-for="lang in dataList" 
          :key="lang.langTag"
        >
          <el-input v-model="lang.langValue" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleOk">{{ $t('Common.Save') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="I18nFieldEditor">
import { listI18nDictByKey, batchSaveI18nDicts } from "@/api/system/i18nDict";

const props = defineProps({
  languageKey: {
    type: String,
    required: true,
    default: ""
  }
})

const { proxy } = getCurrentInstance()

const loading = ref(false)
const open = ref(false)
const dataList = ref([])
const title = computed(() => proxy.$t('System.I18n.EditorTitle', [ props.languageKey ]))

const langKey = ref(props.languageKey)
watch(() => props.languageKey, (newVal) => {
  langKey.value = newVal
})

function handleOpen () {
  open.value = true;
  loading.value = true;
  listI18nDictByKey(langKey.value).then(response => {
    dataList.value = response.data;
    loading.value = false;
  });
}
function handleOk () {
  batchSaveI18nDicts(dataList.value).then(response => {
    open.value = false;
    proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
  });
}
function cancel () {
  open.value = false;
}
</script>