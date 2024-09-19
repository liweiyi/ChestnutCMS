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
          v-hasPermi="['system:menu:add']"
        >{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-sort"
          size="small"
          @click="toggleExpandAll"
        >{{ $t("Common.ExpandOrCollapse") }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true">
        <el-form-item :label="$t('System.Menu.MenuName')" prop="menuName">
          <el-input
            v-model="queryParams.menuName"
            :placeholder="$t('System.Menu.Placeholder.MenuName')"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item :label="$t('System.Menu.Status')" prop="status">
          <el-select v-model="queryParams.status" style="width:100px;" clearable>
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
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t("Common.Search") }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="menuList"
      row-key="menuId"
      :default-expand-all="isExpandAll"
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
    >
      <el-table-column prop="menuName" :label="$t('System.Menu.MenuName')" :show-overflow-tooltip="true" width="160"></el-table-column>
      <el-table-column prop="icon" :label="$t('System.Menu.Icon')" align="center" width="100">
        <template slot-scope="scope">
          <svg-icon :icon-class="scope.row.icon" />
        </template>
      </el-table-column>
      <el-table-column prop="orderNum" :label="$t('System.Menu.Sort')" width="60"></el-table-column>
      <el-table-column prop="perms" :label="$t('System.Menu.Perms')" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="component" :label="$t('System.Menu.Component')" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="status" :label="$t('System.Menu.Visible')" align="center" width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.YesOrNo" :value="scope.row.visible"/>
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('System.Menu.Status')" align="center" width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.EnableOrDisable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime">
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
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['system:menu:edit']"
            >{{ $t('Common.Edit') }}</el-button>
          </span>
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-plus"
              @click="handleAdd(scope.row)"
              v-hasPermi="['system:menu:add']"
            >{{ $t('Common.Add') }}</el-button>
          </span>
          <span class="btn-cell-wrap">
            <el-button
              size="small"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['system:menu:remove']"
            >{{ $t('Common.Delete') }}</el-button>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加或修改菜单对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="140px">
        <el-row>
          <el-col :span="24">
            <el-form-item :label="$t('System.Menu.ParentMenu')" prop="parentId">
              <treeselect
                v-model="form.parentId"
                :options="menuOptions"
                :placeholder="$t('System.Menu.Placeholder.ParentMenu')"
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
              <el-popover
                placement="bottom-start"
                width="460"
                trigger="click"
                @show="$refs['iconSelect'].reset()"
              >
                <IconSelect ref="iconSelect" @selected="selected" />
                <el-input slot="reference" v-model="form.icon" :placeholder="$t('System.Menu.Placeholder.Icon')" readonly>
                  <svg-icon
                    v-if="form.icon"
                    slot="prefix"
                    :icon-class="form.icon"
                    class="el-input__icon"
                    style="height: 32px;width: 16px;"
                  />
                  <i v-else slot="prefix" class="el-icon-search el-input__icon" />
                </el-input>
              </el-popover>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('System.Menu.MenuName')" prop="menuName">
              <el-input v-model="form.menuName" :placeholder="$t('System.Menu.Placeholder.MenuName')">
                <i18n-editor slot="append" v-if="form.menuId" :languageKey="'MENU.NAME.' + form.menuId"></i18n-editor>
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
              <el-input v-model="form.perms" :placeholder="$t('System.Menu.Placeholder.Perms')" maxlength="100" />
              <span slot="label">
                <el-tooltip :content="$t('System.Menu.PermsTips')" placement="top">
                <i class="el-icon-question"></i>
                </el-tooltip>
                {{ $t("System.Menu.Perms") }}
              </span>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType != 'F'">
            <el-form-item prop="path">
              <span slot="label">
                <el-tooltip :content="$t('System.Menu.RouterLinkTips')" placement="top">
                <i class="el-icon-question"></i>
                </el-tooltip>
                {{ $t("System.Menu.RouterLink") }}
              </span>
              <el-input v-model="form.path" :placeholder="$t('System.Menu.Placeholder.RouterLink')" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType == 'C'">
            <el-form-item prop="component">
              <span slot="label">
                <el-tooltip :content="$t('System.Menu.ComponentTips')" placement="top">
                <i class="el-icon-question"></i>
                </el-tooltip>
                {{ $t("System.Menu.Component") }}
              </span>
              <el-input v-model="form.component" :placeholder="$t('System.Menu.Placeholder.Component')" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType == 'C'">
            <el-form-item prop="query">
              <el-input v-model="form.query" :placeholder="$t('System.Menu.Placeholder.RouterParams')" maxlength="255" />
              <span slot="label">
                <el-tooltip :content="$t('System.Menu.RouterParamsTips')" placement="top">
                <i class="el-icon-question"></i>
                </el-tooltip>
                {{ $t("System.Menu.RouterParams") }}
              </span>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType != 'F'">
            <el-form-item prop="isFrame">
              <span slot="label">
                <el-tooltip :content="$t('System.Menu.IsExternalLinkTips')" placement="top">
                <i class="el-icon-question"></i>
                </el-tooltip>
                {{ $t("System.Menu.IsExternalLink") }}
              </span>
              <el-radio-group v-model="form.isFrame">
                <el-radio
                  v-for="dict in dict.type.YesOrNo"
                  :key="dict.value"
                  :label="dict.value"
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType == 'C'">
            <el-form-item prop="isCache">
              <span slot="label">
                <el-tooltip :content="$t('System.Menu.IsCacheTips')" placement="top">
                <i class="el-icon-question"></i>
                </el-tooltip>
                {{ $t("System.Menu.IsCache") }}
              </span>
              <el-radio-group v-model="form.isCache">
                <el-radio
                  v-for="dict in dict.type.YesOrNo"
                  :key="dict.value"
                  :label="dict.value"
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType != 'F'">
            <el-form-item prop="visible">
              <span slot="label">
                <el-tooltip :content="$t('System.Menu.VisibleTips')" placement="top">
                <i class="el-icon-question"></i>
                </el-tooltip>
                {{ $t("System.Menu.Visible") }}
              </span>
              <el-radio-group v-model="form.visible">
                <el-radio
                  v-for="dict in dict.type.YesOrNo"
                  :key="dict.value"
                  :label="dict.value"
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType != 'F'">
            <el-form-item prop="status">
              <span slot="label">
                <el-tooltip :content="$t('System.Menu.StatusTips')" placement="top">
                <i class="el-icon-question"></i>
                </el-tooltip>
                {{ $t("System.Menu.Status") }}
              </span>
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in dict.type.EnableOrDisable"
                  :key="dict.value"
                  :label="dict.value"
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMenu, getMenu, delMenu, addMenu, updateMenu, treeselect } from "@/api/system/menu";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
import IconSelect from "@/components/IconSelect";
import I18nEditor from '../../components/I18nFieldEditor';

export default {
  name: "SystemMenuIndex",
  dicts: ['YesOrNo', 'EnableOrDisable'],
  components: { Treeselect, IconSelect, I18nEditor },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 菜单表格树数据
      menuList: [],
      // 菜单树选项
      menuOptions: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否展开，默认全部折叠
      isExpandAll: false,
      // 重新渲染表格状态
      refreshTable: true,
      // 查询参数
      queryParams: {
        menuName: undefined,
        visible: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        menuName: [
          { required: true, message: this.$t("System.Menu.RuleTips.MenuName"), trigger: "blur" }
        ],
        orderNum: [
          { required: true, message: this.$t("System.Menu.RuleTips.Sort"), trigger: "blur" }
        ],
        path: [
          { required: true, message: this.$t("System.Menu.RuleTips.RouterLink"), trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    // 选择图标
    selected(name) {
      this.form.icon = name;
    },
    /** 查询菜单列表 */
    getList() {
      this.loading = true;
      listMenu(this.queryParams).then(response => {
        this.menuList = this.handleTree(response.data.rows, '0', "menuId");
        this.loading = false;
      });
    },
    /** 转换菜单数据结构 */
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children;
      }
      return {
        id: node.menuId,
        label: node.menuName,
        children: node.children
      };
    },
    /** 查询菜单下拉树结构 */
    getTreeselect() {
      treeselect().then(response => {
        this.menuOptions = [{ id: '0', parentId: '', label: this.$t('APP.TITLE'), isRoot: true, isDefaultExpanded: true, children: response.data }];
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        menuId: undefined,
        parentId: 0,
        menuName: undefined,
        icon: undefined,
        menuType: "M",
        orderNum: undefined,
        isFrame: "N",
        isCache: "Y",
        visible: "Y",
        status: "0"
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd(row) {
      this.reset();
      this.getTreeselect();
      if (row != null && row.menuId) {
        this.form.parentId = row.menuId;
      } else {
        this.form.parentId = 0;
      }
      this.open = true;
      this.title = this.$t('System.Menu.Dialog.Add');
    },
    /** 展开/折叠操作 */
    toggleExpandAll() {
      this.refreshTable = false;
      this.isExpandAll = !this.isExpandAll;
      this.$nextTick(() => {
        this.refreshTable = true;
      });
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      this.getTreeselect();
      getMenu(row.menuId).then(response => {
        this.form = response.data;
        this.open = true;
      this.title = this.$t('System.Menu.Dialog.Edit');
      });
    },
    /** 提交按钮 */
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.menuId != undefined) {
            updateMenu(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.Success'));
              this.open = false;
              this.getList();
            });
          } else {
            addMenu(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.Success'));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm(this.$t("System.Menu.ConfirmDelete", [ row.menuName ])).then(function() {
        return delMenu(row.menuId);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(() => {});
    }
  }
};
</script>
