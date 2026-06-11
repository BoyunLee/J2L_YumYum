<template>
  <section class="container medium page">
    <div class="page-head">
      <div>
        <h1>알림</h1>
        <p>{{ unreadCount ? `${unreadCount}개의 읽지 않은 알림이 있습니다` : '모든 알림을 확인했습니다' }}</p>
      </div>
      <button v-if="unreadCount" class="ghost-btn" type="button" @click="store.markAllAsRead"><svg viewBox="0 0 24 24"><path :d="iconPath('check')" /></svg>모두 읽음 표시</button>
    </div>

    <section class="filter-tabs">
      <button type="button" :class="{ active: notificationFilter === 'all' }" @click="notificationFilter = 'all'">전체 ({{ notifications.length }})</button>
      <button type="button" :class="{ active: notificationFilter === 'expired' }" @click="notificationFilter = 'expired'">만료됨</button>
      <button type="button" :class="{ active: notificationFilter === 'expiry' }" @click="notificationFilter = 'expiry'">임박</button>
      <button type="button" :class="{ active: notificationFilter === 'recipe' }" @click="notificationFilter = 'recipe'">레시피</button>
      <button type="button" :class="{ active: notificationFilter === 'inventory' }" @click="notificationFilter = 'inventory'">재고</button>
    </section>

    <div class="notification-list">
      <article v-for="notification in filteredNotifications" :key="notification.id" class="notification-card" :class="{ unread: !notification.read }">
        <span class="icon-box" :class="notification.type"><svg viewBox="0 0 24 24"><path :d="iconPath(notification.type === 'recipe' ? 'chef' : notification.type === 'inventory' ? 'package' : 'alert')" /></svg></span>
        <div>
          <h3>{{ notification.title }}<i v-if="!notification.read"></i></h3>
          <p>{{ notification.message }}</p>
          <small>{{ notification.time }}</small>
        </div>
        <em :class="`badge ${notification.type}`">{{ notificationLabel(notification.type) }}</em>
        <span class="notification-actions">
          <button v-if="!notification.read" type="button" title="읽음 표시" @click="store.markAsRead(notification.id)"><svg viewBox="0 0 24 24"><path :d="iconPath('check')" /></svg></button>
          <button type="button" title="삭제" @click="store.deleteNotification(notification.id)"><svg viewBox="0 0 24 24"><path :d="iconPath('trash')" /></svg></button>
        </span>
      </article>
    </div>

    <p v-if="filteredNotifications.length === 0" class="empty-text">알림이 없습니다.</p>
  </section>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useFridgeStore } from '../stores/fridge'
import { iconPath, notificationLabel } from '../utils/uiHelpers'

const store = useFridgeStore()
const { notifications, unreadCount } = storeToRefs(store)

const notificationFilter = ref<'all' | string>('all')

const filteredNotifications = computed(() =>
  notifications.value.filter((notification) => notificationFilter.value === 'all' || notification.type === notificationFilter.value),
)
</script>
