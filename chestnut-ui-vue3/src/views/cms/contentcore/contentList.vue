<template>
  <div class="cms-content-list">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" class="permi-wrap">
        <div v-hasPermi="[ $p('Catalog:AddContent:{0}', [ props.cid ]) ]">
          <el-popover v-model="addPopoverVisible" placement="bottom-start" :width="480" trigger="click">
            <div class="btn-add-content">
              <el-row style="margin-bottom:20px;">
                <el-divider content-position="left">{{ $t('CMS.Content.ContentType') }}</el-divider>
                <el-radio-group v-model="addContentType">
                  <el-radio-button
                    v-for="ct in contentTypeOptions"
                    :key="ct.id"
                    :label="ct.id"
                  >{{ct.name}}</el-radio-button>
                </el-radio-group>
                <el-divider v-if="addContentType=='article'" content-position="left">{{ $t('CMS.Article.Format') }}</el-divider>
                <el-radio-group v-if="addContentType=='article'" v-model="addArticleBodyFormat">
                  <el-radio-button
                    v-for="item in articleBodyFormatOptions"
                    :key="item.id"
                    :label="item.id"
                  >{{ item.name }}</el-radio-button>
                </el-radio-group>
              </el-row>
              <el-row justify="end">
                <el-button
                  plain
                  type="primary"
                  @click="handleAdd">{{ $t('Common.Confirm') }}</el-button>
              </el-row>
            </div>
            <template #reference>
              <el-button
                type="primary"
                icon="Plus"
                plain>{{ $t("Common.Add") }}<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
            </template>
          </el-popover>
        </div>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          plain
          type="danger"
          icon="Delete"
          :disabled="multiple"
          v-hasPermi="[ $p('Catalog:DeleteContent:{0}', [ props.cid ]) ]"
          @click="handleDelete">{{ $t("Common.Delete") }}
        </el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          plain
          type="primary"
          icon="Timer"
          :disabled="multiple"
          v-hasPermi="[ $p('Catalog:EditContent:{0}', [ props.cid ]) ]"
          @click="handleToPublish">{{ $t("CMS.ContentCore.ToPublish") }}
        </el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          plain
          type="success"
          icon="Promotion"
          :disabled="multiple"
          v-hasPermi="[ $p('Catalog:EditContent:{0}', [ props.cid ]) ]"
          @click="handlePublish">{{ $t("CMS.ContentCore.Publish") }}
        </el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          plain
          type="warning"
          icon="Download"
          :disabled="multiple"
          v-hasPermi="[ $p('Catalog:EditContent:{0}', [ props.cid ]) ]"
          @click="handleOffline">{{ $t("CMS.Content.Offline") }}
        </el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          plain
          type="primary"
          icon="DocumentCopy"
          :disabled="multiple"
          v-hasPermi="[ $p('Catalog:EditContent:{0}', [ props.cid ]) ]"
          @click="handleCopy">{{ $t("Common.Copy") }}
        </el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-button
          plain
          type="primary"
          icon="Right"
          :disabled="multiple"
          v-hasPermi="[ $p('Catalog:EditContent:{0}', [ props.cid ]) ]"
          @click="handleMove">{{ $t("Common.Move") }}
        </el-button>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-dropdown v-hasPermi="[ $p('Catalog:EditContent:{0}', [ props.cid ]) ]">
          <el-button
            plain
            type="primary"
            :disabled="multiple">
            <svg-icon icon-class="recommend" /> {{ $t('CMS.Content.Recommend') }}<el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item icon="Plus" :disabled="multiple" @click.native="handleRecommend">{{ $t('CMS.Content.Recommend') }}</el-dropdown-item>
              <el-dropdown-item icon="Minus" :disabled="multiple" @click.native="handleCancelRecommend">{{ $t('CMS.Content.CancelRecommend') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-col>
      <el-col :span="1.5" class="permi-wrap">
        <el-dropdown v-hasPermi="[ $p('Catalog:EditContent:{0}', [ props.cid ]) ]">
          <el-button
            plain
            type="primary"
            :disabled="multiple">
            <svg-icon icon-class="fire" /> {{ $t('CMS.Content.Hot') }}<el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item icon="Plus" :disabled="multiple" @click.native="handleHot">{{ $t('CMS.Content.Hot') }}</el-dropdown-item>
              <el-dropdown-item icon="Minus" :disabled="multiple" @click.native="handleCancelHot">{{ $t('CMS.Content.CancelHot') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-col>
    </el-row>
    <el-row>
      <el-form :model="queryParams" ref="queryFormRef" class="el-form-search" style="text-align:left;" :inline="true">
        <div>
          <el-form-item prop="title">
            <el-input
              v-model="queryParams.title"
              :placeholder="$t('CMS.Content.Placeholder.Title')"
              clearable
              style="width: 200px"
              @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item prop="contentType">
            <el-select
              v-model="queryParams.contentType"
              :placeholder="$t('CMS.Content.ContentType')"
              clearable
              style="width: 125px">
              <el-option
                v-for="ct in contentTypeOptions"
                :key="ct.id"
                :label="ct.name"
                :value="ct.id" />
            </el-select>
          </el-form-item>
          <el-form-item prop="status">
            <el-select
              v-model="queryParams.status"
              :placeholder="$t('CMS.Content.Status')"
              clearable
              style="width: 110px">
              <el-option
                v-for="dict in CMSContentStatus"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-select  v-model="queryParams.sorts" @change="loadContentList" style="width: 140px">
              <el-option value="" :label="$t('CMS.Content.SortOption.Default')"></el-option>
              <el-option value="createTime#ASC" :label="$t('CMS.Content.SortOption.CreateTimeAsc')"></el-option>
              <el-option value="createTime#DESC" :label="$t('CMS.Content.SortOption.CreateTimeDesc')"></el-option>
              <el-option value="publishDate#ASC" :label="$t('CMS.Content.SortOption.PublishDateAsc')"></el-option>
              <el-option value="publishDate#DESC" :label="$t('CMS.Content.SortOption.PublishDateDesc')"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button type="primary" icon="Search" @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button icon="Refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
          <el-form-item>
            <el-button icon="Plus" @click="showSearch=!showSearch">{{ $t("Common.More") }}</el-button>
          </el-form-item>
        </div>
        <div v-show="showSearch">
          <el-form-item :label="$t('Common.CreateTime')">
            <el-date-picker
              v-model="dateRange"
              style="width: 386px"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="datetimerange"
              range-separator="-"
              :start-placeholder="$t('Common.BeginDate')"
              :end-placeholder="$t('Common.EndDate')"></el-date-picker>
          </el-form-item>
        </div>
      </el-form>
    </el-row>

    <el-table
      v-loading="loading"
      ref="tableContentListRef"
      :data="contentList"
      :height="tableHeight"
      :max-height="tableMaxHeight"
      @row-click="handleRowClick"
      @cell-dblclick="handleEdit"
      @selection-change="handleSelectionChange"
      @row-contextmenu="handleRowContextMenu">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column :label="$t('CMS.Content.Logo')" align="center" width="110">
          <template #default="scope">
            <el-image
              v-if="scope.row.logoSrc&&scope.row.logoSrc.length > 0"
              style="max-height: 100px;max-width: 100px;"
              :src="scope.row.logoSrc"
              fit="contain"
            ></el-image>
          </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Content.Title')" :show-overflow-tooltip="true">
        <template #default="scope">
          <span class="content_attr" v-if="scope.row.topFlag>0" :title="$t('CMS.Content.SetTop')">[<svg-icon icon-class="top" />]</span>
          <span
            v-for="dict in CMSContentAttribute"
            :key="dict.value"
            :title="dict.label">
            <span class="content_attr" v-if="scope.row.attributes.indexOf(dict.value)>-1">[<svg-icon :icon-class="dict.value" />]</span>
          </span>
          {{ scope.row.title }}
          <!-- <el-link type="primary" @click="handleEdit(scope.row)" :title="scope.row.title" class="link-type">
            {{ scope.row.title }}
          </el-link> -->
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Content.ContentType')" width="110" align="center" prop="contentType" :formatter="contentTypeFormat" />
      <el-table-column :label="$t('CMS.Content.Status')" align="center" width="110">
          <template #default="scope">
            <dict-tag :options="CMSContentStatus" :value="scope.row.status"/>
          </template>
      </el-table-column>
      <el-table-column :label="$t('Common.CreateTime')" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('Common.Operation')"
        align="center"
        width="400">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="View"
            @click="handlePreview(scope.row)"
          >{{ $t('CMS.ContentCore.Preview') }}</el-button>
          <el-button
            link
            type="success"
            icon="Promotion"
            v-hasPermi="[ $p('Catalog:EditContent:{0}', [ scope.row.catalogId ]) ]"
            @click="handlePublish(scope.row)"
          >{{ $t('CMS.ContentCore.Publish') }}</el-button>
          <el-button
            link
            type="warning"
            icon="Download"
            v-hasPermi="[ $p('Catalog:EditContent:{0}', [ scope.row.catalogId ]) ]"
            @click="handleOffline(scope.row)"
          >{{ $t('CMS.Content.Offline') }}</el-button>
          <el-button
            link
            type="primary"
            icon="Timer"
            v-hasPermi="[ $p('Catalog:EditContent:{0}', [ scope.row.catalogId ]) ]"
            @click="handleToPublish(scope.row)"
          >{{ $t('CMS.ContentCore.ToPublish') }}</el-button>
          <el-dropdown>
            <el-button type="primary" link icon="More"></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item 
                  v-if="checkPermi([$p('Catalog:EditContent:{0}', [ scope.row.catalogId ])])"
                  icon="Edit" 
                  @click.native="handleEdit(scope.row)"
                >{{ $t('Common.Edit') }}</el-dropdown-item>
                <el-dropdown-item 
                  v-if="(scope.row.status == '40' || scope.row.status == '0') && checkPermi([$p('Catalog:DeleteContent:{0}', [ scope.row.catalogId ])])"
                  icon="Delete" 
                  @click.native="handleDelete(scope.row)"
                >{{ $t('Common.Delete') }}</el-dropdown-item>
                <el-dropdown-item
                  v-if="checkPermi([$p('Catalog:EditContent:{0}', [ scope.row.catalogId ])])"
                  icon="Sort" 
                  @click.native="handleSort(scope.row)"
                >{{ $t('Common.Sort') }}</el-dropdown-item>
                <el-dropdown-item 
                  v-if="checkPermi([$p('Catalog:EditContent:{0}', [ scope.row.catalogId ])])"
                  icon="Top" 
                  @click.native="handleSetTop(scope.row)"
                >{{ $t('CMS.Content.SetTop') }}</el-dropdown-item>
                <el-dropdown-item 
                  v-if="scope.row.topFlag>0 && checkPermi([$p('Catalog:EditContent:{0}', [ scope.row.catalogId ])])"
                  icon="Bottom" 
                  @click.native="handleCancelTop(scope.row)"
                >{{ $t('CMS.Content.CancelTop') }}</el-dropdown-item>
                <el-dropdown-item 
                  v-if="checkPermi([$p('Catalog:EditContent:{0}', [ scope.row.catalogId ])])"
                  icon="DocumentCopy" 
                  @click.native="handleCopy(scope.row)"
                >{{ $t('Common.Copy') }}</el-dropdown-item>
                <el-dropdown-item 
                  v-if="checkPermi([$p('Catalog:EditContent:{0}', [ scope.row.catalogId ])])"
                  icon="Right" 
                  @click.native="handleMove(scope.row)"
                >{{ $t('Common.Move') }}</el-dropdown-item>
                <el-dropdown-item 
                  v-if="checkPermi([$p('Catalog:EditContent:{0}', [ scope.row.catalogId ])])"
                  icon="Download" 
                  @click.native="handleOffline(scope.row)"
                >{{ $t('CMS.Content.Offline') }}</el-dropdown-item>
                <el-dropdown-item 
                  icon="Search" 
                  @click.native="handleCreateIndex(scope.row)"
                >{{ $t('CMS.Content.GenIndex') }}</el-dropdown-item>
                <!-- <el-dropdown-item icon="Document" @click.native="handleArchive(scope.row)">{{ $t('CMS.Content.Archive') }}</el-dropdown-item> -->
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadContentList" />

    <!-- 置顶时间设置弹窗 -->
    <el-dialog
      :title="$t('CMS.Content.SetTop')"
      width="400px"
      v-model="topDialogVisible"
      :close-on-click-modal="false"
      append-to-body>
      <el-form ref="topFormRef" label-width="100px" :model="topForm">
        <el-form-item :label="$t('CMS.Content.TopEndTime')" prop="topEndTime">
          <el-date-picker
            v-model="topForm.topEndTime"
            :picker-options="topEndTimePickerOptions"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="datetime">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleTopDialogOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="topDialogVisible=false">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <!-- 进度条 -->
    <cms-progress :title="progressTitle" v-model:open="openProgress" :taskId="taskId" @close="handleProgressClose"></cms-progress>
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      v-model:open="openCatalogSelector"
      :showCopyToolbar="isCopy"
      :multiple="isCopy"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"
    ></cms-catalog-selector>
    <!-- 内容排序组件 -->
    <cms-content-sort
      v-model:open="openContentSortDialog"
      :cid="props.cid"
      @ok="handleContentSortDialogOk"
      @close="handleContentSortDialogClose"
    ></cms-content-sort>
    <!-- 右键菜单 -->
    <context-menu ref="contextMenuRef" v-model:open="contextMenuVisible" :data="contextMenuRow">
      <template #default="{ data }">
        <ul v-if="data?.contentId">
          <li 
            v-if="checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handlePreview(data)">
            <el-icon><View /></el-icon>{{ $t('CMS.ContentCore.Preview') }}
          </li>
          <li 
            v-if="checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handlePublish(data)">
            <el-icon><Promotion /></el-icon>{{ $t('CMS.ContentCore.Publish') }}
          </li>
          <li 
            v-if="checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handleToPublish(data)">
            <el-icon><Timer /></el-icon>{{ $t('CMS.ContentCore.ToPublish') }}
          </li>
          <li 
            v-if="checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handleOffline(data)">
            <el-icon><Download /></el-icon>{{ $t('CMS.Content.Offline') }}
          </li>
          <li class="menu-divider"></li>
          <li 
            v-if="checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handleEdit(data)"
            style="color: var(--el-color-success);">
            <el-icon><Edit /></el-icon>{{ $t('Common.Edit') }}
          </li>
          <li 
            v-if="(data.status == '40' || data.status == '0') && checkPermi([$p('Catalog:DeleteContent:{0}', [ data.catalogId ])])"
            @click="handleDelete(data)"
            style="color: var(--el-color-danger);">
            <el-icon><Delete /></el-icon>{{ $t('Common.Delete') }}
          </li>
          <li 
            v-if="checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handleSort(data)">
            <el-icon><Sort /></el-icon>{{ $t('Common.Sort') }}
          </li>
          <li 
            v-if="checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handleCopy(data)">
            <el-icon><DocumentCopy /></el-icon>{{ $t('Common.Copy') }}
          </li>
          <li 
            v-if="checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handleMove(data)">
            <el-icon><Right /></el-icon>{{ $t('Common.Move') }}
          </li>
          <li class="menu-divider"></li>
          <li 
            v-if="checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handleSetTop(data)">
            <el-icon><Top /></el-icon>{{ $t('CMS.Content.SetTop') }}
          </li>
          <li 
            v-if="data?.topFlag > 0 && checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handleCancelTop(data)">
            <el-icon><Bottom /></el-icon>{{ $t('CMS.Content.CancelTop') }}
          </li>
          <li 
            v-if="!data.attributes.includes('recommend') && checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handleRecommend(data)">
            <svg-icon icon-class="recommend" />{{ $t('CMS.Content.Recommend') }}
          </li>
          <li 
            v-if="data.attributes.includes('recommend') && checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handleCancelRecommend(data)">
            <svg-icon icon-class="recommend" /><el-text type="danger">{{ $t('CMS.Content.CancelRecommend') }}</el-text>
          </li>
          <li 
            v-if="!data.attributes.includes('hot') && checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handleHot(data)">
            <svg-icon icon-class="fire" />{{ $t('CMS.Content.Hot') }}
          </li>
          <li 
            v-if="data.attributes.includes('hot') && checkPermi([$p('Catalog:EditContent:{0}', [ data.catalogId ])])"
            @click="handleCancelHot(data)">
            <svg-icon icon-class="fire" /><el-text type="danger">{{ $t('CMS.Content.CancelHot') }}</el-text>
          </li>
          <li class="menu-divider"></li>
          <li 
            v-if="data.status == '30'"
            @click="handleCreateIndex(data)">
            <el-icon><Search /></el-icon>{{ $t('CMS.Content.GenIndex') }}
          </li>
        </ul>
      </template>
    </context-menu>
  </div>
</template>
<script setup name="CMSContentList">
import { getUserPreference } from "@/api/system/user";
import { getContentTypes } from "@/api/contentcore/catalog";
import { getArticleBodyFormats } from "@/api/contentcore/article"
import * as contentApi from "@/api/contentcore/content";
import CmsCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CmsContentSort from "@/views/cms/contentcore/contentSortDialog";
import CmsProgress from '@/views/components/Progress';
import ContextMenu from "@/components/ContextMenu";

const { proxy } = getCurrentInstance();

const { CMSContentStatus, CMSContentAttribute } = proxy.useDict('CMSContentStatus', 'CMSContentAttribute');

const props = defineProps({
  cid: {
    type: String,
    default: undefined,
    required: false,
  }
})

const loading = ref(false);
const addPopoverVisible = ref(false);
const showSearch = ref(false);
const contentTypeOptions = ref([]);
const contentList = ref(null);
const total = ref(0);
const tableHeight = ref(600);
const tableMaxHeight = ref(600);
const selectedRows = ref([]);
const single = ref(true);
const multiple = ref(true);
const dateRange = ref([]);
const topDialogVisible = ref(false);
const objects = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    title: undefined,
    contentType: undefined,
    status: undefined,
    catalogId: undefined,
    sorts: ''
  },
  topForm: {
    topEndTime: undefined
  },
  topEndTimePickerOptions: {
    disabledDate(time) {
      return time.getTime() < Date.now() - 8.64e7;//如果没有后面的-8.64e7就是不可以选择今天的
    }
  }
});
const { queryParams, topForm, topEndTimePickerOptions } = toRefs(objects);
const openProgress = ref(false);
const progressTitle = ref('');
const progressType = ref('');
const taskId = ref('');
const addContentType = ref('');
const articleBodyFormatOptions = ref([]);
const addArticleBodyFormat = ref('RichText');
const openCatalogSelector = ref(false);
const isCopy = ref(false);
const openContentSortDialog = ref(false);
const openEditorW = ref(false);

// 右键菜单相关
const contextMenuVisible = ref(false);
const contextMenuRow = ref(null);

watch(() => props.cid, (newVal) => {
  loadContentList();
});

onMounted(() => {
  changeTableHeight();
  getContentTypes().then(response => {
    contentTypeOptions.value = response.data;
    addContentType.value = contentTypeOptions.value[0].id;
  });
  loadArticleBodyFormats();
  if (props.cid && props.cid > 0) {
    loadContentList();
  }
  getUserPreference('OpenContentEditorW').then(response => {
    openEditorW.value = response.data == 'Y'
  })
});

function loadArticleBodyFormats() {
  getArticleBodyFormats().then(response => {
    articleBodyFormatOptions.value = response.data;
    addArticleBodyFormat.value = articleBodyFormatOptions.value[0].id;
  })
}

function loadContentList () {
  loading.value = true;
  queryParams.value.catalogId = props.cid;
  contentApi.getContentList({
    beginTime: dateRange.value && dateRange.value.length == 2 ? dateRange.value[0] : null,
    endTime: dateRange.value && dateRange.value.length == 2 ? dateRange.value[1] : null,
    ... queryParams.value
  }).then(response => {
    contentList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function contentTypeFormat (row, column) {
  var hitValue = [];
  contentTypeOptions.value.forEach(ct => {
    if (ct.id == ('' + row.contentType)) {
      hitValue = ct.name;
      return;
    }
  });
  return hitValue;
}

function handleSelectionChange (selection) {
  selectedRows.value = selection;
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

function handleRowClick (currentRow) {
  toggleAllCheckedRows();
  proxy.$refs.tableContentListRef.toggleRowSelection(currentRow);
}

function toggleAllCheckedRows() {
  selectedRows.value.forEach(row => {
    proxy.$refs.tableContentListRef.toggleRowSelection(row, false);
  });
  selectedRows.value = [];
}

function handleQuery () {
  queryParams.value.page = 1;
  loadContentList();
}

function resetQuery () {
  dateRange.value = [];
  proxy.resetForm("queryFormRef");
  handleQuery();
}

function handleAdd () {
  if (!props.cid) {
    proxy.$modal.msgError(proxy.$t("CMS.Content.SelectCatalogFirst"));
    return;
  }
  addPopoverVisible.value = false;
  openEditor(props.cid, 0, addContentType.value, addArticleBodyFormat.value);
}

function handleEdit (row) {
  openEditor(row.catalogId, row.contentId, row.contentType);
}

function openEditor(catalogId, contentId, contentType, articleBodyFormat = "") {
  let queryParams = { type: contentType, catalogId: catalogId, id: contentId }
  if (articleBodyFormat.length > 0 && contentType == 'article') {
    queryParams.format = articleBodyFormat;
  }
  if (openEditorW.value) {
    let routeData = proxy.$router.resolve({
      path: "/cms/content/editorW",
      query: queryParams,
    });
    let winEditor = window.open(routeData.href, '_blank');
    winEditor.onbeforeunload = function () {
      loadContentList();
    }
  } else {
    proxy.$router.push({ path: "/cms/content/editor", query: queryParams });
  }
}

function handleDelete (row) {
  const contentIds = row.contentId ? [ row.contentId ] : selectedRows.value.map(row => row.contentId);
  proxy.$modal.confirm(proxy.$t("Common.ConfirmDelete")).then(function () {
    return contentApi.delContent(contentIds);
  }).then(() => {
    loadContentList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(function () { });
}

function handlePublish (row) {
  const contentIds = row.contentId ? [ row.contentId ] : selectedRows.value.map(row => row.contentId);
  if (contentIds.length == 0) {
    proxy.$modal.msgWarning(proxy.$t('CMS.Content.SelectRowFirst'));
    return;
  }
  contentApi.publishContent(contentIds).then(response => {
    taskId.value = response.data;
    proxy.$cms.setPublishFlag("true")
    progressType.value = "publish";
    progressTitle.value = proxy.$t('CMS.Content.PublishProgressTitle')
    openProgress.value = true;
  });
}

function handlePreview (row) {
  let contentId = undefined;
  if (row) {
    contentId = row.contentId;
  } else if (selectedRows.value.length > 0) {
    contentId = selectedRows.value[0].contentId;
  } else {
    proxy.$modal.msgWarning(proxy.$t('CMS.Content.SelectRowFirst'));
    return;
  }
  let routeData = proxy.$router.resolve({
    path: "/cms/preview",
    query: { type: "content", dataId: contentId },
  });
  window.open(routeData.href, '_blank');
}

function handleDropdownBtn (command, row) {
}

function changeTableHeight () {
  let height = document.body.offsetHeight // 网页可视区域高度
  tableHeight.value = height - 330;
  tableMaxHeight.value = tableHeight.value;
}

function handleCreateIndex(row) {
  contentApi.createIndexes(row.contentId).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
  });
}

function handleCopy(row) {
  if (row.contentId) {
    selectedRows.value = [ row ];
  }
  isCopy.value = true;
  openCatalogSelector.value = true;
}

function doCopy(catalogs, copyType) {
  const data = {
    contentIds: selectedRows.value.map(item => item.contentId),
    catalogIds: catalogs.map(item => item.id),
    copyType: copyType
  };
  contentApi.copyContent(data).then(response => {
    openCatalogSelector.value = false;
    taskId.value = response.data;
    progressType.value = "copy";
    progressTitle.value = proxy.$t('CMS.Content.CopyProgressTitle')
    openProgress.value = true;
  });
}

function handleMove(row) {
  if (row.contentId) {
    selectedRows.value = [ row ];
  }
  isCopy.value = false;
  openCatalogSelector.value = true;
}

function doMove (args) {
  const catalogs = args[0];
  const data = {
    contentIds: selectedRows.value.map(item => item.contentId),
    catalogId: catalogs[0].id
  };
  contentApi.moveContent(data).then(response => {
    openCatalogSelector.value = false;
    taskId.value = response.data;
    progressType.value = "copy";
    progressTitle.value = proxy.$t('CMS.Content.MoveProgressTitle')
    openProgress.value = true;
  });
}

function handleCatalogSelectorOk(args) {
  const catalogs = args[0];
  const copyType = args[1];
  if (isCopy.value) {
    doCopy(catalogs, copyType);
  } else {
    doMove(catalogs);
  }
}

function handleCatalogSelectorClose() {
  openCatalogSelector.value = false;
}

function handleSetTop(row) {
  if (row.contentId) {
    toggleAllCheckedRows();
    selectedRows.value.push(row);
  }
  topDialogVisible.value = true;
}

function handleTopDialogOk() {
  const contentIds = selectedRows.value.map(item => item.contentId);
  if (contentIds.length == 0) {
    proxy.$modal.msgWarning(proxy.$t('CMS.Content.SelectRowFirst'));
    return;
  }
  proxy.$refs.topFormRef.validate(valid => {
    if (valid) {
      contentApi.setTopContent({ contentIds: contentIds, topEndTime: topForm.value.topEndTime }).then(response => {
        proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
        topDialogVisible.value = false;
        topForm.value.topEndTime = undefined;
        loadContentList();
      });
    }
  });
}

function handleCancelTop(row) {
  const contentIds = row.contentId ? [ row.contentId ] : selectedRows.value.map(item => item.contentId);
  contentApi.cancelTopContent(contentIds).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    loadContentList();
  });
}

function handleSort(row) {
  if (row.contentId) {
    toggleAllCheckedRows();
    selectedRows.value.push(row);
  }
  openContentSortDialog.value = true;
}

function handleContentSortDialogOk(contents) {
  if (contents && contents.length > 0) {
    doSort(contents[0].contentId);
  }
}

function handleContentSortDialogClose() {
  openContentSortDialog.value = false;
}

function doSort(targetContentId) {
  const data = { contentId: selectedRows.value[0].contentId, targetContentId: targetContentId };
  contentApi.sortContent(data).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    openContentSortDialog.value = false;
    loadContentList();
  });
}

function handleOffline(row) {
  const contentIds = row.contentId ? [ row.contentId ] : selectedRows.value.map(item => item.contentId);
  contentApi.offlineContent(contentIds).then(response => {
    taskId.value = response.data;
    progressType.value = "offline";
    progressTitle.value = proxy.$t('CMS.Content.OfflineProgressTitle')
    openProgress.value = true;
  });
}

function handleToPublish(row) {
  const contentIds = row.contentId ? [ row.contentId ] : selectedRows.value.map(item => item.contentId);
  contentApi.toPublishContent(contentIds).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('CMS.ContentCore.ToPublishSuccess'));
    loadContentList();
  });
}

function handleArchive(row) {
  const contentIds = row.contentId ? [ row.contentId ] : selectedRows.value.map(item => item.contentId);
  contentApi.archiveContent(contentIds).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    loadContentList();
  });
}

function handleRecommend(row) {
  const contentIds = row.contentId ? [ row.contentId ] : selectedRows.value.map(item => item.contentId);
  contentApi.addContentAttribute({ "contentIds": contentIds, "attr": "recommend"}).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    loadContentList();
  });
}

function handleCancelRecommend(row) {
  const contentIds = row.contentId ? [ row.contentId ] : selectedRows.value.map(item => item.contentId);
  contentApi.removeContentAttribute({ "contentIds": contentIds, "attr": "recommend"}).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    loadContentList();
  });
}

function handleHot(row) {
  const contentIds = row.contentId ? [ row.contentId ] : selectedRows.value.map(item => item.contentId);
  contentApi.addContentAttribute({ "contentIds": contentIds, "attr": "hot"}).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    loadContentList();
  });
}

function handleCancelHot(row) {
  const contentIds = row.contentId ? [ row.contentId ] : selectedRows.value.map(item => item.contentId);
  contentApi.removeContentAttribute({ "contentIds": contentIds, "attr": "hot"}).then(response => {
    proxy.$modal.msgSuccess(proxy.$t('Common.OpSuccess'));
    loadContentList();
  });
}

function handleProgressClose (result) {
  if (result.status == 'SUCCESS') {
    loadContentList();
  }
}


// 右键菜单相关方法
function handleRowContextMenu(row, column, event) {
  contextMenuRow.value = row;
  proxy.$refs.contextMenuRef.show(event);
}
</script>
<style lang="scss" scoped>
.cms-content-list .head-container .el-select .el-input {
  width: 110px;
}
.cms-content-list .el-divider {
  margin-top: 10px;
}
.cms-content-list .el-tabs__header {
  margin-bottom: 10px;
}
.cms-content-list .row-more-btn {
  padding-left: 10px;
}
.cms-content-list .top-icon {
  font-weight: bold;
  font-size: 12px;
  color:green;
}
.cms-content-list .content_attr {
  margin-left: 2px;
}
.btn-more:focus-visible {
  outline: none;
  border: none;
  box-shadow: none;
}
</style>