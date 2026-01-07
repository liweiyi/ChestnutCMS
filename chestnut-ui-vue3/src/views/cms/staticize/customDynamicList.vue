<template>
  <div class="cms-custom-dynamic-list-container">
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
          :disabled="single"
          @click="handleEdit">{{ $t("Common.Edit") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="danger"
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadDynamicPageList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
        <el-form-item prop="query">
          <el-input
            v-model="queryParams.query"
            clearable
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
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
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('CMS.Staticize.CustomDynamicPage.Code')" align="left" prop="code"></el-table-column>
      <el-table-column :label="$t('CMS.Staticize.CustomDynamicPage.Name')" align="left" prop="name"></el-table-column>
      <el-table-column :label="$t('CMS.Staticize.CustomDynamicPage.Path')" align="left" prop="path"></el-table-column>
      <el-table-column :label="$t('Common.CreateBy')" align="left" prop="createBy"></el-table-column>
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column  
        :label="$t('Common.Operation')"
        align="right"
        width="350" 
       >
        <template #default="scope">
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
      @pagination="loadDynamicPageList" />

    <el-dialog 
      :title="title" 
      v-model="open"
      :close-on-click-modal="false" 
      width="600px" 
      append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item :label="$t('CMS.Staticize.CustomDynamicPage.Name')" prop="name">
          <el-input v-model="form.name" :maxlength="30" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Staticize.CustomDynamicPage.Code')" prop="code">
          <el-input v-model="form.code" :maxlength="30" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Staticize.CustomDynamicPage.Path')" prop="path">
          <el-input v-model="form.path" :disabled="form.pageId && form.pageId > 0"/>
        </el-form-item>
        <el-form-item :label="$t('CMS.Staticize.CustomDynamicPage.InitDataType')">
          <el-checkbox-group v-model="form.initDataTypes">
            <el-checkbox v-for="item in initDataTypes" :key="item.type" :label="item.type">{{ item.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item 
          v-for="item in publishPipes" 
          :label="item.pipeName" 
          :key="item.pipeCode">
          <el-input v-model="item.template">
            <template #append>
              <el-button icon="FolderOpened" @click="handleSelectTemplate(item.pipeCode)"></el-button>
            </template>
          </el-input>
        </el-form-item>
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
<script setup name="CMSCustomDynamicList">
import { codeValidator } from '@/utils/validate';
import { getDynamicPageInitDataTypes, getDynamicPageList, getDynamicPageDetail, addDynamicPage, editDynamicPage, deleteDynamicPages } from "@/api/contentcore/dynamic";
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe"
import CmsTemplateSelector from '@/views/cms/contentcore/templateSelector';

const { proxy } = getCurrentInstance();


const loading = ref(true);
const showSearch = ref(true);
const openTemplateSelector = ref(false);
const publishPipe = ref("");
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const dataList = ref([]);
const title = ref("");
const open = ref(false);
const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
  form: {
    initDataTypes: [],
    templates: {}
  },
  rules: {
    name: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    code: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { validator: codeValidator, trigger: "change" },
    ],
    path: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    ]
  }
});
const { queryParams, form, rules } = toRefs(objects);
const initDataTypes = ref([]);
const publishPipes = ref([]);

onMounted(() => {
  loadPublishPipes();
  loadDynamicPageInitDataTypes();
  loadDynamicPageList();
});
function loadPublishPipes() {
  getPublishPipeSelectData().then(response => {
    publishPipes.value = response.data.rows;
    publishPipes.value.forEach(item => {
      item.template = ""
    });
  })
}
function loadDynamicPageInitDataTypes() {
  getDynamicPageInitDataTypes().then(response => {
    initDataTypes.value = response.data;
  });
}
function loadDynamicPageList() {
  loading.value = true;
  getDynamicPageList(queryParams.value).then(response => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}
function handleQuery() {
  queryParams.value.pageNum = 1;
  loadDynamicPageList();
}
function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}
function handleSelectionChange (selection) {
  ids.value = selection.map(item => item.pageId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}
function reset () {
  proxy.resetForm("formRef");
  form.value = {
    initDataTypes: [],
    templates: {}
  };
  publishPipes.value.forEach(item => item.template = "")
}
function handleSelectTemplate(publishPipeCode) {
  publishPipe.value = publishPipeCode;
  proxy.$nextTick(() => {
    openTemplateSelector.value = true;
  })
}
function handleTemplateSelected (template) {
  publishPipes.value.some(item => {
    if (item.pipeCode == publishPipe.value) {
      item.template = template;
      return true;
    }
    return false;
  });
  form.value.templates[publishPipe.value] = template;
  openTemplateSelector.value = false;
}
function handleTemplateSelectorCancel () {
  openTemplateSelector.value = false;
}
function handleAdd () {
  reset();
  open.value = true;
  title.value = proxy.$t('CMS.Staticize.CustomDynamicPage.AddTitle');
}
function handleEdit (row) {
  reset();
  const pageId = row.pageId ? row.pageId : ids.value[0];
  getDynamicPageDetail(pageId).then(response => {
    form.value = response.data;
    publishPipes.value.forEach(item => {
      item.template = form.value.templates[item.pipeCode] || ""
    })
    open.value = true;
    title.value = proxy.$t('CMS.Staticize.CustomDynamicPage.EditTitle');
  });
}
function handleCancel () {
  open.value = false;
  reset();
}
function handleSubmitForm () {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.pageId != undefined) {
        editDynamicPage(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          open.value = false;
          loadDynamicPageList();
        });
      } else {
        addDynamicPage(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'));
          open.value = false;
          loadDynamicPageList();
        });
      }
    }
  });
}
function handleDelete (row) {
  const pageIds = row.pageId ? [ row.pageId ] : ids.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function () {
    return deleteDynamicPages(pageIds);
  }).then(() => {
    loadDynamicPageList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(function () { });
}
</script>