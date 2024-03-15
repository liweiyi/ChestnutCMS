<template>
  <div>
    <el-dialog 
      title="选择tag词"
      :visible.sync="visible"
      width="1100px"
      :close-on-click-modal="false"
      custom-class="tag-selector-dialog"
      append-to-body>
      <el-row :gutter="24">
        <el-col :span="4" :xs="24">
          <cms-tagword-group-tree 
            ref="groupTree"
            :showAdd="false"
            @node-click="handleTreeNodeClick">
          </cms-tagword-group-tree>
        </el-col>
        <el-col :span="20" :xs="24">
          <el-row :gutter="24">
            <el-col :span="16" :xs="24">
              <span>标签列表</span>
              <el-divider></el-divider>
              <el-tag 
                :key="tag.wordId"
                v-for="tag in tagList"
                @click="handleSelectTag(tag)">
                {{ tag.word }} 
              </el-tag>
            </el-col>
            <el-col :span="8" :xs="24">
              <span>已选择的标签</span>
              <el-divider></el-divider>
              <el-tag 
                closable
                :key="tag"
                type="success"
                v-for="tag in selectedTags"
                @close="handleRemoveTag(tag)">
                {{ tag }} 
              </el-tag>
            </el-col>
          </el-row>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleClose">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getTagWordList } from "@/api/word/tagWord";
import CMSTagWordGroupTree from '@/views/cms/word/tagWordGroupTree';

export default {
  name: "CMSTagSelector",
  components: {
    'cms-tagword-group-tree': CMSTagWordGroupTree
  },
  props: {
    open: {
      type: Boolean,
      default: false,
      required: true
    }
  },
  watch: {
    open () {
      this.visible = this.open;
    },
    visible (newVal) {
      if (!newVal) {
        this.handleClose();
      } else {
        this.loadWordList();
      }
    }
  },
  data () {
    return {
      loading: false,
      visible: this.open,
      selectedTags: [],
      tagList: [],
      total: 0,
      queryParams: {
        query: undefined,
        groupId: 0,
        pageSize: 100,
        pageNo: 1
      },
    };
  },
  methods: {
    handleTreeNodeClick(data) {
      const groupId = data && data != null ? data.id : 0;
      if (groupId !== this.queryParams.groupId) {
        this.queryParams.groupId = groupId;
        this.loadWordList();
      }
    },
    loadWordList () {
      if (this.queryParams.groupId > 0) {
        this.loading = true;
        getTagWordList(this.queryParams).then(response => {
          this.tagList = response.data.rows;
          this.total = parseInt(response.data.total);
          this.loading = false;
        });
      }
    },
    handleSelectTag (tag) {
      if (this.selectedTags.indexOf(tag.word) > -1) {
        return;
      }
      this.selectedTags.push(tag.word);
    },
    handleRemoveTag (tagWord) {
      this.selectedTags.splice(this.selectedTags.indexOf(tagWord), 1);
    },
    handleOk () {
      this.$emit("ok", this.selectedTags);
    },
    handleClose () {
      this.$emit("close");
      this.queryParams.groupId = 0;
      this.selectedTags = [];
      this.tagList = [];
    },
  }
};
</script>
<style rel="stylesheet/scss" lang="scss">
.tag-selector-dialog .el-dialog__body {
  padding-top: 10px;
  padding-bottom: 10px;
}
.tag-selector-dialog .el-divider {
  margin: 15px 0;
}
.tag-selector-dialog .el-tag {
  margin-bottom: 5px;
  margin-right: 5px;
}
</style>