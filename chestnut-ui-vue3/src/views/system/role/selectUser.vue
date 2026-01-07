<template>
  <!-- 授权用户 -->
  <el-dialog :title="$t('System.User.SelectUser')" v-model="visible" width="800px" top="5vh" append-to-body>
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item :label="$t('System.User.UserName')" prop="userName">
        <el-input
          v-model="queryParams.userName"
          :placeholder="$t('System.User.UserName')"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="$t('System.User.PhoneNumber')" prop="phoneNumber">
        <el-input
          v-model="queryParams.phoneNumber"
          :placeholder="$t('System.User.PhoneNumber')"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
      </el-form-item>
    </el-form>
    <el-row>
      <el-table
        @row-click="clickRow"
        ref="refTable"
        :data="userList"
        @selection-change="handleSelectionChange"
        height="260px"
      >
        <el-table-column type="selection" :selectable="selectable" width="55"></el-table-column>
        <el-table-column :label="$t('System.User.UserName')" prop="userName" :show-overflow-tooltip="true" />
        <el-table-column :label="$t('System.User.NickName')" prop="nickName" :show-overflow-tooltip="true" />
        <el-table-column :label="$t('System.User.Email')" prop="email" :show-overflow-tooltip="true" />
        <el-table-column :label="$t('System.User.PhoneNumber')" prop="phoneNumber" :show-overflow-tooltip="true" />
        <el-table-column :label="$t('System.User.Status')" align="center" prop="status">
          <template #default="scope">
            <dict-tag :options="SysUserStatus" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="180">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-row>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleSelectUser">{{ $t('Common.Confirm') }}</el-button>
        <el-button @click="visible = false">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="SelectUser">
import { authUserAdd, unallocatedUserList } from '@/api/system/role';

const props = defineProps({
  roleId: {
    type: [Number, String],
  },
});

const { proxy } = getCurrentInstance();
const { SysUserStatus } = proxy.useDict('SysUserStatus');

const userList = ref([]);
const visible = ref(false);
const total = ref(0);
const userIds = ref([]);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleId: undefined,
  userName: undefined,
  phoneNumber: undefined,
});

const selectable = (row) => !row.allocated;

// 显示弹框
function show() {
  queryParams.roleId = props.roleId;
  getList();
  visible.value = true;
}

/**选择行 */
function clickRow(row) {
  if (row.allocated) {
    return;
  }
  proxy.$refs['refTable'].toggleRowSelection(row);
}

// 多选框选中数据
function handleSelectionChange(selection) {
  userIds.value = selection.map(item => item.userId);
}

// 查询表数据
function getList() {
  unallocatedUserList(queryParams).then(res => {
    userList.value = res.data.rows;
    total.value = parseInt(res.data.total);
  });
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryRef');
  handleQuery();
}

const emit = defineEmits(['ok']);
/** 选择授权用户操作 */
function handleSelectUser() {
  if (!userIds.value || userIds.value.length == 0) {
    proxy.$modal.msgError(proxy.$t('Common.SelectFirst'));
    return;
  }
  authUserAdd({ roleId: queryParams.roleId, userIds: userIds.value }).then(res => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    if (res.code === 200) {
      visible.value = false;
      emit('ok');
    }
  });
}

defineExpose({
  show,
});
</script>
