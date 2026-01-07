<template>
  <div class="app-container" style="padding: 0;">
    <el-dialog 
      class="resource-dialog"
      :title="$t('CMS.UEditor.ThirdVideo.DialogTitle')"
      v-model="visible"
      width="600px"
      :close-on-click-modal="false"
      append-to-body>
      <el-form ref="formVideoRef" :model="formData" :rules="rules" label-position="top">
        <el-row :gutter="24">
          <el-col :span="16">
            <el-form-item :label="$t('CMS.UEditor.ThirdVideo.Code')" prop="code">
              <el-input v-model="formData.code" type="textarea" :rows="12" @change="handleCodeChange"></el-input>
              <div style="color: #909399;font-size:12px;line-height: 30px;">
                <el-icon class="mr5"><InfoFilled /></el-icon><span v-html="$t('CMS.UEditor.ThirdVideo.CodeTip')"></span>
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
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSUeditorThirdVideo">

const { proxy } = getCurrentInstance()

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
    required: true
  }
})

const emit = defineEmits(['update:open', 'ok', 'close']);

const visible = ref(false);
const alignOptions = ref([
  {
    label: proxy.$t('CMS.UEditor.ThirdVideo.AlignCenter'),
    value: "center"
  },
  {
    label: proxy.$t('CMS.UEditor.ThirdVideo.AlignLeft'),
    value: "left"
  },
  {
    label: proxy.$t('CMS.UEditor.ThirdVideo.AlignRight'),
    value: "right"
  }
])
const rules = ref({
  code: [{ required: true, message: proxy.$t('Common.RuleTips.NotEmpty'), trigger: "blur" }]
})
const objects = reactive({
  formData: {
    code: '',
    width: 720,
    height: 480,
    align: 'center'
  }
})
const { formData } = toRefs(objects);

watch(() => props.open, (newVal) => {
  visible.value = newVal;
})
watch(visible, (newVal) => {
  if (!newVal) {
    noticeClose();
  }
})

function handleOk () {
  noticeOk();
}

function handleCancel () {
  visible.value = false;
}

function noticeOk () {
  if (visible.value) {
    proxy.$refs.formVideoRef.validate((valid) => {
      if (valid) {
        var html = formData.value.code
        if (html.startsWith('<iframe')) {
          var regex = /<iframe.*?src\s*=\s*['"](.*?)['"][^>]*>/g;
          var match = regex.exec(html);
          if (match) {
            html = match[1];
          } else {
            proxy.$modal.msgWarning(proxy.$t('CMS.UEditor.ThirdVideo.RuleTips.IframeSrc'));
            return;
          }
        }
        var width = formData.value.width && formData.value.width > 0 ? formData.value.width : 720;
        var height = formData.value.height && formData.value.height > 0 ? formData.value.height : 480;
        var align = formData.value.align && formData.value.align.length > 0 ?  formData.value.align : 'center';
        html = '<iframe class="edui-video-iframe"'
                + ' src="' + window.UE.utils.html(html) + '"'
                + ' width="' + width + '" height="' + height + '"'
                + ' scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true" />';
        html = '<p style="text-align:'+ align +'">' + html + '</p>'

        emit("ok", html);
        visible.value = false;
      }
    });
  }
}

function noticeClose () {
  if (!visible.value) {
    emit("update:open", false);
    emit("close");
    formData.value = {
      code: '',
      width: 720,
      height: 480,
      align: 'center'
    }
  }
}
</script>