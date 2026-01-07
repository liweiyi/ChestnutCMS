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
            clearable
            :placeholder="$t('Member.LevelType')"
            @keyup.enter.native="handleQuery">
            <el-option
              v-for="lt in levelTypes"
              :key="lt.id"
              :label="lt.name"
              :value="lt.id"
            />
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
    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="55"
        align="center" />
      <el-table-column 
        :label="$t('Member.Level.Type')"
        align="center"
        prop="levelTypeName" />
      <el-table-column 
        :label="$t('Member.Level.Level')"
        align="center">
        <template #default="scope">
          Lv{{ scope.row.level }}
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('Member.Level.Name')"
        align="center"
        prop="name" />
      <el-table-column 
        :label="$t('Member.Level.NextNeedExp')"
        align="center"
        prop="nextNeedExp" />
      <el-table-column 
        :label="$t('Common.Remark')"
        align="center"
        prop="remark" />
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
      width="500px"
      append-to-body>
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px">
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
        <el-form-item :label="$t('Member.Level.Level')" prop="level">
          <el-input v-model="form.level" type="number" :disabled="form.configId!=undefined&&form.configId!=0"/>
        </el-form-item>
        <el-form-item :label="$t('Member.Level.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('Member.Level.NextNeedExp')" prop="nextNeedExp">
          <el-input v-model="form.nextNeedExp" type="number"/>
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
  </div>
</template>
<script setup name="MemberLevelConfig">
import { getLevelTypes, getLevelConfigList, getLevelConfigDetail, addLevelConfig, updateLevelConfig, deleteLevelConfigs } from "@/api/member/levelConfig";

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
const objects = reactive({
  queryParams: {
    levelType: undefined,
    pageNum: 1,
    pageSize: 10,
  },
  form: {},
  rules: {
    levelType: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    level: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    name: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { max: 30, message: proxy.$t('Common.RuleTips.MaxLength', [ 30 ]), trigger: [ "change", "blur" ] },
    ],
    nextNeedExp: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    ],
    remark: [
      { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [ 500 ]), trigger: [ "change", "blur" ] },
    ]
  }
});
const { queryParams, form, rules } = toRefs(objects);

onMounted(() => {
  getList();
  loadLevelTypes();
});

function getList () {
  loading.value = true;
  getLevelConfigList(queryParams.value).then(response => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function loadLevelTypes() {
  getLevelTypes().then(res => levelTypes.value = res.data)
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  queryParams.value = {
    levelType: undefined,
    pageNum: 1,
    pageSize: 10,
  };
  handleQuery();
}

function handleSelectionChange (selection) {
  ids.value = selection.map(item => item.configId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function reset () {
  proxy.resetForm("form");
  form.value = { exp : 0, dayLimit: 0, totalLimit: 0 }
}

function handleAdd () {
  reset();
  open.value = true;
  title.value = proxy.$t('Member.Level.AddTitle');
}

function handleUpdate (row) {
  reset();
  const configId = row.configId ? row.configId : ids.value[0];
  getLevelConfigDetail(configId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t('Member.Level.EditTitle');
  });
}

function handleCancel () {
  open.value = false;
  reset();
}

function handleSubmitForm () {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.configId != undefined) {
        updateLevelConfig(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          open.value = false;
          getList();
        });
      } else {
        addLevelConfig(form.value).then(response => {
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
    return deleteLevelConfigs(configIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(function () { });
}
</script>