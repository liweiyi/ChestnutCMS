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
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="small"
          :disabled="single"
          @click="handleUpdate"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <right-toolbar :search="false" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="configId" width="140" />
      <el-table-column label="AppId" align="left" prop="appId" />
      <el-table-column label="AppSecret" align="left" prop="appSecret" />
      <el-table-column label="Token" align="left" width="180" prop="token" />
      <el-table-column label="EncodingAESKey" align="left" width="180" prop="encodingAesKey" />
      <el-table-column :label="$t('System.WeChat.Backend')" align="center" prop="backend" width="90">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.backend"
            active-value="Y"
            inactive-value="N"
            @change="handleStatusChange(scope.row)"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width"  width="140">
        <template slot-scope="scope">
          <el-button
            size="small"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            size="small"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >{{ $t('Common.Delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" v-loading="loading" :rules="rules" label-width="140px">
        <el-form-item label="AppId" prop="appId">
          <el-input v-model="form.appId"></el-input>
        </el-form-item>
        <el-form-item label="AppSecret" prop="appSecret">
          <el-input v-model="form.appSecret"></el-input>
        </el-form-item>
        <el-form-item label="Token" prop="token">
          <el-input v-model="form.token"></el-input>
        </el-form-item>
        <el-form-item label="EncodingAESKey" prop="encodingAesKey">
          <el-input v-model="form.encodingAesKey"></el-input>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { listWeChatConfigs, getWeChatConfig, addWeChatConfig, saveWeChatConfig, deleteWeChatConfig, changeConfigStatus } from "@/api/system/wechat";

export default {
  name: "SysWeChatConfig",
  dicts: [ "YesOrNo" ],
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
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 参数表格数据
      configList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      form: {},
      // 表单校验
      rules: {
        appId: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        appSecret: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        token: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList () {
      this.loading = true;
      listWeChatConfigs().then(response => {
        this.configList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        backend: 'N'
      };
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = this.$t('System.WeChat.AddTitle');
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.configId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    handleUpdate(row) {
      this.reset();
      const configId = row.configId || this.ids
      getWeChatConfig(configId).then(response => {
        this.form = response.data;
        this.open = true;
      this.title = this.$t('System.WeChat.EditTitle');
      });
    },
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.configId != undefined) {
            saveWeChatConfig(this.form).then(res => {
              this.$modal.msgSuccess(this.$t("Common.Success"));
              this.open = false;
              this.getList();
            });
          } else {
            addWeChatConfig(this.form).then(res => {
              this.$modal.msgSuccess(this.$t("Common.AddSuccess"));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const configIds = row.configId ? [ row.configId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
          return deleteWeChatConfig(configIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess(this.$t("Common.DeleteSuccess"));
        }).catch(() => {});
    },
    handleStatusChange (row) {
      const configId = row.configId;
      changeConfigStatus(configId).then(res => {
        this.$modal.msgSuccess(this.$t("Common.Success"));
        this.getList();
      });
    }
  }
};
</script>