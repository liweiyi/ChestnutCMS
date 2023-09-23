<template>
  <el-form ref="form" :model="user" :rules="rules" label-width="140px">
    <el-form-item :label="$t('AccountCenter.NickName')" prop="nickName">
      <el-input v-model="user.nickName" maxlength="30" />
    </el-form-item> 
    <el-form-item :label="$t('AccountCenter.PhoneNumber')" prop="phoneNumber">
      <el-input v-model="user.phoneNumber" maxlength="11" />
    </el-form-item>
    <el-form-item :label="$t('AccountCenter.Email')" prop="email">
      <el-input v-model="user.email" maxlength="50" />
    </el-form-item>
    <el-form-item :label="$t('AccountCenter.Gender')">
      <el-radio-group v-model="user.sex">
        <el-radio label="0">{{ $t('AccountCenter.GenderMale') }}</el-radio>
        <el-radio label="1">{{ $t('AccountCenter.GenderFemale') }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submit">{{ $t('Common.Save') }}</el-button>
      <el-button type="danger" @click="close">{{ $t('Common.Close') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import { updateUserProfile } from "@/api/system/user";

export default {
  props: {
    user: {
      type: Object
    }
  },
  data() {
    return {
      // 表单校验
      rules: {
        nickName: [
          { required: true, message: this.$t('AccountCenter.AccountEmptyTip'), trigger: "blur" }
        ],
        email: [
          { required: true, message: this.$t('AccountCenter.EmailEmptyTip'), trigger: "blur" },
          {
            type: "email",
            message: this.$t('AccountCenter.EmailRuleTip'),
            trigger: ["blur", "change"]
          }
        ],
        phoneNumber: [
          { required: true, message: this.$t('AccountCenter.PhoneNumEmptyTip'), trigger: "blur" },
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: this.$t('AccountCenter.PhoneNumRuleTip'),
            trigger: "blur"
          }
        ]
      }
    };
  },
  methods: {
    submit() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          updateUserProfile(this.user).then(response => {
            this.$modal.msgSuccess(response.msg);
          });
        }
      });
    },
    close() {
      this.$tab.closePage();
    }
  }
};
</script>
