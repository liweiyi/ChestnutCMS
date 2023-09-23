<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'ChestnutCMSDoc',
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
