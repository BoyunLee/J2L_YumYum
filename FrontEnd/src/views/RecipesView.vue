<template>
  <section class="container page">
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
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useFridgeStore } from '../stores/fridge'
import { iconPath } from '../utils/uiHelpers'

const store = useFridgeStore()
const { recipes } = storeToRefs(store)

const recipeSearchQuery = ref('')
const recipeSortBy = ref<'match' | 'time'>('match')

const filteredRecipes = computed(() =>
  recipes.value
    .filter((recipe) => recipe.name.toLowerCase().includes(recipeSearchQuery.value.trim().toLowerCase()))
    .sort((a, b) => (recipeSortBy.value === 'match' ? b.matchRate - a.matchRate : a.cookTime - b.cookTime)),
)
</script>
