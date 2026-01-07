<template>
  <div class="app-container adv-editor-container" v-loading="loading">
    <el-container>
      <el-header height="40px">
        <el-row :gutter="10" class="btn-row">
          <el-col :span="1.5">
            <el-button 
              plain
              type="info"
              icon="Back"
              @click="handleGoBack">{{ $t('CMS.Adv.GoBack') }}</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button 
              plain
              type="success"
              icon="Edit"
              v-hasPermi="[ $p('PageWidget:Edit:{0}', [ adSpaceId ]) ]"
              @click="handleSave">{{ $t("Common.Save") }}</el-button>
          </el-col>
        </el-row>
      </el-header>
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="110px">
        <el-container>
          <el-aside style="width:500px;">
            <el-card shadow="never">
              <template #header>
                <div class="clearfix">
                  <span>{{ $t('CMS.Adv.Basic') }}</span>
                </div>
              </template>
              <el-form-item :label="$t('CMS.Adv.AdName')" prop="name">
                <el-input v-model="form.name" />
              </el-form-item>
              <el-form-item :label="$t('CMS.Adv.Type')" prop="type">
                <el-select v-model="form.type">
                  <el-option
                    v-for="t in adTypes"
                    :key="t.id"
                    :label="t.name"
                    :value="t.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item :label="$t('CMS.Adv.Weight')" prop="weight">
                <el-input-number v-model="form.weight" :min="0"></el-input-number>
              </el-form-item>
              <el-form-item :label="$t('CMS.Adv.OnlineDate')" prop="onlineDate">
                <el-date-picker v-model="form.onlineDate" value-format="YYYY-MM-DD HH:mm:ss" type="datetime" />
              </el-form-item>
              <el-form-item :label="$t('CMS.Adv.OfflineDate')" prop="offlineDate">
                <el-date-picker v-model="form.offlineDate" value-format="YYYY-MM-DD HH:mm:ss" type="datetime" />
              </el-form-item>
              <el-form-item :label="$t('Common.Remark')" prop="remark">
                <el-input v-model="form.remark" type="textarea" :maxlength="100" />
              </el-form-item>
            </el-card>
          </el-aside>
          <el-main>
            <el-card shadow="never">
              <template #header>
                <div class="clearfix; line-height: 32px; font-size: 16px;">
                  <span>{{ $t('CMS.Adv.AdMaterials') }}</span>
                </div>
              </template>
              <el-form-item label=""
                v-if="form.type==='image'"
                prop="resourcePath">
                <cms-logo-view v-model="form.resourcePath" :width="218" :height="150"></cms-logo-view>
              </el-form-item>
              <el-form-item
                :label="$t('CMS.Adv.RedirectUrl')"
                prop="redirectUrl">
                <el-input v-model="form.redirectUrl" placeholder="http(s)://" />
              </el-form-item>
            </el-card>
          </el-main>
        </el-container>
      </el-form>
    </el-container>
  </div>
</template>
<script setup name="CMSAdvertisement">
import { listAdvertisementTypes, getAdvertisement, addAdvertisement, editAdvertisement } from "@/api/advertisement/advertisement";
import CmsLogoView from '@/views/cms/components/LogoView';

const { proxy } = getCurrentInstance();

const loading = ref(false);
const advertisementId = ref(proxy.$route.query.id);
const adSpaceId = ref(proxy.$route.query.adSpaceId);
const adTypes = ref([]);
const form = reactive({
  weight: 100
});

const rules = reactive({
  type: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  name: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  weight: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" },
  ],
  onlineDate: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ],
  offlineDate: [
    { required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }
  ]
});

function loadAdvertisementTypes() {
  listAdvertisementTypes().then(response => {
    adTypes.value = response.data.rows;
    if (!advertisementId.value && adTypes.value.length > 0) {
      form.type = adTypes.value[0].id;
    }
  });
}

function handleGoBack() {
  const obj = { path: "/cms/adspace/editor", query: { id: adSpaceId.value } };
  proxy.$tab.closeOpenPage(obj);
}

function handleSave() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      loading.value = true;
      if (form.advertisementId) {
        editAdvertisement(form).then(response => {
          proxy.$modal.msgSuccess(response.msg);
          loading.value = false;
        });
      } else {
        form.adSpaceId = adSpaceId.value;
        addAdvertisement(form).then(response => {
          proxy.$modal.msgSuccess(response.msg);
          proxy.$router.push({ path: "/cms/ad/editor", query: { adSpaceId: adSpaceId.value, id: response.data } });
          loading.value = false;
        });
      }
    }
  });
}

onMounted(() => {
  loadAdvertisementTypes();
  if (advertisementId.value) {
    loading.value = true;
    getAdvertisement(advertisementId.value).then(response => {
      Object.assign(form, response.data);
      loading.value = false;
    });
  } else {
    let onlineDate = new Date()
    onlineDate.setHours(0)
    onlineDate.setMinutes(0)
    onlineDate.setSeconds(0)
    form.onlineDate = proxy.parseTime(onlineDate)
    form.offlineDate = proxy.parseTime(new Date(2999, 11, 31))
  }
});
</script>
<style scoped>
.adv-editor-container .el-input, .el-textarea, .el-select, .el-input-number {
  width: 300px;
}
.adv-editor-container .el-header {
  padding-left: 0;
}
.adv-editor-container .el-aside, .el-main {
  padding: 0;
}
.adv-editor-container .el-aside {
  padding-right: 10px;
  background-color: #fff;
}
.adv-editor-container .el-aside, .el-container {
  line-height: 24px;
  font-size: 16px;
}
.adv-editor-container .el-card {
  height: 460px;
}
</style>