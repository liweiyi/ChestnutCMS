<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['system:menu:add']"
        >
          {{ $t('Common.Add') }}
        </el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll">
          {{ isExpandAll ? $t('Common.Collapse') : $t('Common.Expand') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-form
      :model="queryParams"
      ref="queryRef"
      :inline="true"
      v-show="showSearch"
      class="el-form-search"
    >
      <el-form-item prop="status">
        <el-select v-model="queryParams.status" :placeholder="$t('System.Menu.Status')" clearable style="width: 100px">
          <el-option
            v-for="dict in EnableOrDisable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">
          {{ $t('Common.Search') }}
        </el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
      </el-form-item>
    </el-form>
    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="menuList"
      row-key="menuId"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column
        prop="menuName"
        :label="$t('System.Menu.MenuName')"
        :show-overflow-tooltip="true"
        width="160"
      ></el-table-column>
      <el-table-column prop="icon" :label="$t('System.Menu.Icon')" align="center" width="100">
        <template #default="scope">
          <svg-icon :icon-class="scope.row.icon" />
        </template>
      </el-table-column>
      <el-table-column prop="orderNum" :label="$t('System.Menu.Sort')" width="60"></el-table-column>
      <el-table-column
        prop="perms"
        :label="$t('System.Menu.Perms')"
        :show-overflow-tooltip="true"
      ></el-table-column>
      <el-table-column
        prop="component"
        :label="$t('System.Menu.Component')"
        :show-overflow-tooltip="true"
      ></el-table-column>
      <el-table-column prop="visible" :label="$t('System.Menu.Visible')" width="80">
        <template #default="scope">
          <dict-tag :options="YesOrNo" :value="scope.row.visible" />
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('System.Menu.Status')" align="center" width="80">
        <template #default="scope">
          <dict-tag :options="EnableOrDisable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Common.CreateTime')"
        align="center"
        width="160"
        prop="createTime"
      >
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Common.Operation')"
        width="220"
      >
        <template #default="scope">
          <el-button
            link
            type="success"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['system:menu:edit']"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            link
            type="primary"
            icon="Plus"
            @click="handleAdd(scope.row)"
            v-hasPermi="['system:menu:add']"
          >{{ $t('Common.Add') }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:menu:remove']"
          >{{ $t('Common.Delete') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加或修改菜单对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="menuRef" :model="form" :rules="rules" label-width="140px">
        <el-row>
          <el-col :span="24">
            <el-form-item :label="$t('System.Menu.ParentMenu')">
              <el-tree-select
                v-model="form.parentId"
                :data="menuOptions"
                :props="{ value: 'id' }"
                value-key="menuId"
                :placeholder="$t('System.Menu.Placeholder.ParentMenu')"
                check-strictly
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('System.Menu.MenuType')" prop="menuType">
              <el-radio-group v-model="form.menuType">
                <el-radio label="M">{{ $t('System.Menu.TypeDir') }}</el-radio>
                <el-radio label="C">{{ $t('System.Menu.TypeMenu') }}</el-radio>
                <el-radio label="F">{{ $t('System.Menu.TypeBtn') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="form.menuType != 'F'">
            <el-form-item :label="$t('System.Menu.Icon')" prop="icon">
              <el-popover placement="bottom-start" :width="540" trigger="click">
                <template #reference>
                  <el-input
                    v-model="form.icon"
                    :placeholder="$t('System.Menu.Placeholder.Icon')"
                    @blur="showSelectIcon"
                    readonly
                  >
                    <template #prefix>
                      <svg-icon
                        v-if="form.icon"
                        :icon-class="form.icon"
                        class="el-input__icon"
                        style="height: 32px; width: 16px"
                      />
                      <el-icon v-else style="height: 32px; width: 16px"><search /></el-icon>
                    </template>
                  </el-input>
                </template>
                <icon-select ref="iconSelectRef" @selected="selected" :active-icon="form.icon" />
              </el-popover>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('System.Menu.MenuName')" prop="menuName">
              <el-input
                v-model="form.menuName"
                :placeholder="$t('System.Menu.Placeholder.MenuName')"
              >
                <template v-if="form.menuId" #append>
                  <i18n-editor :languageKey="'MENU.NAME.' + form.menuId"></i18n-editor>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('System.Menu.Sort')" prop="orderNum">
              <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item prop="perms">
              <el-input
                v-model="form.perms"
                :placeholder="$t('System.Menu.Placeholder.Perms')"
                maxlength="100"
              />
              <template #label>
                <span>
                  <el-tooltip :content="$t('System.Menu.PermsTips')" placement="top">
                    <el-icon><question-filled /></el-icon>
                  </el-tooltip>
                  {{ $t('System.Menu.Perms') }}
                </span>
              </template>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType != 'F'">
            <el-form-item prop="path">
              <template #label>
                <span>
                  <el-tooltip :content="$t('System.Menu.RouterLinkTips')" placement="top">
                    <el-icon><question-filled /></el-icon>
                  </el-tooltip>
                  {{ $t('System.Menu.RouterLink') }}
                </span>
              </template>
              <el-input
                v-model="form.path"
                :placeholder="$t('System.Menu.Placeholder.RouterLink')"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType == 'C'">
            <el-form-item prop="component">
              <template #label>
                <span>
                  <el-tooltip :content="$t('System.Menu.ComponentTips')" placement="top">
                    <el-icon><question-filled /></el-icon>
                  </el-tooltip>
                  {{ $t('System.Menu.Component') }}
                </span>
              </template>
              <el-input
                v-model="form.component"
                :placeholder="$t('System.Menu.Placeholder.Component')"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType == 'C'">
            <el-form-item>
              <el-input
                v-model="form.query"
                :placeholder="$t('System.Menu.Placeholder.RouterParams')"
                maxlength="255"
              />
              <template #label>
                <span>
                  <el-tooltip :content="$t('System.Menu.RouterParamsTips')" placement="top">
                    <el-icon><question-filled /></el-icon>
                  </el-tooltip>
                  {{ $t('System.Menu.RouterParams') }}
                </span>
              </template>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType != 'F'">
            <el-form-item>
              <template #label>
                <span>
                  <el-tooltip :content="$t('System.Menu.IsExternalLinkTips')" placement="top">
                    <el-icon><question-filled /></el-icon>
                  </el-tooltip>
                  {{ $t('System.Menu.IsExternalLink') }}
                </span>
              </template>
              <el-radio-group v-model="form.isFrame">
                <el-radio v-for="dict in YesOrNo" :key="dict.value" :value="dict.value">
                  {{ dict.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType == 'C'">
            <el-form-item>
              <template #label>
                <span>
                  <el-tooltip :content="$t('System.Menu.IsCacheTips')" placement="top">
                    <el-icon><question-filled /></el-icon>
                  </el-tooltip>
                  {{ $t('System.Menu.IsCache') }}
                </span>
              </template>
              <el-radio-group v-model="form.isCache">
                <el-radio v-for="dict in YesOrNo" :key="dict.value" :value="dict.value">
                  {{ dict.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType != 'F'">
            <el-form-item>
              <template #label>
                <span>
                  <el-tooltip :content="$t('System.Menu.VisibleTips')" placement="top">
                    <el-icon><question-filled /></el-icon>
                  </el-tooltip>
                  {{ $t('System.Menu.Visible') }}
                </span>
              </template>
              <el-radio-group v-model="form.visible">
                <el-radio v-for="dict in YesOrNo" :key="dict.value" :value="dict.value">
                  {{ dict.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item>
              <template #label>
                <span>
                  <el-tooltip :content="$t('System.Menu.StatusTips')" placement="top">
                    <el-icon><question-filled /></el-icon>
                  </el-tooltip>
                  {{ $t('System.Menu.Status') }}
                </span>
              </template>
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in EnableOrDisable" :key="dict.value" :value="dict.value">
                  {{ dict.label }}
                </el-radio>
              </el-radio-group>
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

<script setup name="SysMenu">
import { addMenu, delMenu, getMenu, listMenu, updateMenu, treeselect } from '@/api/system/menu';
import SvgIcon from '@/components/SvgIcon';
import IconSelect from '@/components/IconSelect';
import I18nEditor from '@/views/components/I18nFieldEditor';

const { proxy } = getCurrentInstance();
const { YesOrNo, EnableOrDisable } = proxy.useDict('YesOrNo', 'EnableOrDisable');

const menuList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const title = ref('');
const menuOptions = ref([]);
const isExpandAll = ref(false);
const refreshTable = ref(true);
const iconSelectRef = ref(null);

const data = reactive({
  form: {},
  queryParams: {
    menuName: undefined,
    visible: undefined,
  },
  rules: {
    parentId: [{ required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' }],
    menuType: [{ required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' }],
    menuName: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
      { max: 50, message: proxy.$t('Common.RuleTips.MaxLength', [50]), trigger: 'change' },
    ],
    orderNum: [{ required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' }],
    path: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
      { validator: validatePath, trigger: 'blur' },
    ],
    component: [{ validator: validateComponent, trigger: 'blur' }],
    isFrame: [],
    isCache: [],
    visible: [],
    status: [],
  },
});

const { queryParams, form, rules } = toRefs(data);

watch(
  () => form.value.menuType,
  newVal => {
    if (newVal != 'F') {
      rules.value.isFrame = [
        { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
      ];
      rules.value.visible = [
        { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
      ];
      rules.value.status = [
        { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
      ];
    }
    if (newVal === 'C') {
      rules.value.isCache = [
        { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: 'blur' },
      ];
    }
    nextTick(() => {
      proxy.$refs['menuRef']?.clearValidate && proxy.$refs['menuRef'].clearValidate();
    });
  },
);

function validateComponent(rule, value, callback) {
  if (form.value.menuType == 'C' && form.value.isFrame == 'N') {
    if (proxy.$tools.isEmpty(value)) {
      callback(new Error(proxy.$t('Common.RuleTips.NotEmpty')));
    } else if (value.length > 255) {
      callback(new Error(this.$t('Common.RuleTips.MaxLength', [255])));
    } else {
      callback();
    }
  } else {
    callback();
  }
}

function validatePath(rule, value, callback) {
  if (form.value.menuType != 'F' && form.value.isFrame == 'Y') {
    if (value.length > 200) {
      callback(new Error(proxy.$t('Common.RuleTips.MaxLength', [200])));
    } else if (!value.startsWith('http://') && !value.startsWith('https://')) {
      callback(new Error(proxy.$t('Common.RuleTips.Url')));
    } else {
      callback();
    }
  } else {
    callback();
  }
}


function normalizer(node) {
   if (node.children && !node.children.length) {
      delete node.children;
   }
   return {
      id: node.menuId,
      label: node.menuName,
      children: node.children
   };
}

/** 查询菜单列表 */
function getList() {
  loading.value = true;
  listMenu(queryParams.value).then(response => {
    menuList.value = proxy.handleTree(response.data.rows, '0', 'menuId');
    loading.value = false;
  });
}

/** 查询菜单下拉树结构 */
function getTreeselect() {
  menuOptions.value = [];
  treeselect().then(response => {
    menuOptions.value = [
      {
        id: '0',
        parentId: '',
        label: proxy.$t('APP.TITLE'),
        isRoot: true,
        isDefaultExpanded: true,
        children: response.data,
      },
    ];
  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  form.value = {
    menuId: undefined,
    parentId: 0,
    menuName: undefined,
    icon: undefined,
    menuType: 'M',
    orderNum: undefined,
    isFrame: 'N',
    isCache: 'Y',
    visible: 'Y',
    status: '0',
  };
  proxy.resetForm('menuRef');
}

/** 展示下拉图标 */
function showSelectIcon() {
  iconSelectRef.value.reset();
}

/** 选择图标 */
function selected(name) {
  form.value.icon = name;
}

/** 搜索按钮操作 */
function handleQuery() {
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryRef');
  handleQuery();
}

/** 新增按钮操作 */
function handleAdd(row) {
  reset();
  if (row != null && row.menuId) {
    form.value.parentId = row.menuId;
  } else {
    form.value.parentId = '0';
  }
  open.value = true;
  title.value = proxy.$t('System.Menu.Dialog.Add');
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
  getMenu(row.menuId).then(response => {
    form.value = response.data;
  });
  open.value = true;
  title.value = proxy.$t('System.Menu.Dialog.Edit');
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs['menuRef'].validate(valid => {
    if (valid) {
      if (form.value.menuId != undefined) {
        updateMenu(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          open.value = false;
          getTreeselect();
          getList();
        });
      } else {
        addMenu(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          open.value = false;
          getTreeselect();
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal
    .confirm(proxy.$t('System.Menu.ConfirmDelete', [row.menuName]))
    .then(function () {
      return delMenu(row.menuId);
    })
    .then(() => {
      getTreeselect();
      getList();
      proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
    })
    .catch(() => {});
}

getTreeselect();
getList();
</script>
