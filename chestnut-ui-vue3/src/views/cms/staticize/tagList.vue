<template>
  <div class="cms-template-tag">
    <el-form :inline="true" class="el-form-search">
      <el-form-item>
        <el-input 
          v-model="tagName"
          :placeholder="$t('CMS.Staticize.InputTagName')"
          clearable
          style="width: 200px"
          @input="handleQuery" />
      </el-form-item>
    </el-form>
    <el-table 
      v-loading="loading" 
      ref="tagListTable" 
      :data="tagList"
      @row-click="handleRowClick">
      <el-table-column type="expand">
        <template #default="scope">
          <div style="padding:0 20px;">
            <el-alert v-if="scope.row.deprecated" :title="$t('CMS.Staticize.DeprecatedTip', [ scope.row.deprecatedSince, scope.row.deprecatedForRemoval])"
              type="error" :closable="false" style="margin-bottom: 10px;"></el-alert>
            <el-descriptions :title="$t('CMS.Staticize.UsageDesc')" :column="1">
              <el-descriptions-item>{{ scope.row.description }}</el-descriptions-item>
            </el-descriptions>
            <el-descriptions :title="$t('CMS.Staticize.TagAttr')" :column="1" direction="vertical">
              <el-descriptions-item>
                <el-table :data="scope.row.tagAttrs" border>
                  <el-table-column :label="$t('CMS.Staticize.TagAttrName')" align="center" width="200" prop="name" />
                  <el-table-column :label="$t('CMS.Staticize.TagAttrDataType')" align="center" width="120" prop="dataType" />
                  <el-table-column :label="$t('CMS.Staticize.TagAttrMandatory')" align="center" width="90">
                    <template #default="scope">
                      <dict-tag :options="YesOrNo" :value="scope.row.mandatory?'Y':'N'"/>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('CMS.Staticize.TagAttrOptions')" align="left" prop="options">
                    <template #default="scope">
                      <div v-if="scope.row.options!=null" v-for="item in scope.row.options" :key="item.value">{{ item.value }} = {{ item.desc }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('CMS.Staticize.TagAttrDesc')" align="left" prop="usage" />
                  <el-table-column :label="$t('CMS.Staticize.DefaultValue')" align="left" prop="defaultValue" />
                </el-table>
              </el-descriptions-item>
            </el-descriptions>
            <el-descriptions :title="$t('CMS.Staticize.DataField')" :column="1" direction="vertical" v-if="scope.row.dataFields && scope.row.dataFields.length > 0">
              <el-descriptions-item>
                <el-table :data="scope.row.dataFields" border>
                  <el-table-column :label="$t('CMS.Staticize.TagAttrName')" align="left" width="300" prop="name" />
                  <el-table-column :label="$t('CMS.Staticize.TagAttrName')" align="left" prop="comment" />
                  <el-table-column :label="$t('CMS.Staticize.TagAttrDataType')" align="left" prop="dataType" />
                </el-table>
              </el-descriptions-item>
            </el-descriptions>
            <el-descriptions :title="$t('CMS.Staticize.Demo')" :column="1" direction="vertical">
              <el-descriptions-item><el-link type="primary" target="_blank" :href="scope.row.demoLink">{{ $t('CMS.Staticize.ClickToShowDemo') }}</el-link></el-descriptions-item>
            </el-descriptions>
          </div>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Staticize.TagName')" align="left" width="255" prop="name">
        <template #default="scope">
          <el-tag v-if="scope.row.deprecated" size="small" type="danger">{{ $t("CMS.Staticize.DeprecatedTag") }}</el-tag> {{ scope.row.name }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Staticize.TagDirective')" width="255" prop="tagName">
        <template #default="scope">
          <el-link type="primary">
            <span v-html="'<@'+scope.row.tagName+'>'"></span>
          </el-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Staticize.TagDesc')"
        align="left"
        prop="description">
      </el-table-column>
    </el-table>
  </div>
</template>
<script setup name="CmsTemplateTag">
import { getTagList } from "@/api/contentcore/staticize";

const { proxy } = getCurrentInstance();
const { YesOrNo } = proxy.useDict('YesOrNo');

const loading = ref(false);
const sourceTagList = ref([]);
const tagList = ref([]);
const tagName = ref(undefined);

onMounted(() => {
  loadTagList();
});

function loadTagList() {
  loading.value = true;
  getTagList().then(response => {
    tagList.value = response.data;
    sourceTagList.value = response.data;
    loading.value = false;
  });
}

function handleQuery() {
  tagList.value = sourceTagList.value.filter(data => {
    if (!tagName.value) {
      return true;
    }
    if (tagName.value && tagName.value.length > 0 && !data.name.toLowerCase().includes(tagName.value.toLowerCase()) && !data.tagName.toLowerCase().includes(tagName.value.toLowerCase())) {
      return false;
    }
    return true;
  });
}

function handleRowClick(row) {
  proxy.$refs.tagListTable.toggleRowExpansion(row);
}
</script>