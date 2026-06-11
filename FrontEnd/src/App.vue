<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useFridgeStore } from './stores/fridge'
import SiteHeader from './components/SiteHeader.vue'
import DashboardView from './views/DashboardView.vue'
import InventoryView from './views/InventoryView.vue'
import AddView from './views/AddView.vue'
import DetailView from './views/DetailView.vue'
import RecipesView from './views/RecipesView.vue'
import RecipeDetailView from './views/RecipeDetailView.vue'
import NotificationsView from './views/NotificationsView.vue'
import SiteFooter from './components/SiteFooter.vue'

const store = useFridgeStore()
const { currentView } = storeToRefs(store)
</script>

<template>
  <div class="app-shell">
    <SiteHeader />
    <main>
      <DashboardView v-if="currentView === 'dashboard'" />
      <InventoryView v-else-if="currentView === 'inventory'" />
      <AddView v-else-if="currentView === 'add'" />
      <DetailView v-else-if="currentView === 'detail'" />
      <RecipesView v-else-if="currentView === 'recipes'" />
      <RecipeDetailView v-else-if="currentView === 'recipeDetail'" />
      <NotificationsView v-else-if="currentView === 'notifications'" />
    </main>
    <SiteFooter />
  </div>
</template>
