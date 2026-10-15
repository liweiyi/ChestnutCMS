<template>
  <div class="app-container">
    <cc-spliter storage-key="member-favorites">
      <cc-spliter-column width="37.5%">
        <el-form ref="memberFormRef" :model="memberQuery" :rules="memberRules" :inline="true" class="el-form-search" @submit.prevent>
          <el-form-item prop="memberId">
            <el-input v-model.trim="memberQuery.memberId" clearable maxlength="19" style="width: 170px"
              :placeholder="$t('Member.UID')" @keyup.enter="searchMembers" />
          </el-form-item>
          <el-form-item prop="keyword">
            <el-input v-model.trim="memberQuery.keyword" clearable maxlength="30" style="width: 170px"
              :placeholder="$t('Member.LevelManage.MemberKeyword')" @keyup.enter="searchMembers" />
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button type="primary" icon="Search" @click="searchMembers">{{ $t('Common.Search') }}</el-button>
              <el-button icon="Refresh" @click="resetMembers">{{ $t('Common.Reset') }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
        <el-table v-loading="memberLoading" :data="members" row-key="memberId" highlight-current-row
          :current-row-key="selectedMember?.memberId" @row-click="selectMember" class="member-table">
          <el-table-column :label="$t('Member.UserName')" prop="userName" min-width="110" show-overflow-tooltip />
          <el-table-column :label="$t('Member.NickName')" prop="nickName" min-width="110" show-overflow-tooltip />
          <el-table-column :label="$t('Member.Status')" prop="status" width="80" align="center">
            <template #default="scope"><dict-tag :options="MemberStatus" :value="scope.row.status" /></template>
          </el-table-column>
        </el-table>
        <pagination v-show="memberTotal > 0" :total="memberTotal" v-model:page="memberQuery.pageNum"
          v-model:limit="memberQuery.pageSize" layout="total, prev, pager, next" :pager-count="5"
          :auto-scroll="false" @pagination="loadMembers" />
      </cc-spliter-column>
      <cc-spliter-column>
        <div class="favorites-header">
          {{ selectedMember ? $t('Member.FavoritesManage.MemberFavorites', { name: selectedMember.nickName || selectedMember.userName || selectedMember.memberId }) : $t('Member.FavoritesManage.Favorites') }}
          <el-text v-if="selectedMember" type="info" class="selected-member-id">ID: {{ selectedMember.memberId }}</el-text>
        </div>
        <el-table v-loading="favoriteLoading" :data="favorites" row-key="logId"
          :empty-text="$t(selectedMember ? 'Member.FavoritesManage.NoFavorites' : 'Member.FavoritesManage.SelectMember')">
          <el-table-column :label="$t('Member.FavoritesManage.DataType')" prop="dataType" min-width="150" show-overflow-tooltip />
          <el-table-column :label="$t('Member.FavoritesManage.Name')" min-width="250" show-overflow-tooltip>
            <template #default="scope">
              <span v-if="scope.row.name">{{ scope.row.name }}</span>
              <el-text v-else type="info">{{ $t('Member.FavoritesManage.Unavailable', { id: scope.row.dataId }) }}</el-text>
            </template>
          </el-table-column>
          <el-table-column :label="$t('Member.FavoritesManage.FavoriteTime')" width="180">
            <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <pagination v-show="favoriteTotal > 0" :total="favoriteTotal" v-model:page="favoriteQuery.pageNum"
          v-model:limit="favoriteQuery.pageSize" :auto-scroll="false" @pagination="loadFavorites" />
      </cc-spliter-column>
    </cc-spliter>
  </div>
</template>

<script setup name="MemberFavorites">
import { getFavoriteMembers, getMemberFavorites } from '@/api/member/favorites'
import useMemberFavorites from '@/composables/useMemberFavorites'

const { proxy } = getCurrentInstance()
const { MemberStatus } = proxy.useDict('MemberStatus')
const memberFormRef = ref()
const {
  members, memberTotal, memberLoading, selectedMember, memberQuery,
  favorites, favoriteTotal, favoriteLoading, favoriteQuery,
  loadMembers, selectMember, loadFavorites
} = useMemberFavorites({ getMembers: getFavoriteMembers, getFavorites: getMemberFavorites })

const memberRules = {
  memberId: [{ validator(rule, value, callback) {
    if (!value || (/^[1-9]\d{0,18}$/.test(value) && BigInt(value) <= 9223372036854775807n)) callback()
    else callback(new Error(proxy.$t('Member.Follow.InvalidId')))
  }, trigger: 'blur' }]
}

async function searchMembers() {
  if (!await memberFormRef.value.validate().catch(() => false)) return
  memberQuery.pageNum = 1
  await loadMembers()
}

function resetMembers() {
  memberFormRef.value.resetFields()
  return searchMembers()
}

onMounted(loadMembers)
</script>

<style scoped>
.favorites-header { margin-bottom: 16px; }
.member-table :deep(.el-table__row) { cursor: pointer; }
.selected-member-id { margin-left: 12px; }
</style>
