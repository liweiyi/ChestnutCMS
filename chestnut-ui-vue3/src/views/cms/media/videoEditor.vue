<template>
  <div class="cms-video-editor bg-purple-white">
    <transition-group name="flip-list" tag="div">
      <el-card 
        shadow="always"
        v-for="(item, index) in itemList"
        :key="item.videoId?item.videoId:item.path"
        class="r-card mt10">
        <el-row :gutter="24" class="r-container">
          <el-col :span="6" class="r-left">
            <div v-if="item.type==thirdVideoType" v-html="item.path"></div>
            <div v-else>
              <video-player
                :options="{
                  currentTime: '00:00',
                  preload: 'none',
                  sources: [
                    {
                      src: item.src,//视频地址
                    },
                  ],
                  poster: item.coverSrc, // 视频封面
                  controlBar: {
                    timeDivider: false,
                    durationDisplay: false,
                    remainingTimeDisplay: false,
                    fullscreenToggle: true, //全屏按钮
                  },
                }"
              ></video-player>
            </div>
          </el-col>
          <el-col :span="18" class="r-right">
            <el-row v-if="item.type==thirdVideoType">
              <el-form-item :label="$t('CMS.Video.Parameter')" style="margin-bottom:0;">
                <div style="font-size:12px;color:#777;">
                  [ {{ $t('CMS.Video.Type') }}: {{ item.type }} ]
                </div>
              </el-form-item>
            </el-row>
            <el-row v-else>
              <el-form-item :label="$t('CMS.Video.Parameter')" style="margin-bottom:0;">
                <div style="font-size:12px;color:#777;">
                  [ {{ $t('CMS.Video.Type') }}: {{ item.type }} ]
                  [ {{ $t('CMS.Video.FileSize') }}: {{ item.fileSizeName }} ]
                  [ {{ $t('CMS.Video.Duration') }}: {{ $tools.transferDuration(item.duration) }} ]
                  [ {{ $t('CMS.Video.WidthHeight') }}: {{ item.width }}x{{ item.height }} ]
                  <el-tooltip placement="top" effect="light">
                    <template #content>
                      <p>{{ $t('CMS.Video.Format') }}：[ {{ item.format ? item.format : '-' }} ]</p>
                      <p>{{ $t('CMS.Video.Decoder') }}：[ {{ item.decoder ? item.decoder : '-'  }} ]</p>
                      <p>{{ $t('CMS.Video.BitRate') }}：[ {{ item.bitRate }} ]</p>
                      <p>{{ $t('CMS.Video.FrameRate') }}：[ {{ item.frameRate }} ]</p>
                    </template>
                    <el-link type="primary" :underline="false">{{$t('CMS.Video.Details') }}</el-link>
                  </el-tooltip>
                </div>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item :label="$t('CMS.Video.Title')">
                <el-input v-model="item.title"></el-input>
              </el-form-item>
            </el-row>
            <el-row>
              <el-form-item :label="$t('CMS.Video.Desc')">
                <el-input type="textarea" :rows="3" v-model="item.description"></el-input>
              </el-form-item>
            </el-row>
            <el-row class="mt10" justify="end">
              <el-button link icon="Top" v-if="index > 0" @click="handleMoveUp(index)">{{ $t('CMS.Video.MoveUp') }}</el-button>
              <el-button link icon="Bottom" v-if="itemList.length > 1 && index < itemList.length - 1" @click="handleMoveDown(index)">{{ $t('CMS.Video.MoveDown') }}</el-button>
              <el-dropdown v-if="item.type!=thirdVideoType" @command="handleCoverCommand" class="mr20">
                <el-button link icon="Picture">
                  {{ $t('CMS.Video.Cover') }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item icon="Camera" :command="beforeCommand(index, 'Screenshot')">{{ $t('CMS.Video.ScreenshotCover') }}</el-dropdown-item>
                    <el-dropdown-item icon="Upload" :command="beforeCommand(index, 'Upload')">{{ $t('CMS.Video.UploadCover') }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button v-if="item.type!=thirdVideoType" link icon="Picture" @click="handleSetLogo(index)">{{ $t('CMS.Video.SetLogo') }}</el-button>
              <el-button link icon="Edit" @click="handleEdit(index)">{{ $t('Common.Edit') }}</el-button>
              <el-button link icon="Delete" @click="handleDelete(index)">{{ $t('Common.Delete') }}</el-button>
            </el-row>
          </el-col>
        </el-row>
      </el-card>
    </transition-group>
    <el-row :gutter="10" class="mt10">
      <el-col :span="12">
        <el-card shadow="always">
          <div class="btn-add bg-purple-white" @click="handleAddItem">
            <svg-icon icon-class="video" style="width:60px;height:60px;"></svg-icon>
            <div>{{ $t('CMS.Video.Add') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="always">
          <div class="btn-add bg-purple-white" @click="handleAddThirdVideo(-1)">
            <svg-icon icon-class="video2" style="width:60px;height:60px;"></svg-icon>
            <div>{{ $t('CMS.Video.AddThirdVideo') }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <cms-resource-dialog
      v-if="appStore.isFree"
      v-model:open="openResourceDialog"
      :rtype="resourceType"
      :upload-limit="uploadLimit"
      :single="singleUpload"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
    <!-- 添加/修改第三方视频对话框 -->
    <el-dialog 
      :title="$t('CMS.Video.ThirdVideoTitle')"
      v-model="openThirdVideoDialog"
      width="600px"
      append-to-body>
      <el-form ref="formThirdVideoRef" label-position="top">
        <el-form-item :label="$t('CMS.Video.ThirdVideoCode')">
          <el-input v-model="thirdVideoCode" :rows="8" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleAddThirdVideoOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancel">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
    <!-- 视频帧截图 -->
    <el-dialog 
      :title="$t('CMS.Video.ScreenshotDialog')"
      v-model="openVideoScreenshotDialog"
      width="600px"
      append-to-body>
      <el-form ref="formScreenshotRef" label-position="top" v-loading="screenshotLoading">
        <el-form-item :label="$t('CMS.Video.TimePoint')">
          <el-input-number v-model="screenshotTime" :min="0" :max="videoSeconds"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="handleScreenshot">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleCancelScreenshot">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSVideoEditor">
// 视频播放器
import VideoPlayer from '@/views/components/VideoPlayer';
import CmsResourceDialog from "@/views/cms/contentcore/resourceDialog";
import { videoScreenshot } from "@/api/contentcore/video";
import useAppStore from "@/store/modules/app";
import { watch } from 'vue';
const appStore = useAppStore();

const { proxy } = getCurrentInstance();

const model = defineModel({
  type: Array,
  default: () => [],
});

const emit = defineEmits(["choose"]);

const itemList = ref([])
const resourceType = ref('video')
const thirdVideoType = ref("SHARE");
const openResourceDialog = ref(false)
const uploadLimit = ref(10)
const singleUpload = ref(false)
const editIndex = ref(-1)
const openThirdVideoDialog = ref(false)
const thirdVideoCode = ref("")
const screenshotLoading = ref(false)
const screenshotFlag = ref(false)
const openVideoScreenshotDialog = ref(false)
const videoSeconds = ref(99999) // 当前截图视频长度（单位：秒）
const screenshotTime = ref(0)

watch(() => model.value, (newVal) => {
  console.log('videoEditor.watch model.value', newVal);
  itemList.value = newVal;
});

watch(itemList, (newVal) => {
  console.log('videoEditor.watch itemList', newVal);
  model.value = newVal || [];
}, { deep: true });

function handleResourceDialogOk(results) {
  if (screenshotFlag.value) {
    screenshotFlag.value = false;
      const r = results[0];
      let item = itemList.value[editIndex.value];
      item.cover = r.path;
      item.coverSrc = r.src;
      editIndex.value = -1;
  } else {
    console.log(editIndex.value, results)
    if (editIndex.value > -1) {
      const r = results[0];
      itemList.value[editIndex.value] = { 
          path: r.path, 
          src: r.src, 
          title: r.name, 
          fileName: r.name, 
          fileSize: r.fileSize,
          fileSizeName: r.fileSizeName,
          resourceType: r.resourceType
        };
      editIndex.value = -1;
    } else {
      results.forEach(r => {
        itemList.value.push({ 
          path: r.path, 
          src: r.src, 
          title: r.name, 
          fileName: r.name, 
          fileSize: r.fileSize,
          fileSizeName: r.fileSizeName,
          resourceType: r.resourceType
        });
      });
    }
    console.log(itemList.value)
  }
}

function handleAddThirdVideo(index) {
  editIndex.value = index;
  thirdVideoCode.value = ""
  openThirdVideoDialog.value = true
  if (index > -1) {
    thirdVideoCode.value = itemList.value[index].path
  }
}

function handleAddThirdVideoOk() {
  openThirdVideoDialog.value = false
  if (thirdVideoCode.value && thirdVideoCode.value.length > 0) {
    if (editIndex.value > -1) {
      itemList.value[editIndex.value] = { 
          type: thirdVideoType.value,
          path: thirdVideoCode.value, 
          src: "", 
          title: "", 
          fileName: "", 
          fileSize: 0,
          fileSizeName: "",
          resourceType: ""
        };
      editIndex.value = -1;
    } else {
      itemList.value.push({ 
        type: thirdVideoType.value,
        path: thirdVideoCode.value, 
        src: "", 
        title: "", 
        fileName: "", 
        fileSize: 0,
        fileSizeName: "",
        resourceType: ""
      });
    }
  }
}

function handleCancel() {
  openThirdVideoDialog.value = false
}

function handleAddItem () {
  editIndex.value = -1;
  uploadLimit.value = 10;
  singleUpload.value = false;
  resourceType.value = 'video';
  openResourceDialog.value = true;
}

function handleEdit (index) {
  let item = itemList.value[index]
  if (item.type == thirdVideoType.value) {
    handleAddThirdVideo(index)
  } else {
    editIndex.value = index;
    uploadLimit.value = 1;
    singleUpload.value = true;
    resourceType.value = 'video';
    openResourceDialog.value = true;
  }
}

function handleDelete (index) {
  itemList.value.splice(index, 1);
}

function handleMoveUp (index) {
    let temp = itemList.value[index];
    // itemList.value[index] = itemList.value[index - 1];
    itemList.value[index] = itemList.value[index - 1];
    itemList.value[index - 1] = temp;
}

function handleMoveDown (index) {
    let temp = itemList.value[index];
    // itemList.value[index] = itemList.value[index + 1];
    itemList.value[index] = itemList.value[index + 1];
    itemList.value[index + 1] = temp;
}

// 将视频封面图设置为视频集封面图
function handleSetLogo (index) {
  const item = itemList.value[index];
  if (item.cover && item.cover.length > 0) {
    emit("choose", item.cover, item.coverSrc);
  } else {
    proxy.$modal.msgWarning("No video cover!")
  }
}

function uploadVideoCover(index) {
  uploadLimit.value = 1;
  editIndex.value = index;
  screenshotFlag.value = true;
  resourceType.value = 'image';
  openResourceDialog.value = true;
}

function beforeCommand(index, command) {
  return {index, command}
}

function handleCoverCommand (arg) {
  const { index, command } = arg;
  if (command == 'Screenshot') {
    screenshot(index)
  } else if (command == 'Upload') {
    uploadVideoCover(index);
  }
}

function screenshot(index) {
  editIndex.value = index;
  openVideoScreenshotDialog.value = true;
}

function handleCancelScreenshot() {
  openVideoScreenshotDialog.value = false;
  editIndex.value = -1;
}

function handleScreenshot() {
  let item = itemList.value[editIndex.value]
  screenshotLoading.value = true;
  const data = { path: item.path, timestamp: screenshotTime.value }
  videoScreenshot(data).then(res => {
    let item = itemList.value[editIndex.value];
    item.cover = res.data.internalUrl;
    item.coverSrc = res.data.src;
    openVideoScreenshotDialog.value = false;
    screenshotLoading.value = false;
    editIndex.value = -1;
  })
}
</script>
<style lang="scss" scoped>
.cms-video-editor {
  .flip-list-move {
    transition: transform 1s;
  }

  .flip-list-item {
    transition: all 1s;
    display: inline-block;
  }

  .flip-list-enter {
   
    .flip-list-leave-to {
      opacity: 0;
      transform: translateY(30px);
    }
  }

  .flip-list-leave-active {
    position: absolute;
  }
 
  .btn-add {
    width: 100%;
    text-align: center;
    display: inline-block;
    cursor: pointer;
    fill: #5a5e66;

    &:hover {
      color: #1890ff
    }
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
    }

    .el-form-item {
      margin-bottom: 15px;
      width: 100%;
    }

    .el-link, .el-dropdown {
      margin-left: 20px;
    }
  }
}
</style>