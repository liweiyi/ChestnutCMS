<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['vote:add']"
        >{{ $t('Common.Add') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap"  >
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['vote:add', 'vote:edit']"
        >{{ $t('Common.Edit') }}</el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['vote:delete']"
        >{{ $t('Common.Delete') }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadVoteList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" class="el-form-search">
        <el-form-item prop="title">
          <el-input
            v-model="queryParams.title"
            clearable
            :placeholder="$t('Vote.Title')"
            style="width: 240px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="status">
          <el-select
            v-model="queryParams.status"
            clearable
            :placeholder="$t('Vote.Status')"
            style="width: 100px"
          >
            <el-option
              v-for="dict in VoteStatus"
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
    </el-row>

    <el-table v-loading="loading" :data="voteList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('Common.RowNo')" width="55">
        <template #default="scope">
          {{ tableRowNo(queryParams.pageNum, queryParams.pageSize, scope.$index + 1) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('Vote.Code')" prop="code" width="120" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Vote.Title')" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-link type="primary" @click="handleVoteSubject(scope.row)" class="link-type">
            <span>{{ scope.row.title }}</span>
          </el-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Vote.TimeRange')" align="center" width="300">
        <template #default="scope">
          <span>{{ parseTime(scope.row.startTime) }} - {{ parseTime(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Vote.Status')" align="center" width="80">
        <template #default="scope">
          <dict-tag :options="VoteStatus" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Vote.Total')" align="center" prop="total" width="80" />
      <el-table-column :label="$t('Vote.UserType')" align="center" prop="userType" width="100" />
      <el-table-column :label="$t('Vote.ViewType')" align="center" width="100">
        <template #default="scope">
          <dict-tag :options="VoteViewType" :value="scope.row.viewType"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('Vote.DayAndTotalLimit')" align="center" width="140">
        <template #default="scope">
          {{ scope.row.dayLimit + " / " + scope.row.totalLimit }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('Common.Operation')" align="center" width="260">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Grid"
            @click="handleVoteSubject(scope.row)"
            v-hasPermi="['vote:add', 'vote:edit']"
          >{{ $t('Vote.EidtSubjects') }}</el-button>
          <el-button
            link
            type="success"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['vote:edit']"
          >{{ $t('Common.Edit') }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['vote:delete']"
          >{{ $t('Common.Delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadVoteList"
    />

    <el-dialog :title="title" v-model="open" width="560px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('Vote.Title')" prop="title">
          <el-input v-model="form.title" style="width:400px" />
        </el-form-item>
        <el-form-item :label="$t('Vote.Code')" prop="code">
          <el-input v-model="form.code" style="width:400px" />
        </el-form-item>
        <el-form-item :label="$t('Vote.TimeRange')" prop="timeRange">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="-"
            :start-placeholder="$t('Common.BeginTime')"
            :end-placeholder="$t('Common.EndTime')">
          </el-date-picker>
        </el-form-item>
        <el-form-item :label="$t('Vote.UserType')" prop="userType">
          <el-radio-group v-model="form.userType">
            <el-radio
              v-for="ut in userTypeOptions"
              :key="ut.value"
              :label="ut.value"
            >{{ ut.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Vote.DayLimit')" prop="dayLimit">
          <el-input-number v-model="form.dayLimit" controls-position="right" :min="1" />
        </el-form-item>
        <el-form-item :label="$t('Vote.TotalLimit')" prop="totalLimit">
          <el-input-number v-model="form.totalLimit" controls-position="right" :min="1" />
        </el-form-item>
        <el-form-item :label="$t('Vote.ViewType')" prop="viewType">
          <el-radio-group v-model="form.viewType">
            <el-radio
              v-for="dict in VoteViewType"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Vote.Status')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in VoteStatus"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')">
          <el-input v-model="form.remark" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t('Common.Confirm') }}</el-button>
        <el-button @click="cancel">{{ $t('Common.Cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="VoteIndex">
import { codeValidator } from '@/utils/validate'
import { getVoteUserTypes, getVoteList, getVoteDetail, addVote, updateVote, deleteVotes } from "@/api/vote/vote";

const { proxy } = getCurrentInstance();

const { VoteStatus, VoteViewType } = proxy.useDict('VoteStatus', 'VoteViewType');

const loading = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const showSearch = ref(true);
const total = ref(0);
const voteList = ref([]);
const title = ref("");
const open = ref(false);
const userTypeOptions = ref([]);
const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    status: undefined
  },
  form: {}
});
const { queryParams, form } = toRefs(data);
const rules = reactive({
  title: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  code: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
    { validator: codeValidator, trigger: "change" }
  ],
  timeRange: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  userType: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  dayLimit: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  totalLimit: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  status: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  viewType: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
});

onMounted(() => {
  loadVoteUserTypeOptions();
  loadVoteList();
});

function loadVoteUserTypeOptions() {
  getVoteUserTypes().then(response => {
    userTypeOptions.value = response.data;
  })
}
function loadVoteList() {
  loading.value = true;
  getVoteList(queryParams.value).then(response => {
    voteList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    voteList.value.forEach(vote => {
      vote.timeRange = [ vote.startTime, vote.endTime ]
    });
    loading.value = false;
  });
}
function cancel() {
  open.value = false;
  reset();
}
function reset() {
  proxy.resetForm("formRef");
  form.value = {};
}
function handleQuery() {
  queryParams.value.pageNum = 1;
  loadVoteList();
}
function resetQuery() {
  proxy.resetForm("queryFormRef");
  handleQuery();
}
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.voteId)
  single.value = selection.length!=1
  multiple.value = !selection.length
}
function handleAdd() {
  reset();
  form.value = { 
    status: '0',
    userType: 'ip',
    dayLimit: 1,
    totalLimit: 1
  }
  open.value = true;
  title.value = proxy.$t('Vote.AddVoteTitle');
}
function handleUpdate(row) {
  reset();
  const voteId = row.voteId || ids.value[0];
  getVoteDetail(voteId).then(response => {
    form.value = response.data;
    form.value.timeRange = [ form.value.startTime, form.value.endTime ]
    open.value = true;
    title.value = proxy.$t('Vote.EditVoteTitle');
  });
}
function handleVoteSubject(row) {
  const voteId = row.voteId;
  proxy.$router.push({ path: "/operations/vote/subjects", query: { voteId: voteId } });
}
function submitForm() {
  proxy.$refs.formRef.validate(valid => {
    if (valid) {
      form.value.startTime = form.value.timeRange[0];
      form.value.endTime = form.value.timeRange[1];
      if (form.value.voteId != undefined) {
        updateVote(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.Success'));
          open.value = false;
          loadVoteList();
        });
      } else {
        form.value.source = `cms:${proxy.$cms.getCurrentSite()}`;
        addVote(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
          open.value = false;
          loadVoteList();
        });
      }
    }
  });
}
function handleDelete(row) {
  const voteIds = row.voteId ? [ row.voteId ] : ids.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function() {
    return deleteVotes(voteIds);
  }).then(() => {
    loadVoteList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(() => {});
}
</script>