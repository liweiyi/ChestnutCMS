<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:dept:add']">{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll" v-hasPermi="['system:dept:edit']">{{ $t("Common.ExpandOrCollapse") }}</el-button>
      </el-col>
      <right-toolbar
        v-model:showSearch="showSearch"
        @queryTable="getList"
      ></right-toolbar>
    </el-row>
    <el-form
      v-show="showSearch"
      :model="queryParams"
      ref="queryFormRef"
      :rules="queryRules"
      class="el-form-search"
      :inline="true"
    >
      <el-form-item prop="deptName">
        <el-input
          v-model="queryParams.deptName"
          :placeholder="$t('System.Dept.Placeholder.DeptName')"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="status">
        <el-select
          v-model="queryParams.status"
          :placeholder="$t('System.Dept.Placeholder.Status')"
          style="width: 100px"
          clearable
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
          <el-button type="primary" icon="Search" @click="handleQuery">{{ $t("Common.Search") }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>

    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="deptList"
      row-key="deptId"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column
        prop="deptName"
        :label="$t('System.Dept.DeptName')"
      ></el-table-column>
      <el-table-column
        prop="orderNum"
        :label="$t('System.Dept.Sort')"
        align="center"
        width="200"
      ></el-table-column>
      <el-table-column
        prop="status"
        :label="$t('System.Dept.Status')"
        align="center"
        width="80"
      >
        <template #default="scope">
          <dict-tag :options="EnableOrDisable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Common.CreateTime')"
        align="center"
        prop="createTime"
        width="180"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Common.Operation')"
        width="240"
      >
        <template #default="scope">
          <el-button
            link
            type="success"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:dept:edit']"
          >{{ $t("Common.Edit") }}</el-button>
          <el-button 
            link 
            type="primary"
            icon="Plus" 
            @click="handleAdd(scope.row)" 
            v-hasPermi="['system:dept:add']"
          >{{ $t("Common.Add") }}</el-button>
          <el-button
            v-if="scope.row.parentId != 0"
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
          >{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加或修改部门对话框 -->
    <el-dialog v-model="open" :title="title" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row v-if="form.deptId && form.deptId > 0">
          <el-col :span="24">
            <el-form-item
              v-if="form.parentId > 0"
              :label="$t('System.Dept.ParentDept')"
              prop="parentId"
            >
              <el-tag>{{ form.parentName }}</el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-else>
          <el-col :span="24">
            <el-form-item :label="$t('System.Dept.ParentDept')" prop="parentId">
              <el-tree-select
                v-model="form.parentId"
                :data="deptOptions"
                :props="{
                  value: 'deptId',
                  label: 'deptName',
                  children: 'children',
                }"
                :placeholder="$t('System.Dept.Placeholder.ParentDept')"
                check-strictly
                :render-after-expand="false"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item :label="$t('System.Dept.DeptName')" prop="deptName">
              <el-input
                v-model="form.deptName"
                :placeholder="$t('System.Dept.DeptName')"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('System.Dept.Sort')" prop="orderNum">
              <el-input-number
                v-model="form.orderNum"
                controls-position="right"
                :min="0"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item :label="$t('System.Dept.Phone')" prop="phone">
              <el-input
                v-model="form.phone"
                :placeholder="$t('System.Dept.Placeholder.Phone')"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('System.Dept.Email')" prop="email">
              <el-input
                v-model="form.email"
                :placeholder="$t('System.Dept.Placeholder.Email')"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item :label="$t('System.Dept.Leader')" prop="leader">
              <el-input
                v-model="form.leader"
                :placeholder="$t('System.Dept.Placeholder.Leader')"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('System.Dept.Status')">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in EnableOrDisable"
                  :key="dict.value"
                  :value="dict.value"
                  >{{ dict.label }}</el-radio
                >
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">{{
            $t("Common.Confirm")
          }}</el-button>
          <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SystemDeptIndex">
import { emailValidator, phoneNumberValidator } from "@/utils/validate";
import {
  listDept,
  getDept,
  delDept,
  addDept,
  updateDept,
} from "@/api/system/dept";
import { useDict } from "@/utils/dict";

const { proxy } = getCurrentInstance();
const { EnableOrDisable } = useDict("EnableOrDisable");

// 遮罩层
const loading = ref(true);
// 显示搜索条件
const showSearch = ref(true);
// 表格树数据
const deptList = ref([]);
// 部门树选项
const deptOptions = ref([]);
// 弹出层标题
const title = ref("");
// 是否显示弹出层
const open = ref(false);
// 是否展开，默认全部展开
const isExpandAll = ref(true);
// 重新渲染表格状态
const refreshTable = ref(true);

// 查询参数
const queryParams = reactive({
  deptName: undefined,
  status: undefined,
});

const queryRules = reactive({
  deptName: [
    {
      max: 30,
      message: proxy.$t("Common.RuleTips.MaxLength", [30]),
      trigger: "change",
    },
  ],
});

// 表单参数
const form = ref({
  deptId: undefined,
  parentId: undefined,
  deptName: undefined,
  orderNum: undefined,
  leader: undefined,
  phone: undefined,
  email: undefined,
  status: "0",
});

// 表单校验
const rules = reactive({
  parentId: [
    {
      required: true,
      message: proxy.$t("Common.RuleTips.NotEmpty"),
      trigger: "blur",
    },
  ],
  deptName: [
    {
      required: true,
      message: proxy.$t("Common.RuleTips.NotEmpty"),
      trigger: "blur",
    },
    {
      max: 30,
      message: proxy.$t("Common.RuleTips.MaxLength", [30]),
      trigger: "change",
    },
  ],
  orderNum: [
    {
      required: true,
      message: proxy.$t("Common.RuleTips.NotEmpty"),
      trigger: "blur",
    },
  ],
  email: [
    { validator: emailValidator, trigger: "change" },
    {
      max: 50,
      message: proxy.$t("Common.RuleTips.MaxLength", [50]),
      trigger: "change",
    },
  ],
  phone: [
    { validator: phoneNumberValidator, trigger: "change" },
    {
      max: 11,
      message: proxy.$t("Common.RuleTips.MaxLength", [11]),
      trigger: "change",
    },
  ],
  leader: [
    {
      max: 20,
      message: proxy.$t("Common.RuleTips.MaxLength", [20]),
      trigger: "change",
    },
  ],
});

/** 查询部门列表 */
function getList() {
  proxy.$refs.queryFormRef.validate((valid) => {
    if (valid) {
      loading.value = true;
      listDept(queryParams).then((response) => {
        deptList.value = proxy.handleTree(response.data.rows, "0", "deptId");
        loading.value = false;
      });
    }
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
    deptId: undefined,
    parentId: undefined,
    deptName: undefined,
    orderNum: undefined,
    leader: undefined,
    phone: undefined,
    email: undefined,
    status: "0",
  };
  proxy.resetForm("formRef");
}

/** 搜索按钮操作 */
function handleQuery() {
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}

/** 新增按钮操作 */
function handleAdd(row) {
  reset();
  if (row != undefined) {
    form.value.parentId = row.deptId;
  }
  open.value = true;
  title.value = proxy.$t("System.Dept.Dialog.Add");
  listDept().then((response) => {
    deptOptions.value = proxy.handleTree(response.data.rows, "0", "deptId");
  });
}

/** 展开/折叠操作 */
function toggleExpandAll() {
  refreshTable.value = false;
  isExpandAll.value = !isExpandAll.value;
  nextTick(() => {
    refreshTable.value = true;
  });
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  getDept(row.deptId).then((response) => {
    form.value = response.data;
    open.value = true;
    title.value = proxy.$t("System.Dept.Dialog.Edit");
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs.formRef.validate((valid) => {
    if (valid) {
      if (form.value.deptId != undefined) {
        updateDept(form.value).then((response) => {
          proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
          open.value = false;
          getList();
        });
      } else {
        addDept(form.value).then((response) => {
          proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"));
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal
    .confirm(proxy.$t("Common.ConfirmDelete", [row.deptName]))
    .then(function () {
      return delDept(row.deptId);
    })
    .then(() => {
      getList();
      proxy.$modal.msgSuccess(proxy.$t("Common.DeleteSuccess"));
    })
    .catch(() => {});
}

onMounted(() => {
  getList();
});
</script>
