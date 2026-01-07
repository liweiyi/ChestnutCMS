<template>
  <div>
    <el-form 
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      class="el-form-search">
      <el-form-item prop="query">
        <el-input v-model="queryParams.query" :placeholder="$t('CMS.Content.Placeholder.Title')">
        </el-input>
      </el-form-item>
      <el-form-item prop="query">
        <el-select  v-model="queryParams.sorts" @change="loadContentList" style="width: 200px">
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
            icon="Search"
            @click="handleQuery">{{ $t("Common.Search") }}</el-button>
          <el-button 
            icon="Refresh"
            @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>

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
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadContentList" />
  </div>
</template>
<script setup name="ContentDynamicStat">
import { getContentDynamicData } from "@/api/stat/content";

const { proxy } = getCurrentInstance();

const loading = ref(true);
const dataList = ref([]);
const total = ref(0);
const queryParams = reactive({
  query: undefined,
  sorts: "viewCount#DESC",
  pageSize: 20,
  pageNum: 1
});

onMounted(() => {
  loadContentList();
});

function loadContentList() {
  loading.value = true;
  getContentDynamicData(queryParams).then(response => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function handleQuery() {
  queryParams.pageNum = 1;
  loadContentList();
}

function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}
</script>