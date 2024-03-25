<template>
  <div class="app-container">
    <el-row :gutter="24" class="mb12">
      <el-col :span="8">
        <el-row :gutter="10">
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
      </el-col>
      <el-col :span="16">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="mini"
          style="text-align:right"
          class="el-form-search">
          <el-form-item prop="name">
            <el-input :placeholder="$t('CMS.Resource.Name')" v-model="queryParams.name">
              <el-select v-model="queryParams.resourceType" slot="prepend" :placeholder="$t('CMS.Resource.Type')" style="width:80px;">
                <el-option
                  v-for="rt in resourceTypes"
                  :key="rt.id"
                  :label="rt.name"
                  :value="rt.id"
                />
              </el-select>
            </el-input>
          </el-form-item>
          <el-form-item :label="$t('Common.CreateTime')">
            <el-date-picker 
              v-model="dateRange"
              style="width: 240px"
              value-format="yyyy-MM-dd"
              type="daterange"
              range-separator="-"
              :start-placeholder="$t('Common.BeginDate')"
              :end-placeholder="$t('Common.EndDate')"
            ></el-date-picker>
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

    <el-table v-loading="loading" size="small" :data="resourceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="resourceId" align="center" width="180" />
      <el-table-column :label="$t('CMS.Resource.Type')" prop="resourceTypeName" width="80" />
      <el-table-column :label="$t('CMS.Resource.Name')" prop="name" align="left">
        <template slot-scope="scope">
          <svg-icon :icon-class="scope.row.iconClass" /> <el-link type="primary" target="_blank" :href="scope.row.src">{{ scope.row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Resource.StorageType')" prop="storageType" align="center" width="120" />
      <el-table-column :label="$t('CMS.Resource.FileSize')" prop="fileSizeName" align="center" width="120" />
      <el-table-column :label="$t('Common.CreateTime')" prop="createTime" align="center" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('Common.Operation')"
        align="center"
        width="180" 
        class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button 
            size="small"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)">{{ $t("Common.Edit") }}</el-button>
          <el-button 
            size="small"
            type="text"
            icon="el-icon-delete"
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

    <!-- 添加或修改资源对话框 -->
    <el-dialog 
      :title="title"
      :visible.sync="open"
      width="500px"
      append-to-body>
      <el-form 
        ref="form"
        :model="form"
        :rules="rules"
        label-width="80px">
        <el-form-item :label="$t('CMS.Resource.UploadResource')">
          <el-upload 
            ref="upload"
            drag
            :data="form"
            :action="upload.url"
            :headers="upload.headers"
            :file-list="upload.fileList"
            :on-progress="handleFileUploadProgress"
            :on-success="handleFileSuccess"
            :auto-upload="false"
            :before-upload="handleBeforeUpload"
            :on-change="handleUploadChange"
            :limit="1">
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">{{ $t('CMS.Resource.UploadTip1') }}</div>
              <div class="el-upload__tip" slot="tip">{{ $t('CMS.Resource.UploadTip2', [ '[.jpg, .png]',  '500kb']) }}</div>
            </el-upload>
        </el-form-item>
        <el-form-item :label="$t('CMS.Resource.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary" :loading="upload.isUploading" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getFileSvgIconClass } from "@/utils/chestnut";
import { getToken } from "@/utils/auth";
import { getResourceTypes, getResrouceList, getResourceDetail, delResource } from "@/api/contentcore/resource";

export default {
  name: "CmsResource",
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
      // 资源表格数据
      resourceList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      dateRange: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 15,
        resourceType: undefined,
        name: undefined,
        beginTime: undefined,
        endTime: undefined
      },
      resourceTypes: [],
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          { required: true, message: this.$t('CMS.Resource.RuleTips.Name'), trigger: "blur" }
        ]
      },
      // 上传参数
      upload: {
        // 是否禁用上传
        isUploading: false,
        // 设置上传的请求头部
        headers: { Authorization: "Bearer " + getToken(), CurrentSite: this.$cache.local.get("CurrentSite") },
        // 上传的地址
        url: process.env.VUE_APP_BASE_API + "/cms/resource",
        // 上传的文件列表
        fileList: [],
        data: {}
      },
    };
  },
  created () {
    this.loadResourceTypes();
    this.getList();
  },
  methods: {
    loadResourceTypes() {
      getResourceTypes().then(response => {
        this.resourceTypes = response.data;
      });
    },
    /** 查询资源列表 */
    getList () {
      this.loading = true;
      if (this.dateRange && this.dateRange.length == 2) {
        this.queryParams.beginTime = this.dateRange[0];
        this.queryParams.endTime = this.dateRange[1];
      }
      getResrouceList(this.queryParams).then(response => {
        this.resourceList = response.data.rows;
        this.resourceList.forEach(r => r.iconClass = getFileSvgIconClass(r.name))
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
      this.form = {
        name: "", 
        remark: ""
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery () {
      this.resetForm("queryForm");
      this.queryParams.resourceType = undefined;
      this.dateRange = [];
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.resourceId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd () {
      this.reset();
      this.open = true;
      this.title = this.$t('CMS.Resource.AddDialogTitle');
    },
    /** 修改按钮操作 */
    handleUpdate (row) {
      this.reset();
      const resourceId = row.resourceId || this.ids
      getResourceDetail(resourceId).then(response => {
        this.form = response.data;
        this.title = this.$t('CMS.Resource.EditDialogTitle');
        this.open = true;
      });
    },
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    handleFileSuccess(response, file, fileList) {
      this.upload.isUploading = false;
        this.$modal.msgSuccess(response.msg);
      if (response.code == 200) {
        this.open = false;
        this.getList();
      }
      this.$refs.upload.clearFiles();
      this.resetForm("form");
    },
    handleUploadChange(file) {
      file.name = file.name.toLowerCase();
      // if (!file.name.endsWith(".png") && !file.name.endsWith(".jpg")) {
      //   this.$modal.msgError(this.$t('CMS.Resource.FileTypeErrMsg'));
      //   this.upload.fileList = [];
      //   return;
      // }
      this.form.name = file.name;
    },
    handleBeforeUpload(file) {
      return true;
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.$refs.upload.submit();
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete (row) {
      const resourceIds = row.resourceId || this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function () {
        return delResource(resourceIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(function () { });
    }
  }
};
</script>