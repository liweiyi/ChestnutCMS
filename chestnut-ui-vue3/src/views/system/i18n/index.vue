<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:i18ndict:add']"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:i18ndict:edit']"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:i18ndict:remove']"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
        >{{ $t('Common.Export') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="danger"
          plain
          icon="RefreshLeft"
          @click="handleRefreshCache"
          v-hasPermi="['system:i18ndict:add','system:i18ndict:edit']"
        >{{ $t('Common.RefreshCache') }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-form v-show="showSearch" :model="queryParams" :rules="queryRules" ref="queryFormRef" class="el-form-search" :inline="true">
      <el-form-item prop="langTag">
        <el-select
          v-model="queryParams.langTag"
          clearable
          :placeholder="$t('System.I18n.LangTag')"
          style="width: 135px"
        >
          <el-option
            v-for="dict in I18nDictType"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item prop="langKey">
        <el-input
          v-model="queryParams.langKey"
          clearable
          style="width: 160px"
          :placeholder="$t('System.I18n.LangKey')"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="langValue">
        <el-input
          v-model="queryParams.langValue"
          clearable
          :placeholder="$t('System.I18n.LangValue')"
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button-group>
          <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="dictList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.I18n.LangTag')" align="center" width="180" prop="langTag" />
      <el-table-column :label="$t('System.I18n.LangKey')" align="left" prop="langKey" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.I18n.LangValue')" align="left" prop="langValue" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.Operation')" width="160">
        <template #default="scope">
          <el-button
            link
            type="success"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:i18ndict:edit']"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:i18ndict:remove']"
          >{{ $t('Common.Delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog v-model="open" :title="title" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item :label="$t('System.I18n.LangTag')" prop="langTag">
          <el-select v-model="form.langTag">
            <el-option
              v-for="dict in I18nDictType"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('System.I18n.LangKey')" prop="langKey">
          <el-input v-model="form.langKey" />
        </el-form-item>
        <el-form-item :label="$t('System.I18n.LangValue')" prop="langValue">
          <el-input v-model="form.langValue" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">{{ $t('Common.Save') }}</el-button>
          <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SystemI18nIndex">
import { listI18nDict, getI18nDict, delI18nDict, addI18nDict, updateI18nDict, refreshCache } from "@/api/system/i18nDict";

const { proxy } = getCurrentInstance();
const { I18nDictType } = proxy.useDict('I18nDictType');

const loading = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const showSearch = ref(true);
const total = ref(0);
const dictList = ref([]);
const title = ref("");
const open = ref(false);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  langTag: undefined,
  langKey: undefined,
  langValue: undefined
});
const form = ref({});
const queryRules = reactive({
  langTag: [
    { max: 10, message: proxy.$t('Common.RuleTips.MaxLength', [ 10 ]), trigger: "change" }
  ],
  langKey: [
    { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
  ],
  langValue: [
    { max: 255, message: proxy.$t('Common.RuleTips.MaxLength', [ 255 ]), trigger: "change" }
  ],
});
const rules = reactive({
  langTag: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 10, message: proxy.$t('Common.RuleTips.MaxLength', [ 10 ]), trigger: "change" }
  ],
  langKey: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
  ],
  langValue: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 255, message: proxy.$t('Common.RuleTips.MaxLength', [ 255 ]), trigger: "change" }
  ]
});

function getList() {
  proxy.$refs.queryFormRef.validate(valid => {
    if (valid) {
      loading.value = true;
      listI18nDict(queryParams).then(response => {
          dictList.value = response.data.rows;
          total.value = parseInt(response.data.total);
          loading.value = false;
        }
      );
    }
  });
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = {};
  proxy.resetForm("formRef");
}

function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = proxy.$t("System.I18n.Dialog.Add");
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.dictId)
  single.value = selection.length!=1
  multiple.value = !selection.length
}

function handleUpdate(row) {
  reset();
  const dictId = row.dictId || ids.value[0]
  getI18nDict(dictId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t("System.I18n.Dialog.Edit");
  });
}

function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.dictId != undefined) {
        updateI18nDict(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
          open.value = false;
          getList();
        });
      } else {
        addI18nDict(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const dictIds = row.dictId || ids.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
      return delI18nDict(dictIds);
    }).then(() => {
      getList();
      proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    }).catch(() => {});
}

function handleExport() {
  proxy.exportExcel('system/i18n/dict/list', {
    ...queryParams
  }, `i18n_dict_${proxy.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
}

function handleRefreshCache() {
  proxy.$modal.loading(proxy.$t('System.I18n.Refreshing'));
  refreshCache().then(() => {
    proxy.$modal.msgSuccess(proxy.$t('System.I18n.RefreshSuccess'));
  }).finally(() => {
    proxy.$modal.closeLoading();
  });
}

onMounted(() => {
  getList();
});
</script>
