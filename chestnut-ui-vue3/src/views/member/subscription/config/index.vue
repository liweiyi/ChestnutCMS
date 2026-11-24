<template>
  <div class="app-container member-subscription-config">
    <el-tabs v-if="availableTabs.length" v-model="activeTab" class="member-subscription-config__tabs">
      <el-tab-pane :label="$t('MemberSubscription.DefinitionTitle')" name="definitions" lazy>
        <member-subscription-definition-index ref="definitionsRef" />
      </el-tab-pane>
      <el-tab-pane :label="$t('MemberSubscribeService.Title')" name="services" lazy>
        <member-subscribe-service-index ref="servicesRef" />
      </el-tab-pane>
    </el-tabs>
    <el-empty v-else :description="$t('Error.NoPermission')" />
  </div>
</template>

<script setup name="MemberSubscriptionConfigIndex">
import { checkPermi } from '@/utils/permission'
import MemberSubscriptionDefinitionIndex from '../definition/index.vue'
import MemberSubscribeServiceIndex from '../service/index.vue'

const activeTab = ref('')
const definitionsRef = ref()
const servicesRef = ref()
const canViewConfig = computed(() => checkPermi(['member:subscription:config:view']))
const availableTabs = computed(() => canViewConfig.value ? ['definitions', 'services'] : [])
const visitedTabs = new Set()

watch(availableTabs, tabs => {
  if (!tabs.includes(activeTab.value)) activeTab.value = tabs[0] ?? ''
  for (const name of visitedTabs) {
    if (!tabs.includes(name)) visitedTabs.delete(name)
  }
}, { immediate: true })

watch(activeTab, async name => {
  await nextTick()
  if (name !== activeTab.value) return
  const page = name === 'definitions' ? definitionsRef.value : servicesRef.value
  if (!page) return
  if (visitedTabs.has(name)) await page.refresh()
  else visitedTabs.add(name)
}, { immediate: true })
</script>

<style scoped lang="scss">
.member-subscription-config { display: flex; flex-direction: column; height: 100%; min-height: 0; }
.member-subscription-config__tabs { display: flex; flex: 1; flex-direction: column; min-height: 0; }
.member-subscription-config__tabs :deep(.el-tabs__content) { flex: 1; min-height: 0; overflow: hidden; }
.member-subscription-config__tabs :deep(.el-tab-pane) { height: 100%; }
.member-subscription-config__tabs :deep(.member-subscription-page),
.member-subscription-config__tabs :deep(.member-subscribe-service-page) { padding: 0; }
</style>
