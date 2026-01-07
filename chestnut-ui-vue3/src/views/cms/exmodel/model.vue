<template>
  <div class="app-container">
    <el-row :gutter="24">
      <el-col :span="12">
        <el-row>
          <el-button 
            plain
            type="primary"
            icon="Plus"
            @click="handleAdd">{{ $t("Common.Add") }}</el-button>
          <el-button 
            plain
            type="danger"
            icon="Delete"
            :disabled="selectedRows.length === 0"
            @click="handleBatchDelete">{{ $t("Common.Delete") }}</el-button>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form 
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input :placeholder="$t('CMS.ExModel.Placeholder.Query')" v-model="queryParams.query"></el-input>
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
    <el-table v-loading="loading" :data="xmodelList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('CMS.ExModel.Name')" prop="name" />
      <el-table-column :label="$t('CMS.ExModel.Code')" prop="code" />
      <el-table-column :label="$t('CMS.ExModel.OwnerType')" prop="ownerType" />
      <el-table-column :label="$t('CMS.ExModel.TableName')" prop="tableName" />
      <el-table-column :label="$t('Common.Operation')" align="center" width="300">
        <template #default="scope">
          <el-button
            type="text"
            icon="Setting"
            @click="handleFields(scope.row)">{{ $t("CMS.ExModel.Fields") }}</el-button>
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
      v-show="xmodelTotal>0"
      :total="xmodelTotal"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadXModelList" />
    <!-- 添加/编辑模型弹窗 -->
    <el-dialog 
      :title="title"
      v-model="open"
      width="600px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="el-form-dialog">
        <el-form-item :label="$t('CMS.ExModel.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.ExModel.Code')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item :label="$t('CMS.ExModel.TableName')" prop="tableName">
          <el-select v-model="form.tableName" filterable>
            <el-option
              v-for="item in xmodelDataTableList"
              :key="item"
              :label="item"
              :value="item">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input type="textarea" v-model="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleAddSave">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="closeDialog(false)">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsExmodelModel">
import { codeValidator } from '@/utils/validate';
import { listModelDataTables } from "@/api/meta/model";
import { addXModel, editXModel, deleteXModel, listXModel } from "@/api/contentcore/exmodel";

const { proxy } = getCurrentInstance()

const loading = ref(false)
const title = ref("")
const open = ref(false)
const xmodelDataTableList = ref([])
const xmodelList = ref([])
const xmodelTotal = ref(0)
const selectedRows = ref([])
const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    query: undefined
  },
  form: {},
})

const { queryParams, form } = toRefs(objects)

const rules = {
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
  remark: [
    { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [ 500 ]), trigger: [ "change", "blur" ] }
  ]
}

onMounted(() => {
  loadXModelDataTableList();
  loadXModelList();
})

const loadXModelList = () => {
  loading.value = true;
  listXModel(queryParams.value).then(response => {
    xmodelList.value = response.data.rows;
    xmodelTotal.value = parseInt(response.data.total);
    loading.value = false;
  });
}

const loadXModelDataTableList = () => {
  listModelDataTables("CmsExtend").then(response => {
    xmodelDataTableList.value = response.data.rows;
  });
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection.map(item => item);
}
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  loadXModelList();
}
const resetQuery = () => {
  proxy.$refs.queryFormRef.resetFields();
  handleQuery();
}
const closeDialog = (loadList) => {
  open.value = false;
  form.value = {};
  if (loadList) loadXModelList();
}
const handleAdd = () => {
  form.value = {};
  title.value = proxy.$t('CMS.ExModel.AddTitle');
  open.value = true;
}
const handleEdit = (row) => {
  form.value = row;
  title.value = proxy.$t('CMS.ExModel.EditTitle');
  open.value = true;
}
const handleAddSave = () => {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.modelId) {
        editXModel(form.value).then(response => {
          proxy.$modal.msgSuccess(response.msg);
          closeDialog(true);
        }); 
      } else {
        addXModel(form.value).then(response => {
          proxy.$modal.msgSuccess(response.msg);
          closeDialog(true);
        }); 
      }
    }
  });
}
const handleDelete = (row) => {
  doDelete([ row ]);
}
const handleBatchDelete = () => {
  if (selectedRows.value.length > 0) {
    doDelete(selectedRows.value);
  }
}
const doDelete = (xmodels) => {
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function () {
    return deleteXModel(xmodels);
  }).then(() => {
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
    loadXModelList();
  }).catch(function () { });
}
const handleFields = (row) => {
  proxy.$router.push({ 
    path: "/cms/exmodel/fields", 
    query: { 
      modelId: row.modelId
    } 
  });
}
</script>