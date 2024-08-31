<template>
  <!-- 授权用户 -->
  <el-dialog :title="$t('System.User.SelectUser')" :visible.sync="visible" width="1000px" top="5vh" append-to-body>
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true">
      <el-form-item :label="$t('System.User.UserName')" prop="userName">
        <el-input
          v-model="queryParams.userName"
          :placeholder="$t('System.User.Placeholder.UserName')"
          clearable
          @keyup.enter.native="handleQuery"
          style="width:180px"
        />
      </el-form-item>
      <el-form-item :label="$t('System.User.PhoneNumber')" prop="phoneNumber">
        <el-input
          v-model="queryParams.phoneNumber"
          :placeholder="$t('System.User.Placeholder.PhoneNumber')"
          clearable
          @keyup.enter.native="handleQuery"
          style="width:180px"
        />
      </el-form-item>
      <el-form-item>
        <el-button-group>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>
    <el-row>
      <el-table @row-click="clickRow" ref="table" :data="userList" @selection-change="handleSelectionChange" height="260px">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column :label="$t('System.User.UserName')" prop="userName" :show-overflow-tooltip="true" />
        <el-table-column :label="$t('System.User.NickName')" prop="nickName" :show-overflow-tooltip="true" />
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
        <el-table-column :label="$t('System.User.IsAllocatedRole')" align="center" prop="allocated">
          <template slot-scope="scope">
            <i v-if="scope.row.allocated" class="el-icon-check" style="font-weight:600;color:green;"></i>
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
    </el-row>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="handleSelectUser">{{ $t('Common.Confirm') }}</el-button>
      <el-button @click="visible = false">{{ $t('Common.Cancel') }}</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { unallocatedUserList, authUserAdd } from "@/api/system/role";
export default {
  dicts: ['SysUserStatus'],
  props: {
    // 角色编号
    roleId: {
      type: [Number, String]
    }
  },
  data() {
    return {
      // 遮罩层
      visible: false,
      // 选中数组值
      userIds: [],
      // 总条数
      total: 0,
      // 未授权用户数据
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
  methods: {
    // 显示弹框
    show() {
      this.queryParams.roleId = this.roleId;
      this.getList();
      this.visible = true;
    },
    clickRow(row) {
      this.$refs.table.toggleRowSelection(row);
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.userIds = selection.map(item => item.userId);
    },
    // 查询表数据
    getList() {
      unallocatedUserList(this.queryParams).then(response => {
        this.userList = response.data.rows;
        this.total = parseInt(response.data.total);
      });
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
    /** 选择授权用户操作 */
    handleSelectUser() {
      const roleId = this.queryParams.roleId;
      if (!this.userIds || this.userIds.length == 0) {
        this.$modal.msgError(this.$t('Common.SelectFirst'));
        return;
      }
      authUserAdd({ roleId: roleId, userIds: this.userIds }).then(res => {
        this.$modal.msgSuccess(res.msg);
        if (res.code === 200) {
          this.visible = false;
          this.$emit("ok");
        }
      });
    }
  }
};
</script>
