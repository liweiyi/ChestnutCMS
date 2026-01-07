<template>
  <div class="cms-link-container">
    <el-row :gutter="24" justify="space-between">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button 
              type="primary"
              icon="Plus"
              plain
              v-hasPermi="[ 'cms:friendlink:add' ]"
              @click="handleAdd">{{ $t("Common.Add") }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              type="success"
              icon="Edit"
              plain
              :disabled="single"
              v-hasPermi="[ 'cms:friendlink:add', 'cms:friendlink:edit' ]"
              @click="handleEdit">{{ $t('Common.Edit') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              type="danger"
              icon="Delete"
              plain
              :disabled="multiple"
              v-hasPermi="[ 'cms:friendlink:delete' ]"
              @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align: right;">
        <el-form 
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search"
          v-show="showSearch">
          <el-form-item prop="query">
            <el-input v-model="queryParams.query" :placeholder="$t('CMS.FriendLink.Placeholder.LinkQuery')">
            </el-input>
          </el-form-item>
          <el-form-item class="mr0">
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
      </el-col>
    </el-row>

    <el-table 
      v-loading="loading"
      :data="linkList"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="50">
        <template #default="scope"> 
          {{ tableRowNo(queryParams.pageNum, queryParams.pageSize, scope.$index + 1) }}
        </template>
      </el-table-column>
      <el-table-column label="Logo" align="left" width="100">
        <template #default="scope">
          <el-image 
            v-if="scope.row.src&&scope.row.src.length > 0" 
            :src="scope.row.src"
            style="height: 60px;"
            fit="scale-down">
          </el-image>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.FriendLink.LinkName')" align="left" prop="name">
      </el-table-column>
      <el-table-column :label="$t('CMS.FriendLink.LinkUrl')" align="left" prop="url"/>
      <el-table-column :label="$t('Common.UpdateTime')" align="center" width="160">
        <template #default="scope">
          <span v-if="scope.row.updateTime!=null">{{ parseTime(scope.row.updateTime) }}</span>
          <span v-else>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" width="140" >
        <template #default="scope">
          <el-button 
            link
            type="success"
            icon="Edit"
            v-hasPermi="[ 'cms:friendlink:add', 'cms:friendlink:edit' ]"
            @click="handleEdit(scope.row)">{{ $t('Common.Edit') }}</el-button>
          <el-button 
            link
            type="danger"
            icon="Delete"
            v-hasPermi="[ 'cms:friendlink:delete' ]"
            @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadListData"
    />
    <!-- 添加或修改弹窗 -->
    <el-dialog 
      :title="title"
      v-model="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('CMS.FriendLink.LinkName')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.FriendLink.LinkUrl')" prop="url">
          <el-input v-model="form.url" placeholder="http(s)://" />
        </el-form-item>
        <el-form-item label="Logo" prop="logo">
          <cms-logo-view 
            v-model="form.logo" 
            :width="218" 
            :height="150">
          </cms-logo-view>
        </el-form-item>
        <el-form-item :label="$t('CMS.FriendLink.SortFlag')" prop="sortFlag">
          <el-input-number v-model="form.sortFlag" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsLink">
import { urlValidator } from '@/utils/validate'
import { getLinkList, addLink, editLink, deleteLink } from "@/api/link/link";
import CmsLogoView from '@/views/cms/components/LogoView';

const props = defineProps({
  groupId: {
    type: String,
    required: true
  }
});

const { proxy } = getCurrentInstance();

const loading = ref(true);
const showSearch = ref(true);
const selectedRows = ref([]);
const linkList = ref([]);
const total = ref(0);
const title = ref("");
const open = ref(false);

const single = computed(() => {
  return selectedRows.value.length != 1
})
const multiple = computed(() => {
  return selectedRows.value.length <= 0
})

const data = reactive({
  queryParams: {
    query: undefined,
    groupId: undefined,
    pageSize: 20,
    pageNum: 1
  },
  form: {},
  rules: {
    name: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { max: 255, message: proxy.$t('Common.RuleTips.MaxLength', [255]), trigger: "blur" }
    ],
    url: [
      { required: true, validator: urlValidator, trigger: "blur" },
      { max: 255, message: proxy.$t('Common.RuleTips.MaxLength', [255]), trigger: "blur" }
    ],
    sortFlag: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ]
  }
})

const { queryParams, form, rules } = toRefs(data);

watch(() => props.groupId, (newVal) => {
  if (newVal && newVal.length > 0) {
    queryParams.value.groupId = newVal;
    loadListData();
  }
}, {
  immediate: true
});

function loadListData () {
  loading.value = true;
  getLinkList(queryParams.value).then(response => {
    response.data.rows.forEach(item => {
      item.sortFlag = parseInt(item.sortFlag);
    });
    linkList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}
function cancel () {
  open.value = false;
  reset();
}
function reset () {
  form.value = {};
}
function handleQuery () {
  queryParams.value.pageNo = 1;
  loadListData();
}
function resetQuery () {
  proxy.resetForm("queryFormRef");
  handleQuery();
}
function handleSelectionChange (selection) {
  selectedRows.value = selection.map(item => item);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}
function handleAdd () {
  reset();
  title.value = proxy.$t('CMS.FriendLink.AddLinkTitle');
  open.value = true;
}
function handleEdit (row) {
  reset();
  title.value = proxy.$t('CMS.FriendLink.EditLinkTitle');
  form.value = row;
  open.value = true;
}
function submitForm () {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      form.value.groupId = queryParams.value.groupId;
      if (form.value.linkId) {
        editLink(form.value).then(response => {
          proxy.$modal.msgSuccess(response.msg);
          open.value = false;
          loadListData();
        }); 
      } else {
        addLink(form.value).then(response => {
          proxy.$modal.msgSuccess(response.msg);
          open.value = false;
          loadListData();
        }); 
      }
    }
  });
}
function handleDelete (row) {
  const rows = row.linkId ? [{ linkId: row.linkId }] : selectedRows.value
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteLink(rows);
  }).then((response) => {
    proxy.$modal.msgSuccess(response.msg);
    loadListData();
  }).catch(() => {});
}
</script>