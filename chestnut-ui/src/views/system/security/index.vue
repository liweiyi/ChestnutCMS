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
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <right-toolbar :search="false" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="configId" width="100" />
      <el-table-column :label="$t('System.Security.PasswordLength')" align="center" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.passwordLenMin }} - {{ scope.row.passwordLenMax }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.Security.PasswordRule')" align="center" prop="passwordRule" width="180">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.SecurityPasswordRule" :value="scope.row.passwordRule"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.Security.PasswordExpireSeconds')" align="center" prop="passwordExpireSeconds" width="180" />
      <el-table-column :label="$t('System.Security.PasswordRetryLimit')" align="center" prop="passwordRetryLimit" width="180" />
      <el-table-column :label="$t('System.Security.Status')" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="small"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            size="small"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >{{ $t('Common.Delete') }}</el-button>
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
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" v-loading="loading" :rules="rules" label-width="200px">
        <el-card shadow="hover">
          <div slot="header" class="clearfix">
            <span>{{ $t('System.Security.PasswordConfig') }}</span>
          </div>
          <el-form-item :label="$t('System.Security.PasswordMinLength')" prop="passwordLenMin">
            <el-input-number v-model="form.passwordLenMin" controls-position="right" :min="6" :max="16"></el-input-number>
          </el-form-item>
          <el-form-item :label="$t('System.Security.PasswordMaxLength')" prop="passwordLenMax">
            <el-input-number v-model="form.passwordLenMax" controls-position="right" :min="16" :max="30"></el-input-number>
          </el-form-item>
          <el-form-item :label="$t('System.Security.PasswordRule')" prop="passwordRule">
            <el-select v-model="form.passwordRule">
              <el-option
                v-for="item in dict.type.SecurityPasswordRule"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('System.Security.PasswordSensitive')" prop="passwordSensitive">
            <el-checkbox-group v-model="form.passwordSensitive">
                  <el-checkbox
                    v-for="item in dict.type.SecurityPasswordSensitive"
                    :key="item.value"
                    :label="item.value"
                  >{{item.label}}</el-checkbox>
                </el-checkbox-group>
          </el-form-item>
          <el-form-item :label="$t('System.Security.WeakPasswords')" prop="weakPasswords">
            <el-input type="textarea" v-model="form.weakPasswords" :placeholder="$t('System.Security.WeakPasswordsPlaceholder')" :rows="5"></el-input>
          </el-form-item>
          <el-form-item 
            :label="$t('System.Security.PasswordExpireSeconds')"
            prop="passwordExpireSeconds">
            <el-input-number v-model="form.passwordExpireSeconds" controls-position="right" :min="0" :max="8640000"></el-input-number>
            <i class="el-icon-info tips">{{ $t('System.Security.PasswordExpireSecondsTip') }}</i>
          </el-form-item>
          <el-form-item :label="$t('System.Security.ForceModifyPwdAfterAdd')" prop="forceModifyPwdAfterAdd">
            <el-switch
              v-model="form.forceModifyPwdAfterAdd"
              active-value="Y"
              inactive-value="N">
            </el-switch>
            <i class="el-icon-info tips">{{ $t('System.Security.ForceModifyPwdAfterAddTip') }}</i>
          </el-form-item>
          <el-form-item :label="$t('System.Security.ForceModifyPwdAfterReset')" prop="forceModifyPwdAfterReset">
            <el-switch
              v-model="form.forceModifyPwdAfterReset"
              active-value="Y"
              inactive-value="N">
            </el-switch>
            <i class="el-icon-info tips">{{ $t('System.Security.ForceModifyPwdAfterResetTip') }}</i>
          </el-form-item>
        </el-card>
        <el-card shadow="hover">
          <div slot="header" class="clearfix">
            <span>{{ $t('System.Security.LoginConfig') }}</span>
          </div>
          <el-form-item :label="$t('System.Security.PasswordRetryLimit')" prop="passwordRetryLimit">
            <el-input-number v-model="form.passwordRetryLimit" controls-position="right" :min="0"></el-input-number>
            <i class="el-icon-info tips">{{ $t('System.Security.PasswordRetryLimitTip') }}</i>
          </el-form-item>
          <el-form-item 
            :label="$t('System.Security.PasswordRetryStrategy')"
            prop="passwordRetryStrategy">
            <el-select v-model="form.passwordRetryStrategy" :disabled="form.passwordRetryLimit===0">
              <el-option
                v-for="item in dict.type.SecurityPasswordRetryStrategy"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item 
            v-if="form.passwordRetryStrategy==='LOCK'"
            :label="$t('System.Security.PasswordRetryLockSeconds')"
            prop="passwordRetryLockSeconds">
            <el-input-number v-model="form.passwordRetryLockSeconds" controls-position="right" :min="0" :max="31536000"></el-input-number>
            <i class="el-icon-info tips">{{ $t('System.Security.PasswordRetryLockSecondsTip') }}</i>
          </el-form-item>
        </el-card>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { listSecurityConfigs, getSecurityConfig, addSecurityConfig, saveSecurityConfig, deleteSecurityConfig, changeConfigStatus } from "@/api/system/security";

export default {
  name: "SysSecurityIndex",
  dicts: [ "EnableOrDisable", "SecurityPasswordRule", "SecurityPasswordSensitive", "SecurityPasswordRetryStrategy" ],
  data () {
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
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      form: {},
      // 表单校验
      rules: {
        passwordLenMin: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList () {
      this.loading = true;
      listSecurityConfigs().then(response => {
        this.configList = response.data.rows;
        this.total = parseInt(response.data.total);
        // if (!this.form.passwordSensitive) {
        //   this.$set(this.form, 'passwordSensitive', []);
        // }
        this.loading = false;
      });
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        status: '1',
        passwordLenMin: 0,
        passwordLenMax: 0,
        passwordRule: '0',
        passwordSensitive: [],
        passwordExpireSeconds: 0,
        forceModifyPwdAfterAdd: 'N',
        forceModifyPwdAfterReset: 'N',
        passwordRetryLimit: 0,
        passwordRetryStrategy: '0',
        passwordRetryLockSeconds: 0,
      };
      this.resetForm("form");
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = this.$t('System.Security.AddTitle');
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.configId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      this.reset();
      const configId = row.configId || this.ids
      getSecurityConfig(configId).then(response => {
        this.form = response.data;
        this.open = true;
      this.title = this.$t('System.Security.EditTitle');
      });
    },
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.configId != undefined) {
            saveSecurityConfig(this.form).then(res => {
              this.$modal.msgSuccess(this.$t("Common.Success"));
              this.open = false;
              this.getList();
            });
          } else {
            addSecurityConfig(this.form).then(res => {
              this.$modal.msgSuccess(this.$t("Common.AddSuccess"));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const configIds = row.configId ? [ row.configId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
          return deleteSecurityConfig(configIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess(this.$t("Common.DeleteSuccess"));
        }).catch(() => {});
    },
    handleStatusChange (row) {
      const configId = row.configId;
      changeConfigStatus(configId).then(res => {
        this.$modal.msgSuccess(this.$t("Common.Success"));
        this.getList();
      });
    }
  }
};
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