<template>
  <div class="icons-container">
    <aside>
      <a href="#" target="_blank">Add and use</a>
    </aside>
    <el-tabs type="border-card">
      <el-tab-pane label="Icons">
        <div v-for="item of svgIcons" :key="item">
          <el-tooltip placement="top">
            <template #content>
              {{ generateIconCode(item) }}
            </template>
            <div class="icon-item" v-copyText="generateIconCode(item)" v-copyText:callback="clipboardSuccess">
              <svg-icon :icon-class="item" />
              <span>{{ item }}</span>
            </div>
          </el-tooltip>
        </div>
      </el-tab-pane>
      <el-tab-pane label="Element-UI Icons">
        <div v-for="item of elementIcons" :key="item">
          <el-tooltip placement="top">
            <template #content>
              {{ generateElementIconCode(item) }}
            </template>
            <div class="icon-item" v-copyText="generateElementIconCode(item)" v-copyText:callback="clipboardSuccess">
              <el-icon>
                <component :is="item" />
              </el-icon>
              <span>{{ item }}</span>
            </div>
          </el-tooltip>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="ComponentsIconsIndex">
import SvgIcon from '@/components/SvgIcon';
import svgIcons from './svg-icons'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
const elementIcons = Object.keys(ElementPlusIconsVue)

const { proxy } = getCurrentInstance()

function generateIconCode(symbol) {
  return `<svg-icon icon-class="${symbol}" />`
}
function generateElementIconCode(symbol) {
  return `<el-icon><${symbol} /></el-icon>`
}
function clipboardSuccess() {
  proxy.$modal.msgSuccess(proxy.$t('Common.CopySuccess'));
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
