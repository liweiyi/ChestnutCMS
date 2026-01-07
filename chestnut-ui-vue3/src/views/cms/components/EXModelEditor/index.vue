<template>
  <div>
    <el-form-item v-for="field in fieldList" :key="field.fieldName" :label="field.label" :prop="field.fieldName">
      <el-input v-if="field.controlType === 'input'" v-model="field.value" />
      <el-input v-if="field.controlType === 'textarea'" v-model="field.value" type="textarea" />
      <el-radio-group v-if="field.controlType === 'radio'" v-model="field.value">
        <el-radio v-for="item in field.options" :key="item.value" :label="item.value">{{ item.name }}</el-radio>
      </el-radio-group>
      <el-checkbox-group v-if="field.controlType === 'checkbox'" v-model="field.value">
        <el-checkbox v-for="item in field.options" :key="item.value" :label="item.value">{{ item.name }}
        </el-checkbox>
      </el-checkbox-group>
      <el-select v-if="field.controlType === 'select'" v-model="field.value" clearable>
        <el-option v-for="item in field.options" :key="item.value" :label="item.name" :value="item.value">
        </el-option>
      </el-select>
      <el-date-picker v-if="field.controlType === 'date'" v-model="field.value" type="date" value-format="YYYY-MM-DD">
      </el-date-picker>
      <el-time-picker v-if="field.controlType === 'time'" v-model="field.value" value-format="HH:mm:ss">
      </el-time-picker>
      <el-date-picker v-if="field.controlType === 'datetime'" v-model="field.value" type="datetime"
        value-format="YYYY-MM-DD HH:mm:ss">
      </el-date-picker>
      <cms-resource-uploader v-if="field.controlType === 'CMSImage'" v-model="field.value"
        type="image"></cms-resource-uploader>
      <ueditor v-if="field.controlType === 'UEditor'" :editorId="'ex-' + field.fieldName" :height="200" :configs="ueConfigs"
        v-model="field.value">
      </ueditor>
      <cms-content-selector v-if="field.controlType === 'CMSContentSelect'" v-model="field.value"
        :selected="field.valueObj">
      </cms-content-selector>
      <cms-resource-uploader v-if="field.controlType === 'CMSResource'" v-model="field.value"></cms-resource-uploader>
    </el-form-item>
  </div>
</template>
<script setup name="CmsExModelEditor">
import CmsContentSelector from '@/views/cms/components/ContentSelector';
import CmsResourceUploader from '@/views/cms/components/ResourceUploder';
import Ueditor from '@/views/cms/components/UEditorPlus'
import { getXModelFieldData } from "@/api/contentcore/exmodel";

const props = defineProps({
  xmodel: {
    type: String,
    required: true,
  },
  type: {
    type: String,
    required: true
  },
  id: {
    type: String,
    required: false
  }
});

const modelId = ref(props.xmodel);
const dataId = ref(props.id);
const dataType = ref(props.type);
const fieldList = ref([]);
const ueConfigs = ref({
  elementPathEnabled: false,
  wordCount: false,
  toolbars: [
    [
      "bold",         // 加粗
      "italic",       // 斜体
      "underline",    // 下划线
      "strikethrough",// 删除线
      "blockquote",   // 引用
      "|",
      "forecolor",    // 字体颜色
      "insertorderedlist",   // 有序列表
      "insertunorderedlist", // 无序列表
      "|",
      "indent",              // 首行缩进
      "lineheight",          // 行间距
      "paragraph",           // 段落格式
      "fontsize",            // 字号
      "|",
      "link",                // 超链接
      "xy-third-video",         // 视频
      'xy-resource',
      "xy-check-word",
      "horizontal",          // 分隔线
      "inserttable",         // 插入表格
    ]
  ],
});

watch(() => props.type, (newVal) => {
  dataType.value = newVal;
});
watch(() => props.id, (newVal) => {
  dataId.value = newVal;
});
watch(dataId, (newVal) => {
  if (newVal && newVal != null && newVal.length > 0) {
    loadModelFieldData();
  }
});

onMounted(() => {
  loadModelFieldData();
});

function loadModelFieldData() {
  fieldList.value = []
  getXModelFieldData(modelId.value, dataType.value, dataId.value).then(response => {
    response.data.forEach(item => {
      let field = {};
      Object.keys(item).forEach(key => {
        if (key != 'value' && key != 'valueObj') {
          field[key] = item[key];
        } else {
          if (item.value instanceof Array) {
            field[key] = []
          } else {
            field[key] = ""
          }
        }
      })
      fieldList.value.push(field);
    })
    nextTick(() => {
      response.data.forEach(item => {
        let field = fieldList.value.find(f => item.fieldName == f.fieldName);
        if (field) {
          field.value = item.value
          field.valueObj = item.valueObj
        }
      })
    })
  });
}

function getDatas() {
  let fdatas = {};
  fieldList.value.forEach(f => {
    fdatas[f.fieldName] = f.value;
  })
  return fdatas;
}

defineExpose({
  getDatas
})
</script>
<style scoped>
.el-form-item {
  margin-bottom: 12px;
}
</style>