<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useAuthStore, type OAuthProvider } from '../stores/auth'

const auth = useAuthStore()
const { error, notice } = storeToRefs(auth)

const providers: { id: OAuthProvider; label: string; className: string }[] = [
  { id: 'kakao', label: '카카오로 계속하기', className: 'kakao' },
  { id: 'naver', label: '네이버로 계속하기', className: 'naver' },
  { id: 'google', label: 'Google로 계속하기', className: 'google' },
]
</script>

<template>
  <main class="login-page">
    <section class="login-card" aria-labelledby="login-title">
      <div class="login-brand" aria-hidden="true">🥗</div>
      <p class="login-eyebrow">우리 집 식재료 도우미</p>
      <h1 id="login-title">냠냠에 오신 것을 환영해요</h1>
      <p class="login-description">
        소셜 계정으로 간편하게 로그인하고<br />냉장고 속 재료를 알뜰하게 관리해 보세요.
      </p>

      <div v-if="error" class="login-error" role="alert">{{ error }}</div>
      <div v-if="notice" class="login-notice" role="status">{{ notice }}</div>

      <div class="oauth-buttons">
        <button
          v-for="provider in providers"
          :key="provider.id"
          type="button"
          class="oauth-button"
          :class="provider.className"
          @click="auth.login(provider.id)"
        >
          <span class="oauth-logo" aria-hidden="true">
            <template v-if="provider.id === 'kakao'">K</template>
            <template v-else-if="provider.id === 'naver'">N</template>
            <template v-else>G</template>
          </span>
          <span>{{ provider.label }}</span>
        </button>
      </div>

      <p class="login-notice">로그인하면 서비스 이용약관 및 개인정보 처리방침에 동의하게 됩니다.</p>
    </section>
  </main>
</template>
