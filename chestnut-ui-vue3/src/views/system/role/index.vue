<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:role:add']"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:role:edit']"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:role:remove']"
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
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
        <el-form-item prop="roleName">
          <el-input
            v-model="queryParams.roleName"
            :placeholder="$t('System.Role.Placeholder.RoleName')"
            clearable
            style="width: 240px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="roleKey">
          <el-input
            v-model="queryParams.roleKey"
            :placeholder="$t('System.Role.Placeholder.RoleKey')"
            clearable
            style="width: 240px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="status">
          <el-select
            v-model="queryParams.status"
            :placeholder="$t('System.Role.Placeholder.Status')"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="dict in EnableOrDisable"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
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

    <el-table v-loading="loading" :data="roleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.Role.RoleId')" align="center" width="80" prop="roleId" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.Role.RoleName')" prop="roleName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.Role.RoleKey')" prop="roleKey" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.Role.Sort')" align="center" width="80" prop="roleSort" />
      <el-table-column :label="$t('System.Role.Status')" align="center" width="80">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" width="430" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="success"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:role:edit']"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:role:remove']"
          >{{ $t('Common.Delete') }}</el-button>
          <el-button
            link
            type="primary"
            icon="CircleCheck"
            @click="handleGrantPerms(scope.row)"
            v-hasPermi="['system:role:grant']"
          >{{ $t('System.Role.PermissionSetting') }}</el-button>
          <el-button
            link
            type="primary"
            icon="User"
            @click="handleAuthUser(scope.row)"
            v-hasPermi="['system:role:edit']"
          >{{ $t('System.Role.UserSetting') }}</el-button>
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

    <!-- 添加或修改角色配置对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('System.Role.RoleName')" prop="roleName">
          <el-input v-model="form.roleName" :placeholder="$t('System.Role.Placeholder.RoleName')" />
        </el-form-item>
        <el-form-item :label="$t('System.Role.RoleKey')" prop="roleKey">
          <el-input v-model="form.roleKey" :placeholder="$t('System.Role.Placeholder.RoleKey')" />
        </el-form-item>
        <el-form-item :label="$t('System.Role.Sort')" prop="roleSort">
          <el-input-number v-model="form.roleSort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item :label="$t('System.Role.Status')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in EnableOrDisable"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')">
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
    <!-- 权限设置 -->
    <el-drawer
      direction="rtl"
      size="50%"
      :with-header="false"
      destroy-on-close
      v-model="openPermissionDialog"
      :before-close="handleGrantPermsClose">
      <role-permission owner-type='Role' :owner='owner'></role-permission>
    </el-drawer>
    <!-- 用户设置 -->
    <el-drawer
      direction="rtl"
      size="70%"
      :with-header="false"
      destroy-on-close
      v-model="openUserDialog"
      :before-close="handleUserDialogClose">
      <auth-user :role-id="currentRoleId"></auth-user>
    </el-drawer>
  </div>
</template>

<script setup name="SystemRoleIndex">
import { codeValidator } from '@/utils/validate';
import { listRole, getRole, delRole, addRole, updateRole, changeRoleStatus } from "@/api/system/role";
import RolePermission from '@/views/system/permission/permsTab';
import AuthUser from './authUser';

const { proxy } = getCurrentInstance()
const { EnableOrDisable } = proxy.useDict("EnableOrDisable")

const loading = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const showSearch = ref(true)
const total = ref(0)
const roleList = ref([])
const title = ref("")
const open = ref(false)
const openPermissionDialog = ref(false)
const owner = ref(undefined)
const openUserDialog = ref(false)
const currentRoleId = ref(undefined)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    roleName: undefined,
    roleKey: undefined,
    status: undefined
  },
  form: {},
  rules: {
    roleName: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { max: 30, message: proxy.$t('Common.RuleTips.MaxLength', [ 30 ]), trigger: "blur" }
    ],
    roleKey: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { validator: codeValidator, trigger: ["blur", "change"] },
      { max: 100, message: proxy.$t('Common.RuleTips.MaxLength', [ 100 ]), trigger: ["blur", "change"] }
    ],
    roleSort: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    status: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    remark: [
      { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [ 500 ]), trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listRole(queryParams.value).then(response => {
    roleList.value = response.data.rows
    total.value = parseInt(response.data.total)
    loading.value = false
  })
}

function handleStatusChange(row) {
  let text = row.status === "0" ? proxy.$t('System.Role.Enable') : proxy.$t('System.Role.Disable');
  proxy.$modal.confirm(proxy.$t('System.Role.ConfirmChangeStatus', [ text, row.roleName ])).then(function() {
    return changeRoleStatus(row.roleId, row.status);
  }).then(() => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
  }).catch(function() {
    row.status = row.status === "0" ? "1" : "0";
  });
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  if (proxy.$refs.menu != undefined) {
    proxy.$refs.menu.setCheckedKeys([]);
  }
  proxy.resetForm("formRef");
  form.value = {
    roleId: undefined,
    roleName: undefined,
    roleKey: undefined,
    roleSort: 0,
    status: "0",
    remark: undefined
  };
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.roleId)
  single.value = selection.length!=1
  multiple.value = !selection.length
}

function handleGrantPerms(row) {
  openPermissionDialog.value = true;
  owner.value = row.roleId;
}

function handleGrantPermsClose() {
  openPermissionDialog.value = false;
}

function handleAdd() {
  reset();
  open.value = true;
  title.value = proxy.$t('System.Role.Dialog.Add');
}

function handleUpdate(row) {
  reset();
  const roleId = row.roleId || ids.value
  getRole(roleId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t('System.Role.Dialog.Edit');
  });
}

function handleAuthUser(row) {
  currentRoleId.value = row.roleId;
  openUserDialog.value = true;
}

function handleUserDialogClose() {
  openUserDialog.value = false;
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.roleId != undefined) {
        updateRole(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.Success'));
          open.value = false;
          getList();
        });
      } else {
        addRole(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'));
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  const roleIds = row.roleId ? [ row.roleId ] : ids.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return delRole(roleIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(() => {});
}

function handleExport() {
  proxy.download('system/role/list', {
    ...queryParams.value
  }, `role_${proxy.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
}

getList()
</script>