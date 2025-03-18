<template>
  <div class="app-groovy-container" :loading="loading">
    <el-row :gutter="10">
      <el-col :span="18">
        <el-form :model="form" ref="form" :rules="rules" size="small" label-position="top" label-width="140px">
          <el-form-item label="Groovy Script" prop="scriptText">
            <el-input
              type="textarea"
              :rows="20"
              v-model="form.scriptText"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button v-loading="loading" type="primary" icon="el-icon-s-promotion" @click="handleExecute">{{ $t("GroovyScript.Exec") }}</el-button>
            <el-button v-loading="loading" type="success" icon="el-icon-s-promotion" @click="handleSave">{{ $t("GroovyScript.SaveScript") }}</el-button>
          </el-form-item>
        </el-form>
        <el-row>
          <div class="groovy_result" v-html="resultString"></div>
        </el-row>
      </el-col>
      <el-col :span="6">
        <el-table v-loading="loadingList" size="small" :data="groovyScriptList">
          <el-table-column :label="$t('GroovyScript.Name')" prop="name" align="left" :show-overflow-tooltip="true" />
          <el-table-column :label="$t('Common.Operation')" align="right" width="220" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button size="small" type="text" @click="showScript(scope.row.scriptId)" class="mr10">{{ $t("Common.View") }}</el-button>
              <el-button size="small" type="text" @click="handleDeleteScript(scope.row.scriptId)">{{ $t("Common.Delete") }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <el-dialog :title="$t('GroovyScript.SaveScript')" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="saveForm" :model="formSave" :rules="rulesSaveForm" label-width="100px">
        <el-form-item :label="$t('GroovyScript.Name')" prop="name">
          <el-input v-model="formSave.name" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="formSave.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitSaveForm">{{ $t('Common.Confirm') }}</el-button>
        <el-button @click="open=false">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { executeGroovyScript, getGroovyScript, getGroovyScriptList, saveGroovyScript, deleteGroovyScript } from "@/api/system/groovy";

export default {
  name: "SystemGroovyIndex",
  data() {
    return {
      loading: false,
      resultString: "",
      form: {
        scriptText: ""
      },
      rules: {
        scriptText: [
          { required: true, message: this.$t("Common.RuleTips.NotEmpty"), trigger: "blur" }
        ]
      },
      open: false,
      loadingList: false,
      groovyScriptList: [],
      formSave: {},
      rulesSaveForm: {
        name: [
          { required: true, message: this.$t("Common.RuleTips.NotEmpty"), trigger: "blur" }
        ]
      },
    };
  },
  created() {
    this.loadGroovyScripts()
  },
  methods: {
    handleExecute: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.loading = true;
          executeGroovyScript(this.form).then(response => {
            this.$modal.msgSuccess(this.$t('Common.Success'));
            this.resultString = response.data;
            this.loading = false;
          });
        }
      });
    },
    loadGroovyScripts: function() {
      this.loadingList = true;
      getGroovyScriptList().then(response => {
            this.groovyScriptList = response.data.rows;
            this.loadingList = false;
          });
    },
    loadGroovyScript: function(scriptId) {
      getGroovyScript(scriptId).then(response => {
        this.formSave = response.data;
      })
    },
    showScript: function(scriptId) {
      this.loadingList = true;
      getGroovyScript(scriptId).then(response => {
        this.form.scriptText = response.data.scriptText;
        this.loadingList = false;
      })
    },
    handleSave: function() {
      this.open = true;
      this.formSave = {};
    },
    submitSaveForm: function() {
      this.$refs["saveForm"].validate(valid => {
        if (valid) {
          this.loading = true;
          this.formSave.scriptText = this.form.scriptText;
          saveGroovyScript(this.formSave).then(response => {
            this.$modal.msgSuccess(this.$t('Common.Success'));
            this.loadGroovyScripts();
            this.loading = false;
            this.open = false;
          });
        }
      });
    },
    handleDeleteScript: function(scriptId) {
      deleteGroovyScript([scriptId]).then(response => {
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
        this.loadGroovyScripts()
      })
    }
  }
};
</script>
<style>
.app-groovy-container {
  padding: 20px;
}
.app-groovy-container .groovy_result {
  font-size: 14px;
  line-height: 22px;
  color: #444;
  white-space: break-spaces;
}
</style>
