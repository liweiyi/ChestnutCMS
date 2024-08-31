<template>
  <el-form ref="form" :model="user" :rules="rules" label-width="150px">
    <el-form-item :label="$t('AccountCenter.OldPwd')" prop="oldPassword">
      <el-input v-model="user.oldPassword" :placeholder="$t('AccountCenter.OldPwdPlaceHolder')" style="width:240px" type="password" show-password/>
    </el-form-item>
    <el-form-item :label="$t('AccountCenter.NewPwd')" prop="newPassword">
      <el-tooltip v-if="passwordRuleTips&&passwordRuleTips.length>0" class="item" effect="dark" placement="right">
        <el-input v-model="user.newPassword" :placeholder="$t('AccountCenter.NewPwdPlaceHolder')" style="width:240px" type="password" show-password/>
        <div slot="content" v-html="passwordRuleTips"></div>
      </el-tooltip>
      <el-input v-else v-model="user.newPassword" :placeholder="$t('AccountCenter.NewPwdPlaceHolder')" style="width:240px" type="password" show-password/>
    </el-form-item>
    <el-form-item :label="$t('AccountCenter.ConfirmPwd')" prop="confirmPassword">
      <el-input v-model="user.confirmPassword" :placeholder="$t('AccountCenter.ConfirmPwdPlaceHolder')" style="width:240px" type="password" show-password/>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submit">{{ $t('Common.Save') }}</el-button>
      <el-button type="danger" @click="close">{{ $t('Common.Close') }}</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import { updateUserPwd, getUserProfile } from "@/api/system/user";
import { currentSecurityConfig } from "@/api/system/security";
import pinyin from "@/utils/pinyin";

export default {
  dicts: ['SecurityPasswordRule'],
  data() {
    const equalToPassword = (rule, value, callback) => {
      if (this.user.newPassword !== value) {
        callback(new Error(this.$t('AccountCenter.ConfirmPwdErrTip')));
      } else {
        callback();
      }
    };
    return {
      user: {
        oldPassword: undefined,
        newPassword: undefined,
        confirmPassword: undefined
      },
      userInfo: undefined,
      securityConfig: undefined,
      passwordRuleTips: undefined,
      // 表单校验
      rules: {
        oldPassword: [
          { required: true, message: this.$t('AccountCenter.OldPwdEmptyTip'), trigger: "blur" }
        ],
        newPassword: [
          { required: true, message: this.$t('AccountCenter.NewPwdEmptyTip'), trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, message: this.$t('AccountCenter.ConfirmPwdEmptyTip'), trigger: "blur" },
          { required: true, validator: equalToPassword, trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.loadUserProfile();
    this.loadSecurityConfig();
  },
  methods: {
    loadUserProfile() {
      getUserProfile().then(res => {
        this.userInfo = res.data.user;
      }); 
    },
    loadSecurityConfig() {
      currentSecurityConfig().then(res => {
        if (res.data) {
          this.securityConfig = res.data;
          this.passwordRuleTips = "# " + this.$t('AccountCenter.NewPwdLenTip', [ this.securityConfig.passwordLenMin, this.securityConfig.passwordLenMax ]);
          this.rules.newPassword.push({ 
            min: this.securityConfig.passwordLenMin, 
            max: this.securityConfig.passwordLenMax, 
            message: this.$t('AccountCenter.NewPwdLenTip', [ this.securityConfig.passwordLenMin, this.securityConfig.passwordLenMax ]), 
            trigger: "blur" 
          });
          if (this.securityConfig.passwordRule != '0') {
            this.passwordRuleTips += "<br/># " + this.$t('AccountCenter.NewPwdRule' + this.securityConfig.passwordRule + "Tip");
            this.rules.newPassword.push({
              pattern: this.securityConfig.passwordRulePattern,
              message: this.$t('AccountCenter.NewPwdRule' + this.securityConfig.passwordRule + "Tip"), 
              trigger: "blur"
            });
          }
          if (this.securityConfig.passwordSensitive && this.securityConfig.passwordSensitive.length > 0) {
            this.passwordRuleTips += "<br/># " + this.$t('AccountCenter.NewPwdSensitiveTip');
            this.rules.newPassword.push({ required: true, validator: this.validPasswordSensitive, trigger: "blur" });
          }
        }
      });
    },
    validPasswordSensitive (rule, value, callback) {
      for(var i = 0; i < this.securityConfig.passwordSensitive.length; i++) {
        var sensitiveType = this.securityConfig.passwordSensitive[i];
        if ((sensitiveType == 'ACCOUNT' && this.user.newPassword.indexOf(this.userInfo.userName) > -1) // 用户名
          || (sensitiveType == 'PHONE_NUMBER' && this.user.newPassword.indexOf(this.userInfo.phoneNumber) > -1) // 手机号
          || (sensitiveType == 'EMAIL' && this.user.newPassword.indexOf(this.userInfo.email.substring(0, this.userInfo.email.indexOf('@'))) > -1) // 邮箱名，不带@xx后缀
          || (sensitiveType == 'NICK_NAME' && this.user.newPassword.indexOf(pinyin.fullSpelling(this.userInfo.nickName)) > -1) // 昵称全拼
          || (sensitiveType == 'REAL_NAME' && this.user.newPassword.indexOf(pinyin.fullSpelling(this.userInfo.realName)) > -1) // 真实姓名全拼
          || (sensitiveType == 'BIRTHDAY' && this.user.newPassword.indexOf(parseTime(this.userInfo.birthday)) > -1) // 生日，格式：yyyyMMdd
        ) {
          callback(new Error(this.$t('AccountCenter.NewPwdSensitiveTip')));
          return;
        }
      }
      callback();
    },
    getDictLable(dictType, dictValue) {
      return this.dict.type[dictType].find(dict => dict.value == dictValue);
    },
    submit() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          updateUserPwd(this.user.oldPassword, this.user.newPassword).then(response => {
            this.$modal.msgSuccess("修改成功");
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
