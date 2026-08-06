<template>
  <div class="notify-template-input-container">
    <el-input v-model="selectedName" disabled>
      <template #append>
        <el-button icon="Search" @click="handleSelectTemplate" />
      </template>
    </el-input>
    <message-template-selector v-model:open="open" v-model:type="templateType" @ok="handleSelectTemplateOk" />
  </div>
</template>
<script setup name="MessageTemplateInput">
import * as messageTemplateApi from "@/api/tool/message/template";
import MessageTemplateSelector from "./templateSelector.vue";

const model = defineModel()

const props = defineProps({
  type: {
    type: String,
    required: true
  }
})

const open = ref(false);
const templateType = ref('');
const selectedName = ref('');

watch(() => props.type, (newVal) => {
  templateType.value = newVal;
}, { immediate: true });

watch(() => model.value, (newVal) => {
  if (newVal) {
    initSelected();
  }
}, { immediate: true });

function initSelected() {
  messageTemplateApi.getTemplateDetail(model.value).then(response => {
    selectedName.value = response.data.name;
  });
}

function handleSelectTemplate() {
  open.value = true;
}

function handleSelectTemplateOk(selected) {
  model.value = selected.templateId;
  selectedName.value = selected.name;
  open.value = false;
}
</script>