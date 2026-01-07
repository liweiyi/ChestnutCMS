<template>
  <div class="dict-data-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:dict:add','system:dict:edit']"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:dict:add','system:dict:edit']"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:dict:add','system:dict:edit']"
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
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-form v-show="showSearch" :model="queryParams" ref="queryFormRef" :rules="queryRules" class="el-form-search" :inline="true">
      <el-form-item prop="dictValue">
        <el-input
          v-model="queryParams.dictValue"
          :placeholder="$t('System.Dict.Placeholder.DictValue')"
          clearable
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
 
    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.Dict.DictLabel')" align="center" prop="dictLabel">
        <template #default="scope">
          <span v-if="!scope.row.listClass || scope.row.listClass == 'default' || scope.row.listClass == 'default'">{{scope.row.dictLabel}}</span>
          <el-tag v-else :type="scope.row.listClass">{{scope.row.dictLabel}}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.Dict.DictValue')" align="center" prop="dictValue" />
      <el-table-column :label="$t('System.Dict.DictSort')" align="center" prop="dictSort" />
      <el-table-column :label="$t('System.Dict.SystemFixed')" align="center" prop="fixed">
        <template #default="scope">
          <dict-tag :options="YesOrNo" :value="scope.row.fixed"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Remark')" align="center" prop="remark" :show-overflow-tooltip="true" />
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
            v-hasPermi="['system:dict:add','system:dict:edit']"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:dict:add','system:dict:edit']"
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
        <el-form-item :label="$t('System.Dict.DictType')">
          <el-input v-model="form.dictType" :disabled="true" />
        </el-form-item>
        <el-form-item :label="$t('System.Dict.DictLabel')" prop="dictLabel">
          <el-input v-model="form.dictLabel" :placeholder="$t('System.Dict.Placeholder.DictLabel')">
            <template #append v-if="form.dictCode">
              <i18n-editor :languageKey="'DICT.' + form.dictType + '.' + form.dictValue"></i18n-editor>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('System.Dict.DictValue')" prop="dictValue">
          <el-input v-model="form.dictValue" :placeholder="$t('System.Dict.Placeholder.DictValue')" />
        </el-form-item>
        <el-form-item :label="$t('System.Dict.CssClass')" prop="cssClass">
          <el-input v-model="form.cssClass" :placeholder="$t('System.Dict.Placeholder.CssClass')" />
        </el-form-item>
        <el-form-item :label="$t('System.Dict.DictSort')" prop="dictSort">
          <el-input-number v-model="form.dictSort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item :label="$t('System.Dict.ListClass')" prop="listClass">
          <el-select v-model="form.listClass" :placeholder="$t('System.Dict.Placeholder.ListClass')">
            <el-option
              v-for="item in listClassOptions"
              :key="item.value"
              :label="item.label + '(' + item.value + ')'"
              :value="item.value"
            ></el-option>
          </el-select>
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
  </div>
</template>
 
<script setup name="Data">
import { listData, getData, delData, addData, updateData } from "@/api/system/dict/data";
import I18nEditor from '@/views/components/I18nFieldEditor';
import useDictStore from '@/store/modules/dict'

const props = defineProps({
  dictType: {
    type: String,
    required: false,
    default: ''
  }
});

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
const dataList = ref([]);
// 弹出层标题
const title = ref("");
// 是否显示弹出层
const open = ref(false);
// 数据标签回显样式
const listClassOptions = [
  {
    value: "default",
    label: proxy.$t('System.Dict.ListClassOptions.Default')
  },
  {
    value: "primary",
    label: proxy.$t('System.Dict.ListClassOptions.Primary')
  },
  {
    value: "success",
    label: proxy.$t('System.Dict.ListClassOptions.Success')
  },
  {
    value: "info",
    label: proxy.$t('System.Dict.ListClassOptions.Info')
  },
  {
    value: "warning",
    label: proxy.$t('System.Dict.ListClassOptions.Warning')
  },
  {
    value: "danger",
    label: proxy.$t('System.Dict.ListClassOptions.Danger')
  }
];

const data = reactive({
   queryParams: {
      pageNum: 1,
      pageSize: 10,
      dictValue: undefined,
      dictType: undefined,
   },
   queryRules: {
      dictLabel: [
         { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
      ]
   },
   form: {
      dictCode: undefined,
      dictLabel: undefined,
      dictValue: undefined,
      cssClass: undefined,
      listClass: 'default',
      dictSort: 0,
      remark: undefined
   },
   rules: {
      dictLabel: [
         { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
         { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
      ],
      dictValue: [
         { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
         { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
      ],
      dictSort: [
         { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
      ],
      cssClass: [
         { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: "change" }
      ],
      remark: [
         { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [ 500 ]), trigger: "change" }
      ]
   }
});
const { queryParams, queryRules, form, rules } = toRefs(data);

watch(() => props.dictType, (newV, oldV) => {
  console.log('dictType changed:', oldV, '->', newV);
  if (newV && newV != '') {
    queryParams.value.dictType = newV;
    nextTick(() => {
      getList();
    });
  }
}, { 
  immediate: true,  // 立即执行一次
  flush: 'post'     // 在组件更新后执行
});

/** 查询字典数据列表 */
function getList() {
   if (!queryParams.value.dictType || queryParams.value.dictType == '') {
    return;
   }
  proxy.$refs.queryFormRef.validate(valid => {
    if (valid) {
      loading.value = true;
      listData(queryParams.value).then(response => {
        dataList.value = response.data.rows;
        total.value = parseInt(response.data.total);
        loading.value = false;
      });
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
    dictCode: undefined,
    dictLabel: undefined,
    dictValue: undefined,
    cssClass: undefined,
    listClass: 'default',
    dictSort: 0,
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
  queryParams.value.dictType = props.dictType;
  handleQuery();
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = proxy.$t('System.Dict.Dialog.AddData');
  form.value.dictType = queryParams.value.dictType;
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.dictCode)
  single.value = selection.length!=1
  multiple.value = !selection.length
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const dictCode = row.dictCode || ids.value[0];
  getData(dictCode).then(response => {
   response.data.dictSort = parseInt(response.data.dictSort);
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t('System.Dict.Dialog.EditData');
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.dictCode != undefined) {
        updateData(form.value).then(response => {
          useDictStore().removeDict(queryParams.value.dictType);
          proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
          open.value = false;
          getList();
        });
      } else {
        addData(form.value).then(response => {
          useDictStore().removeDict(queryParams.value.dictType);
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
  const dictCodes = row.dictCode ? [ row.dictCode ] : ids.value;
  proxy.$modal.confirm(proxy.$t('System.Dict.ConfirmDeleteDatas', [ dictCodes ])).then(function() {
    return delData(dictCodes);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
    useDictStore().removeDict(queryParams.value.dictType);
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.exportExcel('system/dict/data/list', {
    ...queryParams.value
  }, `dict_data_${proxy.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
}
</script>