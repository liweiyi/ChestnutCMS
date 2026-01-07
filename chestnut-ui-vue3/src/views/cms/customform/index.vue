<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          plain
          type="primary"
          icon="Plus"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="Edit"
          :disabled="ids.length != 1"
          @click="handleEdit">{{ $t("Common.Edit") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="danger"
          icon="Delete"
          :disabled="ids.length == 0"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadCustomFormList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
        <el-form-item prop="query">
          <el-input
            v-model="queryParams.query"
            clearable
            :placeholder="$t('CMS.CustomForm.Name')"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="status">
          <el-select 
            v-model="queryParams.status"
            clearable
            :placeholder="$t('CMS.CustomForm.Status')"
            style="width: 110px">
            <el-option 
              v-for="dict in CustomFormStatus"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table 
      v-loading="loading"
      :data="dataList"
      @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="55"
        align="center" />
      <el-table-column :label="$t('CMS.CustomForm.Name')" align="left">
        <template #default="scope">
          <el-link type="primary" @click="handleViewData(scope.row)" class="link-type">
            <span>{{ scope.row.name }}</span>
          </el-link>
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('CMS.CustomForm.Code')"
        align="left"
        prop="code" />
      <el-table-column :label="$t('CMS.CustomForm.Status')" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :options="CustomFormStatus" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('Common.CreateTime')"
        align="center"
        prop="createTime"
        width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column  
        :label="$t('Common.Operation')"
        fixed="right" 
        align="right"
        width="450" 
       >
        <template #default="scope">
          <el-button
            type="text"
            icon="Grid"
            @click="handleViewData(scope.row)">{{ $t("CMS.CustomForm.DataList") }}</el-button>
          <el-button
            type="text"
            icon="Setting"
            @click="handleFields(scope.row)">{{ $t("CMS.CustomForm.Fields") }}</el-button>
          <el-button
            type="text"
            icon="Promotion"
            @click="handlePublish(scope.row)">{{ $t("CMS.CustomForm.Publish") }}</el-button>
          <el-button
            v-show="scope.row.status==10"
            type="text"
            icon="Download"
            @click="handleOffline(scope.row)">{{ $t("CMS.CustomForm.Offline") }}</el-button>
          <el-button 
            type="text"
            icon="Edit"
            @click="handleEdit(scope.row)">{{ $t("Common.Edit") }}</el-button>
          <el-button
            type="text"
            icon="Delete"
            @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadCustomFormList" />

    <el-dialog 
      :title="title" 
      v-model:open="open"
      :close-on-click-modal="false" 
      width="600px" 
      append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item :label="$t('CMS.CustomForm.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.CustomForm.Code')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item v-if="!form.formId || form.formId == 0" :label="$t('CMS.CustomForm.TableName')" prop="tableName">
          <el-select v-model="form.tableName" filterable>
            <el-option
              v-for="item in xmodelDataTableList"
              :key="item"
              :label="item"
              :value="item">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.CustomForm.NeedCaptcha')" prop="needCaptcha">
          <el-switch
            v-model="form.needCaptcha"
            active-value="Y"
            inactive-value="N"
          ></el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.CustomForm.NeedLogin')" prop="needLogin">
          <el-switch
            v-model="form.needLogin"
            active-value="Y"
            inactive-value="N"
          ></el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.CustomForm.RuleLimit')" prop="ruleLimit">
          <el-select v-model="form.ruleLimit">
            <el-option 
              v-for="rule in limitRuleOptions"
              :key="rule.value"
              :label="rule.label"
              :value="rule.value" />
          </el-select>
        </el-form-item>
        <div v-if="form.formId && form.formId > 0">
          <el-form-item 
            v-for="item in form.templates" 
            :label="item.name" 
            :key="item.code">
            <el-input v-model="item.template">
              <template #append>
                <el-button icon="FolderOpened" @click="handleSelectTemplate(item.code)"></el-button>
              </template>
            </el-input>
          </el-form-item>
        </div>
        <el-form-item :label="$t('Common.Remark')">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleSubmitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="handleCancel">{{ $t('Common.Cancel') }}</el-button>
      </template>
    </el-dialog>
    <!-- 模板选择组件 -->
    <cms-template-selector 
      :open="openTemplateSelector" 
      :publishPipeCode="publishPipe" 
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel" />
  </div>
</template>
<script setup name="CmsCustomformIndex">
import { codeValidator } from '@/utils/validate';
import { listModelDataTables } from "@/api/meta/model";
import { getLimitRules, listCustomForms, getCustomForm, addCustomForm, editCustomForm, deleteCustomForms, publishCustomForm, offlineCustomForm } from "@/api/customform/customform";

import CmsTemplateSelector from '@/views/cms/contentcore/templateSelector';

const { proxy } = getCurrentInstance()
const { CustomFormStatus } = proxy.useDict('CustomFormStatus');

const loading = ref(true)
const showSearch = ref(true)
const openTemplateSelector = ref(false)
const publishPipe = ref("")
const xmodelDataTableList = ref([])
const ids = ref([])
const total = ref(0)
const limitRuleOptions = ref([])

const dataList = ref([])
const title = ref("")
const open = ref(false)
const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
  form: {
    needCaptcha: 'N',
    needLogin: 'N',
    dayLimit: 1
  },
  rules: {
    name: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: [ "change", "blur" ] }
    ],
    code: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { max: 50, message: proxy.$t('Common.RuleTips.MaxLength', [ 50 ]), trigger: [ "change", "blur" ] },
      { validator: codeValidator, trigger: "change" }
    ],
    tableName: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: [ "change", "blur" ] }
    ],
    ruleLimit: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    ],
    remark: [
      { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [ 500 ]), trigger: [ "change", "blur" ] }
    ]
  }
})
const { queryParams, form } = toRefs(objects)

onMounted(() => {
  loadLimitRules();
  loadXModelDataTableList();
  loadCustomFormList();
});

function loadLimitRules() {
  getLimitRules().then(response => {
    limitRuleOptions.value = response.data;
  }).catch(() => {});
}

function loadXModelDataTableList() {
  listModelDataTables("CmsCustomForm").then(response => {
    xmodelDataTableList.value = response.data.rows;
  }).catch(() => {});
}

function loadCustomFormList () {
  loading.value = true;
  listCustomForms(queryParams.value).then(response => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  }).catch(() => {
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  loadCustomFormList();
}

function resetQuery() {
  if (proxy.$refs.queryFormRef) {
    proxy.resetForm("queryFormRef");
  }
  handleQuery();
}

function handleSelectionChange (selection) {
  ids.value = selection.map(item => item.formId)
}

function reset () {
  proxy.resetForm("formRef");
  form.value = { 
    needCaptcha: 'N',
    needLogin: 'N',
    dayLimit: 1
  };
}

function handleSelectTemplate(publishPipeCode) {
  publishPipe.value = publishPipeCode;
  openTemplateSelector.value = true;
}

function handleTemplateSelected (template) {
  if (form.value.templates && Array.isArray(form.value.templates)) {
    form.value.templates.some(item => {
      if (item.code == publishPipe.value) {
          item.template = template;
          return true;
      }
      return false;
    });
  }
  openTemplateSelector.value = false;
}

function handleTemplateSelectorCancel () {
  openTemplateSelector.value = false;
}

function handleAdd () {
  reset();
  open.value = true;
  title.value = proxy.$t('CMS.CustomForm.AddTitle');
}

function handleEdit (row) {
  reset();
  const formId = row.formId ? row.formId : ids.value[0];
  getCustomForm(formId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t('CMS.CustomForm.EditTitle');
  }).catch(() => {});
}

function handleCancel () {
  open.value = false;
  reset();
}

function handleSubmitForm () {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.formId != undefined) {
        editCustomForm(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          open.value = false;
          loadCustomFormList();
        }).catch(() => {});
      } else {
        addCustomForm(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'));
          open.value = false;
          loadCustomFormList();
        }).catch(() => {});
      }
    }
  });
}

function handleDelete (row) {
  const formIds = row.formId ? [ row.formId ] : ids.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function () {
    return deleteCustomForms(formIds);
  }).then(() => {
    loadCustomFormList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(function () { });
}

function handleFields(row) {
  proxy.$router.push({ 
    path: "/operations/customform/fields", 
    query: { 
      modelId: row.modelId
    }
  });
}

function handlePublish(row) {
  publishCustomForm([ row.formId ]).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    loadCustomFormList();
  }).catch(() => {});
}

function handleOffline(row) {
  offlineCustomForm([ row.formId ]).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    loadCustomFormList();
  }).catch(() => {});
}

function handleViewData(row) {
  const formId = row.formId;
  proxy.$router.push({ path: "/operations/customform/data", query: { formId: formId } });
}
</script>