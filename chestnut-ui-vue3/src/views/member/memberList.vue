<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button plain type="primary" icon="Plus" @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button plain type="success" icon="Edit" :disabled="single" @click="handleUpdate">{{ $t("Common.Edit") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button plain type="danger" icon="Delete" :disabled="multiple" @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
        <el-form-item prop="userName">
          <el-input v-model="queryParams.userName" clearable :placeholder="$t('Member.UserName')" style="width: 160px"
            @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item prop="nickName">
          <el-input v-model="queryParams.nickName" clearable :placeholder="$t('Member.NickName')" style="width: 160px"
            @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="queryParams.email" clearable placeholder="Email" style="width: 160px"
            @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item prop="phoneNumber">
          <el-input v-model="queryParams.phoneNumber" clearable :placeholder="$t('Member.PhoneNumber')"
            style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item prop="status">
          <el-select v-model="queryParams.status" clearable :placeholder="$t('Member.Status')" style="width: 110px">
            <el-option v-for="dict in MemberStatus" :key="dict.value" :label="dict.label" :value="dict.value" />
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
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('Member.UserName')" align="center" :show-overflow-tooltip="true" prop="userName" />
      <el-table-column :label="$t('Member.NickName')" align="center" :show-overflow-tooltip="true" prop="nickName" />
      <el-table-column label="Email" align="center" :show-overflow-tooltip="true" prop="email" />
      <el-table-column :label="$t('Member.PhoneNumber')" align="center" :show-overflow-tooltip="true"
        prop="phoneNumber" />
      <el-table-column :label="$t('Member.Status')" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :options="MemberStatus" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column :label="$t('Member.Source')" align="center" width="100" :show-overflow-tooltip="true"
        prop="sourceType" />
      <el-table-column :label="$t('Member.RegistTime')" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Member.LastLoginTime')" align="center" prop="createTime" width="230">
        <template #default="scope">
          <span>{{ parseTime(scope.row.lastLoginTime) }} - {{ scope.row.lastLoginIp }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" width="220"
       >
        <template #default="scope">
          <el-button type="text" icon="Key" @click="handleResetPwd(scope.row)">{{ $t('Member.ResetPwd') }}</el-button>
          <el-button type="text" icon="Edit" @click="handleUpdate(scope.row)">{{ $t("Common.Edit") }}</el-button>
          <el-button type="text" icon="Delete" @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total > 0" 
      :total="total" 
      v-model:page="queryParams.pageNum" 
      v-model:limit="queryParams.pageSize"
      @pagination="getList" />


    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" v-model="open" :close-on-click-modal="false" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item v-if="form.memberId == undefined" :label="$t('Member.UserName')" prop="userName">
          <el-input v-model="form.userName" />
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item :label="$t('Member.PhoneNumber')" prop="phoneNumber">
          <el-input v-model="form.phoneNumber" />
        </el-form-item>
        <el-form-item :label="$t('Member.NickName')" prop="nickName">
          <el-input v-model="form.nickName" />
        </el-form-item>
        <el-form-item v-if="form.memberId == undefined" :label="$t('Member.Password')" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item :label="$t('Member.Birthday')" prop="birthday">
          <el-date-picker v-model="form.birthday" format="YYYY-MM-DD" value-format="YYYY-MM-DD HH:mm:ss"
            type="date"></el-date-picker>
        </el-form-item>
        <el-form-item :label="$t('Member.Status')">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in MemberStatus" :key="dict.value"
              :label="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleSubmitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="handleCancel">{{ $t('Common.Cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="MemberMemberList">
import { isBlank, userNameValidator, emailValidator, phoneNumberValidator } from '@/utils/validate';
import { getMemberList, getMemberDetail, addMember, updateMember, deleteMembers, resetMemberPassword } from "@/api/member/member";

const { proxy } = getCurrentInstance();
const { MemberStatus } = proxy.useDict('MemberStatus');

const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const dataList = ref([]);
const title = ref("");
const open = ref(false);
const objects = reactive({
  queryParams: {
    userName: undefined,
    nickName: undefined,
    email: undefined,
    phoneNumber: undefined,
    status: undefined,
    pageNum: 1,
    pageSize: 10,
  },
  form: {
    status: '0',
  },
});
const { queryParams, form } = toRefs(objects);

const validateMember = (rule, value, callback) => {
  if (isBlank(form.value.userName) && isBlank(form.value.phoneNumber) && isBlank(form.value.email)) {
    callback(new Error(proxy.$t('Member.RuleTips.Member')));
  } else {
    callback();
  }
};
const rules = reactive({
  userName: [
    { validator: userNameValidator, trigger: ["change", "blur"] },
    { max: 30, message: proxy.$t('Common.RuleTips.MaxLength', [30]), trigger: ["change", "blur"] },
    { validator: validateMember }
  ],
  nickName: [
    { max: 30, message: proxy.$t('Common.RuleTips.MaxLength', [30]), trigger: ["change", "blur"] },
  ],
  realName: [
    { max: 30, message: proxy.$t('Common.RuleTips.MaxLength', [30]), trigger: ["change", "blur"] },
  ],
  password: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [100]), trigger: ["change", "blur"] },
  ],
  email: [
    { validator: emailValidator, trigger: "blur" },
    { validator: validateMember, trigger: "blur" },
    { max: 50, message: proxy.$t('Common.RuleTips.MaxLength', [50]), trigger: ["change", "blur"] },
  ],
  phoneNumber: [
    { validator: phoneNumberValidator, trigger: "blur" },
    { validator: validateMember, trigger: "blur" },
    { max: 20, message: proxy.$t('Common.RuleTips.MaxLength', [20]), trigger: ["change", "blur"] },
  ],
  status: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
  ],
  remark: [
    { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [500]), trigger: ["change", "blur"] },
  ]
});

onMounted(() => {
    getList();
})

function getList() {
  loading.value = true;
  getMemberList(queryParams.value).then(response => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  queryParams.value = {
    userName: undefined,
    nickName: undefined,
    email: undefined,
    phoneNumber: undefined,
    status: undefined,
    pageNum: 1,
    pageSize: 10,
  };
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.memberId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

function reset() {
  proxy.resetForm("form");
  form.value = { status: '0' };
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = proxy.$t('Member.AddTitle');
}

function handleUpdate(row) {
  reset();
  const memberId = row.memberId ? row.memberId : ids.value[0];
  getMemberDetail(memberId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t('Member.EditTitle');
  });
}

function handleCancel() {
  open.value = false;
  reset();
}

function handleSubmitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.memberId != undefined) {
        updateMember(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          open.value = false;
          getList();
        });
      } else {
        addMember(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'));
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const memberIds = row.memberId ? [row.memberId] : ids.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function () {
    return deleteMembers(memberIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(() => { });
}

function handleResetPwd(row) {
  proxy.$prompt(proxy.$t('Member.InputPwd', [row.userName]), proxy.$t('Common.Tips'), {
    confirmButtonText: proxy.$t('Common.Confirm'),
    cancelButtonText: proxy.$t('Common.Cancel'),
    closeOnClickModal: false
  }).then(({ value }) => {
    resetMemberPassword(row.memberId, value).then(response => {
      proxy.$modal.msgSuccess(proxy.$t('Member.RestPwdSuccess', [value]));
    }).catch(() => { });
  });
}
</script>