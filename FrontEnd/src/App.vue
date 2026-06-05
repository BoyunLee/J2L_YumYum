<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import {
  categories,
  locations,
  units,
  useFridgeStore,
  type Category,
  type InventoryForm,
  type NotificationType,
} from './stores/fridge'

const store = useFridgeStore()
const {
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
} = storeToRefs(store)

const searchQuery = ref('')
const selectedCategory = ref<Category | 'all'>('all')
const recipeSearchQuery = ref('')
const recipeSortBy = ref<'match' | 'time'>('match')
const notificationFilter = ref<NotificationType | 'all'>('all')
const isEditing = ref(false)

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
const editForm = reactive<InventoryForm>(emptyForm())

const navItems = [
  { view: 'dashboard' as const, label: '대시보드', icon: 'home' },
  { view: 'inventory' as const, label: '재고 관리', icon: 'package' },
  { view: 'recipes' as const, label: '레시피', icon: 'chef' },
  { view: 'notifications' as const, label: '알림', icon: 'bell' },
]

const categoryLabels = computed(() =>
  Object.fromEntries(categories.map((category) => [category.value, category.label])) as Record<Category | 'all', string>,
)

const filteredInventory = computed(() =>
  inventoryWithStatus.value.filter((item) => {
    const matchesSearch = item.name.toLowerCase().includes(searchQuery.value.trim().toLowerCase())
    const matchesCategory = selectedCategory.value === 'all' || item.category === selectedCategory.value
    return matchesSearch && matchesCategory
  }),
)

const filteredRecipes = computed(() =>
  recipes.value
    .filter((recipe) => recipe.name.toLowerCase().includes(recipeSearchQuery.value.trim().toLowerCase()))
    .sort((a, b) => (recipeSortBy.value === 'match' ? b.matchRate - a.matchRate : a.cookTime - b.cookTime)),
)

const filteredNotifications = computed(() =>
  notifications.value.filter((notification) => notificationFilter.value === 'all' || notification.type === notificationFilter.value),
)

const dashboardNotifications = computed(() => notifications.value.slice(0, 3))
const recommendedRecipes = computed(() => recipes.value.slice(0, 2))

watch(selectedInventory, (item) => {
  if (!item) return

  Object.assign(editForm, {
    name: item.name,
    category: item.category,
    quantity: String(item.quantity),
    unit: item.unit,
    expiryDate: item.expiryDate,
    location: item.location,
    memo: item.memo,
  })
  isEditing.value = false
})

function iconPath(icon: string) {
  const paths: Record<string, string> = {
    home: 'M3 10.5 12 3l9 7.5V21a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1v-10.5Z',
    package: 'M4 7.5 12 3l8 4.5v9L12 21l-8-4.5v-9Zm4.2-2.4 8 4.6M12 12v9',
    chef: 'M8 7a4 4 0 0 1 8 0 3 3 0 1 1 1 5.83V21H7v-8.17A3 3 0 1 1 8 7Zm1 11h6',
    bell: 'M18 16H6l1.5-2V9a4.5 4.5 0 0 1 9 0v5L18 16Zm-8 3h4',
    plus: 'M12 5v14M5 12h14',
    search: 'm21 21-4.35-4.35M10.5 18a7.5 7.5 0 1 1 0-15 7.5 7.5 0 0 1 0 15Z',
    filter: 'M4 5h16l-6 7v5l-4 2v-7L4 5Z',
    calendar: 'M7 3v4M17 3v4M4 9h20M6 5h18v17H6z',
    alert: 'M12 8v5M12 17h.01M21 19 12 4 3 19h18Z',
    trend: 'M4 17 10 11l4 4 7-8M15 7h6v6',
    clock: 'M12 6v6l4 2M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Z',
    users: 'M16 21v-2a4 4 0 0 0-8 0v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
    back: 'M19 12H5M12 19l-7-7 7-7',
    edit: 'M12 20h9M16.5 3.5l4 4L8 20H4v-4L16.5 3.5Z',
    trash: 'M4 7h16M10 11v6M14 11v6M6 7l1 14h10l1-14M9 7V4h6v3',
    check: 'M20 6 9 17l-5-5',
    heart: 'M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.6l-1-1a5.5 5.5 0 0 0-7.8 7.8l1 1L12 21l7.8-7.6 1-1a5.5 5.5 0 0 0 0-7.8Z',
    camera: 'M6 7h2l2-3h4l2 3h2a3 3 0 0 1 3 3v8a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3v-8a3 3 0 0 1 3-3Zm6 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z',
    barcode: 'M4 5v14M8 5v14M12 5v14M17 5v14M21 5v14',
  }
  return paths[icon] ?? paths.package
}

function statusClass(daysLeft: number) {
  if (daysLeft < 0) return 'danger'
  if (daysLeft <= 3) return 'danger'
  if (daysLeft <= 7) return 'warning'
  return 'success'
}

function daysLabel(daysLeft: number) {
  if (daysLeft < 0) return `${Math.abs(daysLeft)}일 지남`
  if (daysLeft === 0) return '오늘 만료'
  return `${daysLeft}일 남음`
}

function notificationLabel(type: NotificationType) {
  return {
    expired: '만료됨',
    expiry: '임박',
    recipe: '레시피',
    inventory: '재고',
  }[type]
}

function submitAdd() {
  store.addInventory({ ...addForm })
  Object.assign(addForm, emptyForm())
}

function submitEdit() {
  if (!selectedInventory.value) return

  store.updateInventory(selectedInventory.value.id, { ...editForm })
  isEditing.value = false
}
</script>

<template>
  <div class="app-shell">
    <header class="site-header">
      <div class="container header-inner">
        <button class="brand" type="button" @click="store.go('dashboard')">
          <span class="brand-icon">
            <svg viewBox="0 0 24 24"><path :d="iconPath('package')" /></svg>
          </span>
          <span>냉장고 관리</span>
        </button>

        <nav class="nav-list" aria-label="주요 메뉴">
          <button
            v-for="item in navItems"
            :key="item.view"
            type="button"
            class="nav-item"
            :class="{ active: currentView === item.view || (item.view === 'inventory' && ['add', 'detail'].includes(currentView)) || (item.view === 'recipes' && currentView === 'recipeDetail') }"
            @click="store.go(item.view)"
          >
            <svg viewBox="0 0 24 24"><path :d="iconPath(item.icon)" /></svg>
            <span>{{ item.label }}</span>
            <b v-if="item.view === 'notifications' && unreadCount">{{ unreadCount }}</b>
          </button>
        </nav>
      </div>
    </header>

    <main>
      <section v-if="currentView === 'dashboard'" class="container page">
        <div class="stats-grid">
          <article class="stat-card">
            <div>
              <p>전체 재고</p>
              <strong>{{ stats.total }}</strong>
            </div>
            <span class="icon-box blue"><svg viewBox="0 0 24 24"><path :d="iconPath('package')" /></svg></span>
          </article>
          <article class="stat-card">
            <div>
              <p>곧 만료</p>
              <strong class="text-warning">{{ stats.expiringSoon }}</strong>
            </div>
            <span class="icon-box orange"><svg viewBox="0 0 24 24"><path :d="iconPath('alert')" /></svg></span>
          </article>
          <article class="stat-card">
            <div>
              <p>만료됨</p>
              <strong class="text-danger">{{ stats.expired }}</strong>
            </div>
            <span class="icon-box red"><svg viewBox="0 0 24 24"><path :d="iconPath('alert')" /></svg></span>
          </article>
        </div>

        <div class="dashboard-grid">
          <section class="panel">
            <div class="panel-head">
              <h2>최근 재고</h2>
              <button class="primary-btn compact" type="button" @click="store.go('add')">
                <svg viewBox="0 0 24 24"><path :d="iconPath('plus')" /></svg>
                추가
              </button>
            </div>
            <div class="list-stack">
              <button v-for="item in recentInventory" :key="item.id" class="inventory-row" type="button" @click="store.openInventory(item.id)">
                <span>
                  <strong>{{ item.name }}</strong>
                  <small>{{ categoryLabels[item.category] }}</small>
                </span>
                <span class="right-text">
                  <small>만료일: {{ item.expiryDate }}</small>
                  <em :class="`text-${statusClass(item.daysLeft)}`">{{ daysLabel(item.daysLeft) }}</em>
                </span>
              </button>
            </div>
            <button class="link-btn center" type="button" @click="store.go('inventory')">전체 재고 보기</button>
          </section>

          <div class="side-stack">
            <section class="panel">
              <div class="panel-head">
                <h2>알림</h2>
                <button class="link-btn" type="button" @click="store.go('notifications')">전체 보기</button>
              </div>
              <div class="list-stack small-gap">
                <article v-for="notification in dashboardNotifications" :key="notification.id" class="notice-strip" :class="notification.type">
                  <svg viewBox="0 0 24 24"><path :d="iconPath(notification.type === 'recipe' ? 'chef' : 'alert')" /></svg>
                  <p>{{ notification.message }}</p>
                </article>
              </div>
            </section>

            <section class="panel">
              <div class="panel-head">
                <h2>추천 레시피</h2>
                <button class="link-btn" type="button" @click="store.go('recipes')">더 보기</button>
              </div>
              <div class="list-stack">
                <button v-for="recipe in recommendedRecipes" :key="recipe.id" class="recipe-mini" type="button" @click="store.openRecipe(recipe.id)">
                  <span>
                    <strong>{{ recipe.name }}</strong>
                    <small>{{ recipe.availableIngredients.join(', ') }}</small>
                  </span>
                  <em>{{ recipe.matchRate }}%</em>
                </button>
              </div>
            </section>
          </div>
        </div>
      </section>

      <section v-else-if="currentView === 'inventory'" class="container page">
        <div class="page-head">
          <div>
            <h1>재고 관리</h1>
            <p>냉장고에 있는 모든 식품을 관리하세요</p>
          </div>
          <button class="primary-btn" type="button" @click="store.go('add')">
            <svg viewBox="0 0 24 24"><path :d="iconPath('plus')" /></svg>
            재고 추가
          </button>
        </div>

        <section class="filter-panel">
          <label class="field-with-icon">
            <svg viewBox="0 0 24 24"><path :d="iconPath('search')" /></svg>
            <input v-model="searchQuery" type="search" placeholder="재고 검색..." />
          </label>
          <label class="field-with-icon">
            <svg viewBox="0 0 24 24"><path :d="iconPath('filter')" /></svg>
            <select v-model="selectedCategory">
              <option v-for="category in categories" :key="category.value" :value="category.value">{{ category.label }}</option>
            </select>
          </label>
        </section>

        <div class="card-grid">
          <button v-for="item in filteredInventory" :key="item.id" class="inventory-card" type="button" @click="store.openInventory(item.id)">
            <span class="card-top">
              <span>
                <strong>{{ item.name }}</strong>
                <small>{{ categoryLabels[item.category] }}</small>
              </span>
              <em :class="`badge ${statusClass(item.daysLeft)}`">{{ daysLabel(item.daysLeft) }}</em>
            </span>
            <span class="meta-row"><small>수량</small><strong>{{ item.quantity }} {{ item.unit }}</strong></span>
            <span class="meta-row"><small>보관 위치</small><strong>{{ item.location }}</strong></span>
            <span class="meta-row bordered">
              <small><svg viewBox="0 0 24 24"><path :d="iconPath('calendar')" /></svg> 유통기한</small>
              <strong :class="`text-${statusClass(item.daysLeft)}`">{{ item.expiryDate }}</strong>
            </span>
          </button>
        </div>

        <p v-if="filteredInventory.length === 0" class="empty-text">검색 결과가 없습니다.</p>
      </section>

      <section v-else-if="currentView === 'add'" class="container narrow page">
        <button class="back-btn" type="button" @click="store.go('inventory')">
          <svg viewBox="0 0 24 24"><path :d="iconPath('back')" /></svg>
          목록으로 돌아가기
        </button>

        <section class="panel padded">
          <h1>재고 추가</h1>
          <div class="quick-actions">
            <button type="button"><svg viewBox="0 0 24 24"><path :d="iconPath('camera')" /></svg>OCR로 추가</button>
            <button type="button"><svg viewBox="0 0 24 24"><path :d="iconPath('barcode')" /></svg>바코드 스캔</button>
          </div>

          <form class="form-grid" @submit.prevent="submitAdd">
            <label class="form-field full">상품명 <input v-model="addForm.name" required placeholder="예: 우유" /></label>
            <label class="form-field">카테고리
              <select v-model="addForm.category" required>
                <option v-for="category in categories.filter((item) => item.value !== 'all')" :key="category.value" :value="category.value">{{ category.label }}</option>
              </select>
            </label>
            <label class="form-field">수량
              <span class="inline-fields">
                <input v-model="addForm.quantity" required type="number" min="0" placeholder="0" />
                <select v-model="addForm.unit"><option v-for="unit in units" :key="unit" :value="unit">{{ unit }}</option></select>
              </span>
            </label>
            <label class="form-field">유통기한 <input v-model="addForm.expiryDate" required type="date" /></label>
            <label class="form-field">보관 위치
              <select v-model="addForm.location" required><option v-for="location in locations" :key="location" :value="location">{{ location }}</option></select>
            </label>
            <label class="form-field full">메모 <textarea v-model="addForm.memo" rows="3" placeholder="특이사항이나 메모를 입력하세요" /></label>
            <div class="button-row full">
              <button class="ghost-btn" type="button" @click="store.go('inventory')">취소</button>
              <button class="primary-btn" type="submit">추가하기</button>
            </div>
          </form>
        </section>
      </section>

      <section v-else-if="currentView === 'detail' && selectedInventory" class="container narrow page">
        <button class="back-btn" type="button" @click="store.go('inventory')">
          <svg viewBox="0 0 24 24"><path :d="iconPath('back')" /></svg>
          목록으로 돌아가기
        </button>

        <article class="detail-card">
          <div class="detail-hero">
            <div>
              <h1>{{ isEditing ? editForm.name : selectedInventory.name }}</h1>
              <p>{{ categoryLabels[selectedInventory.category] }}</p>
            </div>
            <em :class="`badge solid ${statusClass(selectedInventory.daysLeft)}`">{{ daysLabel(selectedInventory.daysLeft) }}</em>
          </div>

          <form v-if="isEditing" class="form-grid padded" @submit.prevent="submitEdit">
            <label class="form-field full">상품명 <input v-model="editForm.name" required /></label>
            <label class="form-field">카테고리
              <select v-model="editForm.category" required>
                <option v-for="category in categories.filter((item) => item.value !== 'all')" :key="category.value" :value="category.value">{{ category.label }}</option>
              </select>
            </label>
            <label class="form-field">수량
              <span class="inline-fields">
                <input v-model="editForm.quantity" required type="number" min="0" />
                <select v-model="editForm.unit"><option v-for="unit in units" :key="unit" :value="unit">{{ unit }}</option></select>
              </span>
            </label>
            <label class="form-field">유통기한 <input v-model="editForm.expiryDate" required type="date" /></label>
            <label class="form-field">보관 위치
              <select v-model="editForm.location" required><option v-for="location in locations" :key="location" :value="location">{{ location }}</option></select>
            </label>
            <label class="form-field full">메모 <textarea v-model="editForm.memo" rows="3" /></label>
            <div class="button-row full">
              <button class="ghost-btn" type="button" @click="isEditing = false">취소</button>
              <button class="primary-btn" type="submit">저장하기</button>
            </div>
          </form>

          <div v-else class="padded detail-body">
            <div class="info-grid">
              <div class="info-box"><span class="icon-box blue"><svg viewBox="0 0 24 24"><path :d="iconPath('package')" /></svg></span><small>수량</small><strong>{{ selectedInventory.quantity }} {{ selectedInventory.unit }}</strong></div>
              <div class="info-box"><span class="icon-box green"><svg viewBox="0 0 24 24"><path :d="iconPath('filter')" /></svg></span><small>보관 위치</small><strong>{{ selectedInventory.location }}</strong></div>
              <div class="info-box"><span class="icon-box orange"><svg viewBox="0 0 24 24"><path :d="iconPath('calendar')" /></svg></span><small>유통기한</small><strong>{{ selectedInventory.expiryDate }}</strong></div>
              <div class="info-box"><span class="icon-box purple"><svg viewBox="0 0 24 24"><path :d="iconPath('calendar')" /></svg></span><small>등록일</small><strong>{{ selectedInventory.addedDate }}</strong></div>
            </div>
            <div v-if="selectedInventory.memo" class="memo-box">
              <strong>메모</strong>
              <p>{{ selectedInventory.memo }}</p>
            </div>
            <div class="button-row">
              <button class="primary-btn" type="button" @click="isEditing = true"><svg viewBox="0 0 24 24"><path :d="iconPath('edit')" /></svg>수정하기</button>
              <button class="danger-btn" type="button" @click="store.deleteInventory(selectedInventory.id)"><svg viewBox="0 0 24 24"><path :d="iconPath('trash')" /></svg>삭제하기</button>
            </div>
          </div>
        </article>
      </section>

      <section v-else-if="currentView === 'recipes'" class="container page">
        <div class="page-head">
          <div>
            <h1>레시피 추천</h1>
            <p>냉장고 재고를 기반으로 만들 수 있는 요리를 추천해드려요</p>
          </div>
        </div>
        <section class="filter-panel">
          <label class="field-with-icon"><svg viewBox="0 0 24 24"><path :d="iconPath('search')" /></svg><input v-model="recipeSearchQuery" type="search" placeholder="레시피 검색..." /></label>
          <select v-model="recipeSortBy"><option value="match">일치율 높은 순</option><option value="time">조리시간 짧은 순</option></select>
        </section>
        <div class="card-grid">
          <button v-for="recipe in filteredRecipes" :key="recipe.id" class="recipe-card" type="button" @click="store.openRecipe(recipe.id)">
            <span class="recipe-art" :class="recipe.gradient"><svg viewBox="0 0 24 24"><path :d="iconPath('chef')" /></svg><em>{{ recipe.matchRate }}%</em></span>
            <span class="recipe-content">
              <strong>{{ recipe.name }}</strong>
              <small>{{ recipe.description }}</small>
              <span class="recipe-meta"><span><svg viewBox="0 0 24 24"><path :d="iconPath('clock')" /></svg>{{ recipe.cookTime }}분</span><span><svg viewBox="0 0 24 24"><path :d="iconPath('users')" /></svg>{{ recipe.servings }}인분</span><span>{{ recipe.difficulty }}</span></span>
              <span class="ingredient-group"><b v-for="ingredient in recipe.availableIngredients" :key="ingredient" class="chip green">{{ ingredient }}</b><b v-for="ingredient in recipe.missingIngredients" :key="ingredient" class="chip gray">{{ ingredient }}</b></span>
            </span>
          </button>
        </div>
        <p v-if="filteredRecipes.length === 0" class="empty-text">검색 결과가 없습니다.</p>
      </section>

      <section v-else-if="currentView === 'recipeDetail' && selectedRecipe" class="container recipe-detail page">
        <button class="back-btn" type="button" @click="store.go('recipes')"><svg viewBox="0 0 24 24"><path :d="iconPath('back')" /></svg>목록으로 돌아가기</button>
        <article class="recipe-hero" :class="selectedRecipe.gradient">
          <div>
            <h1>{{ selectedRecipe.name }}</h1>
            <p>{{ selectedRecipe.description }}</p>
          </div>
          <button class="round-btn" type="button" :class="{ active: favoriteRecipeIds.includes(selectedRecipe.id) }" @click="store.toggleFavoriteRecipe(selectedRecipe.id)">
            <svg viewBox="0 0 24 24"><path :d="iconPath('heart')" /></svg>
          </button>
          <div class="hero-meta"><span>{{ selectedRecipe.cookTime }}분</span><span>{{ selectedRecipe.servings }}인분</span><span>{{ selectedRecipe.calories }} kcal</span><strong>재료 일치율 {{ selectedRecipe.matchRate }}%</strong></div>
        </article>
        <div class="recipe-layout">
          <section class="panel padded">
            <h2>재료</h2>
            <div class="ingredient-list">
              <span v-for="ingredient in [...selectedRecipe.availableIngredients, ...selectedRecipe.missingIngredients]" :key="ingredient" :class="{ available: selectedRecipe.availableIngredients.includes(ingredient) }">
                {{ ingredient }} <b>{{ selectedRecipe.availableIngredients.includes(ingredient) ? '보유' : '필요' }}</b>
              </span>
            </div>
          </section>
          <section class="panel padded">
            <h2>조리 과정</h2>
            <ol class="steps"><li v-for="step in selectedRecipe.steps" :key="step">{{ step }}</li></ol>
          </section>
          <aside class="panel padded">
            <h2>조리 팁</h2>
            <p v-for="tip in selectedRecipe.tips" :key="tip" class="tip-text">{{ tip }}</p>
            <button class="primary-btn full-width" type="button">조리 시작하기</button>
          </aside>
        </div>
      </section>

      <section v-else-if="currentView === 'notifications'" class="container medium page">
        <div class="page-head">
          <div>
            <h1>알림</h1>
            <p>{{ unreadCount ? `${unreadCount}개의 읽지 않은 알림이 있습니다` : '모든 알림을 확인했습니다' }}</p>
          </div>
          <button v-if="unreadCount" class="ghost-btn" type="button" @click="store.markAllAsRead"><svg viewBox="0 0 24 24"><path :d="iconPath('check')" /></svg>모두 읽음 표시</button>
        </div>

        <section class="filter-tabs">
          <button type="button" :class="{ active: notificationFilter === 'all' }" @click="notificationFilter = 'all'">전체 ({{ notifications.length }})</button>
          <button type="button" :class="{ active: notificationFilter === 'expired' }" @click="notificationFilter = 'expired'">만료됨</button>
          <button type="button" :class="{ active: notificationFilter === 'expiry' }" @click="notificationFilter = 'expiry'">임박</button>
          <button type="button" :class="{ active: notificationFilter === 'recipe' }" @click="notificationFilter = 'recipe'">레시피</button>
          <button type="button" :class="{ active: notificationFilter === 'inventory' }" @click="notificationFilter = 'inventory'">재고</button>
        </section>

        <div class="notification-list">
          <article v-for="notification in filteredNotifications" :key="notification.id" class="notification-card" :class="{ unread: !notification.read }">
            <span class="icon-box" :class="notification.type"><svg viewBox="0 0 24 24"><path :d="iconPath(notification.type === 'recipe' ? 'chef' : notification.type === 'inventory' ? 'package' : 'alert')" /></svg></span>
            <div>
              <h3>{{ notification.title }}<i v-if="!notification.read"></i></h3>
              <p>{{ notification.message }}</p>
              <small>{{ notification.time }}</small>
            </div>
            <em :class="`badge ${notification.type}`">{{ notificationLabel(notification.type) }}</em>
            <span class="notification-actions">
              <button v-if="!notification.read" type="button" title="읽음 표시" @click="store.markAsRead(notification.id)"><svg viewBox="0 0 24 24"><path :d="iconPath('check')" /></svg></button>
              <button type="button" title="삭제" @click="store.deleteNotification(notification.id)"><svg viewBox="0 0 24 24"><path :d="iconPath('trash')" /></svg></button>
            </span>
          </article>
        </div>

        <p v-if="filteredNotifications.length === 0" class="empty-text">알림이 없습니다.</p>
      </section>
    </main>
  </div>
</template>
