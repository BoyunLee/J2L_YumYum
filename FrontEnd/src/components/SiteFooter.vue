<template>
  <footer class="site-footer">
    <div class="container footer-inner">
      <nav class="nav-list footer-nav" aria-label="하단 메뉴">
        <button
          v-for="item in navItems"
          :key="item.view"
          type="button"
          class="nav-item footer-item"
          :class="{ active: currentView === item.view || (item.view === 'inventory' && ['add', 'detail'].includes(currentView)) || (item.view === 'recipes' && currentView === 'recipeDetail') }"
          @click="store.go(item.view)"
        >
          <span class="footer-icon"><svg viewBox="0 0 24 24"><path :d="iconPath(item.icon)" /></svg></span>
          <small>{{ item.label }}</small>
          <b v-if="item.view === 'notifications' && unreadCount">{{ unreadCount }}</b>
        </button>
      </nav>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useFridgeStore } from '../stores/fridge'
import { iconPath } from '../utils/uiHelpers'

const store = useFridgeStore()
const { currentView, unreadCount } = storeToRefs(store)

const navItems = [
  { view: 'dashboard' as const, label: '대시보드', icon: 'home' },
  { view: 'inventory' as const, label: '재고 관리', icon: 'package' },
  { view: 'recipes' as const, label: '레시피', icon: 'chef' },
  { view: 'notifications' as const, label: '알림', icon: 'bell' },
]
</script>
