<template>
  <div class="app-container">
    <el-row justify="space-between" :gutter="24">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5" class="permi-wrap">
            <el-button 
              type="primary"
              icon="Plus"
              plain
              v-hasPermi="['cms:friendlink:add']"
              @click="handleAdd">{{ $t("Common.Add") }}</el-button>
          </el-col>
          <el-col :span="1.5" class="permi-wrap">
            <el-button 
              type="success"
              icon="Edit"
              plain
              :disabled="single"
              v-hasPermi="[ 'cms:friendlink:add', 'cms:friendlink:edit' ]"
              @click="handleEdit">{{ $t("Common.Edit") }}</el-button>
          </el-col>
          <el-col :span="1.5" class="permi-wrap">
            <el-button 
              type="danger"
              icon="Delete"
              plain
              :disabled="multiple"
              v-hasPermi="['cms:friendlink:delete']"
              @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align: right;">
        <el-form 
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input v-model="queryParams.query" :placeholder="$t('CMS.FriendLink.Placeholder.GroupQuery')">
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

    <el-table v-loading="loading" :data="linkGroupList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column type="index" :label="$t('Common.RowNo')" align="center" width="50">
        <template #default="scope">
          {{ tableRowNo(queryParams.pageNum, queryParams.pageSize, scope.$index + 1) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.FriendLink.GroupName')" align="left" prop="name">
        <template #default="scope">
          <el-button type="text" @click="handleGroupClick(scope.row)">{{ scope.row.name }}</el-button>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.FriendLink.GroupCode')" align="left" prop="code"/>
      <el-table-column :label="$t('Common.UpdateTime')" align="center" width="160">
        <template #default="scope">
          <span v-if="scope.row.updateTime!=null">{{ parseTime(scope.row.updateTime) }}</span>
          <span v-else>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" width="180">
        <template #default="scope">
          <el-button 
            link
            type="success"
            icon="Edit"
            v-hasPermi="[ 'cms:friendlink:add', 'cms:friendlink:edit' ]"
            @click="handleEdit(scope.row)"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button 
            link
            type="danger"
            icon="Delete"
            v-hasPermi="[ 'cms:friendlink:delete' ]"
            @click="handleDelete(scope.row)"
          >{{ $t("Common.Delete") }}</el-button>
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
        <el-form-item :label="$t('CMS.FriendLink.GroupName')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.FriendLink.GroupCode')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item v-if="form.linkGroupId" :label="$t('CMS.FriendLink.SortFlag')" prop="sortFlag">
          <el-input-number v-model="form.sortFlag" controls-position="right" :min="0" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>

    <el-drawer
      :title="$t('CMS.FriendLink.LinkListTitle')"
      v-model="openLinkList"
      :close-on-click-modal="false"
      size="50%"
      append-to-body>
      <cms-link-list :group-id="currentGroupId" />
    </el-drawer>
  </div>
</template>
<style scoped>
</style>
<script setup name="CmsLinkLinkGroup">
import { codeValidator } from '@/utils/validate'
import { getLinkGroupList, addLinkGroup, editLinkGroup, deleteLinkGroup } from "@/api/link/linkGroup";
import CmsLinkList from './link';

const { proxy } = getCurrentInstance();

const loading = ref(true);
const selectedRows = ref([]);
const linkGroupList = ref([]);
const total = ref(0)
const title = ref("");
const open = ref(false);
const openLinkList = ref(false);
const currentGroupId = ref("");
const single = computed(() => {
  return selectedRows.value.length != 1
})
const multiple = computed(() => {
  return selectedRows.value.length <= 0
})

const _data = reactive({
  queryParams: {
    query: undefined,
    pageSize: 20,
    pageNum: 1
  },
  form: {},
  rules: {
    name: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
        ],
    code: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
      { validator: codeValidator, trigger: "change" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(_data);

onMounted(() => {
  loadListData();
})

function loadListData () {
  loading.value = true;
  getLinkGroupList(queryParams.value).then(response => {
    linkGroupList.value = response.data.rows;
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
  queryParams.value.pageNum = 1;
  loadListData();
}
function resetQuery () {
  proxy.resetForm("queryFormRef");
  handleQuery();
}
function handleSelectionChange (selection) {
  selectedRows.value = selection.map(item => item);
}
function handleAdd () {
  reset();
  title.value = proxy.$t('CMS.FriendLink.AddTitle')
  open.value = true;
}
function handleEdit (row) {
  reset();
  title.value = proxy.$t('CMS.FriendLink.EditTitle')
  form.value = row;
  open.value = true;
}
function submitForm () {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.linkGroupId) {
        editLinkGroup(form.value).then(response => {
          proxy.$modal.msgSuccess(response.msg);
          open.value = false;
          loadListData();
        }); 
      } else {
        addLinkGroup(form.value).then(response => {
          proxy.$modal.msgSuccess(response.msg);
          open.value = false;
          loadListData();
        }); 
      }
    }
  });
}
function handleDelete (row) {
  const rows = row.linkGroupId ? [ row ] : selectedRows.value
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteLinkGroup(rows);
  }).then((response) => {
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
    loadListData();
  }).catch(() => {});
}

function handleGroupClick(row) {
  currentGroupId.value = row.linkGroupId;
  openLinkList.value = true;
}
</script>