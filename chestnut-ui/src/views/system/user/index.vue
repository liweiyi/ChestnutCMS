<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--部门数据-->
      <el-col :span="4" :xs="24">
        <div class="head-container">
          <el-input
            v-model="deptName"
            :placeholder="$t('System.User.Placeholder.DeptName')"
            clearable
            size="small"
            prefix-icon="el-icon-search"
            style="margin-bottom: 20px"
          />
        </div>
        <div class="head-container">
          <el-tree
            :data="deptOptions"
            :props="defaultProps"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            ref="tree"
            node-key="id"
            default-expand-all
            highlight-current
            @node-click="handleNodeClick"
          />
        </div>
      </el-col>
      <!--用户数据-->
      <el-col :span="20" :xs="24">
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
          <el-col :span="1.5">
            <el-button
              type="info"
              plain
              icon="el-icon-upload2"
              size="small"
              @click="handleImport"
            >{{ $t('Common.Import') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button
              type="warning"
              plain
              icon="el-icon-download"
              size="small"
              @click="handleExport"
            >{{ $t('Common.Export') }}</el-button>
          </el-col>
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
        </el-row>
        <el-row style="text-align:right" v-show="showSearch">
          <el-form :model="queryParams" ref="queryForm" class="el-form-search mb8" size="small" :inline="true">
            <el-form-item :label="$t('System.User.UserName')" prop="userName">
              <el-input
                v-model="queryParams.userName"
                :placeholder="$t('System.User.Placeholder.UserName')"
                clearable
                style="width: 140px"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item :label="$t('System.User.PhoneNumber')" prop="phoneNumber">
              <el-input
                v-model="queryParams.phoneNumber"
                :placeholder="$t('System.User.Placeholder.PhoneNumber')"
                clearable
                style="width: 140px"
                @keyup.enter.native="handleQuery"
              />
            </el-form-item>
            <el-form-item :label="$t('System.User.Status')" prop="status">
              <el-select
                v-model="queryParams.status"
                :placeholder="$t('System.User.Placeholder.Status')"
                clearable
                style="width: 140px"
              >
                <el-option
                  v-for="dict in dict.type.SysUserStatus"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button-group>
                <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
                <el-button icon="el-icon-refresh"  @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
              </el-button-group>
            </el-form-item>
          </el-form>
        </el-row>

        <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column :label="$t('System.User.UserId')" align="center" key="userId" prop="userId" width="140" v-if="columns[0].visible" />
          <el-table-column :label="$t('System.User.UserName')" align="center" key="userName" prop="userName" v-if="columns[1].visible" :show-overflow-tooltip="true" />
          <el-table-column :label="$t('System.User.NickName')" align="center" key="nickName" prop="nickName" v-if="columns[2].visible" :show-overflow-tooltip="true" />
          <el-table-column :label="$t('System.User.Dept')" align="center" key="deptName" prop="deptName" v-if="columns[3].visible" :show-overflow-tooltip="true" />
          <el-table-column :label="$t('System.User.PhoneNumber')" align="center" key="phoneNumber" prop="phoneNumber" v-if="columns[4].visible" width="120" />
          <el-table-column :label="$t('System.User.Status')" align="center" prop="status" width="80">
            <template slot-scope="scope">
              <dict-tag :options="dict.type.SysUserStatus" :value="scope.row.status"/>
            </template>
          </el-table-column>
          <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" v-if="columns[6].visible" width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            :label="$t('Common.Operation')"
            align="center"
            width="200"
            class-name="small-padding fixed-width"
          >
            <template slot-scope="scope" v-if="scope.row.userId !== 1">
              <span class="btn-cell-wrap">
                <el-button
                  size="small"
                  type="text"
                  icon="el-icon-edit"
                  @click="handleUpdate(scope.row)"
                  v-hasPermi="['system:user:edit']"
                >{{ $t('Common.Edit') }}</el-button>
              </span>
              <span class="btn-cell-wrap">
                <el-button
                  size="small"
                  type="text"
                  icon="el-icon-delete"
                  @click="handleDelete(scope.row)"
                  v-hasPermi="['system:user:remove']"
                >{{ $t('Common.Delete') }}</el-button>
              </span>
              <span class="btn-cell-wrap">
                <el-dropdown size="small" @command="(command) => handleCommand(command, scope.row)">
                  <el-button size="small" type="text" icon="el-icon-more"></el-button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="handleResetPwd" icon="el-icon-key"
                      v-hasPermi="['system:user:resetPwd']">{{ $t('System.User.ResetPwd') }}</el-dropdown-item>
                    <el-dropdown-item command="handleAuthRole" icon="el-icon-circle-check"
                      v-hasPermi="['system:user:edit']">{{ $t('System.User.RoleSetting') }}</el-dropdown-item>
                    <el-dropdown-item command="handleGrantPerms" icon="el-icon-circle-check"
                      v-hasPermi="['system:user:edit']">{{ $t('System.Role.PermissionSetting') }}</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
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
      </el-col>
    </el-row>

    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
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
          <treeselect v-model="form.deptId" :options="deptOptions" :show-count="true" :placeholder="$t('System.User.Placeholder.Dept')" />
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
              v-for="dict in dict.type.Gender"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('System.User.Status')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.SysUserStatus"
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
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>

    <!-- 用户导入对话框 -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload
        ref="upload"
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
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">{{ $t('System.User.ImportTip1') }}</div>
        <div class="el-upload__tip text-center" slot="tip">
          <div class="el-upload__tip" slot="tip">
            <el-checkbox v-model="upload.updateSupport" /> {{ $t('System.User.ImportUpdate') }}
          </div>
          <span>{{ $t('Common.InvalidFileSuffix', [ '.xls, .xlsx' ]) }}</span>
          <el-link type="primary" :underline="false" style="font-size:12px;vertical-align: baseline;" @click="importTemplate">{{ $t('System.User.DownloadTemplate') }}</el-link>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="upload.open = false">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
    <!-- 角色设置 -->
    <el-drawer
      direction="rtl"
      size="35%"
      :title="$t('System.User.RoleSetting')"
      :visible.sync="openRoleDialog"
      :before-close="handleRoleDialogClose">
      <div style="padding: 0 20px">
        <el-row class="mb10">
          <el-col>
            <el-button
              type="primary"
              plain
              icon="el-icon-save"
              size="mini"
              @click="handleSaveAuthRole"
              v-hasPermi="['system:user:edit']"
            >{{ $t('Common.Save') }}</el-button>
          </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-card shadow="never">
              <el-table v-loading="loading" ref="roleTable" :data="roleOptions" @selection-change="handleRoleSelectionChange">
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column :label="$t('System.Role.RoleName')" prop="roleName" :show-overflow-tooltip="true" />
                <el-table-column :label="$t('System.Role.Status')" align="center" prop="status">
                  <template slot-scope="scope">
                    <dict-tag :options="dict.type.EnableOrDisable" :value="scope.row.status"/>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-drawer>
    <!-- 权限设置 -->
    <el-drawer
      direction="rtl"
      size="60%"
      :with-header="false"
      destroy-on-close
      :visible.sync="openPermissionDialog"
      :before-close="handleGrantPermsClose">
      <role-permission owner-type='User' :owner='owner'></role-permission>
    </el-drawer>
  </div>
</template>

<script>
import { userNameValidator, emailValidator, phoneNumberValidator } from '@/utils/validate';
import { listUser, getUser, delUser, addUser, updateUser, resetUserPwd, changeUserStatus, deptTreeSelect, getAuthRole, updateAuthRole } from "@/api/system/user";
import { getToken } from "@/utils/auth";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
import RolePermission from '@/views/system/permission/permsTab';

export default {
  name: "SystemUserIndex",
  dicts: ['SysUserStatus', 'Gender', 'EnableOrDisable'],
  components: { 
    Treeselect,
    'role-permission': RolePermission  
  },
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
      // 用户表格数据
      userList: null,
      // 弹出层标题
      title: "",
      // 部门树选项
      deptOptions: undefined,
      // 是否显示弹出层
      open: false,
      // 部门名称
      deptName: undefined,
      // 岗位选项
      postOptions: [],
      // 角色选项
      roleOptions: [],
      openRoleDialog: false,
      // 表单参数
      form: {},
      defaultProps: {
        children: "children",
        label: "label"
      },
      openPermissionDialog: false,
      owner: undefined,
      // 用户导入参数
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
        headers: { Authorization: "Bearer " + getToken() },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/system/user/importData"
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        phoneNumber: undefined,
        status: undefined,
        deptId: undefined
      },
      // 列信息
      columns: [
        { key: 0, label: this.$t('System.User.UserId'), visible: true },
        { key: 1, label: this.$t('System.User.UserName'), visible: true },
        { key: 2, label: this.$t('System.User.NickName'), visible: true },
        { key: 3, label: this.$t('System.User.Dept'), visible: true },
        { key: 4, label: this.$t('System.User.PhoneNumber'), visible: true },
        { key: 5, label: this.$t('System.User.Status'), visible: true },
        { key: 6, label: this.$t('Common.CreateTime'), visible: true }
      ],
      // 表单校验
      rules: {
        userName: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
          { validator: userNameValidator, trigger: [ "blur", "change" ] },
          { min: 2, max: 20, message: this.$t('Common.RuleTips.LengthRange', [ 2, 20 ]), trigger: 'blur' }
        ],
        deptId: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        nickName: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
          { max: 30, message: this.$t('Common.RuleTips.MaxLength', [ 30 ]), trigger: "change" }
        ],
        realName: [
          { max: 30, message: this.$t('Common.RuleTips.MaxLength', [ 30 ]), trigger: "change" }
        ],
        password: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        sex: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        email: [
          { validator: emailValidator, trigger: [ "blur", "change" ] },
          { max: 50, message: this.$t('Common.RuleTips.MaxLength', [ 50 ]), trigger: "change" }
        ],
        phoneNumber: [,
          { validator: phoneNumberValidator, trigger: [ "blur", "change" ] },
          { max: 20, message: this.$t('Common.RuleTips.MaxLength', [ 20 ]), trigger: "change" }
        ]
      }
    };
  },
  watch: {
    // 根据名称筛选部门树
    deptName(val) {
      this.$refs.tree.filter(val);
    }
  },
  created() {
    this.getList();
    this.getDeptTree();
  },
  methods: {
    /** 查询用户列表 */
    getList() {
      this.loading = true;
      listUser(this.queryParams).then(response => {
          this.userList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      );
    },
    /** 查询部门下拉树结构 */
    getDeptTree() {
      deptTreeSelect().then(response => {
        this.deptOptions = response.data;
      });
    },
    // 筛选节点
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    // 节点单击事件
    handleNodeClick(data) {
      this.queryParams.deptId = data.id;
      this.handleQuery();
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        userId: undefined,
        deptId: undefined,
        userName: undefined,
        nickName: undefined,
        password: undefined,
        phoneNumber: undefined,
        email: undefined,
        sex: undefined,
        status: "0",
        remark: undefined,
        postIds: [],
        roleIds: []
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
      this.queryParams.deptId = undefined;
      this.$refs.tree.setCurrentKey(null);
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.userId);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    handleRoleSelectionChange(selection) {
      this.form.roleIds = selection.map(item => item.roleId);
    },
    handleRoleDialogClose() {
      this.openRoleDialog = false;
      this.$refs.roleTable.clearSelection();
    },
    // 更多操作触发
    handleCommand(command, row) {
      switch (command) {
        case "handleResetPwd":
          this.handleResetPwd(row);
          break;
        case "handleAuthRole":
          this.handleAuthRole(row);
          break;
        case "handleGrantPerms":
          this.handleGrantPerms(row);
          break;
        default:
          break;
      }
    },
    handleGrantPerms(row) {
      this.openPermissionDialog = true;
      this.owner = row.userId;
    },
    // 取消按钮（授权）
    handleGrantPermsClose() {
      this.openPermissionDialog = false;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      getUser().then(response => {
        this.postOptions = response.data.posts;
        this.open = true;
        this.title = this.$t('System.User.Dialog.Add');
      });
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const userId = row.userId || this.ids;
      getUser(userId).then(response => {
        this.form = response.data.user;
        this.postOptions = response.data.posts;
        this.open = true;
        this.title = this.$t('System.User.Dialog.Edit');
        this.form.password = "";
      });
    },
    /** 重置密码按钮操作 */
    handleResetPwd(row) {
      this.$prompt(this.$t('System.User.InputNewPwd', [ row.userName ]), this.$t('Common.Tips'), {
        confirmButtonText: this.$t('Common.Confirm'),
        cancelButtonText: this.$t('Common.Cancel'),
        closeOnClickModal: false
      }).then(({ value }) => {
          resetUserPwd(row.userId, value).then(response => {
            this.$modal.msgSuccess(this.$t('System.User.ChangePwdSucc', [ value ]));
          });
        }).catch(() => {});
    },
    /** 分配角色操作 */
    handleAuthRole: function(row) {
      // const userId = row.userId;
      // this.$router.push("/system/user-auth/role/" + userId);
      this.reset();
      this.loading = true;
      this.openRoleDialog = true;
      getAuthRole(row.userId).then(response => {
        this.form = response.data.user;
        this.roleOptions = response.data.roles;
        const userRoleIds = response.data.user.roleIds;
        this.$nextTick(() => {
          this.roleOptions.forEach((row) => {
            if (userRoleIds && userRoleIds.includes(row.roleId)) {
              this.$refs.roleTable.toggleRowSelection(row);
            }
          });
          this.loading = false;
        });
      });
    },
    handleSaveAuthRole: function() {
      const data = { userId: this.form.userId, roleIds: this.form.roleIds };
      updateAuthRole(data).then((response) => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
      });
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.userId != undefined) {
            updateUser(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.EditSuccess'));
              this.open = false;
              this.getList();
            });
          } else {
            addUser(this.form).then(response => {
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
      const userIds = row.userId ? [ row.userId ] : this.ids;
      this.$modal.confirm(this.$t('System.User.ConfirmDelete', [ userIds ])).then(function() {
        return delUser(userIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.exportExcel('system/user/list', {
        ...this.queryParams
      }, `user_${this.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
    },
    /** 导入按钮操作 */
    handleImport() {
      this.upload.title = this.$t('System.User.Dialog.Import');
      this.upload.open = true;
    },
    /** 下载模板操作 */
    importTemplate() {
      this.download('system/user/importTemplate', {
      }, `user_template_${this.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.xlsx`)
    },
    // 文件上传中处理
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    // 文件上传成功处理
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      this.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.data + "</div>", this.$t('System.User.ImportResult'), { dangerouslyUseHTMLString: true });
      this.getList();
    },
    // 提交上传文件
    submitFileForm() {
      this.$refs.upload.submit();
    }
  }
};
</script>