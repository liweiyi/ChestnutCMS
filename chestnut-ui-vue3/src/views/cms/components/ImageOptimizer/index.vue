<template>
  <el-dialog
    v-model="visible"
    :title="t('Title')"
    width="1200px"
    class="image-optimizer-dialog"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
    :close-on-press-escape="!saving"
    :show-close="!saving"
    @opened="initCropper"
  >
    <div class="optimizer-heading">
      <span class="source-name" :title="activeResource?.name">{{ activeResource?.name }}</span>
      <el-tag size="small" type="info">{{ t('LocalProcessing') }}</el-tag>
    </div>
    <el-alert v-if="errorText" :title="errorText" type="error" :closable="false" show-icon class="optimizer-alert">
      <el-button v-if="!busy" link type="primary" @click="retry">{{ t('Retry') }}</el-button>
    </el-alert>

    <div class="optimizer-layout" v-loading="loading" :element-loading-text="stageText">
      <section class="optimizer-left" :aria-label="t('Original')">
        <div class="canvas-toolbar">
          <el-radio-group v-model="viewMode" size="small" :disabled="!source || busy || saving">
            <el-radio-button value="original">{{ t('Original') }}</el-radio-button>
            <el-radio-button value="result" :disabled="!result">{{ t('Result') }}</el-radio-button>
            <el-radio-button value="compare" :disabled="!result || source?.animated">{{ t('Compare') }}</el-radio-button>
          </el-radio-group>
          <div class="view-actions">
            <el-button size="small" text :disabled="!source || busy || saving" @click="fitView">{{ t('Fit') }}</el-button>
            <el-button size="small" text :disabled="!source || busy || saving" @click="actualView">100%</el-button>
            <el-button size="small" text icon="ZoomOut" :aria-label="t('ZoomOut')" :disabled="!source || busy || saving" @click="zoomView(-0.1)" />
            <el-button size="small" text icon="ZoomIn" :aria-label="t('ZoomIn')" :disabled="!source || busy || saving" @click="zoomView(0.1)" />
            <el-button v-if="source?.animated" size="small" text :disabled="busy || saving || (cropEnabled && viewMode === 'original')" @click="playing = !playing">
              {{ t(playing ? 'Pause' : 'Play') }}
            </el-button>
          </div>
        </div>

        <div ref="imageViewport" class="image-viewport checkerboard">
          <el-empty v-if="!source && !loading" :description="t('Empty')" />
          <div v-show="source && viewMode === 'original'" class="original-canvas">
            <img ref="cropperImage" :src="originalPreviewUrl || undefined" :alt="t('Original')" @load="initCropper" />
            <GifPreview v-if="source?.animated && !cropEnabled" class="animated-original" :buffer="source.buffer" :orientation="orientation" :playing="playing && viewMode === 'original' && !busy" :label="t('Original')" :style="gifImageStyle" @error="errorText = t('ProcessFailed')" />
          </div>
          <div v-if="result && viewMode !== 'original'" class="result-canvas" :style="{ transform: `scale(${resultZoom})` }">
            <template v-if="viewMode === 'compare'">
              <img :src="referencePreviewUrl" :alt="t('Original')" />
              <img :src="resultPreviewUrl" :alt="t('Result')" :style="{ clipPath: `inset(0 ${100 - comparePosition}% 0 0)` }" />
              <div class="comparison-divider" :style="{ left: `${comparePosition}%` }" />
              <span class="comparison-label result-label">{{ t('Result') }}</span>
              <span class="comparison-label original-label">{{ t('Original') }}</span>
            </template>
            <GifPreview v-else-if="source.animated" :buffer="result.buffer" :playing="playing && !busy" :label="t('Result')" @error="errorText = t('ProcessFailed')" />
            <img v-else :src="resultPreviewUrl" :alt="t('Result')" />
          </div>
        </div>
        <el-slider v-if="viewMode === 'compare' && result" v-model="comparePosition" :min="0" :max="100" :aria-label="t('Compare')" class="comparison-slider" />

        <div v-if="source" class="image-statistics" aria-live="polite">
          <div><span>{{ t('Original') }}</span><strong>{{ source.width }} × {{ source.height }}</strong><span>{{ formatLabel(source.format) }} · {{ formatSize(source.buffer.byteLength) }}</span></div>
          <div v-if="result"><span>{{ t('Result') }}</span><strong>{{ result.width }} × {{ result.height }}</strong><span>{{ formatLabel(result.format) }} · {{ formatSize(result.blob.size) }}</span></div>
          <div v-if="result" :class="{ 'size-increased': !result.usedOriginal && result.blob.size >= source.buffer.byteLength }">
            <span>{{ t('Reduction') }}</span><strong>{{ reduction }}%</strong><span>{{ t('Elapsed', { seconds: (result.elapsed / 1000).toFixed(2) }) }}</span>
          </div>
        </div>
        <el-alert v-if="retainedOriginal" :title="t('OriginalRetained')" type="info" :closable="false" show-icon />
        <el-alert v-else-if="validResult && result.format === source.format && result.blob.size >= source.buffer.byteLength" :title="t('NoReduction')" type="info" :closable="false" show-icon />
        <div class="processing-status" aria-live="polite">
          <span v-if="busy">{{ stageText }}<template v-if="stageProgress"> · {{ stageProgress }}</template></span>
          <span v-else-if="dirty && result" class="pending-result">{{ t('StaleResult') }}</span>
          <span v-else-if="result">{{ t('Ready') }}</span>
        </div>
      </section>

      <aside class="optimizer-settings">
        <!-- InputNumber 2.11 initializes aria-disabled only at mount. Recreate
             controls on lock changes so they remain accessible after a task. -->
        <el-form v-if="source && !loading" :key="busy || saving ? 'locked' : 'editable'" label-position="top" size="small" :disabled="busy || saving">
          <section class="settings-section">
            <div class="section-title">
              <h3>{{ t('Crop') }}<el-tooltip :content="t('CropHint')" placement="top"><el-icon class="parameter-help" tabindex="0" :aria-label="t('CropHint')"><InfoFilled /></el-icon></el-tooltip></h3>
              <el-switch v-model="cropEnabled" :aria-label="t('EnableCrop')" />
            </div>
            <template v-if="cropEnabled">
              <div class="dimension-fields">
                <el-form-item :label="`${t('Width')} (px)`"><el-input-number v-model="crop.width" :min="1" :max="workingSize.width" :value-on-clear="1" :precision="0" controls-position="right" @change="setCropDimensions('width')" /></el-form-item>
                <el-form-item :label="`${t('Height')} (px)`"><el-input-number v-model="crop.height" :min="1" :max="workingSize.height" :value-on-clear="1" :precision="0" controls-position="right" @change="setCropDimensions('height')" /></el-form-item>
              </div>
              <div class="dimension-fields">
                <el-form-item :label="t('Ratio')">
                  <el-select v-model="cropRatio" @change="changeRatio">
                    <el-option :label="t('FreeRatio')" value="free" /><el-option :label="t('OriginalRatio')" value="original" />
                    <el-option v-for="ratio in ratios" :key="ratio" :label="ratio" :value="ratio" />
                  </el-select>
                </el-form-item>
                <el-form-item :label="t('PresetSize')">
                  <el-select v-model="cropPreset" clearable :placeholder="t('NoPreset')" @change="applyPreset">
                    <el-option v-for="preset in presets" :key="preset.label" :label="preset.label" :value="preset.label" :disabled="preset.width > workingSize.width || preset.height > workingSize.height" />
                  </el-select>
                </el-form-item>
              </div>
            </template>
            <div class="transform-actions">
              <el-button :disabled="!source || busy || saving" @click="applyImageTransform('rotate90')">{{ t('Rotate90') }}</el-button>
              <el-button :disabled="!source || busy || saving" @click="applyImageTransform('flipX')">{{ t('FlipHorizontal') }}</el-button>
              <el-button :disabled="!source || busy || saving" @click="applyImageTransform('flipY')">{{ t('FlipVertical') }}</el-button>
            </div>
          </section>
          <section class="settings-section">
            <div class="section-title">
              <h3>{{ t('OutputSize') }}<el-tooltip :content="t('PercentHint')" placement="top"><el-icon class="parameter-help" tabindex="0" :aria-label="t('PercentHint')"><InfoFilled /></el-icon></el-tooltip></h3>
              <el-radio-group v-model="sizeMode" size="small" class="size-mode">
                <el-radio-button value="percentage">{{ t('Percentage') }}</el-radio-button><el-radio-button value="pixels">{{ t('Pixels') }}</el-radio-button>
              </el-radio-group>
            </div>
            <el-form-item v-if="sizeMode === 'percentage'" :label="`${t('Percentage')} (%)`" label-position="left" label-width="84px">
              <el-input-number v-model="percentage" :min="0.01" :max="100" :value-on-clear="100" :precision="2" controls-position="right" />
            </el-form-item>
            <div v-else class="dimension-fields">
              <el-form-item :label="`${t('Width')} (px)`"><el-input-number :model-value="outputSize.width" :min="1" :max="baseSize.width" :precision="0" controls-position="right" @change="resizeFrom('width', $event)" /></el-form-item>
              <el-form-item :label="`${t('Height')} (px)`"><el-input-number :model-value="outputSize.height" :min="1" :max="baseSize.height" :precision="0" controls-position="right" @change="resizeFrom('height', $event)" /></el-form-item>
            </div>
            <p class="setting-hint output-dimensions">{{ outputSize.width }} × {{ outputSize.height }} px</p>
          </section>
          <section class="settings-section">
            <div class="section-title"><h3>{{ t('Compression') }}</h3></div>
            <el-form-item :label="t('OutputFormat')">
              <el-radio-group v-model="outputFormat" :aria-label="t('OutputFormat')" class="format-options">
                <el-radio-button v-for="format in outputFormats" :key="format" :value="format">{{ formatLabel(format) }}</el-radio-button>
              </el-radio-group>
              <p v-if="source.animated" class="setting-hint">{{ t('AnimatedFormatHint') }}</p>
              <p v-else-if="outputFormat === 'jpeg' && source.format !== 'jpeg'" class="setting-hint">{{ t('JpegTransparencyHint') }}</p>
            </el-form-item>
            <div class="dimension-fields">
              <el-form-item :label="t('Quality')">
                <el-select v-model="compression.qualityPreset">
                  <el-option v-for="item in qualityPresets" :key="item.value" :value="item.value" :label="t(item.label)" :disabled="item.disabled" :title="item.disabled ? t('LosslessUnavailable') : undefined" />
                </el-select>
              </el-form-item>
              <el-form-item :label="t('Strategy')">
                <template #label><span class="parameter-label">{{ t('Strategy') }}<el-tooltip :content="t(effortAvailable ? 'EffortHint' : 'EffortUnavailable')" placement="top"><el-icon class="parameter-help" tabindex="0" :aria-label="t(effortAvailable ? 'EffortHint' : 'EffortUnavailable')"><InfoFilled /></el-icon></el-tooltip></span></template>
                <el-select v-model="compression.effort" :disabled="!effortAvailable">
                  <el-option v-for="item in EFFORT_PRESETS" :key="item.value" :value="item.value" :label="t(item.label)" />
                </el-select>
              </el-form-item>
            </div>
            <p class="setting-hint compression-hint">{{ qualityHint }}</p>
            <div class="compression-actions"><el-button link type="primary" @click="resetCompression">{{ t('Recommended') }}</el-button></div>
          </section>
        </el-form>
        <el-alert v-if="source" :title="canStoreFormat ? t('ServerNotice') : saveUnavailableText" type="info" :closable="false" show-icon />
      </aside>
    </div>
    <template #footer>
      <div class="optimizer-footer">
        <div>
          <el-button :disabled="!source || busy || saving" @click="resetSettings">{{ t('Reset') }}</el-button>
          <el-button v-if="busy" :disabled="saving" @click="cancelTask">{{ t('Cancel') }}</el-button>
          <el-button v-else type="primary" :disabled="!source || saving" @click="processImage">{{ t('Process') }}</el-button>
        </div>
        <div class="save-actions">
          <el-button :disabled="!validResult || saving" icon="Download" @click="downloadResult">{{ t(retainedOriginal ? 'DownloadOriginal' : 'Download') }}</el-button>
          <el-tooltip :disabled="canStoreFormat && !retainedOriginal" :content="saveUnavailableText"><span><el-button :disabled="!canSaveResult || saving" :loading="savingMode === 'copy'" @click="saveResult('copy')">{{ t('SaveCopy') }}</el-button></span></el-tooltip>
          <el-tooltip :disabled="canReplaceFormat && !retainedOriginal" :content="replaceUnavailableText"><span><el-button type="primary" :disabled="!canReplaceResult || saving" :loading="savingMode === 'replace'" @click="saveResult('replace')">{{ t('Replace') }}</el-button></span></el-tooltip>
          <el-button :disabled="saving" @click="visible = false">{{ t('Close') }}</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="CMSImageOptimizer">
import { computed, nextTick, onBeforeUnmount, reactive, ref, shallowRef, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { saveAs } from 'file-saver'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'
import GifPreview from './GifPreview.vue'
import { addResource, downloadResourceFile, getResourceDetail, getResourceTypes } from '@/api/contentcore/resource'
import { getConfigKey } from '@/api/system/config'
import cms from '@/plugins/cms'
import useSiteStore from '@/store/modules/site'
import { getOutputFormats } from '@/utils/imageOptimizer/model.js'
import { getOptimizedFilename } from '@/utils/imageOptimizer/resource.js'
import { QUALITY_PRESETS, EFFORT_PRESETS, getDefaultCompression, resolveCompressionOptions, supportsEffort } from '@/utils/imageOptimizer/compression.js'
import { ImageOptimizerClient } from '@/utils/imageOptimizer/client.js'
import { composeOrientation, getOrientedSize, transformCrop, transformPixels } from '@/utils/imageOptimizer/transforms.js'

const props = defineProps({
  modelValue: Boolean,
  resource: { type: Object, default: null },
  siteId: { type: [String, Number], default: null },
  acceptSize: { type: Number, default: null },
  resourceTypes: { type: Array, default: null }
})
const emit = defineEmits(['update:modelValue', 'saved'])
const { t: translate } = useI18n()
const t = (key, values) => translate(`CMS.ImageOptimizer.${key}`, values || {})
const visible = computed({ get: () => props.modelValue, set: value => { if (!saving.value) emit('update:modelValue', value) } })
const siteStore = useSiteStore()
const source = shallowRef(null)
const activeResource = shallowRef(null)
const result = shallowRef(null)
const loading = ref(false)
const processing = ref(false)
const transforming = ref(false)
const savingMode = ref('')
const confirming = ref(false)
const saving = computed(() => !!savingMode.value || confirming.value)
const busy = computed(() => loading.value || processing.value || transforming.value)
const dirty = ref(false)
const errorText = ref('')
const stage = ref('download')
const stageProgress = ref('')
const stageText = computed(() => t(`Stage${stage.value.charAt(0).toUpperCase()}${stage.value.slice(1)}`))
const viewMode = ref('original')
const playing = ref(true)
const comparePosition = ref(50)
const resultZoom = ref(1)
const cropEnabled = ref(false)
const orientation = ref(1)
const crop = reactive({ x: 0, y: 0, width: 1, height: 1 })
const cropRatio = ref('free')
const cropPreset = ref('')
const sizeMode = ref('percentage')
const percentage = ref(100)
const outputFormat = ref('')
const compression = reactive(getDefaultCompression())
const uploadConstraints = reactive({ acceptSize: 0, resourceTypes: [] })
const acceptSize = computed(() => props.acceptSize ?? uploadConstraints.acceptSize)
const resourceTypes = computed(() => props.resourceTypes ?? uploadConstraints.resourceTypes)
const cropperImage = ref(null)
const imageViewport = ref(null)
const gifImageStyle = ref({})
const originalPreviewUrl = ref('')
const basePreviewUrl = ref('')
const sourcePreview = shallowRef(null)
const resultPreviewUrl = ref('')
const referencePreviewUrl = ref('')
const ratios = ['1:1', '1:2', '2:1', '2:3', '3:2', '3:4', '4:3', '16:9', '9:16']
const presets = ['90x90', '120x120', '300x300', '360x120', '1200x400', '360x200', '1280x720', '360x240', '1200x800'].map(label => {
  const [width, height] = label.split('x').map(Number)
  return { label, width, height }
})
const outputFormats = computed(() => getOutputFormats(source.value))
const losslessAvailable = computed(() => !['jpeg', 'gif'].includes(outputFormat.value))
const qualityPresets = computed(() => QUALITY_PRESETS.map(item => ({ ...item,
  disabled: item.value === 'lossless' && !losslessAvailable.value
})))
const effortAvailable = computed(() => supportsEffort(outputFormat.value))
const resolvedCompression = computed(() => outputFormat.value ? resolveCompressionOptions(outputFormat.value, compression) : null)
const qualityHint = computed(() => {
  if (compression.qualityPreset === 'lossless') return t('LosslessQualityHint')
  if (['png', 'gif'].includes(outputFormat.value)) return t('PaletteQualityHint', {
    colors: resolvedCompression.value.colors
  })
  return t('QualityHint')
})
const workingSize = computed(() => getOrientedSize(source.value?.width || 1, source.value?.height || 1, orientation.value))
const previewSize = computed(() => getOrientedSize(source.value?.previewWidth || 1, source.value?.previewHeight || 1, orientation.value))
const baseSize = computed(() => cropEnabled.value ? { width: crop.width || 1, height: crop.height || 1 } : workingSize.value)
const outputSize = computed(() => ({
  width: Math.max(1, Math.min(baseSize.value.width, Math.round(baseSize.value.width * (percentage.value || 100) / 100))),
  height: Math.max(1, Math.min(baseSize.value.height, Math.round(baseSize.value.height * (percentage.value || 100) / 100)))
}))
const validResult = computed(() => !!result.value && result.value.format === outputFormat.value && !dirty.value && !busy.value)
const retainedOriginal = computed(() => validResult.value && result.value.usedOriginal)
const downloadOnlyFormat = computed(() => ['avif', 'jxl'].includes(outputFormat.value))
const canStoreFormat = computed(() => {
  if (!outputFormat.value || downloadOnlyFormat.value) return false
  const extensions = outputFormat.value === 'jpeg' ? ['jpg', 'jpeg'] : [outputFormat.value]
  return !resourceTypes.value.length || resourceTypes.value.some(type => (type.accepts || '').toLowerCase().split(',').some(ext => extensions.includes(ext.trim().replace(/^\./, ''))))
})
const canReplaceFormat = computed(() => {
  if (!canStoreFormat.value || !activeResource.value?.path) return false
  // Published image URLs include the original storage suffix.
  const extension = outputFileName(false, outputFormat.value).split('.').pop()
  return activeResource.value.path.split(/[?#]/)[0].endsWith(`.${extension}`)
})
const canSaveResult = computed(() => validResult.value && !retainedOriginal.value && canStoreFormat.value)
const canReplaceResult = computed(() => canSaveResult.value && canReplaceFormat.value)
const saveUnavailableText = computed(() => t(retainedOriginal.value
  ? 'OriginalRetainedHint'
  : downloadOnlyFormat.value ? 'SaveUnavailable' : 'SaveFormatUnavailable'))
const replaceUnavailableText = computed(() => canStoreFormat.value && !retainedOriginal.value && !canReplaceFormat.value
  ? t('ReplaceFormatUnavailable') : saveUnavailableText.value)
const reduction = computed(() => {
  if (!source.value || !result.value || result.value.usedOriginal) return '0.0'
  const change = Number(((result.value.blob.size - source.value.buffer.byteLength) / source.value.buffer.byteLength * 100).toFixed(1))
  return `${change > 0 ? '+' : ''}${change.toFixed(1)}`
})
let cropper = null
let controller = null
let generation = 0
let sourceSite = null
let sessionSite = null
let constraintsTask = null
let pendingSaved = null
const objectUrls = new Set()
const worker = new ImageOptimizerClient((value, progress) => {
  stage.value = value
  stageProgress.value = progress == null ? '' : typeof progress === 'number' ? `${Math.round(progress * 100)}%` : String(progress)
})

function formatLabel(format) { return format === 'webp' ? 'WebP' : format.toUpperCase() }
function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1048576).toFixed(2)} MB`
}
function objectUrl(blob) {
  const url = URL.createObjectURL(blob)
  objectUrls.add(url)
  return url
}
function revoke(url) { if (url) { URL.revokeObjectURL(url); objectUrls.delete(url) } }
async function previewUrl(preview) {
  const canvas = document.createElement('canvas')
  canvas.width = preview.width
  canvas.height = preview.height
  canvas.getContext('2d').putImageData(new ImageData(new Uint8ClampedArray(preview.data), preview.width, preview.height), 0, 0)
  const blob = await new Promise((resolve, reject) => canvas.toBlob(value => value ? resolve(value) : reject(new Error('Preview failed')), 'image/png'))
  canvas.width = canvas.height = 0
  return objectUrl(blob)
}
function releaseResults() {
  for (const item of [resultPreviewUrl, referencePreviewUrl]) { revoke(item.value); item.value = '' }
  result.value = null
}
function cleanup() {
  generation++
  pendingSaved = null
  controller?.abort()
  worker.cancel()
  cropper?.destroy()
  cropper = null
  for (const url of objectUrls) URL.revokeObjectURL(url)
  objectUrls.clear()
  originalPreviewUrl.value = basePreviewUrl.value = resultPreviewUrl.value = referencePreviewUrl.value = ''
  sourcePreview.value = null
  source.value = result.value = null
  outputFormat.value = ''
  orientation.value = 1
  loading.value = processing.value = transforming.value = false
}
function errorMessage(error) {
  return t(({ UNSUPPORTED_ANIMATION: 'UnsupportedAnimation', INVALID_IMAGE: 'InvalidImage', INVALID_DIMENSIONS: 'InvalidDimensions' })[error.code] || 'ProcessFailed')
}
function resolveSiteId(resource) {
  return [resource?.siteId, props.siteId, props.resource?.siteId, cms.getCurrentSite()]
    .map(value => value == null ? '' : String(value).trim()).find(value => /^[1-9]\d*$/.test(value))
}
function resolveSaveSiteId(mode) {
  if (mode === 'replace') return sourceSite
  // 另存属于当前编辑页面；原文件的读取与覆盖始终使用其所属站点。
  return [props.siteId, sessionSite, sourceSite]
    .map(value => value == null ? '' : String(value).trim()).find(value => /^[1-9]\d*$/.test(value))
}
function loadUploadConstraints() {
  if (constraintsTask) return constraintsTask
  constraintsTask = Promise.allSettled([
    props.acceptSize == null ? getConfigKey('ResourceUploadAcceptSize') : Promise.resolve(null),
    props.resourceTypes == null ? getResourceTypes() : Promise.resolve(null)
  ]).then(([size, types]) => {
    if (size.status === 'fulfilled' && size.value) uploadConstraints.acceptSize = Number(size.value.data) || 0
    if (types.status === 'fulfilled' && Array.isArray(types.value?.data)) uploadConstraints.resourceTypes = types.value.data
  })
  return constraintsTask
}
async function loadResource(resource, { keepSession = false } = {}) {
  const expectedSession = keepSession ? sessionSite : cms.getCurrentSite()
  if (keepSession && String(expectedSession) !== String(cms.getCurrentSite())) {
    cleanup()
    emit('update:modelValue', false)
    return null
  }
  const savedToNotify = keepSession ? pendingSaved : null
  cleanup()
  pendingSaved = savedToNotify
  const current = generation
  activeResource.value = resource
  errorText.value = ''
  loading.value = true
  stage.value = 'download'
  sessionSite = expectedSession
  sourceSite = resolveSiteId(resource)
  loadUploadConstraints()
  controller = new AbortController()
  try {
    const detail = await getResourceDetail(resource.resourceId, { siteId: sourceSite, signal: controller.signal })
    if (current !== generation) return
    if (!visible.value || String(sessionSite) !== String(cms.getCurrentSite())) {
      cleanup()
      emit('update:modelValue', false)
      return null
    }
    activeResource.value = { ...resource, ...detail.data,
      internalUrl: detail.data?.internalUrl || resource.internalUrl,
      src: detail.data?.src || resource.src || '' }
    if (pendingSaved && String(pendingSaved.resource.resourceId) === String(resource.resourceId)) {
      if (!activeResource.value.src) throw new Error('Stored image preview is unavailable')
      const saved = pendingSaved
      pendingSaved = null
      ElMessage.success(t('SaveSuccess'))
      emit('saved', { ...saved.resource, ...activeResource.value,
        internalUrl: saved.resource.internalUrl || activeResource.value.internalUrl,
        width: activeResource.value.width || saved.dimensions.width,
        height: activeResource.value.height || saved.dimensions.height }, saved.context)
    }
    const external = /^https?:\/\//i.test(activeResource.value.path || '')
    let blob
    if (external) {
      const response = await fetch(activeResource.value.path, { mode: 'cors', credentials: 'omit', signal: controller.signal })
      if (!response.ok) throw new Error('Image download failed')
      blob = await response.blob()
    } else {
      blob = await downloadResourceFile(resource.resourceId, { siteId: sourceSite, signal: controller.signal })
    }
    if (current !== generation) return
    const buffer = await blob.arrayBuffer()
    if (current !== generation) return
    const info = await worker.run('inspect', buffer)
    if (current !== generation) return
    const url = await previewUrl(info.preview)
    if (current !== generation) { revoke(url); return }
    const { preview, ...metadata } = info
    source.value = { ...metadata, buffer, previewWidth: preview.width, previewHeight: preview.height }
    sourcePreview.value = preview
    originalPreviewUrl.value = basePreviewUrl.value = url
    resetSettings()
    await nextTick()
    initCropper()
  } catch (error) {
    if (current !== generation || error.name === 'AbortError' || error.code === 'ERR_CANCELED') return
    errorText.value = source.value ? errorMessage(error) : (error.code ? errorMessage(error) : t('ReadFailed'))
  } finally {
    if (current === generation) loading.value = false
  }
  return current === generation ? activeResource.value : null
}
function resetCompression() { if (source.value) Object.assign(compression, getDefaultCompression()) }
function resetSettings() {
  releaseResults()
  outputFormat.value = source.value?.format || ''
  if (orientation.value !== 1) {
    cropper?.destroy()
    cropper = null
    if (originalPreviewUrl.value !== basePreviewUrl.value) revoke(originalPreviewUrl.value)
    originalPreviewUrl.value = basePreviewUrl.value
    nextTick(initCropper)
  }
  orientation.value = 1
  dirty.value = false
  cropEnabled.value = false
  cropRatio.value = 'free'
  cropPreset.value = ''
  Object.assign(crop, { x: 0, y: 0, width: source.value?.width || 1, height: source.value?.height || 1 })
  percentage.value = 100
  sizeMode.value = 'percentage'
  viewMode.value = 'original'
  playing.value = true
  resultZoom.value = 1
  errorText.value = ''
  resetCompression()
  cropper?.clear()
  fitView()
}
function initCropper() {
  if (cropper || !source.value || !cropperImage.value?.complete || !cropperImage.value?.naturalWidth || !cropperImage.value.parentElement?.clientWidth) return
  const initialCrop = { ...crop }
  let initializing = true
  let instance
  const syncThisView = () => { if (cropper === instance) syncAnimatedView() }
  instance = new Cropper(cropperImage.value, {
    viewMode: 1, autoCrop: cropEnabled.value, autoCropArea: 1, dragMode: 'move', background: false,
    aspectRatio: cropperRatio(),
    checkCrossOrigin: false, checkOrientation: false, toggleDragModeOnDblclick: false,
    ready() {
      if (cropper !== instance) return
      if (cropEnabled.value) { cropper?.crop(); Object.assign(crop, initialCrop); setCropData() }
      initializing = false
      syncAnimatedView()
      if (busy.value || saving.value) cropper?.disable()
    },
    zoom() { requestAnimationFrame(syncThisView) },
    cropmove() { requestAnimationFrame(syncThisView) },
    cropend() { syncThisView() },
    crop(event) {
      if (cropper !== instance || initializing || !cropEnabled.value || !source.value) return
      const scaleX = workingSize.value.width / previewSize.value.width
      const scaleY = workingSize.value.height / previewSize.value.height
      const x = Math.max(0, Math.min(workingSize.value.width - 1, Math.round(event.detail.x * scaleX)))
      const y = Math.max(0, Math.min(workingSize.value.height - 1, Math.round(event.detail.y * scaleY)))
      Object.assign(crop, {
        x, y,
        width: Math.max(1, Math.min(workingSize.value.width - x, Math.round(event.detail.width * scaleX))),
        height: Math.max(1, Math.min(workingSize.value.height - y, Math.round(event.detail.height * scaleY)))
      })
    }
  })
  cropper = instance
}
function syncAnimatedView() {
  if (!cropper || !source.value?.animated) return
  const data = cropper.getCanvasData()
  gifImageStyle.value = { width: `${data.width}px`, height: `${data.height}px`, left: `${data.left}px`, top: `${data.top}px` }
}
function setCropData() {
  if (!cropper || !source.value) return
  const scaleX = previewSize.value.width / workingSize.value.width
  const scaleY = previewSize.value.height / workingSize.value.height
  cropper.setData({ x: crop.x * scaleX, y: crop.y * scaleY, width: crop.width * scaleX, height: crop.height * scaleY })
}
function selectedRatio() {
  if (cropRatio.value === 'free') return NaN
  if (cropRatio.value === 'original') return workingSize.value.width / workingSize.value.height
  const [width, height] = cropRatio.value.split(':').map(Number)
  return width / height
}
function cropperRatio() {
  return selectedRatio() * (previewSize.value.width / workingSize.value.width) / (previewSize.value.height / workingSize.value.height)
}
async function applyImageTransform(operation) {
  if (!source.value || !sourcePreview.value || busy.value || saving.value) return
  const current = generation
  const nextOrientation = composeOrientation(orientation.value, operation)
  const nextCrop = transformCrop({ ...crop }, workingSize.value, operation)
  transforming.value = true
  stage.value = 'preview'
  stageProgress.value = ''
  errorText.value = ''
  let url = basePreviewUrl.value
  try {
    if (nextOrientation !== 1) {
      const pixels = transformPixels({ ...sourcePreview.value, data: new Uint8ClampedArray(sourcePreview.value.data) }, nextOrientation)
      url = await previewUrl({ ...pixels, data: pixels.data.buffer })
    }
    if (current !== generation) { if (url !== basePreviewUrl.value) revoke(url); return }
    cropper?.destroy()
    cropper = null
    const previousUrl = originalPreviewUrl.value
    orientation.value = nextOrientation
    Object.assign(crop, nextCrop)
    if (operation === 'rotate90' && !['free', 'original'].includes(cropRatio.value)) cropRatio.value = cropRatio.value.split(':').reverse().join(':')
    cropPreset.value = ''
    viewMode.value = 'original'
    playing.value = false
    originalPreviewUrl.value = url
    if (previousUrl !== basePreviewUrl.value) revoke(previousUrl)
    await nextTick()
    if (current === generation) initCropper()
  } catch (error) {
    if (current === generation) errorText.value = errorMessage(error)
    if (url !== basePreviewUrl.value && url !== originalPreviewUrl.value) revoke(url)
  } finally { if (current === generation) transforming.value = false }
}
function setCropDimensions(axis) {
  if (!source.value) return
  let width = Number(crop.width) || 1
  let height = Number(crop.height) || 1
  const ratio = selectedRatio()
  if (Number.isFinite(ratio)) { if (axis === 'width') height = Math.round(width / ratio); else width = Math.round(height * ratio) }
  const shrink = Math.min(1, workingSize.value.width / width, workingSize.value.height / height)
  crop.width = Math.max(1, Math.round(width * shrink))
  crop.height = Math.max(1, Math.round(height * shrink))
  crop.x = Math.max(0, Math.min(crop.x, workingSize.value.width - crop.width))
  crop.y = Math.max(0, Math.min(crop.y, workingSize.value.height - crop.height))
  cropPreset.value = ''
  setCropData()
}
function changeRatio() { cropPreset.value = ''; cropper?.setAspectRatio(cropperRatio()) }
function applyPreset(label) {
  const preset = presets.find(item => item.label === label)
  if (!preset || !source.value || preset.width > workingSize.value.width || preset.height > workingSize.value.height) return
  cropRatio.value = 'free'
  cropper?.setAspectRatio(NaN)
  crop.width = preset.width
  crop.height = preset.height
  crop.x = Math.max(0, Math.min(crop.x, workingSize.value.width - crop.width))
  crop.y = Math.max(0, Math.min(crop.y, workingSize.value.height - crop.height))
  setCropData()
}
function resizeFrom(axis, value) {
  if (Number.isFinite(value) && value > 0) percentage.value = Math.min(100, value / baseSize.value[axis] * 100)
}
function fitView() {
  resultZoom.value = 1
  if (!cropper) return
  const data = cropper.getData()
  const box = cropper.getContainerData()
  const image = cropper.getImageData()
  cropper.zoomTo(Math.min(box.width / image.naturalWidth, box.height / image.naturalHeight))
  if (cropEnabled.value) cropper.setData(data)
  syncAnimatedView()
}
function actualView() {
  if (viewMode.value === 'original') { cropper?.zoomTo(workingSize.value.width / previewSize.value.width); syncAnimatedView() }
  else resultZoom.value = 1 / Math.min(imageViewport.value.clientWidth / result.value.width, imageViewport.value.clientHeight / result.value.height)
}
function zoomView(delta) { if (viewMode.value === 'original') { cropper?.zoom(delta); syncAnimatedView() } else resultZoom.value = Math.max(0.001, Math.min(32, resultZoom.value * (1 + delta))) }
function cancelTask() {
  const cancelledDownload = loading.value
  generation++
  controller?.abort()
  worker.cancel()
  loading.value = processing.value = transforming.value = false
  stageProgress.value = ''
  if (cancelledDownload) errorText.value = t('ReadFailed')
}
function retry() {
  if (pendingSaved || !source.value) loadResource(activeResource.value || props.resource, { keepSession: true })
  else processImage()
}
async function processImage() {
  if (!source.value || busy.value) return
  const current = ++generation
  processing.value = true
  stage.value = 'decode'
  stageProgress.value = ''
  errorText.value = ''
  const options = { ...compression, orientation: orientation.value, crop: cropEnabled.value ? { ...crop } : null, ...outputSize.value }
  try {
    const processed = await worker.run('process', source.value.buffer, outputFormat.value, options)
    if (current !== generation) return
    const preview = await previewUrl(processed.preview)
    const reference = await previewUrl(processed.reference)
    if (current !== generation) { revoke(preview); revoke(reference); return }
    releaseResults()
    const blob = new Blob([processed.buffer], { type: processed.mime })
    result.value = { buffer: processed.buffer, width: processed.width, height: processed.height,
      mime: processed.mime, format: processed.format, elapsed: processed.elapsed, usedOriginal: !!processed.usedOriginal, blob }
    resultPreviewUrl.value = preview
    referencePreviewUrl.value = reference
    dirty.value = false
    viewMode.value = source.value.animated ? 'result' : 'compare'
    comparePosition.value = 50
    resultZoom.value = 1
  } catch (error) {
    if (current === generation && error.name !== 'AbortError') errorText.value = errorMessage(error)
  } finally { if (current === generation) processing.value = false }
}
function outputFileName(copy, format = result.value.format) {
  return getOptimizedFilename(activeResource.value.fileName || activeResource.value.name, format, copy ? t('FileSuffix') : '')
}
function downloadResult() {
  if (!validResult.value) return
  saveAs(result.value.blob, outputFileName(!result.value.usedOriginal))
}
async function saveResult(mode) {
  const canSave = mode === 'replace' ? canReplaceResult : canSaveResult
  if (!canSave.value || saving.value) return
  if (String(sessionSite) !== String(cms.getCurrentSite())) { visible.value = false; return }
  const current = generation
  const confirmedResult = result.value
  const confirmedResourceId = activeResource.value.resourceId
  const saveSiteId = resolveSaveSiteId(mode)
  await loadUploadConstraints()
  if (current !== generation || result.value !== confirmedResult || !visible.value
    || !canSave.value || saving.value || String(sessionSite) !== String(cms.getCurrentSite())) return
  if (acceptSize.value > 0 && result.value.blob.size > acceptSize.value) { ElMessage.warning(t('SizeLimit', { size: formatSize(acceptSize.value) })); return }
  if (mode === 'replace') {
    confirming.value = true
    try { await ElMessageBox.confirm(t('ReplaceConfirm'), t('Replace'), { type: 'warning', confirmButtonText: t('Replace'), cancelButtonText: t('Cancel') }) } catch { return }
    finally { confirming.value = false }
    if (current !== generation || result.value !== confirmedResult || activeResource.value?.resourceId !== confirmedResourceId
      || saving.value || !visible.value || !canSave.value || String(sessionSite) !== String(cms.getCurrentSite())) return
  }
  savingMode.value = mode
  try {
    const filename = outputFileName(mode === 'copy')
    let name = activeResource.value.name
    if (!name || name === activeResource.value.fileName) name = filename
    else if (mode === 'copy') name += t('CopySuffix')
    const data = new FormData()
    data.append('file', new File([result.value.blob], filename, { type: result.value.mime }))
    data.append('name', name)
    data.append('remark', activeResource.value.remark || '')
    if (mode === 'replace') data.append('resourceId', activeResource.value.resourceId)
    const dimensions = { width: result.value.width, height: result.value.height }
    const response = await addResource(data, { siteId: saveSiteId })
    if (current !== generation) return
    // Read back the stored file before notifying forms. Upload responses include
    // an internal URL; details provide the actual preview URL and fresh metadata.
    const saved = response.data
    pendingSaved = { resource: saved, dimensions, context: { mode, previousResourceId: confirmedResourceId } }
    await loadResource({ ...saved, siteId: saved.siteId || saveSiteId,
      src: saved.src || (mode === 'replace' ? activeResource.value.src : '') }, { keepSession: true })
  } catch (error) {
    if (current === generation) errorText.value = error.message || t('ProcessFailed')
  } finally { savingMode.value = '' }
}

watch(cropEnabled, async enabled => {
  if (!source.value) return
  viewMode.value = 'original'
  await nextTick()
  initCropper()
  if (enabled) {
    const selection = { ...crop }
    playing.value = false
    cropper?.crop()
    Object.assign(crop, selection)
    setCropData()
  } else cropper?.clear()
})
watch([busy, saving], ([isBusy, isSaving]) => { if (isBusy || isSaving) cropper?.disable(); else cropper?.enable() }, { flush: 'sync' })
watch(outputFormat, () => {
  if (!losslessAvailable.value && compression.qualityPreset === 'lossless') compression.qualityPreset = getDefaultCompression().qualityPreset
}, { flush: 'sync' })
watch([() => ({ ...crop }), cropEnabled, orientation, percentage, outputFormat, () => ({ ...compression })], () => { if (result.value) dirty.value = true }, { flush: 'sync' })
watch(() => [props.modelValue, props.resource?.resourceId, props.resource?.siteId, props.siteId], ([open]) => { if (open && props.resource?.resourceId) loadResource(props.resource); else cleanup() }, { immediate: true })
watch(() => siteStore.siteId, () => { if (visible.value && String(sessionSite) !== String(cms.getCurrentSite())) { cleanup(); emit('update:modelValue', false) } })
onBeforeUnmount(cleanup)
</script>

<style scoped lang="scss">
.optimizer-heading { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; .source-name { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; } }
.optimizer-alert { margin-bottom: 12px; }
.optimizer-layout { display: grid; grid-template-columns: minmax(0, 1fr) 310px; gap: 20px; min-height: 480px; }
.optimizer-left { min-width: 0; display: flex; flex-direction: column; gap: 12px; }
.canvas-toolbar { display: flex; gap: 8px; align-items: center; justify-content: space-between; flex-wrap: wrap; }
.view-actions { display: flex; align-items: center; gap: 2px; :deep(.el-button + .el-button) { margin-left: 0; } }
.checkerboard { background-color: #fff; background-image: linear-gradient(45deg, #e9e9e9 25%, transparent 25%), linear-gradient(-45deg, #e9e9e9 25%, transparent 25%), linear-gradient(45deg, transparent 75%, #e9e9e9 75%), linear-gradient(-45deg, transparent 75%, #e9e9e9 75%); background-size: 20px 20px; background-position: 0 0, 0 10px, 10px -10px, -10px 0; }
.image-viewport { position: relative; height: clamp(300px, 48vh, 540px); border: 1px solid var(--el-border-color); border-radius: 6px; overflow: auto; display: flex; align-items: center; justify-content: center; }
.original-canvas { position: absolute; inset: 0; > img { display: block; max-width: 100%; } .animated-original { position: absolute; max-width: none; pointer-events: none; } }
.result-canvas { position: relative; width: 100%; height: 100%; transform-origin: center; > img, > canvas { position: absolute; width: 100%; height: 100%; object-fit: contain; } }
.comparison-divider { position: absolute; top: 0; bottom: 0; width: 2px; background: var(--el-color-primary); pointer-events: none; }
.comparison-label { position: absolute; top: 10px; padding: 3px 7px; border-radius: 4px; background: #0009; color: #fff; font-size: 12px; }
.result-label { left: 10px; }.original-label { right: 10px; }
.comparison-slider { width: calc(100% - 20px); margin: -4px 10px; }
.image-statistics { display: flex; justify-content: space-around; flex-wrap: wrap; gap: 12px; padding: 12px; border: 1px solid var(--el-border-color-lighter); border-radius: 6px; > div { display: flex; flex-direction: column; gap: 4px; font-size: 12px; color: var(--el-text-color-secondary); strong { color: var(--el-text-color-primary); font-size: 14px; } } .size-increased strong { color: var(--el-color-warning); } }
.processing-status { min-height: 20px; color: var(--el-text-color-secondary); font-size: 13px; }.pending-result { color: var(--el-color-warning); }
.optimizer-settings { max-height: min(68vh, 740px); overflow-y: auto; padding: 0 4px 0 0; background: transparent; margin: 0; border-radius: 0; font: inherit; line-height: 1.5; color: var(--el-text-color-regular); }
.settings-section {
  padding: 0 0 10px; margin: 0 0 12px; border-bottom: 1px solid var(--el-border-color-lighter);
  &:last-child { padding-bottom: 0; margin-bottom: 0; border-bottom: 0; }
  h3 { display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; margin: 0; }
  :deep(.el-select), :deep(.el-input-number) { width: 100%; min-width: 0; }
  :deep(.el-form-item) { min-width: 0; margin-bottom: 8px; }
  :deep(.el-form-item__label) { height: auto; margin-bottom: 4px; font-size: 12px; font-weight: 400; line-height: 18px; }
  :deep(.el-form-item__content) { min-width: 0; }
  :deep(.el-form-item--label-left .el-form-item__label) { margin-bottom: 0; line-height: 24px; }
}
.section-title { display: flex; justify-content: space-between; align-items: center; gap: 8px; min-height: 24px; margin-bottom: 8px; }
.parameter-label { display: inline-flex; align-items: center; gap: 4px; }
.parameter-help { flex: 0 0 auto; font-size: 13px; color: var(--el-text-color-placeholder); cursor: help; }
.dimension-fields { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; min-width: 0; :deep(.el-input__wrapper) { padding-left: 6px; } }
.transform-actions { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 6px; margin-top: 4px; :deep(.el-button) { min-width: 0; margin: 0; padding-left: 4px; padding-right: 4px; } }
.size-mode { flex: 0 0 auto; }.setting-hint { font-size: 12px; line-height: 1.5; color: var(--el-text-color-secondary); margin: 0; }
.output-dimensions { text-align: right; font-variant-numeric: tabular-nums; }
.format-options { display: flex; width: 100%; :deep(.el-radio-button) { flex: 1; } :deep(.el-radio-button__inner) { width: 100%; padding-inline: 7px; } }
.compression-hint { margin: 0; }
.compression-actions { display: flex; justify-content: flex-end; margin-top: 4px; }
.optimizer-settings > :deep(.el-alert) { margin-top: 12px; padding: 8px 10px; }
.optimizer-footer { display: flex; justify-content: space-between; gap: 12px; flex-wrap: wrap; > div { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; } :deep(.el-button + .el-button) { margin-left: 0; } }
@media (max-width: 800px) { .optimizer-layout { grid-template-columns: 1fr; }.optimizer-settings { max-height: none; }.image-viewport { height: 340px; }.optimizer-footer { justify-content: flex-end; }.canvas-toolbar { gap: 10px; } }
</style>
<style>
.image-optimizer-dialog { max-width: calc(100vw - 24px); margin-top: 4vh; margin-bottom: 4vh; }
.image-optimizer-dialog .el-dialog__body { padding-top: 8px; }
</style>
