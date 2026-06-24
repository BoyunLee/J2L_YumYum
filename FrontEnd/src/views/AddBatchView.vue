<template>
  <section class="container narrow page">
    <button class="back-btn" type="button" @click="store.go('inventory')">
      <svg viewBox="0 0 24 24"><path :d="iconPath('back')" /></svg>
      목록으로 돌아가기
    </button>

    <section class="panel padded">
      <h1>상품 추가</h1>
      <p class="batch-add-description">여러 상품을 한 번에 입력하거나 OCR 결과를 여러 입력칸에 나눠 채울 수 있습니다.</p>

      <div class="quick-actions">
        <button
          type="button"
          :class="{ active: analysisType === 'OCR' }"
          :aria-pressed="analysisType === 'OCR'"
          @click="selectAnalysisType('OCR')"
        >
          <svg viewBox="0 0 24 24"><path :d="iconPath('camera')" /></svg>
          OCR로 추가
        </button>
        <button
          type="button"
          :class="{ active: analysisType === 'BARCODE' }"
          :aria-pressed="analysisType === 'BARCODE'"
          @click="selectAnalysisType('BARCODE')"
        >
          <svg viewBox="0 0 24 24"><path :d="iconPath('barcode')" /></svg>
          바코드로 추가
        </button>
      </div>

      <section v-if="analysisType" class="image-analysis-panel" aria-labelledby="image-analysis-title">
        <div class="image-analysis-head">
          <div>
            <h2 id="image-analysis-title">{{ analysisType === 'OCR' ? '상품 정보 OCR' : '바코드 이미지 분석' }}</h2>
            <p>영수증처럼 여러 품목이 보이면 현재 작성 중인 칸은 유지하고 필요한 만큼 입력칸을 더 만들어 자동으로 채웁니다.</p>
          </div>
          <button class="text-btn" type="button" @click="closeAnalysis">닫기</button>
        </div>

        <input
          ref="galleryInput"
          class="visually-hidden"
          type="file"
          accept="image/*"
          @change="handleImageSelection"
        />
        <input
          ref="cameraInput"
          class="visually-hidden"
          type="file"
          accept="image/*"
          capture="environment"
          @change="handleImageSelection"
        />

        <div class="image-source-actions">
          <button class="ghost-btn" type="button" :disabled="isAnalyzing" @click="galleryInput?.click()">
            사진 불러오기
          </button>
          <button
            class="ghost-btn"
            type="button"
            :disabled="isAnalyzing || !isMobileDevice"
            :title="isMobileDevice ? '기기의 카메라 앱을 엽니다.' : '모바일 기기에서만 사용할 수 있습니다.'"
            @click="cameraInput?.click()"
          >
            기기 카메라로 촬영
          </button>
          <button
            v-if="hasWebCamera"
            class="ghost-btn"
            type="button"
            :disabled="isAnalyzing"
            @click="startWebCamera"
          >
            웹캠으로 촬영
          </button>
        </div>
        <p class="image-help">
          <template v-if="isMobileDevice">모바일에서는 화면 카메라가 우선 열립니다. </template>
          <template v-else>현재 브라우저 환경에서는 기기 카메라 촬영을 사용할 수 없습니다. </template>
          웹캠은 HTTPS 또는 localhost 환경에서 사용할 수 있습니다.
        </p>

        <div v-if="isCameraOpen" class="camera-capture">
          <video ref="cameraVideo" autoplay muted playsinline aria-label="카메라 미리보기"></video>
          <div class="camera-actions">
            <button class="primary-btn" type="button" @click="captureWebCamera">이 사진 사용</button>
            <button class="ghost-btn" type="button" @click="stopWebCamera">카메라 닫기</button>
          </div>
        </div>

        <div v-if="selectedImage && imagePreviewUrl" class="selected-image">
          <img :src="imagePreviewUrl" alt="분석할 상품 사진 미리보기" />
          <div>
            <strong>{{ selectedImage.name }}</strong>
            <small>{{ selectedImageSize }}</small>
            <div class="selected-image-actions">
              <button class="primary-btn" type="button" :disabled="isAnalyzing" @click="analyzeSelectedImage">
                {{ isAnalyzing ? '분석 중...' : '분석하고 자동 채우기' }}
              </button>
              <button class="text-btn" type="button" :disabled="isAnalyzing" @click="clearSelectedImage">
                사진 제거
              </button>
            </div>
          </div>
        </div>

        <p v-if="analysisMessage" class="analysis-message" role="status">{{ analysisMessage }}</p>
        <p v-if="analysisError" class="form-error" role="alert">{{ analysisError }}</p>
      </section>

      <form class="inventory-form-stack" @submit.prevent="submitAdd">
        <div class="inventory-form-toolbar">
          <div>
            <h2>입력 상품</h2>
            <p>{{ addForms.length }}개의 입력칸이 준비되어 있습니다.</p>
          </div>
          <button class="ghost-btn" type="button" @click="appendForm()">상품 추가</button>
        </div>

        <section
          v-for="(entry, index) in addForms"
          :key="entry.id"
          class="panel padded inventory-form-group"
        >
          <div class="inventory-form-head">
            <strong>상품 {{ index + 1 }}</strong>
            <button
              v-if="addForms.length > 1"
              class="text-btn"
              type="button"
              @click="removeForm(index)"
            >
              삭제
            </button>
          </div>

          <div class="form-grid">
            <label class="form-field full">상품명
              <input v-model="entry.data.name" placeholder="예: 우유" />
            </label>
            <label class="form-field">카테고리
              <select v-model="entry.data.category">
                <option v-for="category in categories.filter((item) => item.value !== 'all')" :key="category.value" :value="category.value">
                  {{ category.label }}
                </option>
              </select>
            </label>
            <label class="form-field">수량
              <span class="inline-fields">
                <input v-model="entry.data.quantity" type="number" min="0.01" step="any" placeholder="0" />
                <select v-model="entry.data.unit">
                  <option v-for="unit in units" :key="unit" :value="unit">{{ unit }}</option>
                </select>
              </span>
            </label>
            <label class="form-field">유통기한
              <input v-model="entry.data.expiryDate" type="date" />
            </label>
            <label class="form-field">보관 위치
              <select v-model="entry.data.location">
                <option v-for="location in locations" :key="location" :value="location">{{ location }}</option>
              </select>
            </label>
            <label class="form-field full">메모
              <textarea v-model="entry.data.memo" rows="3" placeholder="특이사항이나 메모를 입력하세요"></textarea>
            </label>
          </div>
        </section>

        <p v-if="errorMessage" class="form-error" role="alert">{{ errorMessage }}</p>

        <div class="button-row">
          <button class="ghost-btn" type="button" @click="store.go('inventory')">취소</button>
          <button class="primary-btn" type="submit" :disabled="isSubmitting">
            {{ isSubmitting ? '등록 중...' : submitButtonLabel }}
          </button>
        </div>
      </form>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import {
  useFridgeStore,
  categories,
  locations,
  units,
  type Category,
  type ImageAnalysisType,
  type InventoryForm,
  type InventoryImageAnalysis,
  type InventoryImageAnalysisItem,
} from '../stores/fridge'
import { iconPath } from '../utils/uiHelpers'

const MAX_IMAGE_SIZE = 10 * 1024 * 1024
const locationLabels: Record<string, string> = {
  REFRIGERATOR: locations[0] ?? '냉장',
  FREEZER: locations[1] ?? '냉동',
  ROOM_TEMPERATURE: locations[2] ?? '실온',
}
const validCategories = new Set(
  categories
    .filter((item) => item.value !== 'all')
    .map((item) => item.value as Category),
)

type InventoryFormEntry = {
  id: number
  data: InventoryForm
}

const store = useFridgeStore()
let nextFormId = 1

const emptyForm = (): InventoryForm => ({
  name: '',
  category: 'dairy',
  quantity: '',
  unit: units[0] ?? '개',
  expiryDate: '',
  location: locations[0] ?? '냉장',
  memo: '',
})

const createFormEntry = (): InventoryFormEntry => ({
  id: nextFormId++,
  data: emptyForm(),
})

const addForms = ref<InventoryFormEntry[]>([createFormEntry()])
const isSubmitting = ref(false)
const errorMessage = ref('')
const analysisType = ref<ImageAnalysisType | null>(null)
const selectedImage = ref<File | null>(null)
const imagePreviewUrl = ref('')
const isAnalyzing = ref(false)
const analysisMessage = ref('')
const analysisError = ref('')
const galleryInput = ref<HTMLInputElement | null>(null)
const cameraInput = ref<HTMLInputElement | null>(null)
const cameraVideo = ref<HTMLVideoElement | null>(null)
const cameraStream = ref<MediaStream | null>(null)
const isCameraOpen = ref(false)
const navigatorWithHints = navigator as Navigator & { userAgentData?: { mobile?: boolean } }
const isMobileDevice = Boolean(
  navigatorWithHints.userAgentData?.mobile
  || /Android|iPhone|iPad|iPod|Mobile/i.test(navigator.userAgent)
  || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1),
)
const hasWebCamera = Boolean(navigator.mediaDevices?.getUserMedia)

const selectedImageSize = computed(() => {
  if (!selectedImage.value) return ''
  const megabytes = selectedImage.value.size / 1024 / 1024
  return megabytes >= 0.1 ? `${megabytes.toFixed(1)} MB` : `${Math.ceil(selectedImage.value.size / 1024)} KB`
})

const activeFormCount = computed(() => addForms.value.filter(({ data }) => hasFormContent(data)).length)
const submitButtonLabel = computed(() => {
  const count = Math.max(activeFormCount.value, 1)
  return count > 1 ? `${count}개 상품 등록` : '상품 등록'
})

function appendForm() {
  addForms.value.push(createFormEntry())
}

function removeForm(index: number) {
  if (addForms.value.length === 1) {
    addForms.value = [createFormEntry()]
    return
  }
  addForms.value.splice(index, 1)
}

function resetForms() {
  addForms.value = [createFormEntry()]
}

function hasFormContent(form: InventoryForm) {
  return Boolean(form.name.trim() || form.quantity.trim() || form.expiryDate.trim() || form.memo.trim())
}

function isCompleteForm(form: InventoryForm) {
  return Boolean(
    form.name.trim()
    && form.quantity.trim()
    && Number(form.quantity) > 0
    && form.expiryDate
    && form.unit
    && form.location
    && form.category,
  )
}

function isMeaningfulAnalysisItem(item: InventoryImageAnalysisItem) {
  return Boolean(item.name || item.rawText || item.memo || item.quantity != null || item.barcode)
}

function normalizeCategory(category: string | null) {
  if (!category) return null
  const normalized = category.toLowerCase() as Category
  return validCategories.has(normalized) ? normalized : null
}

function selectAnalysisType(type: ImageAnalysisType) {
  analysisType.value = type
  analysisMessage.value = ''
  analysisError.value = ''
}

function closeAnalysis() {
  stopWebCamera()
  clearSelectedImage()
  analysisType.value = null
}

function handleImageSelection(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (file) setSelectedImage(file)
}

function setSelectedImage(file: File) {
  analysisMessage.value = ''
  analysisError.value = ''
  if (!file.type.startsWith('image/')) {
    analysisError.value = '이미지 형식의 파일만 선택할 수 있습니다.'
    return
  }
  if (file.size > MAX_IMAGE_SIZE) {
    analysisError.value = '이미지는 10MB 이하로 선택해 주세요.'
    return
  }
  stopWebCamera()
  clearPreviewUrl()
  selectedImage.value = file
  imagePreviewUrl.value = URL.createObjectURL(file)
}

function clearPreviewUrl() {
  if (imagePreviewUrl.value) URL.revokeObjectURL(imagePreviewUrl.value)
  imagePreviewUrl.value = ''
}

function clearSelectedImage() {
  clearPreviewUrl()
  selectedImage.value = null
  analysisMessage.value = ''
  analysisError.value = ''
}

async function startWebCamera() {
  analysisMessage.value = ''
  analysisError.value = ''
  if (!navigator.mediaDevices?.getUserMedia) {
    analysisError.value = '이 브라우저에서는 웹캠 촬영을 지원하지 않습니다. 기기 카메라 버튼을 사용해 주세요.'
    return
  }
  stopWebCamera()
  try {
    cameraStream.value = await navigator.mediaDevices.getUserMedia({
      audio: false,
      video: { facingMode: { ideal: 'environment' } },
    })
    isCameraOpen.value = true
    await nextTick()
    if (cameraVideo.value) {
      cameraVideo.value.srcObject = cameraStream.value
      await cameraVideo.value.play()
    }
  } catch {
    stopWebCamera()
    analysisError.value = '카메라를 열지 못했습니다. 브라우저 권한을 확인하거나 기기 카메라 버튼을 사용해 주세요.'
  }
}

function stopWebCamera() {
  cameraStream.value?.getTracks().forEach((track) => track.stop())
  cameraStream.value = null
  if (cameraVideo.value) cameraVideo.value.srcObject = null
  isCameraOpen.value = false
}

async function captureWebCamera() {
  const video = cameraVideo.value
  if (!video?.videoWidth || !video.videoHeight) {
    analysisError.value = '카메라 화면이 아직 준비되지 않았습니다. 잠시 후 다시 시도해 주세요.'
    return
  }
  const canvas = document.createElement('canvas')
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  canvas.getContext('2d')?.drawImage(video, 0, 0)
  const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, 'image/jpeg', 0.9))
  if (!blob) {
    analysisError.value = '촬영한 사진을 처리하지 못했습니다.'
    return
  }
  setSelectedImage(new File([blob], `camera-${Date.now()}.jpg`, { type: 'image/jpeg' }))
}

function ensureFormCount(targetCount: number) {
  while (addForms.value.length < targetCount) {
    appendForm()
  }
}

function applyAnalysisItem(target: InventoryForm, item: InventoryImageAnalysisItem) {
  let appliedFields = 0

  if (item.name) {
    target.name = item.name
    appliedFields += 1
  }

  const category = normalizeCategory(item.category)
  if (category) {
    target.category = category
    appliedFields += 1
  }

  if (item.quantity != null && item.quantity > 0) {
    target.quantity = String(item.quantity)
    appliedFields += 1
  }

  if (item.unit && units.includes(item.unit)) {
    target.unit = item.unit
    appliedFields += 1
  }

  if (item.expirationDate) {
    target.expiryDate = item.expirationDate
    appliedFields += 1
  }

  const location = item.storageLocation ? locationLabels[item.storageLocation] : null
  if (location) {
    target.location = location
    appliedFields += 1
  }

  if (item.memo) {
    target.memo = item.memo
    appliedFields += 1
  }

  if (item.barcode) {
    const barcodeMemo = `바코드: ${item.barcode}`
    target.memo = [target.memo, barcodeMemo].filter(Boolean).join('\n')
    appliedFields += 1
  }

  return appliedFields
}

function applyAnalysisResults(result: InventoryImageAnalysis) {
  const items = result.items.filter(isMeaningfulAnalysisItem)
  if (!items.length) {
    return { itemCount: 0, appliedFields: 0, addedForms: 0 }
  }

  const originalLength = addForms.value.length
  let startIndex = addForms.value.findIndex(({ data }) => !hasFormContent(data))
  if (startIndex === -1) {
    startIndex = originalLength
  }

  ensureFormCount(startIndex + items.length)

  let appliedFields = 0
  items.forEach((item, offset) => {
    appliedFields += applyAnalysisItem(addForms.value[startIndex + offset].data, item)
  })

  return {
    itemCount: items.length,
    appliedFields,
    addedForms: Math.max(0, addForms.value.length - originalLength),
  }
}

async function analyzeSelectedImage() {
  if (!analysisType.value || !selectedImage.value || isAnalyzing.value) return
  isAnalyzing.value = true
  analysisMessage.value = ''
  analysisError.value = ''
  try {
    const result = await store.analyzeInventoryImage(analysisType.value, selectedImage.value)
    const summary = applyAnalysisResults(result)
    analysisMessage.value = summary.appliedFields > 0
      ? summary.addedForms > 0
        ? `${summary.itemCount}개 상품 정보를 채우고 입력칸 ${summary.addedForms}개를 추가했습니다. 등록 전에 내용을 확인해 주세요.`
        : `${summary.itemCount}개 상품 정보를 자동으로 채웠습니다. 등록 전에 내용을 확인해 주세요.`
      : result.message
  } catch (error) {
    analysisError.value = error instanceof Error ? error.message : '이미지를 분석하지 못했습니다.'
  } finally {
    isAnalyzing.value = false
  }
}

async function submitAdd() {
  if (isSubmitting.value) return

  const activeForms = addForms.value
    .map((entry) => entry.data)
    .filter(hasFormContent)

  errorMessage.value = ''

  if (!activeForms.length) {
    errorMessage.value = '등록할 상품을 한 개 이상 입력해 주세요.'
    return
  }

  const invalidFormIndex = activeForms.findIndex((form) => !isCompleteForm(form))
  if (invalidFormIndex >= 0) {
    errorMessage.value = `상품 ${invalidFormIndex + 1}의 필수 항목을 모두 입력해 주세요.`
    return
  }

  isSubmitting.value = true
  try {
    await store.addInventory(activeForms.map((form) => ({ ...form })))
    resetForms()
    clearSelectedImage()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '상품을 등록하지 못했습니다.'
  } finally {
    isSubmitting.value = false
  }
}

onBeforeUnmount(() => {
  stopWebCamera()
  clearPreviewUrl()
})
</script>
