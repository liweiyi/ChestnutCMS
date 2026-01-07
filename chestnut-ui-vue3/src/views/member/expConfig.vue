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
          :disabled="single"
          @click="handleUpdate">{{ $t("Common.Edit") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="danger"
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
        <el-form-item prop="levelType">
          <el-select
            v-model="queryParams.levelType"
            style="width: 160px"
            :placeholder="$t('Member.LevelType')"
            clearable
            @keyup.enter="handleQuery">
            <el-option
              v-for="lt in levelTypes"
              :key="lt.id"
              :label="lt.name"
              :value="lt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item prop="opType">
          <el-input
            v-model="queryParams.opType"
            clearable
            :placeholder="$t('Member.ExpOpType')"
            style="width: 180px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="Refresh"  @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>
    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="55"
        align="center" />
      <el-table-column 
        :label="$t('Member.ExpOpType')"
        align="center"
        width="180">
        <template #default="scope">
          {{ scope.row.opTypeName }}[{{ scope.row.opType }}]
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('Member.LevelType')"
        align="center"
        prop="levelTypeName" />
      <el-table-column 
        :label="$t('Member.Exp')"
        align="center"
        width="100"
        prop="exp" />
      <el-table-column 
        :label="$t('Member.ExpOpDayLimit')"
        align="center"
        width="100"
        prop="dayLimit">
        <template #default="scope">
          <span v-if="scope.row.dayLimit==0">{{ $t('Member.ExpOpNoLimit') }}</span>
          <span v-else>{{ scope.row.dayLimit }}</span>
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('Member.ExpOpTotalLimit')"
        align="center"
        width="100"
        prop="totalLimit">
        <template #default="scope">
          <span v-if="scope.row.totalLimit==0">{{ $t('Member.ExpOpNoLimit') }}</span>
          <span v-else>{{ scope.row.totalLimit }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('Common.Operation')"
        align="center"
        width="180" 
       >
        <template #default="scope">
          <el-button 
            type="text"
            icon="Edit"
            @click="handleUpdate(scope.row)">{{ $t("Common.Edit") }}</el-button>
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
      @pagination="getList" />

    <!-- 添加或修改对话框 -->
    <el-dialog 
      :title="title"
      v-model="open"
      :close-on-click-modal="false"
      width="600px"
      append-to-body>
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="145px">
        <el-form-item :label="$t('Member.ExpOpType')" prop="opTypeName">
          <el-input class="mr5"  v-model="form.opTypeName" :disabled="true" style="width: 186px" />
          <el-button 
            icon="Search" 
            type="success" 
            :disabled="form.configId!=undefined&&form.configId!=0" 
            @click="handleOpenSelector()">{{ $t('Common.Select') }}</el-button>
        </el-form-item>
        <el-form-item :label="$t('Member.LevelType')" prop="levelType">
          <el-select
            v-model="form.levelType"
            :disabled="form.configId!=undefined&&form.configId!=0"
            style="width:100%">
            <el-option
              v-for="lt in levelTypes"
              :key="lt.id"
              :label="lt.name"
              :value="lt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('Member.Exp')" prop="exp">
          <el-input-number v-model="form.exp" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('Member.ExpOpDayLimit')" prop="dayLimit">
          <el-input-number v-model="form.dayLimit" :min="0" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('Member.ExpOpTotalLimit')" prop="totalLimit">
          <el-input-number v-model="form.totalLimit" :min="0" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleSubmitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <!-- 选择操作项列表弹窗 -->
    <el-dialog 
      :title="title"
      v-model="selectorVisible"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-table 
        v-loading="loading"
        :height="500"
        :data="opTypes"
        highlight-current-row
        @row-dblclick="handleOpTypeDblClick"
        @current-change="handleOpTypeSelectionChange">
        <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="50" />
        <el-table-column :label="$t('Member.ExpOpType')" align="left">
          <template #default="scope">
            {{ scope.row.name }}[{{ scope.row.id }}]
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button type="primary" :disabled="okBtnDisabled" @click="handleSelectorOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleSelectorCancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="MemberExpConfig">
import { getLevelTypes } from "@/api/member/levelConfig";
import { getExpOperations, getExpConfigList, getExpConfigDetail, addExpConfig, updateExpConfig, deleteExpConfigs } from "@/api/member/expConfig";

const { proxy } = getCurrentInstance();

const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const dataList = ref([]);
const title = ref("");
const open = ref(false);
const levelTypes = ref([]);
const opTypes = ref([]);
const selectorVisible = ref(false);
const selectedOpType = ref(undefined);
const selectedOpTypeName = ref(undefined);
const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
  form: {
    exp: 0,
    dayLimit: 0,
    totalLimit: 0,
  },
  rules: {
    opType: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    levelType: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    exp: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    dayLimit: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    ],
    totalLimit: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    ],
    remark: [
      { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [ 500 ]), trigger: [ "change", "blur" ] },
    ]
  }
});
const { queryParams, form, rules } = toRefs(objects);

const okBtnDisabled = computed(() => {
  return !selectedOpType.value || selectedOpType.value.length == 0;
});

onMounted(() => {
  getList();
  loadLevelTypes();
  loadExpOperations();
});

function getList () {
  loading.value = true;
  getExpConfigList(queryParams.value).then(response => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function loadExpOperations() {
  getExpOperations().then(res => opTypes.value = res.data); 
}

function loadLevelTypes() {
  getLevelTypes().then(res => levelTypes.value = res.data)
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}
function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}
function handleSelectionChange (selection) {
  ids.value = selection.map(item => item.configId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}
function reset () {
  proxy.resetForm("formRef");
  form.value = { exp : 0, dayLimit: 0, totalLimit: 0 }
}
function handleAdd () {
  reset();
  open.value = true;
  title.value = proxy.$t('Member.AddExpOpTitle');
}
function handleUpdate (row) {
  reset();
  const configId = row.configId ? row.configId : ids.value[0];
  getExpConfigDetail(configId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t('Member.EditExpOpTitle');
  });
}

function handleCancel () {
  open.value = false;
  reset();
}   

function handleSubmitForm () {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.configId != undefined) {
        updateExpConfig(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          open.value = false;
          getList();
        });
      } else {
        addExpConfig(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'));
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete (row) {
  const configIds = row.configId ? [ row.configId ] : ids.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function () {
    return deleteExpConfigs(configIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(function () { });
}

function handleOpenSelector() {
  selectorVisible.value = true;
}

function handleSelectorOk() {
  if (selectedOpType.value) {
    form.value.opType = selectedOpType.value;
    form.value.opTypeName = selectedOpTypeName.value;
  }
  handleSelectorCancel();
}

function handleSelectorCancel() {
  selectorVisible.value = false;
  selectedOpType.value = undefined;
  selectedOpTypeName.value = undefined;
}

function handleOpTypeSelectionChange (selection) {
  if (selection) {
    selectedOpType.value = selection.id;
    selectedOpTypeName.value = selection.name;
  }
}

function handleOpTypeDblClick (row) {
  form.value.opType = row.id;
  form.value.opTypeName = row.name;
  handleSelectorCancel();
}
</script>