<template>
  <div class="app-container cms-template-tag">
    <el-row>
      <el-form size="small" :inline="true">
        <el-form-item>
          <el-input 
            v-model="tagName"
            :placeholder="$t('CMS.Staticize.InputTagName')"
            clearable
            style="width: 200px"
            @input="handleQuery" />
        </el-form-item>
      </el-form>
    </el-row>
    <el-row>
      <el-table 
        v-loading="loading" 
        ref="tagListTable" 
        :data="tagList"
        @row-click="handleRowClick">
        <el-table-column type="expand">
          <template slot-scope="scope">
            <el-row style="padding:0 20px;">
              <el-alert v-if="scope.row.deprecated" :title="$t('CMS.Staticize.DeprecatedTip', [ scope.row.deprecatedSince, scope.row.deprecatedForRemoval])"
                type="error" :closable="false" style="margin-bottom: 10px;"></el-alert>
              <el-descriptions :title="$t('CMS.Staticize.UsageDesc')" :colon="false">
                <el-descriptions-item :contentStyle="{width:'100%'}">{{ scope.row.description }}</el-descriptions-item>
              </el-descriptions>
              <el-descriptions :title="$t('CMS.Staticize.TagAttr')" :colon="false">
                <el-descriptions-item :contentStyle="{width:'100%'}">
                  <el-table :data="scope.row.tagAttrs" border>
                    <el-table-column :label="$t('CMS.Staticize.TagAttrName')" align="center" width="150" prop="name" />
                    <el-table-column :label="$t('CMS.Staticize.TagAttrDataType')" align="center" width="100" prop="dataType" />
                    <el-table-column :label="$t('CMS.Staticize.TagAttrMandatory')" align="center" width="80">
                      <template slot-scope="scope">
                        <dict-tag :options="dict.type.YesOrNo" :value="scope.row.mandatory?'Y':'N'"/>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('CMS.Staticize.TagAttrOptions')" align="left" prop="options">
                      <template slot-scope="scope" v-if="scope.row.options!=null">
                        <div v-for="item in scope.row.options" :key="item.value">{{ item.value }} = {{ item.desc }}</div>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('CMS.Staticize.TagAttrDesc')" align="left" prop="usage" />
                    <el-table-column :label="$t('CMS.Staticize.DefaultValue')" align="left" prop="defaultValue" />
                  </el-table>
                </el-descriptions-item>
              </el-descriptions>
              <el-descriptions :title="$t('CMS.Staticize.DataField')" :colon="false" v-if="scope.row.dataFields && scope.row.dataFields.length > 0">
                <el-descriptions-item :contentStyle="{width:'100%'}">
                  <el-table :data="scope.row.dataFields" border>
                    <el-table-column :label="$t('CMS.Staticize.TagAttrName')" align="left" width="300" prop="name" />
                    <el-table-column :label="$t('CMS.Staticize.TagAttrName')" align="left" prop="comment" />
                    <el-table-column :label="$t('CMS.Staticize.TagAttrDataType')" align="left" prop="dataType" />
                  </el-table>
                </el-descriptions-item>
              </el-descriptions>
              <el-descriptions :title="$t('CMS.Staticize.Demo')" :colon="false">
                <el-descriptions-item><el-link type="primary" target="_blank" :href="scope.row.demoLink">{{ $t('CMS.Staticize.ClickToShowDemo') }}</el-link></el-descriptions-item>
              </el-descriptions>
            </el-row>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.Staticize.TagName')" align="left" width="255" prop="name">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.deprecated" size="mini" type="danger">{{ $t("CMS.Staticize.DeprecatedTag") }}</el-tag> {{ scope.row.name }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.Staticize.TagDirective')" width="255" prop="tagName">
          <template slot-scope="scope">
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
    </el-row>
  </div>
</template>
<script>
import { getTagList } from "@/api/contentcore/staticize";

export default {
  name: "CMSTemlateTag",
  dicts: ['YesOrNo'],
  data () {
    return {
      loading: false,
      defaultProps: {
        children: "children",
        label: "label"
      },
      sourceTagList: [],
      tagList: [],
      tagName: undefined,
    };
  },
  created () {
    this.loadTagList();
  },
  methods: {
    loadTagList() {
      this.loading = true;
      getTagList().then(response => {
        this.tagList = response.data;
        this.sourceTagList = response.data;
        this.loading = false;
      });
    },
    handleQuery() {
      this.tagList = this.sourceTagList.filter(data => {
        if (!this.tagName) {
          return true;
        }
        if (this.tagName && this.tagName.length > 0 && !data.name.toLowerCase().includes(this.tagName.toLowerCase()) && !data.tagName.toLowerCase().includes(this.tagName.toLowerCase())) {
          return false;
        }
        return true;
      });
    },
    handleRowClick(row) {
      this.$refs.tagListTable.toggleRowExpansion(row);
    },
  }
};
</script>