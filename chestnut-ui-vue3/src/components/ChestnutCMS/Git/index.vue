<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template> 

<script setup name="ChestnutCMSGit">
const url = ref('https://gitee.com/liweiyi/ChestnutCMS')

const goto = () => {
  window.open(url.value, '_blank')
}
</script>
