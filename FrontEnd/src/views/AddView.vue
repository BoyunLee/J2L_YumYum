<template>
  <section class="container narrow page">
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
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useFridgeStore, categories, locations, units, type InventoryForm } from '../stores/fridge'
import { iconPath } from '../utils/uiHelpers'

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

function submitAdd() {
  store.addInventory({ ...addForm })
  Object.assign(addForm, emptyForm())
}
</script>
