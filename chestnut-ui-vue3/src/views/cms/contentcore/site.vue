<template>
  <div class="app-container">
    <el-row :gutter="24" class="mb8">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button 
              plain 
              type="primary" 
              icon="Plus" 
              @click="handleAdd" 
              v-hasPermi="[ $p('cms:site:add') ]"
            >{{ $t("Common.Add") }}</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align: right">
        <el-form
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search"
        >
          <el-form-item prop="siteName">
            <el-input :placeholder="$t('CMS.Site.Name')" v-model="queryParams.siteName" />
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button
                type="primary"
                icon="Search"
                @click="handleQuery"
                >{{ $t("Common.Search") }}</el-button>
              <el-button icon="Refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-table
      v-loading="siteListLoading"
      :data="siteList"
      style="width: 100%; line-height: normal"
    >
      <el-table-column label="ID" width="200" prop="siteId" />
      <el-table-column :label="$t('CMS.Site.Name')">
        <template #default="scope">
          <el-link
            type="primary"
            @click="handleEdit(scope.row)"
            class="link-type"
          >
            <span>{{ scope.row.name }}</span>
          </el-link>
        </template>
      </el-table-column>
      <el-table-column :label="$t('CMS.Site.Path')" prop="path" />
      <el-table-column
        :label="$t('Common.Operation')"
        align="center"
        width="360"
        
      >
        <template #default="scope" class="permi-wrap">
          <el-button
            link
            type="primary"
            icon="View"
            @click="handlePreview(scope.row)"
          >{{ $t("CMS.ContentCore.Preview") }}</el-button>
          <el-button
            link
            type="primary"
            icon="Promotion"
            v-hasPermi="[$p('Site:Publish:{0}', [scope.row.siteId])]"
            @click="handlePublish(scope.row)"
          >{{ $t("CMS.Site.PublishHome") }}</el-button>
          <el-button
            link
            type="success"
            icon="Edit"
            v-hasPermi="[$p('Site:Edit:{0}', [scope.row.siteId])]"
            @click="handleEdit(scope.row)"
          >{{ $t("Common.Edit") }}</el-button>
          <el-button
            link
            type="danger"
            icon="Delete"
            v-hasPermi="[$p('Site:Delete:{0}', [scope.row.siteId])]"
            @click="handleDelete(scope.row)"
          >{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 添加站点对话框 -->
    <el-dialog
      :title="$t('CMS.Site.Dialog.AddTitle')"
      v-model="open"
      width="600px"
      append-to-body
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="135px"
        class="el-form-dialog"
      >
        <el-form-item :label="$t('CMS.Site.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Path')" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.ResourceUrl')" prop="resourceUrl">
          <el-input v-model="form.resourceUrl" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Desc')" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :maxlength="300"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleAddSave">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <!-- 进度条 -->
    <cms-progress
      :title="$t('CMS.Site.DeleteProgressTitle')"
      v-model:open="openProgress"
      :taskId="taskId"
      @close="handleCloseProgress"
    ></cms-progress>
  </div>
</template>
<script setup name="CmsContentcoreSite">
import { codeValidator } from "@/utils/validate";
import {
  delSite,
  addSite,
  listSite,
  publishSite,
} from "@/api/contentcore/site";
import CmsProgress from "@/views/components/Progress";

const { proxy } = getCurrentInstance();

const open = ref(false)
const siteList = ref([])
const siteListLoading = ref(true)
const siteTotal = ref(0)
const openProgress = ref(false)
const taskId = ref("")
const objects = reactive({
  queryParams: {
    siteName: undefined,
  },
  form: {},
  rules: {
    name: [
      { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
    ],
    path: [
      { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
    ]
  }
});
const { queryParams, form, rules } = toRefs(objects)

onMounted(() => {
    loadSiteList();
});
const loadSiteList = () => {
  siteListLoading.value = true;
  listSite(queryParams.value).then(response => {
    siteList.value = response.data.rows;
    siteTotal.value = parseInt(response.data.total);
    siteListLoading.value = false;
  });
}
const handleQuery = () => {
  queryParams.value.pageNum = 1;
  loadSiteList();
}
const resetQuery = () => {
  proxy.resetForm("queryFormRef");
  handleQuery();
}
const cancel = () => {
  open.value = false;
}
const handleAdd = () => {
  proxy.resetForm("formRef");
  form.value = {}
  open.value = true;
}
const handleAddSave = () => {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      form.value.parentId = 0;
      addSite(form.value).then(response => {
        proxy.$modal.msgSuccess(proxy.$t("Common.AddSuccess"));
        open.value = false;
        if (siteList.value.length == 0) {
          proxy.$router.go(0); // 无站点时刷新下重置当前站点
        } else {
          loadSiteList();
        }
      });
    }
  });
}
const handleEdit = (row) => {
  const siteId = row.siteId;
  proxy.$router.push({ path: "/cms/site/tabs", query: { siteId: siteId } });
}
const handleDelete = (row) => {
  const siteId = row.siteId;
  proxy.$modal.confirm(proxy.$t("Common.ConfirmDelete")).then(() => {
    return delSite(siteId);
  }).then(response => {
    if (proxy.$cms.getCurrentSite() == siteId) {
      proxy.$cms.setCurrentSite("0");
    }
    taskId.value = response.data;
    openProgress.value = true;
  }).catch(() => {});
}
const handleCloseProgress = () => {
  proxy.$router.go(0);
}
const handlePreview = (row) => {
  const siteId = row.siteId;
  let routeData = proxy.$router.resolve({
    path: "/cms/preview",
    query: { type: "site", dataId: siteId },
  });
  window.open(routeData.href, "_blank");
}
const handlePublish = (row) => {
  publishSite({ siteId: row.siteId, publishIndex: true }).then(response => {
      proxy.$modal.msgSuccess(proxy.$t("CMS.ContentCore.PublishSuccess"));
  });
}
</script>
