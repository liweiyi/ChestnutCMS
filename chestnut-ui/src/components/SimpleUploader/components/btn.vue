<template>
  <div>
    <label class="el-button el-button--primary el-button--small" ref="btn" v-show="support">
      <slot></slot>
    </label>
  </div>
</template>

<script>
  import { uploaderMixin, supportMixin } from '../common/mixins'

  const COMPONENT_NAME = 'uploader-btn'

  export default {
    name: COMPONENT_NAME,
    mixins: [uploaderMixin, supportMixin],
    props: {
      directory: {
        type: Boolean,
        default: false
      },
      single: {
        type: Boolean,
        default: false
      },
      attrs: {
        type: Object,
        default () {
          return {}
        }
      }
    },
    watch: {
      attrs: {
        handler(newV) {
          this.inputAttrs = JSON.parse(JSON.stringify(newV));
          this.assignBrowseBtn()
        },
        deep: true // 开启深度监听
      },
      single(newV) {
        this.singleSelect = newV;
        this.assignBrowseBtn()
      }
    },
    mounted () {
      this.singleSelect = this.single;
      this.assignBrowseBtn()
    },
    data () {
      return {
        inputAttrs: {},
        singleSelect: true,
      }
    },
    methods: {
      assignBrowseBtn() {
        this.$refs.btn.querySelectorAll('input').forEach(input => input.remove());
        this.$nextTick(() => {
          this.uploader.uploader.assignBrowse(this.$refs.btn, this.directory, this.singleSelect, this.inputAttrs)
        })
      }
    }
  }
</script>
