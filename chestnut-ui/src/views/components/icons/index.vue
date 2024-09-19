<template>
  <div class="icons-container">
    <aside>
      <a href="#" target="_blank">Add and use
      </a>
    </aside>
    <el-tabs type="border-card">
      <el-tab-pane label="Icons">
        <div v-for="item of svgIcons" :key="item">
          <el-tooltip placement="top">
            <div slot="content">
              {{ generateIconCode(item) }}
            </div>
            <div class="icon-item" v-clipboard:copy="generateIconCode(item)" v-clipboard:success="clipboardSuccess">
              <svg-icon :icon-class="item" class-name="disabled" />
              <span>{{ item }}</span>
            </div>
          </el-tooltip>
        </div>
      </el-tab-pane>
      <el-tab-pane label="Element-UI Icons">
        <div v-for="item of elementIcons" :key="item">
          <el-tooltip placement="top">
            <div slot="content">
              {{ generateElementIconCode(item) }}
            </div>
            <div class="icon-item" >
              <i :class="'el-icon-' + item" v-clipboard:copy="generateElementIconCode(item)" v-clipboard:success="clipboardSuccess" />
              <span v-clipboard:copy="'el-icon-' + item" v-clipboard:success="clipboardSuccess">{{ item }}</span>
            </div>
          </el-tooltip>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import svgIcons from './svg-icons'
import elementIcons from './element-icons'

export default {
  name: 'ComponentsIconsIndex',
  data() {
    return {
      svgIcons,
      elementIcons
    }
  },
  methods: {
    generateIconCode(symbol) {
      return `<svg-icon icon-class="${symbol}" />`
    },
    generateElementIconCode(symbol) {
      return `<i class="el-icon-${symbol}" />`
    },
    clipboardSuccess() {
      this.$modal.msgSuccess(this.$t('Common.CopySuccess'));
    }
  }
}
</script>

<style lang="scss" scoped>
.icons-container {
  margin: 10px 20px 0;
  overflow: hidden;

  .icon-item {
    margin: 20px;
    height: 85px;
    text-align: center;
    width: 100px;
    float: left;
    font-size: 30px;
    color: #24292e;
    cursor: pointer;
  }

  span {
    display: block;
    font-size: 16px;
    margin-top: 10px;
  }

  .disabled {
    pointer-events: none;
  }
}
</style>
