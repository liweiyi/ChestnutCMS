<template>
  <div class="cms-template-func">
    <el-form :inline="true" class="el-form-search">
      <el-form-item>
        <el-input 
          v-model="funcName"
          :placeholder="$t('CMS.Staticize.InputFuncName')"
          clearable
          style="width: 200px"
          @input="handleQuery" />
      </el-form-item>
    </el-form>
    <el-table 
      v-loading="loading" 
      ref="funcListTableRef" 
      :data="funcList"
      @row-click="handleRowClick">
      <el-table-column type="expand">
        <template #default="scope">
          <div v-if="scope.row.description!=''" style="padding:0 20px;">
            <el-descriptions :title="$t('CMS.Staticize.UsageDesc')" :column="1" direction="vertical">
              <el-descriptions-item>{{ scope.row.desc }}</el-descriptions-item>
            </el-descriptions>
            <el-descriptions v-if="scope.row.aliases.length > 0" :title="$t('CMS.Staticize.FuncAlias')" :column="1" direction="vertical">
              <el-descriptions-item>{{ formatAliases(scope.row.aliases) }}</el-descriptions-item>
            </el-descriptions>
            <el-descriptions :title="$t('CMS.Staticize.FuncArgs')" :column="1" direction="vertical">
              <el-descriptions-item>
                <el-table :data="scope.row.funcArgs" border>
                  <el-table-column :label="$t('CMS.Staticize.FuncAttr')" align="center" width="150" type="index" />
                  <el-table-column :label="$t('CMS.Staticize.FuncAttrName')" align="center" prop="name" />
                  <el-table-column :label="$t('CMS.Staticize.FuncAttrType')" align="center" width="100" prop="type" />
                  <el-table-column :label="$t('CMS.Staticize.FuncAttrRequired')" align="center" width="80">
                    <template #default="scope">
                      <dict-tag :options="YesOrNo" :value="scope.row.required?'Y':'N'"/>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('CMS.Staticize.FuncAttrDesc')" align="left" prop="desc">
                  </el-table-column>
                  <el-table-column :label="$t('CMS.Staticize.DefaultValue')" align="left" width="200" prop="defaultValue" />
                </el-table>
              </el-descriptions-item>
            </el-descriptions>
            <el-descriptions :title="$t('CMS.Staticize.Demo')" :column="1" direction="vertical">
              <el-descriptions-item><el-link type="primary" target="_blank" :href="scope.row.demoLink">{{ $t('CMS.Staticize.ClickToShowDemo') }}</el-link></el-descriptions-item>
            </el-descriptions>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Staticize.FuncName')" align="left" width="255" prop="funcName" />
      <el-table-column :label="$t('CMS.Staticize.FuncDesc')" align="left" prop="desc" />
    </el-table>
  </div>
</template>
<script setup name="CmsTemplateFunc">
import { getFunctionList } from "@/api/contentcore/staticize";

const { proxy } = getCurrentInstance();
const { YesOrNo } = proxy.useDict('YesOrNo');

const loading = ref(false);
const sourceFuncList = ref([]);
const funcList = ref([]);
const funcName = ref(undefined);

onMounted(() => {
  loadFuncList();
});

function loadFuncList() {
  loading.value = true;
  getFunctionList().then(response => {
    funcList.value = response.data;
    sourceFuncList.value = response.data;
    loading.value = false;
  });
}

function handleQuery() {
  funcList.value = sourceFuncList.value.filter(data => {
    if (!funcName.value) {
      return true;
    }
    if (funcName.value && funcName.value.length > 0 && !data.funcName.toLowerCase().includes(funcName.value.toLowerCase()) 
        && !data.funcName.toLowerCase().includes(funcName.value.toLowerCase())) {
      return false;
    }
    return true;
  });
}

function handleRowClick(row) {
  proxy.$refs.funcListTableRef.toggleRowExpansion(row);
}

function formatAliases(aliases) {
  if (!aliases || aliases.length == 0) {
    return "";
  }
  return aliases.map(alias => "[ "+alias+" ]").join(", ");
}

</script>