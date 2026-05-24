<template>
  <el-dialog
    :title="$t('Member.Selector.Title')"
    v-model="selectVisible"
    width="1100px"
    append-to-body
    :close-on-click-modal="false"
  >
    <cc-spliter storage-key="member-selector">
      <cc-spliter-column width="68%" style="padding-right: 10px;">
        <div class="mb8">{{ $t('Member.Selector.MemberList') }}</div>
        <el-form
          :model="queryParams"
          ref="selectQueryRef"
          :inline="true"
          class="el-form-search"
          @submit.prevent
        >
          <el-form-item prop="userName">
            <el-input
              v-model="queryParams.userName"
              :placeholder="$t('Member.UserName')"
              clearable
              style="width: 120px"
              @keyup.enter="handleSelectQuery"
            />
          </el-form-item>
          <el-form-item prop="nickName">
            <el-input
              v-model="queryParams.nickName"
              :placeholder="$t('Member.NickName')"
              clearable
              style="width: 120px"
              @keyup.enter="handleSelectQuery"
            />
          </el-form-item>
          <el-form-item prop="status">
            <el-select
              v-model="queryParams.status"
              clearable
              :placeholder="$t('Member.Status')"
              style="width: 90px"
            >
              <el-option
                v-for="dict in MemberStatus"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button type="primary" icon="Search" @click="handleSelectQuery">{{
                $t('Common.Search')
              }}</el-button>
              <el-button icon="Refresh" @click="resetSelectQuery">{{ $t('Common.Reset') }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
        <el-table
          ref="selectTableRef"
          :data="selectMemberList"
          size="small"
          row-key="memberId"
          @row-click="clickRow"
          @selection-change="handleSelectChange"
          height="370px"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column
            :label="$t('Member.UserName')"
            prop="userName"
            :show-overflow-tooltip="true"
          />
          <el-table-column
            :label="$t('Member.NickName')"
            prop="nickName"
            :show-overflow-tooltip="true"
          />
          <el-table-column :label="$t('Member.Status')" align="center" prop="status" width="100">
            <template #default="scope">
              <dict-tag :options="MemberStatus" :value="scope.row.status" />
            </template>
          </el-table-column>
          <el-table-column
            :label="$t('Common.CreateTime')"
            align="center"
            prop="createTime"
            width="160"
          >
            <template #default="scope">
              <el-text>{{ parseTime(scope.row.createTime) }}</el-text>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="selectTotal > 0"
          :total="selectTotal"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="loadSelectList"
        />
      </cc-spliter-column>
      <cc-spliter-column style="padding-left: 10px;">
        <div class="mb8">
          {{ $t('Member.Selector.SelectedMemberList') }} ({{ selectedMembers.length }})
        </div>
        <el-table :data="selectedMembers" size="small" row-key="memberId" height="430px">
          <el-table-column
            :label="$t('Member.UserName')"
            prop="userName"
            :show-overflow-tooltip="true"
          />
          <el-table-column
            :label="$t('Member.NickName')"
            prop="nickName"
            :show-overflow-tooltip="true"
          />
          <el-table-column :label="$t('Common.Operation')" align="center" width="90">
            <template #default="scope">
              <el-button
                link
                type="danger"
                icon="Delete"
                @click="removeSelectedMember(scope.row)"
              >
                {{ $t('Common.Remove') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </cc-spliter-column>
    </cc-spliter>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleGrantMembers">{{ $t('Common.Confirm') }}</el-button>
        <el-button @click="selectVisible = false">{{ $t('Common.Cancel') }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="MemberSelector">
import * as memberApi from '@/api/member/member';

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  }
})
const emit = defineEmits(['update:open', 'ok', 'cancel'])

const { proxy } = getCurrentInstance()
const { MemberStatus } = proxy.useDict('MemberStatus')

const selectVisible = ref(props.open)
const selectMemberList = ref([])
const selectTotal = ref(0)
const selectedMembers = ref([])
const syncingSelection = ref(false)
const confirming = ref(false)

const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    userName: undefined,
    nickName: undefined,
    email: undefined,
    phoneNumber: undefined,
    status: undefined
  }
})

const { queryParams } = toRefs(objects)

const selectedMemberIds = computed(() => selectedMembers.value.map(item => item.memberId))

watch(() => props.open, (newVal) => {
  selectVisible.value = newVal
})

watch(selectVisible, (newVal) => {
  emit('update:open', newVal)
  if (newVal) {
    resetSelector()
    loadSelectList()
  } else if (confirming.value) {
    confirming.value = false
  } else {
    emit('cancel')
  }
})

onMounted(() => {
  if (selectVisible.value) {
    resetSelector()
    loadSelectList()
  }
})

function loadSelectList() {
  memberApi.getMemberList(queryParams.value).then(response => {
    selectMemberList.value = response.data.rows
    selectTotal.value = parseInt(response.data.total)
    syncCurrentPageSelection()
  })
}

function handleSelectQuery() {
  queryParams.value.pageNum = 1
  loadSelectList()
}

function resetSelectQuery() {
  proxy.resetForm('selectQueryRef')
  handleSelectQuery()
}

function clickRow(row) {
  proxy.$refs['selectTableRef'].toggleRowSelection(row)
}

function handleSelectChange(selection) {
  if (syncingSelection.value) {
    return
  }
  const checkedIds = selection.map(item => item.memberId)
  const currentPageIds = selectMemberList.value.map(item => item.memberId)
  selectedMembers.value = selectedMembers.value.filter(item => {
    return !currentPageIds.includes(item.memberId) || checkedIds.includes(item.memberId)
  })
  selection.forEach(item => {
    if (!selectedMemberIds.value.includes(item.memberId)) {
      selectedMembers.value.push(item)
    }
  })
}

function removeSelectedMember(member) {
  selectedMembers.value = selectedMembers.value.filter(item => item.memberId !== member.memberId)
  syncCurrentPageSelection()
}

function handleGrantMembers() {
  emit('ok', selectedMemberIds.value)
  confirming.value = true
  selectVisible.value = false
}

function resetSelector() {
  queryParams.value.pageNum = 1
  queryParams.value.userName = undefined
  queryParams.value.nickName = undefined
  queryParams.value.email = undefined
  queryParams.value.phoneNumber = undefined
  queryParams.value.status = undefined
  selectedMembers.value = []
}

function syncCurrentPageSelection() {
  nextTick(() => {
    const table = proxy.$refs['selectTableRef']
    if (!table) {
      return
    }
    syncingSelection.value = true
    table.clearSelection()
    selectMemberList.value.forEach(row => {
      if (selectedMemberIds.value.includes(row.memberId)) {
        table.toggleRowSelection(row, true)
      }
    })
    nextTick(() => {
      syncingSelection.value = false
    })
  })
}

function open() {
  selectVisible.value = true
}

defineExpose({ open })
</script>