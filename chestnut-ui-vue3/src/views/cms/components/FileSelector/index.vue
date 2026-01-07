<template>
  <div class="app-container" style="padding: 0px;">
    <el-dialog 
      :title="$t('CMS.File.SelectorTitle')"
      v-model="visible"
      width="1100px"
      :close-on-click-modal="false"
      custom-class="file-selector-dialog"
      append-to-body>
      <el-row class="mb10">
        <el-col :span="20" class="path-container">
          <el-link icon="Folder" @click="handlePathClick(-1)"></el-link>
          <div class="path-spliter">/</div>
          <span class="span-path" v-for="(item, index) in pathArray" :key="item" >
            <el-link type="primary" @click="handlePathClick(index)" v-if="index < pathArray.length - 1">{{item}}</el-link>
            <div class="path-spliter" v-if="index < pathArray.length - 1">/</div>
          </span>
        </el-col>
        <el-col :span="4">
          <el-input :placeholder="$t('CMS.File.InputFileName')" v-model="queryParams.fileName" @input="handleFilterFile" />
        </el-col>
      </el-row>
      <el-table v-loading="loading" 
        ref="tableFileListRef"
        :data="fileList"
        highlight-current-row
        @row-click="handleRowClick"
        @selection-change="handleSelectionChange"
        @dblclick="handleRowDblClick"
      >
        <el-table-column v-if="multi" type="selection" width="50" align="center" />
        <el-table-column :label="$t('CMS.File.FileName')" align="left" prop="fileName">
          <template #default="scope">
            <i v-if="scope.row.isDirectory" icon="Folder"></i> <el-button v-if="scope.row.isDirectory" type="text" @click="handleDirectoryClick(scope.row)">{{ scope.row.fileName }}</el-button>
            <span v-else><svg-icon :icon-class="scope.row.iconClass" /> {{ scope.row.fileName }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('CMS.File.FileSize')" align="center" width="160" prop="fileSize"/>
        <el-table-column :label="$t('CMS.File.ModifyTime')" align="center" width="200" prop="modifyTime"/>
      </el-table>
      
      <template #footer>
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleClose">{{ $t("Common.Cancel") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup name="CMSFileSelector">
import { getFileList } from "@/api/contentcore/file";
import { isImage, getFileSvgIconClass } from "@/utils/chestnut";

const { proxy } = getCurrentInstance()

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
    required: true
  },
  multi: {
    type: Boolean,
    default: false,
    required: false
  },
  path: {
    type: String,
    default: '',
    required: false
  },
  suffix: {
    type: String,
    default: '',
    required: false
  }
})

const emit = defineEmits(['ok', 'close'])

const visible = ref(props.open)
const loading = ref(false)
const fileList = ref([])
const sourceFileList = ref([])
const objects = reactive({
  queryParams: {
    filePath: '/',
    fileName: ''
  }
})
const { queryParams } = toRefs(objects)

watch(() => props.path, (newVal) => {
  queryParams.value.filePath = newVal
})

watch(() => props.open, (newVal) => {
  visible.value = newVal
})

watch(() => props.suffix, (newVal) => {
  queryParams.value.fileName = "." + newVal
})

watch(visible, (newVal) => {
  if (!newVal) {
    handleClose();
  } else {
    loadFileList();
  }
})

const pathArray = computed(() => {
  if (queryParams.value.filePath == "/") {
    return [];
  }
  return queryParams.value.filePath.split("/");
})

function loadFileList () {
  if (!visible.value) {
        return;
      }
  loading.value = true;
  getFileList(queryParams.value).then(response => {
    sourceFileList.value = response.data;
    sourceFileList.value.forEach(f => {
      f.isImage = isImage(f.fileName);
      f.iconClass = getFileSvgIconClass(f.fileName);
    });
    fileList.value = sourceFileList.value.map(f => ({ ...f, selected: false }));
    loading.value = false;
  });
}

function handleDirectoryClick(row) {
  queryParams.value.filePath = row.filePath + "/";
  loadFileList();
}

function handlePathClick (index) {
  if (index == -1) {
    queryParams.value.filePath = "/";
  } else {
    queryParams.value.filePath = pathArray.value.slice(0, index + 1).join("/") + "/";
  }
  loadFileList();
}

function handleRowDblClick (row) {
  emit("ok", row);
}

function handleOk () {
  const row = proxy.$refs.tableFileListRef.getCurrentRow();
  if (!row) {
    proxy.$modal.msgError("请选择文件");
    return;
  }
  emit("ok", row);
}

function handleClose () {
  emit("close");
  queryParams.value.filePath = '/';
  sourceFileList.value = [];
  fileList.value = [];
}

function handleFilterFile() {
  fileList.value = sourceFileList.value.filter(data => {
    if (!queryParams.value.fileName || queryParams.value.fileName.length == 0) {
      return true;
    }
    if (!data.fileName.toLowerCase().includes(queryParams.value.fileName.toLowerCase())) {
      return false;
    }
    return true;
  });
}
</script>
<style lang="scss" scoped>
    
    .path-container {
      display: inline-flex;
      align-items: center;
      justify-content: flex-start;

      .path-spliter {
        display: inline-block;
        margin: 0 8px;
        vertical-align: middle;
        position: relative;
      }
    }
</style>