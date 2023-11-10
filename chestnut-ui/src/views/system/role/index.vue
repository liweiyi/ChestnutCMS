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
          v-hasPermi="['system:role:add']"
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
          v-hasPermi="['system:role:edit']"
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
          v-hasPermi="['system:role:remove']"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="['system:role:export']"
        >{{ $t('Common.Export') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true">
        <el-form-item :label="$t('System.Role.RoleName')" prop="roleName">
          <el-input
            v-model="queryParams.roleName"
            :placeholder="$t('System.Role.Placeholder.RoleName')"
            clearable
            style="width: 240px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item :label="$t('System.Role.RoleKey')" prop="roleKey">
          <el-input
            v-model="queryParams.roleKey"
            :placeholder="$t('System.Role.Placeholder.RoleKey')"
            clearable
            style="width: 240px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item :label="$t('System.Role.Status')" prop="status">
          <el-select
            v-model="queryParams.status"
            :placeholder="$t('System.Role.Placeholder.Status')"
            clearable
            style="width: 240px"
          >
            <el-option
              v-for="dict in dict.type.EnableOrDisable"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table v-loading="loading" :data="roleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.Role.RoleId')" prop="roleId" />
      <el-table-column :label="$t('System.Role.RoleName')" prop="roleName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.Role.RoleKey')" prop="roleKey" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.Role.Sort')" align="center" prop="roleSort" />
      <el-table-column :label="$t('System.Role.Status')" align="center">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width" width="280">
        <template slot-scope="scope" v-if="scope.row.roleId !== 1">
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['system:role:edit']"
            >{{ $t('Common.Edit') }}</el-button>
          </span>
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['system:role:remove']"
            >{{ $t('Common.Delete') }}</el-button>
          </span>
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-circle-check"
              @click="handleGrantPerms(scope.row)"
              v-hasPermi="['system:role:edit']"
            >{{ $t('System.Role.PermissionSetting') }}</el-button>
          </span>
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-user"
              @click="handleAuthUser(scope.row)"
              v-hasPermi="['system:role:edit']"
            >{{ $t('System.Role.UserSetting') }}</el-button>
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

    <!-- 添加或修改角色配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('System.Role.RoleName')" prop="roleName">
          <el-input v-model="form.roleName" :placeholder="$t('System.Role.Placeholder.RoleName')" />
        </el-form-item>
        <el-form-item prop="roleKey">
          <span slot="label">
            <el-tooltip :content="$t('System.Role.RoleKeyTips')" placement="top">
              <i class="el-icon-question"></i>
            </el-tooltip>
            {{ $t('System.Role.RoleKey') }}
          </span>
          <el-input v-model="form.roleKey" :placeholder="$t('System.Role.Placeholder.RoleKey')" />
        </el-form-item>
        <el-form-item :label="$t('System.Role.Sort')" prop="roleSort">
          <el-input-number v-model="form.roleSort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item :label="$t('System.Role.Status')">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.EnableOrDisable"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('Common.Confirm') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
    <!-- 权限设置 -->
    <el-drawer
      direction="rtl"
      size="50%"
      :with-header="false"
      :visible.sync="openPermissionDialog"
      :before-close="handleGrantPermsClose">
      <role-permission owner-type='Role' :owner='owner'></role-permission>
    </el-drawer>
  </div>
</template>

<script>
import { listRole, getRole, delRole, addRole, updateRole, changeRoleStatus } from "@/api/system/role";
import RolePermission from '@/views/system/permission/permsTab';

export default {
  name: "Role",
  dicts: ['EnableOrDisable'],
  components: { 
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
      // 角色表格数据
      roleList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示弹出层（数据权限）
      openPermissionDialog: false,
      owner: undefined,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        roleName: undefined,
        roleKey: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        roleName: [
          { required: true, message: this.$t('System.Role.RuleTips.RoleName'), trigger: "blur" }
        ],
        roleKey: [
          { required: true, pattern: "^[A-Za-z0-9_]+$", message: this.$t('System.Role.RuleTips.RoleKey'), trigger: "blur" }
        ],
        roleSort: [
          { required: true, message: this.$t('System.Role.RuleTips.Sort'), trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询角色列表 */
    getList() {
      this.loading = true;
      listRole(this.queryParams).then(response => {
          this.roleList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      );
    },
    // 角色状态修改
    handleStatusChange(row) {
      let text = row.status === "0" ? this.$t('System.Role.Enable') : this.$t('System.Role.Disable');
      this.$modal.confirm(this.$t('System.Role.ConfirmChangeStatus', [ text, row.roleName ])).then(function() {
        return changeRoleStatus(row.roleId, row.status);
      }).then(() => {
        this.$modal.msgSuccess(this.$t('Common.OpSuccess'));
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0";
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      if (this.$refs.menu != undefined) {
        this.$refs.menu.setCheckedKeys([]);
      }
      this.form = {
        roleId: undefined,
        roleName: undefined,
        roleKey: undefined,
        roleSort: 0,
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.roleId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    handleGrantPerms(row) {
      this.openPermissionDialog = true;
      this.owner = row.roleId;
    },
    // 取消按钮（授权）
    handleGrantPermsClose() {
      this.openPermissionDialog = false;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = this.$t('System.Role.Dialog.Add');
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const roleId = row.roleId || this.ids
      getRole(roleId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = this.$t('System.Role.Dialog.Edit');
      });
    },
    /** 分配用户操作 */
    handleAuthUser: function(row) {
      const roleId = row.roleId;
      this.$router.push("/system/role-auth/user/" + roleId);
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.roleId != undefined) {
            updateRole(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.Success'));
              this.open = false;
              this.getList();
            });
          } else {
            addRole(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const roleIds = row.roleId ? [ row.roleId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return delRole(roleIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(() => {});
    },
    handleExport() {
      this.download('system/role/export', {
        ...this.queryParams
      }, `role_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>