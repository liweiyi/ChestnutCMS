<template>
   <el-form ref="pwdRef" :model="user" :rules="rules" label-width="150px">
      <el-form-item :label="$t('AccountCenter.OldPwd')" prop="oldPassword">
         <el-input v-model="user.oldPassword" :placeholder="$t('AccountCenter.OldPwdPlaceHolder')" style="width:240px" type="password" show-password />
      </el-form-item>
      <el-form-item :label="$t('AccountCenter.NewPwd')" prop="newPassword">
        <el-tooltip 
          v-if="ruleTips&&ruleTips.length>0" 
          class="item" 
          effect="dark" 
          placement="right"
          raw-content
          :content="ruleTips">
          <el-input v-model="user.newPassword" :placeholder="$t('AccountCenter.NewPwdPlaceHolder')" style="width:240px" type="password" show-password/>
        </el-tooltip>
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

<script setup name="ProfileResetPwd">
import { updateUserPwd, getUserProfile } from "@/api/system/user";
import { getSecurityConfigByType } from "@/api/system/security";
import pinyin from "@/utils/pinyin";

const { proxy } = getCurrentInstance()

const ruleTips = ref("")
const data = reactive({
  user: {
    oldPassword: undefined,
    newPassword: undefined,
    confirmPassword: undefined
  },
  userInfo: {},
  securityConfig: {}
})
const { user, userInfo, securityConfig } = toRefs(data)

const equalToPassword = (rule, value, callback) => {
  if (user.value.newPassword !== value) {
    callback(new Error(proxy.$t('AccountCenter.ConfirmPwdErrTip')));
  } else {
    callback()
  }
}

const validPasswordSensitive = (rule, value, callback) => {
  for(let i = 0; i < securityConfig.value.sensitives.length; i++) {
    let sensitiveType = securityConfig.value.sensitives[i];
    if ((sensitiveType == 'ACCOUNT' && user.value.newPassword.indexOf(userInfo.value.userName) > -1) // 用户名
      || (sensitiveType == 'PHONE_NUMBER' && proxy.$tools.isNotEmpty(userInfo.value.phoneNumber) && user.value.newPassword.indexOf(userInfo.value.phoneNumber) > -1) // 手机号
      || (sensitiveType == 'EMAIL' && proxy.$tools.isNotEmpty(userInfo.value.email) && user.value.newPassword.indexOf(userInfo.value.email.substring(0, userInfo.value.email.indexOf('@'))) > -1) // 邮箱名，不带@xx后缀
      || (sensitiveType == 'NICK_NAME' && user.value.newPassword.indexOf(pinyin.fullSpelling(userInfo.value.nickName)) > -1) // 昵称全拼
      || (sensitiveType == 'REAL_NAME' && proxy.$tools.isNotEmpty(userInfo.value.realName) && user.value.newPassword.indexOf(pinyin.fullSpelling(userInfo.value.realName)) > -1) // 真实姓名全拼
      || (sensitiveType == 'BIRTHDAY' && proxy.$tools.isNotEmpty(userInfo.value.birthday) && user.value.newPassword.indexOf(parseTime(userInfo.value.birthday)) > -1) // 生日，格式：yyyyMMdd
    ) {
      callback(new Error(proxy.$t('AccountCenter.NewPwdSensitiveTip')));
      return;
    }
  }
  callback();
}


const rules = ref({
  oldPassword: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  newPassword: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { required: true, validator: equalToPassword, trigger: "blur" }
  ]
})

function loadUserProfile() {
  getUserProfile().then(res => {
    userInfo.value = res.data.user;
  })
}

function loadSecurityConfig() {
  getSecurityConfigByType(['Password']).then(res => {
    if (res.data) {
      securityConfig.value = res.data.Password;
      ruleTips.value = "# " + proxy.$t('AccountCenter.NewPwdLenTip', [ securityConfig.value.minLength, securityConfig.value.maxLength ]);
      rules.value.newPassword.push({ 
        min: securityConfig.value.minLength, 
        max: securityConfig.value.maxLength, 
        message: proxy.$t('AccountCenter.NewPwdLenTip', [ securityConfig.value.minLength, securityConfig.value.maxLength ]), 
        trigger: "blur" 
      });
      if (securityConfig.value.rule != '0') {
        ruleTips.value += "<br/># " + proxy.$t('AccountCenter.NewPwdRule' + securityConfig.value.rule + "Tip");
        rules.value.newPassword.push({
          pattern: securityConfig.value.rulePattern,
          message: proxy.$t('AccountCenter.NewPwdRule' + securityConfig.value.rule + "Tip"), 
          trigger: "blur"
        });
      }
      if (securityConfig.value.sensitives && securityConfig.value.sensitives.length > 0) {
        ruleTips.value += "<br/># " + proxy.$t('AccountCenter.NewPwdSensitiveTip');
        rules.value.newPassword.push({ required: true, validator: validPasswordSensitive, trigger: "blur" });
      }
      console.log(ruleTips.value);
      console.log(rules.value);
    }
  })
}

function submit() {
  proxy.$refs.pwdRef.validate(valid => {
    if (valid) {
      updateUserPwd(user.value.oldPassword, user.value.newPassword).then(response => {
        proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'))
      })
    }
  })
}

function close() {
  proxy.$tab.closePage()
}

loadUserProfile()
loadSecurityConfig()
</script>
