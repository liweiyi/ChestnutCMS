<template>
  <div class="app-container">
    <el-row justify="space-between">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5" class="permi-wrap">
            <el-button
              type="primary"
              plain
              icon="Plus"
              @click="handleAdd"
              v-hasPermi="['system:config:add']"
            >{{ $t('Common.Add') }}</el-button>
          </el-col>
          <el-col :span="1.5" class="permi-wrap">
            <el-button
              type="success"
              plain
              icon="Edit"
              :disabled="single"
              @click="handleUpdate"
              v-hasPermi="['system:config:edit']"
            >{{ $t('Common.Edit') }}</el-button>
          </el-col>
          <el-col :span="1.5" class="permi-wrap">
            <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="multiple"
              @click="handleDelete"
              v-hasPermi="['system:config:remove']"
            >{{ $t('Common.Delete') }}</el-button>
          </el-col>
          <el-col :span="1.5" class="permi-wrap">
            <el-button
              type="warning"
              plain
              icon="Download"
              @click="handleExport"
              v-hasPermi="['system:config:export']"
            >{{ $t('Common.Export') }}</el-button>
          </el-col>
          <el-col :span="1.5" class="permi-wrap">
            <el-button
              type="danger"
              plain
              icon="Refresh"
              @click="handleRefreshCache"
              v-hasPermi="['system:config:remove']"
            >{{ $t('Common.RefreshCache') }}</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align: right">
        <el-form
          :model="queryParams"
          ref="queryFormRef"
          class="el-form-search"
          :inline="true"
          @submit.prevent
        >
          <el-form-item prop="configKey">
            <el-input
              v-model="queryParams.configKey"
              :placeholder="$t('System.Config.Placeholder.ConfigKey')"
              clearable
              style="width: 240px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button type="primary" icon="Search" @click="handleQuery">
                {{ $t('Common.Search') }}
              </el-button>
              <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column
        :label="$t('System.Config.ConfigName')"
        align="left"
        prop="configName"
        :show-overflow-tooltip="true"
      />
      <el-table-column
        :label="$t('System.Config.ConfigKey')"
        align="left"
        prop="configKey"
        :show-overflow-tooltip="true"
      />
      <el-table-column :label="$t('System.Config.ConfigValue')" align="left" prop="configValue" />
      <el-table-column :label="$t('System.Config.ConfigType')" align="center" width="120" prop="fixed">
        <template #default="scope">
          <dict-tag :options="YesOrNo" :value="scope.row.fixed" />
        </template>
      </el-table-column>
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
        width="160"
      >
        <template #default="scope">
          <el-button
            link
            type="success"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:config:edit']"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:config:remove']"
          >{{ $t('Common.Delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadList"
    />

    <el-dialog
      :title="title"
      v-model="open"
      width="600px"
      :close-on-click-modal="false"
      append-to-body
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('System.Config.ConfigName')" prop="configName">
          <el-input
            v-model="form.configName"
            :placeholder="$t('System.Config.Placeholder.ConfigName')"
            :disabled="$tools.isNotEmpty(form.configId)"
          >
          <template #append v-if="$tools.isNotEmpty(form.configId)">
              <i18n-editor :languageKey="'CONFIG.' + form.configKey"></i18n-editor>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('System.Config.ConfigKey')" prop="configKey">
          <el-input
            v-model="form.configKey"
            :placeholder="$t('System.Config.Placeholder.ConfigKey')"
          />
        </el-form-item>
        <el-form-item :label="$t('System.Config.ConfigValue')" prop="configValue">
          <el-input
            v-model="form.configValue"
            :placeholder="$t('System.Config.Placeholder.ConfigValue')"
          />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SystemConfigIndex">
import { codeValidator } from '@/utils/validate';
import {
  listConfig,
  getConfig,
  delConfig,
  addConfig,
  updateConfig,
  refreshCache,
} from '@/api/system/config';
import I18nEditor from '@/views/components/I18nFieldEditor';

const { proxy } = getCurrentInstance();
const { YesOrNo } = proxy.useDict('YesOrNo');

const loading = ref(false);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const configList = ref([]);
const title = ref('');
const open = ref(false);
const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    configKey: undefined,
  },
  form: {},
  rules: {
    configName: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
      { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [100]), trigger: 'change' },
    ],
    configKey: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
      { validator: codeValidator, trigger: 'change' },
      { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [100]), trigger: 'change' },
    ],
    configValue: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
      { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [500]), trigger: 'change' },
    ],
    remark: [
      { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [500]), trigger: 'change' },
    ],
  },
});
const { queryParams, form, rules } = toRefs(data);

function loadList() {
  loading.value = true;
  listConfig(queryParams.value).then(response => {
    configList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  proxy.resetForm('form');
  form.value = {};
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  loadList();
}

function resetQuery() {
  proxy.resetForm('queryForm');
  handleQuery();
}

function handleAdd() {
  reset();
  nextTick(() => {
    open.value = true;
    title.value = proxy.$t('System.Config.Dialog.Add');
  });
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.configId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function handleUpdate(row) {
  reset();
  const configId = row.configId || ids.value;
  getConfig(configId).then(response => {
    response.data.configName = `{CONFIG.${response.data.configKey}}`
    form.value = response.data;
  });
  open.value = true;
  title.value = proxy.$t('System.Config.Dialog.Edit');
}

function submitForm() {
  proxy.$refs['formRef'].validate(valid => {
    if (valid) {
      if (form.value.configId != undefined) {
        updateConfig(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.Success'));
          open.value = false;
          loadList();
        });
      } else {
        addConfig(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'));
          open.value = false;
          loadList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const configIds = row.configId || ids.value;
  proxy.$modal
    .confirm(proxy.$t('Common.ConfirmDelete'))
    .then(function () {
      return delConfig(configIds);
    })
    .then(() => {
      loadList();
      proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
    })
    .catch(() => {});
}

function handleExport() {
  proxy.exportExcel(
    'system/config/list',
    {
      ...queryParams.value,
    },
    `config_${proxy.parseTime(new Date(), '{y}{m}{d}{h}{i}{s}')}.xlsx`,
  );
}

function handleRefreshCache() {
  refreshCache().then(() => {
    proxy.$modal.msgSuccess(proxy.$t('Common.Success'));
  });
}

loadList();
</script>
