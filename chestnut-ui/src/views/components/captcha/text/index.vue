<template>
  <div class="text-captcha-container">
    <el-input
      v-model="authCode"
      auto-complete="off"
      :placeholder="$t('Login.Captcha')"
      class="captcha-code"
      @keyup.native="handleCodeChanged"
    >
      <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
      <template slot="append" style="padding: 0;border: none;">
        <img :src="codeImage" @click="reloadCaptcha" class="captcha-img" :style="{ width: `${imgWidth}` }" />
      </template>
    </el-input>
  </div>
</template>
<script>
import { getCaptcha } from "@/api/system/captcha";

export default {
  name: "CaptchaText",
  props: {
    width: {
      type: Number,
      default: 100,
      required: false,
    }
  },
  computed: {
    imgWidth () {
      return this.width + 'px';
    },
  },
  watch: {
    code(newVal) {
      this.authCode = newVal;
    },
  },
  data () {
    return {
      loading: false,
      captchaType: "Text",
      captcha: {},
      authCode: this.code,
      codeImage: ""
    };
  },
  mounted() {
    this.reloadCaptcha()
  },
  methods: {
    handleReloadCaptcha() {
      this.reloadCaptcha();
    },
    reloadCaptcha() {
      this.loading = true;
      this.authCode = ""; // 重置验证码
      getCaptcha(this.captchaType).then(res => {
        this.captcha = res.data;
        this.codeImage = "data:image/gif;base64," + this.captcha.image;
        this.loading = false;
        this.$emit("change", {
          type: this.captchaType,
          token: this.captcha.token,
          data: ""
        });
      })
    },
    handleCodeChanged () {
      this.$emit("change", {
        type: this.captchaType,
        token: this.captcha.token,
        data: this.authCode
      });
    },
  }
};
</script>
<style scoped>
.text-captcha-container {
  .captcha-img {
    cursor: pointer;
  }
  .el-input-group__append {
    padding: 0;
    border: 0;
  }
}
</style>