<template>
  <div class="cms-dynamic-list-container">
    <el-form :inline="true" class="el-form-search">
      <el-form-item>
        <el-input 
          v-model="filterName"
          clearable
          style="width: 200px"
          @input="handleQuery" />
      </el-form-item>
    </el-form>
    <el-table 
      v-loading="loading" 
      ref="dynamicPageTypeTableRef" 
      :data="dynamicPageTypes"
      @row-click="handleRowClick">
      <el-table-column type="expand">
        <template #default="scope">
          <div v-if="scope.row.description!=''" style="padding:0 20px;">
            <el-descriptions v-if="scope.row.desc!=''" :title="$t('CMS.Staticize.UsageDesc')" :column="1" direction="vertical">
              <el-descriptions-item>{{ scope.row.desc }}</el-descriptions-item>
            </el-descriptions>
            <el-descriptions :title="$t('CMS.Staticize.RequestArgs')" :column="1" direction="vertical">
              <el-descriptions-item>
                <el-table :data="scope.row.requestArgs">
                  <el-table-column :label="$t('CMS.Staticize.FuncAttr')" algin="center" width="200" prop="name" />
                  <el-table-column :label="$t('CMS.Staticize.FuncAttrType')" algin="center" width="120" prop="type" />
                  <el-table-column :label="$t('CMS.Staticize.FuncAttrRequired')" algin="center" width="90">
                    <template #default="scope">
                      <dict-tag :options="YesOrNo" :value="scope.row.mandatory?'Y':'N'"/>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('CMS.Staticize.DefaultValue')" algin="center" width="200" prop="defaultValue" />
                  <el-table-column :label="$t('CMS.Staticize.FuncAttrDesc')" algin="left" prop="desc">
                  </el-table-column>
                </el-table>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Staticize.DynamicTempType')" algin="left" width="255" prop="type" />
      <el-table-column :label="$t('CMS.Staticize.DynamicTempName')" algin="left" width="255" prop="name" />
      <el-table-column :label="$t('CMS.Staticize.DynamicTempRequestPath')" algin="left" prop="requestPath">
        <template #default="scope">
          {{ '/' + scope.row.requestPath }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script setup name="CMSDynamicTemlateFunc">
import { getDynamicPageTypes } from "@/api/contentcore/site";

const { proxy } = getCurrentInstance();
const { YesOrNo } = proxy.useDict('YesOrNo');

const defaultProps = {
  children: "children",
  label: "label"
};

const loading = ref(false);
const dynamicPageTypes = ref([]);
const allDynamicPageTypes = ref([]);
const filterName = ref(undefined);

onMounted(() => {
  loadDynamicPageTypes();
});
function loadDynamicPageTypes() {
  loading.value = true;
  getDynamicPageTypes().then(response => {
    dynamicPageTypes.value = response.data;
    allDynamicPageTypes.value = response.data;
    loading.value = false;
  });
}
function handleQuery() {
  dynamicPageTypes.value = allDynamicPageTypes.value.filter(data => {
    if (!filterName.value || filterName.value.length == 0) {
      return true;
    }
    if (!data.name.toLowerCase().includes(filterName.value.toLowerCase()) 
        && !data.type.toLowerCase().includes(filterName.value.toLowerCase())) {
      return false;
    }
    return true;
  });
}
function handleRowClick(row) {
  proxy.$refs.dynamicPageTypeTableRef.toggleRowExpansion(row);
}
</script>