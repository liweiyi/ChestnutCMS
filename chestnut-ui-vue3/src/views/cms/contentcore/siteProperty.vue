<template>
  <div class="">
    <el-row :gutter="24">
      <el-col :span="12">
        <el-row :gutter="10">
          <el-col :span="1.5" class="permi-wrap">
            <el-button 
              plain
              type="primary"
              icon="Plus"
              v-hasPermi="[ $p('Site:Edit:{0}', [ props.site ]) ]"
              @click="handleAdd">{{ $t("Common.Add") }}</el-button>
          </el-col>
          <el-col :span="1.5" class="permi-wrap">
            <el-button 
              plain
              type="danger"
              icon="Delete"
              :disabled="selectedIds.length==0"
              v-hasPermi="[ $p('Site:Edit:{0}', [ props.site ]) ]"
              @click="handleDelete">{{ $t("Common.Delete") }}</el-button>
          </el-col>
        </el-row>
      </el-col>
      <el-col :span="12" style="text-align:right">
        <el-form 
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          class="el-form-search">
          <el-form-item prop="query">
            <el-input :placeholder="$t('CMS.Site.Property.QueryPlaceholder')" v-model="queryParams.query"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button-group>
              <el-button 
                type="primary"
                icon="Search"
                @click="handleQuery">{{ $t("Common.Search") }}</el-button>
              <el-button 
                icon="Refresh"
                @click="resetQuery">{{ $t("Common.Reset") }}</el-button>
            </el-button-group>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="propertyList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column :label="$t('CMS.Site.Property.PropName')" prop="propName" />
      <el-table-column :label="$t('CMS.Site.Property.PropCode')" prop="propCode" />
      <el-table-column :label="$t('CMS.Site.Property.PropValue')" prop="propValue" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.Remark')" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column :label="$t('Common.Operation')" align="center" width="180">
        <template #default="scope">
          <el-button 
            link
            type="success"
            icon="Edit"
            v-hasPermi="[ $p('Site:Edit:{0}', [ scope.row.siteId ]) ]"
            @click="handleEdit(scope.row)"
          >{{ $t("Common.Edit") }}</el-button>
          <el-button 
            link
            type="danger"
            icon="Delete"
            v-hasPermi="[ $p('Site:Edit:{0}', [ scope.row.siteId ]) ]"
            @click="handleDelete(scope.row)"
          >{{ $t("Common.Delete") }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination 
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadSitePropertyList" />

    <!-- 添加或修改对话框 -->
    <el-dialog 
      :title="title"
      v-model="open"
      :close-on-click-modal="false"
      width="500px"
      append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item :label="$t('CMS.Site.Property.PropName')" prop="propName">
          <el-input v-model="form.propName"/>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Property.PropCode')" prop="propCode">
          <el-input v-model="form.propCode" />
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Property.PropValue')" prop="propValue">
          <el-input v-model="form.propValue" />
        </el-form-item>
        <el-form-item :label="$t('Common.Remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSSitePropperty">
import { codeValidator } from '@/utils/validate';
import { getSitePropertyList, addSiteProperty, saveSiteProperty, deleteSiteProperty  } from "@/api/contentcore/site";

const props = defineProps({
  site: {
    type: String,
    default: undefined,
    required: false,
  }
});

const { proxy } = getCurrentInstance()

const loading = ref(false)
const selectedIds = ref([])
const total = ref(0)
const propertyList = ref([])
const title = ref("")
const open = ref(false)
const objects = reactive({
  queryParams: {
    siteId: props.site,
    pageNum: 1,
    pageSize: 20,
    query: undefined
  },
  form: {},
  rules: {
    propName: [
      { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
    ],
    propCode: [
      { required: true, message: proxy.$t("Common.RuleTips.NotEmpty"), trigger: "blur" },
      { validator: codeValidator, trigger: "change" }
    ]
  }
});
const { queryParams, form, rules } = toRefs(objects);

watch(() => props.site, (newVal, oldVal) => {
  if (proxy.$tools.isNotEmpty(newVal) && newVal != oldVal) {
    loadSitePropertyList();
  }
});

onMounted(() => {
  loadSitePropertyList();
});

const loadSitePropertyList = () => {
  loading.value = true;
  getSitePropertyList({ siteId: props.site, ...queryParams.value }).then(response => {
    propertyList.value = response.data.rows;
    total.value = parseInt(response.data.total);
    loading.value = false;
  });
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  loadSitePropertyList();
};
const resetQuery = () => {
  proxy.resetForm("queryFormRef");
  handleQuery();
};
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.propertyId);
};
const cancel = () => {
  open.value = false;
  form.value = {};
};
const handleAdd = () => {
  form.value = { siteId: props.site };
  title.value = proxy.$t("CMS.Site.Property.AddTitle");
  open.value = true;
};
const handleEdit = (row) => {
  form.value = row;
  title.value = proxy.$t("CMS.Site.Property.EditTitle");
  open.value = true;
};
const submitForm = () => {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      if (form.value.propertyId) {
        saveSiteProperty(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          open.value = false;
          loadSitePropertyList();
        }); 
      } else {
        form.value.siteId = props.site;
        addSiteProperty(form.value).then(response => {
          proxy.$modal.msgSuccess(proxy.$t('Common.SaveSuccess'));
          open.value = false;
          loadSitePropertyList();
        }); 
      }
    }
  });
};
const handleDelete = (row) => {
  const propertyIds = row.propertyId ? [ row.propertyId ] : selectedIds.value;
  proxy.$modal.confirm(proxy.$t('Common.ConfirmDelete')).then(() => {
    return deleteSiteProperty(propertyIds);
  }).then(() => {
    loadSitePropertyList();
    proxy.$modal.msgSuccess(proxy.$t('Common.DeleteSuccess'));
  }).catch(() => { });
};
</script>