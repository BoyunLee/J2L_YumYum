import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { useAuthStore } from './auth'

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

export type ViewName = 'dashboard' | 'inventory' | 'add' | 'detail' | 'meals' | 'recipes' | 'recipeDetail' | 'notifications' | 'myPage'
export type Category = 'dairy' | 'meat' | 'vegetable' | 'fruit' | 'etc'
export type NotificationType = 'expiry' | 'expired' | 'recipe' | 'inventory' | 'notice'
export type ImageAnalysisType = 'OCR' | 'BARCODE'
export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK'

export interface InventoryItem {
  id: number
  name: string
  category: Category
  quantity: number
  unit: string
  expiryDate: string
  location: string
  addedDate: string
  memo: string
}

export interface InventoryForm {
  name: string
  category: Category
  quantity: string
  unit: string
  expiryDate: string
  location: string
  memo: string
}

export interface InventoryImageAnalysis {
  analysisType: ImageAnalysisType
  detected: boolean
  items: InventoryImageAnalysisItem[]
  message: string
}

export interface InventoryImageAnalysisItem {
  name: string | null
  category: string | null
  quantity: number | null
  unit: string | null
  expirationDate: string | null
  storageLocation: string | null
  memo: string | null
  barcode: string | null
  rawText: string | null
  confidence: number | null
}

interface ApiInventoryItem {
  id: number
  name: string
  category: string
  quantity: number
  unit: string
  expirationDate: string
  storageLocation: string
  memo: string | null
  addedDate: string
}

interface ApiLatestRecipeRecommendations {
  mealLogId: number | null
  recipes: Array<Omit<Recipe, 'gradient'>>
}

interface ApiNotification {
  id: number
  type: 'expiry' | 'expired' | 'notice'
  title: string
  message: string
  createdAt: string
  read: boolean
}

interface ApiMealFoodSuggestion {
  foodCode: string
  name: string
  category: string | null
  baseAmount: string | null
  calories: number | null
  protein: number | null
  carbohydrate: number | null
  fat: number | null
}

interface ApiMealLogItem {
  id: number
  foodCode: string
  name: string
  quantity: number
  baseAmount: string | null
  calories: number | null
  protein: number | null
  carbohydrate: number | null
  fat: number | null
}

interface ApiMealLog {
  id: number
  mealType: MealType
  date: string
  memo: string | null
  totalCalories: number | null
  totalProtein: number | null
  totalCarbohydrate: number | null
  totalFat: number | null
  items: ApiMealLogItem[]
}

interface ApiMealSummary {
  date: string
  mealCount: number | null
  itemCount: number | null
  totalCalories: number | null
  totalProtein: number | null
  totalCarbohydrate: number | null
  totalFat: number | null
  goalCalories: number | null
  goalProtein: number | null
  goalCarbohydrate: number | null
  goalFat: number | null
}

export interface Recipe {
  id: number
  name: string
  description: string
  matchRate: number
  cookTime: number
  servings: number
  difficulty: string
  calories: number
  gradient: string
  availableIngredients: string[]
  missingIngredients: string[]
  steps: string[]
  tips: string[]
}

export interface NotificationItem {
  id: number
  type: NotificationType
  title: string
  message: string
  time: string
  read: boolean
}

export interface MealFoodSuggestion {
  foodCode: string
  name: string
  category: string | null
  baseAmount: string | null
  calories: number
  protein: number
  carbohydrate: number
  fat: number
}

export interface MealLogItem {
  id: number
  foodCode: string
  name: string
  quantity: number
  baseAmount: string | null
  calories: number
  protein: number
  carbohydrate: number
  fat: number
}

export interface MealLog {
  id: number
  mealType: MealType
  date: string
  memo: string | null
  totalCalories: number
  totalProtein: number
  totalCarbohydrate: number
  totalFat: number
  items: MealLogItem[]
}

export interface MealSummary {
  date: string
  mealCount: number
  itemCount: number
  totalCalories: number
  totalProtein: number
  totalCarbohydrate: number
  totalFat: number
  goalCalories: number
  goalProtein: number
  goalCarbohydrate: number
  goalFat: number
}

export interface SaveMealLogPayload {
  mealType: MealType
  date: string
  memo: string
  items: Array<{
    foodCode: string
    quantity: number
  }>
}

const visibleNotificationTypes = new Set<NotificationType>(['expiry', 'expired', 'notice'])

const daysBetween = (date: string) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const target = new Date(`${date}T00:00:00`)
  return Math.ceil((target.getTime() - today.getTime()) / 86_400_000)
}

export const categories: { value: Category | 'all'; label: string }[] = [
  { value: 'all', label: '전체' },
  { value: 'dairy', label: '유제품' },
  { value: 'meat', label: '육류' },
  { value: 'vegetable', label: '채소' },
  { value: 'fruit', label: '과일' },
  { value: 'etc', label: '기타' },
]

export const units = ['개', 'g', 'kg', 'ml', 'L']
export const locations = ['냉장', '냉동', '실온']

const categoryMap: Record<Category, string> = {
  dairy: 'DAIRY', meat: 'MEAT', vegetable: 'VEGETABLE', fruit: 'FRUIT', etc: 'ETC',
}
const locationMap: Record<string, string> = {
  냉장: 'REFRIGERATOR',
  냉동: 'FREEZER',
  실온: 'ROOM_TEMPERATURE',
}
const locationLabels: Record<string, string> = {
  REFRIGERATOR: '냉장',
  FREEZER: '냉동',
  ROOM_TEMPERATURE: '실온',
}
const recipeGradients = ['yellow', 'red', 'green', 'lime']
const toNumber = (value: number | null | undefined) => Number(value ?? 0)

const toRequestBody = (form: InventoryForm) => ({
  name: form.name.trim(),
  category: categoryMap[form.category],
  quantity: Number(form.quantity),
  unit: form.unit,
  expirationDate: form.expiryDate,
  storageLocation: locationMap[form.location],
  memo: form.memo.trim(),
})

const toInventoryItem = (item: ApiInventoryItem): InventoryItem => ({
  id: item.id,
  name: item.name,
  category: item.category.toLowerCase() as Category,
  quantity: Number(item.quantity),
  unit: item.unit,
  expiryDate: item.expirationDate,
  location: locationLabels[item.storageLocation],
  addedDate: item.addedDate,
  memo: item.memo ?? '',
})

const toMealFoodSuggestion = (item: ApiMealFoodSuggestion): MealFoodSuggestion => ({
  foodCode: item.foodCode,
  name: item.name,
  category: item.category,
  baseAmount: item.baseAmount,
  calories: toNumber(item.calories),
  protein: toNumber(item.protein),
  carbohydrate: toNumber(item.carbohydrate),
  fat: toNumber(item.fat),
})

const toMealLogItem = (item: ApiMealLogItem): MealLogItem => ({
  id: item.id,
  foodCode: item.foodCode,
  name: item.name,
  quantity: Number(item.quantity),
  baseAmount: item.baseAmount,
  calories: toNumber(item.calories),
  protein: toNumber(item.protein),
  carbohydrate: toNumber(item.carbohydrate),
  fat: toNumber(item.fat),
})

const toMealLog = (item: ApiMealLog): MealLog => ({
  id: item.id,
  mealType: item.mealType,
  date: item.date,
  memo: item.memo,
  totalCalories: toNumber(item.totalCalories),
  totalProtein: toNumber(item.totalProtein),
  totalCarbohydrate: toNumber(item.totalCarbohydrate),
  totalFat: toNumber(item.totalFat),
  items: item.items.map(toMealLogItem),
})

const toMealSummary = (item: ApiMealSummary): MealSummary => ({
  date: item.date,
  mealCount: Number(item.mealCount ?? 0),
  itemCount: Number(item.itemCount ?? 0),
  totalCalories: toNumber(item.totalCalories),
  totalProtein: toNumber(item.totalProtein),
  totalCarbohydrate: toNumber(item.totalCarbohydrate),
  totalFat: toNumber(item.totalFat),
  goalCalories: toNumber(item.goalCalories),
  goalProtein: toNumber(item.goalProtein),
  goalCarbohydrate: toNumber(item.goalCarbohydrate),
  goalFat: toNumber(item.goalFat),
})

export const useFridgeStore = defineStore('fridge', () => {
  const auth = useAuthStore()
  const currentView = ref<ViewName>('dashboard')
  const selectedInventoryId = ref<number | null>(null)
  const selectedRecipeId = ref<number | null>(null)
  const favoriteRecipeIds = ref<number[]>([])

  const inventory = ref<InventoryItem[]>([])
  const isRecommendingRecipes = ref(false)
  const isLoadingSavedRecipes = ref(false)
  const recipeRecommendationError = ref('')
  const lastRecipeInventoryKey = ref('')
  const hasRequestedRecipes = ref(false)
  const recipes = ref<Recipe[]>([])

  const notifications = ref<NotificationItem[]>([])
  const mealLogs = ref<MealLog[]>([])
  const mealSummary = ref<MealSummary | null>(null)
  const isLoadingMealLogs = ref(false)
  const isSavingMealLog = ref(false)
  const mealLogError = ref('')

  const inventoryWithStatus = computed(() =>
    inventory.value.map((item) => ({ ...item, daysLeft: daysBetween(item.expiryDate) })),
  )

  const stats = computed(() => ({
    total: inventory.value.length,
    expiringSoon: inventoryWithStatus.value.filter((item) => item.daysLeft >= 0 && item.daysLeft <= 7).length,
    expired: inventoryWithStatus.value.filter((item) => item.daysLeft < 0).length,
  }))

  const recentInventory = computed(() => inventoryWithStatus.value.slice(0, 4))
  const unreadCount = computed(() => notifications.value.filter((notification) => !notification.read).length)
  const selectedInventory = computed(() => inventoryWithStatus.value.find((item) => item.id === selectedInventoryId.value) ?? null)
  const selectedRecipe = computed(() => recipes.value.find((recipe) => recipe.id === selectedRecipeId.value) ?? recipes.value[0])

  async function apiRequest<T>(path: string, options: RequestInit = {}) {
    const isMultipart = options.body instanceof FormData
    const response = await auth.authorizedFetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        ...(options.body && !isMultipart ? { 'Content-Type': 'application/json' } : {}),
        ...options.headers,
      },
    })
    const contentType = response.headers.get('content-type') ?? ''
    const body = contentType.includes('application/json') ? await response.json() : null
    if (!response.ok) {
      if (response.status === 401) {
        auth.expireSession()
        throw new Error('로그인이 만료되었습니다. 다시 로그인해 주세요.')
      }
      if (response.status === 403) throw new Error('요청을 처리할 권한이 없습니다.')
      throw new Error(body?.message ?? body?.msg ?? `요청을 처리하지 못했습니다. (HTTP ${response.status})`)
    }
    return body?.data as T
  }

  function go(view: ViewName) {
    currentView.value = view
  }

  function openInventory(id: number) {
    selectedInventoryId.value = id
    currentView.value = 'detail'
  }

  function openRecipe(id: number) {
    selectedRecipeId.value = id
    currentView.value = 'recipeDetail'
  }

  async function addInventory(forms: InventoryForm[]) {
    const items = await apiRequest<ApiInventoryItem[]>('/api/refrigerator/items/manual', {
      method: 'POST',
      body: JSON.stringify({
        items: forms.map(toRequestBody),
      }),
    })
    const addedItems = items.map(toInventoryItem)
    inventory.value = [...addedItems.slice().reverse(), ...inventory.value]
    currentView.value = 'inventory'
  }

  async function analyzeInventoryImage(analysisType: ImageAnalysisType, image: File) {
    const formData = new FormData()
    formData.append('image', image, image.name)
    return apiRequest<InventoryImageAnalysis>(
      `/api/refrigerator/items/analyze/${analysisType.toLowerCase()}`,
      {
        method: 'POST',
        body: formData,
      },
    )
  }

  async function loadInventory() {
    const items = await apiRequest<ApiInventoryItem[]>('/api/refrigerator/items')
    inventory.value = items.map(toInventoryItem)
  }

  async function loadNotifications() {
    const items = await apiRequest<ApiNotification[]>('/api/notifications')
    notifications.value = items
      .map((item) => ({
        id: item.id,
        type: item.type,
        title: item.title,
        message: item.message,
        time: item.createdAt.replace('T', ' ').slice(0, 16),
        read: item.read,
      }))
      .filter((item) => visibleNotificationTypes.has(item.type))
  }

  async function loadSavedRecipes() {
    if (isLoadingSavedRecipes.value) return
    isLoadingSavedRecipes.value = true
    recipeRecommendationError.value = ''
    try {
      const response = await apiRequest<ApiLatestRecipeRecommendations>('/api/meal-logs/recommendations/latest')
      recipes.value = response.recipes.map((recipe, index) => ({
        ...recipe,
        gradient: recipeGradients[index % recipeGradients.length],
      }))
      hasRequestedRecipes.value = recipes.value.length > 0
      selectedRecipeId.value = null
    } catch (error) {
      recipeRecommendationError.value = error instanceof Error
        ? error.message
        : '저장된 레시피를 불러오지 못했습니다.'
      throw error
    } finally {
      isLoadingSavedRecipes.value = false
    }
  }

  async function searchMealFoods(query: string, limit = 8) {
    const trimmedQuery = query.trim()
    if (!trimmedQuery) return []

    const params = new URLSearchParams({
      q: trimmedQuery,
      limit: String(limit),
    })
    const items = await apiRequest<ApiMealFoodSuggestion[]>(`/api/meal-logs/foods/search?${params.toString()}`)
    return items.map(toMealFoodSuggestion)
  }

  async function refreshMealDashboard(date: string) {
    isLoadingMealLogs.value = true
    mealLogError.value = ''
    try {
      const params = new URLSearchParams({ date })
      const [logs, summary] = await Promise.all([
        apiRequest<ApiMealLog[]>(`/api/meal-logs?${params.toString()}`),
        apiRequest<ApiMealSummary>(`/api/meal-logs/summary?${params.toString()}`),
      ])
      mealLogs.value = logs.map(toMealLog)
      mealSummary.value = toMealSummary(summary)
    } catch (error) {
      mealLogError.value = error instanceof Error ? error.message : '식단 기록을 불러오지 못했습니다.'
      throw error
    } finally {
      isLoadingMealLogs.value = false
    }
  }

  async function loadMealLogs(date: string) {
    await refreshMealDashboard(date)
  }

  async function saveMealLog(payload: SaveMealLogPayload) {
    if (isSavingMealLog.value) return null

    isSavingMealLog.value = true
    mealLogError.value = ''
    try {
      const created = await apiRequest<ApiMealLog>('/api/meal-logs', {
        method: 'POST',
        body: JSON.stringify({
          mealType: payload.mealType,
          date: payload.date,
          memo: payload.memo.trim() || null,
          items: payload.items.map((item) => ({
            foodCode: item.foodCode,
            quantity: item.quantity,
          })),
        }),
      })
      await refreshMealDashboard(payload.date)
      return toMealLog(created)
    } catch (error) {
      mealLogError.value = error instanceof Error ? error.message : '식단 기록을 저장하지 못했습니다.'
      throw error
    } finally {
      isSavingMealLog.value = false
    }
  }

  async function deleteMealLog(id: number, date: string) {
    mealLogError.value = ''
    try {
      await apiRequest<void>(`/api/meal-logs/${id}`, { method: 'DELETE' })
      await refreshMealDashboard(date)
    } catch (error) {
      mealLogError.value = error instanceof Error ? error.message : '식단 기록을 삭제하지 못했습니다.'
      throw error
    }
  }

  async function recommendRecipes(force = false) {
    if (isRecommendingRecipes.value) return
    const inventoryKey = inventory.value
      .map(({ id, name, quantity, unit, expiryDate }) => `${id}:${name}:${quantity}:${unit}:${expiryDate}`)
      .sort()
      .join('|')
    if (!force && inventoryKey === lastRecipeInventoryKey.value) return

    isRecommendingRecipes.value = true
    hasRequestedRecipes.value = true
    recipeRecommendationError.value = ''
    try {
      const response = await apiRequest<ApiLatestRecipeRecommendations>('/api/meal-logs/recommendations/generate', {
        method: 'POST',
      })
      recipes.value = response.recipes.map((recipe, index) => ({
        ...recipe,
        gradient: recipeGradients[index % recipeGradients.length],
      }))
      lastRecipeInventoryKey.value = inventoryKey
      selectedRecipeId.value = null
    } catch (error) {
      const message = error instanceof Error ? error.message : '레시피를 추천받지 못했습니다.'
      recipeRecommendationError.value = recipes.value.length > 0
        ? `추천 결과는 표시했지만 새로 저장하지 못했습니다. ${message}`
        : message
      throw error
    } finally {
      isRecommendingRecipes.value = false
    }
  }

  async function updateInventory(id: number, form: InventoryForm) {
    const item = await apiRequest<ApiInventoryItem>(`/api/refrigerator/items/${id}`, {
      method: 'PUT',
      body: JSON.stringify(toRequestBody(form)),
    })
    const index = inventory.value.findIndex((inventoryItem) => inventoryItem.id === id)
    if (index >= 0) inventory.value[index] = toInventoryItem(item)
  }

  async function deleteInventory(id: number) {
    await apiRequest<void>(`/api/refrigerator/items/${id}`, { method: 'DELETE' })
    inventory.value = inventory.value.filter((item) => item.id !== id)
    selectedInventoryId.value = null
    currentView.value = 'inventory'
  }

  async function markAsRead(id: number) {
    await apiRequest<void>(`/api/notifications/${id}/read`, { method: 'PUT' })
    const target = notifications.value.find((notification) => notification.id === id)
    if (target) target.read = true
  }

  async function markAllAsRead() {
    await apiRequest<void>('/api/notifications/read-all', { method: 'PUT' })
    notifications.value.forEach((notification) => {
      notification.read = true
    })
  }

  async function deleteNotification(id: number) {
    await apiRequest<void>(`/api/notifications/${id}`, { method: 'DELETE' })
    notifications.value = notifications.value.filter((notification) => notification.id !== id)
  }

  function toggleFavoriteRecipe(id: number) {
    favoriteRecipeIds.value = favoriteRecipeIds.value.includes(id)
      ? favoriteRecipeIds.value.filter((recipeId) => recipeId !== id)
      : [...favoriteRecipeIds.value, id]
  }

  return {
    currentView,
    favoriteRecipeIds,
    hasRequestedRecipes,
    inventoryWithStatus,
    isLoadingSavedRecipes,
    isLoadingMealLogs,
    isRecommendingRecipes,
    isSavingMealLog,
    mealLogError,
    mealLogs,
    mealSummary,
    notifications,
    recentInventory,
    recipes,
    recipeRecommendationError,
    selectedInventory,
    selectedRecipe,
    stats,
    unreadCount,
    addInventory,
    analyzeInventoryImage,
    deleteInventory,
    deleteMealLog,
    deleteNotification,
    go,
    loadMealLogs,
    markAllAsRead,
    markAsRead,
    loadInventory,
    loadNotifications,
    loadSavedRecipes,
    openInventory,
    openRecipe,
    recommendRecipes,
    refreshMealDashboard,
    saveMealLog,
    searchMealFoods,
    toggleFavoriteRecipe,
    updateInventory,
  }
})

