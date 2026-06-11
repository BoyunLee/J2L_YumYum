<template>
  <header class="site-header">
    <div class="container header-inner">
      <button class="brand" type="button" @click="store.go('dashboard')">
        <span class="brand-icon">
          <svg viewBox="0 0 24 24"><path :d="iconPath('package')" /></svg>
        </span>
        <span>냉장고 관리</span>
      </button>

      <div class="header-actions">
        <button class="alarm-btn" type="button" @click="store.go('notifications')">
          <svg viewBox="0 0 24 24"><path :d="iconPath('bell')" /></svg>
          <b v-if="unreadCount">{{ unreadCount }}</b>
        </button>
      </div>

      <nav class="nav-list" aria-label="주요 메뉴">
        <button
          v-for="item in navItems"
          :key="item.view"
          type="button"
          class="nav-item"
          :class="{ active: currentView === item.view || (item.view === 'inventory' && ['add', 'detail'].includes(currentView)) || (item.view === 'recipes' && currentView === 'recipeDetail') }"
          @click="store.go(item.view)"
        >
          <svg viewBox="0 0 24 24"><path :d="iconPath(item.icon)" /></svg>
          <span>{{ item.label }}</span>
          <b v-if="item.view === 'notifications' && unreadCount">{{ unreadCount }}</b>
        </button>
      </nav>
    </div>
  </header>
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
