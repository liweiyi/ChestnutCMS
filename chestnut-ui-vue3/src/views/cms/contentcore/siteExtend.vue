<template>
  <div class="site-extend-container">
    <el-row class="mb8">
      <el-button  
        plain
        type="success"
        icon="Edit"
        :disabled="!props.site"
        v-hasPermi="[ $p('Site:Edit:{0}', [ props.site ]) ]"
        @click="handleSaveExtend">{{ $t("Common.Save") }}</el-button>
    </el-row>
    <el-form 
      ref="formExtendRef"
      :model="formExtend"
      v-loading="loading"
      :disabled="!props.site"
      label-width="220px">
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t("CMS.Site.Extend.BasicCardTitle") }}</span>
        </template>
        <el-form-item :label="$t('CMS.Site.Extend.EnableIndex')" prop="EnableIndex">
          <el-switch
            v-model="formExtend.EnableIndex"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.EnableSearchLog')" prop="EnableSearchLog">
          <el-switch
            v-model="formExtend.EnableSearchLog"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.TitleRepeatCheck')" prop="RepeatTitleCheck">
          <el-select v-model="formExtend.RepeatTitleCheck" filterable>
            <el-option
              v-for="item in repeatCheckOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.ExModel')" prop="SiteExtendModel">
          <el-select 
            v-model="formExtend.SiteExtendModel" 
            filterable 
            clearable >
            <el-option
              v-for="item in exmodelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.PublishMaxPageNum')" prop="MaxPageOnContentPublish">
          <el-input-number v-model="formExtend.MaxPageOnContentPublish" controls-position="right" :min="-1"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.EnableEditPublished')" prop="PublishedContentEdit">
          <el-switch
            v-model="formExtend.PublishedContentEdit"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.EnableSSI')" prop="SSIEnabled">
          <el-switch
            v-model="formExtend.SSIEnabled"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.EnableSiteDeleteBackup')" prop="EnableSiteDeleteBackup">
          <el-switch
            v-model="formExtend.EnableSiteDeleteBackup"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.SiteApiUrl')" prop="SiteApiUrl">
          <el-input v-model="formExtend.SiteApiUrl" placeholder="http(s)://"></el-input>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.MemberResourceUrl')" prop="MemberResourceUrl">
          <el-input v-model="formExtend.MemberResourceUrl" placeholder="http(s)://"></el-input>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t("CMS.Site.Extend.CatalogConfCardTitle") }}</span>
        </template>
        <el-form-item :label="$t('CMS.Site.Extend.CatalogPageSize')" prop="CatalogPageSize">
          <el-input-number v-model="formExtend.CatalogPageSize" controls-position="right" :min="0"></el-input-number>
          <el-tooltip
            :content="$t('CMS.Site.Extend.CatalogPageSizeTip')"
            placement="right"
          ><el-icon class="ml5" style="width: var(--font-size);"><InfoFilled /></el-icon></el-tooltip>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t("CMS.Site.Extend.ContentConfCardTitle") }}</span>
        </template>
        <!-- <el-form-item 
          label="文章正文图片尺寸">
          宽：<el-input v-model="formExtend.ArticleImageWidth" style="width:100px"></el-input>
          高：<el-input v-model="formExtend.ArticleImageHeight" style="width:100px"></el-input>
        </el-form-item> -->
        <el-form-item :label="$t('CMS.Site.Extend.AutoArticleLogo')" prop="AutoArticleLogo">
          <el-switch
            v-model="formExtend.AutoArticleLogo"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.DownloadRemoteImage')" prop="DownloadRemoteImage">
          <el-switch
            v-model="formExtend.DownloadRemoteImage"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
          <el-tooltip
            :content="$t('CMS.Site.Extend.DownloadRemoteImageTip')"
            placement="right"
          >
            <el-icon class="ml5" style="width: var(--font-size);"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.RecycleKeepDays')" prop="RecycleKeepDays">
          <el-input-number v-model="formExtend.RecycleKeepDays" controls-position="right" :min="0"></el-input-number>
          <el-tooltip
            :content="$t('CMS.Site.Extend.RecycleKeepDaysTip')"
            placement="right"
          >
            <el-icon class="ml5" style="width: var(--font-size);"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t("CMS.Site.Extend.ResourceConfCardTitle") }}</span>
        </template>
        <el-form-item :label="$t('CMS.Site.Extend.ThumbnailWidth')">
          <el-input-number v-model="formExtend.ThumbnailWidth" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.ThumbnailHeight')">
          <el-input-number v-model="formExtend.ThumbnailHeight" :min="0"></el-input-number>
          <el-tooltip
            :content="$t('CMS.Site.Extend.ThumbnailSizeTip')"
            placement="right"
          >
            <el-icon class="ml5" style="width: var(--font-size);"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.StorageType')" prop="FileStorageType">
          <el-radio-group v-model="formExtend.FileStorageType" @change="handleFileStorageTypeChange">
            <el-radio-button
              v-for="rt in storageTypes"
              :key="rt.id"
              :label="rt.id">{{ rt.name }}</el-radio-button>
          </el-radio-group>
          <el-tooltip
            :content="$t('CMS.Site.Extend.StorageTip')"
            placement="right"
          >
            <el-icon class="ml5" style="width: var(--font-size);"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item v-if="formExtend.FileStorageType != 'Local'" label="access key">
          <el-input v-model="formExtend.FileStorageArgs.accessKey"></el-input>
        </el-form-item>
        <el-form-item v-if="formExtend.FileStorageType != 'Local'" label="access secret">
          <el-input v-model="formExtend.FileStorageArgs.accessSecret"></el-input>
        </el-form-item>
        <el-form-item v-if="formExtend.FileStorageType != 'Local'" label="bucket">
          <el-input v-model="formExtend.FileStorageArgs.bucket"></el-input>
        </el-form-item>
        <el-form-item v-if="formExtend.FileStorageType != 'Local'" label="domain">
          <el-input v-model="formExtend.FileStorageArgs.domain"></el-input>
        </el-form-item>
        <el-form-item v-if="formExtend.FileStorageType != 'Local'" label="endpoint">
          <el-input v-model="formExtend.FileStorageArgs.endpoint"></el-input>
        </el-form-item>
        <el-form-item v-if="formExtend.FileStorageType != 'Local'" label="region">
          <el-input v-model="formExtend.FileStorageArgs.region"></el-input>
        </el-form-item>
        <el-form-item v-if="formExtend.FileStorageType != 'Local'" label="pipeline">
          <el-input v-model="formExtend.FileStorageArgs.pipeline"></el-input>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t("CMS.Site.Extend.ImageWatermarkCardTitle") }}</span>
        </template>
        <el-form-item :label="$t('CMS.Site.Extend.ImageWatermark')" prop="ImageWatermark">
          <el-switch
            v-model="formExtend.ImageWatermark"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item v-show="formExtend.ImageWatermark=='Y'" :label="$t('CMS.Site.Extend.WatermarkImage')">
          <simple-image-viewer 
            v-model="formExtend.ImageWatermarkArgs.image"
            v-model:src="watermarkImageSrc"
            v-model:site="props.site"
            action="/cms/site/upload_watermarkimage"
          />
        </el-form-item>
        <el-form-item v-show="formExtend.ImageWatermark=='Y'" :label="$t('CMS.Site.Extend.WatermarkPosition')">
          <div class="watermarker_position">
            <el-radio-group v-model="formExtend.ImageWatermarkArgs.position" text-color="">
              <el-radio-button label="TOP_LEFT">↖</el-radio-button>
              <el-radio-button label="TOP_CENTER">↑</el-radio-button>
              <el-radio-button label="TOP_RIGHT">↗</el-radio-button>
              <el-radio-button label="CENTER_LEFT">←</el-radio-button>
              <el-radio-button label="CENTER">+</el-radio-button>
              <el-radio-button label="CENTER_RIGHT">→</el-radio-button>
              <el-radio-button label="BOTTOM_LEFT">↙</el-radio-button>
              <el-radio-button label="BOTTOM_CENTER">↓</el-radio-button>
              <el-radio-button label="BOTTOM_RIGHT">↘</el-radio-button>
            </el-radio-group>
          </div>
        </el-form-item>
        <el-form-item v-show="formExtend.ImageWatermark=='Y'" :label="$t('CMS.Site.Extend.WatermarkOpacity')">
          <el-input-number 
            v-model="formExtend.ImageWatermarkArgs.opacity"
            controls-position="right"  
            :precision="1" 
            :step="0.1" 
            :min="0.1" 
            :max="1"
          ></el-input-number>
          <el-tooltip
            :content="$t('CMS.Site.Extend.WatermarkOpacityTip')"
            placement="right"
          ><el-icon class="ml5" style="width: var(--font-size);"><InfoFilled /></el-icon></el-tooltip>
        </el-form-item>
        <el-form-item v-show="formExtend.ImageWatermark=='Y'" :label="$t('CMS.Site.Extend.WatermarkRatio')">
          <el-input-number 
            v-model="formExtend.ImageWatermarkArgs.ratio"
            controls-position="right"  
            :step="1" 
            :min="1" 
            :max="100"></el-input-number>
          <el-tooltip
            :content="$t('CMS.Site.Extend.WatermarkRatioTip')"
            placement="right"
          ><el-icon class="ml5" style="width: var(--font-size);"><InfoFilled /></el-icon></el-tooltip>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t("CMS.Site.Extend.WordConfCardTitle") }}</span>
        </template>
        <el-form-item 
          :label="$t('CMS.Site.Extend.SensitiveWordEnable')"
          prop="SensitiveWordEnable">
          <el-switch
            v-model="formExtend.SensitiveWordEnable"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.ErrorProneWordEnable')"
          prop="ErrorProneWordEnable">
          <el-switch
            v-model="formExtend.ErrorProneWordEnable"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.HotWordGroup')"
          prop="HotWordGroups">
          <el-checkbox-group v-model="formExtend.HotWordGroups">
            <el-checkbox v-for="group in hotWordGroups" :label="group.value" :key="group.value">{{ group.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.HotWordMaxReplaceCount')" prop="HotWordMaxReplaceCount">
          <el-input-number v-model="formExtend.HotWordMaxReplaceCount" controls-position="right" :min="0"></el-input-number>
          <el-tooltip
            :content="$t('CMS.Site.Extend.HotWordMaxReplaceCountTip')"
            placement="right"
          >
            <el-icon class="ml5" style="width: var(--font-size);"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t('CMS.Site.Extend.SEO') }}</span>
        </template>
        <el-form-item 
          :label="$t('CMS.Site.Extend.BaiduPushAccessSecret')"
          prop="BaiduPushAccessSecret">
          <el-input v-model="formExtend.BaiduPushAccessSecret"></el-input>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t('CMS.Site.Extend.StatConfCardTitle') }}</span>
        </template>
        <el-form-item 
          :label="$t('CMS.Site.Extend.BaiduApiKey')"
          prop="BaiduTjApiKey">
          <el-input v-model="formExtend.BaiduTjApiKey"></el-input>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.BaiduSecretKey')"
          prop="BaiduTjSecretKey">
          <el-input v-model="formExtend.BaiduTjSecretKey"></el-input>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.BaiduRefreshToken')"
          prop="BaiduTjRefreshToken">
          <el-input v-model="formExtend.BaiduTjRefreshToken"></el-input>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.BaiduAccessToken')"
          prop="BaiduTjAccessToken">
          <el-input v-model="formExtend.BaiduTjAccessToken"></el-input>
          <el-button
            class="ml5"
            type="primary"
            icon="el-icon-refresh"
            :disabled="!props.site"
            v-hasPermi="[ $p('Site:Edit:{0}', [ props.site ]) ]"
            @click="handleRefreshBdTongjiToken">{{ $t("Common.Refresh") }}</el-button>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.BaiduDomain')"
          prop="BaiduTjDomain">
          <el-input v-model="formExtend.BaiduTjDomain"></el-input>
          <el-tooltip
            :content="$t('CMS.Site.Extend.BaiduDomainTip')"
            placement="right"
          >
            <el-icon class="ml5" style="width: var(--font-size);"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.EnableStat')"
          prop="EnableStat">
          <el-switch
            v-model="formExtend.EnableStat"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item 
          v-if="formExtend.EnableStat=='Y'"
          :label="$t('CMS.Site.Extend.EnableDynamicStatScript')"
          prop="EnableDynamicStatScript">
          <el-switch
            v-model="formExtend.EnableDynamicStatScript"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item 
          v-if="formExtend.EnableStat=='Y'"
          :label="$t('CMS.Site.Extend.EnableHyperLogLog')"
          prop="EnableHyperLogLog">
          <el-switch
            v-model="formExtend.EnableHyperLogLog"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
      </el-card>
      
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t('CMS.Site.Extend.CommentCardTitle') }}</span>
        </template>
        <el-form-item :label="$t('CMS.Site.Extend.EnableCommentAudit')" prop="EnableCommentAudit">
          <el-switch
            v-model="formExtend.EnableCommentAudit"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
      </el-card>
      
      <el-card shadow="hover">
        <template #header>
          <span>{{ $t('CMS.Site.Extend.CloudCardTitle') }}</span>
        </template>
        <el-form-item :label="$t('CMS.Site.Extend.CdnCloudConfig')" prop="CdnCloudConfig">
          <el-select v-model="formExtend.CdnCloudConfig" clearable>
            <el-option v-for="item in cloudConfigList" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
      </el-card>
    </el-form>
  </div>
</template>
<script setup name="CMSSiteExtend">
import { saveSiteExtends, getSiteExtends } from "@/api/contentcore/site";
import { getHotWordGroupOptions } from "@/api/contentcore/word";
import { listXModelOptions } from "@/api/contentcore/exmodel";
import { refreshBdTongjiToken } from "@/api/stat/baidu";
import SimpleImageViewer from '@/views/cms/components/SimpleImageViewer';
import * as cloudConfigApi from "@/api/cloud/config";

const { proxy } = getCurrentInstance();

const props = defineProps({
  site: {
    type: String,
    default: undefined,
    required: false,
  }
})

const repeatCheckOptions = [
  { label: proxy.$t("CMS.Site.Extend.TitlteRepeatCheckNone"), value: "0" },
  { label: proxy.$t("CMS.Site.Extend.TitlteRepeatCheckSite"), value: "1" },
  { label: proxy.$t("CMS.Site.Extend.TitlteRepeatCheckCatalog"), value: "2" }
]

const storageTypes = [
  { id: "Local", name: proxy.$t("CMS.Site.Extend.Local") },
  { id: "AliyunOSS", name: proxy.$t("CMS.Site.Extend.AliyunOSS") },
  { id: "TencentCOS", name: proxy.$t("CMS.Site.Extend.TencentCOS") },
  { id: "MinIO", name: proxy.$t("CMS.Site.Extend.MinIO") },
  { id: "AmazonS3", name: "AmazonS3" }
]

const loading = ref(false);
const exmodelOptions = ref([]);
const hotWordGroups = ref([]);
const objects = reactive({
  formExtend: {
    FileStorageArgs: {},
    ImageWatermarkArgs: {},
    HotWordGroups:[]
  }
})

const { formExtend } = toRefs(objects);

const cloudConfigList = ref([]);

const watermarkImageSrc = computed(() => {
  if (formExtend.value.ImageWatermarkArgs.image && formExtend.value.ImageWatermarkArgs.image.length > 0) {
    return formExtend.value.PreviewPrefix + formExtend.value.ImageWatermarkArgs.image;
  }
  return undefined;
});

watch(() => props.site, (newVal, oldVal) => {
  if (proxy.$tools.isNotEmpty(newVal) && newVal != oldVal) {
    loadSiteExtends();
  }
})

onMounted(() => {
  loadEXModelList();
  loadSiteExtends();
  loadHotWordGroups();
  loadCloudConfigList();
})

const loadCloudConfigList = () => {
  cloudConfigApi.getOptions().then(response => {
    cloudConfigList.value = response.data;
  });
}

const loadSiteExtends = () => {
  loading.value = true;
  const params = { siteId: props.site };
  getSiteExtends(params).then(response => {
    formExtend.value = response.data == null ? {} : response.data;
    loading.value = false;
  });
}
const loadEXModelList = () => {
  const params = { siteId: props.site };
  listXModelOptions(params).then(response => {
    exmodelOptions.value = response.data.rows.map(m => {
      return { label: m.name, value: m.modelId };
    });
  });
}
const loadHotWordGroups = () => {
  getHotWordGroupOptions().then(response => {
    hotWordGroups.value = response.data;
  });
}
const handleFileStorageTypeChange = () => {
  if (formExtend.value.FileStorageType === 'Local') {
    formExtend.value.FileStorageArgs = {};
  }
}
const handleSaveExtend = () => {
  proxy.$refs.formExtendRef.validate(valid => {
    if (valid) {
      const data = {};
      Object.keys(formExtend.value).forEach(key => {
        if (typeof formExtend.value[key] == 'object') {
        data[key] = JSON.stringify(formExtend.value[key]);
        } else {
          data[key] = formExtend.value[key];
        }
      })
      saveSiteExtends(props.site, data).then(response => {
        proxy.$modal.msgSuccess(proxy.$t("Common.SaveSuccess"),);
      });
    }
  });
}
const handleRefreshBdTongjiToken = () => {
  refreshBdTongjiToken().then(response => {
    proxy.$modal.msgSuccess(proxy.$t("Common.OpSuccess"),);
  });
}
</script>
<style lang="scss" scoped>
.site-extend-container .el-form-item {
  margin-bottom: 12px;
}
.site-extend-container .el-card {
  margin-bottom: 10px;
}
.site-extend-container .el-input, 
.site-extend-container .el-input-number  {
  width: 301.5px;
}
.site-extend-container .el-upload-list {
  width: 300px;
}
.watermarker_position {
  width: 188px;
  border: 1px solid #a7a7a7;
  border-radius: 4px;
  padding: 3px;

  :deep(.el-radio-button) {
    margin: 2px;
  }

  :deep(.el-radio-button__inner) {
    width: 56px;
    border: 1px dashed #a7a7a7;
    border-radius: 0;
  }
}
</style>