<template>
  <div class="app-container">
    <el-dialog 
      :title="$t('Comment.LikeList')"
      v-model="visible"
      width="700px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form 
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
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
            icon="Search"
            @click="handleQuery">
            {{ $t("Common.Search") }}
          </el-button>
          <el-button
            icon="Refresh"
            @click="resetQuery">
            {{ $t("Common.Reset") }}
          </el-button>
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
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="loadCommentLikeList"
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleClose">{{ $t("Common.Close") }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CommentLike">
import { getCommentLikeList } from "@/api/comment/comment"

const props = defineProps({
  commentId: {
    type: String,
    required: true
  },
  open: {
    type: Boolean,
    default: false,
    required: true
  }
})

const emit = defineEmits(['update:open'])

const { proxy } = getCurrentInstance()

const loading = ref(false)
const visible = ref(props.open)
const likeList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageSize: 8,
  pageNum: 1,
  uid: undefined
})

watch(() => props.open, (newVal) => {
  visible.value = newVal
})

watch(visible, (newVal) => {
  if (!newVal) {
    handleClose()
  } else {
    loadCommentLikeList()
  }
})

const loadCommentLikeList = () => {
  if (!visible.value) {
    return
  }
  loading.value = true
  getCommentLikeList(props.commentId, queryParams).then(response => {
    likeList.value = response.data.rows
    total.value = parseInt(response.data.total)
    loading.value = false
  })
}

const handleClose = () => {
  emit("update:open", false)
  queryParams.uid = undefined
  queryParams.pageNum = 1
  likeList.value = []
}

const handleQuery = () => {
  queryParams.pageNum = 1
  loadCommentLikeList()
}

const resetQuery = () => {
  proxy.resetForm("queryFormRef")
  queryParams.uid = undefined
  handleQuery()
}
</script>