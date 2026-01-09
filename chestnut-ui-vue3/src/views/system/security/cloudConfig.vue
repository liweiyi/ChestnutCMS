<template>
  <div class="login-config-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <right-toolbar :search="false" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="configId" width="180" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.CloudConfig.Type')" width="180" align="center">
        <template #default="scope"> 
          <dict-tag :options="typeOptions" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.CloudConfig.Name')" align="left" prop="configName" />
      <el-table-column :label="$t('System.CloudConfig.Desc')" align="left" prop="configDesc" />
      <el-table-column :label="$t('System.CloudConfig.Status')" align="center" prop="status" width="180">
        <template #default="scope">
          <dict-tag :options="EnableOrDisable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" width="160">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
              link
              type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
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

    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" v-loading="loading" :rules="rules" label-width="100px">
        <el-form-item :label="$t('System.CloudConfig.Type')" prop="type">  
          <el-select v-model="form.type" :disabled="form.configId != undefined">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('System.CloudConfig.Name')" prop="configName">
          <el-input v-model="form.configName" />
        </el-form-item>
        <el-form-item :label="$t('System.CloudConfig.Desc')" prop="configDesc">
          <el-input v-model="form.configDesc" />
        </el-form-item>
        <el-form-item :label="$t('System.CloudConfig.Status')" prop="status">
          <el-switch v-model="form.status" active-value="0" inactive-value="1" />
        </el-form-item>
        <CloudConfigVolc v-if="form.type === 'Volc'" v-model="form.configProps" />
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">{{ $t('Common.Submit') }}</el-button>
          <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="SysCloudConfig">
import * as cloudConfigApi from "@/api/cloud/config";
import CloudConfigVolc from "./cloud/volc.vue";

const { proxy } = getCurrentInstance()

const { EnableOrDisable } = proxy.useDict('EnableOrDisable')

// 响应式数据
const formRef = ref()
const loading = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const configList = ref([])
const title = ref("")
const open = ref(false)
const rules = reactive({
  name: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  type: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  status: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  configProps: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ]
})
const objects = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
})
const { form, queryParams } = toRefs(objects);
const typeOptions = ref([]);

onMounted(() => {
  loadCloudConfigTypeOptions();
  getList();
});

const loadCloudConfigTypeOptions = () => {
  cloudConfigApi.getTypeOptions().then(response => {
    typeOptions.value = response.data;
  });
}

const getList = () => {
  loading.value = true;
  cloudConfigApi.getList(queryParams.value).then(response => {
    configList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

const cancel = () => {
  open.value = false;
  reset();
}

const reset = () => {
  proxy.resetForm("formRef");
  form.value = {
    configProps: {}
  };
}

const handleAdd = () => {
  reset();
  open.value = true;
  title.value = proxy.$t('System.CloudConfig.AddTitle');
}

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.configId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

const handleUpdate = (row) => {
  reset();
  const configId = row.configId || ids.value
  cloudConfigApi.getDetail(configId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t('System.CloudConfig.EditTitle');
  });
}

const submitForm = () => {
  formRef.value.validate(valid => { 
    if (valid) {
      if (form.value.configId != undefined) {
        cloudConfigApi.updateConfig(form.value).then(res => {
          proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
          open.value = false;
          getList();
        });
      } else {
        cloudConfigApi.addConfig(form.value).then(res => {
          proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
          open.value = false;
          getList();
        });
      }
    }
  });
}

const handleDelete = (row) => {
  const configIds = row.configId ? [ row.configId ] : ids.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(() => {
    return cloudConfigApi.deleteConfigs(configIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t("Common.DeleteSuccess"));
  }).catch(() => {});
}
</script>
<style scoped>
.tips {
  margin-left: 10px;
  color: #909399;
}
.el-form-item {
  margin-bottom: 12px;
}
.el-card {
  margin-bottom: 10px;
}
.el-input, .el-input-number  {
  width: 217px;
}
</style>