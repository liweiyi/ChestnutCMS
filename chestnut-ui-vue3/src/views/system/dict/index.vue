<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:dict:add']"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:dict:edit']"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:dict:remove']"
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
          v-hasPermi="['system:dict:add','system:dict:edit']"
        >{{ $t('Common.RefreshCache') }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-form v-show="showSearch" :model="queryParams" ref="queryFormRef" :rules="queryRules" class="el-form-search" :inline="true">
      <el-form-item prop="dictType">
        <el-input
          v-model="queryParams.dictType"
          :placeholder="$t('System.Dict.Placeholder.DictType')"
          clearable
          style="width: 240px"
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
 
    <el-table v-loading="loading" :data="typeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.Dict.DictName')" align="left" prop="dictName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.Dict.DictType')" align="left" :show-overflow-tooltip="true">
        <template #default="scope">
         <el-button type="text" @click="handleDictData(scope.row)">{{ scope.row.dictType }}</el-button>
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.Dict.SystemFixed')" align="center" width="80" prop="fixed">
        <template #default="scope">
          <dict-tag :options="YesOrNo" :value="scope.row.fixed"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Remark')" align="left" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" width="160">
        <template #default="scope">
          <el-button
            link
            type="success"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:dict:edit']"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:dict:remove']"
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
 
    <el-dialog v-model="open" :title="title" width="600px" :close-on-click-modal="false" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('System.Dict.DictName')" prop="dictName">
          <el-input v-model="form.dictName" :placeholder="$t('System.Dict.Placeholder.DictName')">
            <template #append v-if="form.dictId&&form.dictId.length>0">
              <i18n-editor :languageKey="'DICT.' + form.dictType"></i18n-editor>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('System.Dict.DictType')" prop="dictType">
          <el-input v-model="form.dictType" :placeholder="$t('System.Dict.Placeholder.DictType')" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">{{ $t('Common.Confirm') }}</el-button>
          <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 字典数据 -->
     <el-drawer 
      direction="rtl"
      size="70%"
      destroy-on-close
      v-model="dictDataOpen"
      :title="$t('System.Dict.DataList')"
      append-to-body>
      <dict-data :dictType="dictType" />
     </el-drawer>
  </div>
</template>
 
<script setup name="SystemDictIndex">
import { listType, getType, delType, addType, updateType, refreshCache } from "@/api/system/dict/type";
import I18nEditor from '@/views/components/I18nFieldEditor';
import DictData from './data.vue';
import useDictStore from '@/store/modules/dict'

const { proxy } = getCurrentInstance();
const { YesOrNo } = proxy.useDict('YesOrNo');

// 遮罩层
const loading = ref(true);
// 选中数组
const ids = ref([]);
// 非单个禁用
const single = ref(true);
// 非多个禁用
const multiple = ref(true);
// 显示搜索条件
const showSearch = ref(true);
// 总条数
const total = ref(0);
// 字典表格数据
const typeList = ref([]);
// 弹出层标题
const title = ref("");
// 是否显示弹出层
const open = ref(false);
// 是否显示字典数据弹出层
const dictDataOpen = ref(false);
// 当前字典类型
const dictType = ref('');

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dictName: undefined,
    dictType: undefined,
  },
  queryRules: {
    dictType: [
      { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
    ]
  },
  form: {
    dictId: undefined,
    dictName: undefined,
    dictType: undefined,
    remark: undefined
  },
  rules: {
    dictName: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
    ],
    dictType: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
    ],
    remark: [
      { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [ 500 ]), trigger: "change" }
    ]
  }
});
const { queryParams, queryRules, form, rules } = toRefs(data);

/** 查询字典类型列表 */
function getList() {
  proxy.$refs.queryFormRef.validate(valid => {
    if (valid) {
      loading.value = true;
      listType(queryParams.value).then(response => {
          typeList.value = response.data.rows;
          total.value = parseInt(response.data.total);
          loading.value = false;
        }
      );
    }
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    dictId: undefined,
    dictName: undefined,
    dictType: undefined,
    remark: undefined
  };
  proxy.resetForm("formRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = proxy.$t('System.Dict.Dialog.Add');
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.dictId)
  single.value = selection.length!=1
  multiple.value = !selection.length
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const dictId = row.dictId || ids.value[0]
  getType(dictId).then(response => {
    form.value = response.data;
  });
   open.value = true;
   title.value = proxy.$t('System.Dict.Dialog.Edit');
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.dictId != undefined) {
        updateType(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
          open.value = false;
          getList();
        });
      } else {
        addType(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const dictIds = row.dictId ? [ row.dictId ] : ids.value;
  proxy.$modal.confirm(proxy.$t('System.Dict.ConfirmDelete', [ dictIds ])).then(function() {
    return delType(dictIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.exportExcel('system/dict/type/list', {
    ...queryParams.value
  }, `dict_type_${proxy.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
}

/** 刷新缓存按钮操作 */
function handleRefreshCache() {
  refreshCache().then(() => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    useDictStore().cleanDict()
  });
}

/** 字典数据按钮操作 */
function handleDictData(row) {
   dictType.value = row.dictType;
  dictDataOpen.value = true;
}

onMounted(() => {
  getList();
});
</script>