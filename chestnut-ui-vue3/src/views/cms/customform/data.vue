<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          plain
          type="primary"
          icon="Plus"
          @click="handleAdd">{{ $t("Common.Add") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="success"
          icon="Edit"
          :disabled="ids.length != 1"
          @click="handleEdit">{{ $t("Common.Edit") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="danger"
          icon="Delete"
          :disabled="ids.length == 0"
          @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          plain
          type="warning"
          icon="Close"
          @click="handleClose">{{ $t("Common.Close") }}</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="loadCustomFormDataList"></right-toolbar>
    </el-row>
    <el-row v-show="showSearch">
      <el-form :model="queryParams" ref="queryFormRef" class="el-form-search" :inline="true">
        <el-form-item prop="ip">
          <el-input
            v-model="queryParams.ip"
            clearable
            placeholder="IP"
            style="width: 160px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button-group>
            <el-button type="primary" icon="Search" @click="handleQuery">{{ $t('Common.Search') }}</el-button>
            <el-button icon="Refresh" @click="resetQuery">{{ $t('Common.Reset') }}</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-row>

    <el-table 
      v-loading="loading"
      :data="dataList"
      @selection-change="handleSelectionChange">
      <el-table-column 
        type="selection"
        width="55"
        align="center" />
      <el-table-column 
        v-for="field in fields"
        :key="field.code"
        :label="field.name"
        :show-overflow-tooltip="true">
        <template #default="scope">
          <div v-if="field.controlType=='CMSImage' && scope.row[field.code]">
            <el-image
              v-for="item in scope.row[field.code]"
              :key="item.src"
              :src="item.src"
              style="max-width:300px;">
            </el-image>
          </div>
          <div v-else-if="field.controlType=='CMSContentSelect' && scope.row[field.code]">
            <span>{{ scope.row[field.code].title }}</span>
          </div>
          <div v-else-if="field.controlType=='UEditor' && scope.row[field.code]">
            <el-popover :width="500">
              <template #reference>
                <el-icon><View /></el-icon>
              </template>
              <template #default>
                <div class="rich-content" v-html="scope.row[field.code]"></div>
              </template>
            </el-popover>
          </div>
          <span v-else>{{ scope.row[field.code] }}</span>
        </template>
      </el-table-column>
      <el-table-column  
        :label="$t('Common.Operation')"
        align="center"
        fixed="right"
        width="160" 
      >
        <template #default="scope">
          <el-button 
            type="text"
            icon="Edit"
            @click="handleEdit(scope.row)">{{ $t("Common.Edit") }}</el-button>
          <el-button
            type="text"
            icon="Delete"
            @click="handleDelete(scope.row)">{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadCustomFormDataList" />

    <el-dialog 
      :title="title" 
      v-model="open"
      :close-on-click-modal="false" 
      width="800px" 
      append-to-body>
      <el-form ref="formRef" :model="form" label-width="130px">
        <div v-for="field in fields" :key="field.code">
          <el-form-item
            v-if="field.editable"
            :label="field.name">   
            <el-input v-if="field.controlType === 'input'" v-model="form[field.code]" />
            <el-input v-if="field.controlType === 'textarea'" v-model="form[field.code]" type="textarea" />
            <el-radio-group v-if="field.controlType === 'radio'" v-model="form[field.code]">
              <el-radio v-for="item in field.options" :key="item.value" :label="item.value">{{ item.name }}</el-radio>
            </el-radio-group>
            <el-checkbox-group v-if="field.controlType === 'checkbox'" v-model="form[field.code]">
              <el-checkbox v-for="item in field.options" :key="item.value" :label="item.value">{{ item.name }}
              </el-checkbox>
            </el-checkbox-group>
            <el-select v-if="field.controlType === 'select'" v-model="form[field.code]" clearable>
              <el-option v-for="item in field.options" :key="item.value" :label="item.name" :value="item.value">
              </el-option>
            </el-select>
            <el-date-picker v-if="field.controlType === 'date'" v-model="form[field.code]" type="date" value-format="YYYY-MM-DD">
            </el-date-picker>
            <el-time-picker v-if="field.controlType === 'time'" v-model="form[field.code]" value-format="HH:mm:ss">
            </el-time-picker>
            <el-date-picker v-if="field.controlType === 'datetime'" v-model="form[field.code]" type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss">
            </el-date-picker>   
            <ueditor v-if="field.controlType === 'UEditor'" :editorId="'ex-' + field.fieldName" :height="200" :configs="ueConfigs"
              v-model="form[field.code]">
            </ueditor>
            <cms-content-selector v-if="field.controlType === 'CMSContentSelect'" v-model="form[field.code]"
              :selected="field.valueObj">
            </cms-content-selector>
            <cms-resource-uploader v-if="field.controlType === 'CMSImage'" v-model="form[field.code]"
              type="image"></cms-resource-uploader>
            <cms-resource-uploader v-if="field.controlType === 'CMSResource'" v-model="form[field.code]"></cms-resource-uploader>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleSubmitForm">{{ $t('Common.Submit') }}</el-button>
        <el-button @click="handleCancel">{{ $t('Common.Cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CmsCustomformData2">
  import { listModelAllField } from "@/api/meta/model";
  import { listCustomFormDatas, getCustomFormData, addCustomFormData, editCustomFormData, deleteCustomFormDatas } from "@/api/customform/customform";
  import CmsResourceUploader from '@/views/cms/components/ResourceUploder';
  import CmsContentSelector from '@/views/cms/components/ContentSelector';
  import Ueditor from '@/views/cms/components/UEditorPlus'
  
  const { proxy } = getCurrentInstance()
  
  const loading = ref(true)
  const showSearch = ref(true)
  const ids = ref([])
  const total = ref(0)
  const dataList = ref([])
  const fields = ref([])
  const title = ref("")
  const open = ref(false)
  const objects = reactive({
    queryParams: {
      pageNum: 1,
      pageSize: 10,
      formId: proxy.$route.query.formId
    },
    form: {
    }
  })
  const { queryParams, form } = toRefs(objects)
  const ueConfigs = ref({
    elementPathEnabled: false,
    wordCount: false,
    toolbars: [
      [
        "bold",         // 加粗
        "italic",       // 斜体
        "underline",    // 下划线
        "strikethrough",// 删除线
        "blockquote",   // 引用
        "|",
        "forecolor",    // 字体颜色
        "insertorderedlist",   // 有序列表
        "insertunorderedlist", // 无序列表
        "|",
        "indent",              // 首行缩进
        "lineheight",          // 行间距
        "paragraph",           // 段落格式
        "fontsize",            // 字号
        "|",
        "link",                // 超链接
        "xy-third-video",         // 视频
        'xy-resource',
        "xy-check-word",
        "horizontal",          // 分隔线
        "inserttable",         // 插入表格
      ]
    ],
  });
  
  onMounted(() => {
    loadCustomFormFields();
    loadCustomFormDataList();
  });
  
  function loadCustomFormFields() {
    listModelAllField(queryParams.value.formId, false).then(response => {
      fields.value = response.data.rows;
      loading.value = false;
    });
  }
  
  function loadCustomFormDataList() {
    loading.value = true;
    listCustomFormDatas(queryParams.value).then(response => {
      dataList.value = response.data.rows;
      total.value = parseInt(response.data.total);
      loading.value = false;
    });
  }
  
  function handleQuery() {
    queryParams.value.pageNum = 1;
    loadCustomFormDataList();
  }
  
  function resetQuery() {
    proxy.$refs.queryFormRef.resetFields();
    handleQuery();
  }
  
  function handleSelectionChange(selection) { 
    ids.value = selection.map(item => item.dataId)
  }
  
  function handleAdd() {
    form.value = {};
    open.value = true;
    title.value = proxy.$t('CMS.CustomForm.AddTitle');
  }
  
  function handleEdit(row) {
    form.value = {};
    const dataId = row.dataId ? row.dataId : ids.value[0];
    getCustomFormData(queryParams.value.formId, dataId).then(response => {
      form.value = response.data;
      open.value = true;
      title.value = proxy.$t('CMS.CustomForm.EditDataTitle');
    });
  }
  
  function handleCancel() {
    open.value = false;
  }
  
  function handleSubmitForm() {
    proxy.$refs.formRef.validate(valid => {
      if (valid) {
        if (form.value.dataId) {
          editCustomFormData(queryParams.value.formId, form.value).then(response => {
            proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
            open.value = false;
            loadCustomFormDataList();
          });
        } else {
          addCustomFormData(queryParams.value.formId, form.value).then(response => {
            proxy.$modal.msgSuccess(proxy.$t('Common.AddSuccess'));
            open.value = false;
            loadCustomFormDataList();
          });
        }
      }
    });
  }
  
  function handleDelete(row) {
    const dataIds = row.dataId ? [ row.dataId ] : ids.value;
    const formId = queryParams.value.formId;
    proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(function () {
      return deleteCustomFormDatas(formId, dataIds);
    }).then(() => {
      loadCustomFormDataList();
      proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
    }).catch(function () { });
  }
  
  function handleClose() {
    const obj = { name: "CmsCustomformIndex" };
    proxy.$tab.closeOpenPage(obj);
  }
  </script>
  <style scoped lang="scss">
    .rich-content {
  
      :deep(img) {
        width: 100%;
      }
    }
  </style>