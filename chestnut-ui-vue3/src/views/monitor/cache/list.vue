<template>
  <div class="app-container">
    <el-row :gutter="10">
      <el-col :span="8">
        <el-card style="height: calc(100vh - 125px)">
          <template #header>
            <span>{{ $t('Monitor.Cache.CacheList') }}</span>
            <el-button
              style="float: right; padding: 3px 0"
              type="text"
              icon="RefreshRight"
              @click="refreshCacheNames()"
            ></el-button>
          </template>
          <el-table
            v-loading="loading"
            :data="cacheNames"
            :height="tableHeight"
            highlight-current-row
            @row-click="getCacheKeys"
            style="width: 100%"
          >
            <el-table-column
              :label="$t('Common.RowNo')"
              width="60"
              type="index"
            ></el-table-column>
            <el-table-column
              :label="$t('Monitor.Cache.CacheName')"
              align="center"
              prop="cacheName"
              :show-overflow-tooltip="true"
            />
            <el-table-column
              :label="$t('Monitor.Cache.CachePrefix')"
              align="center"
              prop="cacheKey"
              :show-overflow-tooltip="true"
            ></el-table-column>
            <el-table-column
              :label="$t('Common.Operation')"
              width="90"
              align="center"
             
            >
              <template #default="scope">
                <el-button
                  link
                  type="danger"
                  icon="Delete"
                  @click="handleClearCacheName(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card style="height: calc(100vh - 125px)">
          <template #header>
            <span>{{ $t('Monitor.Cache.CacheKeyList') }}</span>
            <el-button
              style="float: right; padding: 3px 0"
              type="text"
              icon="RefreshRight"
              @click="refreshCacheKeys()"
            ></el-button>
          </template>
          <el-table
            v-loading="subLoading"
            :data="cacheKeys"
            :height="tableHeight"
            highlight-current-row
            @row-click="handleCacheValue"
            style="width: 100%"
          >
            <el-table-column
              :label="$t('Common.RowNo')"
              width="60"
              type="index"
            ></el-table-column>
            <el-table-column
              :label="$t('Monitor.Cache.CacheKey')"
              :show-overflow-tooltip="true"
              :formatter="keyFormatter"
            >
            </el-table-column>
            <el-table-column
              :label="$t('Common.Operation')"
              width="60"
              align="center"
             
            >
              <template #default="scope">
                <el-button
                  link
                  type="danger"
                  icon="Delete"
                  @click="handleClearCacheKey(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card :bordered="false" style="height: calc(100vh - 125px)">
          <template #header>
            <span>{{ $t('Monitor.Cache.CacheValue') }}</span>
            <el-button
              style="float: right; padding: 3px 0"
              type="text"
              icon="RefreshRight"
              @click="handleClearCacheAll()"
              >{{ $t('Monitor.Cache.ClearCache') }}</el-button
            >
          </template>
          <el-form :model="cacheForm">
            <el-row :gutter="32">
              <el-col :offset="1" :span="22">
                <el-form-item :label="$t('Monitor.Cache.CacheKey')" prop="cacheKey">
                  <el-input v-model="cacheForm.cacheKey" :readOnly="true" />
                </el-form-item>
              </el-col>
              <el-col :offset="1" :span="22">
                <el-form-item :label="$t('Monitor.Cache.CacheValue')" prop="cacheValue">
                  <el-input
                    v-model="cacheForm.cacheValue"
                    type="textarea"
                    :rows="8"
                    :readOnly="true"
                  />
                </el-form-item>
              </el-col>
              <el-col :offset="1" :span="22">
                <el-form-item :label="$t('Monitor.Cache.ExpireTime')" prop="cacheKey">
                  <el-input v-model="cacheForm.expireTime" :readOnly="true" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="MonitorCacheList">
import { listCacheName, listCacheKey, getCacheValue, clearCacheName, clearCacheKey, clearCacheAll } from "@/api/monitor/cache";

const { proxy } = getCurrentInstance();

const cacheNames = ref([]);
const cacheKeys = ref([]);
const cacheForm = ref({});
const loading = ref(true);
const subLoading = ref(false);
const selectedCache = ref({});
const tableHeight = ref(window.innerHeight - 200);

/** 查询缓存名称列表 */
function getCacheNames() {
  loading.value = true;
  listCacheName().then(response => {
    cacheNames.value = response.data;
    loading.value = false;
  });
}

/** 刷新缓存名称列表 */
function refreshCacheNames() {
  getCacheNames();
  proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
}

/** 清理指定名称缓存 */
function handleClearCacheName(row) {
  clearCacheName(row.monitoredId).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Monitor.Cache.ClearSuccess', [ selectedCache.value.cacheName ]));
    getCacheKeys();
  });
}

/** 查询缓存键名列表 */
function getCacheKeys(row) {
  if (!row) {
    return;
  }
  selectedCache.value = row;
  refreshCacheKeys();
}

/** 刷新缓存键名列表 */
function refreshCacheKeys(func) {
  if (!selectedCache.value.monitoredId) {
    return;
  }
  subLoading.value = true;
  listCacheKey(selectedCache.value.monitoredId).then(response => {
    subLoading.value = false;
    if (func) {
      func(response)
    }
    cacheKeys.value = response.data;
    cacheForm.value = {};
  });
}

/** 清理指定键名缓存 */
function handleClearCacheKey(cacheKey) {
  if (!selectedCache.value.monitoredId) {
    return;
  }
  clearCacheKey(selectedCache.value.monitoredId, cacheKey).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    refreshCacheKeys();
  });
}

/** 键名前缀去除 */
function keyFormatter(cacheKey) {
  if (cacheKey == selectedCache.value.cacheKey) {
    return cacheKey;
  }
  return cacheKey.replace(selectedCache.value.cacheKey, "");
}

/** 查询缓存内容详细 */
function handleCacheValue(cacheKey) {
  if (!selectedCache.value.monitoredId) {
    return;
  }
  getCacheValue(selectedCache.value.monitoredId, cacheKey).then(response => {
    cacheForm.value = response.data;
  });
}

/** 清理全部缓存 */
function handleClearCacheAll() {
  clearCacheAll().then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    selectedCache.value = {};
    cacheForm.value = {};
    cacheKeys.value = [];
  });
}

onMounted(() => {
  getCacheNames();
});
</script>
