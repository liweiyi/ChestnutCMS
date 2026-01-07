<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          v-if="subjectTableExpand"
          type="primary"
          plain
          icon="Minus"
          @click="handleTableExpand"
        >{{ $t('Common.Collapse') }}</el-button>
        <el-button
          v-else
          type="primary"
          plain
          icon="Plus"
          @click="handleTableExpand"
        >{{ $t('Common.Expand') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAddVoteSubject"
          v-hasPermi="['vote:add', 'vote:edit']"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleEditVoteSubject"
          v-hasPermi="['vote:add', 'vote:edit']"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDeleteVoteSubject"
          v-hasPermi="['vote:add', 'vote:edit']"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Close"
          @click="handleClose"
        >{{ $t('Common.Close') }}</el-button>
      </el-col>
    </el-row>
    
    <el-table v-loading="loading" ref="subjectTableRef" :data="subjectList" @selection-change="handleSelectionChange">
      <el-table-column type="expand">
        <template #default="scope">
          <el-table v-if="scope.row.type!=='input'"  :show-header="false" :data="scope.row.itemList">
            <el-table-column type="index" align="right" width="100">
              <template #default="scope2">
                {{ subjectItemIndex(scope2.$index) }}
              </template>
            </el-table-column>
            <el-table-column width="120">
              <template #default="scope2">
                <el-select v-model="scope2.row.type" disabled>
                  <el-option
                    v-for="type in subjectItemTypes"
                    :key="type.value"
                    :label="type.label"
                    :value="type.value"
                  />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column>
              <template #default="scope2">
                {{ scope2.row.content }}
              </template>
            </el-table-column>
            <el-table-column :lable="$t('Vote.ItemTotal')" width="200">
              <template #default="scope2">
                {{ $t('Vote.ItemTotal') }}：{{ scope2.row.total }}
                <el-progress :percentage="itemProgressPercent(scope2.row)"></el-progress> 
              </template>
            </el-table-column>
          </el-table>
          <div v-else style="background-color: #f4f4f5;color: #909399;font-size:12px;line-height: 30px;padding-left:10px;display: flex;align-items: center;">
            <el-icon :size="16" class="mr5"><InfoFilled /></el-icon>{{ $t('Vote.InputItemTip') }}
          </div>
        </template>
      </el-table-column>
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column type="index" :label="$t('Common.RowNo')" width="50">
      </el-table-column>
      <el-table-column :label="$t('Vote.Subject')">
        <template #default="scope">
          <el-link v-if="scope.row.type!='input'" type="primary" @click="handleItemList(scope.row)">{{ scope.row.title }}</el-link>
          <span v-else>{{ scope.row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Vote.SubjectType')" align="center" prop="type" width="140">
        <template #default="scope">
          <dict-tag :options="VoteSubjectType" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Common.Operation')"
        width="300"
        align="center"
       >
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Plus"
            v-hasPermi="['vote:add', 'vote:edit']"
            @click="handleAddVoteSubject(scope.row)"
          >{{ $t('Vote.InsertSubject') }}</el-button>
          <el-button
            link
            type="success"
            icon="Edit"
            v-hasPermi="['vote:add', 'vote:edit']"
            @click="handleEditVoteSubject(scope.row)"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            v-hasPermi="['vote:add', 'vote:edit']"
            @click="handleDeleteVoteSubject(scope.row)"
          >{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('Vote.SubjectTitle')" prop="title">
          <el-input v-model="form.title" style="width:400px" />
        </el-form-item>
        <el-form-item :label="$t('Vote.SubjectType')" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio 
              v-for="dict in VoteSubjectType"
              :key="dict.value"
              :label="dict.value">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t('Common.Confirm') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </template>
    </el-dialog>
    <!-- 选项列表 -->
    <el-drawer
      direction="rtl"
      size="60%"
      :with-header="false"
      v-model="openSubjectItems"
      :before-close="handleSubjectItemsClose">
      <el-row :gutter="10" style="padding:10px">
        <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="Edit"
            @click="handleSaveItems"
            v-hasPermi="['vote:add', 'vote:edit']"
          >{{ $t("Common.Save") }}</el-button>
        </el-col>
      </el-row>
      <el-table v-loading="loadingItems" :data="itemList">
        <el-table-column align="center" width="60">
          <template #default="scope">
            <el-button
              circle
              plain
              v-if="scope.$index>0"
              type="success"
              icon="Top"
              @click="handleUpItem(scope.$index)"
            ></el-button>
          </template>
        </el-table-column>
        <el-table-column align="center" width="60">
          <template #default="scope">
            <el-button
              circle
              plain
              v-if="scope.$index<itemList.length-1"
              type="warning"
              icon="Bottom"
              @click="handleDownItem(scope.$index)"
            ></el-button>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Common.RowNo')" type="index" align="center" width="80">
          <template #default="scope">
            {{ subjectItemIndex(scope.$index) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('Vote.SubjectItemType')" width="120">
          <template #default="scope">
            <el-select v-model="scope.row.type">
              <el-option
                v-for="type in subjectItemTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Common.Details')">
          <template #default="scope">
            <el-input v-if="scope.row.type==='Text'" type="text" v-model="scope.row.content"></el-input>
          </template>
        </el-table-column>
        <el-table-column width="220">
          <template #default="scope">
            <el-button
              plain
              type="primary"
              icon="Plus"
              @click="handleAddItem(scope.$index)">{{ $t('Vote.InsertSubjectItem') }}</el-button>
            <el-button
              plain
              type="danger"
              icon="Delete"
              @click="handleDeleteItem(scope.$index)">{{ $t("Common.Delete") }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-row :gutter="10" style="padding:10px">
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            icon="Plus"
            style="float:right"
            @click="handleAddItem(itemList.length)"
          >{{ $t('Vote.AddSubjectItem') }}</el-button>
        </el-col>
      </el-row>
    </el-drawer>
  </div>
</template>

<script setup name="VoteSubjectList">
import { getVoteItemTypes } from "@/api/vote/vote";
import { getVoteSubjectList, getVoteSubjectDetail, addVoteSubject, updateVoteSubject, deleteVoteSubjects, getSubjectItems, saveSubjectItems } from "@/api/vote/subject";

const { proxy } = getCurrentInstance()
const { VoteSubjectType } = proxy.useDict('VoteSubjectType')

const voteId = ref(proxy.$route.query.voteId)
const loading = ref(true)
const loadingItems = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const title = ref("")
const open = ref(false)
const itemIndexName = ref([ 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' ])
const subjectItemTypes = ref([])
const subjectList = ref([])
const subjectTableExpand = ref(false)
const openSubjectItems = ref(false)
const currentSubjectId = ref(undefined)
const itemList = ref([])

const data = reactive({
  form: {},
  rules: {
    title: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
    type: [
      { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
    ],
  },
})
const { form, rules } = toRefs(data)

onMounted(() => {
  loadVoteSubjectItemTypes();
  loadVoteSubjectList();
})

function itemProgressPercent(row) {
  return row.voteTotal > 0 ? parseInt(row.total*100/row.voteTotal) : 0;
}
function subjectItemIndex(index) {
  let num = parseInt(index / 26);
  index = index % 26;
  let name = itemIndexName.value[index];
  if (num > 0) {
    name = name + num;
  }
  return name;
}
function loadVoteSubjectItemTypes() {
      getVoteItemTypes().then(response => {
    subjectItemTypes.value = response.data;
  });
}
function loadVoteSubjectList() {
  loading.value = true;
  getVoteSubjectList(voteId.value).then(response => {
    subjectList.value = response.data.rows;
    loading.value = false;
  });
}
function loadVoteSubjectItemList() {
  loadingItems.value = true;
  getSubjectItems(currentSubjectId.value).then(response => {
    itemList.value = response.data.rows;
    loadingItems.value = false;
  });
}
function cancel() {
  open.value = false;
  reset();
}
function reset() {
  form.value = {
    title: undefined,
    status: undefined,
    remark: undefined
  };
  proxy.resetForm("formRef");
}
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.subjectId)
  single.value = selection.length!=1
  multiple.value = !selection.length
}
function handleAddVoteSubject(row) {
  reset();
  form.value = { 
    type: 'radio',
    nextSubjectId: row.subjectId || undefined
  }
  open.value = true;
  title.value = proxy.$t('Vote.AddSubjectTitle');
}
function handleEditVoteSubject(row) {
  reset();
  const subjectId = row.subjectId || ids.value[0];
  getVoteSubjectDetail(subjectId).then(response => {
    form.value = response.data;
    title.value = proxy.$t('Vote.EditSubjectTitle');
    open.value = true;
  });
}
function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      if (form.value.subjectId != undefined) {
        updateVoteSubject(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.Success'));
          open.value = false;
          loadVoteSubjectList();
        });
      } else {
        form.value.voteId = voteId.value;
        addVoteSubject(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
          open.value = false;
          loadVoteSubjectList();
        });
      }
    }
  });
}
function handleDeleteVoteSubject(row) {
  const subjectIds = row.subjectId ? [ row.subjectId ] : ids.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteVoteSubjects(subjectIds);
  }).then(() => {
    loadVoteSubjectList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(() => {});
}
function handleItemList(row) {
  currentSubjectId.value = row.subjectId;
  loadVoteSubjectItemList();
  openSubjectItems.value = true;
}
function handleSubjectItemsClose() {
  currentSubjectId.value = undefined;
  openSubjectItems.value = false;
  loadVoteSubjectList();
}
function handleAddItem(rowIndex) {
  itemList.value.splice(rowIndex, 0, { type: "Text" });
}
function handleDeleteItem(rowIndex) {
  itemList.value.splice(rowIndex, 1);
}
function handleUpItem(rowIndex) {
  itemList.value.splice(rowIndex,1,...itemList.value.splice(rowIndex - 1, 1 , itemList.value[rowIndex]));
}
function handleDownItem(rowIndex) {
  itemList.value.splice(rowIndex + 1,1,...itemList.value.splice(rowIndex, 1 , itemList.value[rowIndex + 1]));
}
function handleSaveItems() {
  const data = { subjectId: currentSubjectId.value, itemList: itemList.value }
  saveSubjectItems(data).then(response => {
    loadVoteSubjectItemList();
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
  });
}
function handleClose() {
  const obj = { path: "/operations/vote" };
  proxy.$tab.closeOpenPage(obj);
}
function handleTableExpand() {
  subjectList.value.forEach(row => proxy.$refs.subjectTableRef.toggleRowExpansion(row, !subjectTableExpand.value));
  subjectTableExpand.value = !subjectTableExpand.value;
}
</script>