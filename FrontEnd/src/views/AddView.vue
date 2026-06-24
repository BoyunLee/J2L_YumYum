<template>
  <section class="container narrow page">
    <button class="back-btn" type="button" @click="store.go('inventory')">
      <svg viewBox="0 0 24 24"><path :d="iconPath('back')" /></svg>
      목록으로 돌아가기
    </button>

    <section class="panel padded">
      <h1>재고 추가</h1>
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
          바코드 스캔
        </button>
      </div>

      <section v-if="analysisType" class="image-analysis-panel" aria-labelledby="image-analysis-title">
        <div class="image-analysis-head">
          <div>
            <h2 id="image-analysis-title">{{ analysisType === 'OCR' ? '상품 정보 OCR' : '바코드 이미지 분석' }}</h2>
            <p>사진을 선택하거나 카메라로 촬영한 뒤 분석하면 아래 입력란을 자동으로 채웁니다.</p>
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
          <template v-if="isMobileDevice">모바일에서는 후면 카메라가 우선 열립니다. </template>
          <template v-else>현재 컴퓨터 환경에서는 기기 카메라 촬영을 사용할 수 없습니다. </template>
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
                {{ isAnalyzing ? '분석 중…' : '분석하고 자동 채우기' }}
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

      <form class="form-grid" @submit.prevent="submitAdd">
        <label class="form-field full">상품명 <input v-model="addForm.name" required placeholder="예: 우유" /></label>
        <label class="form-field">카테고리
          <select v-model="addForm.category" required>
            <option v-for="category in categories.filter((item) => item.value !== 'all')" :key="category.value" :value="category.value">{{ category.label }}</option>
          </select>
        </label>
        <label class="form-field">수량
          <span class="inline-fields">
            <input v-model="addForm.quantity" required type="number" min="0.01" step="any" placeholder="0" />
            <select v-model="addForm.unit"><option v-for="unit in units" :key="unit" :value="unit">{{ unit }}</option></select>
          </span>
        </label>
        <label class="form-field">유통기한 <input v-model="addForm.expiryDate" required type="date" /></label>
        <label class="form-field">보관 위치
          <select v-model="addForm.location" required><option v-for="location in locations" :key="location" :value="location">{{ location }}</option></select>
        </label>
        <label class="form-field full">메모 <textarea v-model="addForm.memo" rows="3" placeholder="특이사항이나 메모를 입력하세요" /></label>
        <p v-if="errorMessage" class="form-error full" role="alert">{{ errorMessage }}</p>
        <div class="button-row full">
          <button class="ghost-btn" type="button" @click="store.go('inventory')">취소</button>
          <button class="primary-btn" type="submit" :disabled="isSubmitting">{{ isSubmitting ? '추가 중…' : '추가하기' }}</button>
        </div>
      </form>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, reactive, ref } from 'vue'
import {
  useFridgeStore,
  categories,
  locations,
  units,
  type Category,
  type ImageAnalysisType,
  type InventoryForm,
  type InventoryImageAnalysis,
} from '../stores/fridge'
import { iconPath } from '../utils/uiHelpers'

const MAX_IMAGE_SIZE = 10 * 1024 * 1024
const locationLabels: Record<string, string> = {
  REFRIGERATOR: '냉장실',
  FREEZER: '냉동실',
  ROOM_TEMPERATURE: '실온',
}
const validCategories: Category[] = ['dairy', 'meat', 'vegetable', 'fruit', 'etc']

const store = useFridgeStore()

const emptyForm = (): InventoryForm => ({
  name: '',
  category: 'dairy',
  quantity: '',
  unit: '개',
  expiryDate: '',
  location: '냉장실',
  memo: '',
})

const addForm = reactive<InventoryForm>(emptyForm())
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
    analysisError.value = '이 브라우저는 웹 카메라 촬영을 지원하지 않습니다. 기기 카메라 버튼을 이용해 주세요.'
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
    analysisError.value = '카메라를 열지 못했습니다. 브라우저 권한을 확인하거나 기기 카메라 버튼을 이용해 주세요.'
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
    analysisError.value = '카메라 화면이 준비되지 않았습니다. 잠시 후 다시 시도해 주세요.'
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

function applyAnalysisResult(result: InventoryImageAnalysis) {
  let appliedFields = 0
  if (result.name) {
    addForm.name = result.name
    appliedFields += 1
  }
  const category = result.category?.toLowerCase() as Category | undefined
  if (category && validCategories.includes(category)) {
    addForm.category = category
    appliedFields += 1
  }
  if (result.quantity != null && result.quantity > 0) {
    addForm.quantity = String(result.quantity)
    appliedFields += 1
  }
  if (result.unit && units.includes(result.unit)) {
    addForm.unit = result.unit
    appliedFields += 1
  }
  if (result.expirationDate) {
    addForm.expiryDate = result.expirationDate
    appliedFields += 1
  }
  const location = result.storageLocation ? locationLabels[result.storageLocation] : null
  if (location) {
    addForm.location = location
    appliedFields += 1
  }
  if (result.memo) {
    addForm.memo = result.memo
    appliedFields += 1
  }
  if (result.barcode) {
    const barcodeMemo = `바코드: ${result.barcode}`
    if (!addForm.memo.includes(barcodeMemo)) addForm.memo = [addForm.memo, barcodeMemo].filter(Boolean).join('\n')
    appliedFields += 1
  }
  return appliedFields
}

async function analyzeSelectedImage() {
  if (!analysisType.value || !selectedImage.value || isAnalyzing.value) return
  isAnalyzing.value = true
  analysisMessage.value = ''
  analysisError.value = ''
  try {
    const result = await store.analyzeInventoryImage(analysisType.value, selectedImage.value)
    const appliedFields = applyAnalysisResult(result)
    analysisMessage.value = appliedFields > 0
      ? `${appliedFields}개 항목을 자동 입력했습니다. 저장하기 전에 내용을 확인해 주세요.`
      : result.message
  } catch (error) {
    analysisError.value = error instanceof Error ? error.message : '이미지를 분석하지 못했습니다.'
  } finally {
    isAnalyzing.value = false
  }
}

async function submitAdd() {
  if (isSubmitting.value) return
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    await store.addInventory({ ...addForm })
    Object.assign(addForm, emptyForm())
    clearSelectedImage()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '재고를 추가하지 못했습니다.'
  } finally {
    isSubmitting.value = false
  }
}

onBeforeUnmount(() => {
  stopWebCamera()
  clearPreviewUrl()
})
</script>
