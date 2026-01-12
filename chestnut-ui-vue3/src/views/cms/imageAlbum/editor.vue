<template>
  <div class="cms-image-editor bg-purple-white">
    <transition-group name="flip-image-list" tag="div">
      <el-card
        shadow="always"
        v-for="(item, index) in imageList"
        :key="item.path"
        class="r-card mt10"
      >
        <el-container class="r-container">
          <el-aside width="240px" class="r-left">
            <el-image :src="item.src" fit="scale-down"></el-image>
            <div class="r-name">{{ item.title }}</div>
            <div class="r-info">
              [ {{ $t("CMS.Image.FileSize") }}: {{ item.fileSizeName }} ]
              <span :v-if="item.resourceType == 'image'">
                [ {{ $t("CMS.Image.WidthHeight") }}: {{ item.width }} x {{ item.height }} ]
              </span>
            </div>
          </el-aside>
          <el-main>
            <el-row>
              <el-form-item :label="$t('CMS.Image.Title')">
                <el-input v-model="item.title"></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item :label="$t('CMS.Image.Summary')">
                <el-input
                  type="textarea"
                  :rows="3"
                  v-model="item.description"
                ></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item :label="$t('CMS.Image.RedirectUrl')">
                <el-input v-model="item.redirectUrl" placeholder="http(s)://"></el-input>
              </el-form-item>
            </el-row>
            <el-row class="r-opr-row" justify="end">
              <el-button link icon="Top" v-if="index > 0" @click="handleUpImage(index)">{{ $t("CMS.Image.MoveUp") }}</el-button>
              <el-button link icon="Bottom" v-if="imageList.length > 1 && index < imageList.length - 1" @click="handleDownImage(index)">{{ $t("CMS.Image.MoveDown") }}</el-button>
              <el-button link icon="Search" @click="handleShowImage(index)">{{ $t("Common.View") }}</el-button>
              <el-button link icon="Picture" @click="handleChooseImage(index)">{{ $t("CMS.Image.SetLogo") }}</el-button>
              <el-button link icon="Crop" @click="handleCropImage(index)">{{ $t("CMS.Resource.Cut") }}</el-button>
              <el-button link icon="Edit" @click="handleEditImage(index)">{{ $t("Common.Edit") }}</el-button>
              <el-button link icon="Delete" @click="handleDeleteImage(index)">{{ $t("Common.Delete") }}</el-button>
            </el-row>
          </el-main>
        </el-container>
      </el-card>
    </transition-group>
    <el-card shadow="always" class="mt10">
      <div class="btn-add-image bg-purple-white" @click="handleAddImage">
        <svg-icon icon-class="image" style="width: 60px; height: 60px;" />
        <div>{{ $t("CMS.Image.Add") }}</div>
      </div>
    </el-card>
    <cms-resource-dialog
      v-if="appStore.isFree"
      v-model:open="openResourceDialog"
      rtype="image"
      :upload-limit="uploadLimit"
      :single="singleUpload"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
    <el-image-viewer
      v-if="showImageViewer"
      :initial-index="imageViewerIndex"
      @close="handleImageViewerClose"
      :url-list="imageList.map((img) => img.src)"
    >
    </el-image-viewer>
  </div>
</template>
<script setup name="CMSImageEditor">
import { ElImageViewer } from "element-plus"
import CmsResourceDialog from "@/views/cms/contentcore/resourceDialog";
import useAppStore from "@/store/modules/app";
import { watch } from "vue";
const appStore = useAppStore();

const { proxy } = getCurrentInstance();
const model = defineModel({
  type: Array,
  default: () => [],
});

const emit = defineEmits(["choose"]);

const openResourceDialog = ref(false);
const editIndex = ref(-1);
const showImageViewer = ref(false);
const imageViewerIndex = ref(0);
const uploadLimit = ref(100);
const singleUpload = ref(false);
const showCropper = ref(false);
const cropImage = ref({})

const imageList = ref([]);

watch(() => model.value, (newVal) => {
  imageList.value = newVal;
});

watch(imageList, (newVal) => {
  model.value = newVal;
});

function handleImageViewerClose() {
  showImageViewer.value = false;
}

function handleResourceDialogOk(results) {
  if (editIndex.value > -1) {
    const r = results[0];
    imageList.value[editIndex.value] = {
      path: r.path,
      src: r.src,
      title: r.name,
      fileName: r.name,
      width: r.width,
      height: r.height,
      fileSize: r.fileSize,
      fileSizeName: r.fileSizeName,
      resourceType: r.resourceType,
    };
    editIndex.value = -1;
  } else {
    results.forEach((r) => {
      imageList.value.push({
        path: r.path,
        src: r.src,
        title: r.name,
        fileName: r.name,
        width: r.width,
        height: r.height,
        fileSize: r.fileSize,
        fileSizeName: r.fileSizeName,
        resourceType: r.resourceType,
      });
    });
  }
}

function handleAddImage() {
  editIndex.value = -1;
  singleUpload.value = false;
  uploadLimit.value = 100;
  openResourceDialog.value = true;
}

function handleEditImage(index) {
  singleUpload.value = true;
  uploadLimit.value = 1;
  editIndex.value = index;
  openResourceDialog.value = true;
}

function handleDeleteImage(index) {
  imageList.value.splice(index, 1);
}

function handleShowImage(index) {
  imageViewerIndex.value = index;
  showImageViewer.value = true;
}

function handleUpImage(index) {
  let temp = imageList.value[index];
  // this.imageList[index] = this.imageList[index - 1];
  imageList.value[index] = imageList.value[index - 1];
  imageList.value[index - 1] = temp;
}

function handleDownImage(index) {
  let temp = imageList.value[index];
  // this.imageList[index] = this.imageList[index + 1];
  imageList.value[index] = imageList.value[index + 1];
  imageList.value[index + 1] = temp;
}

function handleChooseImage(index) {
  emit("choose", imageList.value[index].path, imageList.value[index].src);
}

function handleCropImage(index) {
  cropImage.value = {
    index: index,
    src: imageList.value[index].src,
    resourceId: proxy.$tools.getInternalUrlId(imageList.value[index].path),
  }
  showCropper.value = true;
}

function handleCutDone() {
  showCropper.value = false;
  if (cropImage.value.index > -1) {
    imageList.value[cropImage.value.index].src = proxy.$tools.setUrlParameter(cropImage.value.src, "t", new Date().getTime());
  }
}
</script>
<style lang="scss" scoped>
.cms-image-editor {
  .flip-image-list-move {
    transition: transform 0.5s;
  }

  .flip-image-list-item {
    transition: all 0.5s;
    display: inline-block;
  }

  .flip-image-list-enter {
   
    .flip-image-list-leave-to {
      opacity: 0;
      transform: translateY(30px);
    }
  }

  .flip-image-list-leave-active {
    position: absolute;
  }

  .btn-add-image {
    width: 100%;
    text-align: center;
    display: inline-block;
    cursor: pointer;
    color: #5a5e66;
  }
  
  .btn-add-image:hover {
    color: var(--el-color-primary);
  }

  .r-card {
    .el-card__body {
      padding: 15px;
    }
  }

  .r-container {
    height: 200px;
    overflow: hidden;

    .r-left {
      padding: 0;
      background-color: #fff;
      text-align: center;
      overflow: hidden;
      height: 200px;
      font-size: 12px;
      color: #777;

      .el-image {
        width: 240px;
        height: 150px;
        background-color: #f7f7f7;
      }

      .r-name, .r-info {
        line-height: 20px;
        height: 20px;
        overflow: hidden;
        text-align: left;
      }

      .r-name {
        color: #5587f0;
      }
    }

    .el-main {
      padding: 0px;
      overflow: hidden;

      .el-form-item {
        width: 100%;
        margin-bottom: 12px;
      }

      .el-link {
        text-decoration: none;
        margin-left: 20px;
      }
    }

    .r-opr-row {
      margin-top: 10px;
    }
  }
}
</style>