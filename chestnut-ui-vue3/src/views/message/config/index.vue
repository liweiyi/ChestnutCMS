<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['message:config:add']"
          >{{ $t("Common.Add") }}</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          @click="handleDelete"
          v-hasPermi="['message:config:del']"
          >{{ $t("Common.Delete") }}</el-button
        >
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="loadConfigList"
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
        :label="$t('Message.Config.Type')"
        align="center"
        prop="type"
        width="180"
      />
      <el-table-column
        :label="$t('Message.Config.Name')"
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
            icon="MagicStick"
            @click="handleTest(scope.row)"
            v-hasPermi="['message:config:add','message:config:edit']"
            >{{ $t("Message.Config.TestSend") }}</el-button
          >
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['message:config:edit']"
            >{{ $t("Common.Edit") }}</el-button
          >
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['message:config:del']"
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
      @pagination="loadConfigList"
    />

    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-form-item :label="$t('Message.Config.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('Message.Config.Type')" prop="type">
          <el-select v-model="form.type" @change="handleTypeChange">
            <el-option
              v-for="item in typeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-for="item in configTypeProps" :label="item.label" :key="item.key" :prop="`configProps.${item.key}`">
          <el-input v-if="item.type==='INPUT'" v-model="form.configProps[item.key]" />
          <el-input-number v-if="item.type==='INPUT_NUMBER'" v-model="form.configProps[item.key]" />
          <pairs-input v-if="item.type==='INPUT_PAIRS'" v-model="form.configProps[item.key]" :keyName="item.pairsKeyName" :valueName="item.pairsValueName" />
          <el-date-picker v-if="item.type==='DATE'" v-model="form.configProps[item.key]" type="date" />
          <el-date-picker v-if="item.type==='DATE_TIME'" v-model="form.configProps[item.key]" type="datetime" />
          <el-time-picker v-if="item.type==='TIME'" v-model="form.configProps[item.key]" />
          <el-switch
            v-if="item.type==='SWITCH'"
            v-model="form.configProps[item.key]"
            active-value="Y"
            inactive-value="N"
          >
            <template #active-text>
              <span>{{ $t('Common.Yes') }}</span>
            </template>
            <template #inactive-text>
              <span>{{ $t('Common.No') }}</span>
            </template>
          </el-switch>
          <el-select v-if="item.type==='SELECT'" v-model="form.configProps[item.key]">
            <el-option
              v-for="option in item.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
          <el-radio-group v-if="item.type==='RADIO'" v-model="form.configProps[item.key]">
            <el-radio
              v-for="option in item.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-radio-group>
          <el-checkbox-group v-if="item.type==='CHECKBOX'" v-model="form.configProps[item.key]">
            <el-checkbox
              v-for="option in item.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item :label="$t('Message.Config.RateLimits')" prop="configProps.rateLimits">
          <div>
            <pairs-input
              v-model="form.configProps.rateLimits"
              :key-name="$t('Message.Config.RateLimitWindow')"
              :value-name="$t('Message.Config.RateLimitCount')"
            />
            <div class="el-form-item-tip"><el-text type="info" size="small">{{ $t('Message.Config.RateLimitTip') }}</el-text></div>
          </div>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :loading="submitLoading" type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <el-dialog :title="$t('Message.Config.TestSend')" v-model="openTest" width="800px" append-to-body>
      <el-form ref="formTestRef" :loading="submitLoading" :model="formTest" :rules="rulesTest" label-width="140px">
        <el-form-item v-if="formTest.type === 'SMS'" :label="$t('Message.Config.SMS.TestPhone')" prop="params.phoneNumber">
          <PhoneNumberInput v-model="formTest.params.phoneNumber" v-model:phone-calling-code="formTest.params.phoneCallingCode" />
        </el-form-item>
        <el-form-item v-if="formTest.type === 'SMS'" :label="$t('Message.Config.SMS.TestTemplateParams')" prop="params.templateVariables">
          <pairs-input v-model="formTest.params.templateVariables" />
        </el-form-item>
        <el-form-item v-if="formTest.type === 'Email'" :label="$t('Message.Config.Email.TestTo')" prop="params.receiver">
          <el-input v-model.trim="formTest.params.receiver" type="email" />
        </el-form-item>
        <el-form-item v-if="formTest.type === 'Email'" :label="$t('Message.Config.TestTitle')" prop="params.title">
          <el-input v-model="formTest.params.title" />
        </el-form-item>
        <el-form-item v-if="formTest.type === 'Email'" :label="$t('Message.Config.TestContent')" prop="params.content">
          <el-input v-model="formTest.params.content" type="textarea" :rows="10" />
        </el-form-item>
        <el-form-item v-if="formTest.type === 'DingTalkRobot' || formTest.type === 'FeiShuRobot'" :label="$t('Message.Config.TestContent')" prop="params.content">
          <el-input v-model="formTest.params.content" type="textarea" :rows="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :loading="submitLoading" type="primary" @click="handleTestSend">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancelTest">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MessageConfigIndex">
import * as messageConfigApi from "@/api/tool/message/config.js";
import PairsInput from '@/views/components/PairsInput';
import PhoneNumberInput from '@/components/PhoneNumberInput';
import { DEFAULT_PHONE_COUNTRY_CODE, normalizePhoneNumber, validatePhoneNumber } from '@/utils/phoneNumber';
import { isValidRateLimits } from './rateLimits.js';

const { proxy } = getCurrentInstance();

const typeRules = {
  default: {
    'name': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'type': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'configProps.rateLimits': [
      { validator: (_rule, value, callback) => callback(isValidRateLimits(value)
        ? undefined : new Error(proxy.$t('Message.Config.RateLimitInvalid'))), trigger: ['change', 'blur'] },
    ],
  },
  sms: {},
  sms_tencent: {
    'name': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'type': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'configProps.sdkAppId': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'configProps.templateId': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'configProps.signName': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'configProps.secretId': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'configProps.secretKey': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
  },
  email: {
    'name': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'type': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'configProps.host': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'configProps.port': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
  },
  dingtalkrobot: {
    'name': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'type': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'configProps.webHook': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ]
  },
  feishurobot: {
    'name': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'type': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'configProps.webHook': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ]
  },
}

const testRules = {
  sms: {
    'params.phoneNumber': [
      { required: true, whitespace: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
      { validator: (_rule, value, callback) => callback(validatePhoneNumber(value, formTest.value.params.phoneCallingCode)
        ? undefined : new Error(proxy.$t('Common.RuleTips.PhoneNumber'))), trigger: ['change', 'blur'] },
    ],
  },
  email: {
    'params.receiver': [
      { required: true, whitespace: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
      { type: 'email', message: proxy.$t('Common.RuleTips.Email'), trigger: ['change', 'blur'] },
    ],
    'params.title': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    'params.content': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
  },
  dingtalkrobot: {
    'params.content': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
  },
  feishurobot: {
    'params.content': [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
  },
}

const dataList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const selectedIds = ref([]);
const title = ref("");
const total = ref(0);
const typeOptions = ref([]);
const smsTypeOptions = ref([
  { label: '腾讯云', value: 'tencent' },
]);
const openTest = ref(false);
const data = reactive({
  form: {
    configProps: {}
  },
  queryParams: {
    page: 1,
    size: 10,
    type: undefined,
  },
  rules: typeRules.default,
  formTest: {},
  rulesTest: {}
});
const { queryParams, form, rules, formTest, rulesTest } = toRefs(data);
const submitLoading = ref(false);

const configTypeProps = computed(() => {
  return typeOptions.value.find(item => item.value === form.value.type)?.props || [];
});

onMounted(() => {
  loadTypeOptions();
  loadConfigList();
});

function loadTypeOptions() {
  messageConfigApi.getTypeOptions().then(response => {
    typeOptions.value = response.data;
  });
}

function loadConfigList() {
  loading.value = true;
  messageConfigApi.getConfigList(queryParams.value).then((response) => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function cancel() {
  open.value = false;
  proxy.resetForm("formRef");
}

function handleQuery() {
  queryParams.value.page = 1;
  loadConfigList();
}

function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map((item) => item.configId);
}

function handleAdd() {
  form.value = {
    configProps: {}
  };
  rules.value = { ...typeRules.default };
  title.value = proxy.$t("Message.Config.AddTitle");
  open.value = true;
}

function handleUpdate(row) {
  if (!row.configId && selectedIds.value.length == 0) {
    proxy.$modal.msgWarning(proxy.$t("Common.SelectFirst"));
    return;
  }
  messageConfigApi.getConfigDetail(row.configId).then(response => {
    form.value = response.data;
    handleTypeChange(form.value.type);
  });
  title.value = proxy.$t("Message.Config.EditTitle");
  open.value = true;
}

function submitForm() {
  proxy.$refs["formRef"].validate((valid) => {
    if (valid) {
      submitLoading.value = true;
      if (form.value.configId) {
        messageConfigApi.updateConfig(form.value).then((response) => {
          proxy.$modal.msgSuccess(proxy.$t("Common.SaveSuccess"));
          open.value = false;
          loadConfigList();
          submitLoading.value = false;
        }).catch(() => {
          submitLoading.value = false;
        });
      } else {
        messageConfigApi.createConfig(form.value).then((response) => {
          proxy.$modal.msgSuccess(proxy.$t("Common.SaveSuccess"));
          open.value = false;
          loadConfigList();
          submitLoading.value = false;
        }).catch(() => {
          submitLoading.value = false;
        });
      }
    }
  });
}

function handleDelete(row) {
  const configIds = row.configId ? [row.configId] : selectedIds.value;
  proxy.$modal.confirm(proxy.$t("Common.ConfirmDelete")).then(function () {
    return messageConfigApi.deleteConfigs(configIds);
  }).then(() => {
    proxy.$modal.msgSuccess(proxy.$t("Common.DeleteSuccess"));
    loadConfigList();
  }).catch(() => {});
}

function handleTypeChange(value) {
  rules.value = { ...typeRules.default, ...typeRules[value?.toLowerCase()] };
  form.value.configProps = form.value.configProps || {};
}

function handleSmsTypeChange(value) {
  rules.value = { ...typeRules.default, ...typeRules[`sms_${value.toLowerCase()}`] };
}

function cancelTest() {
  openTest.value = false;
  proxy.resetForm("formTestRef");
}

function handleTest(row) {
  formTest.value = {
    configId: row.configId,
    type: row.type,
    params: {}
  };
  rulesTest.value = testRules[row.type.toLowerCase()] || {};
  
  if (row.type === 'SMS') {
    formTest.value.params.phoneCallingCode = DEFAULT_PHONE_COUNTRY_CODE;
    formTest.value.params.phoneNumber = '';
  } else if (row.type === 'Email') {
    formTest.value.params.receiver = '';
    formTest.value.params.title = `邮件通知测试消息 - ${new Date().toLocaleString()}`;
    formTest.value.params.content = `测试消息，请忽略。<br/>时间：${new Date().toLocaleString()}`;
  } else if (row.type === 'DingTalkRobot') {
    formTest.value.params.content = `{
      "msgtype": "text",
      "text": {
        "content": "测试消息，请忽略。"
      },
      "at": {
        "isAtAll": true
      }
    }`;
  } else if (row.type === 'FeiShuRobot') {
    formTest.value.params.content = `{
      "msg_type": "text",
      "content": {
        "text": "测试消息，请忽略。"
      }
    }`;
  }
  openTest.value = true;
}

function handleTestSend() {
  submitLoading.value = true;
  proxy.$refs["formTestRef"].validate((valid) => {
    if (valid) {
      const { phoneNumber, phoneCallingCode, ...params } = formTest.value.params;
      const request = formTest.value.type === 'SMS' ? {
        ...formTest.value,
        params: {
          ...params,
          receiver: normalizePhoneNumber(phoneNumber, phoneCallingCode)
        }
      } : formTest.value;
      messageConfigApi.testConfig(request).then(response => {
        submitLoading.value = false;
        proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
      }).catch(() => {
        submitLoading.value = false;
      });
    } else {
      submitLoading.value = false;
    }
  });
}
</script>
