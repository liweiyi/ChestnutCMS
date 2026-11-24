// EXIF's eight orientations also describe every rotation/reflection of a rectangle.
const MATRICES = [null,
  [1, 0, 0, 1], [-1, 0, 0, 1], [-1, 0, 0, -1], [1, 0, 0, -1],
  [0, 1, 1, 0], [0, -1, 1, 0], [0, -1, -1, 0], [0, 1, -1, 0]
]
const OPERATIONS = { rotate90: 6, flipX: 2, flipY: 4 }

function invalidDimensions() {
  return Object.assign(new Error('INVALID_DIMENSIONS'), { code: 'INVALID_DIMENSIONS' })
}

function validateOrientation(orientation) {
  if (!Number.isInteger(orientation) || orientation < 1 || orientation > 8) throw invalidDimensions()
}

function validateOperation(operation) {
  if (!Object.hasOwn(OPERATIONS, operation)) throw invalidDimensions()
}

export function getOrientedSize(width, height, orientation = 1) {
  validateOrientation(orientation)
  if (![width, height].every(value => Number.isSafeInteger(value) && value > 0)
    || width * height > 64 * 1024 * 1024) throw invalidDimensions()
  return orientation >= 5 ? { width: height, height: width } : { width, height }
}

/** Compose a new operation in displayed coordinates, after the current transform. */
export function composeOrientation(current, operation) {
  validateOrientation(current)
  validateOperation(operation)
  const [a, b, c, d] = MATRICES[OPERATIONS[operation]]
  const [e, f, g, h] = MATRICES[current]
  const result = [a * e + b * g, a * f + b * h, c * e + d * g, c * f + d * h]
  return MATRICES.findIndex(matrix => matrix && matrix.every((value, index) => value === result[index]))
}

/** Preserve the selected image region when the displayed image changes orientation. */
export function transformCrop(crop, size, operation) {
  validateOperation(operation)
  const { width: imageWidth, height: imageHeight } = getOrientedSize(size?.width, size?.height)
  if (crop === null || crop === undefined) return crop
  const { x, y, width, height } = crop
  if (![x, y, width, height].every(Number.isSafeInteger) || x < 0 || y < 0 || width < 1 || height < 1
    || x + width > imageWidth || y + height > imageHeight) throw invalidDimensions()
  if (operation === 'rotate90') return { x: imageHeight - y - height, y: x, width: height, height: width }
  if (operation === 'flipX') return { x: imageWidth - x - width, y, width, height }
  return { x, y: imageHeight - y - height, width, height }
}

export function transformPixels(image, orientation = 1) {
  const { width, height } = getOrientedSize(image?.width, image?.height, orientation)
  if (!(image.data instanceof Uint8Array || image.data instanceof Uint8ClampedArray)
    || image.data.length !== image.width * image.height * 4) throw invalidDimensions()
  if (orientation === 1) return image.data instanceof Uint8ClampedArray ? image : { data: new Uint8ClampedArray(image.data), width, height }
  const output = new Uint8ClampedArray(image.data.length)
  for (let y = 0; y < image.height; y++) for (let x = 0; x < image.width; x++) {
    let dx = x
    let dy = y
    switch (orientation) {
      case 2: dx = image.width - 1 - x; break
      case 3: dx = image.width - 1 - x; dy = image.height - 1 - y; break
      case 4: dy = image.height - 1 - y; break
      case 5: dx = y; dy = x; break
      case 6: dx = image.height - 1 - y; dy = x; break
      case 7: dx = image.height - 1 - y; dy = image.width - 1 - x; break
      case 8: dx = y; dy = image.width - 1 - x; break
    }
    const from = (y * image.width + x) * 4
    output.set(image.data.subarray(from, from + 4), (dy * width + dx) * 4)
  }
  return { data: output, width, height }
}
