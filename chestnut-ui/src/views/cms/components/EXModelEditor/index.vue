<template>
  <div>
    <el-form-item v-for="field in fieldList" 
                  :key="field.fieldName"
                  :label="field.label"
                  :prop="field.fieldName">
      <el-input v-if="field.controlType==='input'" 
                v-model="field.value" />
      <el-input v-if="field.controlType==='textarea'" 
                v-model="field.value"
                type="textarea" />
      <el-radio-group v-if="field.controlType==='radio'" 
                      v-model="field.value">
        <el-radio v-for="item in field.options" 
                  :key="item.value"
                  :label="item.value">{{ item.name }}</el-radio>
      </el-radio-group>
      <el-checkbox-group v-if="field.controlType==='checkbox'" v-model="field.value">
        <el-checkbox 
          v-for="item in field.options" 
          :key="item.value"
          :label="item.value">{{ item.name }}
        </el-checkbox>
      </el-checkbox-group>
      <el-select v-if="field.controlType==='select'" 
                 v-model="field.value" 
                 clearable>
        <el-option
          v-for="item in field.options"
          :key="item.value"
          :label="item.name"
          :value="item.value">
        </el-option>
      </el-select>
      <el-date-picker v-if="field.controlType==='date'" 
        v-model="field.value"
        type="date"
        value-format="yyyy-MM-dd">
      </el-date-picker>
      <el-time-picker v-if="field.controlType==='time'" 
        v-model="field.value"
        value-format="HH:mm:ss">
      </el-time-picker>
      <el-date-picker v-if="field.controlType==='datetime'" 
        v-model="field.value"
        type="datetime"
        value-format="yyyy-MM-dd HH:mm:ss">
      </el-date-picker>
      <cms-logo-view v-if="field.controlType==='CMSImage'" 
        v-model="field.value" 
        :src="field.valueObj" 
        :width="210" 
        :height="150">
      </cms-logo-view>
      <ueditor v-if="field.controlType==='UEditor'" 
        :editorId="'ex-'+field.fieldName" 
        :height="200" 
        :configs="ueConfigs" 
        v-model="field.value">
      </ueditor>
      <cms-content-selector v-if="field.controlType==='CMSContentSelect'" 
        v-model="field.value" 
        :selected="field.valueObj">
      </cms-content-selector>
    </el-form-item>
  </div>
</template>
<script>
import CMSLogoView from '@/views/cms/components/LogoView';
import CMSContentSelector from '@/views/cms/components/ContentSelector';
import UEditor from '@/views/cms/components/UEditorPlus'
import { getXModelFieldData } from "@/api/contentcore/exmodel";

export default {
  name: "CMSEXModelEditor",
  components: {
    "cms-logo-view": CMSLogoView,
    "cms-content-selector": CMSContentSelector,
    'ueditor': UEditor,
  },
  props: {
    xmodel: {
      type: String,
      required: true,
    },
    type: {
      type: String,
      required: true
    },
    id: {
      type: String,
      required: false
    }
  },
  watch: {
    type(newVal) {
      this.dataType = newVal;
    },
    id(newVal) {
      this.dataId = newVal;
    },
    dataId(newVal) {
      if (newVal && newVal != null && newVal.length > 0) {
        this.loadModelFieldData();
      }
    },
  },
  data () {
    return {
      modelId: this.xmodel,                                   
      dataId: this.id,
      dataType: this.type,
      fieldList: [],
      ueConfigs: {
        elementPathEnabled: false,
        wordCount: false,
        toolbars: [
          [
            "bold",         // 加粗
            "italic",       // 斜体
            "underline",    // 下划线
            "strikethrough",// 删除线
            "blockquote",   // 引用
            "|",
            "forecolor",    // 字体颜色
            "insertorderedlist",   // 有序列表
            "insertunorderedlist", // 无序列表
            "|",
            "indent",              // 首行缩进
            "lineheight",          // 行间距
            "paragraph",           // 段落格式
            "fontsize",            // 字号
            "|",
            "link",                // 超链接
            "xy-third-video",         // 视频
            'xy-resource',
            "xy-check-word",
            "horizontal",          // 分隔线
            "inserttable",         // 插入表格
          ]
        ],
      }
    };
  },
  created() {
    this.loadModelFieldData();
  },
  methods: {
    loadModelFieldData() {
      this.fieldList = []
      getXModelFieldData(this.modelId, this.dataType, this.dataId).then(response => {
        response.data.forEach(item => {
          let field = {};
          Object.keys(item).forEach(key => field[key] = (key === 'value' || key === 'valueObj') ? '' : item[key])
          this.fieldList.push(field);
        })
        this.$nextTick(() => {
          response.data.forEach(item => {
            let field = this.fieldList.find(f => item.fieldName == f.fieldName);
            if (field) {
              field.value = item.value
              field.valueObj = item.valueObj
            }
          })
        })
      });
    },
    getDatas() {
      let fdatas = {};
      this.fieldList.forEach(f => {
        fdatas[f.fieldName] = f.value;
      })
      return fdatas;
    }
  }
};
</script>
<style scoped>
.el-form-item {
  margin-bottom: 12px;
}
</style>