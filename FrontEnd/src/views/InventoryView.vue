<template>
  <section class="container page">
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
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useFridgeStore, categories } from '../stores/fridge'
import { iconPath, statusClass, daysLabel } from '../utils/uiHelpers'

const store = useFridgeStore()
const { inventoryWithStatus } = storeToRefs(store)

const searchQuery = ref('')
const selectedCategory = ref<'all' | string>('all')

const categoryLabels = computed(() =>
  Object.fromEntries(categories.map((category) => [category.value, category.label])) as Record<string, string>,
)

const filteredInventory = computed(() =>
  inventoryWithStatus.value.filter((item) => {
    const matchesSearch = item.name.toLowerCase().includes(searchQuery.value.trim().toLowerCase())
    const matchesCategory = selectedCategory.value === 'all' || item.category === selectedCategory.value
    return matchesSearch && matchesCategory
  }),
)
</script>
