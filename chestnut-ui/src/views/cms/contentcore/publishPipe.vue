<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button 
          plain
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate">{{ $t("Common.Edit") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="danger"
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
        </el-col>
    </el-row>

    <el-table v-loading="loading" :data="publishPipeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="publishpipeId" width="180" />
      <el-table-column :label="$t('CMS.PublishPipe.Name')" align="center" prop="name" />
      <el-table-column :label="$t('CMS.PublishPipe.Code')" align="center" width="80" prop="code" />
      <el-table-column :label="$t('Common.Sort')" align="center" width="80" prop="sort" />
      <el-table-column :label="$t('CMS.PublishPipe.Status')" align="center" prop="state">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.EnableOrDisable" :value="scope.row.state"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" width="180" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button 
            type="text"
            icon="el-icon-edit"
            size="small"
            @click="handleUpdate(scope.row)">{{ $t("Common.Edit") }}</el-button>
          <el-button 
            type="text"
            icon="el-icon-delete"
            size="small"
            @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改发布通道对话框 -->
    <el-dialog 
      :title="title"
      :visible.sync="open"
      width="500px"
      append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('CMS.PublishPipe.Name')" prop="name">
          <el-input v-model="form.name"/>
        </el-form-item>
        <el-form-item :label="$t('CMS.PublishPipe.Code')" prop="code">
          <el-input v-model="form.code" :disabled="form.publishpipeId&&form.publishpipeId.length>0" />
        </el-form-item>
        <el-form-item :label="$t('CMS.PublishPipe.Status')" prop="state">
          <el-radio-group v-model="form.state">
            <el-radio
              v-for="dict in dict.type.EnableOrDisable"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Sort')" prop="sort">
          <el-input-number v-model="form.sort" :min="1"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getPublishPipeList, getPublishPipeData, addPublishPipe, updatePublishPipe, delPublishPipe } from "@/api/contentcore/publishpipe";

export default {
  name: "CmsContentcorePublishPipe",
  dicts: ['EnableOrDisable'],
  data () {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 发布通道表格数据
      publishPipeList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: this.$t('CMS.PublishPipe.RuleTips.Name'), trigger: "blur" },
        ],
        code :[
          { required: true, pattern: "^[A-Za-z0-9_]+$", message: this.$t('CMS.PublishPipe.RuleTips.Code'), trigger: "blur" }
        ],
        state :[
          { required: true, message: this.$t('CMS.PublishPipe.RuleTips.Status'), trigger: "blur" }
        ],
        sort :[
          { required: true, message: this.$t('CMS.PublishPipe.RuleTips.Sort'), trigger: "blur" }
        ]
      }
    };
  },
  created () {
    this.getList();
  },
  methods: {
    getList () {
      this.loading = true;
      getPublishPipeList(this.queryParams).then(response => {
        this.publishPipeList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    // 取消按钮
    cancel () {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset () {
      this.resetForm("form");
      this.form = { state : "0" }
    },
    // 多选框选中数据
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.publishpipeId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd () {
      this.reset();
      this.open = true;
      this.title = this.$t('CMS.PublishPipe.AddDialogTitle');
    },
    /** 修改按钮操作 */
    handleUpdate (row) {
      this.reset();
      const publishpipeId = row.publishpipeId || this.ids
      getPublishPipeData(publishpipeId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = this.$t('CMS.PublishPipe.EditDialogTitle');
      });
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.publishpipeId != undefined) {
            updatePublishPipe(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
              this.open = false;
              this.getList();
            });
          } else {
            addPublishPipe(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.AddSuccess'));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete (row) {
      const publishpipeIds = row.publishpipeId ? [ row.publishpipeId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function () {
        return delPublishPipe(publishpipeIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(function () { });
    }
  }
};
</script>