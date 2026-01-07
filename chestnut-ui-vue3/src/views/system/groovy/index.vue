<template>
  <div class="app-groovy-container" :loading="loading">
    <el-row :gutter="10">
      <el-col :span="18">
        <el-form :model="form" ref="formRef" :rules="rules" label-position="top" label-width="140px">
          <el-form-item label="Groovy Script" prop="scriptText">
            <el-input
              type="textarea"
              :rows="20"
              v-model="form.scriptText"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button v-loading="loading" type="primary" icon="Promotion" @click="handleExecute">{{ $t("GroovyScript.Exec") }}</el-button>
            <el-button v-loading="loading" type="success" icon="Promotion" @click="handleSave">{{ $t("GroovyScript.SaveScript") }}</el-button>
          </el-form-item>
        </el-form>
        <el-row>
          <div class="groovy_result" v-html="resultString"></div>
        </el-row>
      </el-col>
      <el-col :span="6">
        <el-table v-loading="loadingList" :data="groovyScriptList">
          <el-table-column :label="$t('GroovyScript.Name')" prop="name" align="left" :show-overflow-tooltip="true" />
          <el-table-column :label="$t('Common.Operation')" align="right" width="220">
            <template #default="scope">
              <el-button type="text" @click="showScript(scope.row.scriptId)" class="mr10">{{ $t("Common.View") }}</el-button>
              <el-button type="text" @click="handleDeleteScript(scope.row.scriptId)">{{ $t("Common.Delete") }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <el-dialog :title="$t('GroovyScript.SaveScript')" v-model="open" width="600px" append-to-body>
      <el-form ref="saveFormRef" :model="formSave" :rules="rulesSaveForm" label-width="100px">
        <el-form-item :label="$t('GroovyScript.Name')" prop="name">
          <el-input v-model="formSave.name" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="formSave.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitSaveForm">{{ $t('Common.Confirm') }}</el-button>
        <el-button @click="open=false">{{ $t('Common.Cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="SystemGroovyIndex">
import * as groovyApi from "@/api/system/groovy";

const { proxy } = getCurrentInstance();

const loading = ref(false);
const resultString = ref("");
const open = ref(false);
const loadingList = ref(false);
const groovyScriptList = ref([]);
const data = reactive({
  form: {
    scriptText: ""
  },
  rules: {
    scriptText: [
      { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" }
    ]
  },
  formSave: {},
  rulesSaveForm: {
    name: [
      { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" }
    ]
  }
});
const { form, rules, formSave, rulesSaveForm } = toRefs(data);

onMounted(() => {
  loadGroovyScripts();
});

function handleExecute() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      loading.value = true;
      groovyApi.executeGroovyScript(form.value).then(response => {
        proxy.$modal.msgSuccess(proxy.$t('Common.Success'));
        resultString.value = response.data;
        loading.value = false;
      });
    }
  });
}

function loadGroovyScripts() {
  loadingList.value = true;
  groovyApi.getGroovyScriptList().then(response => {
    groovyScriptList.value = response.data.rows;
    loadingList.value = false;
  });
}

function loadGroovyScript(scriptId) {
  groovyApi.getGroovyScript(scriptId).then(response => {
    formSave.value = response.data;
  })
}

function showScript(scriptId) {
  loadingList.value = true;
  groovyApi.getGroovyScript(scriptId).then(response => {
    form.value.scriptText = response.data.scriptText;
    loadingList.value = false;
  })
}

function handleSave() {
  open.value = true;
  formSave.value = {};
}

function submitSaveForm() {
  proxy.$refs.saveFormRef.validate(valid => {
    if (valid) {
      loading.value = true;
      formSave.value.scriptText = form.value.scriptText;
      groovyApi.saveGroovyScript(formSave.value).then(response => {
        proxy.$modal.msgSuccess(proxy.$t('Common.Success'));
        loadGroovyScripts();
        loading.value = false;
        open.value = false;
      });
    }
  });
}

function handleDeleteScript(scriptId) {
  groovyApi.deleteGroovyScript([scriptId]).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
    loadGroovyScripts()
  })
}
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
