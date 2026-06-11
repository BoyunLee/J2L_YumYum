<template>
  <section v-if="selectedInventory" class="container narrow page">
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
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useFridgeStore, categories, units, locations, type InventoryForm } from '../stores/fridge'
import { iconPath, statusClass, daysLabel } from '../utils/uiHelpers'

const store = useFridgeStore()
const { selectedInventory } = storeToRefs(store)

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

const editForm = reactive<InventoryForm>(emptyForm())

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

function submitEdit() {
  if (!selectedInventory.value) return

  store.updateInventory(selectedInventory.value.id, { ...editForm })
  isEditing.value = false
}
</script>
