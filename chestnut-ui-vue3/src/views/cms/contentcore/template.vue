<template>
  <div class="app-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button 
              type="primary"
              icon="Plus"
              plain
              @click="handleAdd">{{ $t("Common.Add") }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              type="danger"
              icon="Delete"
              plain
              :disabled="selectedIds.length==0"
              @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              type="danger"
              icon="Remove"
              plain
              :disabled="selectedIds.length==0"
              @click="handleClearTemplateCache">{{ $t("CMS.Template.ClearIncludeCache") }}</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form 
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search">
          <el-form-item prop="publishPipeCode">
            <el-select v-model="queryParams.publishPipeCode" :placeholder="$t('CMS.ContentCore.PublishPipe')" style="width:140px;">
              <el-option
                v-for="pp in publishPipes"
                :key="pp.pipeCode"
                :label="pp.pipeName"
                :value="pp.pipeCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item prop="filename">
            <el-input v-model="queryParams.filename" :placeholder="$t('CMS.Template.Name')"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="Search"
                @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button 
                icon="Refresh"
                @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <el-table 
      v-loading="loading"
      :data="templateList"
      @selection-change="handleSelectionChange"
      @row-dblclick="handleEdit">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="80" />
      <el-table-column 
        :label="$t('CMS.ContentCore.PublishPipe')"
        align="center"
        width="160"
        prop="publishPipeCode" />
      <el-table-column 
        :label="$t('CMS.Template.Name')"
        align="left"
        :show-overflow-tooltip="true"
        prop="path">
      </el-table-column>
      <el-table-column 
        :label="$t('Common.Remark')"
        align="left"
        :show-overflow-tooltip="true"
        width="120"
        prop="remark">
      </el-table-column>
      <el-table-column 
        :label="$t('CMS.Template.FileSize')"
        align="right"
        width="160"
        prop="filesizeName" />
      <el-table-column 
        :label="$t('CMS.Template.ModifyTime')"
        align="center"
        width="160"
        prop="modifyTime" />
      <el-table-column 
        :label="$t('Common.Operation')"
        align="center"
        width="360" 
       >
        <template #default="scope">
          <el-button 
            type="text"
            icon="Edit"
            @click="handleRename(scope.row)">{{ $t('CMS.Template.Rename') }}</el-button>
          <el-button
            type="text"
            icon="Edit"
            @click="handleEdit(scope.row)">{{ $t("Common.Edit") }}</el-button>
          <el-button
            type="text"
            icon="Delete"
            @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
          <el-button
            type="text"
            icon="Remove"
            @click="handleClearTemplateCache(scope.row)">{{ $t("CMS.Template.ClearIncludeCache") }}</el-button>
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
    <!-- 添加或修改模板文件对话框 -->
    <el-dialog 
      :title="title"
      v-model="open"
      width="700px"
      append-to-body>
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px">
        <el-form-item v-if="!form.templateId||form.templateId==0" :label="$t('CMS.ContentCore.PublishPipe')" prop="publishPipeCode">
          <el-select v-model="form.publishPipeCode" >
            <el-option
              v-for="pp in publishPipes"
              :key="pp.pipeCode"
              :label="pp.pipeName"
              :value="pp.pipeCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.Template.Name')" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsContentcoreTemplate">
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import { getConfigKey } from "@/api/system/config"
import { getTemplateList, getTemplateDetail, renameTemplate, addTemplate, delTemplate, clearIncludeCache } from "@/api/contentcore/template";

const { proxy } = getCurrentInstance()

const loading = ref(false)
const selectedIds = ref([])
const templateList = ref([])
const total = ref(0)
const title = ref("")
const open = ref(false)
const publishPipes = ref([])
const templateSuffix = ref(".template.html")

const validatePath = (rule, value, callback) => {
  if (!value || value.length == 0 || !value.endsWith(templateSuffix.value)) {
    callback(new Error(proxy.$t('CMS.Template.RuleTips.Name', [ templateSuffix.value ])));
    return;
  }
  const hasMatchFail = value.substring(0, value.indexOf('.template.html')).split('\/').some(item => !(/^[A-Za-z0-9_\.\/]+$/.test(item)));
  if (hasMatchFail) {
      callback(new Error(proxy.$t('CMS.Template.RuleTips.Name', [ templateSuffix.value ])));
      return;
  }
  callback();
};

const objects = reactive({
  queryParams: {
    publishPipeCode: undefined,
    filename: undefined
  },
  form: {},
  rules: {
    path: [
      { trigger: "blur", validator: validatePath }
    ]
  }
})
const { queryParams, form, rules } = toRefs(objects);

onMounted(() => {
  loadPublishPipes()
  loadTemplateSuffix()
  getList()
})

const loadPublishPipes = () => {
  getPublishPipeSelectData().then(response => {
    publishPipes.value = response.data.rows;
  })
}

const loadTemplateSuffix = () => {
  getConfigKey("CMSTemplateSuffix").then(response => {
    templateSuffix.value = response.data;
  })
}

const getList = () => {
  loading.value = true;
  getTemplateList(queryParams.value).then(response => {
    templateList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  })
}

const cancel = () => {
  open.value = false;
  reset();
}

const reset = () => {
  proxy.resetForm("formRef");
}
/** 搜索按钮操作 */
const handleQuery = () => {
  getList();
}

const resetQuery = () => {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.templateId);
}
const handleAdd = () => {
  form.value = {};
  open.value = true;
  title.value = proxy.$t('CMS.Template.AddTitle');
}
const handleRename = (row) => {
  form.value = {};
  getTemplateDetail(row.templateId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t('CMS.Template.EditTitle');
  });
}
const handleEdit = (row) => {
  proxy.$router.push({ 
    path: "/cms/template/editor", 
    query: { 
      id: row.templateId
    } 
  });
}
const submitForm = () => {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.templateId) {
        renameTemplate(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          open.value = false;
          getList();
        }); 
      } else {
        addTemplate(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          open.value = false;
          getList();
        }); 
      }
      
    }
  });
}
const handleDelete = (row) => {
  const templateIds = row.templateId ? [ row.templateId ] : selectedIds.value
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function () {
    return delTemplate(templateIds);
  }).then(() => {
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
    getList();
  });
}
const handleClearTemplateCache = (row) => {
  const templateIds = row.templateId ? [ row.templateId ] : selectedIds.value
  clearIncludeCache(templateIds).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
  });
}
</script>
<style scoped>
.time {
  font-size: 13px;
  color: #999;
}
.el-card {
  margin-bottom: 10px;
  padding: 10px;
}
.r-image {
  width: 130px;
}
.el-form-search {
  width: 100%;
}
</style>