<template>
  <div class="app-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="12">
        <el-button 
          plain
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
        <el-button 
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleBatchDelete">{{ $t("Common.Delete") }}</el-button>
        <el-button 
          plain
          type="warning"
          icon="el-icon-close"
          size="mini"
          @click="handleClose">{{ $t("Common.Close") }}</el-button>
      </el-col>
      <el-col :span="12">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="mini"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input :placeholder="$t('MetaModel.Placeholder.FieldQuery')" v-model="queryParams.query"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="el-icon-search"
                @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button 
                icon="el-icon-refresh"
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
          <template slot-scope="scope">
            {{ formatControlType(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('MetaModel.FieldMappingName')" align="center" prop="fieldName" />
        <el-table-column :label="$t('Common.Sort')" align="center" prop="sortFlag" />
        <el-table-column :label="$t('Common.Operation')" align="center" width="300" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button
              size="small"
              type="text"
              icon="el-icon-edit"
              @click="handleEdit(scope.row)">{{ $t("Common.Edit") }}</el-button>
            <el-button 
              size="small"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination 
        v-show="fieldTotal>0"
        :total="fieldTotal"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="loadXModelFieldList" />
    </el-row>
    <!-- 添加/编辑弹窗 -->
    <el-dialog 
      :title="title"
      :visible.sync="open"
      width="600px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="el-form-dialog">
        <el-form-item :label="$t('MetaModel.FieldName')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('MetaModel.FieldCode')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item v-if="model.isDefaultTable" :label="$t('MetaModel.FieldType')" prop="fieldType">
          <el-select v-model="form.fieldType">
            <el-option
              v-for="dict in dict.type.MetaFieldType"
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
              :key="control.id"
              :label="control.name"
              :value="control.id"
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
          <div>
            <el-radio-group v-model="form.options.type">
              <el-radio label="text">{{ $t('MetaModel.FieldOptionsInput') }}</el-radio>
              <el-radio label="dict">{{ $t('MetaModel.FieldOptionsDict') }}</el-radio>
            </el-radio-group>
          </div>
          <div>
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
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" @click="handleAddSave">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="closeDialog(false)">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { optionselect as getDictOptions } from "@/api/system/dict/type";
import { getControlOptions, getModel, addModelField, editModelField, deleteModelField, listModelField, listModelTableFields } from "@/api/meta/model";

export default {
  name: "MetaModelField",
  dicts: [ 'MetaFieldType' ],
  data () {
    return {
      // 遮罩层
      loading: false,
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      fieldList: undefined,
      fieldTotal: 0,
      selectedRows: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      queryParams: {
        pageNum: 1,
        pageSize: 20,
        modelId: this.$route.query.modelId,
        query: undefined
      },
      modelId: this.$route.query.modelId,
      model: {},
      dictTypeOptions: [],
      tableFields: [],
      controlOptions: [],
      // 表单参数
      form: {
        options:{ type: "text" }
      },
      validation: {
        types: [],
        regex: false,
        regexValue: ""
      },
      // 表单校验
      rules: {
        name: [
          { required: true, message: this.$t('MetaModel.RuleTips.FieldName'), trigger: "blur" }
        ],
        code: [
          { required: true, pattern: "^[A-Za-z0-9_]+$", message: this.$t('MetaModel.RuleTips.FieldCode'), trigger: "blur" }
        ],
        controlType: [
          { required: true, message: this.$t('MetaModel.RuleTips.FieldControlType'), trigger: "blur" }
        ],
        fieldType: [
          { required: true, validator: (rule, value, callback) => {
                if (this.model.isDefaultTable && (!value || value == null || value == '')) {
                  return callback(new Error(this.$t('MetaModel.RuleTips.FieldType')));
                }
                callback();
              }, trigger: "blur" }
        ],
        fieldName: [
          { required: true, validator: (rule, value, callback) => {
                if (!this.model.isDefaultTable) {
                  if (!value || value == null || value == '') {
                    return callback(new Error(this.$t('MetaModel.RuleTips.FieldMappingName')));
                  }
                  for(let i = 0; i < this.fieldList.length; i++) {
                    if (this.fieldList[i].fieldName == value && this.fieldList[i].fieldId != this.form.fieldId) {
                      return callback(new Error(this.$t('MetaModel.RuleTips.FieldMappingUsed')));
                    }
                  }
                }
                callback();
              }, trigger: "blur" }
        ]
      }
    };
  },
  computed: {
    showOptions() {
      return this.form.controlType==='select' || this.form.controlType==='radio' || this.form.controlType==='checkbox'
    }
  },
  watch: {
    showOptions() {
      if (!this.showOptions) {
        this.form.options = {}
      }
    }
  },
  created () {
    if (!this.modelId) {
      this.$modal.msgWarning("The modelId is undefined.");
      return;
    }
    this.loadControlOptions();
    this.loadXModel();
    this.loadXModelFieldList();
    this.loadDicTypeOptions();
  },
  methods: {
    loadDicTypeOptions() {
      getDictOptions().then(response => {
        this.dictTypeOptions = response.data;
      });
    },
    loadControlOptions() {
      getControlOptions().then(response => {
        this.controlOptions = response.data;
      })
    },
    formatControlType(row) {
      let control = this.controlOptions.find(item => item.id == row.controlType)
      return control ? control.name : row.controlType
    },
    loadXModel() {
      getModel(this.modelId).then(response => {
        this.model = response.data
        if (!response.data.isDefaultTable) {
          this.loadXmodelUsableFields();
        }
      })
    },
    loadXModelFieldList () {
      this.loading = true;
      listModelField(this.queryParams).then(response => {
        this.fieldList = response.data.rows.map(item => {
          if (item.options == null) {
            item.options = { type: "text" };
          }
          return item;
        });
        this.fieldTotal = parseInt(response.data.total);
        this.loading = false;
      });
    },
    loadXmodelUsableFields () {
      const params = { modelId: this.modelId };
      listModelTableFields(params).then(response => {
        this.tableFields = response.data.rows;
      });
    },
    handleSelectionChange (selection) {
      this.single = selection.length != 1
      this.multiple = !selection.length
      this.selectedRows = selection;
    },
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.loadXModelFieldList();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    closeDialog (loadList) {
      this.open = false;
      this.form = { options:{ type: "text" } };
      if (loadList) this.loadXModelFieldList();
    },
    handleAdd () {
      this.form = { options:{ type: "text" } };
      this.validation = {
        types: [],
        regex: false,
        regexValue: ""
      }
      this.title = this.$t('MetaModel.AddFieldTitle');
      this.open = true;
    },
    handleEdit (row) {
      this.form = row;
      this.parseValidations()
      this.title = this.$t('MetaModel.EditFieldTitle');
      this.open = true;
    },
    handleAddSave () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.dealFormValidations();
          if (this.form.fieldId) {
            editModelField(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.closeDialog(true);
            }); 
          } else {
            this.form.modelId = this.modelId;
            addModelField(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.closeDialog(true);
            }); 
          }
        }
      });
    },
    parseValidations() {
      this.validation = {
        types: [],
        regex: false,
        regexValue: ""
      }
      if (this.form.validations && this.form.validations.length > 0) {
        this.form.validations.forEach(valid => {
          if (valid.type == "Regex") {
            this.validation.regex = true;
            this.validation.regexValue = valid.regex;
          } else {
            if (this.validation.types.indexOf(valid.type) < 0) {
              this.validation.types.push(valid.type)
            }
          }
        })
      }
    },
    dealFormValidations() {
      this.form.validations = []
      this.validation.types.forEach(t => {
        this.form.validations.push({ type: t })
      })
      if (this.validation.regex && this.validation.regexValue.length > 0) {
        this.form.validations.push({ type: "Regex", regex: this.validation.regexValue })
      }
    },
    handleDelete (row) {
      this.doDelete([ row ]);
    },
    handleBatchDelete () {
      if (this.selectedRows.length > 0) {
        this.doDelete(this.selectedRows);
      }
    },
    doDelete (fields) {
      const fieldIds = fields.map(f => f.fieldId);
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function () {
        return deleteModelField(fieldIds);
      }).then(() => {
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
        this.loadXModelFieldList();
      }).catch(function () { });
    },
    handleClose() {
      if (this.model.ownerType == 'CmsCustomForm') {
        const obj = { path: "/operations/customform" };
        this.$tab.closeOpenPage(obj);
      } else if (this.model.ownerType == 'CmsExtend') {
        const obj = { path: "/configs/exmodel" };
        this.$tab.closeOpenPage(obj);
      } else {
        this.$tab.closePage();
      }
    }
  }
};
</script>