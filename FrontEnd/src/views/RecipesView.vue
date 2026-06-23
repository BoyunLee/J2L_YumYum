<template>
  <section class="container page">
    <div class="page-head">
      <div>
        <h1>레시피 추천</h1>
        <p>냉장고 재고를 기반으로 만들 수 있는 요리를 추천해드려요</p>
      </div>
      <button class="primary-btn recommendation-btn" type="button" :disabled="isRecommendingRecipes || isLoadingSavedRecipes || inventoryWithStatus.length === 0" @click="refreshRecommendations(true)">
        {{ isRecommendingRecipes ? '추천을 만들고 있어요…' : 'AI 추천 새로 받기' }}
      </button>
    </div>
    <p v-if="recipeRecommendationError" class="form-error recommendation-error" role="alert">{{ recipeRecommendationError }}</p>
    <p v-if="inventoryWithStatus.length === 0" class="empty-text recommendation-notice">냉장고에 재료를 추가하면 맞춤 레시피를 추천해드려요.</p>
    <section v-if="recipes.length > 0" class="filter-panel">
      <label class="field-with-icon"><svg viewBox="0 0 24 24"><path :d="iconPath('search')" /></svg><input v-model="recipeSearchQuery" type="search" placeholder="레시피 검색..." /></label>
      <select v-model="recipeSortBy"><option value="match">일치율 높은 순</option><option value="time">조리시간 짧은 순</option></select>
    </section>
    <div v-if="isRecommendingRecipes || isLoadingSavedRecipes" class="recommendation-loading" role="status">
      <span class="callback-spinner" aria-hidden="true"></span>
      <p>{{ isRecommendingRecipes ? '보유 재료와 유통기한을 살펴보고 있어요…' : '저장된 추천을 불러오고 있어요…' }}</p>
    </div>
    <div v-else-if="recipes.length > 0" class="card-grid">
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
    <p v-if="recipes.length > 0 && filteredRecipes.length === 0" class="empty-text">검색 결과가 없습니다.</p>
    <div v-else-if="!isRecommendingRecipes && !isLoadingSavedRecipes && inventoryWithStatus.length > 0 && recipes.length === 0" class="recommendation-empty">
      <svg viewBox="0 0 24 24" aria-hidden="true"><path :d="iconPath('chef')" /></svg>
      <strong>{{ hasRequestedRecipes ? '추천 결과가 없습니다.' : 'AI가 냉장고 재료를 분석할 준비가 됐어요.' }}</strong>
      <p>{{ recipeRecommendationError ? '오류 내용을 확인한 뒤 다시 시도해 주세요.' : 'AI 추천 새로 받기를 눌러 맞춤 레시피를 받아보세요.' }}</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useFridgeStore } from '../stores/fridge'
import { iconPath } from '../utils/uiHelpers'

const store = useFridgeStore()
const { hasRequestedRecipes, inventoryWithStatus, isLoadingSavedRecipes, isRecommendingRecipes, recipeRecommendationError, recipes } = storeToRefs(store)

const recipeSearchQuery = ref('')
const recipeSortBy = ref<'match' | 'time'>('match')

const filteredRecipes = computed(() =>
  recipes.value
    .filter((recipe) => recipe.name.toLowerCase().includes(recipeSearchQuery.value.trim().toLowerCase()))
    .sort((a, b) => (recipeSortBy.value === 'match' ? b.matchRate - a.matchRate : a.cookTime - b.cookTime)),
)

async function refreshRecommendations(force = false) {
  try {
    await store.recommendRecipes(force)
  } catch {
    // The store exposes a user-friendly error message in the view.
  }
}

onMounted(async () => {
  try {
    await store.loadSavedRecipes()
  } catch {
    // The store exposes a user-friendly error message in the view.
  }
})
</script>
