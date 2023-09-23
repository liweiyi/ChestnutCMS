<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'ChestnutCMSGit',
  data() {
    return {
      url: 'https://gitee.com/liweiyi/ChestnutCMS'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
