<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb12">
      <el-col :span="1.5">
        <el-button
          type="success" 
          plain 
          icon="el-icon-cricle-check"
          size="mini" 
          :disabled="commentMultiple" 
          v-hasPermi="['comment:audit']"
          @click="handleAuditPass">{{ $t('Comment.AuditPass') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="warning" 
          plain 
          icon="el-icon-cricle-close" 
          size="mini" 
          :disabled="commentMultiple"
          v-hasPermi="['comment:audit']"
          @click="handleAuditNotPass">{{ $t('Comment.AuditNotPass') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="danger" 
          plain 
          icon="el-icon-delete" 
          size="mini" 
          :disabled="commentMultiple" 
          v-hasPermi="['comment:delete']"
          @click="handleDelete" >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="loadCommentList"></right-toolbar>
    </el-row>
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" style="float:right;" v-show="showSearch">
      <el-form-item :label="$t('Comment.SourceType')" prop="sourceType">
        <el-input
          v-model="queryParams.sourceType"
          clearable
          style="width: 160px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="$t('Comment.SourceId')" prop="sourceId">·
        <el-input
          v-model="queryParams.sourceId"
          clearable
          style="width: 160px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="$t('Comment.UID')" prop="uid">
        <el-input
          v-model="queryParams.uid"
          clearable
          style="width: 160px"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item :label="$t('Comment.AuditStatus')" prop="auditStatus">
        <el-select
          v-model="queryParams.auditStatus"
          clearable
          style="width: 100px"
        >
          <el-option
            v-for="dict in dict.type.CommentAuditStatus"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button-group>
          <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
          <el-button icon="el-icon-refresh" size="small" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="commentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('Comment.SourceType')" prop="sourceType" width="120" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Comment.SourceId')" prop="sourceId" width="140" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Comment.Content')" prop="content" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Comment.AuditStatus')" align="center" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.CommentAuditStatus" :value="scope.row.auditStatus"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Comment.DelFlag')" align="center" width="80">
        <template slot-scope="scope">
          <el-tag type="danger" v-if="scope.row.delFlag==1">{{ $t('Comment.Deleted') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Comment.LikeCount')" align="center" width="80">
        <template slot-scope="scope">
          <el-link type="primary" @click="handleLikeList(scope.row)">{{ scope.row.likeCount }}</el-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Comment.ReplyCount')" align="center" width="80">
        <template slot-scope="scope">
          <el-link type="primary" @click="handleReplyList(scope.row)">{{ scope.row.replyCount }}</el-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Comment.Time')" align="center" prop="commentTime" width="150">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.commentTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Comment.Location')" prop="location" align="center" width="120" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Comment.ClientType')" prop="clientType" align="center" width="100" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope" v-if="scope.row.roleId !== 1">
          <el-button
            v-if="scope.row.auditStatus==0||scope.row.auditStatus==2"
            size="mini"
            type="text"
            icon="el-icon-circle-check"
            @click="handleAuditPass(scope.row)"
          >{{ $t('Comment.AuditPass') }}</el-button>
          <el-button
            v-if="scope.row.auditStatus==0||scope.row.auditStatus==1"
            size="mini"
            type="text"
            icon="el-icon-circle-close"
            @click="handleAuditNotPass(scope.row)"
          >{{ $t('Comment.AuditNotPass') }}</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >{{ $t('Common.Delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="loadCommentList"
    />
    <!-- 回复列表 -->
    <el-drawer
      direction="rtl"
      size="60%"
      :with-header="false"
      :visible.sync="replyVisible"
      :before-close="handleReplyListClose">
      <el-table v-loading="replyLoading" :data="replyList" @selection-change="handleReplySelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column :label="$t('Comment.ReplyContent')" prop="content" :show-overflow-tooltip="true" />
        <el-table-column :label="$t('Comment.AuditStatus')" align="center" width="100">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.CommentAuditStatus" :value="scope.row.auditStatus"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Comment.LikeCount')" align="center" width="80">
          <template slot-scope="scope">
            <el-link type="primary" @click="handleLikeList(scope.row)">{{ scope.row.likeCount }}</el-link>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Comment.ReplyTime')" align="center" prop="commentTime" width="150">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.commentTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Comment.Location')" prop="location" align="center" width="120" :show-overflow-tooltip="true" />
        <el-table-column :label="$t('Comment.ClientType')" prop="clientType" align="center" width="100" :show-overflow-tooltip="true" />
        <el-table-column :label="$t('Common.Operation')" align="center" class-name="small-padding fixed-width" width="220">
          <template slot-scope="scope" v-if="scope.row.roleId !== 1">
            <el-button
            v-if="scope.row.auditStatus==0||scope.row.auditStatus==2"
              size="small"
              type="text"
              icon="el-icon-circle-check"
              @click="handleAuditPass(scope.row)"
            >{{ $t('Comment.AuditPass') }}</el-button>
            <el-button
            v-if="scope.row.auditStatus==0||scope.row.auditStatus==1"
              size="small"
              type="text"
              icon="el-icon-circle-close"
              @click="handleAuditNotPass(scope.row)"
            >{{ $t('Comment.AuditNotPass') }}</el-button>
            <el-button
              size="small"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
            >{{ $t('Common.Delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="replyTotal>0"
        :total="replyTotal"
        :page.sync="replyQueryParams.pageNum"
        :limit.sync="replyQueryParams.pageSize"
        @pagination="loadReplyList"
      />
    </el-drawer>
    <comment-like-dialog :open.sync="likeVisible" :commentId="likeCommentId"></comment-like-dialog>
  </div>
</template>

<script>
import { getCommentList, getCommentReplyList, getCommentLikeList, deleteComments, auditComment } from "@/api/comment/comment";
import CommentLikeDialog from '@/views/comment/commentLike';

export default {
  name: "CommentCommentList",
  components: {
    "comment-like-dialog": CommentLikeDialog
  },
  dicts: [ 'CommentAuditStatus' ],
  data() {
    return {
      // 遮罩层
      loading: true,
      replyLoading: true,
      selectedCommentIds: [],
      commentMultiple: true,
      selectedReplyIds: [],
      replyMultiple: true,
      showSearch: false,
      // 总条数
      total: 0,
      replyTotal: 0,
      commentList: [],
      replyList: [],
      replyVisible: false,
      replyCommentId: "0",
      likeVisible: false,
      likeCommentId: "0",
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        auditStatus: undefined,
        sourceType: undefined,
        sourceId: undefined,
        uid: undefined
      },
      replyQueryParams: {
        pageNum: 1,
        pageSize: 10
      }
    };
  },
  created() {
    this.loadCommentList();
  },
  methods: {
    loadCommentList() {
      this.loading = true;
      getCommentList(this.queryParams).then(response => {
          this.commentList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        }
      );
    },
    loadReplyList(commentId) {
      this.replyLoading = true;
      getCommentReplyList(commentId, this.replyQueryParams).then(response => {
          this.replyList = response.data.rows;
          this.replyTotal = parseInt(response.data.total);
          this.replyLoading = false;
        }
      );
    },
    loadLikeList() {
      this.likeLoading = true;
      getCommentLikeList(this.likeQueryParams).then(response => {
          this.likeList = response.data.rows;
          this.likeTotal = parseInt(response.data.total);
          this.likeLoading = false;
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.loadCommentList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.selectedCommentIds = selection.map(item => item.commentId)
      this.commentMultiple = !selection.length
    },
    handleReplySelectionChange(selection) {
      this.selectedReplyIds = selection.map(item => item.commentId)
      this.replyMultiple = !selection.length
    },
    handleReplyList(row) {
      this.replyCommentId = row.commentId;
      this.replyVisible = true;
      this.replyQueryParams.pageNum = 1;
      this.loadReplyList(row.commentId);
    },
    handleReplyListClose() {
      this.replyVisible = false;
      this.replyCommentId = "0";
    },
    handleLikeList(row) {
      this.likeCommentId = row.commentId;
      this.likeVisible = true;
    },
    handleCommentLikeDialogClose() {
      this.likeVisible = false;
    },
    handleAudit(commentIds, auditFlag) {
      const data = { commentIds: commentIds, auditFlag: auditFlag };
      auditComment(data).then(response => {
          if (this.replyVisible) {
            this.loadReplyList(this.replyCommentId);
          } else {
            this.loadCommentList();
          }
        }
      );
    },
    handleAuditPass(row) {
      if (this.replyVisible) {
        const commentIds = row.commentId ? [ row.commentId ] : this.selectedReplyIds;
        this.handleAudit(commentIds, 'Y');
      } else {
        const commentIds = row.commentId ? [ row.commentId ] : this.selectedCommentIds;
        this.handleAudit(commentIds, 'Y');
      }
    },
    handleAuditNotPass(row) {
      if (this.replyVisible) {
        const commentIds = row.commentId ? [ row.commentId ] : this.selectedReplyIds;
        this.handleAudit(commentIds, 'N');
      } else {
        const commentIds = row.commentId ? [ row.commentId ] : this.selectedCommentIds;
        this.handleAudit(commentIds, 'N');
      }
    },
    handleDelete(row) {
      const commentIds = row.commentId ? [ row.commentId ] : this.selectedCommentIds;
      this.$modal.confirm(this.$t('Common.ConfirmDelete')).then(function() {
        return deleteComments(commentIds);
      }).then(() => {
        this.loadCommentList();
        this.$modal.msgSuccess(this.$t('Common.DeleteSuccess'));
      }).catch(() => {});
    }
  }
};
</script>