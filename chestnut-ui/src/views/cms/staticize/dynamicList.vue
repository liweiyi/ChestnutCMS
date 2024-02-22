<template>
  <div class="app-container cms-template-func">
    <el-row>
      <el-form size="small" :inline="true">
        <el-form-item>
          <el-input 
            v-model="filterName"
            clearable
            style="width: 200px"
            @input="handleQuery" />
        </el-form-item>
      </el-form>
    </el-row>
    <el-row>
      <el-table 
        v-loading="loading" 
        ref="dynamicPageTypeTable" 
        :data="dynamicPageTypes"
        @row-click="handleRowClick">
        <el-table-column type="expand">
          <template slot-scope="scope">
            <el-row v-if="scope.row.description!=''" style="padding:0 20px;">
              <el-descriptions v-if="scope.row.desc!=''" :title="$t('CMS.Staticize.UsageDesc')" :colon="false">
                <el-descriptions-item :contentStyle="{width:'100%'}">{{ scope.row.desc }}</el-descriptions-item>
              </el-descriptions>
              <el-descriptions :title="$t('CMS.Staticize.RequestArgs')" :colon="false">
                <el-descriptions-item :contentStyle="{width:'100%'}">
                  <el-table :data="scope.row.requestArgs">
                    <el-table-column :label="$t('CMS.Staticize.FuncAttr')" algin="center" width="150" prop="name" />
                    <el-table-column :label="$t('CMS.Staticize.FuncAttrType')" algin="center" width="100" prop="type" />
                    <el-table-column :label="$t('CMS.Staticize.FuncAttrRequired')" algin="center" width="80">
                      <template slot-scope="scope">
                        <dict-tag :options="dict.type.YesOrNo" :value="scope.row.mandatory?'Y':'N'"/>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('CMS.Staticize.DefaultValue')" algin="center" width="150" prop="defaultValue" />
                    <el-table-column :label="$t('CMS.Staticize.FuncAttrDesc')" algin="left" prop="desc">
                    </el-table-column>
                  </el-table>
                </el-descriptions-item>
              </el-descriptions>
            </el-row>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.Staticize.DynamicTempType')" algin="left" width="255" prop="type" />
        <el-table-column :label="$t('CMS.Staticize.DynamicTempName')" algin="left" width="255" prop="name" />
        <el-table-column :label="$t('CMS.Staticize.DynamicTempRequestPath')" algin="left" prop="requestPath">
          <template slot-scope="scope">
            {{ '/' + scope.row.requestPath }}
          </template>
        </el-table-column>
      </el-table>
    </el-row>
  </div>
</template>
<style>
</style>
<script>
import { getDynamicPageTypes } from "@/api/contentcore/site";

export default {
  name: "CMSDynamicTemlateFunc",
  dicts: ['YesOrNo'],
  data () {
    return {
      loading: false,
      defaultProps: {
        children: "children",
        label: "label"
      },
      dynamicPageTypes: [],
      allDynamicPageTypes: [],
      filterName: undefined
    };
  },
  created () {
    this.loadDynamicPageTypes();
  },
  methods: {
    loadDynamicPageTypes() {
      this.loading = true;
      getDynamicPageTypes().then(response => {
        this.dynamicPageTypes = response.data;
        this.allDynamicPageTypes = response.data;
        this.loading = false;
      });
    },
    handleQuery() {
      this.dynamicPageTypes = this.allDynamicPageTypes.filter(data => {
        if (!this.filterName || this.filterName.length == 0) {
          return true;
        }
        if (!data.name.toLowerCase().includes(this.filterName.toLowerCase()) 
            && !data.type.toLowerCase().includes(this.filterName.toLowerCase())) {
          return false;
        }
        return true;
      });
    },
    handleRowClick(row) {
      this.$refs.dynamicPageTypeTable.toggleRowExpansion(row);
    }
  }
};
</script>