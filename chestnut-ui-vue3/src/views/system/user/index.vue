<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="4" :xs="24">
        <div class="head-container">
          <el-input
            v-model="deptName"
            :placeholder="$t('System.User.Placeholder.DeptName')"
            clearable
            prefix-icon="Search"
            style="margin-bottom: 20px"
          />
        </div>
        <div class="head-container">
          <el-tree
            :data="deptOptions"
            :props="defaultProps"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            ref="treeRef"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handleNodeClick"
          />
        </div>
      </el-col>
      <!--用户数据-->
      <el-col :span="20" :xs="24">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button
              type="primary"
              plain
              icon="Plus"
              @click="handleAdd"
              v-hasPermi="['system:user:add']"
            >{{ $t('Common.Add') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="success"
              plain
              icon="Edit"
              :disabled="single"
              @click="handleUpdate"
              v-hasPermi="['system:user:edit']"
            >{{ $t('Common.Edit') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="danger"
              plain
              icon="Delete"
              :disabled="multiple"
              @click="handleDelete"
              v-hasPermi="['system:user:remove']"
            >{{ $t('Common.Delete') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="info"
              plain
              icon="Upload"
              @click="handleImport"
              v-hasPermi="['system:user:add']"
            >{{ $t('Common.Import') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="warning"
              plain
              icon="Download"
              @click="handleExport"
            >{{ $t('Common.Export') }}</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
        </el-row>
        <el-form v-show="showSearch" :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
          <el-form-item prop="userName">
            <el-input
              v-model="queryParams.userName"
              :placeholder="$t('System.User.Placeholder.UserName')"
              clearable
              style="width: 140px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item prop="phoneNumber">
            <el-input
              v-model="queryParams.phoneNumber"
              :placeholder="$t('System.User.Placeholder.PhoneNumber')"
              clearable
              style="width: 140px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item prop="status">
            <el-select
              v-model="queryParams.status"
              :placeholder="$t('System.User.Placeholder.Status')"
              clearable
              style="width: 140px"
            >
              <el-option
                v-for="dict in SysUserStatus"
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

        <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column :label="$t('System.User.UserName')" prop="userName" v-if="columns[1].visible" :show-overflow-tooltip="true" />
          <el-table-column :label="$t('System.User.NickName')" prop="nickName" v-if="columns[2].visible" :show-overflow-tooltip="true" />
          <el-table-column :label="$t('System.User.Dept')" prop="deptName" v-if="columns[3].visible" :show-overflow-tooltip="true" />
          <el-table-column :label="$t('System.User.PhoneNumber')" prop="phoneNumber" v-if="columns[4].visible" width="120" />
          <el-table-column :label="$t('System.User.Status')" align="center" prop="status" v-if="columns[5].visible" width="80">
            <template #default="scope">
              <dict-tag :options="SysUserStatus" :value="scope.row.status" />
            </template>
          </el-table-column>
          <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" v-if="columns[6].visible" width="160">
            <template #default="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            :label="$t('Common.Operation')"
            align="right"
            width="200"
          >
            <template #default="scope">
                <el-button
                  type="primary"
                  link
                  icon="Edit"
                  @click="handleUpdate(scope.row)"
                  v-hasPermi="['system:user:edit']"
                >{{ $t('Common.Edit') }}</el-button>
                <el-button
                  v-if="scope.row.userId !== '1'"
                  type="danger"
                  link
                  icon="Delete"
                  @click="handleDelete(scope.row)"
                  v-hasPermi="['system:user:remove']"
                >{{ $t('Common.Delete') }}</el-button>
                <el-dropdown
                  @command="(command) => handleCommand(command, scope.row)">
                  <el-button class="btn-more" type="primary" link icon="More"></el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item 
                        v-if="checkPermi(['system:user:resetPwd'])"
                        command="handleResetPwd" 
                        icon="Key"
                      >{{ $t('System.User.ResetPwd') }}</el-dropdown-item>
                      <el-dropdown-item 
                        v-if="scope.row.userId !== '1' && checkPermi(['system:user:edit'])"
                        command="handleAuthRole" 
                        icon="CircleCheck"
                       >{{ $t('System.User.RoleSetting') }}</el-dropdown-item>
                      <el-dropdown-item 
                        v-if="scope.row.userId !== '1' && checkPermi(['system:user:grant'])"
                        command="handleGrantPerms" 
                        icon="CircleCheck"
                      >{{ $t('System.Role.PermissionSetting') }}</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
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
      </el-col>
    </el-row>

    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item v-if="form.userId == undefined" :label="$t('System.User.UserName')" prop="userName">
          <el-input v-model="form.userName" :placeholder="$t('System.User.Placeholder.UserName')" maxlength="30" />
        </el-form-item>
        <el-form-item :label="$t('System.User.NickName')" prop="nickName">
          <el-input v-model="form.nickName" :placeholder="$t('System.User.Placeholder.NickName')" maxlength="30" />
        </el-form-item>
        <el-form-item :label="$t('System.User.RealName')" prop="realName">
          <el-input v-model="form.realName" :placeholder="$t('System.User.Placeholder.RealName')" maxlength="30" />
        </el-form-item>
        <el-form-item v-if="form.userId == undefined" :label="$t('System.User.Password')" prop="password">
          <el-input v-model="form.password" :placeholder="$t('System.User.Placeholder.Password')" type="password" maxlength="20" show-password/>
        </el-form-item>
        <el-form-item :label="$t('System.User.Dept')" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptOptions"
            :props="defaultProps"
            :render-after-expand="false"
            check-strictly
            :placeholder="$t('System.User.Placeholder.Dept')"
          />
        </el-form-item>
        <el-form-item :label="$t('System.User.PhoneNumber')" prop="phoneNumber">
          <el-input v-model="form.phoneNumber" :placeholder="$t('System.User.Placeholder.PhoneNumber')" maxlength="11" />
        </el-form-item>
        <el-form-item :label="$t('System.User.Email')" prop="email">
          <el-input v-model="form.email" :placeholder="$t('System.User.Placeholder.Email')" maxlength="50" />
        </el-form-item>
        <el-form-item :label="$t('System.User.Gender')" prop="sex">
          <el-select v-model="form.sex" :placeholder="$t('System.User.Placeholder.Gender')">
            <el-option
              v-for="dict in Gender"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('System.User.Status')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in SysUserStatus"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('System.User.Post')" prop="postIds">
          <el-select v-model="form.postIds" multiple :placeholder="$t('System.User.Placeholder.Post')">
            <el-option
              v-for="item in postOptions"
              :key="item.postId"
              :label="item.postName"
              :value="item.postId"
              :disabled="item.status == 1"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">{{ $t('Common.Submit') }}</el-button>
          <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用户导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body>
      <el-upload
        ref="uploadRef"
        :limit="1"
        accept=".xlsx, .xls"
        :headers="upload.headers"
        :action="upload.url + '?updateSupport=' + upload.updateSupport"
        :disabled="upload.isUploading"
        :on-progress="handleFileUploadProgress"
        :on-success="handleFileSuccess"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text" v-html="$t('System.User.ImportTip1')"></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <div class="el-upload__tip">
              <el-checkbox v-model="upload.updateSupport">{{ $t('System.User.ImportUpdate') }}</el-checkbox>
            </div>
            <span>{{ $t('System.User.ImportFileTypes') }}</span>
            <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="importTemplate">{{ $t('System.User.DownloadTemplate') }}</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">{{ $t('Common.Submit') }}</el-button>
          <el-button @click="upload.open = false">{{ $t('Common.Cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 角色设置 -->
    <el-drawer
      direction="rtl"
      size="35%"
      :title="$t('System.User.RoleSetting')"
      v-model="openRoleDialog"
      :before-close="handleRoleDialogClose">
        <el-row class="mb8">
          <el-col>
            <el-button
              type="primary"
              plain
              icon="Document"
              @click="handleSaveAuthRole"
              v-hasPermi="['system:user:edit']"
            >{{ $t('Common.Save') }}</el-button>
          </el-col>
        </el-row>
        <el-table v-loading="loading" ref="roleTableRef" :data="roleOptions" @selection-change="handleRoleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column :label="$t('System.Role.RoleName')" prop="roleName" :show-overflow-tooltip="true" />
          <el-table-column :label="$t('System.Role.Status')" align="center" prop="status">
            <template #default="scope">
              <dict-tag :options="EnableOrDisable.value" :value="scope.row.status"/>
            </template>
          </el-table-column>
        </el-table>
    </el-drawer>
    <!-- 权限设置 -->
    <el-drawer
      direction="rtl"
      size="60%"
      :with-header="false"
      destroy-on-close
      v-model="openPermissionDialog"
      :before-close="handleGrantPermsClose">
      <role-permission owner-type='User' :owner='owner'></role-permission>
    </el-drawer>
  </div>
</template>

<script setup>
import { userNameValidator, emailValidator, phoneNumberValidator } from '@/utils/validate';
import { listUser, getUser, delUser, addUser, updateUser, resetUserPwd, deptTreeSelect, getAuthRole, updateAuthRole } from "@/api/system/user";
import RolePermission from '@/views/system/permission/permsTab';

const { proxy } = getCurrentInstance();
const { SysUserStatus, Gender, EnableOrDisable } = proxy.useDict('SysUserStatus', 'Gender', 'EnableOrDisable');

// 响应式数据
const loading = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const showSearch = ref(true);
const total = ref(0);
const userList = ref([]);
const title = ref("");
const deptOptions = ref([]);
const open = ref(false);
const deptName = ref(undefined);
const postOptions = ref([]);
const roleOptions = ref([]);
const openRoleDialog = ref(false);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    phoneNumber: undefined,
    status: undefined,
    deptId: undefined
  },
  form: {},
  rules: {
    userName: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { validator: userNameValidator, trigger: [ "blur", "change" ] },
      { min: 2, max: 20, message: proxy.$t('Common.RuleTips.LengthRange', [ 2, 20 ]), trigger: 'blur' }
    ],
    deptId: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    nickName: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { max: 30, message: proxy.$t('Common.RuleTips.MaxLength', [ 30 ]), trigger: "change" }
    ],
    realName: [
      { max: 30, message: proxy.$t('Common.RuleTips.MaxLength', [ 30 ]), trigger: "change" }
    ],
    password: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    sex: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    email: [
      { validator: emailValidator, trigger: [ "blur", "change" ] },
      { max: 50, message: proxy.$t('Common.RuleTips.MaxLength', [ 50 ]), trigger: "change" }
    ],
    phoneNumber: [
      { validator: phoneNumberValidator, trigger: [ "blur", "change" ] },
      { max: 20, message: proxy.$t('Common.RuleTips.MaxLength', [ 20 ]), trigger: "change" }
    ]
  },
  upload: {
    // 是否显示弹出层（用户导入）
    open: false,
    // 弹出层标题（用户导入）
    title: "",
    // 是否禁用上传
    isUploading: false,
    // 是否更新已经存在的用户数据
    updateSupport: 0,
    // 设置上传的请求头部
    headers: { ...proxy.$auth.getTokenHeader() },
    // 上传的地址
    url: import.meta.env.VITE_APP_BASE_API + "/system/user/importData"
  }
})
const { queryParams, form, rules, upload } = toRefs(data)
const openPermissionDialog = ref(false);
const owner = ref(undefined);

const defaultProps = {
  value: "id",
  children: "children",
  label: "label"
};

const columns = ref([
  { key: 0, label: proxy.$t('System.User.UserId'), visible: true },
  { key: 1, label: proxy.$t('System.User.UserName'), visible: true },
  { key: 2, label: proxy.$t('System.User.NickName'), visible: true },
  { key: 3, label: proxy.$t('System.User.Dept'), visible: true },
  { key: 4, label: proxy.$t('System.User.PhoneNumber'), visible: true },
  { key: 5, label: proxy.$t('System.User.Status'), visible: true },
  { key: 6, label: proxy.$t('Common.CreateTime'), visible: true }
]);

// 监听器
watch(deptName, (val) => {
  proxy.$refs.treeRef.filter(val);
});

const getList = () => {
  loading.value = true;
  listUser(queryParams.value).then(response => {
    userList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
};

const getDeptTree = () => {
  deptTreeSelect().then(response => {
    deptOptions.value = response.data;
  });
};

const filterNode = (value, data) => {
  if (!value) return true;
  return data.label.indexOf(value) !== -1;
};

const handleNodeClick = (data) => {
  queryParams.deptId = data.id;
  handleQuery();
};

// 取消按钮
const cancel = () => {
  open.value = false;
  reset();
};

// 表单重置
const reset = () => {
  proxy.resetForm("formRef")
  form.value = {
    status: "0"
  };
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  proxy.resetForm("queryFormRef")
  queryParams.deptId = undefined;
  proxy.$refs.treeRef.setCurrentKey(null);
  handleQuery();
};

// 多选框选中数据
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.userId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
};

const handleRoleSelectionChange = (selection) => {
  form.value.roleIds = selection.map(item => item.roleId);
};

const handleRoleDialogClose = () => {
  openRoleDialog.value = false;
  proxy.$refs.roleTableRef.clearSelection();
};

// 更多操作触发
const handleCommand = (command, row) => {
  switch (command) {
    case "handleResetPwd":
      handleResetPwd(row);
      break;
    case "handleAuthRole":
      handleAuthRole(row);
      break;
    case "handleGrantPerms":
      handleGrantPerms(row);
      break;
    default:
      break;
  }
};

const handleGrantPerms = (row) => {
  openPermissionDialog.value = true;
  owner.value = row.userId;
};

// 取消按钮（授权）
const handleGrantPermsClose = () => {
  openPermissionDialog.value = false;
};

/** 新增按钮操作 */
const handleAdd = () => {
  reset();
  getUser().then(response => {
    postOptions.value = response.data.posts;
    open.value = true;
    title.value = proxy.$t('System.User.Dialog.Add');
  });
};

/** 修改按钮操作 */
const handleUpdate = (row) => {
  reset();
  const userId = row.userId || ids.value;
  getUser(userId).then(response => {
    form.value = response.data.user;
    postOptions.value = response.data.posts;
    open.value = true;
    title.value = proxy.$t('System.User.Dialog.Edit');
    form.value.password = "";
  });
};

/** 重置密码按钮操作 */
const handleResetPwd = (row) => {
  proxy.$modal.prompt(proxy.$t('System.User.InputNewPwd', [ row.userName ]), proxy.$t('Common.Tips'), {
    confirmButtonText: proxy.$t('Common.Confirm'),
    cancelButtonText: proxy.$t('Common.Cancel'),
    closeOnClickModal: false
  }).then(({ value }) => {
    resetUserPwd(row.userId, value).then(response => {
      proxy.$modal.msgSuccess(proxy.$t('System.User.ChangePwdSucc', [ value ]));
    });
  }).catch(() => {});
};

/** 分配角色操作 */
const handleAuthRole = (row) => {
  reset();
  loading.value = true;
  openRoleDialog.value = true;
  getAuthRole(row.userId).then(response => {
    form.value = response.data.user;
    roleOptions.value = response.data.roles;
    const userRoleIds = response.data.user.roleIds;
    proxy.$nextTick(() => {
      roleOptions.value.forEach((row) => {
        if (userRoleIds && userRoleIds.includes(row.roleId)) {
          proxy.$refs.roleTableRef.toggleRowSelection(row);
        }
      });
      loading.value = false;
    });
  });
};

const handleSaveAuthRole = () => {
  const data = { userId: form.value.userId, roleIds: form.value.roleIds };
  updateAuthRole(data).then((response) => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
  });
};

/** 提交按钮 */
const submitForm = () => {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.userId != undefined) {
        updateUser(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.EditSuccess'));
          open.value = false;
          getList();
        });
      } else {
        addUser(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'));
          open.value = false;
          getList();
        });
      }
    }
  });
};

/** 删除按钮操作 */
const handleDelete = (row) => {
  const userIds = row.userId ? [ row.userId ] : ids.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return delUser(userIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(() => {});
};

/** 导出按钮操作 */
const handleExport = () => {
  proxy.exportExcel('system/user/list', {
    ...queryParams
  }, `user_${proxy.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
};

/** 导入按钮操作 */
const handleImport = () => {
  upload.value.title = proxy.$t('System.User.Dialog.Import');
  upload.value.open = true;
};

/** 下载模板操作 */
const importTemplate = () => {
  proxy.download('system/user/importTemplate', {
  }, `user_template_${proxy.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
};

// 文件上传中处理
const handleFileUploadProgress = (event, file, fileList) => {
  upload.value.isUploading = true;
};

// 文件上传成功处理
const handleFileSuccess = (response, file, fileList) => {
  upload.value.open = false;
  upload.value.isUploading = false;
  proxy.$refs.uploadRef.clearFiles();
  proxy.$modal.alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.data + "</div>", proxy.$t('System.User.ImportResult'), { dangerouslyUseHTMLString: true });
  getList();
};

// 提交上传文件
const submitFileForm = () => {
  proxy.$refs.uploadRef.submit();
};

getList();
getDeptTree();
</script>
<style lang="scss" scoped>
.btn-more:focus-visible {
  outline: none;
  border: none;
  box-shadow: none;
}
</style>