<template>
  <section class="container page">
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
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useFridgeStore, categories } from '../stores/fridge'
import { iconPath, statusClass, daysLabel } from '../utils/uiHelpers'

const store = useFridgeStore()
const { recentInventory, notifications, recipes, stats } = storeToRefs(store)

const categoryLabels = computed(() =>
  Object.fromEntries(categories.map((category) => [category.value, category.label])) as Record<string, string>,
)

const dashboardNotifications = computed(() => notifications.value.slice(0, 3))
const recommendedRecipes = computed(() => recipes.value.slice(0, 2))
</script>
