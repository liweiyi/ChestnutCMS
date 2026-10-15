<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['message:template:add']"
          >{{ $t("Common.Add") }}</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          @click="handleDelete"
          v-hasPermi="['message:template:del']"
          >{{ $t("Common.Delete") }}</el-button
        >
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="loadTemplateList"
      ></right-toolbar>
    </el-row>
    <el-form
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      v-show="showSearch"
      label-width="100px"
      class="el-form-search"
    >
      <el-form-item prop="type">
        <el-select v-model="queryParams.type" clearable style="width: 240px">
          <el-option
            v-for="item in typeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{
          $t("Common.Search")
        }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{
          $t("Common.Reset")
        }}</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="dataList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column
        :label="$t('Message.Template.Type')"
        align="center"
        prop="type"
        width="180"
      />
      <el-table-column
        :label="$t('Message.Template.Name')"
        align="left"
        prop="name"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        :label="$t('Common.Remark')"
        align="left"
        prop="remark"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        :label="$t('Common.CreateTime')"
        align="center"
        prop="createTime"
        width="180"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Common.Operation')"
        align="center"
        width="260"
      >
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['message:template:edit']"
            >{{ $t("Common.Edit") }}</el-button
          >
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['message:template:del']"
            >{{ $t("Common.Delete") }}</el-button
          >
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.page"
      v-model:limit="queryParams.size"
      @pagination="loadTemplateList"
    />

    <el-dialog :title="title" v-model="open" width="1000px" top="5vh"
               style="max-width: calc(100vw - 32px);" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" class="template-form">
        <el-form-item :label="$t('Message.Template.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('Message.Template.Type')" prop="type">
          <el-select v-model="form.type">
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.type === 'Email'" :label="$t('Message.Template.Title')" prop="title">
          <el-input ref="titleInputRef" v-model="form.title"
                    @focus="variableTarget = 'title'" @blur="rememberTextSelection('title')" />
        </el-form-item>
        <el-form-item v-if="form.type" :label="$t('Message.Template.Content')" prop="content">
          <div v-if="form.type === 'SMS'" style="width: 100%;">
            <el-input ref="smsInputRef" type="textarea" :rows="10" v-model="form.content"
                      @blur="rememberTextSelection('content')" />
          </div>
          <div v-else style="width: 100%;">
            <editor v-if="form.type === 'Email'" :key="editorSession" ref="emailEditorRef"
                    v-model="form.content" :min-height="192" @focus="variableTarget = 'content'"
                    upload-action="/message/template/image/upload"/>
            <div v-if="form.type === 'DingTalkRobot' || form.type === 'FeiShuRobot'">
              <code-mirror 
                class="template-editor"
                ref="cmEditorRef"
                v-model="form.content" 
                basic
                :tab="cmOptions.tab"
                :tab-size="cmOptions.tabSize"
                :readonly="cmOptions.readonly"
                :disabled="cmOptions.disabled"
                :lang="cmOptions.lang"
                :extensions="cmOptions.extensions"
                :line-wrapping="cmOptions.lineWrapping"
                :wrap="cmOptions.wrap"
                :dark="cmOptions.dark"
                @update="handleCMUpdate"
              />
              <div class="editor-actions" style="width: 100%;">
                <el-button size="small" type="primary" icon="MagicStick" @click="handleFormatJSON">格式化</el-button>
                <el-button size="small" type="success" icon="CircleCheck" @click="handleValidateJSON">校验</el-button>
                <el-text v-if="jsonValidationError" :type="jsonValidationError === 'SUCCESS' ? 'success' : 'danger'" style="margin-left: 10px;">{{ jsonValidationError }}</el-text>
              </div>
              <div class="demo-wrap">
                <div class="demo-buttons">
                  示例：
                  <el-button v-if="form.type === 'DingTalkRobot'" link type="primary" @click="handleDingTalkDemo('text')">文本</el-button>
                  <el-button v-if="form.type === 'DingTalkRobot'" link type="primary" @click="handleDingTalkDemo('link')">链接</el-button>
                  <el-button v-if="form.type === 'DingTalkRobot'" link type="primary" @click="handleDingTalkDemo('markdown')">Markdown</el-button>
                  <el-button v-if="form.type === 'FeiShuRobot'" link type="primary" @click="handleFeiShuDemo('text')">文本</el-button>
                  <el-button v-if="form.type === 'FeiShuRobot'" link type="primary" @click="handleFeiShuDemo('post')">富文本</el-button>
                  <el-button v-if="form.type === 'FeiShuRobot'" link type="primary" @click="handleFeiShuDemo('interactive')">卡片</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="form.type === 'Email' || form.type === 'SMS'"
                      :label="$t('Message.Template.Variables')">
          <div class="template-variables">
            <p>{{ $t('Message.Template.VariableUsage', { syntax: variableSyntax }) }}</p>
            <p>{{ $t('Message.Template.VariableScope') }}</p>
            <div v-if="form.type === 'Email'" class="variable-target">
              <span>{{ $t('Message.Template.InsertTarget') }}</span>
              <el-radio-group v-model="variableTarget" size="small">
                <el-radio-button value="title">{{ $t('Message.Template.Title') }}</el-radio-button>
                <el-radio-button value="content">{{ $t('Message.Template.Content') }}</el-radio-button>
              </el-radio-group>
            </div>
            <div class="variable-row">
              <el-button size="small" type="primary" plain @mousedown.prevent
                         @click="insertVariable(form.type === 'SMS' ? 'Code' : 'code')">
                {{ form.type === 'SMS' ? '${Code}' : '${code}' }}
              </el-button>
              <span>{{ $t(form.type === 'SMS' ? 'Message.Template.SmsCodeVariable' : 'Message.Template.EmailCodeVariable') }}</span>
            </div>
            <div class="custom-variable">
              <el-input v-model="customVariableName" size="small"
                        :aria-label="$t('Message.Template.CustomVariable')"
                        :placeholder="$t('Message.Template.VariableNamePlaceholder')"
                        @keydown.enter.prevent="insertCustomVariable" />
              <el-button size="small" @mousedown.prevent @click="insertCustomVariable">
                {{ $t('Message.Template.InsertVariable') }}
              </el-button>
            </div>
            <p>{{ $t('Message.Template.VariableInsertTip') }}</p>
            <p>{{ $t('Message.Template.VariableDefaultTip', { syntax: variableDefaultSyntax }) }}</p>
            <p v-if="form.type === 'SMS'">{{ $t('Message.Template.SmsProviderTip') }}</p>
          </div>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
          <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MessageTemplateIndex">
import CodeMirror from 'vue-codemirror6';
import { json, jsonParseLinter } from '@codemirror/lang-json';
import { linter } from '@codemirror/lint';
import * as messageConfigApi from "@/api/tool/message/config.js";
import * as messageTemplateApi from "@/api/tool/message/template";

const { proxy } = getCurrentInstance();
const cmEditorRef = ref(null);
const titleInputRef = ref(null);
const smsInputRef = ref(null);
const emailEditorRef = ref(null);
const editorSession = ref(0);
const variableTarget = ref('content');
const customVariableName = ref('');
const textSelections = { title: null, content: null };
const variableSyntax = '${variable}';
const variableDefaultSyntax = "${variable!''}";

const validateKeywords = (rule, value, callback) => {
  if (proxy.$tools.isEmpty(value)) {
    callback();
    return;
  }
  if (value.split('\n').length > 10) {
    callback(new Error(proxy.$t('Message.Template.RuleTips.MaxKeywords', [10])));
    return;
  }
  callback();
}

const validateIpWhiteList = (rule, value, callback) => {
  if (proxy.$tools.isEmpty(value)) {
    callback();
    return;
  }
  if (value.split('\n').length > 10) {
    callback(new Error(proxy.$t('Message.Template.RuleTips.MaxIpWhiteList', [10])));
    return;
  }
  callback();
}

const dataList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const selectedIds = ref([]);
const title = ref("");
const total = ref(0);
const typeOptions = ref([]);
const jsonValidationError = ref('');
const data = reactive({
  cmOptions: {
    dark: false,
    readonly: false, // 只读
    disabled: false, // 禁用
    tab: true, // 启用制表符缩进
    tabSize: 2,// tab的空格个数
    lineWrapping: true, // 是否自动换行
    lang: json(),
    extensions: [linter(jsonParseLinter())], // 启用JSON语法校验
  },
  form: {},
  queryParams: {
    page: 1,
    size: 10,
    type: undefined,
  },
  rules: {},
});

const { cmOptions, queryParams, form, rules } = toRefs(data);

watch(() => form.value.type, resetVariableInsertion);

function resetVariableInsertion() {
  variableTarget.value = 'content';
  customVariableName.value = '';
  textSelections.title = null;
  textSelections.content = null;
  editorSession.value++;
}

function getTextInput(target) {
  return target === 'title' ? titleInputRef.value?.input : smsInputRef.value?.textarea;
}

function rememberTextSelection(target) {
  const input = getTextInput(target);
  if (input) {
    textSelections[target] = { start: input.selectionStart, end: input.selectionEnd };
  }
}

async function insertVariable(name) {
  const token = '${' + name + '}';
  if (form.value.type === 'Email' && variableTarget.value === 'content') {
    emailEditorRef.value?.insertText(token);
    return;
  }
  const target = form.value.type === 'SMS' ? 'content' : 'title';
  const input = getTextInput(target);
  if (!input) return;
  if (document.activeElement === input) rememberTextSelection(target);
  const value = form.value[target] || '';
  const selection = textSelections[target];
  const start = Math.min(selection?.start ?? value.length, value.length);
  const end = Math.min(selection?.end ?? start, value.length);
  form.value[target] = value.slice(0, start) + token + value.slice(end);
  const cursor = start + token.length;
  textSelections[target] = { start: cursor, end: cursor };
  await nextTick();
  input.focus();
  input.setSelectionRange(cursor, cursor);
}

function insertCustomVariable() {
  const name = customVariableName.value.trim();
  if (!/^[A-Za-z_][A-Za-z0-9_]*$/.test(name)) {
    proxy.$modal.msgWarning(proxy.$t('Message.Template.VariableNameInvalid'));
    return;
  }
  insertVariable(name);
}

onMounted(() => {
  loadTypeOptions();
  loadTemplateList();
});

function loadTypeOptions() {
  messageConfigApi.getSupportTemplateTypeOptions().then(response => {
    typeOptions.value = response.data;
  });
}

function loadTemplateList() {
  loading.value = true;
  messageTemplateApi.getTemplateList(queryParams.value).then((response) => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function cancel() {
  open.value = false;
  jsonValidationError.value = '';
  proxy.resetForm("formRef");
}

function handleQuery() {
  queryParams.value.page = 1;
  loadTemplateList();
}

function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map((item) => item.templateId);
}

function handleAdd() {
  resetVariableInsertion();
  form.value = {};
  title.value = proxy.$t("Message.Template.AddTitle");
  open.value = true;
}

function handleUpdate(row) {
  if (!row.templateId && selectedIds.value.length == 0) {
    proxy.$modal.msgWarning(proxy.$t("Common.SelectFirst"));
    return;
  }
  messageTemplateApi.getTemplateDetail(row.templateId).then(response => {
    resetVariableInsertion();
    form.value = response.data;
  });
  title.value = proxy.$t("Message.Template.EditTitle");
  open.value = true;
}

function submitForm() {
  proxy.$refs["formRef"].validate((valid) => {
    if (valid) {
      if (form.value.templateId) {
        messageTemplateApi.updateTemplate(form.value).then((response) => {
          proxy.$modal.msgSuccess(proxy.$t("Common.SaveSuccess"));
          open.value = false;
          loadTemplateList();
        });
      } else {
        messageTemplateApi.createTemplate(form.value).then((response) => {
          proxy.$modal.msgSuccess(proxy.$t("Common.SaveSuccess"));
          open.value = false;
          loadTemplateList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const templateIds = row.templateId ? [row.templateId] : selectedIds.value;
  proxy.$modal.confirm(proxy.$t("Common.ConfirmDelete")).then(function () {
    return messageTemplateApi.deleteTemplates(templateIds);
  }).then(() => {
    proxy.$modal.msgSuccess(proxy.$t("Common.DeleteSuccess"));
    loadTemplateList();
  }).catch(() => {});
}

function handleCMUpdate(viewUpdate) {
  if (jsonValidationError.value && form.value.content) {
    jsonValidationError.value = '';
  }
}

// JSON格式化
function handleFormatJSON() {
  try {
    if (!form.value.content || form.value.content.trim() === '') {
      return;
    }
    const jsonObj = JSON.parse(form.value.content);
    form.value.content = JSON.stringify(jsonObj, null, 2);
    jsonValidationError.value = '';
  } catch (error) {
    jsonValidationError.value = error.message;
  }
}

// JSON校验
function handleValidateJSON() {
  try {
    if (!form.value.content || form.value.content.trim() === '') {
      return;
    }
    JSON.parse(form.value.content);
    jsonValidationError.value = 'SUCCESS';
  } catch (error) {
    jsonValidationError.value = error.message;
  }
}

function handleDingTalkDemo(type) {
  if (type === 'text') {
    form.value.content = `{
  "msgtype": "text",
  "text": {
    "content": "测试消息，请忽略。"
  },
  "at": {
    "isAtAll": true
  }
}`;
  } else if (type === 'link') {
    form.value.content = `{
  "msgtype": "link",
  "link": {
    "title": "测试消息",
    "text": "测试消息，请忽略。",
    "picUrl": "https://example.com/picture.jpg",
    "messageUrl": "https://example.com/message",
  }
}`;
  } else if (type === 'markdown') {
    form.value.content = `{
  "msgtype": "markdown",
  "markdown": {
    "title": "测试消息",
    "text": "# 一级标题 \\n## 二级标题 \\n测试消息，请忽略。"
  }
}`;
  }
}

function handleFeiShuDemo(type) {
  if (type === 'text') {
    form.value.content = `{
  "msg_type": "text",
  "content": {
    "text": "测试消息，请忽略。<at user_id="all">所有人</at>"
  }
}`;
  } else if (type === 'post') {
    form.value.content = `{
  "msg_type": "post",
  "content": {
    "post": {
      "zh_cn": {
        "title": "项目更新通知",
        "content": [
          [{
            "tag": "text",
            "text": "第一行：项目有更新: "
          }, {
            "tag": "a",
            "text": "请查看",
            "href": "http://www.example.com/"
          }, {
            "tag": "at",
            "user_id": "all"
          }],
          [{
            "tag": "text",
            "text": "第二行：更新内容: XXXXXXX"
          }]
        ]
      }
    }
  }
}`;
  } else if (type === 'interactive') {
    form.value.content = `{
  "msg_type": "interactive",
  "card": {
    "schema": "2.0",
    "config": {
      "update_multi": true,
      "style": {
        "text_size": {
          "normal_v2": {
            "default": "normal",
            "pc": "normal",
            "mobile": "heading"
          }
        }
      }
    },
    "body": {
      "direction": "vertical",
      "padding": "12px 12px 12px 12px",
      "elements": [
        {
          "tag": "markdown",
          "content": "西湖，位于中国浙江省杭州市西湖区龙井路1号，杭州市区西部，汇水面积为21.22平方千米，湖面面积为6.38平方千米。",
          "text_align": "left",
          "text_size": "normal_v2",
          "margin": "0px 0px 0px 0px"
        },
        {
          "tag": "button",
          "text": {
            "tag": "plain_text",
            "content": "🌞更多景点介绍"
          },
          "type": "default",
          "width": "default",
          "size": "medium",
          "behaviors": [
            {
              "type": "open_url",
              "default_url": "https://baike.baidu.com/item/%E8%A5%BF%E6%B9%96/4668821",
              "pc_url": "",
              "ios_url": "",
              "android_url": ""
            }
          ],
          "margin": "0px 0px 0px 0px"
        }
      ]
    },
    "header": {
      "title": {
        "tag": "plain_text",
        "content": "今日旅游推荐"
      },
      "subtitle": {
        "tag": "plain_text",
        "content": ""
      },
      "template": "blue",
      "padding": "12px 12px 12px 12px"
    }
  }
}`;
  }
}
</script>
<style scoped lang="scss">
.template-form {
  max-height: calc(90vh - 140px);
  overflow-y: auto;
  padding-right: 8px;
}

.template-variables {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-regular);
  font-size: 13px;
  line-height: 1.6;

  p {
    margin: 0 0 8px;
  }

  p:last-child {
    margin-bottom: 0;
  }
}

.variable-target, .variable-row, .custom-variable {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.custom-variable .el-input {
  width: 240px;
  max-width: 100%;
}

.template-editor {
  height: 50vh;
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 4px;

  :deep(.cm-editor) {
    height: 100% !important;
  }

  :deep(.editor-actions) {
    width: 100%;
    background-color: #f5f7fa;
    align-items: center;
    gap: 10px;
    margin-top: 8px;
  }

  :deep(.demo-wrap) {
    width: 100%;

    .demo-buttons {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-top: 8px;
    }
  }
}
</style>
