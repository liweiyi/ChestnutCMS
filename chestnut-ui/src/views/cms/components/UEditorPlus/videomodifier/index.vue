<template>
  <div style="padding: 0;">
    <el-dialog 
      class="video-modifier"
      :title="$t('CMS.UEditor.VideoModifier.DialogTitle')"
      :visible.sync="visible"
      width="700px"
      :close-on-click-modal="false"
      append-to-body>
      <el-row :gutter="24">
        <el-form ref="formVideo" :model="formData" :rules="rules" label-position="top" :loading="loading">
          <el-col :span="16">
            <el-form-item :label="$t('CMS.UEditor.VideoModifier.Cover')" prop="poster">
              <el-row>
                <cms-logo-view v-model="formData.poster" :src="formData.posterSrc" @update="handleUploadCoverChange" :width="392" :height="220"></cms-logo-view>
              </el-row>
              <el-row>{{ $t('CMS.UEditor.VideoModifier.FrameCoverSeconds') }}</el-row>
              <el-row :gutter="10">
                <el-col :span="1.5">
                  <el-input-number v-model="screenshotTime" :min="0" style="width:180px"></el-input-number>
                </el-col>
                <el-col :span="1.5">
                  <el-button type="primary" @click="handleScreenshot">{{ $t('CMS.UEditor.VideoModifier.Screenshot') }}</el-button>
                </el-col>
              </el-row>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="$t('CMS.UEditor.VideoModifier.Width')" prop="width">
              <el-input v-model="formData.width" style="width:180px"></el-input>
            </el-form-item>
            <el-form-item :label="$t('CMS.UEditor.VideoModifier.Height')" prop="height">
              <el-input v-model="formData.height" style="width:180px"></el-input>
            </el-form-item>
            <el-form-item prop="controls">
              <el-checkbox v-model="formData.controls">{{ $t('CMS.UEditor.VideoModifier.Controls') }}</el-checkbox>
            </el-form-item>
            <el-form-item prop="loop">
              <el-checkbox v-model="formData.loop">{{ $t('CMS.UEditor.VideoModifier.Loop') }}</el-checkbox>
            </el-form-item>
            <el-form-item prop="muted">
              <el-checkbox v-model="formData.muted">{{ $t('CMS.UEditor.VideoModifier.Muted') }}</el-checkbox>
            </el-form-item>
            <el-form-item prop="autoplay">
              <el-checkbox v-model="formData.autoplay">{{ $t('CMS.UEditor.VideoModifier.AutoPlay') }}</el-checkbox>
            </el-form-item>
            <!-- <el-form-item v-if="!formData.autoplay" :label="$t('CMS.UEditor.VideoModifier.Preload')" prop="preload">
              <el-select v-model="formData.preload">
                <el-option value="none">{{ $t('CMS.UEditor.VideoModifier.PreloadNone') }}</el-option>
                <el-option value="meta">{{ $t('CMS.UEditor.VideoModifier.PreloadMeta') }}</el-option>
                <el-option value="auto">{{ $t('CMS.UEditor.VideoModifier.PreloadAuto') }}</el-option>
              </el-select>
            </el-form-item> -->
          </el-col>
        </el-form>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import CMSLogoView from '@/views/cms/components/LogoView';
import { videoScreenshot } from "@/api/contentcore/video";

export default {
  name: "CMSUeditorVideoModifier",
  components: {
    "cms-logo-view": CMSLogoView,
  },
  props: {
    open: {
      type: Boolean,
      default: false,
      required: true
    },
    data: {
      type: Object,
      default: () => {},
      required: true,
    }
  },
  watch: {
    data(newVal) {
      this.formData = newVal;
    },
    open (newVal) {
      this.visible = newVal;
    },
    visible (newVal) {
      if (!newVal) {
        this.noticeClose();
      }
    }
  },
  data () {
    return {
      visible: false,
      loading: false,
      screenshotTime: 0,
      formData: {},
      rules: {},
    };
  },
  methods: {
    handleScreenshot() {
      this.loading = true;
      const data = { path: this.formData.videoIUrl, timestamp: this.screenshotTime }
      videoScreenshot(data).then(res => {
        this.formData.poster = res.data.internalUrl;
        this.formData.posterSrc = res.data.src;
        this.openVideoScreenshotDialog = false;
        this.loading = false;
      })
    },
    handleUploadCoverChange(data) {
      this.formData.poster = data.path;
      this.formData.posterSrc = data.src;
    },
    handleOk () {
      this.noticeOk();
    },
    handleCancel () {
      this.visible = false;
    },
    noticeOk () {
      if (this.visible) {
        this.$refs["formVideo"].validate(valid => {
          if (valid) {
            this.$emit("ok", this.formData);
            this.visible = false;
          }
        });
      }
    },
    noticeClose () {
      if (!this.visible) {
        this.$emit('update:open', false);
        this.$emit("close");
        this.reset();
      }
    },
    reset() {
      this.formData = {
        code: '',
        width: 720,
        height: 480,
        align: 'center'
      }
    }
  }
};
</script>
<style scoped>
::v-deep .el-dialog .el-form-item {
  margin-bottom: 0;
}
::v-deep .el-dialog .el-form-item__label {
  padding-bottom: 5px;
}
::v-deep .el-dialog .el-form--label-top .el-form-item__label {
  padding-bottom: 0;
}
</style>