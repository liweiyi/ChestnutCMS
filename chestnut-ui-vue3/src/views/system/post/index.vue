<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:post:add']"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:post:edit']"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:post:remove']"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
        >{{ $t('Common.Export') }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-form v-show="showSearch" :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
      <el-form-item prop="postCode">
        <el-input
          v-model="queryParams.postCode"
          :placeholder="$t('System.Post.Placeholder.PostCode')"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="postName">
        <el-input
          v-model="queryParams.postName"
          :placeholder="$t('System.Post.Placeholder.PostName')"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="status">
        <el-select v-model="queryParams.status" :placeholder="$t('System.Post.Placeholder.Status')" style="width: 100px" clearable>
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
 
    <el-table v-loading="loading" :data="postList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('System.Post.PostId')" align="center" prop="postId" />
      <el-table-column :label="$t('System.Post.PostCode')" align="center" prop="postCode" />
      <el-table-column :label="$t('System.Post.PostName')" align="center" prop="postName" />
      <el-table-column :label="$t('System.Post.PostSort')" align="center" prop="postSort" />
      <el-table-column :label="$t('System.Post.Status')" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="EnableOrDisable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" width="160">
        <template #default="scope">
          <el-button
            link
            type="success"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:post:edit']"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:post:remove']"
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
 
    <!-- 添加或修改岗位对话框 -->
    <el-dialog v-model="open" :title="title" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('System.Post.PostName')" prop="postName">
          <el-input v-model="form.postName" :placeholder="$t('System.Post.Placeholder.PostName')" />
        </el-form-item>
        <el-form-item :label="$t('System.Post.PostCode')" prop="postCode">
          <el-input v-model="form.postCode" :placeholder="$t('System.Post.Placeholder.PostCode')" />
        </el-form-item>
        <el-form-item :label="$t('System.Post.PostSort')" prop="postSort">
          <el-input-number v-model="form.postSort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item :label="$t('System.Post.Status')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in EnableOrDisable"
              :key="dict.value"
              :value="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">{{ $t('Common.Confirm') }}</el-button>
          <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
 
<script setup name="SystemPostIndex">
import { codeValidator } from '@/utils/validate';
import { listPost, getPost, delPost, addPost, updatePost } from "@/api/system/post";
import { useDict } from '@/utils/dict';

const { proxy } = getCurrentInstance();
const { EnableOrDisable } = useDict('EnableOrDisable');

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
// 岗位表格数据
const postList = ref([]);
// 弹出层标题
const title = ref("");
// 是否显示弹出层
const open = ref(false);
// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  postCode: undefined,
  postName: undefined,
  status: undefined
});
// 表单参数
const form = ref({
  postId: undefined,
  postCode: undefined,
  postName: undefined,
  postSort: 0,
  status: "0",
  remark: undefined
});
// 表单校验
const rules = reactive({
  postName: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 50, message: proxy.$t('Common.RuleTips.MaxLength', [ 50 ]), trigger: "blur" }
  ],
  postCode: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { validator: codeValidator, trigger: "change" },
    { max: 64, message: proxy.$t('Common.RuleTips.MaxLength', [ 64 ]), trigger: "blur" },
  ],
  postSort: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  remark: [
    { max: 500, message: proxy.$t('Common.RuleTips.MaxLength', [ 500 ]), trigger: "blur" },
  ]
});

/** 查询岗位列表 */
function getList() {
  loading.value = true;
  listPost(queryParams).then(response => {
    postList.value = response.data.rows;
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
    postId: undefined,
    postCode: undefined,
    postName: undefined,
    postSort: 0,
    status: "0",
    remark: undefined
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
  ids.value = selection.map(item => item.postId)
  single.value = selection.length!=1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = proxy.$t('System.Post.Dialog.Add');
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const postId = row.postId || ids.value
  getPost(postId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t('System.Post.Dialog.Edit');
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.postId != undefined) {
        updatePost(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
          open.value = false;
          getList();
        });
      } else {
        addPost(form.value).then(response => {
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
  const postIds = row.postId ? [ row.postId ] : ids.value;
  proxy.$modal.confirm(proxy.$t('System.Post.ConfirmDelete', [ postIds ])).then(function() {
    return delPost(postIds);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.exportExcel('system/post/list', {
    ...queryParams
  }, `post_${proxy.parseTime(new Date(),'{y}{m}{d}{h}{i}{s}')}.getTime()}.xlsx`)
}

getList();
</script>
 