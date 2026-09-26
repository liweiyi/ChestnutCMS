<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['message:event:save']"
          >{{ $t("Common.Add") }}</el-button
        >
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          @click="handleDelete"
          v-hasPermi="['message:event:del']"
          >{{ $t("Common.Delete") }}</el-button
        >
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="loadEventList"
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
            v-for="item in configOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ $t("Common.Search") }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="dataList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column
        :label="$t('Message.Event.Key')"
        align="left"
        prop="eventId"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        :label="$t('Message.Event.Name')"
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
            icon="Promotion"
            @click="handleTest(scope.row)"
            v-hasPermi="['message:event:save']"
            >测试</el-button
          >
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['message:event:save']"
            >{{ $t("Common.Edit") }}</el-button
          >
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['message:event:del']"
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
      @pagination="loadEventList"
    />

    <el-dialog :title="title" v-model="open" width="1200px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-card shadow="never">
          <template #header>
            <b>事件信息</b>
          </template>
          <el-form-item :label="$t('Message.Event.Key')" prop="id">
            <el-input v-model="form.eventId">
              <template #append>
                <el-button icon="Search" @click="handleSelectInnerEvent" />
              </template>
            </el-input>
          </el-form-item>
          <el-form-item :label="$t('Message.Event.Name')" prop="name">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item :label="$t('Message.Event.Status')" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio v-for="item in EnableOrDisable" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="$t('Common.Remark')" prop="remark">
            <el-input v-model="form.remark" type="textarea" :rows="3" />
          </el-form-item>
        </el-card>
        <el-card shadow="never" class="mt5">
          <template #header>
            <el-row justify="space-between">
              <el-col :span="12">
                <b>{{ $t("Message.Event.NotifyConfig") }}</b>
              </el-col>
              <el-col :span="12" align="right">
                <el-button type="primary" icon="Plus" @click="handleAddNotify">
                  {{ $t("Common.Add") }}
                </el-button>
              </el-col>
            </el-row>
          </template>
          
          <div class="notify-config-list">
            <div v-for="(item, index) in form.notifies" :key="item.eventId">
              <div class="el-form-item el-form-item--label-right">
                <label class="el-form-item__label" style="width: 140px;">{{ index + 1 }}. {{ $t("Message.Event.NotifyConfig") }}</label>
                <div class="el-form-item__content">
                  [{{ item.type }}] {{ item.name }}
                  <el-button link type="primary" icon="Delete" @click="handleDeleteNotify(index)">{{ $t("Common.Delete") }}</el-button>
                </div>
              </div>
              <div v-if="$tools.isNotEmpty(item.type) && item.type !== 'sms'" class="el-form-item el-form-item--label-right">
                <label class="el-form-item__label" style="width: 140px;">{{ $t("Message.Event.Template") }}</label>
                <div class="el-form-item__content">
                  <message-template-input v-model="item.templateId" v-model:type="item.type" />
                </div>
              </div>
              <div v-if="item.type === 'sms'" class="el-form-item el-form-item--label-right">
                <label class="el-form-item__label" style="width: 140px;">{{ $t("Message.Event.SMSTemplateId") }}</label>
                <div class="el-form-item__content">
                  <el-input v-model="item.params.templateId" />
                </div>
              </div>
              <div v-if="item.type === 'sms'" class="el-form-item el-form-item--label-right">
                <label class="el-form-item__label" style="width: 140px;">{{ $t("Message.Event.PhoneNumberSet") }}</label>
                <div class="el-form-item__content">
                  <el-input-tag v-model="item.params.phoneNumberSet" :placeholder="$t('OPS.Notify.PhoneNumberSetPlaceholder')" />
                </div>
              </div>
              <div v-if="item.type === 'email'" class="el-form-item el-form-item--label-right">
                <label class="el-form-item__label" style="width: 140px;">{{ $t("Message.Event.EmailTo") }}</label>
                <div class="el-form-item__content">
                  <el-input-tag v-model="item.params.to" :placeholder="$t('OPS.Notify.EmailToPlaceholder')" />
                </div>
              </div>
              <el-divider />
            </div>
          </div>
        </el-card>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
          <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
        </div>
      </template>
    </el-dialog>
    <message-config-selector v-model:open="openConfigSelector" @ok="handleSelectConfigOk" />
    <el-dialog title="测试发送" v-model="openTest" width="800px" append-to-body>
      <el-form ref="formTestRef" :model="formTest" :rules="rulesTest" label-width="140px">
        <el-form-item :label="$t('Message.Event.TestEventParams')" prop="params">
          <PairsInput v-model="formTest.params" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="handleTestSend">{{ $t("Common.Confirm") }}</el-button>
          <el-button @click="cancelTest">{{ $t("Common.Cancel") }}</el-button>
        </div>
      </template>
    </el-dialog>
    <message-inner-event-selector v-model:open="openInnerEventSelector" @ok="handleSelectInnerEventOk" />
  </div>
</template>
<script setup name="MessageEventIndex">
import * as messageEventApi from "@/api/tool/message/event";
import MessageTemplateInput from "../template/templateInput.vue";
import MessageConfigSelector from "../config/configSelector.vue";
import PairsInput from '@/views/components/PairsInput'
import MessageInnerEventSelector from "./innerEventSelector.vue";
import { codeValidator } from '@/utils/validate';

const openTest = ref(false);
const { proxy } = getCurrentInstance();
const { EnableOrDisable } = proxy.useDict("EnableOrDisable")

const dataList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const selectedIds = ref([]);
const title = ref("");
const total = ref(0);
const configOptions = ref([]);
const data = reactive({
  form: {
  },
  queryParams: {
    page: 1,
    size: 10,
    type: undefined,
  },
  rules: {
    eventId: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
      { validator: codeValidator, trigger: 'blur' },
    ],
    name: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ],
    status: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
    ]
  },
  formTest: {},
  rulesTest: {},
});
const { queryParams, form, rules, formTest, rulesTest } = toRefs(data);

const openConfigSelector = ref(false);
const openInnerEventSelector = ref(false);

onMounted(() => {
  loadEventList();
});

function handleSelectInnerEventOk(selected) {
  form.value.eventId = selected.value;
  form.value.name = selected.label;
  openInnerEventSelector.value = false;
}

function handleSelectInnerEvent() {
  openInnerEventSelector.value = true;
}

function loadEventList() {
  loading.value = true;
  messageEventApi.getEventList(queryParams.value).then((response) => {
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
  loadEventList();
}

function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map((item) => item.eventId);
}

function handleAdd() {
  form.value = {
    create: true,
    status: '1', // 0: 禁用, 1: 启用
    notifies: []
  };
  title.value = proxy.$t("Message.Event.AddTitle");
  open.value = true;
}

function handleUpdate(row) {
  if (!row.eventId && selectedIds.value.length == 0) {
    proxy.$modal.msgWarning(proxy.$t("Common.SelectFirst"));
    return;
  }
  messageEventApi.getEventDetail(row.eventId).then(response => {
    form.value = response.data;
  });
  title.value = proxy.$t("Message.Event.EditTitle");
  open.value = true;
}

function submitForm() {
  proxy.$refs["formRef"].validate((valid) => {
    if (valid) {
      messageEventApi.saveEvent(form.value).then((response) => {
        proxy.$modal.msgSuccess(proxy.$t("Common.SaveSuccess"));
        open.value = false;
        loadEventList();
      });
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm(proxy.$t("Common.ConfirmDelete")).then(function () {
    return messageEventApi.deleteEvents([row.eventId]);
  }).then(() => {
    proxy.$modal.msgSuccess(proxy.$t("Common.DeleteSuccess"));
    loadEventList();
  }).catch(() => {});
}

function handleAddNotify() {
  openConfigSelector.value = true;
}

function handleSelectConfigOk(selected) {
  const notify = form.value.notifies.find((item) => item.eventId === selected.eventId);
  if (notify) {
    proxy.$modal.msgWarning(proxy.$t("Common.ConfigAlreadyExists"));
    return;
  }
  form.value.notifies.push({
    configId: selected.configId,
    type: selected.type,
    name: selected.name,
    templateId: undefined,
    params: {
      phoneNumberSet: [],
      toAddress: []
    }
  });
  openConfigSelector.value = false;
}

function handleDeleteNotify(index) {
  form.value.notifies = form.value.notifies.filter((_, i) => i !== index);
}

function handleTest(row) {
  formTest.value = {
    eventId: row.eventId,
    params: {}
  };
  openTest.value = true;
}

function handleTestSend() {
  proxy.$refs.formTestRef.validate((valid) => {
    if (valid) {
      messageEventApi.testEvent(formTest.value).then(response => {
        proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
        openTest.value = false;
      });
    }
  });
}

function cancelTest() {
  openTest.value = false;
  proxy.resetForm("formTestRef");
}
</script>
