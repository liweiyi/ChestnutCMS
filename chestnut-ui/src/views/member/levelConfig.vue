<template>
  <!-- 会员等级配置页 -->
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
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true">
        <el-form-item prop="levelType">
          <el-select
            v-model="queryParams.levelType"
            style="width: 160px"
            clearable
            :placeholder="$t('Member.LevelType')"
            @keyup.enter.native="handleQuery">
            <el-option
              v-for="lt in levelTypes"
              :key="lt.id"
              :label="lt.name"
              :value="lt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>
    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="55"
        align="center" />
      <el-table-column 
        :label="$t('Member.Level.Type')"
        align="center"
        prop="levelTypeName" />
      <el-table-column 
        :label="$t('Member.Level.Level')"
        align="center">
        <template slot-scope="scope">
          Lv{{ scope.row.level }}
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('Member.Level.Name')"
        align="center"
        prop="name" />
      <el-table-column 
        :label="$t('Member.Level.NextNeedExp')"
        align="center"
        prop="nextNeedExp" />
      <el-table-column 
        :label="$t('Common.Remark')"
        align="center"
        prop="remark" />
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

    <!-- 添加或修改对话框 -->
    <el-dialog 
      :title="title"
      :visible.sync="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form 
        ref="form"
        :model="form"
        :rules="rules"
        label-width="120px">
        <el-form-item :label="$t('Member.LevelType')" prop="levelType">
          <el-select
            v-model="form.levelType"
            :disabled="form.configId!=undefined&&form.configId!=0"
            style="width:100%">
            <el-option
              v-for="lt in levelTypes"
              :key="lt.id"
              :label="lt.name"
              :value="lt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('Member.Level.Level')" prop="level">
          <el-input-number v-model="form.level" :min="0" :disabled="form.configId!=undefined&&form.configId!=0" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('Member.Level.Name')" prop="name">
          <el-input v-model="form.name" style="width:100%"></el-input>
        </el-form-item>
        <el-form-item :label="$t('Member.Level.NextNeedExp')" prop="nextNeedExp">
          <el-input-number v-model="form.nextNeedExp" :min="0" style="width:100%"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleSubmitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getLevelTypes, getLevelConfigList, getLevelConfigDetail, addLevelConfig, updateLevelConfig, deleteLevelConfigs } from "@/api/member/levelConfig";

export default {
  name: "MemberLevelConfig",
  data () {
    return {
      // 遮罩层
      loading: true,
      showSearch: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 发布通道表格数据
      dataList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      levelTypes: [],
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        levelType: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        level: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        name: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
        ],
        nextNeedExp: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
        ]
      }
    };
  },
  created () {
    this.getList();
    this.loadLevelTypes();
  },
  methods: {
    getList () {
      this.loading = true;
      getLevelConfigList(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    loadLevelTypes() {
      getLevelTypes().then(res => this.levelTypes = res.data)
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.configId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    reset () {
      this.resetForm("form");
      this.form = { exp : 0, dayLimit: 0, totalLimit: 0 }
    },
    handleAdd () {
      this.reset();
      this.open = true;
      this.title = this.$t('Member.Level.AddTitle');
    },
    handleUpdate (row) {
      this.reset();
      const configId = row.configId ? row.configId : this.ids[0];
      getLevelConfigDetail(configId).then(response => {
        this.form = response.data;
        this.open = true;
      this.title = this.$t('Member.Level.EditTitle');
      });
    },
    handleCancel () {
      this.open = false;
      this.reset();
    },
    handleSubmitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.configId != undefined) {
            updateLevelConfig(this.form).then(response => {
              this.$modal.msgSuccess(ths.$t('Common.SaveSuccess'));
              this.open = false;
              this.getList();
            });
          } else {
            addLevelConfig(this.form).then(response => {
              this.$modal.msgSuccess(ths.$t('Common.AddSuccess'));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete (row) {
      const configIds = row.configId ? [ row.configId ] : this.ids;
      this.$modal.confirm(ths.$t('Common.ConfirmDelete')).then(function () {
        return deleteLevelConfigs(configIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(ths.$t('Common.DeleteSuccess'));
      }).catch(function () { });
    }
  }
};
</script>