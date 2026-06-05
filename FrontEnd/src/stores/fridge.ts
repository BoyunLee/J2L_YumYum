import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export type ViewName = 'dashboard' | 'inventory' | 'add' | 'detail' | 'recipes' | 'recipeDetail' | 'notifications'
export type Category = 'dairy' | 'meat' | 'vegetable' | 'fruit' | 'etc'
export type NotificationType = 'expiry' | 'expired' | 'recipe' | 'inventory'

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

const today = new Date('2026-06-05T00:00:00+09:00')

const daysBetween = (date: string) => {
  const target = new Date(`${date}T00:00:00+09:00`)
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
export const locations = ['냉장실', '냉동실', '실온']

export const useFridgeStore = defineStore('fridge', () => {
  const currentView = ref<ViewName>('dashboard')
  const selectedInventoryId = ref<number | null>(null)
  const selectedRecipeId = ref<number | null>(null)
  const favoriteRecipeIds = ref<number[]>([])

  const inventory = ref<InventoryItem[]>([
    { id: 1, name: '우유', category: 'dairy', quantity: 2, unit: '개', expiryDate: '2026-06-08', location: '냉장실', addedDate: '2026-06-01', memo: '세일할 때 구매함' },
    { id: 2, name: '계란', category: 'dairy', quantity: 10, unit: '개', expiryDate: '2026-06-14', location: '냉장실', addedDate: '2026-06-02', memo: '' },
    { id: 3, name: '닭고기', category: 'meat', quantity: 500, unit: 'g', expiryDate: '2026-06-06', location: '냉동실', addedDate: '2026-06-04', memo: '소분 보관' },
    { id: 4, name: '토마토', category: 'vegetable', quantity: 5, unit: '개', expiryDate: '2026-06-07', location: '냉장실', addedDate: '2026-06-03', memo: '' },
    { id: 5, name: '사과', category: 'fruit', quantity: 3, unit: '개', expiryDate: '2026-06-11', location: '냉장실', addedDate: '2026-06-01', memo: '' },
    { id: 6, name: '치즈', category: 'dairy', quantity: 1, unit: '개', expiryDate: '2026-06-19', location: '냉장실', addedDate: '2026-05-30', memo: '' },
    { id: 7, name: '요구르트', category: 'dairy', quantity: 4, unit: '개', expiryDate: '2026-06-03', location: '냉장실', addedDate: '2026-05-29', memo: '만료 확인 필요' },
    { id: 8, name: '양배추', category: 'vegetable', quantity: 1, unit: '개', expiryDate: '2026-06-12', location: '냉장실', addedDate: '2026-06-02', memo: '' },
  ])

  const recipes = ref<Recipe[]>([
    {
      id: 1,
      name: '치즈 오믈렛',
      description: '부드럽고 고소한 치즈 오믈렛',
      matchRate: 100,
      cookTime: 15,
      servings: 2,
      difficulty: '쉬움',
      calories: 320,
      gradient: 'yellow',
      availableIngredients: ['계란', '치즈', '우유'],
      missingIngredients: [],
      steps: ['계란과 우유를 넣고 잘 섞어주세요.', '팬에 버터를 두르고 중불로 예열해주세요.', '계란물을 붓고 가장자리가 익으면 천천히 저어주세요.', '치즈를 올리고 반으로 접어 1분간 익혀주세요.'],
      tips: ['중불보다 낮은 온도에서 익히면 더 부드러워요', '치즈는 계란이 다 익기 전에 넣어야 잘 녹아요'],
    },
    {
      id: 2,
      name: '토마토 카프레제',
      description: '신선한 토마토와 치즈를 활용한 샐러드',
      matchRate: 80,
      cookTime: 10,
      servings: 2,
      difficulty: '쉬움',
      calories: 210,
      gradient: 'red',
      availableIngredients: ['토마토', '치즈'],
      missingIngredients: ['바질', '올리브오일'],
      steps: ['토마토와 치즈를 먹기 좋게 썰어주세요.', '접시에 번갈아 올린 뒤 간을 맞춰주세요.', '올리브오일과 바질을 더해 마무리하세요.'],
      tips: ['토마토는 조리 직전에 썰면 수분감이 좋아요'],
    },
    {
      id: 3,
      name: '닭고기 야채볶음',
      description: '냉장고 야채와 닭고기로 만드는 든든한 볶음',
      matchRate: 75,
      cookTime: 25,
      servings: 3,
      difficulty: '보통',
      calories: 410,
      gradient: 'green',
      availableIngredients: ['닭고기', '양배추', '토마토'],
      missingIngredients: ['간장', '참기름'],
      steps: ['닭고기를 한입 크기로 썰어주세요.', '팬에 닭고기를 먼저 익힌 뒤 야채를 넣어주세요.', '간장과 참기름으로 간을 맞춰 볶아주세요.'],
      tips: ['닭고기는 물기를 제거하면 더 잘 익고 식감이 좋아요'],
    },
    {
      id: 4,
      name: '사과 샐러드',
      description: '상큼하고 가벼운 과일 샐러드',
      matchRate: 70,
      cookTime: 10,
      servings: 2,
      difficulty: '쉬움',
      calories: 180,
      gradient: 'lime',
      availableIngredients: ['사과'],
      missingIngredients: ['양상추', '견과류', '드레싱'],
      steps: ['사과를 얇게 썰어주세요.', '양상추와 견과류를 곁들여주세요.', '드레싱을 넣고 가볍게 섞어주세요.'],
      tips: ['사과에 레몬즙을 살짝 뿌리면 갈변을 줄일 수 있어요'],
    },
  ])

  const notifications = ref<NotificationItem[]>([
    { id: 1, type: 'expired', title: '유통기한 만료', message: '요구르트의 유통기한이 지났습니다. 폐기를 권장합니다.', time: '2026-06-05 09:00', read: false },
    { id: 2, type: 'expiry', title: '유통기한 임박', message: '닭고기가 1일 후 유통기한이 만료됩니다.', time: '2026-06-05 08:30', read: false },
    { id: 3, type: 'expiry', title: '유통기한 임박', message: '토마토가 2일 후 유통기한이 만료됩니다.', time: '2026-06-05 08:30', read: false },
    { id: 4, type: 'recipe', title: '새로운 레시피 추천', message: "현재 보유한 재료로 '치즈 오믈렛'을 만들 수 있어요.", time: '2026-06-04 18:00', read: true },
    { id: 5, type: 'inventory', title: '재고 추가 완료', message: '닭고기가 냉장고에 추가되었습니다.', time: '2026-06-04 12:10', read: true },
  ])

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

  function addInventory(form: InventoryForm) {
    const nextId = Math.max(...inventory.value.map((item) => item.id), 0) + 1
    inventory.value.unshift({
      id: nextId,
      name: form.name.trim(),
      category: form.category,
      quantity: Number(form.quantity),
      unit: form.unit,
      expiryDate: form.expiryDate,
      location: form.location,
      addedDate: '2026-06-05',
      memo: form.memo.trim(),
    })
    notifications.value.unshift({
      id: Math.max(...notifications.value.map((notification) => notification.id), 0) + 1,
      type: 'inventory',
      title: '재고 추가 완료',
      message: `${form.name.trim()} 재고가 추가되었습니다.`,
      time: '2026-06-05 12:00',
      read: false,
    })
    currentView.value = 'inventory'
  }

  function updateInventory(id: number, form: InventoryForm) {
    const target = inventory.value.find((item) => item.id === id)
    if (!target) return

    target.name = form.name.trim()
    target.category = form.category
    target.quantity = Number(form.quantity)
    target.unit = form.unit
    target.expiryDate = form.expiryDate
    target.location = form.location
    target.memo = form.memo.trim()
  }

  function deleteInventory(id: number) {
    inventory.value = inventory.value.filter((item) => item.id !== id)
    selectedInventoryId.value = null
    currentView.value = 'inventory'
  }

  function markAsRead(id: number) {
    const target = notifications.value.find((notification) => notification.id === id)
    if (target) target.read = true
  }

  function markAllAsRead() {
    notifications.value.forEach((notification) => {
      notification.read = true
    })
  }

  function deleteNotification(id: number) {
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
    inventoryWithStatus,
    notifications,
    recentInventory,
    recipes,
    selectedInventory,
    selectedRecipe,
    stats,
    unreadCount,
    addInventory,
    deleteInventory,
    deleteNotification,
    go,
    markAllAsRead,
    markAsRead,
    openInventory,
    openRecipe,
    toggleFavoriteRecipe,
    updateInventory,
  }
})
