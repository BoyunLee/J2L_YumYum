<template>
  <section v-if="selectedRecipe" class="container recipe-detail page">
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
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useFridgeStore } from '../stores/fridge'
import { iconPath } from '../utils/uiHelpers'

const store = useFridgeStore()
const { selectedRecipe, favoriteRecipeIds } = storeToRefs(store)
</script>
