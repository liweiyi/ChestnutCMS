<template>
  <el-dropdown trigger="click" @command="handleSetLanguage">
    <div style="font-size:14px">
      {{ langName }} <i v-if="showArrowDown" class="el-icon-arrow-down el-icon--right"></i>
    </div>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item v-for="item of langOptions" :key="item.value" :disabled="lang===item.value" :command="item.value">
        {{ item.label }}
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
import { listLangOptions } from '@/api/system/i18nDict'

export default {
  name: "LanguageSelect",
  props: {
    arrow: {
      type: Boolean,
      default: false,
      required: false
    },
  },
  data() {
    return {
      showArrowDown: this.arrow,
      langOptions: []
    }
  },
  computed: {
    lang() {
      return this.$i18n.locale
    },
    langName() {
      const language = this.$i18n.locale || 'zh-CN';
      for (var i = 0; i < this.langOptions.length; i++) {
        if (this.langOptions[i].value == language) {
          return this.langOptions[i].label;
        }
      }
    }
  },
  created() {
    this.loadLangSelectData()
  },
  methods: {
    loadLangSelectData() {
      listLangOptions().then(response => {
        this.langOptions = response.data
      })
    },
    handleSetLanguage(lang) {
      this.$cache.local.set('lang', lang)
      this.$i18n.locale = lang;
      window.location.reload();
      this.$message({
        message: 'Switch Language Success',
        type: 'success'
      })
    }
  }

}
</script>
