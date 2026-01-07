<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:notice:add']"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:notice:edit']"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:notice:remove']"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-form v-show="showSearch" :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
      <el-form-item prop="noticeTitle">
        <el-input
          v-model="queryParams.noticeTitle"
          clearable
          :placeholder="$t('System.Notice.NoticeTitle')"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="createBy">
        <el-input
          v-model="queryParams.createBy"
          clearable
          :placeholder="$t('Common.CreateBy')"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="noticeType">
        <el-select v-model="queryParams.noticeType" :placeholder="$t('System.Notice.NoticeType')" style="width: 100px" clearable>
          <el-option
            v-for="dict in NoticeType"
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
 
    <el-table v-loading="loading" :data="noticeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="noticeId" width="100" :show-overflow-tooltip="true" />
      <el-table-column
        :label="$t('System.Notice.NoticeTitle')"
        prop="noticeTitle"
        :show-overflow-tooltip="true"
      />
      <el-table-column :label="$t('System.Notice.NoticeType')" align="center" prop="noticeType" width="140">
        <template #default="scope">
          <dict-tag :options="NoticeType" :value="scope.row.noticeType"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('System.Notice.Status')" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="NoticeStatus" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateBy')" align="center" prop="createBy" width="100" />
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="100">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" width="160">
        <template #default="scope">
          <el-button
            link
            type="success"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:notice:edit']"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:notice:remove']"
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
 
    <!-- 添加或修改公告对话框 -->
    <el-dialog v-model="open" :title="title" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item :label="$t('System.Notice.NoticeTitle')" prop="noticeTitle">
              <el-input v-model="form.noticeTitle" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('System.Notice.NoticeType')" prop="noticeType">
              <el-select v-model="form.noticeType">
                <el-option
                  v-for="dict in NoticeType"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.Notice.Status')" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in NoticeStatus"
                  :key="dict.value"
                  :value="dict.value"
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.Notice.NoticeContent')" prop="noticeContent">
              <editor v-model="form.noticeContent" :min-height="192"/>
            </el-form-item>
          </el-col>
        </el-row>
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
 
<script setup name="SystemNoticeIndex">
import { listNotice, getNotice, delNotice, addNotice, updateNotice } from "@/api/system/notice";

const { proxy } = getCurrentInstance();
const { NoticeStatus, NoticeType } = proxy.useDict('NoticeStatus', 'NoticeType');

// 遮罩层
const loading = ref(true);
// 选中数组
const ids = ref([]);
// 非单个禁用
const single = ref(true);
// 非多个禁用
const multiple = ref(true);
// 显示搜索条件
const showSearch = ref(true);
// 总条数
const total = ref(0);
// 公告表格数据
const noticeList = ref([]);
// 弹出层标题
const title = ref("");
// 是否显示弹出层
const open = ref(false);
// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  noticeTitle: undefined,
  createBy: undefined,
  status: undefined
});
// 表单参数
const form = ref({
  noticeId: undefined,
  noticeTitle: undefined,
  noticeType: undefined,
  noticeContent: undefined,
  status: "0"
});
// 表单校验
const rules = reactive({
  noticeTitle: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 50, message: proxy.$t('Common.RuleTips.MaxLength', [ 50 ]), trigger: "blur" }
  ],
  noticeType: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  status: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  noticeContent: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ]
});

/** 查询公告列表 */
function getList() {
  loading.value = true;
  listNotice(queryParams).then(response => {
    noticeList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    noticeId: undefined,
    noticeTitle: undefined,
    noticeType: undefined,
    noticeContent: undefined,
    status: "0"
  };
  proxy.resetForm("formRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.noticeId)
  single.value = selection.length!=1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = proxy.$t('System.Notice.Dialog.Add');
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const noticeId = row.noticeId || ids.value[0]
  getNotice(noticeId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t('System.Notice.Dialog.Edit');
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.noticeId != undefined) {
        updateNotice(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
          open.value = false;
          getList();
        });
      } else {
        addNotice(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const noticeIds = row.noticeId ? [ row.noticeId ] : ids.value
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return delNotice(noticeIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(() => {});
}

getList();
</script>
 