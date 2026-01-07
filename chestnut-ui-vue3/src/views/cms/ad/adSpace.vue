<template>
  <div class="app-container adspace-container">
    <el-row :gutter="24" class="mb8">
      <el-col :span="12">
        <el-button plain type="primary" icon="Plus" @click="handleAdd">{{ $t("Common.Add") }}</el-button>
        <el-button
          plain
          type="danger"
          icon="Delete"
          :disabled="selectedRows.length === 0"
          @click="handleDelete"
          >{{ $t("Common.Delete") }}</el-button
        >
      </el-col>
      <el-col :span="12" style="text-align: right">
        <el-input
          :placeholder="$t('CMS.Adv.Placeholder.Name')"
          v-model="queryParams.name"
          class="mr10"
          style="width: 200px"
        ></el-input>
        <el-button-group>
          <el-button type="primary" icon="Search" @click="loadAdSpaceList">{{ $t("Common.Search") }}</el-button>
          <el-button icon="Refresh" @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
        </el-button-group>
      </el-col>
    </el-row>
    <el-row>
      <el-table
        v-loading="loading"
        :data="dataList"
        @selection-change="handleSelectionChange"
        @row-dblclick="handleEdit"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column :label="$t('CMS.PageWidget.Name')">
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
        <el-table-column :label="$t('CMS.PageWidget.Code')" prop="code" />
        <el-table-column
          :label="$t('CMS.PageWidget.PublishPipe')"
          width="100"
          prop="publishPipeCode"
        />
        <el-table-column
          :label="$t('Common.Operation')"
          align="center"
          width="300"
         
        >
          <template #default="scope">
            <el-button
              type="text"
              icon="Promotion"
              v-hasPermi="[
                $p('PageWidget:Publish:{0}', [scope.row.pageWidgetId]),
              ]"
              @click="handlePublish(scope.row)"
              >{{ $t("CMS.ContentCore.Publish") }}</el-button
            >
            <el-button type="text" @click="handlePreview(scope.row)">
              <svg-icon icon-class="eye-open" class="mr2"></svg-icon
              >{{ $t("CMS.ContentCore.Preview") }}
            </el-button>
            <el-button
              type="text"
              icon="Edit"
              v-hasPermi="[$p('PageWidget:Edit:{0}', [scope.row.pageWidgetId])]"
              @click="handleEdit(scope.row)"
              >{{ $t("Common.Edit") }}</el-button
            >
            <el-button
              type="text"
              icon="Delete"
              v-hasPermi="[
                $p('PageWidget:Delete:{0}', [scope.row.pageWidgetId]),
              ]"
              @click="handleDelete(scope.row)"
              >{{ $t("Common.Delete") }}</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="loadAdSpaceList"
      />
    </el-row>
    <!-- 表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      :close-on-click-modal="false"
      width="600px"
      append-to-body
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('CMS.PageWidget.Name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.Code')" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item
          :label="$t('CMS.PageWidget.PublishPipe')"
          prop="publishPipeCode"
        >
          <el-select v-model="form.publishPipeCode">
            <el-option
              v-for="pp in publishPipes"
              :key="pp.pipeCode"
              :label="pp.pipeName"
              :value="pp.pipeCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.Path')" prop="path">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item :label="$t('CMS.PageWidget.Template')" prop="template">
          <el-input v-model="form.template">
            <template #append>
              <el-button type="primary" @click="handleSelectTemplate()">{{ $t("Common.Select") }}</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :maxlength="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleDialogOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleDialogClose">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <!-- 模板选择组件 -->
    <cms-template-selector
      :open="openTemplateSelector"
      :publishPipeCode="form.publishPipeCode"
      @ok="handleTemplateSelected"
      @cancel="handleTemplateSelectorCancel"
    />
  </div>
</template>
<script setup name="CmsAdAdSpace">
import { codeValidator, pathValidator } from "@/utils/validate";
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import * as advertisementApi from "@/api/advertisement/advertisement";
import CmsTemplateSelector from "@/views/cms/contentcore/templateSelector";

const { proxy } = getCurrentInstance();

const loading = ref(true);
const dataList = ref(undefined);
const total = ref(0);
const selectedRows = ref([]);
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: undefined,
});

const dialogTitle = ref("");
const dialogVisible = ref(false);
const form = reactive({
  path: "include/ad/",
  publishPipeCode: "",
});

const rules = reactive({
  name: [
    {
      required: true,
      message: proxy.$t("Common.RuleTips.NotEmpty"),
      trigger: "blur",
    },
  ],
  code: [
    {
      required: true,
      message: proxy.$t("Common.RuleTips.NotEmpty"),
      trigger: "blur",
    },
    { validator: codeValidator, trigger: "change" },
  ],
  publishPipeCode: [
    {
      required: true,
      message: proxy.$t("Common.RuleTips.NotEmpty"),
      trigger: "blur",
    },
  ],
  path: [
    {
      required: true,
      message: proxy.$t("Common.RuleTips.NotEmpty"),
      trigger: "blur",
    },
    { validator: pathValidator, trigger: "change" },
  ],
});

const publishPipes = ref([]);
const openTemplateSelector = ref(false);

function loadPublishPipes() {
  getPublishPipeSelectData().then((response) => {
    publishPipes.value = response.data.rows;
  });
}

function loadAdSpaceList() {
  loading.value = true;
  advertisementApi.listAdSpaces(queryParams).then((response) => {
    dataList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
}

function resetQuery() {
  queryParams.pageNum = 1;
  queryParams.name = undefined;
  loadAdSpaceList();
}

function handleSelectionChange(selection) {
  selectedRows.value = selection.map((item) => item);
}

function handleAdd() {
  dialogTitle.value = proxy.$t("CMS.Adv.AddSpaceTitle");
  Object.assign(form, { path: "include/ad/" });
  dialogVisible.value = true;
}

function handleEdit(row) {
  proxy.$router.push({
    path: "/cms/adspace/editor",
    query: { id: row.pageWidgetId, from: "adspace" },
  });
}

function handleDialogClose(reload) {
  dialogVisible.value = false;
  if (reload) {
    loadAdSpaceList();
  }
}

function handleDialogOk() {
  proxy.$refs["formRef"].validate((valid) => {
    if (valid) {
      advertisementApi.addAdSpace(form).then((response) => {
        proxy.$modal.msgSuccess(response.msg);
        handleDialogClose(true);
      });
    }
  });
}

function handleDelete(row) {
  const pageWidgetIds = row.pageWidgetId
    ? [row.pageWidgetId]
    : selectedRows.value.map((item) => item.pageWidgetId);
  if (pageWidgetIds.length == 0) {
    return;
  }
  proxy.$modal
    .confirm(proxy.$t("Common.ConfirmDelete"))
    .then(function () {
      return advertisementApi.deleteAdSpace(pageWidgetIds);
    })
    .then((response) => {
      proxy.$modal.msgSuccess(response.msg);
      loadAdSpaceList();
    })
    .catch(() => {});
}

function handlePublish(row) {
  const pageWidgetIds = [row.pageWidgetId];
  advertisementApi.publishAdSpace(pageWidgetIds).then((response) => {
    proxy.$modal.msgSuccess(response.msg);
    loadAdSpaceList();
  });
}

function handlePreview(row) {
  let routeData = proxy.$router.resolve({
    path: "/cms/preview",
    query: { type: "pagewidget", dataId: row.pageWidgetId },
  });
  window.open(routeData.href, "_blank");
}

function handleSelectTemplate() {
  openTemplateSelector.value = true;
}

function handleTemplateSelected(template) {
  form.template = template;
  openTemplateSelector.value = false;
}

function handleTemplateSelectorCancel() {
  openTemplateSelector.value = false;
}

onMounted(() => {
  loadPublishPipes();
  loadAdSpaceList();
});
</script>
<style scoped>
.adspace-container .el-input,
.el-select,
.el-textarea {
  width: 300px;
}
</style>
