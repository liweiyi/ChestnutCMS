<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script setup name="ChestnutCMSDoc">
const url = ref('https://www.1000mz.com/')

const goto = () => {
  window.open(url.value, '_blank')
}
</script>
