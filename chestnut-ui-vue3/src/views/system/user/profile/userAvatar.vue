<template>
  <div class="user-info-head" @click="editCropper()">
    <el-image :src="options.img" :title="$t('System.User.UploadAvatar')" class="img-circle img-lg">
      <template #error>
        <img src="@/assets/images/default_home_avatar.png" class="img-circle img-lg" />
      </template>
    </el-image>
    <el-dialog 
      :title="title" 
      v-model="open" 
      width="800px" 
      :close-on-click-modal="false" 
      append-to-body 
      @opened="modalOpened" 
      @close="closeDialog">
      <el-row>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <vue-cropper
            ref="cropper"
            :img="options.img"
            :info="true"
            :autoCrop="options.autoCrop"
            :autoCropWidth="options.autoCropWidth"
            :autoCropHeight="options.autoCropHeight"
            :fixedBox="options.fixedBox"
            :outputType="options.outputType"
            @realTime="realTime"
            v-if="visible"
          />
        </el-col>
        <el-col :xs="24" :md="12" :style="{ height: '350px' }">
          <div class="avatar-upload-preview">
            <img :src="options.previews.url" :style="options.previews.img" />
          </div>
        </el-col>
      </el-row>
      <br />
      <el-row>
        <el-col :lg="2" :sm="3" :xs="3">
          <el-upload
            action="#"
            :http-request="requestUpload"
            :show-file-list="false"
            :before-upload="beforeUpload"
          >
            <el-button>
              {{ $t('Common.Upload') }}
              <el-icon class="el-icon--right"><Upload /></el-icon>
            </el-button>
          </el-upload>
        </el-col>
        <el-col :lg="{ span: 1, offset: 2 }" :sm="2" :xs="2">
          <el-button icon="Plus" @click="changeScale(1)"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :sm="2" :xs="2">
          <el-button icon="Minus" @click="changeScale(-1)"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :sm="2" :xs="2">
          <el-button icon="RefreshLeft" @click="rotateLeft()"></el-button>
        </el-col>
        <el-col :lg="{ span: 1, offset: 1 }" :sm="2" :xs="2">
          <el-button icon="RefreshRight" @click="rotateRight()"></el-button>
        </el-col>
        <el-col :lg="{ span: 2, offset: 6 }" :sm="2" :xs="2">
          <el-button type="primary" @click="uploadImg()">{{ $t('Common.Submit') }}</el-button>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup name="ProfileUserAvatar">
import "vue-cropper/dist/index.css"
import { VueCropper } from "vue-cropper"
import { uploadAvatar } from "@/api/system/user"
import { debounce } from '@/utils'
import useUserStore from "@/store/modules/user"

defineProps({
  user: {
    type: Object,
    default: () => ({})
  }
})

const userStore = useUserStore()
const { proxy } = getCurrentInstance()

const open = ref(false)
const visible = ref(false)
const title = ref(proxy.$t('System.User.EditAvatar'))

//图片裁剪数据
const options = reactive({
  img: userStore.avatar,     // 裁剪图片的地址
  autoCrop: true,            // 是否默认生成截图框
  autoCropWidth: 200,        // 默认生成截图框宽度
  autoCropHeight: 200,       // 默认生成截图框高度
  fixedBox: true,            // 固定截图框大小 不允许改变
  outputType: "png",         // 默认生成截图为PNG格式
  filename: 'avatar',        // 文件名称
  previews: {}               //预览数据
})
const resizeHandler = ref(null)

/** 编辑头像 */
function editCropper() {
  open.value = true
}

/** 打开弹出层结束时的回调 */
function modalOpened() {
  visible.value = true
  if (!resizeHandler.value) {
    resizeHandler.value = debounce(() => {
      refresh()
    }, 100)
  }
  window.addEventListener("resize", resizeHandler.value)
}

function refresh() {
  proxy.$refs.cropper.refresh()
}

/** 覆盖默认上传行为 */
function requestUpload() {}

/** 向左旋转 */
function rotateLeft() {
  proxy.$refs.cropper.rotateLeft()
}

/** 向右旋转 */
function rotateRight() {
  proxy.$refs.cropper.rotateRight()
}

/** 图片缩放 */
function changeScale(num) {
  num = num || 1
  proxy.$refs.cropper.changeScale(num)
}

/** 上传预处理 */
function beforeUpload(file) {
  if (file.type.indexOf("image/") == -1) {
    proxy.$modal.msgError(proxy.$t('Common.InvalidFileSuffix', [ '.jpg, .png' ]));
  } else {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => {
      options.img = reader.result
    }
  }
}

/** 上传图片 */
function uploadImg() {
  proxy.$refs.cropper.getCropBlob(data => {
    let formData = new FormData()
    formData.append("avatarfile", data)
    uploadAvatar(formData).then(response => {
      open.value = false
      options.img = import.meta.env.VITE_APP_BASE_API + response.data
      userStore.avatar = options.img
      proxy.$modal.msgSuccess(proxy.$t('Common.EditSuccess'));
      visible.value = false
    })
  })
}

/** 实时预览 */
function realTime(data) {
  options.previews = data
}

/** 关闭窗口 */
function closeDialog() {
  options.img = userStore.avatar
  options.visible = false
}
</script>

<style lang='scss' scoped>
.user-info-head {
  position: relative;
  display: inline-block;
  height: 120px;
}

.user-info-head:hover:after {
  content: "+";
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  color: #eee;
  background: rgba(0, 0, 0, 0.5);
  font-size: 24px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  cursor: pointer;
  line-height: 110px;
  border-radius: 50%;
}
</style>