<template>
  <!-- 会员管理页 -->
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
        <el-form-item prop="userName">
          <el-input
            v-model="queryParams.userName"
            clearable
            :placeholder="$t('Member.UserName')"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="nickName">
          <el-input
            v-model="queryParams.nickName"
            clearable
            :placeholder="$t('Member.NickName')"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="queryParams.email"
            clearable
            placeholder="Email"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="phoneNumber">
          <el-input
            v-model="queryParams.phoneNumber"
            clearable
            :placeholder="$t('Member.PhoneNumber')"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="status">
          <el-select 
            v-model="queryParams.status"
            clearable
            :placeholder="$t('Member.Status')"
            style="width: 110px">
            <el-option 
              v-for="dict in dict.type.MemberStatus"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value" />
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

    <el-table v-loading="loading"
              :data="dataList"
              @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="55"
        align="center" />
      <el-table-column 
        :label="$t('Member.UID')"
        align="center"
        :show-overflow-tooltip="true"
        width="100"
        prop="memberId" />
      <el-table-column 
        :label="$t('Member.UserName')"
        align="center"
        :show-overflow-tooltip="true"
        prop="userName" />
      <el-table-column 
        :label="$t('Member.NickName')"
        align="center"
        :show-overflow-tooltip="true"
        prop="nickName" />
      <el-table-column 
        label="Email"
        align="center"
        :show-overflow-tooltip="true"
        prop="email" />
      <el-table-column 
        :label="$t('Member.PhoneNumber')"
        align="center"
        :show-overflow-tooltip="true"
        prop="phoneNumber" />
      <el-table-column :label="$t('Member.Status')" align="center" prop="status" width="80">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.MemberStatus" :value="scope.row.status"/>
          </template>
      </el-table-column>
      <el-table-column 
        :label="$t('Member.Source')"
        align="center"
        width="100"
        :show-overflow-tooltip="true"
        prop="sourceType" />
      <el-table-column 
        :label="$t('Member.RegistTime')"
        align="center"
        prop="createTime"
        width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column 
        :label="$t('Member.LastLoginTime')"
        align="center"
        prop="createTime"
        width="230">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastLoginTime) }} - {{ scope.row.lastLoginIp }}</span>
        </template>
      </el-table-column>
      <el-table-column  
        :label="$t('Common.Operation')"
        align="center"
        width="220" 
        class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button 
            size="small"
            type="text"
            icon="el-icon-key"
            @click="handleResetPwd(scope.row)">{{ $t('Member.ResetPwd') }}</el-button>
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


    <!-- 添加或修改用户配置对话框 -->
    <el-dialog 
      :title="title" 
      :visible.sync="open"
      :close-on-click-modal="false" 
      width="600px" 
      append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item v-if="form.memberId == undefined" :label="$t('Member.UserName')" prop="userName">
          <el-input v-model="form.userName" :maxlength="30" />
        </el-form-item>
        <el-form-item :label="$t('Member.NickName')" prop="nickName">
          <el-input v-model="form.nickName" :maxlength="30" />
        </el-form-item>
        <el-form-item v-if="form.memberId == undefined" :label="$t('Member.Password')" prop="password">
          <el-input v-model="form.password" type="password" :maxlength="32" show-password/>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" :maxlength="30" />
        </el-form-item>
        <el-form-item :label="$t('Member.PhoneNumber')" prop="phoneNumber">
          <el-input v-model="form.phoneNumber" :maxlength="11" />
        </el-form-item>
        <el-form-item :label="$t('Member.Birthday')" prop="birthday">
          <el-date-picker v-model="form.birthday" format="yyyy-MM-dd" value-format="yyyy-MM-dd HH:mm:ss" type="date"></el-date-picker>
        </el-form-item>
        <el-form-item :label="$t('Member.Status')">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.MemberStatus"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleSubmitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="handleCancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { isBlank, validEmail, validPhoneNumber } from '@/utils/validate';
import { getMemberList, getMemberDetail, addMember, updateMember, deleteMembers, resetMemberPassword } from "@/api/member/member";

export default {
  name: "MemberMemberList",
  dicts: [ 'MemberStatus' ],
  data () {
    const validateMember = (rule, value, callback) => {
        if (isBlank(this.form.userName) && isBlank(this.form.phoneNumber) && isBlank(this.form.email)) {
          callback(new Error(this.$t('Member.RuleTips.Member')));
        } else {
          callback();
        }
      };
    const emailValidator = (rule, value, callback) => {
        if (!isBlank(value) && !validEmail(value)) {
          callback(new Error(this.$t('Common.RuleTips.Email')));
        } else {
          callback();
        }
      };
    const phoneNumberValidator = (rule, value, callback) => {
        if (!isBlank(value) && !validPhoneNumber(value)) {
          callback(new Error(this.$t('Common.RuleTips.PhoneNumber')));
        } else {
          callback();
        }
      };
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
      // 表单参数
      form: {
        status: '0'
      },
      // 表单校验
      rules: {
        userName: [,
          {
            pattern: /^[A-Za-z][A-Za-z0-9_]+$/,
            message: this.$t('Member.RuleTips.UserName'),
            trigger: "blur"
          },
          { validator: validateMember }
        ],
        password: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
        email: [
          { validator: emailValidator, trigger: "blur" },
          { validator: validateMember, trigger: "blur" }
        ],
        phoneNumber: [
          { validator: phoneNumberValidator, trigger: "blur" },
          { validator: validateMember, trigger: "blur" }
        ],
        status: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
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
      getMemberList(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
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
      this.ids = selection.map(item => item.memberId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    reset () {
      this.resetForm("form");
      this.form = { status: '0' };
    },
    handleAdd () {
      this.reset();
      this.open = true;
      this.title = this.$t('Member.AddTitle');
    },
    handleUpdate (row) {
      this.reset();
      const memberId = row.memberId ? row.memberId : this.ids[0];
      getMemberDetail(memberId).then(response => {
        this.form = response.data;
        this.open = true;
      this.title = this.$t('Member.EditTitle');
      });
    },
    handleCancel () {
      this.open = false;
      this.reset();
    },
    handleSubmitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.memberId != undefined) {
            updateMember(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.SaveSuccess'));
              this.open = false;
              this.getList();
            });
          } else {
            addMember(this.form).then(response => {
              this.$modal.msgSuccess(this.$t('Common.AddSuccess'));
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete (row) {
      const memberIds = row.memberId ? [ row.memberId ] : this.ids;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function () {
        return deleteMembers(memberIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(function () { });
    },
    handleResetPwd(row) {
      this.$prompt(this.$t('Member.InputPwd', [ row.userName ]), this.$t('Common.Tips'), {
        confirmButtonText: this.$t('Common.Confirm'),
        cancelButtonText: this.$t('Common.Cancel'),
        closeOnClickModal: false
      }).then(({ value }) => {
          resetMemberPassword(row.memberId, value).then(response => {
            this.$modal.msgSuccess(this.$t('Member.RestPwdSuccess', [ value ]));
          });
        }).catch(() => {});
    },
  }
};
</script>