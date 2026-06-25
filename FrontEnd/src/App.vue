<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useFridgeStore } from './stores/fridge'
import { useAuthStore } from './stores/auth'
import SiteHeader from './components/SiteHeader.vue'
import DashboardView from './views/DashboardView.vue'
import InventoryView from './views/InventoryView.vue'
import AddView from './views/AddBatchView.vue'
import DetailView from './views/DetailView.vue'
import MealLogsView from './views/MealLogsView.vue'
import RecipesView from './views/RecipesView.vue'
import RecipeDetailView from './views/RecipeDetailView.vue'
import NotificationsView from './views/NotificationsView.vue'
import SiteFooter from './components/SiteFooter.vue'
import LoginView from './views/LoginView.vue'
import OnboardingView from './views/OnboardingView.vue'
import MyPageView from './views/MyPageView.vue'
import AdminView from './views/AdminView.vue'

const store = useFridgeStore()
const { currentView } = storeToRefs(store)
const auth = useAuthStore()
const { isAuthenticated, requiresOnboarding } = storeToRefs(auth)
const isHandlingCallback = ref(window.location.pathname === '/login/callback')
const isAdminRoute = window.location.pathname === '/admin' || window.location.pathname.startsWith('/admin/')

onMounted(() => {
  if (!isHandlingCallback.value) return

  auth.completeOAuthLogin()
  window.history.replaceState({}, document.title, '/')
  isHandlingCallback.value = false
})
</script>

<template>
  <AdminView v-if="isAdminRoute" />
  <div v-else-if="isHandlingCallback" class="oauth-callback" role="status">
    <div class="callback-spinner" aria-hidden="true"></div>
    <p>로그인을 완료하고 있어요…</p>
  </div>
  <LoginView v-else-if="!isAuthenticated" />
  <OnboardingView v-else-if="requiresOnboarding" />
  <div v-else class="app-shell">
    <SiteHeader />
    <main>
      <DashboardView v-if="currentView === 'dashboard'" />
      <InventoryView v-else-if="currentView === 'inventory'" />
      <AddView v-else-if="currentView === 'add'" />
      <DetailView v-else-if="currentView === 'detail'" />
      <MealLogsView v-else-if="currentView === 'meals'" />
      <RecipesView v-else-if="currentView === 'recipes'" />
      <RecipeDetailView v-else-if="currentView === 'recipeDetail'" />
      <NotificationsView v-else-if="currentView === 'notifications'" />
      <MyPageView v-else-if="currentView === 'myPage'" />
    </main>
    <SiteFooter />
  </div>
</template>
