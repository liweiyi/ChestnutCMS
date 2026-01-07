<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="success" 
          plain 
          icon="Check"
          :disabled="commentMultiple" 
          v-hasPermi="['comment:audit']"
          @click="handleAuditPass">
          {{ $t('Comment.AuditPass') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="warning" 
          plain 
          icon="Close"
          :disabled="commentMultiple"
          v-hasPermi="['comment:audit']"
          @click="handleAuditNotPass">
          {{ $t('Comment.AuditNotPass') }}
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="danger" 
          plain 
          icon="Delete"
          :disabled="commentMultiple" 
          v-hasPermi="['comment:delete']"
          @click="handleDelete">
          {{ $t('Common.Delete') }}
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadCommentList"></right-toolbar>
    </el-row>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" class="el-form-search">
      <el-form-item prop="sourceType">
        <el-input
          v-model="queryParams.sourceType"
          clearable
          :placeholder="$t('Comment.SourceType')"
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="sourceId">
        <el-input
          v-model="queryParams.sourceId"
          clearable
          :placeholder="$t('Comment.SourceId')"
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="uid">
        <el-input
          v-model="queryParams.uid"
          clearable
          :placeholder="$t('Comment.UID')"
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="auditStatus">
        <el-select
          v-model="queryParams.auditStatus"
          clearable
          :placeholder="$t('Comment.AuditStatus')"
          style="width: 100px"
        >
          <el-option
            v-for="dict in CommentAuditStatus"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button-group>
          <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
        </el-button-group>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="commentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('Comment.SourceType')" prop="sourceType" width="120" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Comment.SourceId')" prop="sourceId" width="140" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Comment.Content')" prop="content" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Comment.AuditStatus')" align="center" width="100">
        <template #default="scope">
          <dict-tag :options="CommentAuditStatus" :value="scope.row.auditStatus"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Comment.DelFlag')" align="center" width="80">
        <template #default="scope">
          <el-tag type="danger" v-if="scope.row.delFlag==1">{{ $t('Comment.Deleted') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Comment.LikeCount')" align="center" width="80">
        <template #default="scope">
          <el-link type="primary" @click="handleLikeList(scope.row)">{{ scope.row.likeCount }}</el-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Comment.ReplyCount')" align="center" width="80">
        <template #default="scope">
          <el-link type="primary" @click="handleReplyList(scope.row)">{{ scope.row.replyCount }}</el-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Comment.Time')" align="center" prop="commentTime" width="150">
        <template #default="scope">
          <span>{{ parseTime(scope.row.commentTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Comment.Location')" prop="location" align="center" width="120" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Comment.ClientType')" prop="clientType" align="center" width="100" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.Operation')" align="center" width="220">
        <template #default="scope">
          <el-button
            v-if="scope.row.auditStatus==0||scope.row.auditStatus==2"
            type="text"
            icon="Check"
            @click="handleAuditPass(scope.row)"
          >{{ $t('Comment.AuditPass') }}</el-button>
          <el-button
            v-if="scope.row.auditStatus==0||scope.row.auditStatus==1"
            type="text"
            icon="Close"
            @click="handleAuditNotPass(scope.row)"
          >{{ $t('Comment.AuditNotPass') }}</el-button>
          <el-button
            v-if="scope.row.delFlag==0"
            type="text"
            icon="Delete"
            @click="handleDelete(scope.row)"
          >{{ $t('Common.Delete') }}</el-button>
          <el-button
            v-if="scope.row.delFlag==1"
            type="text"
            icon="RefreshLeft"
            @click="handleRecover(scope.row)"
          >{{ $t('Common.Recover') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadCommentList"
    />
    <!-- 回复列表 -->
    <el-drawer
      direction="rtl"
      size="70%"
      :with-header="false"
      v-model="replyVisible"
      :before-close="handleReplyListClose">
      <el-table v-loading="replyLoading" :data="replyList" @selection-change="handleReplySelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column :label="$t('Comment.ReplyContent')" prop="content" :show-overflow-tooltip="true" />
        <el-table-column :label="$t('Comment.AuditStatus')" align="center" width="100">
          <template #default="scope">
            <dict-tag :options="CommentAuditStatus" :value="scope.row.auditStatus"/>
          </template>
        </el-table-column>
      <el-table-column :label="$t('Comment.DelFlag')" align="center" width="80">
        <template #default="scope">
          <el-tag type="danger" v-if="scope.row.delFlag==1">{{ $t('Comment.Deleted') }}</el-tag>
        </template>
      </el-table-column>
        <el-table-column :label="$t('Comment.LikeCount')" align="center" width="80">
          <template #default="scope">
            <el-link type="primary" @click="handleLikeList(scope.row)">{{ scope.row.likeCount }}</el-link>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Comment.ReplyTime')" align="center" prop="commentTime" width="150">
          <template #default="scope">
            <span>{{ parseTime(scope.row.commentTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Comment.Location')" prop="location" align="center" width="120" :show-overflow-tooltip="true" />
        <el-table-column :label="$t('Comment.ClientType')" prop="clientType" align="center" width="100" :show-overflow-tooltip="true" />
        <el-table-column :label="$t('Common.Operation')" align="center" width="220">
          <template #default="scope">
            <el-button
            v-if="scope.row.auditStatus==0||scope.row.auditStatus==2"
              type="text"
              icon="Check"
              @click="handleAuditPass(scope.row)"
            >{{ $t('Comment.AuditPass') }}</el-button>
            <el-button
            v-if="scope.row.auditStatus==0||scope.row.auditStatus==1"
              type="text"
              icon="Close"
              @click="handleAuditNotPass(scope.row)"
            >{{ $t('Comment.AuditNotPass') }}</el-button>
            <el-button
              v-if="scope.row.delFlag==0"
              type="text"
              icon="Delete"
              @click="handleDelete(scope.row)"
            >{{ $t('Common.Delete') }}</el-button>
            <el-button
              v-if="scope.row.delFlag==1"
              type="text"
              :icon="RefreshLeft"
              @click="handleRecover(scope.row)"
            >{{ $t('Common.Recover') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="replyTotal>0"
        :total="replyTotal"
        v-model:page="replyQueryParams.pageNum"
        v-model:limit="replyQueryParams.pageSize"
        @pagination="loadReplyList"
      />
    </el-drawer>
    <comment-like-dialog v-model:open="likeVisible" :commentId="likeCommentId"></comment-like-dialog>
  </div>
</template>

<script setup name="CommentList">
import { getCommentList, getCommentReplyList, deleteComments, auditComment, recoverComments } from "@/api/comment/comment"
import CommentLikeDialog from '@/views/comment/commentLike'

const { proxy } = getCurrentInstance()
const { CommentAuditStatus } = proxy.useDict('CommentAuditStatus')

// 遮罩层
const loading = ref(true)
const replyLoading = ref(true)
const selectedCommentIds = ref([])
const commentMultiple = ref(true)
const selectedReplyIds = ref([])
const replyMultiple = ref(true)
const showSearch = ref(false)
// 总条数
const total = ref(0)
const replyTotal = ref(0)
const commentList = ref([])
const replyList = ref([])
const replyVisible = ref(false)
const replyCommentId = ref("0")
const likeVisible = ref(false)
const likeCommentId = ref("0")
// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  auditStatus: undefined,
  sourceType: undefined,
  sourceId: undefined,
  uid: undefined
})
const replyQueryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

onMounted(() => {
  loadCommentList()
})

const loadCommentList = () => {
  loading.value = true
  getCommentList(queryParams).then(response => {
    commentList.value = response.data.rows
    total.value = parseInt(response.data.total)
    loading.value = false
  })
}

const loadReplyList = (commentId) => {
  replyLoading.value = true
  getCommentReplyList(commentId, replyQueryParams).then(response => {
    replyList.value = response.data.rows
    replyTotal.value = parseInt(response.data.total)
    replyLoading.value = false
  })
}

const handleQuery = () => {
  queryParams.pageNum = 1
  loadCommentList()
}

const resetQuery = () => {
  proxy.resetForm("queryFormRef")
  handleQuery()
}

const handleSelectionChange = (selection) => {
  selectedCommentIds.value = selection.map(item => item.commentId)
  commentMultiple.value = !selection.length
}

const handleReplySelectionChange = (selection) => {
  selectedReplyIds.value = selection.map(item => item.commentId)
  replyMultiple.value = !selection.length
}

const handleReplyList = (row) => {
  replyCommentId.value = row.commentId
  replyVisible.value = true
  replyQueryParams.pageNum = 1
  loadReplyList(row.commentId)
}

const handleReplyListClose = () => {
  replyVisible.value = false
  replyCommentId.value = "0"
}

const handleLikeList = (row) => {
  likeCommentId.value = row.commentId
  likeVisible.value = true
}

const handleAudit = (commentIds, auditFlag) => {
  const data = { commentIds: commentIds, auditFlag: auditFlag }
  auditComment(data).then(response => {
    if (replyVisible.value) {
      loadReplyList(replyCommentId.value)
    } else {
      loadCommentList()
    }
  })
}

const handleAuditPass = (row) => {
  if (replyVisible.value) {
    const commentIds = row.commentId ? [ row.commentId ] : selectedReplyIds.value
    handleAudit(commentIds, 'Y')
  } else {
    const commentIds = row.commentId ? [ row.commentId ] : selectedCommentIds.value
    handleAudit(commentIds, 'Y')
  }
}

const handleAuditNotPass = (row) => {
  if (replyVisible.value) {
    const commentIds = row.commentId ? [ row.commentId ] : selectedReplyIds.value
    handleAudit(commentIds, 'N')
  } else {
    const commentIds = row.commentId ? [ row.commentId ] : selectedCommentIds.value
    handleAudit(commentIds, 'N')
  }
}

const handleDelete = (row) => {
  const commentIds = row.commentId ? [ row.commentId ] : selectedCommentIds.value
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteComments(commentIds)
  }).then(() => {
    if (replyVisible.value) {
      loadReplyList(replyCommentId.value)
    } else {
      loadCommentList()
    }
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'))
  }).catch(() => {})
}

const handleRecover = (row) => {
  const commentIds = row.commentId ? [ row.commentId ] : selectedCommentIds.value
  recoverComments(commentIds).then(response => {
    if (replyVisible.value) {
      loadReplyList(replyCommentId.value)
    } else {
      loadCommentList()
    }
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'))
  })
}
</script>