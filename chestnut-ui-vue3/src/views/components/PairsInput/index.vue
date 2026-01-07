<template>
  <div class="pairs-input-container">
    <el-button type="primary" icon="Plus" @click="handleAdd" :disabled="limit > 0 && pairs.length >= limit">
      {{ proxy.$t('Common.Add') }}
    </el-button>
    <div class="pairs-list" v-for="(pair, index) in pairs" :key="index">
      <el-row :gutter="10">
        <el-col :span="1.5">
          <el-input v-model="pair.key" @change="handleKeyChange(index)">
            <template #prefix>KEY =&nbsp;</template>
            <template v-if="keyOptions.length > 0" #suffix>
              <el-popover :ref="`keySelectRef_${index}`" placement="bottom" :width="400">
                <template #reference>
                  <el-icon><ArrowDown /></el-icon>
                </template>
                <el-link type="primary" v-for="(keyOption) in keyOptions" :key="keyOption.value" @click="handleSelectKey(pair, keyOption, index)">{{ keyOption.value }}: {{ keyOption.label }}</el-link>
              </el-popover>
            </template>
          </el-input>
        </el-col>
        <el-col :span="1.5">
          <el-input v-model="pair.value" @change="handleValueChange(index)">
            <template #prefix>VALUE =&nbsp;</template>
          </el-input>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" icon="Delete" @click="handleRemove(index)" />
        </el-col>
      </el-row>
    </div>
  </div>
</template>
<script setup name="PairsInputComponent">
const { proxy } = getCurrentInstance()

const model = defineModel();

const props = defineProps({
  limit: {
    type: Number,
    default: 0
  },
  keys: {
    type: Array,
    default: () => []
  }
})

const pairs = ref([])
const keyOptions = ref([])

watch(() => props.keys, (newVal) => {
  keyOptions.value = newVal
}, { immediate: true })

watch(model, (newVal) => {
  if (!newVal) {
    pairs.value = []
    return;
  }
  pairs.value = Object.keys(newVal)
    .map(key => ({ key: key, value: newVal[key] }))
}, { immediate: true });

function handleKeyChange(index) {
  const exist = pairs.value.find((pair, i) => i !== index && pair.key === pairs.value[index].key)
  if (exist) {
    proxy.$modal.msgWarning("Duplicate KEY")
    return;
  }
  updateModelValue()
}

function handleValueChange(index) {
  const map = pairs.value.reduce((acc, pair) => {
    acc[pair.key] = pair.value || ''
    return acc
  }, {})
  if (Object.keys(map).length != pairs.value.length) {
    proxy.$modal.msgWarning("Duplicate KEY")
    return;
  }
  updateModelValue()
}

function updateModelValue() {
  model.value = pairs.value.reduce((acc, pair) => {
    acc[pair.key] = pair.value || ''
    return acc
  }, {})
}

function handleAdd() {
  if (props.limit > 0 && pairs.value.length >= props.limit) {
    return
  }
  let exist = pairs.value.find(pair => proxy.$tools.isEmpty(pair.key))
  if (exist) {
    proxy.$modal.msgWarning("Empty KEY")
    return;
  }
  if (pairs.value.length > 1) {
    const lastIndex = pairs.value.length - 1;
    exist = pairs.value.find((pair, i) => i !== lastIndex && pair.key === pairs.value[lastIndex].key)
    if (exist) {
      proxy.$modal.msgWarning("Duplicate KEY")
      return;
    }
  }
  pairs.value.push({ key: '', value: '' })
}

function handleRemove(index) {
  pairs.value.splice(index, 1)
}

function handleSelectKey(pair, keyOption, index) {
  pair.key = keyOption.value
  const refName = `keySelectRef_${index}`
  proxy.$refs[refName][0].hide()
  handleKeyChange(index)
}
</script>
<style lang="scss" scoped>
.pairs-list:not(:first-child) {
  margin-top: 8px;
}
</style>