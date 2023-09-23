<template>
  <div>
    <el-row :gutter="24">
      <el-col :span="24">
        <el-form 
          :model="queryParams"
          ref="queryForm"
          :inline="true"
          size="small">
          <el-form-item prop="query">
            <el-input v-model="queryParams.query" placeholder="输入内容标题">
            </el-input>
          </el-form-item>
          <el-form-item prop="query">
            <el-select  v-model="queryParams.sorts" @change="loadContentList" style="width: 140px">
              <el-option value="viewCount#DESC" :label="$t('CMS.Content.SortOption.ViewCountDesc')"></el-option>
              <el-option value="favoriteCount#DESC" :label="$t('CMS.Content.SortOption.FavoriteCountDesc')"></el-option>
              <el-option value="likeCount#DESC" :label="$t('CMS.Content.SortOption.LikeCountDesc')"></el-option>
              <el-option value="commentCount#DESC" :label="$t('CMS.Content.SortOption.CommentCountDesc')"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="el-icon-search"
                @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button 
                icon="el-icon-refresh"
                @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="dataList">
      <el-table-column :label="$t('CMS.Content.Title')" align="left" prop="title"></el-table-column>
      <el-table-column :label="$t('CMS.Content.ViewCount')" align="center" width="140" prop="viewCount"/>
      <el-table-column :label="$t('CMS.Content.FavoriteCount')" align="center" width="140" prop="favoriteCount"/>
      <el-table-column :label="$t('CMS.Content.LikeCount')" align="center" width="140" prop="likeCount"/>
      <el-table-column :label="$t('CMS.Content.CommentCount')" align="center" width="140" prop="commentCount"/>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="loadContentList" />
  </div>
</template>
<script>
import { getContentDynamicData } from "@/api/stat/content";

export default {
  name: "ContentDynamicStat",
  data () {
    return {
      loading: true,
      dataList: [],
      total: 0,
      queryParams: {
        query: undefined,
        sorts: "viewCount#DESC",
        pageSize: 20,
        pageNum: 1
      }
    };
  },
  created () {
    this.loadContentList();
  },
  methods: {
    loadContentList () {
      this.loading = true;
      getContentDynamicData(this.queryParams).then(response => {
        this.dataList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    handleQuery () {
      this.queryParams.pageNo = 1;
      this.loadListData();
    },
    resetQuery () {
      this.resetForm("queryForm");
      this.handleQuery();
    }
  }
};
</script>