<template>
  <div class="app-container">
    <el-row :gutter="24">
      <el-col :span="12">
        <el-button 
          plain
          type="primary"
          icon="Plus"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
        <el-button 
          type="danger"
          icon="Delete"
          :disabled="selectedRows.length === 0"
          @click="handleBatchDelete">{{ $t("Common.Delete") }}</el-button>
        <el-button 
          plain
          type="warning"
          icon="Close"
          @click="handleClose">{{ $t("Common.Close") }}</el-button>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form 
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input :placeholder="$t('MetaModel.Placeholder.FieldQuery')" v-model="queryParams.query"></el-input>
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
    <el-row>
      <el-table v-loading="loading" :data="fieldList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
        <el-table-column :label="$t('MetaModel.FieldName')" align="center" prop="name" />
        <el-table-column :label="$t('MetaModel.FieldCode')" align="center" prop="code" />
        <el-table-column :label="$t('MetaModel.FieldControlType')" align="center" prop="controlType">
          <template #default="scope">
            {{ formatControlType(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('MetaModel.FieldMappingName')" align="center" prop="fieldName" />
        <el-table-column :label="$t('Common.Sort')" align="center" prop="sortFlag" />
        <el-table-column :label="$t('Common.Operation')" align="center" width="300">
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
        v-show="fieldTotal>0"
        :total="fieldTotal"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="loadXModelFieldList" />
    </el-row>
    <!-- 添加/编辑弹窗 -->
    <el-dialog 
      :title="title"
      v-model="open"
      width="600px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="el-form-dialog">
        <el-form-item :label="$t('MetaModel.FieldName')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('MetaModel.FieldCode')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item v-if="model.isDefaultTable" :label="$t('MetaModel.FieldType')" prop="fieldType">
          <el-select v-model="form.fieldType">
            <el-option
              v-for="dict in MetaFieldType"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-else :label="$t('MetaModel.FieldMappingName')" prop="fieldName">
          <el-select v-model="form.fieldName">
            <el-option
              v-for="fieldName in tableFields"
              :key="fieldName"
              :label="fieldName"
              :value="fieldName"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('MetaModel.FieldControlType')" prop="controlType">
          <el-select v-model="form.controlType">
            <el-option
              v-for="control in controlOptions"
              :key="control.value"
              :label="control.label"
              :value="control.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('MetaModel.FieldValidation')">
          <el-checkbox-group v-model="validation.types">
            <el-checkbox label="NotEmpty">{{ $t('MetaModel.Validation.NotEmpty') }}</el-checkbox>
            <el-checkbox label="Number">{{ $t('MetaModel.Validation.Number') }}</el-checkbox>
            <el-checkbox label="Int">{{ $t('MetaModel.Validation.Int') }}</el-checkbox>
            <el-checkbox label="Date">{{ $t('MetaModel.Validation.Date') }}</el-checkbox>
            <el-checkbox label="Time">{{ $t('MetaModel.Validation.Time') }}</el-checkbox>
            <el-checkbox label="DateTime">{{ $t('MetaModel.Validation.DateTime') }}</el-checkbox>
            <el-checkbox label="Email">{{ $t('MetaModel.Validation.Email') }}</el-checkbox>
            <el-checkbox label="PhoneNumber">{{ $t('MetaModel.Validation.PhoneNumber') }}</el-checkbox>
          </el-checkbox-group>
            <el-checkbox v-model="validation.regex">{{ $t('MetaModel.Validation.Regex') }}</el-checkbox>
          <el-input v-model="validation.regexValue" :placeholder="$t('MetaModel.Placeholder.Regex')" />
        </el-form-item>
        <el-form-item :label="$t('MetaModel.FieldDefaultValue')" prop="defaultValue">
          <el-input v-model="form.defaultValue" />
        </el-form-item>
        <el-form-item v-if="showOptions" :label="$t('MetaModel.FieldOptions')" prop="options">
          <div style="width: 100%">
            <el-radio-group v-model="form.options.type">
              <el-radio label="text">{{ $t('MetaModel.FieldOptionsInput') }}</el-radio>
              <el-radio label="dict">{{ $t('MetaModel.FieldOptionsDict') }}</el-radio>
            </el-radio-group>
          </div>
          <div style="width: 100%">
            <el-input v-if="form.options.type==='text'" type="textarea" v-model="form.options.value" :placeholder="$t('MetaModel.Placeholder.FieldOptionsInput')" />
            <el-select v-if="form.options.type==='dict'" v-model="form.options.value">
              <el-option
                v-for="item in dictTypeOptions"
                :key="item.dictId"
                :label="item.dictName"
                :value="item.dictType"
              />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item :label="$t('Common.Sort')" prop="sortFlag">
          <el-input-number v-model="form.sortFlag" />
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
<script setup name="MetaModelField">
import { isBlank, codeValidator } from '@/utils/validate';
import { optionselect as getDictOptions } from "@/api/system/dict/type";
import { getControlOptions, getModel, addModelField, editModelField, deleteModelField, listModelField, listModelTableFields } from "@/api/meta/model";

const { proxy } = getCurrentInstance();
const { MetaFieldType } = proxy.useDict("MetaFieldType");

const loading = ref(false);
const title = ref("");
const open = ref(false);
const fieldList = ref([]);
const fieldTotal = ref(0);
const selectedRows = ref([]);
const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 20,
    modelId: proxy.$route.query.modelId,
    query: undefined
  },
  form: {}
})

const { queryParams, form } = toRefs(objects);

const modelId = ref(proxy.$route.query.modelId);
const model = ref({});
const dictTypeOptions = ref([]);
const tableFields = ref([]);
const controlOptions = ref([]);
const validation = ref({ types: [], regex: false, regexValue: "" });
const rules = {
  name: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 50, message: proxy.$t('Common.RuleTips.MaxLength', [ 50 ]), trigger: [ "change", "blur" ] }
  ],
  code: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 50, message: proxy.$t('Common.RuleTips.MaxLength', [ 50 ]), trigger: [ "change", "blur" ] },
    { validator: codeValidator, trigger: "change" }
  ],
  controlType: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 20, message: proxy.$t('Common.RuleTips.MaxLength', [ 20 ]), trigger: [ "change", "blur" ] },
  ],
  fieldType: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { validator: (rule, value, callback) => {
          if (model.value.isDefaultTable && (!value || value == null || value == '')) {
            return callback(new Error(proxy.$t('Common.RuleTips.NotEmpty')));
          }
          callback();
        }, trigger: "blur" }
  ],
  fieldName: [
    { required: true, validator: (rule, value, callback) => {
          if (!model.value.isDefaultTable) {
            if (isBlank(value)) {
              return callback(new Error(proxy.$t('Common.RuleTips.NotEmpty')));
            }
            for(let i = 0; i < fieldList.value.length; i++) {
              if (fieldList.value[i].fieldName == value && fieldList.value[i].fieldId != form.value.fieldId) {
                return callback(new Error(proxy.$t('MetaModel.RuleTips.FieldMappingUsed')));
              }
            }
          }
          callback();
        }, trigger: "blur" }
  ],
  controlType: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
  ],
  defaultValue: [
    { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: [ "change", "blur" ] }
  ],
  sortFlag: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
  ],
  remark: [
    { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [ 500 ]), trigger: [ "change", "blur" ] }
  ]
}

const showOptions = computed(() => {
  return form.value.controlType==='select' || form.value.controlType==='radio' || form.value.controlType==='checkbox'
})

watch(showOptions, (newVal) => {
  if (!newVal) {
    form.value.options = {}
  }
})

onMounted(() => {
  if (!modelId.value) {
    proxy.$modal.msgWarning("The modelId is undefined.");
    return;
  }
  loadControlOptions();
  loadXModel();
  loadXModelFieldList();
  loadDicTypeOptions();
})


const loadDicTypeOptions = () => {
      getDictOptions().then(response => {
    dictTypeOptions.value = response.data;
  });
}
const loadControlOptions = () => {
      getControlOptions().then(response => {
    controlOptions.value = response.data;
  });
}
const formatControlType = (row) => {
  let control = controlOptions.value.find(item => item.id == row.controlType)
  return control ? control.name : row.controlType
}
const loadXModel = () => {
  getModel(modelId.value).then(response => {
    model.value = response.data
    if (!response.data.isDefaultTable) {
      loadXmodelUsableFields();
    }
  });
}
const loadXModelFieldList = () => {
  loading.value = true;
  listModelField(queryParams.value).then(response => {
    fieldList.value = response.data.rows.map(item => {
          if (item.options == null) {
            item.options = { type: "text" };
          }
          item.sortFlag = parseInt(item.sortFlag);
          return item;
        });
    fieldTotal.value = parseInt(response.data.total);
    loading.value = false;
  });
}
const loadXmodelUsableFields = () => {
  const params = { modelId: modelId.value };
  listModelTableFields(params).then(response => {
    tableFields.value = response.data.rows;
  });
}
const handleSelectionChange = (selection) => {
  selectedRows.value = selection.map(item => item);
}
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  loadXModelFieldList();
}
const resetQuery = () => {
  proxy.$refs.queryFormRef.resetFields();
  handleQuery();
}
const closeDialog = (loadList) => {
  open.value = false;
  form.value = { options:{ type: "text" } };
  if (loadList) loadXModelFieldList();
}
const handleAdd = () => {
  form.value = { 
    options:{ type: "text" },
    sortFlag: 1 
  };
  validation.value = {
    types: [],
    regex: false,
    regexValue: ""
  }
  title.value = proxy.$t('MetaModel.AddFieldTitle');
  open.value = true;
}
const handleEdit = (row) => {
  form.value = row;
  parseValidations();
  title.value = proxy.$t('MetaModel.EditFieldTitle');
  open.value = true;
}
const handleAddSave = () => {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      dealFormValidations();
      if (form.value.fieldId) {
        editModelField(form.value).then(response => {
          proxy.$modal.msgSuccess(response.msg);
          closeDialog(true);
        }); 
      } else {
        form.value.modelId = modelId.value;
        addModelField(form.value).then(response => {
          proxy.$modal.msgSuccess(response.msg);
          closeDialog(true);
        }); 
      }
    }
  });
}
const parseValidations = () => {
  validation.value = {
    types: [],
    regex: false,
    regexValue: ""
  }
  if (form.value.validations && form.value.validations.length > 0) {
    form.value.validations.forEach(valid => {
      if (valid.type == "Regex") {
        validation.value.regex = true;
        validation.value.regexValue = valid.regex;
      } else {
        if (validation.value.types.indexOf(valid.type) < 0) {
          validation.value.types.push(valid.type)
        }
      }
    })
  }
}
const dealFormValidations = () => {
  form.value.validations = []
  validation.value.types.forEach(t => {
    form.value.validations.push({ type: t })
  })
  if (validation.value.regex && validation.value.regexValue.length > 0) {
    form.value.validations.push({ type: "Regex", regex: validation.value.regexValue })
  }
}
const handleDelete = (row) => {
  doDelete([ row ]);
}
const handleBatchDelete = () => {
  if (selectedRows.value.length > 0) {
    doDelete(selectedRows.value);
  }
}
const doDelete = (fields) => {
  const fieldIds = fields.map(f => f.fieldId);
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function () {
    return deleteModelField(fieldIds);
  }).then(() => {
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
    loadXModelFieldList();
  }).catch(function () { });
}
const handleClose = () => {
  if (model.value.ownerType == 'CmsCustomForm') {
    const obj = { path: "/operations/customform" };
    proxy.$tab.closeOpenPage(obj);
  } else if (model.value.ownerType == 'CmsExtend') {
    const obj = { path: "/configs/exmodel" };
    proxy.$tab.closeOpenPage(obj);
  } else {
    proxy.$tab.closePage();
  }
}
</script>