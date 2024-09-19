<template>
  <div class="app-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button 
              type="primary"
              icon="el-icon-plus"
              size="mini"
              plain
              v-hasPermi="['cms:friendlink:add']"
              @click="handleAdd">{{ $t("Common.Add") }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              type="success"
              icon="el-icon-edit"
              size="mini"
              plain
              :disabled="single"
              v-hasPermi="[ 'cms:friendlink:add', 'cms:friendlink:edit' ]"
              @click="handleEdit">{{ $t("Common.Edit") }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              type="danger"
              icon="el-icon-delete"
              size="mini"
              plain
              :disabled="multiple"
              v-hasPermi="['cms:friendlink:delete']"
              @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="mini"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input v-model="queryParams.query" :placeholder="$t('CMS.FriendLink.Placeholder.GroupQuery')">
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="el-icon-search"
                @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button 
                icon="el-icon-refresh"
                @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <el-row>
      <el-col>
        <el-table v-loading="loading"
                  :data="linkGroupList"
                  @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="50" />
          <el-table-column :label="$t('CMS.FriendLink.GroupName')" align="left" prop="name">
            <template slot-scope="scope">
              <el-button type="text" @click="handleGroupClick(scope.row)">{{ scope.row.name }}</el-button>
            </template>
          </el-table-column>
          <el-table-column :label="$t('CMS.FriendLink.GroupCode')" align="left" prop="code"/>
          <el-table-column :label="$t('Common.UpdateTime')" align="center" width="160">
            <template slot-scope="scope">
              <span v-if="scope.row.updateTime!=null">{{ parseTime(scope.row.updateTime) }}</span>
              <span v-else>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('Common.Operation')" align="center" width="180" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <span class="btn-cell-wrap">
                <el-button 
                  type="text"
                  icon="el-icon-edit"
                  size="small"
                  v-hasPermi="[ 'cms:friendlink:add', 'cms:friendlink:edit' ]"
                  @click="handleEdit(scope.row)">{{ $t('Common.Edit') }}</el-button>
              </span>
              <span class="btn-cell-wrap">
                <el-button 
                  type="text"
                  icon="el-icon-delete"
                  size="small"
                  v-hasPermi="[ 'cms:friendlink:delete' ]"
                  @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-col> 
    </el-row>
    <!-- 添加或修改弹窗 -->
    <el-dialog 
      :title="title"
      :visible.sync="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('CMS.FriendLink.GroupName')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.FriendLink.GroupCode')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item v-if="form.linkGroupId" :label="$t('CMS.FriendLink.SortFlag')" prop="sortFlag">
          <el-input-number v-model="form.sortFlag" controls-position="right" :min="0" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
</style>
<script>
import { codeValidator } from '@/utils/validate'
import { getLinkGroupList, addLinkGroup, editLinkGroup, deleteLinkGroup } from "@/api/link/linkGroup";

export default {
  name: "CmsLinkLinkGroup",
  data () {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      selectedRows: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 资源表格数据
      linkGroupList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        query: undefined,
        pageSize: 20,
        pageNo: 1
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        code: [
          { required: true, validator: codeValidator, trigger: "blur" }
        ]
      }
    };
  },
  created () {
    this.loadListData();
  },
  methods: {
    loadListData () {
      this.loading = true;
      getLinkGroupList(this.queryParams).then(response => {
        this.linkGroupList = response.data.rows;
        this.loading = false;
      });
    },
    cancel () {
      this.open = false;
      this.reset();
    },
    reset () {
      this.form = {};
    },
    handleQuery () {
      this.queryParams.pageNo = 1;
      this.loadListData();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange (selection) {
      this.selectedRows = selection.map(item => item);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    handleAdd () {
      this.reset();
      this.title = this.$t('CMS.FriendLink.AddTitle')
      this.open = true;
    },
    handleEdit (row) {
      this.reset();
      this.title = this.$t('CMS.FriendLink.EditTitle')
      this.form = row;
      this.open = true;
    },
    /** 提交按钮 */
    submitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.linkGroupId) {
            editLinkGroup(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.open = false;
              this.loadListData();
            }); 
          } else {
            addLinkGroup(this.form).then(response => {
              this.$modal.msgSuccess(response.msg);
              this.open = false;
              this.loadListData();
            }); 
          }
        }
      });
    },
    handleDelete (row) {
      const rows = row.linkGroupId ? [ row ] : this.selectedRows
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return deleteLinkGroup(rows);
      }).then((response) => {
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
        this.loadListData();
      }).catch(() => {});
    },
    handleGroupClick(row) {
      this.$router.push({ 
        path: "/operations/link/list", 
        query: { 
          groupId: row.linkGroupId
        } 
      });
    }
  }
};
</script>