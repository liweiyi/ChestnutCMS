<template>
  <div class="app-container">
    <el-row :gutter="10">
      <el-col :span="8">
        <el-card style="height: calc(100vh - 125px)">
          <div slot="header">
            <span>{{ $t('Monitor.Cache.CacheList') }}</span>
            <el-button
              style="float: right; padding: 3px 0"
              type="text"
              icon="el-icon-refresh-right"
              @click="refreshCacheNames()"
            ></el-button>
          </div>
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
              :formatter="nameFormatter"
            ></el-table-column>

            <el-table-column
              :label="$t('Common.Remark')"
              align="center"
              prop="remark"
              :show-overflow-tooltip="true"
            />
            <el-table-column
              :label="$t('Common.Operation')"
              width="90"
              align="center"
              class-name="small-padding fixed-width"
            >
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-delete"
                  @click="handleClearCacheName(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card style="height: calc(100vh - 125px)">
          <div slot="header">
            <span>{{ $t('Monitor.Cache.CacheKeyList') }}</span>
            <el-button
              style="float: right; padding: 3px 0"
              type="text"
              icon="el-icon-refresh-right"
              @click="refreshCacheKeys()"
            ></el-button>
          </div>
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
              align="center"
              :show-overflow-tooltip="true"
              :formatter="keyFormatter"
            >
            </el-table-column>
            <el-table-column
              :label="$t('Common.Operation')"
              width="60"
              align="center"
              class-name="small-padding fixed-width"
            >
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="text"
                  icon="el-icon-delete"
                  @click="handleClearCacheKey(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card :bordered="false" style="height: calc(100vh - 125px)">
          <div slot="header">
            <span>{{ $t('Monitor.Cache.CacheValue') }}</span>
            <el-button
              style="float: right; padding: 3px 0"
              type="text"
              icon="el-icon-refresh-right"
              @click="handleClearCacheAll()"
              >{{ $t('Monitor.Cache.ClearCache') }}</el-button
            >
          </div>
          <el-form :model="cacheForm">
            <el-row :gutter="32">
              <el-col :offset="1" :span="22">
                <el-form-item :label="$t('Monitor.Cache.CacheName')" prop="cacheName">
                  <el-input v-model="cacheForm.cacheName" :readOnly="true" />
                </el-form-item>
              </el-col>
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

<script>
import { listCacheName, listCacheKey, getCacheValue, clearCacheName, clearCacheKey, clearCacheAll } from "@/api/monitor/cache";

export default {
  name: "MonitorCacheList",
  data() {
    return {
      cacheNames: [],
      cacheKeys: [],
      cacheForm: {},
      loading: true,
      subLoading: false,
      nowCacheName: "",
      tableHeight: window.innerHeight - 200
    };
  },
  created() {
    this.getCacheNames();
  },
  methods: {
    /** 查询缓存名称列表 */
    getCacheNames() {
      this.loading = true;
      listCacheName().then(response => {
        this.cacheNames = response.data;
        this.loading = false;
      });
    },
    /** 刷新缓存名称列表 */
    refreshCacheNames() {
      this.getCacheNames();
      this.$modal.msgSuccess(this.$t('Common.Success'));
    },
    /** 清理指定名称缓存 */
    handleClearCacheName(row) {
      clearCacheName(row.cacheName).then(response => {
        this.$modal.msgSuccess(this.$t('Monitor.Cache.ClearSuccess', [ this.nowCacheName ]));
        this.getCacheKeys();
      });
    },
    /** 查询缓存键名列表 */
    getCacheKeys(row) {
      const cacheName = row !== undefined ? row.cacheName : this.nowCacheName;
      if (cacheName === "") {
        return;
      }
      this.subLoading = true;
      listCacheKey(cacheName).then(response => {
        this.cacheKeys = response.data;
        this.subLoading = false;
        this.nowCacheName = cacheName;
      });
    },
    /** 刷新缓存键名列表 */
    refreshCacheKeys() {
      this.getCacheKeys();
      this.$modal.msgSuccess(this.$t('Common.Success'));
    },
    /** 清理指定键名缓存 */
    handleClearCacheKey(cacheKey) {
      clearCacheKey(cacheKey).then(response => {
        this.$modal.msgSuccess(this.$t('Common.Success'));
        this.getCacheKeys();
      });
    },
    /** 列表前缀去除 */
    nameFormatter(row) {
      return row.cacheName.replace(":", "");
    },
    /** 键名前缀去除 */
    keyFormatter(cacheKey) {
      return cacheKey.replace(this.nowCacheName, "");
    },
    /** 查询缓存内容详细 */
    handleCacheValue(cacheKey) {
      getCacheValue(this.nowCacheName, cacheKey).then(response => {
        this.cacheForm = response.data;
      });
    },
    /** 清理全部缓存 */
    handleClearCacheAll() {
      clearCacheAll().then(response => {
        this.$modal.msgSuccess(this.$t('Common.Success'));
      });
    }
  },
};
</script>
