<template>
   <el-form ref="userRef" :model="form" :rules="rules" label-width="140px">
      <el-form-item :label="$t('AccountCenter.NickName')" prop="nickName">
         <el-input v-model="form.nickName" maxlength="30" />
      </el-form-item>
      <el-form-item :label="$t('AccountCenter.PhoneNumber')" prop="phonenumber">
         <el-input v-model="form.phoneNumber" maxlength="11" />
      </el-form-item>
      <el-form-item :label="$t('AccountCenter.Email')" prop="email">
         <el-input v-model="form.email" maxlength="50" />
      </el-form-item>
      <el-form-item :label="$t('AccountCenter.Gender')">
         <el-radio-group v-model="form.sex">
            <el-radio value="0">{{ $t('AccountCenter.GenderMale') }}</el-radio>
            <el-radio value="1">{{ $t('AccountCenter.GenderFemale') }}</el-radio>
         </el-radio-group>
      </el-form-item>
      <el-form-item>
      <el-button type="primary" @click="submit">{{ $t('Common.Save') }}</el-button>
      <el-button type="danger" @click="close">{{ $t('Common.Close') }}</el-button>
      </el-form-item>
   </el-form>
</template>

<script setup>
import { emailValidator, phoneNumberValidator } from '@/utils/validate';
import { updateUserProfile } from "@/api/system/user"

const props = defineProps({
  user: {
    type: Object
  }
})

const { proxy } = getCurrentInstance()

const form = ref({})
const rules = ref({
  nickName: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { max: 30, message: proxy.$t('Common.RuleTips.MaxLength', [ 30 ]), trigger: "change" }
  ],
  realName: [
    { max: 30, message: proxy.$t('Common.RuleTips.MaxLength', [ 30 ]), trigger: "change" }
  ],
  email: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { validator: emailValidator, trigger: "change" },
    { max: 50, message: proxy.$t('Common.RuleTips.MaxLength', [ 50 ]), trigger: "change" }
  ],
  phoneNumber: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { validator: phoneNumberValidator, trigger: "change" },
    { max: 20, message: proxy.$t('Common.RuleTips.MaxLength', [ 20 ]), trigger: "change" }
  ]
})

function submit() {
  proxy.$refs.userRef.validate(valid => {
    if (valid) {
      updateUserProfile(form.value).then(response => {
        proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'))
        props.user.phoneNumber = form.value.phoneNumber
        props.user.email = form.value.email
      })
    }
  })
}

function close() {
  proxy.$tab.closePage()
}

watch(() => props.user, user => {
  if (user) {
    form.value = { nickName: user.nickName, phoneNumber: user.phoneNumber, email: user.email, sex: user.sex }
  }
},{ immediate: true })
</script>
