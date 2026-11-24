import test from 'node:test'
import assert from 'node:assert/strict'
import { getOptimizedFilename } from '../src/utils/imageOptimizer/resource.js'

test('optimized image filenames use the encoded format instead of the source extension', () => {
  assert.equal(getOptimizedFilename('photo.jpg', 'png', '-optimized'), 'photo-optimized.png')
  assert.equal(getOptimizedFilename('photo.png', 'jpeg'), 'photo.jpg')
  assert.equal(getOptimizedFilename('photo.png', 'avif'), 'photo.avif')
})

test('optimized image filenames preserve JPEG extension aliases and support extensionless files', () => {
  assert.equal(getOptimizedFilename('photo.jpg', 'jpeg'), 'photo.jpg')
  assert.equal(getOptimizedFilename('photo.jpeg', 'jpeg', '-optimized'), 'photo-optimized.jpeg')
  assert.equal(getOptimizedFilename('photo.JPG', 'jpeg'), 'photo.JPG')
  assert.equal(getOptimizedFilename('photo', 'webp'), 'photo.webp')
  assert.equal(getOptimizedFilename('', 'jxl'), 'image.jxl')
})
