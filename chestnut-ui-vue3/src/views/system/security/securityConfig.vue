<template>
  <div class="security-config-container">
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
      <el-table-column label="ID" align="center" prop="configId" width="100" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.Security.PasswordLength')" align="center">
        <template #default="scope">
          <span>{{ scope.row.passwordLenMin }} - {{ scope.row.passwordLenMax }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.Security.PasswordRule')" align="center" prop="passwordRule">
        <template #default="scope">
          <dict-tag :options="SecurityPasswordRule" :value="scope.row.passwordRule"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.Security.PasswordExpireSeconds')" align="center" prop="passwordExpireSeconds" />
      <el-table-column :label="$t('System.Security.PasswordRetryLimit')" align="center" prop="passwordRetryLimit" />
      <el-table-column :label="$t('System.Security.Status')" align="center" prop="status" width="90">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
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

     <!-- 添加或修改参数配置对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" v-loading="loading" :rules="rules" label-width="200px">
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>{{ $t('System.Security.PasswordConfig') }}</span>
            </div>
          </template>
          <el-form-item :label="$t('System.Security.PasswordMinLength')" prop="passwordLenMin">
            <el-input-number v-model="form.passwordLenMin" controls-position="right" :min="6" :max="16"></el-input-number>
          </el-form-item>
          <el-form-item :label="$t('System.Security.PasswordMaxLength')" prop="passwordLenMax">
            <el-input-number v-model="form.passwordLenMax" controls-position="right" :min="16" :max="30"></el-input-number>
          </el-form-item>
          <el-form-item :label="$t('System.Security.PasswordRule')" prop="passwordRule">
            <el-select v-model="form.passwordRule">
              <el-option
                v-for="item in SecurityPasswordRule"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('System.Security.PasswordSensitive')" prop="passwordSensitive">
            <el-checkbox-group v-model="form.passwordSensitive">
                  <el-checkbox
                    v-for="item in SecurityPasswordSensitive"
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
            <el-icon class="tips"><InfoFilled /></el-icon>
            <span class="tips">{{ $t('System.Security.PasswordExpireSecondsTip') }}</span>
          </el-form-item>
          <el-form-item :label="$t('System.Security.ForceModifyPwdAfterAdd')" prop="forceModifyPwdAfterAdd">
            <el-switch
              v-model="form.forceModifyPwdAfterAdd"
              active-value="Y"
              inactive-value="N">
            </el-switch>
            <el-icon class="tips"><InfoFilled /></el-icon>
            <span class="tips">{{ $t('System.Security.ForceModifyPwdAfterAddTip') }}</span>
          </el-form-item>
          <el-form-item :label="$t('System.Security.ForceModifyPwdAfterReset')" prop="forceModifyPwdAfterReset">
            <el-switch
              v-model="form.forceModifyPwdAfterReset"
              active-value="Y"
              inactive-value="N">
            </el-switch>
            <el-icon class="tips"><InfoFilled /></el-icon>
            <span class="tips">{{ $t('System.Security.ForceModifyPwdAfterResetTip') }}</span>
          </el-form-item>
        </el-card>
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>{{ $t('System.Security.LoginConfigCard') }}</span>
            </div>
          </template>
          <el-form-item :label="$t('System.Security.PasswordRetryLimit')" prop="passwordRetryLimit">
            <el-input-number v-model="form.passwordRetryLimit" controls-position="right" :min="0"></el-input-number>
            <el-icon class="tips"><InfoFilled /></el-icon>
            <span class="tips">{{ $t('System.Security.PasswordRetryLimitTip') }}</span>
          </el-form-item>
          <el-form-item 
            :label="$t('System.Security.PasswordRetryStrategy')"
            prop="passwordRetryStrategy">
            <el-select v-model="form.passwordRetryStrategy" :disabled="form.passwordRetryLimit===0">
              <el-option
                v-for="item in SecurityPasswordRetryStrategy"
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
            <el-icon class="tips"><InfoFilled /></el-icon>
            <span class="tips">{{ $t('System.Security.PasswordRetryLockSecondsTip') }}</span>
          </el-form-item>
          <el-form-item :label="$t('System.Security.CaptchaEnable')" prop="captchaEnable">
            <el-switch
              v-model="form.captchaEnable"
              active-value="Y"
              inactive-value="N">
            </el-switch>
          </el-form-item>
          <el-form-item :label="$t('System.Security.CaptchaType')" prop="captchaType">
            <el-select v-model="form.captchaType">
              <el-option
                v-for="item in captchaTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('System.Security.CaptchaExpireSeconds')" prop="captchaExpires">
            <el-input-number v-model="form.captchaExpires" controls-position="right" :min="1" :max="3600"></el-input-number>
          </el-form-item>
          <el-form-item :label="$t('System.Security.CaptchaRetryDuration')" prop="captchaDuration">
            <el-input-number v-model="form.captchaDuration" controls-position="right" :min="0"></el-input-number>
          </el-form-item>
          <el-form-item :label="$t('System.Security.ThirdLogin')" prop="loginTypeConfigIds">
            <el-checkbox-group v-model="form.loginTypeConfigIds">
              <el-checkbox
                v-for="item in loginConfigOptions"
                :key="item.value"
                :label="item.value"
                :value="item.value">
                {{ item.label }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-card>
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
<script setup name="SysSecurityIndex">
import { listSecurityConfigs, getSecurityConfig, addSecurityConfig, saveSecurityConfig, deleteSecurityConfig, changeConfigStatus } from "@/api/system/security";
import { getCaptchaTypeOptions } from "@/api/system/captcha";
import { getLoginConfigs } from "@/api/system/login";

const { proxy } = getCurrentInstance()
const { SecurityPasswordRule, SecurityPasswordSensitive, SecurityPasswordRetryStrategy } = proxy.useDict('SecurityPasswordRule', 'SecurityPasswordSensitive', 'SecurityPasswordRetryStrategy')

// 响应式数据
const formRef = ref()
const loading = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const configList = ref([])
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
})
const title = ref("")
const open = ref(false)
const form = reactive({})

// 表单校验规则
const rules = reactive({
  passwordLenMin: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  passwordLenMax: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  passwordRule: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  passwordExpireSeconds: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  passwordRetryLimit: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  passwordRetryStrategy: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  weakPasswords: [
    { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [ 500 ]), trigger: "blur" }
  ]
})
const captchaTypeOptions = ref([]);
const loginConfigOptions = ref([]);

onMounted(() => {
  loadCaptchaTypeOptions();
  loadLoginConfigOptions();
  getList();
});

const loadCaptchaTypeOptions = () => {
  getCaptchaTypeOptions().then(response => {
    captchaTypeOptions.value = response.data;
  });
}

const loadLoginConfigOptions = () => {
  getLoginConfigs().then(response => {
    loginConfigOptions.value = response.data;
  });
}

// 方法
const getList = () => {
  loading.value = true;
  listSecurityConfigs().then(response => {
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
    passwordLenMin: 0,
    passwordLenMax: 0,
    passwordRule: 'NONE',
    passwordSensitive: [],
    passwordExpireSeconds: 0,
    forceModifyPwdAfterAdd: 'N',
    forceModifyPwdAfterReset: 'N',
    passwordRetryLimit: 0,
    passwordRetryStrategy: 'NONE',
    passwordRetryLockSeconds: 0,
    captchaEnable: 'N',
    captchaType: 'NONE',
    captchaExpires: 1,
    captchaDuration: 0,
    loginTypeConfigIds: [],
  };
}

const handleAdd = () => {
  reset();
  open.value = true;
  title.value = proxy.$t('System.Security.AddTitle');
  console.log(form.value)
}

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.configId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

const handleUpdate = (row) => {
  reset();
  const configId = row.configId || ids.value
  getSecurityConfig(configId).then(response => {
    Object.assign(form, response.data);
    open.value = true;
    title.value = proxy.$t('System.Security.EditTitle');
  });
}

const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      if (form.configId != undefined) {
        saveSecurityConfig(form).then(res => {
          proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
          open.value = false;
          getList();
        });
      } else {
        addSecurityConfig(form).then(res => {
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
    return deleteSecurityConfig(configIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t("Common.DeleteSuccess"));
  }).catch(() => {});
}

const handleStatusChange = (row) => {
  const configId = row.configId;
  changeConfigStatus(configId).then(res => {
    proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
    getList();
  });
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