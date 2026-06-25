<template>
  <section class="container page">
    <div class="page-head">
      <div>
        <h1>식단 기록</h1>
        <p>먹은 음식을 빠르게 저장하고 하루 영양 흐름을 한눈에 확인해 보세요.</p>
      </div>
      <button class="ghost-btn" type="button" :disabled="isLoadingMealLogs" @click="reloadMealData">
        새로고침
      </button>
    </div>

    <p v-if="mealLogError" class="form-error" role="alert">{{ mealLogError }}</p>

    <section class="filter-panel meal-date-filter">
      <label class="form-field">
        <span>기록 날짜</span>
        <div class="field-with-icon">
          <input v-model="selectedDate" type="date" />
        </div>
      </label>

      <article class="meal-day-card">
        <strong>{{ selectedDateLabel }}</strong>
        <p>하루 식단 기록과 영양 합계를 함께 확인할 수 있어요.</p>
        <div class="meal-day-meta">
          <span>{{ summary.mealCount }}번 기록</span>
          <span>{{ summary.itemCount }}개 음식</span>
        </div>
      </article>
    </section>

    <div class="meal-page-grid">
      <section class="panel meal-entry-panel">
        <div class="panel-head">
          <div>
            <h2>식단 입력</h2>
            <p>기본 제공량 기준으로 자동완성하고, 섭취량에 맞춰 영양값을 계산합니다.</p>
          </div>
        </div>

        <div class="padded meal-entry-body">
          <div class="meal-type-tabs">
            <button
              v-for="option in mealTypeOptions"
              :key="option.value"
              type="button"
              :class="{ active: selectedMealType === option.value }"
              @click="selectedMealType = option.value"
            >
              {{ option.label }}
            </button>
          </div>

          <label class="form-field full">
            <span>음식 검색</span>
            <div class="meal-search">
              <div class="field-with-icon">
                <svg viewBox="0 0 24 24"><path :d="iconPath('search')" /></svg>
                <input
                  v-model="searchQuery"
                  type="search"
                  placeholder="예: 김밥, 된장국, 닭가슴살"
                  autocomplete="off"
                />
              </div>

              <div v-if="isSearchingFoods || suggestions.length > 0" class="meal-suggestion-list">
                <p v-if="isSearchingFoods" class="meal-suggestion-empty">음식 정보를 찾는 중이에요.</p>
                <button
                  v-for="food in suggestions"
                  :key="food.foodCode"
                  type="button"
                  class="meal-suggestion-item"
                  @click="selectFood(food)"
                >
                  <span>
                    <strong>{{ food.name }}</strong>
                    <small>{{ food.category ?? '기타' }} · {{ food.baseAmount ?? '1회 제공량' }}</small>
                  </span>
                  <b>{{ formatNumber(food.calories) }} kcal</b>
                </button>
              </div>
            </div>

            <small v-if="searchQuery.trim() && suggestions.length === 0 && !selectedFood && !isSearchingFoods" class="meal-helper-text">
              검색 결과가 없으면 음식 이름을 조금 다르게 입력해 보세요.
            </small>
            <small v-if="foodSearchError" class="form-error">{{ foodSearchError }}</small>
          </label>

          <article v-if="selectedFood" class="meal-selected-food">
            <div class="meal-selected-head">
              <div>
                <strong>{{ selectedFood.name }}</strong>
                <small>{{ selectedFood.category ?? '기타' }} · {{ selectedFood.baseAmount ?? '1회 제공량' }}</small>
              </div>
              <span class="badge recipe">{{ formatNumber(selectedFood.calories) }} kcal</span>
            </div>

            <div class="meal-selected-macros">
              <span>탄수 {{ formatNumber(selectedFood.carbohydrate) }}g</span>
              <span>단백질 {{ formatNumber(selectedFood.protein) }}g</span>
              <span>지방 {{ formatNumber(selectedFood.fat) }}g</span>
            </div>
          </article>

          <div class="meal-quantity-row">
            <label class="form-field meal-quantity-field">
              <span>섭취량 배수</span>
              <span
                class="meal-quantity-help"
                tabindex="0"
                aria-label="기록 팁: 음식 1개는 DB에 저장된 기본 제공량 1회 기준입니다. 배수를 2로 넣으면 영양값도 2배로 반영돼요."
              >
                <span class="meal-info-icon" aria-hidden="true">i</span>
                <span class="meal-info-bubble" role="tooltip">
                  음식 1개는 DB에 저장된 기본 제공량 1회 기준입니다. 배수를 2로 넣으면 영양값도 2배로 반영돼요.
                </span>
              </span>
              <input v-model="quantity" type="number" min="0.1" step="0.5" />
            </label>
            <button class="primary-btn" type="button" @click="addPendingItem">목록에 추가</button>
          </div>

          <div class="meal-pending-summary">
            <strong>저장 예정 {{ pendingItems.length }}개</strong>
            <span>{{ formatNumber(totalPendingCalories) }} kcal</span>
          </div>

          <div class="meal-pending-list">
            <article v-for="item in pendingItems" :key="item.foodCode" class="meal-pending-item">
              <div>
                <strong>{{ item.name }}</strong>
                <small>{{ item.baseAmount ?? '1회 제공량' }} x {{ formatNumber(item.quantity) }}</small>
              </div>
              <div class="meal-pending-actions">
                <b>{{ formatNumber(item.calories * item.quantity) }} kcal</b>
                <button class="text-btn" type="button" @click="removePendingItem(item.foodCode)">삭제</button>
              </div>
            </article>
            <p v-if="pendingItems.length === 0" class="empty-text meal-empty-inline">추가한 음식이 아직 없어요.</p>
          </div>

          <label class="form-field full">
            <span>메모</span>
            <textarea v-model="memo" placeholder="식사 상황이나 특이사항을 남겨둘 수 있어요." />
          </label>

          <p v-if="localError" class="form-error" role="alert">{{ localError }}</p>

          <button class="primary-btn full-width" type="button" :disabled="isSavingMealLog" @click="submitMealLog">
            {{ isSavingMealLog ? '저장 중...' : '식단 기록 저장' }}
          </button>
        </div>
      </section>

      <section class="panel meal-log-panel">
        <div class="panel-head">
          <div>
            <h2>{{ selectedDateLabel }} 기록</h2>
            <p>저장한 식단을 다시 확인하거나 바로 삭제할 수 있어요.</p>
          </div>
        </div>

        <div v-if="isLoadingMealLogs" class="recommendation-loading" role="status">
          <span class="callback-spinner" aria-hidden="true"></span>
          <p>식단 기록을 불러오는 중이에요.</p>
        </div>

        <div v-else class="list-stack meal-log-list">
          <article v-for="log in mealLogs" :key="log.id" class="meal-log-card">
            <div class="meal-log-top">
              <div class="meal-log-headline">
                <span class="badge" :class="mealTypeBadgeClass(log.mealType)">{{ mealTypeLabel(log.mealType) }}</span>
                <strong>{{ formatNumber(log.totalCalories) }} kcal</strong>
              </div>
              <button class="ghost-btn compact" type="button" @click="removeMealLog(log.id)">삭제</button>
            </div>

            <p v-if="log.memo" class="meal-log-memo">{{ log.memo }}</p>

            <div class="meal-log-macros">
              <span>탄수 {{ formatNumber(log.totalCarbohydrate) }}g</span>
              <span>단백질 {{ formatNumber(log.totalProtein) }}g</span>
              <span>지방 {{ formatNumber(log.totalFat) }}g</span>
            </div>

            <div class="meal-log-items">
              <div v-for="item in log.items" :key="item.id" class="meal-log-item-row">
                <span>
                  <strong>{{ item.name }}</strong>
                  <small>{{ item.baseAmount ?? '1회 제공량' }} x {{ formatNumber(item.quantity) }}</small>
                </span>
                <span class="right-text">
                  <small>{{ formatNumber(item.calories) }} kcal</small>
                  <small>탄 {{ formatNumber(item.carbohydrate) }} / 단 {{ formatNumber(item.protein) }} / 지 {{ formatNumber(item.fat) }}</small>
                </span>
              </div>
            </div>
          </article>

          <p v-if="mealLogs.length === 0" class="empty-text">선택한 날짜에는 아직 저장한 식단이 없어요.</p>
        </div>
      </section>

      <aside class="side-stack meal-side-stack">
        <section class="panel padded">
          <div class="meal-summary-head">
            <div>
              <h2>하루 영양 요약</h2>
              <p>현재 날짜 기준 누적 영양 합계예요.</p>
            </div>
            <span class="badge inventory">{{ summary.mealCount }}회</span>
          </div>

          <div class="meal-summary-grid">
            <article class="meal-summary-card calories">
              <div class="meal-summary-row">
                <span>칼로리</span>
                <strong>{{ formatNumber(summary.totalCalories) }} kcal</strong>
              </div>
              <div class="meal-progress">
                <span :style="progressStyle(summary.totalCalories, summary.goalCalories)"></span>
              </div>
              <small>목표 {{ formatNumber(summary.goalCalories) }} kcal</small>
            </article>

            <article class="meal-summary-card protein">
              <div class="meal-summary-row">
                <span>단백질</span>
                <strong>{{ formatNumber(summary.totalProtein) }} g</strong>
              </div>
              <div class="meal-progress">
                <span :style="progressStyle(summary.totalProtein, summary.goalProtein)"></span>
              </div>
              <small>목표 {{ formatNumber(summary.goalProtein) }} g</small>
            </article>

            <article class="meal-summary-card carbohydrate">
              <div class="meal-summary-row">
                <span>탄수화물</span>
                <strong>{{ formatNumber(summary.totalCarbohydrate) }} g</strong>
              </div>
              <div class="meal-progress">
                <span :style="progressStyle(summary.totalCarbohydrate, summary.goalCarbohydrate)"></span>
              </div>
              <small>목표 {{ formatNumber(summary.goalCarbohydrate) }} g</small>
            </article>

            <article class="meal-summary-card fat">
              <div class="meal-summary-row">
                <span>지방</span>
                <strong>{{ formatNumber(summary.totalFat) }} g</strong>
              </div>
              <div class="meal-progress">
                <span :style="progressStyle(summary.totalFat, summary.goalFat)"></span>
              </div>
              <small>목표 {{ formatNumber(summary.goalFat) }} g</small>
            </article>
          </div>
        </section>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { type MealFoodSuggestion, type MealSummary, type MealType, useFridgeStore } from '../stores/fridge'
import { iconPath } from '../utils/uiHelpers'

interface PendingMealItem extends MealFoodSuggestion {
  quantity: number
}

const mealTypeOptions: Array<{ value: MealType; label: string }> = [
  { value: 'BREAKFAST', label: '아침' },
  { value: 'LUNCH', label: '점심' },
  { value: 'DINNER', label: '저녁' },
  { value: 'SNACK', label: '간식' },
]

const mealTypeLabels: Record<MealType, string> = {
  BREAKFAST: '아침',
  LUNCH: '점심',
  DINNER: '저녁',
  SNACK: '간식',
}

const store = useFridgeStore()
const { isLoadingMealLogs, isSavingMealLog, mealLogError, mealLogs, mealSummary } = storeToRefs(store)

const selectedDate = ref(formatLocalDate(new Date()))
const selectedMealType = ref<MealType>('BREAKFAST')
const memo = ref('')
const searchQuery = ref('')
const quantity = ref('1')
const selectedFood = ref<MealFoodSuggestion | null>(null)
const suggestions = ref<MealFoodSuggestion[]>([])
const pendingItems = ref<PendingMealItem[]>([])
const isSearchingFoods = ref(false)
const foodSearchError = ref('')
const localError = ref('')

let searchTimer: number | null = null
let latestSearchToken = 0

const summary = computed<MealSummary>(() => mealSummary.value ?? {
  date: selectedDate.value,
  mealCount: 0,
  itemCount: 0,
  totalCalories: 0,
  totalProtein: 0,
  totalCarbohydrate: 0,
  totalFat: 0,
  goalCalories: 2000,
  goalProtein: 100,
  goalCarbohydrate: 250,
  goalFat: 65,
})

const selectedDateLabel = computed(() => formatDisplayDate(selectedDate.value))
const totalPendingCalories = computed(() =>
  pendingItems.value.reduce((total, item) => total + (item.calories * item.quantity), 0),
)

watch(selectedDate, () => {
  void reloadMealData()
})

watch(searchQuery, (value) => {
  localError.value = ''
  foodSearchError.value = ''

  if (selectedFood.value && value.trim() !== selectedFood.value.name) {
    selectedFood.value = null
  }

  if (searchTimer !== null) {
    window.clearTimeout(searchTimer)
  }

  const trimmed = value.trim()
  if (!trimmed) {
    suggestions.value = []
    isSearchingFoods.value = false
    return
  }

  searchTimer = window.setTimeout(async () => {
    const token = ++latestSearchToken
    isSearchingFoods.value = true

    try {
      const items = await store.searchMealFoods(trimmed)
      if (token !== latestSearchToken) return
      suggestions.value = selectedFood.value && selectedFood.value.name === trimmed ? [] : items
    } catch (error) {
      if (token !== latestSearchToken) return
      suggestions.value = []
      foodSearchError.value = error instanceof Error ? error.message : '음식 검색 중 오류가 발생했습니다.'
    } finally {
      if (token === latestSearchToken) {
        isSearchingFoods.value = false
      }
    }
  }, 180)
})

onMounted(() => {
  void reloadMealData()
})

onBeforeUnmount(() => {
  if (searchTimer !== null) {
    window.clearTimeout(searchTimer)
  }
})

function formatLocalDate(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatDisplayDate(dateString: string) {
  const [year, month, day] = dateString.split('-').map(Number)
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'short',
  }).format(new Date(year, month - 1, day))
}

function formatNumber(value: number) {
  return new Intl.NumberFormat('ko-KR', { maximumFractionDigits: 1 }).format(value)
}

function progressStyle(value: number, goal: number) {
  const ratio = goal > 0 ? Math.min(100, Math.round((value / goal) * 100)) : 0
  return { width: `${ratio}%` }
}

function mealTypeLabel(type: MealType) {
  return mealTypeLabels[type]
}

function mealTypeBadgeClass(type: MealType) {
  return {
    BREAKFAST: 'warning',
    LUNCH: 'recipe',
    DINNER: 'success',
    SNACK: 'notice',
  }[type]
}

function selectFood(food: MealFoodSuggestion) {
  selectedFood.value = food
  searchQuery.value = food.name
  suggestions.value = []
  foodSearchError.value = ''
  if (!quantity.value.trim()) {
    quantity.value = '1'
  }
}

function addPendingItem() {
  localError.value = ''

  if (!selectedFood.value) {
    localError.value = '자동완성 목록에서 음식을 먼저 선택해 주세요.'
    return
  }

  const parsedQuantity = Number(quantity.value)
  if (!Number.isFinite(parsedQuantity) || parsedQuantity <= 0) {
    localError.value = '섭취량은 0보다 큰 숫자로 입력해 주세요.'
    return
  }

  const existingItem = pendingItems.value.find((item) => item.foodCode === selectedFood.value?.foodCode)
  if (existingItem) {
    existingItem.quantity = Number((existingItem.quantity + parsedQuantity).toFixed(2))
  } else {
    pendingItems.value = [...pendingItems.value, { ...selectedFood.value, quantity: parsedQuantity }]
  }

  searchQuery.value = ''
  quantity.value = '1'
  selectedFood.value = null
  suggestions.value = []
}

function removePendingItem(foodCode: string) {
  pendingItems.value = pendingItems.value.filter((item) => item.foodCode !== foodCode)
}

async function reloadMealData() {
  try {
    await store.loadMealLogs(selectedDate.value)
  } catch {
    // The store exposes a user-friendly error message.
  }
}

async function submitMealLog() {
  localError.value = ''

  if (pendingItems.value.length === 0) {
    localError.value = '저장할 음식을 하나 이상 추가해 주세요.'
    return
  }

  try {
    await store.saveMealLog({
      mealType: selectedMealType.value,
      date: selectedDate.value,
      memo: memo.value,
      items: pendingItems.value.map((item) => ({
        foodCode: item.foodCode,
        quantity: item.quantity,
      })),
    })

    memo.value = ''
    searchQuery.value = ''
    quantity.value = '1'
    selectedFood.value = null
    suggestions.value = []
    pendingItems.value = []
  } catch {
    // The store exposes a user-friendly error message.
  }
}

async function removeMealLog(id: number) {
  if (!window.confirm('이 식단 기록을 삭제할까요?')) return

  try {
    await store.deleteMealLog(id, selectedDate.value)
  } catch {
    // The store exposes a user-friendly error message.
  }
}
</script>
