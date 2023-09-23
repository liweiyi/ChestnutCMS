<template>
  <div class="cms-audio-editor bg-purple-white">
    <transition-group name="flip-a-list" tag="div">
      <el-card shadow="always"
              v-for="(item, index) in itemList"
              :key="item.path"
              class="r-card mt10">
        <el-row>
          <aplayer :autoplay="false" preload="none" style="margin:0;margin-bottom:10px;"
            :music="{
              title: item.title,
              artist: item.author,
              src: item.src,
              pic: logo
            }"
          />
        </el-row>
        <el-row>
          <el-form-item :label="$t('CMS.Audio.Parameter')">
            <div style="font-size:12px;color:#777;">
              [ {{ $t('CMS.Audio.Type') }}: {{ item.type }} ]
              [ {{ $t('CMS.Audio.FileSize') }}: {{ item.fileSizeName }} ]
              [ {{ $t('CMS.Audio.Duration') }}: {{ transferDuration(item.duration) }} ]
              [ {{ $t('CMS.Audio.Channels') }}: {{ item.channels }} ]
              [ {{ $t('CMS.Audio.Format') }}: {{ item.format ? item.format : '-' }} ]
              [ {{ $t('CMS.Audio.Decoder') }}: {{ item.decoder ? item.decoder : '-' }} ]
              [ {{ $t('CMS.Audio.BitRate') }}: {{ item.bitRate }} ]
              <el-tooltip placement="top" effect="light">
                <div slot="content">
                  <p>{{ $t('CMS.Audio.SamplingRate') }}：[ {{ item.samplingRate }} ]</p>
                </div>
                <el-link type="primary" :underline="false">{{$t('CMS.Video.Details') }}</el-link>
              </el-tooltip>
            </div>
          </el-form-item>
        </el-row>
        <el-row>
          <el-form-item :label="$t('CMS.Audio.Title')">
            <el-input v-model="item.title"></el-input>
          </el-form-item>
        </el-row>
        <el-row>
          <el-form-item :label="$t('CMS.Audio.Author')">
            <el-input v-model="item.author"></el-input>
          </el-form-item>
        </el-row>
        <el-row>
          <el-form-item :label="$t('CMS.Audio.Desc')">
            <el-input type="textarea" :rows="2" v-model="item.description"></el-input>
          </el-form-item>
        </el-row>
        <el-row class="r-opr-row">
          <el-link 
            icon="el-icon-top"
            v-if="index > 0" 
            @click="moveUp(index)"
          >{{ $t('CMS.Audio.MoveUp') }}</el-link>
          <el-link 
            icon="el-icon-bottom"
            v-if="itemList.length > 1 && index < itemList.length - 1" 
            @click="moveDown(index)"
          >{{ $t('CMS.Audio.MoveUp') }}</el-link>
          <el-link icon="el-icon-edit" @click="editItem(index)">{{ $t('Common.Edit') }}</el-link>
          <el-link icon="el-icon-delete" @click="deleteImage(index)">{{ $t('Common.Delete') }}</el-link>
        </el-row>
      </el-card>
    </transition-group>
    <el-card shadow="always" class="mt10">
        <div class="btn-add-audio bg-purple-white" @click="addItem">
          <svg-icon icon-class="video" style="width:60px;height:60px;"></svg-icon>
          <div>{{ $t('CMS.Audio.Add') }}</div>
        </div>
    </el-card>
    <cms-resource-dialog 
      :open.sync="openResourceDialog"
      :upload-limit="uploadLimit"
      rtype="audio"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
  </div>
</template>
<script>
import Aplayer from 'vue-aplayer'
import CMSResourceDialog from "@/views/cms/contentcore/resourceDialog";

export default {
  name: "CMSAudioList",
  components: {
    "cms-resource-dialog": CMSResourceDialog,
    Aplayer
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
    },
    logo: {
      type: String,
      required: false
    }
  },
  data () {
    return {
      openResourceDialog: false,
      uploadLimit: 10,
      editIndex: -1
    };
  },
  watch: {
    itemList(newVal) {
      this.$emit("change", newVal);
    }
  },
  methods: {
    handleResourceDialogOk (results) {
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
    },
    addItem () {
      this.editIndex = -1;
      this.uploadLimit = 10;
      this.openResourceDialog = true;
    },
    editItem (index) {
      this.uploadLimit = 1;
      this.editIndex = index;
      this.openResourceDialog = true;
    },
    deleteImage (index) {
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
    chooseImage (index) {
        this.$emit("choose", this.itemList[index].path, this.itemList[index].src);
    }
  }
};
</script>
<style>
.cms-audio-editor .flip-a-list-move {
  transition: transform 1s;
}
.cms-audio-editor .flip-a-list-item {
  transition: all 1s;
  display: inline-block;
}
.cms-audio-editor .flip-a-list-enter, .flip-a-list-leave-to
/* .list-complete-leave-active for below version 2.1.8 */ {
  opacity: 0;
  transform: translateY(30px);
}
.cms-audio-editor .flip-a-list-leave-active {
  position: absolute;
}
.cms-audio-editor .el-form-item {
  margin-bottom: 5px;
}
.cms-audio-editor label.a-left-label {
  width: 70px;
  text-align: right;
  vertical-align: middle;
  float: left;
  font-size: 12px;
  color: #606266;
  line-height: 22px;
  padding: 0 12px 0 0;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
}
.cms-audio-editor .a-left-item {
  clear: both;
}
.cms-audio-editor .a-left-value {
  margin-left: 70px;
  line-height: 22px;
  position: relative;
  font-size: 12px;
}
.cms-audio-editor .btn-add-audio {
  width: 100%;
  text-align: center;
  display: inline-block;
  cursor: pointer;
  fill: #5a5e66;
}
.cms-audio-editor .btn-add-audio:hover {
  color:#1890ff;
}
.cms-audio-editor .r-card .el-card__body {
  padding: 15px;
}
.cms-audio-editor .el-link {
  text-decoration: none;
  margin-left: 20px;
}
.cms-audio-editor .r-opr-row {
  text-align: right;
  margin-top: 10px;
}
</style>