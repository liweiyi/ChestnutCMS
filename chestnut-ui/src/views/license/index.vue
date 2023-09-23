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
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" class="el-form-search mb12" :inline="true">
        <el-form-item label="授权描述" prop="description">
          <el-input
            v-model="queryParams.description"
            clearable
            style="width: 240px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="el-icon-search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table v-loading="loading" :data="licenseList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="licenseId" />
      <el-table-column label="授权产品" align="center" prop="subject" :show-overflow-tooltip="true" />
      <el-table-column label="生效时间" align="center" prop="issuedTime" :show-overflow-tooltip="true" />
      <el-table-column label="过期时间" align="center" prop="expiryTime" :show-overflow-tooltip="true" />
      <el-table-column label="授权用户类型" align="center" prop="consumerType" />
      <el-table-column label="授权用户数量" align="center" prop="consumerAmount" />
      <el-table-column label="描述" align="center" prop="description" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
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
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="授权产品" prop="subject">
          <el-input v-model="form.subject" />
        </el-form-item>
        <el-form-item label="生效时间" prop="onlineDate">
          <el-date-picker v-model="form.issuedTime" value-format="yyyy-MM-dd" type="date" />
        </el-form-item>
        <el-form-item label="过期时间" prop="offlineDate">
          <el-date-picker v-model="form.expiryTime" value-format="yyyy-MM-dd" type="date" />
        </el-form-item>
        <el-form-item label="授权用户类型" prop="consumerType">
          <el-input v-model="form.consumerType" />
        </el-form-item>
        <el-form-item label="授权用户数量" prop="consumerAmount">
          <el-input-number v-model="form.consumerAmount" :min="0" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="是否IP授权" prop="checkIp">
          <el-switch
            v-model="form.checkIp"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item label="授权IP" prop="ipAddress">
          <el-input v-model="form.ipAddress" />
        </el-form-item>
        <el-form-item label="是否MAC授权" prop="checkMac">
          <el-switch
            v-model="form.checkMac"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item label="授权MAC" prop="macAddress">
          <el-input v-model="form.macAddress" />
        </el-form-item>
        <el-form-item label="是否CPU授权" prop="checkCpu">
          <el-switch
            v-model="form.checkCpu"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item label="授权CPU序列号" prop="cpuSerial">
          <el-input v-model="form.cpuSerial" />
        </el-form-item>
        <el-form-item label="是否主板授权" prop="checkMainBoard">
          <el-switch
            v-model="form.checkMainBoard"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item label="授权主板序列号" prop="mainBoardSerial">
          <el-input v-model="form.mainBoardSerial" />
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
import { listLicenses, addLicense, delLicense } from "@/api/license/license";

export default {
  name: "License",
  dicts: [ 'YesOrNo' ],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      licenseList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        description: undefined
      },
      form: {},
      rules: {
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listLicenses(this.queryParams).then(response => {
          this.licenseList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      );
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加License授权";
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.configId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    },
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          addLicense(this.form).then(response => {
            this.$modal.msgSuccess(this.$t("Common.AddSuccess"));
            this.open = false;
            this.getList();
          });
        }
      });
    },
    handleDelete(row) {
      const licenseIds = row.licenseId ? [ row.licenseId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
          return delLicense(licenseIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess(this.$t("Common.DeleteSuccess"));
        }).catch(() => {});
    },
    handleDownloadLicense() {
      this.download('license/export', {
        ...this.queryParams
      }, `license.lic`)
    },
  }
};
</script>
