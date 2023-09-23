<template>
  <div class="cms-video-editor bg-purple-white">
    <transition-group name="flip-list" tag="div">
      <el-card shadow="always"
              v-for="(item, index) in itemList"
              :key="item.videoId?item.videoId:item.path"
              class="r-card mt10">
        <el-row :gutter="24" class="r-container">
          <el-col :span="6" class="r-left">
            <div v-if="item.type==ThirdVideoType" v-html="item.path"></div>
            <div v-else>
              <video-player
                class="video-player vjs-custom-skin"
                :options="{
                  playbackRates: [0.5, 1.0, 1.5, 2.0, 3.0], //播放速度
                  currentTime: '00:00',
                  autoplay: false,
                  muted: false, // 默认情况下将会消除任何音频。
                  loop: false,
                  preload: 'none',
                  language: 'zh-CN',
                  aspectRatio: '16:9', // 将播放器置于流畅模式，并在计算播放器的动态大小时使用该值。值应该代表一个比例 - 用冒号分隔的两个数字（例如'16:9'或'4:3'）
                  fluid: true, // 当true时，Video.js player将拥有流体大小。换句话说，它将按比例缩放以适应其容器
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
            <el-row v-if="item.type==ThirdVideoType">
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
                  [ {{ $t('CMS.Video.Duration') }}: {{ transferDuration(item.duration) }} ]
                  [ {{ $t('CMS.Video.WidthHeight') }}: {{ item.width }}x{{ item.height }} ]
                  <el-tooltip placement="top" effect="light">
                    <div slot="content">
                      <p>{{ $t('CMS.Video.Format') }}：[ {{ item.format ? item.format : '-' }} ]</p>
                      <p>{{ $t('CMS.Video.Decoder') }}：[ {{ item.decoder ? item.decoder : '-'  }} ]</p>
                      <p>{{ $t('CMS.Video.BitRate') }}：[ {{ item.bitRate }} ]</p>
                      <p>{{ $t('CMS.Video.FrameRate') }}：[ {{ item.frameRate }} ]</p>
                    </div>
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
                <el-input type="textarea" :rows="2" v-model="item.description"></el-input>
              </el-form-item>
            </el-row>
            <el-row class="r-opr-row">
              <el-link 
                icon="el-icon-top"
                :underline="false"
                v-if="index > 0" 
                @click="moveUp(index)">{{ $t('CMS.Video.MoveUp') }}</el-link>
              <el-link
                icon="el-icon-bottom"
                :underline="false"
                v-if="itemList.length > 1 && index < itemList.length - 1" 
                @click="moveDown(index)">{{ $t('CMS.Video.MoveDown') }}</el-link>
              <el-dropdown @command="handleCoverCommand">
                <span class="el-dropdown-link">
                  <svg-icon icon-class="images" /> {{ $t('CMS.Video.Cover') }}<i class="el-icon-arrow-down el-icon--right"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item icon="el-icon-camera" :command="beforeCommand(index, 'Screenshot')">{{ $t('CMS.Video.ScreenshotCover') }}</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-upload" :command="beforeCommand(index, 'Upload')">{{ $t('CMS.Video.UploadCover') }}</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <el-link 
                icon="el-icon-picture"
                :underline="false"
                @click="setLogo(index)">{{ $t('CMS.Video.SetLogo') }}</el-link>
              <el-link 
                icon="el-icon-edit"
                :underline="false"
                @click="editItem(index)">{{ $t('Common.Edit') }}</el-link>
              <el-link 
                icon="el-icon-delete"
                :underline="false"
                @click="deleteItem(index)">{{ $t('Common.Delete') }}</el-link>
            </el-row>
          </el-col>
        </el-row>
      </el-card>
    </transition-group>
      <el-row :gutter="10" class="mt10">
        <el-col :span="12">
          <el-card shadow="always">
            <div class="btn-add bg-purple-white" @click="addItem">
              <svg-icon icon-class="video" style="width:60px;height:60px;"></svg-icon>
              <div>{{ $t('CMS.Video.Add') }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="always">
            <div class="btn-add bg-purple-white" @click="handleThirdVideo(-1)">
              <svg-icon icon-class="video2" style="width:60px;height:60px;"></svg-icon>
              <div>{{ $t('CMS.Video.AddThirdVideo') }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    <cms-resource-dialog 
      :open.sync="openResourceDialog"
      :upload-limit="uploadLimit"
      :rtype="resourceType"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
    <!-- 添加/修改第三方视频对话框 -->
    <el-dialog 
      :title="$t('CMS.Video.ThirdVideoTitle')"
      :visible.sync="openThirdVideoDialog"
      width="600px"
      append-to-body>
      <el-form ref="form" label-position="top">
        <el-form-item :label="$t('CMS.Video.ThirdVideoCode')">
          <el-input v-model="thirdVideoCode" :rows="8" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleThirdVideoOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancel">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
    <!-- 视频帧截图 -->
    <el-dialog 
      :title="$t('CMS.Video.ScreenshotDialog')"
      :visible.sync="openVideoScreenshotDialog"
      v-loading="screenshotLoading"
      width="600px"
      append-to-body>
      <el-form ref="form" label-position="top">
        <el-form-item label="时间点（单位：秒）">
          <el-input-number v-model="screenshotTime" :min="0" :max="videoSeconds"></el-input-number>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleScreenshot">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="cancelScreenshot">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import CMSResourceDialog from "@/views/cms/contentcore/resourceDialog";
import { videoScreenshot } from "@/api/contentcore/video";

export default {
  name: "CMSVideoList",
  components: {
    "cms-resource-dialog": CMSResourceDialog
  },
  model: {
    prop: 'itemList',
    event: 'change'
  },
  props: {
    itemList: {
      type: Array,
      default: () => [],
      required: false,
    }
  },
  data () {
    return {
      resourceType: 'video',
      ThirdVideoType: "SHARE",
      openResourceDialog: false,
      uploadLimit: 10,
      editIndex: -1,
      openThirdVideoDialog: false,
      thirdVideoCode: "",
      screenshotLoading: false,
      screenshotFlag: false,
      openVideoScreenshotDialog: false,
      videoSeconds: 99999, // 当前截图视频长度（单位：秒）
      screenshotTime: 0
    };
  },
  watch: {
    itemList(newVal) {
      this.$emit("change", newVal);
    }
  },
  methods: {
    handleResourceDialogOk (results) {
      if (this.screenshotFlag) {
          this.screenshotFlag = false;
          const r = results[0];
          let item = this.itemList[this.editIndex];
          item.cover = r.path;
          item.coverSrc = r.src;
          this.editIndex = -1;
      } else {
        if (this.editIndex > -1) {
          const r = results[0];
          this.itemList[this.editIndex] = { 
              path: r.path, 
              src: r.src, 
              title: r.name, 
              fileName: r.name, 
              fileSize: r.fileSize,
              fileSizeName: r.fileSizeName,
              resourceType: r.resourceType
            };
          this.editIndex = -1;
        } else {
          results.forEach(r => {
            this.itemList.push({ 
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
      }
    },
    handleThirdVideo(index) {
      this.editIndex = index;
      this.thirdVideoCode = ""
      this.openThirdVideoDialog = true
      if (index > -1) {
        this.thirdVideoCode = this.itemList[index].path
      }
    },
    handleThirdVideoOk() {
      this.openThirdVideoDialog = false
      if (this.thirdVideoCode && this.thirdVideoCode.length > 0) {
        if (this.editIndex > -1) {
          this.itemList[this.editIndex] = { 
              type: this.ThirdVideoType,
              path: this.thirdVideoCode, 
              src: "", 
              title: "", 
              fileName: "", 
              fileSize: 0,
              fileSizeName: "",
              resourceType: ""
            };
          this.editIndex = -1;
        } else {
          this.itemList.push({ 
            type: this.ThirdVideoType,
            path: this.thirdVideoCode, 
            src: "", 
            title: "", 
            fileName: "", 
            fileSize: 0,
            fileSizeName: "",
            resourceType: ""
          });
        }
      }
    },
    cancel() {
      this.openThirdVideoDialog = false
    },
    addItem () {
      this.editIndex = -1;
      this.uploadLimit = 10;
      this.resourceType = 'video';
      this.openResourceDialog = true;
    },
    editItem (index) {
      let item = this.itemList[index]
      if (item.type == this.ThirdVideoType) {
        this.handleThirdVideo(index)
      } else {
        this.uploadLimit = 1;
        this.editIndex = index;
        this.resourceType = 'video';
        this.openResourceDialog = true;
      }
    },
    deleteItem (index) {
      this.itemList.splice(index, 1);
    }, 
    moveUp (index) {
        let temp = this.itemList[index];
        // this.itemList[index] = this.itemList[index - 1];
        this.$set(this.itemList, index, this.itemList[index - 1]);
        this.$set(this.itemList, index - 1, temp);
    },
    moveDown (index) {
        let temp = this.itemList[index];
        // this.itemList[index] = this.itemList[index + 1];
        this.$set(this.itemList, index, this.itemList[index + 1]);
        this.$set(this.itemList, index + 1, temp);
    },
    // 将视频封面图设置为视频集封面图
    setLogo (index) {
      const item = this.itemList[index];
      if (item.cover && item.cover.length > 0) {
        this.$emit("choose", item.cover, item.coverSrc);
      } else {
        this.$modal.msgWarning("No video cover!")
      }
    },
    uploadVideoCover(index) {
      this.uploadLimit = 1;
      this.editIndex = index;
      this.screenshotFlag = true;
      this.resourceType = 'image';
      this.openResourceDialog = true;
    },
    beforeCommand(index, command) {
      return {index, command}
    },
    handleCoverCommand (arg) {
      const { index, command } = arg;
      if (command == 'Screenshot') {
        this.screenshot(index)
      } else if (command == 'Upload') {
        this.uploadVideoCover(index);
      }
    },
    screenshot(index) {
      this.uploadLimit = 1;
      this.editIndex = index;
      this.openVideoScreenshotDialog = true;
    },
    cancelScreenshot() {
      this.openVideoScreenshotDialog = false;
      this.editIndex = -1;
    },
    handleScreenshot() {
      let item = this.itemList[this.editIndex]
      this.screenshotLoading = true;
      const data = { path: item.path, timestamp: this.screenshotTime }
      videoScreenshot(data).then(res => {
        let item = this.itemList[this.editIndex];
        item.cover = res.data.internalUrl;
        item.coverSrc = res.data.src;
        this.openVideoScreenshotDialog = false;
        this.screenshotLoading = false;
        this.editIndex = -1;
      })
    }
  }
};
</script>
<style>
.cms-video-editor .flip-list-move {
  transition: transform 1s;
}
.cms-video-editor .flip-list-item {
  transition: all 1s;
  display: inline-block;
}
.cms-video-editor .flip-list-enter, .flip-list-leave-to
/* .list-complete-leave-active for below version 2.1.8 */ {
  opacity: 0;
  transform: translateY(30px);
}
.cms-video-editor .flip-list-leave-active {
  position: absolute;
}
.cms-video-editor .btn-add {
  width: 100%;
  text-align: center;
  display: inline-block;
  cursor: pointer;
  fill: #5a5e66;
}
.cms-video-editor .btn-add:hover {
  color:#1890ff;
}
.cms-video-editor .r-card .el-card__body {
  padding: 15px;
}
.cms-video-editor .r-container {
  height: 200px;
  overflow: hidden;
}
.cms-video-editor .r-container .r-left {
  padding: 0;
  background-color: #fff;
  text-align: center;
  overflow: hidden;
  height: 200px;
  font-size: 12px;
  color: #777;
}
.cms-video-editor .r-container .el-form-item {
  margin-bottom: 15px;
}
.cms-video-editor .r-container .el-link, .cms-video-editor .r-container .el-dropdown {
  margin-left: 20px;
}
.cms-video-editor .r-container .r-opr-row {
  text-align: right;
}
</style>