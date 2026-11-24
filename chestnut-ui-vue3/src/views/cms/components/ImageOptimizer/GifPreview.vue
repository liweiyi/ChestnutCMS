<template><canvas ref="canvas" role="img" :aria-label="label" /></template>

<script setup>
import { onBeforeUnmount, ref, watch } from 'vue'
import { createGifFrameReader } from '@/utils/imageOptimizer/gif.js'
import { transformPixels } from '@/utils/imageOptimizer/transforms.js'

const props = defineProps({ buffer: { type: ArrayBuffer, required: true }, playing: Boolean, label: String, orientation: { type: Number, default: 1 } })
const emit = defineEmits(['error'])
const canvas = ref(null)
let reader
let frame
let raf = 0
let lastTime = 0
let remaining = 0
let repeats = 0
let ended = false

function renderFrame() {
  // Render synchronously: reader.next() reuses its buffer only on the next
  // animation tick. Reorientation therefore keeps the actual paused frame.
  const image = transformPixels(frame, props.orientation)
  canvas.value.width = image.width
  canvas.value.height = image.height
  canvas.value.getContext('2d').putImageData(new ImageData(image.data, image.width, image.height), 0, 0)
}
function draw() {
  frame = reader.next()
  renderFrame()
  // Match normal browser animation playback without spinning on zero-delay GIFs.
  // The encoded result still retains the unmodified delay from the source.
  remaining = Math.max(20, frame.delay)
}
function stop() {
  cancelAnimationFrame(raf)
  raf = 0
  if (lastTime) remaining = Math.max(0, remaining - (performance.now() - lastTime))
  lastTime = 0
}
function tick(now) {
  try {
    remaining -= now - lastTime
    lastTime = now
    if (remaining <= 0) {
      if (frame.index === reader.frameCount - 1) {
        if (reader.repeat === -1 || (reader.repeat > 0 && repeats >= reader.repeat)) {
          ended = true
          raf = 0
          lastTime = 0
          return
        }
        repeats++
      }
      draw()
    }
    raf = requestAnimationFrame(tick)
  } catch (error) { fail(error) }
}
function fail(error) {
  stop()
  reader = frame = null
  emit('error', error)
}
function play() {
  if (!reader || raf || !props.playing) return
  try {
    if (ended) { reader.reset(); repeats = 0; ended = false; draw() }
    lastTime = performance.now()
    raf = requestAnimationFrame(tick)
  } catch (error) { fail(error) }
}
watch([() => props.buffer, canvas], () => {
  stop()
  reader = frame = null
  if (!canvas.value || !props.buffer) return
  try {
    reader = createGifFrameReader(props.buffer)
    repeats = 0
    ended = false
    draw()
    play()
  } catch (error) { fail(error) }
}, { flush: 'post', immediate: true })
watch(() => props.playing, value => { if (value) play(); else stop() })
watch(() => props.orientation, () => {
  if (!canvas.value || !frame) return
  try { renderFrame() } catch (error) { fail(error) }
}, { flush: 'post' })
onBeforeUnmount(() => { stop(); reader = frame = null })
</script>
