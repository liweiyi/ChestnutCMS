<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="small"
          @click="handleAdd"
          v-hasPermi="['system:dict:add']"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="small"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:dict:edit']"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:dict:remove']"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="['system:dict:export']"
        >{{ $t('Common.Export') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-refresh"
          size="small"
          @click="handleRefreshCache"
          v-hasPermi="['system:dict:remove']"
        >{{ $t('Common.RefreshCache') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row style="text-align:right" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true">
        <el-form-item :label="$t('System.Dict.DictName')" prop="dictName">
          <el-input
            v-model="queryParams.dictName"
            :placeholder="$t('System.Dict.Placeholder.DictName')"
            clearable
            style="width: 240px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item :label="$t('System.Dict.DictType')" prop="dictType">
          <el-input
            v-model="queryParams.dictType"
            :placeholder="$t('System.Dict.Placeholder.DictType')"
            clearable
            style="width: 240px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table v-loading="loading" :data="typeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.Dict.DictId')" align="center" prop="dictId" />
      <el-table-column :label="$t('System.Dict.DictName')" align="center" prop="dictName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.Dict.DictType')" align="center" :show-overflow-tooltip="true">
        <template slot-scope="scope">
          <router-link :to="'/system/dict-data/index/' + scope.row.dictId" class="link-type">
            <span>{{ scope.row.dictType }}</span>
          </router-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.Dict.SystemFixed')" align="center" prop="fixed">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.YesOrNo" :value="scope.row.fixed"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.Dict.Status')" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.EnableOrDisable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Remark')" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['system:dict:edit']"
            >{{ $t('Common.Edit') }}</el-button>
          </span>
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['system:dict:remove']"
            >{{ $t('Common.Delete') }}</el-button>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('System.Dict.DictName')" prop="dictName">
          <el-input v-model="form.dictName" :placeholder="$t('System.Dict.Placeholder.DictName')">
            <i18n-editor slot="append" v-if="form.dictType&&form.dictType.length>0" :languageKey="'DICT.' + form.dictType"></i18n-editor>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('System.Dict.DictType')" prop="dictType">
          <el-input v-model="form.dictType" :placeholder="$t('System.Dict.Placeholder.DictType')" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('Common.Confirm') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listType, getType, delType, addType, updateType, refreshCache } from "@/api/system/dict/type";
import I18nEditor from '@/views/components/I18nFieldEditor';

export default {
  name: "SystemDictIndex",
  components: {
    I18nEditor,
  },
  dicts: [ 'EnableOrDisable', 'YesOrNo' ],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 字典表格数据
      typeList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        dictName: undefined,
        dictType: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        dictName: [
          { required: true, message: this.$t('System.Dict.RuleTips.DictName'), trigger: "blur" }
        ],
        dictType: [
          { required: true, message: this.$t('System.Dict.RuleTips.DictType'), trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询字典类型列表 */
    getList() {
      this.loading = true;
      listType(this.queryParams).then(response => {
          this.typeList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      );
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        dictId: undefined,
        dictName: undefined,
        dictType: undefined,
        status: "0",
        remark: undefined
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = this.$t('System.Dict.Dialog.Add');
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.dictId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const dictId = row.dictId || this.ids
      getType(dictId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = this.$t('System.Dict.Dialog.Edit');
      });
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.dictId != undefined) {
            updateType(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.Success'));
              this.open = false;
              this.getList();
            });
          } else {
            addType(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.AddSuccess'));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const dictIds = row.dictId ? [ row.dictId ] : this.ids;
      this.$modal.confirm(this.$t('System.Dict.ConfirmDelete', [ dictIds ])).then(function() {
        return delType(dictIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.exportExcel('system/dict/type/list', {
        ...this.queryParams
      }, `dict_type_${this.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
    },
    /** 刷新缓存按钮操作 */
    handleRefreshCache() {
      refreshCache().then(() => {
        this.$modal.msgSuccess(this.$t('Common.Success'));
        this.$store.dispatch('dict/cleanDict');
      });
    }
  }
};
</script>