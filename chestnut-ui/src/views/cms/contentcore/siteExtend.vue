<template>
  <div class="site-extend-container">
    <el-row class="mb12">
      <el-button  
        plain
        type="success"
        icon="el-icon-edit"
        size="mini"
        :disabled="!this.siteId"
        v-hasPermi="[ $p('Site:Edit:{0}', [ siteId ]) ]"
        @click="handleSaveExtend">{{ $t("Common.Save") }}</el-button>
    </el-row>
    <el-form 
      ref="form_extend"
      :model="form_extend"
      v-loading="loading"
      :disabled="!this.siteId"
      label-width="200px">
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t("CMS.Site.Extend.BasicCardTitle") }}</span>
        </div>
        <el-form-item :label="$t('CMS.Site.Extend.EnableIndex')" prop="EnableIndex">
          <el-switch
            v-model="form_extend.EnableIndex"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.TitleRepeatCheck')" prop="RepeatTitleCheck">
          <el-select v-model="form_extend.RepeatTitleCheck" filterable>
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
            v-model="form_extend.SiteExtendModel" 
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
          <el-input-number v-model="form_extend.MaxPageOnContentPublish" controls-position="right" :min="-1"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.EnableEditPublished')" prop="PublishedContentEdit">
          <el-switch
            v-model="form_extend.PublishedContentEdit"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.EnableSSI')" prop="SSIEnabled">
          <el-switch
            v-model="form_extend.SSIEnabled"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.EnableSiteDeleteBackup')" prop="EnableSiteDeleteBackup">
          <el-switch
            v-model="form_extend.EnableSiteDeleteBackup"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.SiteApiUrl')" prop="SiteApiUrl">
          <el-input v-model="form_extend.SiteApiUrl" placeholder="http(s)://"></el-input>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.MemberResourceUrl')" prop="MemberResourceUrl">
          <el-input v-model="form_extend.MemberResourceUrl" placeholder="http(s)://"></el-input>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t("CMS.Site.Extend.CatalogConfCardTitle") }}</span>
        </div>
        <el-form-item :label="$t('CMS.Site.Extend.CatalogPageSize')" prop="CatalogPageSize">
          <el-input-number v-model="form_extend.CatalogPageSize" controls-position="right" :min="0"></el-input-number>
          <div style="color: #909399;font-size:12px;line-height: 30px;">
            <i class="el-icon-info mr5"></i>{{ $t('CMS.Site.Extend.CatalogPageSizeTip') }}
          </div>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t("CMS.Site.Extend.ContentConfCardTitle") }}</span>
        </div>
        <!-- <el-form-item 
          label="文章正文图片尺寸">
          宽：<el-input v-model="form_extend.ArticleImageWidth" style="width:100px"></el-input>
          高：<el-input v-model="form_extend.ArticleImageHeight" style="width:100px"></el-input>
        </el-form-item> -->
        <el-form-item :label="$t('CMS.Site.Extend.AutoArticleLogo')" prop="AutoArticleLogo">
          <el-switch
            v-model="form_extend.AutoArticleLogo"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.DownloadRemoteImage')" prop="DownloadRemoteImage">
          <el-switch
            v-model="form_extend.DownloadRemoteImage"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.RecycleKeepDays')" prop="RecycleKeepDays">
          <el-input-number v-model="form_extend.RecycleKeepDays" controls-position="right" :min="0"></el-input-number>
          <div style="color: #909399;font-size:12px;line-height: 30px;">
            <i class="el-icon-info mr5"></i>{{ $t('CMS.Site.Extend.RecycleKeepDaysTip') }}
          </div>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t("CMS.Site.Extend.ResourceConfCardTitle") }}</span>
        </div>
        <el-form-item :label="$t('CMS.Site.Extend.ThumbnailWidth')">
          <el-input-number v-model="form_extend.ThumbnailWidth" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.ThumbnailHeight')">
          <el-input-number v-model="form_extend.ThumbnailHeight" :min="0"></el-input-number>
          <div style="color: #909399;font-size:12px;line-height: 30px;">
            <i class="el-icon-info mr5"></i>{{ $t('CMS.Site.Extend.ThumbnailSizeTip') }}
          </div>
        </el-form-item>
        <el-form-item :label="$t('CMS.Site.Extend.StorageType')" prop="FileStorageType">
          <el-radio-group v-model="form_extend.FileStorageType" size="mini" @change="handleFileStorageTypeChange">
            <el-radio-button
              v-for="rt in storageTypes"
              :key="rt.id"
              :label="rt.id">{{ rt.name }}</el-radio-button>
          </el-radio-group>
          <div style="color: #909399;font-size:12px;line-height: 30px;">
            <i class="el-icon-info mr5"></i>{{ $t('CMS.Site.Extend.StorageTip') }}
          </div>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="access key">
          <el-input v-model="form_extend.FileStorageArgs.accessKey"></el-input>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="access secret">
          <el-input v-model="form_extend.FileStorageArgs.accessSecret"></el-input>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="bucket">
          <el-input v-model="form_extend.FileStorageArgs.bucket"></el-input>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="domain">
          <el-input v-model="form_extend.FileStorageArgs.domain"></el-input>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="endpoint">
          <el-input v-model="form_extend.FileStorageArgs.endpoint"></el-input>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="region">
          <el-input v-model="form_extend.FileStorageArgs.region"></el-input>
        </el-form-item>
        <el-form-item v-if="form_extend.FileStorageType != 'Local'" label="pipeline">
          <el-input v-model="form_extend.FileStorageArgs.pipeline"></el-input>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t("CMS.Site.Extend.ImageWatermarkCardTitle") }}</span>
        </div>
        <el-form-item :label="$t('CMS.Site.Extend.ImageWatermark')" prop="ImageWatermark">
          <el-switch
            v-model="form_extend.ImageWatermark"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item v-show="form_extend.ImageWatermark=='Y'" :label="$t('CMS.Site.Extend.WatermarkImage')">
          <cms-image-viewer 
            v-model="form_extend.ImageWatermarkArgs.image"
            :src="watermarkImageSrc"
            :site="siteId"
            action="/cms/site/upload_watermarkimage"
          ></cms-image-viewer>
        </el-form-item>
        <el-form-item v-show="form_extend.ImageWatermark=='Y'" :label="$t('CMS.Site.Extend.WatermarkPosition')">
          <div class="watermarker_position">
            <el-radio-group v-model="form_extend.ImageWatermarkArgs.position" text-color="" size="mini">
              <el-radio-button label="TOP_LEFT">↖</el-radio-button>
              <el-radio-button label="TOP_CENTER">↑</el-radio-button>
              <el-radio-button label="TOP_RIGHT">↗</el-radio-button>
              <el-radio-button label="CENTER_LEFT">←</el-radio-button>
              <el-radio-button label="CENTER">┼</el-radio-button>
              <el-radio-button label="CENTER_RIGHT">→</el-radio-button>
              <el-radio-button label="BOTTOM_LEFT">↙</el-radio-button>
              <el-radio-button label="BOTTOM_CENTER">↓</el-radio-button>
              <el-radio-button label="BOTTOM_RIGHT">↘</el-radio-button>
            </el-radio-group>
          </div>
        </el-form-item>
        <el-form-item v-show="form_extend.ImageWatermark=='Y'" :label="$t('CMS.Site.Extend.WatermarkOpacity')">
          <el-input-number 
            v-model="form_extend.ImageWatermarkArgs.opacity"
            controls-position="right"  
            :precision="1" 
            :step="0.1" 
            :min="0.1" 
            :max="1"></el-input-number>
          <el-tooltip placement="right" style="margin-left:5px;">
            <div slot="content">
              {{ $t('CMS.Site.Extend.WatermarkOpacityTip') }}
            </div>
            <i class="el-icon-info"></i>
          </el-tooltip>
        </el-form-item>
        <el-form-item v-show="form_extend.ImageWatermark=='Y'" :label="$t('CMS.Site.Extend.WatermarkRatio')">
          <el-input-number 
            v-model="form_extend.ImageWatermarkArgs.ratio"
            controls-position="right"  
            :step="1" 
            :min="1" 
            :max="100"></el-input-number>
          <el-tooltip placement="right" style="margin-left:5px;">
            <div slot="content">
              {{ $t('CMS.Site.Extend.WatermarkRatioTip') }}
            </div>
            <i class="el-icon-info"></i>
          </el-tooltip>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t("CMS.Site.Extend.WordConfCardTitle") }}</span>
        </div>
        <el-form-item 
          :label="$t('CMS.Site.Extend.SensitiveWordEnable')"
          prop="SensitiveWordEnable">
          <el-switch
            v-model="form_extend.SensitiveWordEnable"
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
            v-model="form_extend.ErrorProneWordEnable"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.HotWordGroup')"
          prop="HotWordGroups">
          <el-checkbox-group v-model="form_extend.HotWordGroups">
            <el-checkbox v-for="group in hotWordGroups" :label="group.code" :key="group.code">{{ group.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.Site.Extend.SEO') }}</span>
        </div>
        <el-form-item 
          :label="$t('CMS.Site.Extend.BaiduPushAccessSecret')"
          prop="BaiduPushAccessSecret">
          <el-input v-model="form_extend.BaiduPushAccessSecret"></el-input>
        </el-form-item>
      </el-card>
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.Site.Extend.StatConfCardTitle') }}</span>
        </div>
        <el-form-item 
          :label="$t('CMS.Site.Extend.BaiduApiKey')"
          prop="BaiduTjApiKey">
          <el-input v-model="form_extend.BaiduTjApiKey"></el-input>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.BaiduSecretKey')"
          prop="BaiduTjSecretKey">
          <el-input v-model="form_extend.BaiduTjSecretKey"></el-input>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.BaiduRefreshToken')"
          prop="BaiduTjRefreshToken">
          <el-input v-model="form_extend.BaiduTjRefreshToken"></el-input>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.BaiduAccessToken')"
          prop="BaiduTjAccessToken">
          <el-input v-model="form_extend.BaiduTjAccessToken"></el-input>
          <span class="btn-cell-wrap ml5">
            <el-button  
              type="primary"
              icon="el-icon-refresh"
              :disabled="!this.siteId"
              v-hasPermi="[ $p('Site:Edit:{0}', [ siteId ]) ]"
              @click="handleRefreshBdTongjiToken">{{ $t("Common.Refresh") }}</el-button>
          </span>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.BaiduDomain')"
          prop="BaiduTjDomain">
          <el-input v-model="form_extend.BaiduTjDomain"></el-input>
          <div style="color: #909399;font-size:12px;line-height: 30px;">
            <i class="el-icon-info mr5"></i>{{ $t('CMS.Site.Extend.BaiduDomainTip') }}
          </div>
        </el-form-item>
        <el-form-item 
          :label="$t('CMS.Site.Extend.EnableStat')"
          prop="EnableStat">
          <el-switch
            v-model="form_extend.EnableStat"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item 
          v-if="form_extend.EnableStat=='Y'"
          :label="$t('CMS.Site.Extend.EnableDynamicStatScript')"
          prop="EnableDynamicStatScript">
          <el-switch
            v-model="form_extend.EnableDynamicStatScript"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
        <el-form-item 
          v-if="form_extend.EnableStat=='Y'"
          :label="$t('CMS.Site.Extend.EnableHyperLogLog')"
          prop="EnableHyperLogLog">
          <el-switch
            v-model="form_extend.EnableHyperLogLog"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
      </el-card>
      
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>{{ $t('CMS.Site.Extend.CommentCardTitle') }}</span>
        </div>
        <el-form-item :label="$t('CMS.Site.Extend.EnableCommentAudit')" prop="EnableCommentAudit">
          <el-switch
            v-model="form_extend.EnableCommentAudit"
            :active-text="$t('Common.Yes')"
            :inactive-text="$t('Common.No')"
            active-value="Y"
            inactive-value="N">
          </el-switch>
        </el-form-item>
      </el-card>
    </el-form>
  </div>
</template>
<script>
import { saveSiteExtends, getSiteExtends } from "@/api/contentcore/site";
import { getHotWordGroupOptions } from "@/api/contentcore/word";
import { listXModelOptions } from "@/api/contentcore/exmodel";
import { refreshBdTongjiToken } from "@/api/stat/baidu";
import CMSSimpleImageViewer from '@/views/cms/components/SimpleImageViewer';

export default {
  name: "CMSSiteExtend",
  components: {
    "cms-image-viewer": CMSSimpleImageViewer
  },
  props: {
    site: {
      type: String,
      default: undefined,
      required: false,
    }
  },
  data () {
    return {
      loading: false,
      siteId: this.site,
      repeatCheckOptions: [
        { label: this.$t("CMS.Site.Extend.TitlteRepeatCheckNone"), value: "0" },
        { label: this.$t("CMS.Site.Extend.TitlteRepeatCheckSite"), value: "1" },
        { label: this.$t("CMS.Site.Extend.TitlteRepeatCheckCatalog"), value: "2" }
        ],
      exmodelOptions: [],
      form_extend: {
        FileStorageArgs: {},
        ImageWatermarkArgs: {},
        HotWordGroups:[]
      },
      hotWordGroups: [],
      storageTypes: [
        { id: "Local", name: this.$t("CMS.Site.Extend.Local") },
        { id: "AliyunOSS", name: this.$t("CMS.Site.Extend.AliyunOSS") },
        { id: "TencentCOS", name: this.$t("CMS.Site.Extend.TencentCOS") },
        { id: "MinIO", name: this.$t("CMS.Site.Extend.MinIO") },
        { id: "AmazonS3", name: "AmazonS3" }
      ]
    };
  },
  computed: {
    watermarkImageSrc() {
      if (this.form_extend.ImageWatermarkArgs.image && this.form_extend.ImageWatermarkArgs.image.length > 0) {
        return this.form_extend.PreviewPrefix + this.form_extend.ImageWatermarkArgs.image;
      }
      return undefined;
    }
  },
  watch: {
    siteId(newVal) {
      if (newVal != undefined && newVal != null && newVal.length > 0) {
        this.loadSiteExtends();
      }
    },
  },
  created() {
    this.loadEXModelList();
    this.loadSiteExtends();
    this.loadHotWordGroups();
  },
  methods: {
    loadSiteExtends () {
      this.loading = true;
      const params = { siteId: this.siteId };
      getSiteExtends(params).then(response => {
        this.form_extend = response.data == null ? {} : response.data;
        this.loading = false;
      });
    },
    loadEXModelList() {
      const params = { siteId: this.siteId };
      listXModelOptions(params).then(response => {
        this.exmodelOptions = response.data.rows.map(m => {
          return { label: m.name, value: m.modelId };
        });
      });
    },
    loadHotWordGroups() {
      getHotWordGroupOptions().then(response => {
        this.hotWordGroups = response.data.rows;
      });
    },
    handleFileStorageTypeChange() {
      if (this.form_extend.FileStorageType === 'Local') {
        this.form_extend.FileStorageArgs = {};
      }
    },
    handleSaveExtend () {
      this.$refs["form_extend"].validate(valid => {
        if (valid) {
          const data = {};
          Object.keys(this.form_extend).forEach(key => {
            if (typeof this.form_extend[key] == 'object') {
            data[key] = JSON.stringify(this.form_extend[key]);
            } else {
              data[key] = this.form_extend[key];
            }
          })
          saveSiteExtends(this.siteId, data).then(response => {
            this.$modal.msgSuccess(this.$t("Common.SaveSuccess"),);
          });
        }
      });
    },
    handleRefreshBdTongjiToken() {
      refreshBdTongjiToken().then(response => {
        this.$modal.msgSuccess(this.$t("Common.SaveSuccess"),);
      });
    }
  }
};
</script>
<style>
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
}
.watermarker_position .el-radio-button {
  margin: 2px;
}
.watermarker_position .el-radio-group .el-radio-button .el-radio-button__inner, 
.watermarker_position .el-radio-button:first-child .el-radio-button__inner,
.watermarker_position .el-radio-button:last-child .el-radio-button__inner {
  width: 56px;
  border: 1px dashed #a7a7a7;
  border-radius: 0;
}
</style>