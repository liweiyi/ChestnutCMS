<template>
  <div class="app-container" style="padding: 0;">
    <el-dialog 
      class="resource-dialog"
      :title="$t('CMS.UEditor.ThirdVideo.DialogTitle')"
      :visible.sync="visible"
      width="600px"
      :close-on-click-modal="false"
      append-to-body>
      <el-row :gutter="24">
        <el-form ref="formVideo" :model="formData" :rules="rules" label-position="top">
          <el-col :span="16">
            <el-form-item :label="$t('CMS.UEditor.ThirdVideo.Code')" prop="code">
              <el-input v-model="formData.code" type="textarea" :rows="12" @change="handleCodeChange"></el-input>
              <div style="color: #909399;font-size:12px;line-height: 30px;">
                <i class="el-icon-info mr5"></i><span v-html="$t('CMS.UEditor.ThirdVideo.CodeTip')"></span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="$t('CMS.UEditor.ThirdVideo.Width')" prop="width">
              <el-input-number v-model="formData.width" :min="100" :max="1000" style="width:180px"></el-input-number>
            </el-form-item>
            <el-form-item :label="$t('CMS.UEditor.ThirdVideo.Height')" prop="height">
              <el-input-number v-model="formData.height" :min="100" :max="1000" style="width:180px"></el-input-number>
            </el-form-item>
            <el-form-item :label="$t('CMS.UEditor.ThirdVideo.Align')" prop="align">
              <el-select v-model="formData.align" style="width:180px">
                <el-option
                  v-for="item in alignOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
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
export default {
  name: "CMSUeditorThirdVideo",
  props: {
    open: {
      type: Boolean,
      default: false,
      required: true
    }
  },
  data () {
    return {
      visible: false,
      alignOptions: [
        {
          label: this.$t('CMS.UEditor.ThirdVideo.AlignCenter'),
          value: "center"
        },
        {
          label: this.$t('CMS.UEditor.ThirdVideo.AlignLeft'),
          value: "left"
        },
        {
          label: this.$t('CMS.UEditor.ThirdVideo.AlignRight'),
          value: "right"
        },
      ],
      formData: {
        code: '',
        width: 720,
        height: 480,
        align: 'center'
      },
      rules: {
        code: [{ required: true, message: this.$t('CMS.UEditor.ThirdVideo.RuleTips.Code'), trigger: "blur" }]
      },
    };
  },
  watch: {
    open (newVal) {
      this.visible = newVal;
    },
    visible (newVal) {
      if (!newVal) {
        this.noticeClose();
      }
    }
  },
  methods: {
    handleOk () {
      this.noticeOk();
    },
    handleCancel () {
      this.visible = false;
    },
    handleCodeChange() {

    },
    noticeOk () {
      if (this.visible) {
        this.$refs["formVideo"].validate(valid => {
          if (valid) {
            var html = this.formData.code
            if (html.startsWith('<iframe')) {
              var regex = /<iframe.*?src\s*=\s*['"](.*?)['"][^>]*>/g;
              var match = regex.exec(html);
              if (match) {
                html = match[1];
              } else {
                this.$modal.msgWarning(this.$t('CMS.UEditor.ThirdVideo.RuleTips.IframeSrc'));
                return;
              }
            }
            var width = this.formData.width && this.formData.width > 0 ? this.formData.width : 720;
            var height = this.formData.height && this.formData.height > 0 ? this.formData.height : 480;
            var align = this.formData.align && this.formData.align.length > 0 ?  this.formData.align : 'center';
            html = '<iframe class="edui-video-iframe"'
                    + ' src="' + window.UE.utils.html(html) + '"'
                    + ' width="' + width + '" height="' + height + '"'
                    + ' scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true" />';
            html = '<p style="text-align:'+ align +'">' + html + '</p>'

            this.$emit("ok", html);
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