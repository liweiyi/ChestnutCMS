<template>
  <div class="app-container cms-template-func">
    <el-row>
      <el-form size="small" :inline="true">
        <el-form-item>
          <el-input 
            v-model="funcName"
            :placeholder="$t('CMS.Staticize.InputFuncName')"
            clearable
            style="width: 200px"
            @input="handleQuery" />
        </el-form-item>
      </el-form>
    </el-row>
    <el-row>
      <el-table 
        v-loading="loading" 
        ref="funcListTable" 
        :data="funcList"
        @row-click="handleRowClick">
        <el-table-column type="expand">
          <template slot-scope="scope">
            <el-row v-if="scope.row.description!=''" style="padding:0 20px;">
              <el-descriptions :title="$t('CMS.Staticize.UsageDesc')" :colon="false">
                <el-descriptions-item :contentStyle="{width:'100%'}">{{ scope.row.desc }}</el-descriptions-item>
              </el-descriptions>
              <el-descriptions :title="$t('CMS.Staticize.FuncAlias')" :colon="false">
                <el-descriptions-item :contentStyle="{width:'100%'}">{{ formatAliases(scope.row.aliases) }}</el-descriptions-item>
              </el-descriptions>
              <el-descriptions :title="$t('CMS.Staticize.FuncArgs')" :colon="false">
                <el-descriptions-item :contentStyle="{width:'100%'}">
                  <el-table :data="scope.row.funcArgs" border>
                    <el-table-column :label="$t('CMS.Staticize.FuncAttr')" align="center" width="150" type="index" />
                    <el-table-column :label="$t('CMS.Staticize.FuncAttrName')" align="center" width="150" prop="name" />
                    <el-table-column :label="$t('CMS.Staticize.FuncAttrType')" align="center" width="100" prop="type" />
                    <el-table-column :label="$t('CMS.Staticize.FuncAttrRequired')" align="center" width="80">
                      <template slot-scope="scope">
                        <dict-tag :options="dict.type.YesOrNo" :value="scope.row.required?'Y':'N'"/>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('CMS.Staticize.FuncAttrDesc')" align="left" prop="desc">
                    </el-table-column>
                  </el-table>
                </el-descriptions-item>
              </el-descriptions>
            </el-row>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.Staticize.FuncName')" align="left" width="255" prop="funcName" />
        <el-table-column :label="$t('CMS.Staticize.FuncDesc')" align="left" prop="desc" />
      </el-table>
    </el-row>
  </div>
</template>
<style>
</style>
<script>
import { getFunctionList } from "@/api/contentcore/staticize";

export default {
  name: "CMSTemlateFunc",
  dicts: ['YesOrNo'],
  data () {
    return {
      loading: false,
      defaultProps: {
        children: "children",
        label: "label"
      },
      sourceFuncList: [],
      funcList: [],
      funcName: undefined
    };
  },
  created () {
    this.loadFuncList();
  },
  methods: {
    loadFuncList() {
      this.loading = true;
      getFunctionList().then(response => {
        this.funcList = response.data;
        this.sourceFuncList = response.data;
        this.loading = false;
      });
    },
    handleQuery() {
      this.funcList = this.sourceFuncList.filter(data => {
        if (!this.funcName) {
          return true;
        }
        if (this.funcName && this.funcName.length > 0 && !data.funcName.toLowerCase().includes(this.funcName.toLowerCase()) 
            && !data.funcName.toLowerCase().includes(this.funcName.toLowerCase())) {
          return false;
        }
        return true;
      });
    },
    handleRowClick(row) {
      this.$refs.funcListTable.toggleRowExpansion(row);
    },
    formatAliases(aliases) {
      if (!aliases || aliases.length == 0) {
        return ""
      }
      return aliases.map(alias => "[ "+alias+" ]").join(", ")
    }
  }
};
</script>