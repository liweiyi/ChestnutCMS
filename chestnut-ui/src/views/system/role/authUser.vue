<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="openSelectUser"
          v-hasPermi="['system:role:add']"
        >{{ $t('System.UserRole.AddUser') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-circle-close"
          size="mini"
          :disabled="multiple"
          @click="cancelAuthUser"
          v-hasPermi="['system:role:remove']"
        >{{ $t('System.UserRole.RemoveUser') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-close"
          size="mini"
          @click="handleClose"
        >{{ $t('Common.Close') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row style="text-align: right" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true">
        <el-form-item :label="$t('System.User.UserName')" prop="userName">
          <el-input
            v-model="queryParams.userName"
            clearable
            style="width: 240px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item :label="$t('System.User.PhoneNumber')" prop="phoneNumber">
          <el-input
            v-model="queryParams.phoneNumber"
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

    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.User.UserName')" prop="userName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.User.NickName')" prop="nickName" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.User.Email')" prop="email" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.User.PhoneNumber')" prop="phoneNumber" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('System.User.Status')" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.SysUserStatus" :value="scope.row.status"/>
        </template>
      </el-table-column>
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
              icon="el-icon-circle-close"
              @click="cancelAuthUser(scope.row)"
              v-hasPermi="['system:role:remove']"
            >{{ $t('Common.Remove') }}</el-button>
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
    <select-user ref="select" :roleId="queryParams.roleId" @ok="handleQuery" />
  </div>
</template>

<script>
import { allocatedUserList, authUserCancel } from "@/api/system/role";
import selectUser from "./selectUser";

export default {
  name: "AuthUser",
  dicts: ['SysUserStatus'],
  components: { selectUser },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中用户组
      userIds: [],
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 用户表格数据
      userList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        roleId: undefined,
        userName: undefined,
        phoneNumber: undefined
      }
    };
  },
  created() {
    const roleId = this.$route.params && this.$route.params.roleId;
    if (roleId) {
      this.queryParams.roleId = roleId;
      this.getList();
    }
  },
  methods: {
    /** 查询授权用户列表 */
    getList() {
      this.loading = true;
      allocatedUserList(this.queryParams).then(response => {
          this.userList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      );
    },
    // 返回按钮
    handleClose() {
      const obj = { path: "/system/role" };
      this.$tab.closeOpenPage(obj);
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
      this.userIds = selection.map(item => item.userId)
      this.multiple = !selection.length
    },
    /** 打开授权用户表弹窗 */
    openSelectUser() {
      this.$refs.select.show();
    },
    /** 取消授权 */
    cancelAuthUser(row) {
      const roleId = this.queryParams.roleId;
      const _userIds = row.userId ? [ row.userId ] : this.userIds;
      if (!_userIds || _userIds.length == 0) {
        return;
      }
      this.$modal.confirm(this.$t('System.Role.ConfirmRemoveUser')).then(function() {
        return authUserCancel({ roleId: roleId, userIds: _userIds });
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.Success'));
      });
    }
  }
};
</script>