<template>
  <div>
    <el-link :underline="false" @click="handleOpen" style="font-size: 20px">
      <svg-icon icon-class="language" />
    </el-link>
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form label-width="80px">
        <el-form-item :label="lang.langTag"  v-for="lang in dataList" :key="lang.langTag">
          <el-input v-model="lang.langValue" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleOk">{{ $t('Common.Save') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
</style>
<script>
import { listI18nDictByKey, batchSaveI18nDicts } from "@/api/system/i18nDict";

export default {
  name: "I18nFieldEditor",
  props: {
    languageKey: {
      type: String,
      required: true,
      default: {}
    }
  },
  data () {
    return {
      loading: false,
      open: false,
      dataList: [],
      title: this.$t('System.I18n.EditorTitle', [ this.languageKey ]),
    };
  },
  methods: {
    handleOpen () {
      this.open = true;
      this.loading = true;
      listI18nDictByKey(this.languageKey).then(response => {
          this.dataList = response.data;
          this.loading = false;
        }
      );
    },
    handleOk () {
      batchSaveI18nDicts(this.dataList).then(response => {
          this.open = false;
          this.$modal.msgSuccess(this.$t('Common.Success'));
        }
      );
    },
    cancel () {
      this.open = false;
    }
  },
  beforeDestroy() {
    
  }
};
</script>