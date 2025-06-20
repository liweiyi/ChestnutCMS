<template>
  <div class="errPage-container">
    <el-row style="display: flex; align-items: center;">
      <el-col :span="12">
        <h1 class="text-jumbo text-ginormous">
          {{ $t('Error.NoPermission') }}
        </h1>
        <h2 class="text-desc">{{ $t('Error.NoPermissionTip') }}</h2>
        <el-button icon="arrow-left" class="pan-back-btn" @click="goHome">
          {{ $t('Error.GoHome') }}
        </el-button>
      </el-col>
      <el-col :span="12">
        <img :src="errGif" width="313" height="428" alt="Girl has dropped her ice cream.">
      </el-col>
    </el-row>
  </div>
</template>

<script>
import errGif from '@/assets/error/403.gif'

export default {
  name: 'Page403',
  data() {
    return {
      errGif: errGif + '?' + +new Date()
    }
  },
  methods: {
    goHome() {
      this.$router.push({ path: '/' })
    }
  }
}
</script>

<style lang="scss" scoped>
  .errPage-container {
    width: 800px;
    max-width: 100%;
    margin: 100px auto;
    .pan-back-btn {
      background: #008489;
      color: #fff;
      border: none!important;
    }
    .pan-gif {
      margin: 0 auto;
      display: block;
    }
    .pan-img {
      display: block;
      margin: 0 auto;
      width: 100%;
    }
    .text-jumbo {
      font-size: 60px;
      font-weight: 700;
      color: #484848;
    }
    .text-desc {
      line-height: 1.5;
    }
  }
</style>
