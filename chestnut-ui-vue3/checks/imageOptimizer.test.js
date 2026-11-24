import assert from 'node:assert/strict'
import { mkdtemp, rm } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import { after, before, test } from 'node:test'
import { fileURLToPath, pathToFileURL } from 'node:url'
import { build } from 'vite'
import { detectImageFormat, getOutputFormats } from '../src/utils/imageOptimizer/model.js'

// A single transparent 1 × 1 GIF frame.
const gif = Buffer.from('R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7', 'base64')
const firstFrameOffset = 19
const animatedGif = Buffer.concat([gif.subarray(0, -1), gif.subarray(firstFrameOffset)])

let outDir
let response

before(async () => {
  outDir = await mkdtemp(join(tmpdir(), 'chestnut-image-optimizer-'))
  await build({
    configFile: false,
    publicDir: false,
    logLevel: 'silent',
    build: {
      outDir,
      minify: false,
      lib: {
        entry: fileURLToPath(new URL('../src/utils/imageOptimizer/worker.js', import.meta.url)),
        formats: ['es'],
        fileName: () => 'worker.mjs'
      },
      rollupOptions: { output: { inlineDynamicImports: true } }
    }
  })
  globalThis.self = {
    postMessage(message) { if (message.type !== 'stage') response = message }
  }
  globalThis.ImageData = class ImageData {
    constructor(data, width, height) { Object.assign(this, { data, width, height }) }
  }
  await import(pathToFileURL(join(outDir, 'worker.mjs')))
})

after(async () => { if (outDir) await rm(outDir, { recursive: true, force: true }) })

async function run(type, format, input, options) {
  const buffer = Uint8Array.from(input).buffer
  response = undefined
  await self.onmessage({ data: { id: 1, type, format, buffer, options } })
  return response
}

test('encodes the selected format even when the converted file grows', async () => {
  const source = await run('inspect', 'png', gif)
  assert.equal(source.format, 'gif')
  assert.ok(getOutputFormats(source).includes('png'))

  const result = await run('process', 'png', gif)
  assert.equal(result.type, 'processed', result.message)
  assert.equal(result.usedOriginal, false)
  assert.equal(result.format, 'png')
  assert.equal(result.mime, 'image/png')
  assert.equal(detectImageFormat(result.buffer), 'png')
  assert.ok(result.buffer.byteLength > gif.byteLength)
})

test('keeps animated GIFs in GIF format and preserves all frames', async () => {
  const source = await run('inspect', 'png', animatedGif)
  assert.equal(source.animated, true)
  assert.deepEqual(getOutputFormats(source), ['gif'])

  const rejected = await run('process', 'png', animatedGif)
  assert.equal(rejected.code, 'UNSUPPORTED_ANIMATION')

  const result = await run('process', 'gif', animatedGif, { orientation: 2 })
  assert.equal(result.type, 'processed', result.message)
  assert.equal(result.usedOriginal, false)
  const output = await run('inspect', 'gif', new Uint8Array(result.buffer))
  assert.equal(output.frames, 2)
})
