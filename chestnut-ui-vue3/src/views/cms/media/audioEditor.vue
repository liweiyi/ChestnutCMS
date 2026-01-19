<template>
  <div class="cms-audio-editor bg-purple-white">
    <transition-group name="flip-a-list" tag="div">
      <el-card
        shadow="always"
        v-for="(item, index) in itemList"
        :key="item.path"
        class="r-card mt10"
      >
        <el-row>
          <APlayer
            style="margin: 0; margin-bottom: 10px"
            :autoSwitch="false"
            :audio="[{
              name: item.title,
              artist: item.author || 'Unknown Artist',
              url: item.src,
              cover: item.coverSrc || '',
              lrc: item.lrc || '',
              theme: item.theme || '#41b883',
            }]"
          />
        </el-row>
        <el-row>
          <el-form-item :label="$t('CMS.Audio.Parameter')">
            <div class="audio-info">
              [ {{ $t("CMS.Audio.Type") }}: {{ item.type }} ] 
              [ {{ $t("CMS.Audio.FileSize") }}: {{ item.fileSizeName }} ] 
              [ {{ $t("CMS.Audio.Duration") }}: {{ $tools.transferDuration(item.duration) }} ] 
              [ {{ $t("CMS.Audio.Format") }}: {{ item.format ? item.format : "-" }} ] 
              <el-tooltip placement="top" effect="light">
                <template #content>
                  <p>{{ $t("CMS.Audio.Channels") }}：[ {{ item.channels }} ]</p>
                  <p>{{ $t("CMS.Audio.Decoder") }}：[ {{ item.decoder ? item.decoder : "-" }} ]</p>
                  <p>{{ $t("CMS.Audio.BitRate") }}：[ {{ item.bitRate }} ]</p>
                  <p>{{ $t("CMS.Audio.SamplingRate") }}：[ {{ item.samplingRate }} ]</p>
                </template>
                <el-link type="primary" :underline="false">{{ $t("CMS.Video.Details") }}</el-link>
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
            <el-input
              type="textarea"
              :rows="3"
              v-model="item.description"
            ></el-input>
          </el-form-item>
        </el-row>
        <el-row class="mt10" justify="end">
          <el-button link icon="Top" v-if="index > 0" @click="handleMoveUp(index)">{{ $t("CMS.Audio.MoveUp") }}</el-button>
          <el-button link icon="Bottom" v-if="itemList.length > 1 && index < itemList.length - 1" @click="handleMoveDown(index)">{{ $t("CMS.Audio.MoveDown") }}</el-button>
          <el-button link icon="Picture" @click="handleCover(index)">{{ $t("CMS.Audio.Cover") }}</el-button>
          <el-button link icon="Edit" @click="handleEditItem(index)">{{ $t("Common.Edit") }}</el-button>
          <el-button link icon="Delete" @click="handleDeleteItem(index)">{{ $t("Common.Delete") }}</el-button>
        </el-row>
      </el-card>
    </transition-group>
    <el-card shadow="always" class="mt10">
      <div class="btn-add-audio bg-purple-white" @click="handleAddItem">
        <svg-icon
          icon-class="video"
          style="width: 60px; height: 60px"
        ></svg-icon>
        <div>{{ $t("CMS.Audio.Add") }}</div>
      </div>
    </el-card>
    <cms-resource-dialog
      v-if="appStore.isFree"
      v-model:open="openResourceDialog"
      :rtype="resourceType"
      :upload-limit="uploadLimit"
      :single="singleUpload"
      @ok="handleResourceDialogOk">
    </cms-resource-dialog>
  </div>
</template>
<script setup name="CMSAudioEditor">
import APlayer from "@worstone/vue-aplayer";
import CmsResourceDialog from "@/views/cms/contentcore/resourceDialog";
import useAppStore from "@/store/modules/app";
const appStore = useAppStore();

const model = defineModel({
  type: Array,
  default: () => [],
});

const props = defineProps({
  logo: {
    type: String,
    default: "",
  },
});

const itemList = ref([]);
const resourceType = ref('audio');
const openResourceDialog = ref(false);
const uploadLimit = ref(10);
const singleUpload = ref(false);
const editIndex = ref(-1);

watch(() => model.value, (newVal) => {
  itemList.value = newVal;
});

watch(itemList, (newVal) => {
  model.value = newVal || [];
}, { deep: true });

function handleResourceDialogOk(results) {
  if (resourceType.value == 'image') {
    const r = results[0];
    itemList.value[editIndex.value].cover = r.path;
    itemList.value[editIndex.value].coverSrc = r.src;
    editIndex.value = -1;
  } else {
    if (editIndex.value > -1) {
      const r = results[0];
      itemList.value[editIndex.value] = {
        path: r.path,
        src: r.src,
        title: r.name,
        fileName: r.name,
        fileSize: r.fileSize,
        fileSizeName: r.fileSizeName,
        resourceType: r.resourceType,
      };
      editIndex.value = -1;
    } else {
      results.forEach((r) => {
        itemList.value.push({
          path: r.path,
          src: r.src,
          title: r.name,
          fileName: r.name,
          fileSize: r.fileSize,
          fileSizeName: r.fileSizeName,
          resourceType: r.resourceType,
        });
      });
    }
  }
}

function handleAddItem() {
  editIndex.value = -1;
  uploadLimit.value = 10;
  singleUpload.value = false;
  resourceType.value = 'audio';
  openResourceDialog.value = true;
}

function handleEditItem(index) {
  uploadLimit.value = 1;
  singleUpload.value = true;
  editIndex.value = index;
  resourceType.value = 'audio';
  openResourceDialog.value = true;
}

function handleDeleteItem(index) {
  itemList.value.splice(index, 1);
}

function handleMoveUp(index) {
  let temp = itemList.value[index];
  // this.itemList[index] = this.itemList[index - 1];
  itemList.value[index] = itemList.value[index - 1];
  itemList.value[index - 1] = temp;
}

function handleMoveDown(index) {
  let temp = itemList.value[index];
  // this.itemList[index] = this.itemList[index + 1];
  itemList.value[index] = itemList.value[index + 1];
  itemList.value[index + 1] = temp;
}

function handleCover(index) {
  uploadLimit.value = 1;
  singleUpload.value = true;
  editIndex.value = index;
  resourceType.value = 'image';
  openResourceDialog.value = true;
}
</script>
<style lang="scss" scoped>
.cms-audio-editor {
  .flip-a-list-move {
    transition: transform 1s;
  }

  .flip-a-list-item {
    transition: all 1s;
    display: inline-block;
  }

  .flip-a-list-enter, .flip-a-list-leave-to {
    opacity: 0;
    transform: translateY(30px);
  }

  .flip-a-list-leave-active {
    position: absolute;
  }
  
  .el-form-item {
    margin-bottom: 5px;
    width: 100%;
  }

  label.a-left-label {
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

  .a-left-item {
    clear: both;
  }

  .a-left-value {
    margin-left: 70px;
    line-height: 22px;
    position: relative;
    font-size: 12px;
  }

  .btn-add-audio {
    width: 100%;
    text-align: center;
    display: inline-block;
    cursor: pointer;
    fill: #5a5e66;
  }
  
  .btn-add-audio:hover {
    color: #1890ff;
  }

  .r-card .el-card__body {
    padding: 15px;
  }

  .el-link {
    text-decoration: none;
    margin-left: 20px;
  }

  .audio-info {
    font-size: 12px;
    color: #777;
  }
}
</style>