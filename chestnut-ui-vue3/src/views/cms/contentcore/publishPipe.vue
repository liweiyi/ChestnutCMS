<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button 
          plain
          type="primary"
          icon="Plus"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="Edit"
          :disabled="selectedIds.length != 1"
          @click="handleUpdate">{{ $t("Common.Edit") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="danger"
          icon="Delete"
          :disabled="selectedIds.length == 0"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
        </el-col>
    </el-row>

    <el-table v-loading="loading" :data="publishPipeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="publishpipeId" width="180" />
      <el-table-column :label="$t('CMS.PublishPipe.Name')" align="center" prop="name" />
      <el-table-column :label="$t('CMS.PublishPipe.Code')" align="center" width="80" prop="code" />
      <el-table-column :label="$t('Common.Sort')" align="center" width="80" prop="sort" />
      <el-table-column :label="$t('CMS.PublishPipe.Status')" align="center" prop="state">
        <template #default="scope">
          <dict-tag :options="EnableOrDisable" :value="scope.row.state"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" width="180">
        <template #default="scope">
          <el-button 
            type="text"
            icon="Edit"
            @click="handleUpdate(scope.row)">{{ $t("Common.Edit") }}</el-button>
          <el-button 
            type="text"
            icon="Delete"
            @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改发布通道对话框 -->
    <el-dialog 
      :title="title"
      v-model="open"
      width="500px"
      append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('CMS.PublishPipe.Name')" prop="name">
          <el-input v-model="form.name"/>
        </el-form-item>
        <el-form-item :label="$t('CMS.PublishPipe.Code')" prop="code">
          <el-input v-model="form.code" :disabled="$tools.isNotEmpty(form.publishpipeId)" />
        </el-form-item>
        <el-form-item :label="$t('CMS.PublishPipe.Status')" prop="state">
          <el-radio-group v-model="form.state">
            <el-radio
              v-for="dict in EnableOrDisable"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Sort')" prop="sort">
          <el-input-number v-model="form.sort" :min="1"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t("Common.Save") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsContentcorePublishPipe">
import { codeValidator } from '@/utils/validate';
import { getPublishPipeList, getPublishPipeData, addPublishPipe, updatePublishPipe, delPublishPipe } from "@/api/contentcore/publishpipe";

const { proxy } = getCurrentInstance()
const { EnableOrDisable } = proxy.useDict('EnableOrDisable')

const loading = ref(true)
const selectedIds = ref([])
const publishPipeList = ref([])
const total = ref(0)
const open = ref(false)
const title = ref("")
const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
  },
  // 表单参数
  form: {},
  // 表单校验
  rules: {
    name: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    ],
    code :[
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { validator: codeValidator, trigger: "change" },
    ],
    state :[
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    sort :[
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ]
  }
})
const { queryParams, form, rules } = toRefs(objects)

onMounted(() => {
  getList()
})

const getList = () => {
  loading.value = true
  getPublishPipeList(queryParams.value).then(response => {
    publishPipeList.value = response.data.rows
    total.value = parseInt(response.data.total)
    loading.value = false
  })
}

const cancel = () => {
  open.value = false
  reset()
}

const reset = () => {
  proxy.resetForm("formRef")
  form.value = { state : "0" }
}

const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.publishpipeId)
}

const handleAdd = () => {
  reset()
  open.value = true
  title.value = proxy.$t('CMS.PublishPipe.AddDialogTitle')
}

const handleUpdate = (row) => {
  reset()
  const publishpipeId = row.publishpipeId || selectedIds.value
  getPublishPipeData(publishpipeId).then(response => {
    form.value = response.data
    open.value = true
    title.value = proxy.$t('CMS.PublishPipe.EditDialogTitle')
  })
}

const submitForm = () => {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.publishpipeId != undefined) {
        updatePublishPipe(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'))
          open.value = false
          getList()
        })
      } else {
        addPublishPipe(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'))
          open.value = false
          getList()
        })
      }
    }
  })
}

const handleDelete = (row) => {
  const publishpipeIds = row.publishpipeId ? [ row.publishpipeId ] : selectedIds.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(() => {
    return delPublishPipe(publishpipeIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'))
  })
}
</script>