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
          v-hasPermi="['system:config:add']"
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
          v-hasPermi="['system:config:edit']"
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
          v-hasPermi="['system:config:remove']"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="['system:config:export']"
        >{{ $t('Common.Export') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-refresh"
          size="small"
          @click="handleRefreshCache"
          v-hasPermi="['system:config:remove']"
        >{{ $t('Common.RefreshCache') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true">
        <el-form-item :label="$t('System.Config.ConfigName')" prop="configName">
          <el-input
            v-model="queryParams.configName"
            :placeholder="$t('System.Config.Placeholder.ConfigName')"
            clearable
            style="width: 240px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item :label="$t('System.Config.ConfigKey')" prop="configKey">
          <el-input
            v-model="queryParams.configKey"
            :placeholder="$t('System.Config.Placeholder.ConfigKey')"
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

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.Config.ConfigId')" align="center" prop="configId" />
      <el-table-column :label="$t('System.Config.ConfigName')" align="center" prop="configName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.Config.ConfigKey')" align="center" prop="configKey" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.Config.ConfigValue')" align="center" prop="configValue" />
      <el-table-column :label="$t('System.Config.ConfigType')" align="center" prop="fixed">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.YesOrNo" :value="scope.row.fixed"/>
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
              v-hasPermi="['system:config:edit']"
            >{{ $t('Common.Edit') }}</el-button>
          </span>
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['system:config:remove']"
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

    <!-- 添加或修改参数配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('System.Config.ConfigName')" prop="configName">
          <el-input v-model="form.configName" :placeholder="$t('System.Config.Placeholder.ConfigName')">
            <i18n-editor slot="append" v-if="form.configKey&&form.configKey.length>0" :languageKey="'CONFIG.' + form.configKey"></i18n-editor>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('System.Config.ConfigKey')" prop="configKey">
          <el-input v-model="form.configKey" :placeholder="$t('System.Config.Placeholder.ConfigKey')" />
        </el-form-item>
        <el-form-item :label="$t('System.Config.ConfigValue')" prop="configValue">
          <el-input v-model="form.configValue" :placeholder="$t('System.Config.Placeholder.ConfigValue')" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listConfig, getConfig, delConfig, addConfig, updateConfig, refreshCache } from "@/api/system/config";
import I18nEditor from '@/views/components/I18nFieldEditor';

export default {
  name: "SystemConfigIndex",
  components: {
    I18nEditor,
  },
  dicts: ['YesOrNo'],
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
      // 参数表格数据
      configList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        configName: undefined,
        configKey: undefined,
        configType: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        configName: [
          { required: true, message: this.$t('System.Config.RuleTips.ConfigName'), trigger: "blur" }
        ],
        configKey: [
          { required: true, message: this.$t('System.Config.RuleTips.ConfigKey'), trigger: "blur" }
        ],
        configValue: [
          { required: true, message: this.$t('System.Config.RuleTips.ConfigValue'), trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询参数列表 */
    getList() {
      this.loading = true;
      listConfig(this.queryParams).then(response => {
          this.configList = response.data.rows;
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
        configId: undefined,
        configName: undefined,
        configKey: undefined,
        configValue: undefined,
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
      this.title = this.$t("System.Config.Dialog.Add");
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.configId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const configId = row.configId || this.ids
      getConfig(configId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = this.$t("System.Config.Dialog.Edit");
      });
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.configId != undefined) {
            updateConfig(this.form).then(response => {
              this.$modal.msgSuccess(this.$t("Common.Success"));
              this.open = false;
              this.getList();
            });
          } else {
            addConfig(this.form).then(response => {
              this.$modal.msgSuccess(this.$t("Common.AddSuccess"));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const configIds = row.configId ? [ row.configId ] : this.ids;
      this.$modal.confirm(this.$t('System.Config.ConfirmDelete', [ configIds ])).then(function() {
          return delConfig(configIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess(this.$t("Common.DeleteSuccess"));
        }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.exportExcel('system/config/list', {
        ...this.queryParams
      }, `config_${this.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
    },
    /** 刷新缓存按钮操作 */
    handleRefreshCache() {
      refreshCache().then(() => {
        this.$modal.msgSuccess(this.$t("Common.Success"));
      });
    }
  }
};
</script>
