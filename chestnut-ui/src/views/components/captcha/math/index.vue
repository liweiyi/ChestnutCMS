<template>
  <div class="math-captcha-container">
    <el-input
      v-if="username && token"
      v-model="authCode"
      auto-complete="off"
      :placeholder="$t('Login.Captcha')"
      class="captcha-code"
      @keyup.native="handleCodeChanged"
    >
      <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
      <template slot="append" style="padding: 0;border: none;">
        <img v-if="codeImage!=''" :src="codeImage" @click="reloadCaptcha" class="captcha-img" :style="{ width: `${imgWidth}` }" />
        <el-link v-else icon="el-icon-refresh" :underline="false" @click="reloadCaptcha" :style="{ width: `${imgWidth}` }">刷新</el-link>
      </template>
    </el-input>
  </div>
</template>
<script>
import { getCaptcha } from "@/api/system/captcha";

export default {
  name: "CaptchaMath",
  props: {
    width: {
      type: Number,
      default: 100,
      required: false,
    },
    username: {
      type: String,
      required: true,
    },
    token: {
      type: String,
      required: true,
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
      captchaType: "Math",
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
      getCaptcha({ username: this.username, token: this.token }).then(res => {
        this.captcha = res.data;
        this.codeImage = "data:image/gif;base64," + this.captcha.image;
        this.loading = false;
        this.$emit("change", {
          type: this.captchaType,
          token: this.captcha.token,
          data: "",
        });
      }).catch(err => {
        this.captcha = {};
        this.codeImage = "";
        this.loading = false;
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
.math-captcha-container {
  .captcha-img {
    cursor: pointer;
  }
  .el-input-group__append {
    padding: 0;
    border: 0;
  }
}
</style>