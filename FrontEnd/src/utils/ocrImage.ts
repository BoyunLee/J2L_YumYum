const DEFAULT_MAX_WIDTH = 1600
const DEFAULT_MAX_HEIGHT = 1600
const DEFAULT_MAX_BYTES = 2 * 1024 * 1024
const DEFAULT_JPEG_QUALITY = 0.82
const DEFAULT_MIN_JPEG_QUALITY = 0.62
const QUALITY_STEP = 0.08

type OptimizeImageOptions = {
  maxWidth?: number
  maxHeight?: number
  maxBytes?: number
  jpegQuality?: number
  minJpegQuality?: number
}

type LoadedImage = {
  image: HTMLImageElement
  width: number
  height: number
  revoke: () => void
}

export async function maybeOptimizeImageForOcr(
  file: File,
  options: OptimizeImageOptions = {},
) {
  try {
    const maxWidth = Math.max(1, options.maxWidth ?? DEFAULT_MAX_WIDTH)
    const maxHeight = Math.max(1, options.maxHeight ?? DEFAULT_MAX_HEIGHT)
    const maxBytes = Math.max(1, options.maxBytes ?? DEFAULT_MAX_BYTES)
    const jpegQuality = clampQuality(options.jpegQuality ?? DEFAULT_JPEG_QUALITY)
    const minJpegQuality = clampQuality(options.minJpegQuality ?? DEFAULT_MIN_JPEG_QUALITY)

    const loaded = await loadImage(file)
    try {
      const targetSize = fitWithin(loaded.width, loaded.height, maxWidth, maxHeight)
      const alreadyOptimized = file.type === 'image/jpeg'
        && file.size <= maxBytes
        && targetSize.width === loaded.width
        && targetSize.height === loaded.height

      if (alreadyOptimized) {
        return file
      }

      let quality = jpegQuality
      let blob = await renderToJpegBlob(loaded.image, targetSize.width, targetSize.height, quality)

      while (blob.size > maxBytes && quality > minJpegQuality) {
        quality = Math.max(minJpegQuality, Number((quality - QUALITY_STEP).toFixed(2)))
        blob = await renderToJpegBlob(loaded.image, targetSize.width, targetSize.height, quality)
      }

      const fileName = replaceExtension(file.name, 'jpg')
      return new File([blob], fileName, {
        type: 'image/jpeg',
        lastModified: file.lastModified,
      })
    } finally {
      loaded.revoke()
    }
  } catch {
    return file
  }
}

function fitWithin(width: number, height: number, maxWidth: number, maxHeight: number) {
  if (width <= maxWidth && height <= maxHeight) {
    return { width, height }
  }

  const scale = Math.min(maxWidth / width, maxHeight / height)
  return {
    width: Math.max(1, Math.round(width * scale)),
    height: Math.max(1, Math.round(height * scale)),
  }
}

async function loadImage(file: Blob): Promise<LoadedImage> {
  const objectUrl = URL.createObjectURL(file)
  const image = new Image()
  image.decoding = 'async'

  return new Promise((resolve, reject) => {
    image.onload = () => {
      resolve({
        image,
        width: image.naturalWidth,
        height: image.naturalHeight,
        revoke: () => URL.revokeObjectURL(objectUrl),
      })
    }
    image.onerror = () => {
      URL.revokeObjectURL(objectUrl)
      reject(new Error('이미지를 불러오지 못했습니다.'))
    }
    image.src = objectUrl
  })
}

async function renderToJpegBlob(
  source: CanvasImageSource,
  width: number,
  height: number,
  quality: number,
) {
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height

  const context = canvas.getContext('2d')
  if (!context) {
    throw new Error('이미지를 처리할 수 없습니다.')
  }

  context.fillStyle = '#ffffff'
  context.fillRect(0, 0, width, height)
  context.imageSmoothingEnabled = true
  context.imageSmoothingQuality = 'medium'
  context.drawImage(source, 0, 0, width, height)

  const blob = await new Promise<Blob | null>((resolve) => {
    canvas.toBlob(resolve, 'image/jpeg', quality)
  })
  if (!blob) {
    throw new Error('이미지를 JPEG로 변환하지 못했습니다.')
  }
  return blob
}

function replaceExtension(filename: string, extension: string) {
  if (!filename) {
    return `ocr-upload.${extension}`
  }
  const trimmed = filename.trim()
  const dotIndex = trimmed.lastIndexOf('.')
  const baseName = dotIndex > 0 ? trimmed.slice(0, dotIndex) : trimmed
  return `${baseName}.${extension}`
}

function clampQuality(quality: number) {
  return Math.max(0.05, Math.min(1, quality))
}
