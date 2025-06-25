<template>
  <div class="login">
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{ $t('APP.TITLE') }}</h3>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          auto-complete="off"
          :placeholder="$t('Login.Account')"
        >
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          auto-complete="off"
          :placeholder="$t('Login.Password')"
          @keyup.enter.native="handleLogin"
        >
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item v-if="captchaConfig.enabled">
        <text-captcha v-if="captchaConfig.type=='Text'" ref="TextCaptcha" @change="handleCaptchaSuccess"></text-captcha>
        <math-captcha v-if="captchaConfig.type=='Math'" ref="MathCaptcha" @change="handleCaptchaSuccess"></math-captcha>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">{{ $t('Login.RememberMe') }}</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="medium"
          type="primary"
          style="width:100%;"
          @click.native.prevent="handleLogin"
        >
          <span v-if="!loading">{{ $t('Login.Login') }}</span>
          <span v-else>{{ $t('Login.Logining') }}</span>
        </el-button>
        <div style="float: right;" v-if="register">
          <router-link class="link-type" :to="'/register'">{{ $t('Login.Register') }}</router-link>
        </div>
      </el-form-item>
      <el-form-item style="float:right;">
        <language-select id="language-select" :arrow="true" class="right-menu-item hover-effect" />
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>{{ appInfo.copyright }}</span>
    </div>
  </div>
</template>

<script>
import { getLoginCaptchaConfig } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'
import LanguageSelect from '@/components/LanguageSelect'
import MathCaptcha from './components/captcha/math'
import TextCaptcha from './components/captcha/text'

export default {
  name: "Login",
  components: {
    LanguageSelect,
    MathCaptcha,
    TextCaptcha,
  },
  data() {
    return {
      appInfo: process.env.VUE_APP_INFO,
      loginForm: {
        username: "",
        password: "",
        rememberMe: false,
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: this.$t('Login.AccountRuleTip') }
        ],
        password: [
          { required: true, trigger: "blur", message: this.$t('Login.PasswordRuleTip') }
        ],
      },
      loading: false,
      captchaConfig: {
        enabled: false,
        type: ""
      },
      // 注册开关
      register: false,
      redirect: undefined
    };
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  created() {
    this.appInfo = process.env.VUE_APP_INFO;
    this.loadCaptchaConfig();
    this.getCookie();
  },
  methods: {
    loadCaptchaConfig() {
      getLoginCaptchaConfig().then(res => {
        this.captchaConfig = res.data;
      });
    },
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    handleCaptchaSuccess(data) {
      this.loginForm.captcha = data;
    },
    handleLogin() {
      if (this.captchaConfig.enabled && !this.loginForm.captcha) {
        this.$modal.msgError(this.$t('Login.CaptchaRuleTip'));
        return;
      }
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove('rememberMe');
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || process.env.VUE_APP_PATH }).catch(()=>{});
          }).catch(() => {
            this.loading = false;
            if (this.captchaConfig.enabled) {
              this.$refs[this.captchaConfig.type+'Captcha'].reloadCaptcha();
            }
          });
        }
      });
    }
  }
};
</script>

<style rel="stylesheet/scss" lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
  line-height: 24px;
  font-weight: bold;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  .el-input {
    height: 38px;
    input {
      height: 38px;
    }
  }
  .input-icon {
    height: 38px;
    width: 14px;
    margin-left: 2px;
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 38px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  height: 38px;
}
</style>