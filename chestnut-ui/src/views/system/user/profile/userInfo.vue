<template>
  <el-form ref="form" :model="user" :rules="rules" label-width="140px">
    <el-form-item :label="$t('AccountCenter.NickName')" prop="nickName">
      <el-input v-model="user.nickName" />
    </el-form-item> 
    <el-form-item :label="$t('AccountCenter.RealName')" prop="realName">
      <el-input v-model="user.realName" />
    </el-form-item> 
    <el-form-item :label="$t('AccountCenter.PhoneNumber')" prop="phoneNumber">
      <el-input v-model="user.phoneNumber" />
    </el-form-item>
    <el-form-item :label="$t('AccountCenter.Email')" prop="email">
      <el-input v-model="user.email" />
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
import { emailValidator, phoneNumberValidator } from '@/utils/validate';
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
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
          { max: 30, message: this.$t('Common.RuleTips.MaxLength', [ 30 ]), trigger: "change" }
        ],
        realName: [
          { max: 30, message: this.$t('Common.RuleTips.MaxLength', [ 30 ]), trigger: "change" }
        ],
        email: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
          { validator: emailValidator, trigger: "change" },
          { max: 50, message: this.$t('Common.RuleTips.MaxLength', [ 50 ]), trigger: "change" }
        ],
        phoneNumber: [
          { required: true, message: this.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
          { validator: phoneNumberValidator, trigger: "change" },
          { max: 20, message: this.$t('Common.RuleTips.MaxLength', [ 20 ]), trigger: "change" }
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
