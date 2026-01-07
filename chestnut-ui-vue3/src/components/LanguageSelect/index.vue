<template>
  <el-dropdown trigger="click" @command="handleSetLanguage" style="display: flex; align-items: center;">
    <el-button :text="isTextBtn" plain>
      {{ langName }}
      <el-icon v-if="showArrowDown" class="el-icon--right">
        <ArrowDown />
      </el-icon>
    </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item v-for="item of langOptions" :key="item.value" :disabled="i18n.global.locale.value===item.value" :command="item.value">
            {{ item.label }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
  </el-dropdown>
</template>

<script setup>
import { useRouter } from "vue-router"
import { i18n } from '@/i18n'
import { listLangOptions } from '@/api/system/i18nDict'
import cache from '@/plugins/cache'

const props = defineProps(['arrow', 'text'])

defineOptions({
  name: 'LanguageSelect'
})

const router = useRouter()
const showArrowDown = ref(props.arrow)
const isTextBtn = ref(props.text || true)
const langOptions = ref([])

const langName = computed(() => {
  const current = langOptions.value.find(l => l.value === i18n.global.locale.value)
  return current ? current.label : ''
})

const handleSetLanguage = (lang) => {
  i18n.global.locale.value = lang
  cache.local.set("cc.language", lang)
  router.go(0)
}

const loadLangOptions = () => {
  listLangOptions().then(response => {
    langOptions.value = response.data
  })
}

onMounted(() => {
  loadLangOptions()
})
</script>
<style lang='scss' scoped>
.right-menu-item {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.language-switch {
  display: flex;
  align-items: center;
}
</style>
