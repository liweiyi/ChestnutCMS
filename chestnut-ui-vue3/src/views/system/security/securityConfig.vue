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

    <el-table 
      v-loading="loading" 
      :data="configList" 
      @selection-change="handleSelectionChange"
      @row-dblclick="handleDbRowClick">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.Security.Name')" prop="name" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.Security.Status')" align="center" prop="status" width="120">
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
  <el-drawer 
    direction="rtl"
    size="50%"
    destroy-on-close
    v-model="open"
    :title="title"
    append-to-body>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="success" plain icon="Edit" @click="submitForm">{{ $t('Common.Save') }}</el-button>
        </el-col>
      </el-row>
      <el-form ref="formRef" :model="form" v-loading="loading" :rules="rules" label-width="200px">
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>{{ $t('System.Security.Basic') }}</span>
            </div>
          </template>
          <el-form-item :label="$t('System.Security.Name')" prop="name">
            <el-input v-model="form.name" />
          </el-form-item>
        </el-card>
        <el-card shadow="hover">
          <template #header>
            <div class="clearfix">
              <span>{{ $t('System.Security.PasswordConfig') }}</span>
            </div>
          </template>
          <el-form-item :label="$t('System.Security.PasswordMinLength')" prop="configs.Password.minLength">
            <el-input-number v-model="form.configs.Password.minLength" controls-position="right" :min="6" :max="16"></el-input-number>
          </el-form-item>
          <el-form-item :label="$t('System.Security.PasswordMaxLength')" prop="configs.Password.maxLength">
            <el-input-number v-model="form.configs.Password.maxLength" controls-position="right" :min="16" :max="30"></el-input-number>
          </el-form-item>
          <el-form-item :label="$t('System.Security.PasswordRule')" prop="configs.Password.rule">
            <el-select v-model="form.configs.Password.rule">
              <el-option
                v-for="item in SecurityPasswordRule"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('System.Security.PasswordSensitive')" prop="configs.Password.sensitives">
            <el-checkbox-group v-model="form.configs.Password.sensitives">
                  <el-checkbox
                    v-for="item in SecurityPasswordSensitive"
                    :key="item.value"
                    :label="item.value"
                  >{{item.label}}</el-checkbox>
                </el-checkbox-group>
          </el-form-item>
          <el-form-item :label="$t('System.Security.WeakPasswords')" prop="configs.Password.weakPasswords">
            <el-input type="textarea" v-model="form.configs.Password.weakPasswords" :placeholder="$t('System.Security.WeakPasswordsPlaceholder')" :rows="5"></el-input>
          </el-form-item>
          <el-form-item 
            :label="$t('System.Security.PasswordExpireSeconds')"
            prop="configs.Password.expireSeconds">
            <el-input-number v-model="form.configs.Password.expireSeconds" controls-position="right" :min="0" :max="8640000"></el-input-number>
            <el-icon class="tips"><InfoFilled /></el-icon>
            <span class="tips">{{ $t('System.Security.PasswordExpireSecondsTip') }}</span>
          </el-form-item>
          <el-form-item :label="$t('System.Security.ForceModifyPwdAfterAdd')" prop="configs.Password.forceModifyPwdAfterAdd">
            <el-switch
              v-model="form.configs.Password.forceModifyPwdAfterAdd"
              active-value="Y"
              inactive-value="N">
            </el-switch>
            <el-icon class="tips"><InfoFilled /></el-icon>
            <span class="tips">{{ $t('System.Security.ForceModifyPwdAfterAddTip') }}</span>
          </el-form-item>
          <el-form-item :label="$t('System.Security.ForceModifyPwdAfterReset')" prop="configs.Password.forceModifyPwdAfterReset">
            <el-switch
              v-model="form.configs.Password.forceModifyPwdAfterReset"
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
          <el-form-item :label="$t('System.Security.PasswordRetryLimit')" prop="configs.Login.passwordRetryLimit">
            <el-input-number v-model="form.configs.Login.passwordRetryLimit" controls-position="right" :min="0"></el-input-number>
            <el-icon class="tips"><InfoFilled /></el-icon>
            <span class="tips">{{ $t('System.Security.PasswordRetryLimitTip') }}</span>
          </el-form-item>
          <el-form-item 
            :label="$t('System.Security.PasswordRetryStrategy')"
            prop="configs.Login.passwordRetryStrategy">
            <el-select v-model="form.configs.Login.passwordRetryStrategy" :disabled="form.configs.Login.passwordRetryLimit===0">
              <el-option
                v-for="item in SecurityPasswordRetryStrategy"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item 
            v-if="form.configs.Login.passwordRetryStrategy==='LOCK'"
            :label="$t('System.Security.PasswordRetryLockSeconds')"
            prop="configs.Login.passwordRetryLockSeconds">
            <el-input-number v-model="form.configs.Login.passwordRetryLockSeconds" controls-position="right" :min="0" :max="31536000"></el-input-number>
            <el-icon class="tips"><InfoFilled /></el-icon>
            <span class="tips">{{ $t('System.Security.PasswordRetryLockSecondsTip') }}</span>
          </el-form-item>
          <el-form-item :label="$t('System.Security.CaptchaEnable')" prop="configs.Login.captchaEnable">
            <el-switch
              v-model="form.configs.Login.captchaEnable"
              active-value="Y"
              inactive-value="N">
            </el-switch>
          </el-form-item>
          <el-form-item :label="$t('System.Security.CaptchaType')" prop="configs.Login.captchaType">
            <el-select v-model="form.configs.Login.captchaType">
              <el-option
                v-for="item in captchaTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('System.Security.CaptchaExpireSeconds')" prop="configs.Login.captchaExpires">
            <el-input-number v-model="form.configs.Login.captchaExpires" controls-position="right" :min="1" :max="3600"></el-input-number>
          </el-form-item>
          <el-form-item :label="$t('System.Security.CaptchaRetryDuration')" prop="configs.Login.captchaDuration">
            <el-input-number v-model="form.configs.Login.captchaDuration" controls-position="right" :min="0"></el-input-number>
          </el-form-item>
          <el-form-item :label="$t('System.Security.SmsLoginEnable')" prop="configs.Login.smsLoginEnable">
            <el-switch v-model="form.configs.Login.smsLoginEnable" active-value="Y" inactive-value="N" />
          </el-form-item>
          <el-form-item v-if="form.configs.Login.smsLoginEnable === 'Y'" :label="$t('System.Security.SmsMessageConfigId')" prop="configs.Login.smsMessageConfigId">
            <el-select
              v-model="form.configs.Login.smsMessageConfigId"
              :placeholder="$t('System.Security.SmsMessageRequired')"
              filterable
              remote
              clearable
              :remote-method="loadSmsConfigOptions"
              :loading="smsConfigLoading"
              style="width: 100%"
              @change="handleSmsConfigChange"
            >
              <el-option
                v-for="item in smsConfigOptions"
                :key="item.configId"
                :label="item.name"
                :value="String(item.configId)"
              />
            </el-select>
          </el-form-item>
        </el-card>
      </el-form>
    </el-drawer>
  </div>
</template>
<script setup name="SysSecurityIndex">
import { listSecurityConfigs, getSecurityConfig, addSecurityConfig, saveSecurityConfig, deleteSecurityConfig, changeConfigStatus } from "@/api/system/security";
import { getCaptchaTypeOptions } from "@/api/system/captcha";
import { getConfigList, getConfigDetail } from "@/api/tool/message/config";

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
const title = ref("")
const open = ref(false)
const objects = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
})
const { form, queryParams } = toRefs(objects);

const validateSmsMessageId = (rule, value, callback) => {
  if (form.value.configs.Login.smsLoginEnable !== 'Y' || /^[1-9]\d*$/.test(String(value ?? ''))) {
    callback();
  } else {
    callback(new Error(proxy.$t('System.Security.SmsMessageRequired')));
  }
};

// 表单校验规则
const rules = reactive({
  name: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  'configs.Password.minLength': [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  'configs.Password.maxLength': [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  'configs.Password.rule': [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  'configs.Password.expireSeconds': [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  'configs.Login.passwordRetryLimit': [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  'configs.Login.passwordRetryStrategy': [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  'configs.Login.smsMessageConfigId': [
    { validator: validateSmsMessageId, trigger: ['blur', 'change'] }
  ],
  'configs.Password.weakPasswords': [
    { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [ 500 ]), trigger: "blur" }
  ]
})
const captchaTypeOptions = ref([]);
const smsConfigLoading = ref(false);
const smsConfigRows = ref([]);
const selectedSmsConfig = ref();
let smsConfigRequestId = 0;
let smsConfigDetailRequestId = 0;
const smsConfigOptions = computed(() => {
  const selected = selectedSmsConfig.value;
  const selectedId = String(form.value?.configs?.Login?.smsMessageConfigId ?? '');
  if (selected && String(selected.configId) === selectedId
      && !smsConfigRows.value.some(item => String(item.configId) === selectedId)) {
    return [selected, ...smsConfigRows.value];
  }
  return smsConfigRows.value;
});
const newPattern = ref('');
const patternSearch = ref('');
const patternSelection = ref([]);

const filteredPatterns = computed(() => {
  const keyword = patternSearch.value.trim().toLowerCase();
  const list = form.value?.configs?.Biz?.secondaryVerifications || [];
  const filtered = keyword ? list.filter(p => p.toLowerCase().includes(keyword)) : list;
  return filtered.map(pattern => ({ pattern }));
});

onMounted(() => {
  loadCaptchaTypeOptions();
  getList();
});

const loadCaptchaTypeOptions = () => {
  getCaptchaTypeOptions().then(response => {
    captchaTypeOptions.value = response.data.filter(item => item.value !== 'SMS'); // 用户名密码登录不支持手机验证码，前端直接过滤掉
  });
}

const loadSmsConfigOptions = async (keyword = '') => {
  const requestId = ++smsConfigRequestId;
  smsConfigLoading.value = true;
  try {
    const response = await getConfigList({
      type: 'SMS', name: keyword.trim() || undefined, pageNum: 1, pageSize: 50,
    });
    if (requestId === smsConfigRequestId) {
      smsConfigRows.value = response.data.rows;
    }
  } catch {
    if (requestId === smsConfigRequestId) smsConfigRows.value = [];
  } finally {
    if (requestId === smsConfigRequestId) smsConfigLoading.value = false;
  }
};

const loadSelectedSmsConfig = async () => {
  const selectedId = form.value.configs.Login.smsMessageConfigId;
  const requestId = ++smsConfigDetailRequestId;
  selectedSmsConfig.value = undefined;
  if (!/^[1-9]\d*$/.test(String(selectedId ?? ''))) return;
  try {
    const response = await getConfigDetail(selectedId);
    if (requestId === smsConfigDetailRequestId && response.data.type === 'SMS'
        && String(form.value?.configs?.Login?.smsMessageConfigId) === String(selectedId)) {
      selectedSmsConfig.value = response.data;
    }
  } catch {
    // 请求层展示配置读取错误，保留当前选择供用户重新选择。
  }
};

watch(() => open.value && form.value?.configs?.Login?.smsLoginEnable === 'Y', visible => {
  if (visible) {
    loadSmsConfigOptions();
    loadSelectedSmsConfig();
  } else {
    smsConfigRequestId++;
    smsConfigDetailRequestId++;
    smsConfigLoading.value = false;
  }
});

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
  smsConfigRows.value = [];
  selectedSmsConfig.value = undefined;
  smsConfigRequestId++;
  smsConfigDetailRequestId++;
  proxy.resetForm("formRef");
  form.value = {
    configs: {
      Password: {
        minLength: 0,
        maxLength: 0,
        rule: 'NONE',
        sensitives: [],
        weakPasswords: '',
        expireSeconds: 0,
        forceModifyPwdAfterAdd: 'N',
        forceModifyPwdAfterReset: 'N',
      },
      Login: {
        passwordRetryLimit: 0,
        passwordRetryStrategy: 'NONE',
        passwordRetryLockSeconds: 0,
        captchaEnable: 'N',
        captchaType: 'NONE',
        captchaExpires: 1,
        captchaDuration: 0,
        smsLoginEnable: 'N',
        smsMessageConfigId: undefined,
      },
      Biz: {
        secondaryVerifications: [],
      },
    }
  };
}

const handleAdd = () => {
  reset();
  open.value = true;
  title.value = proxy.$t('System.Security.AddTitle');
}

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.configId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

const handleDbRowClick = (row) => {
  handleUpdate(row);
}

const handleUpdate = (row) => {
  reset();
  loading.value = true;
  const configId = row.configId || ids.value
  getSecurityConfig(configId).then(response => {
    response.data.configs.Login = { ...form.value.configs.Login, ...response.data.configs.Login };
    const smsConfigId = response.data.configs.Login.smsMessageConfigId;
    response.data.configs.Login.smsMessageConfigId = /^[1-9]\d*$/.test(String(smsConfigId ?? ''))
      ? String(smsConfigId) : undefined;
    response.data.configs.Password.weakPasswords = response.data.configs.Password.weakPasswords.join('\n');
    if (!response.data.configs.Biz) {
      response.data.configs.Biz = { secondaryVerifications: [] };
    }
    form.value = response.data;
  }).finally(() => {
    loading.value = false;
  });
  title.value = proxy.$t('System.Security.EditTitle');
  open.value = true;
}

const submitForm = () => {
  proxy.$refs['formRef'].validate(valid => {
    if (valid) {
      const payload = {
        ...form.value,
        configs: {
          ...form.value.configs,
          Password: {
            ...form.value.configs.Password,
            weakPasswords: form.value.configs.Password.weakPasswords.split('\n'),
          },
        },
      };
      if (form.value.configId != undefined) {
        saveSecurityConfig(payload).then(res => {
          proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
          open.value = false;
          getList();
        });
      } else {
        addSecurityConfig(payload).then(res => {
          proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
          open.value = false;
          getList();
        });
      }
    }
  });
}

const handleSmsConfigChange = (configId) => {
  smsConfigDetailRequestId++;
  selectedSmsConfig.value = smsConfigRows.value.find(item => String(item.configId) === String(configId));
};

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

const addPattern = () => {
  const val = newPattern.value.trim();
  if (!val) return;
  const list = form.value.configs.Biz.secondaryVerifications;
  if (!list.includes(val)) {
    list.push(val);
  }
  newPattern.value = '';
}

const removePatternByValue = (pattern) => {
  const list = form.value.configs.Biz.secondaryVerifications;
  const idx = list.indexOf(pattern);
  if (idx !== -1) list.splice(idx, 1);
}

const handlePatternSelectionChange = (selection) => {
  patternSelection.value = selection.map(row => row.pattern);
}

const removeSelectedPatterns = () => {
  const toDelete = new Set(patternSelection.value);
  form.value.configs.Biz.secondaryVerifications =
    form.value.configs.Biz.secondaryVerifications.filter(p => !toDelete.has(p));
  patternSelection.value = [];
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
.sv-editor {
  width: 100%;
  max-width: 680px;
}
.sv-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}
.sv-search {
  width: 180px;
  flex-shrink: 0;
}
.sv-table {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}
.sv-path {
  font-family: monospace;
  font-size: 13px;
}
.sv-tip {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}
</style>