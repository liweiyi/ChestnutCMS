<template>
  <div class="app-container">
    <el-dialog 
      :title="$t('Comment.LikeList')"
      :visible.sync="visible"
      width="700px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form 
        :model="queryParams"
        ref="queryForm"
        :inline="true"
        size="small"
        label-width="68px"
        class="el-form-search mb12"
        style="text-align: left">
        <el-form-item :label="$t('Comment.UID')" prop="uid">
          <el-input v-model="queryParams.uid">
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary"
            icon="el-icon-search"
            @click="handleQuery">{{ $t("Common.Search") }}</el-button>
          <el-button
            icon="el-icon-refresh"
            @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
        </el-form-item>
      </el-form>
      <el-table 
        v-loading="loading"
        :height="435"
        :data="likeList"
        highlight-current-row>
        <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="50" />
        <el-table-column :label="$t('Comment.UID')" align="left" prop="uid"/>
        <el-table-column :label="$t('Comment.LikeTime')" align="right" prop="likeTime"/>
      </el-table>
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="loadCommentLikeList"
      />
      <div slot="footer" class="dialog-footer">
        <el-button @click="handleClose">{{ $t("Common.Close") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style scoped>
</style>
<script>
import { getCommentLikeList } from "@/api/comment/comment";

export default {
  name: "CommentLikeListDialog",
  props: {
    commentId: {
      type: String,
      required: true
    },
    open: {
      type: Boolean,
      default: false,
      required: true
    }
  },
  watch: {
    open () {
      this.visible = this.open;
    },
    visible (newVal) {
      if (!newVal) {
        this.handleClose();
      } else {
        this.loadCommentLikeList();
      }
    }
  },
  data () {
    return {
      loading: false,
      visible: this.open,
      likeList: [],
      total: 0,
      queryParams: {
        pageSize: 8,
        pageNum: 1,
        uid: undefined
      }
    };
  },
  methods: {
    loadCommentLikeList () {
      if (!this.visible) {
        return;
      }
      this.loading = true;
      getCommentLikeList(this.commentId, this.queryParams).then(response => {
        this.likeList = response.data.rows;
        this.total = parseInt(response.data.total);
        this.loading = false;
      });
    },
    handleClose () {
      this.$emit("update:open", false);
      this.queryParams.uid = undefined;
      this.queryParams.pageNum = 1;
      this.likeList = [];
    },
    handleQuery () {
      this.queryParams.pageNum = 1;
      this.loadCommentLikeList();
    },
    resetQuery () {
      this.queryParams.uid = undefined;
      this.handleQuery();
    }
  }
};
</script>