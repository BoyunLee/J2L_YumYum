<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useAuthStore, type OnboardingForm } from '../stores/auth'

const auth = useAuthStore()
const isSubmitting = ref(false)
const errorMessage = ref('')
const today = new Date()
const birthYear = ref('')
const birthMonth = ref('')
const birthDay = ref('')
const years = Array.from({ length: 101 }, (_, index) => today.getFullYear() - index)
const months = Array.from({ length: 12 }, (_, index) => index + 1)
const days = computed(() => {
  if (!birthYear.value || !birthMonth.value) return Array.from({ length: 31 }, (_, index) => index + 1)
  const lastDay = new Date(Number(birthYear.value), Number(birthMonth.value), 0).getDate()
  return Array.from({ length: lastDay }, (_, index) => index + 1)
})

watch(days, (availableDays) => {
  if (Number(birthDay.value) > availableDays.length) birthDay.value = ''
})
const form = reactive<OnboardingForm>({
  nickname: '',
  email: '',
  gender: 'M',
  birthDate: '',
  heightCm: 170,
  weightKg: 65,
  activityLevel: 'MEDIUM',
})

async function submit() {
  errorMessage.value = ''
  if (!birthYear.value || !birthMonth.value || !birthDay.value) {
    errorMessage.value = '생년월일을 모두 선택해 주세요.'
    return
  }

  const pad = (value: string) => value.padStart(2, '0')
  form.birthDate = `${birthYear.value}-${pad(birthMonth.value)}-${pad(birthDay.value)}`
  const todayString = [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, '0'),
    String(today.getDate()).padStart(2, '0'),
  ].join('-')
  if (form.birthDate >= todayString) {
    errorMessage.value = '생년월일은 오늘보다 이전 날짜여야 합니다.'
    return
  }

  isSubmitting.value = true
  try {
    await auth.completeOnboarding({ ...form })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '추가 정보를 저장하지 못했습니다.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <main class="onboarding-page">
    <section class="onboarding-card" aria-labelledby="onboarding-title">
      <p class="login-eyebrow">마지막 한 단계</p>
      <h1 id="onboarding-title">기본 정보를 알려주세요</h1>
      <p class="onboarding-description">맞춤형 식재료 관리와 레시피 추천에 활용됩니다.</p>

      <form class="form-grid onboarding-form" @submit.prevent="submit">
        <label class="form-field">
          닉네임
          <input v-model.trim="form.nickname" required maxlength="100" autocomplete="nickname" />
        </label>
        <label class="form-field">
          이메일
          <input v-model.trim="form.email" required type="email" maxlength="255" autocomplete="email" />
        </label>
        <label class="form-field">
          성별
          <select v-model="form.gender" required>
            <option value="M">남성</option>
            <option value="F">여성</option>
            <option value="OTHER">기타</option>
          </select>
        </label>
        <label class="form-field">
          생년월일
          <span class="birth-date-fields">
            <select v-model="birthYear" required aria-label="출생 연도">
              <option value="" disabled>연도</option>
              <option v-for="year in years" :key="year" :value="String(year)">{{ year }}년</option>
            </select>
            <select v-model="birthMonth" required aria-label="출생 월">
              <option value="" disabled>월</option>
              <option v-for="month in months" :key="month" :value="String(month)">{{ month }}월</option>
            </select>
            <select v-model="birthDay" required aria-label="출생 일">
              <option value="" disabled>일</option>
              <option v-for="day in days" :key="day" :value="String(day)">{{ day }}일</option>
            </select>
          </span>
        </label>
        <label class="form-field">
          키 (cm)
          <input v-model.number="form.heightCm" required type="number" min="50" max="300" step="0.1" />
        </label>
        <label class="form-field">
          몸무게 (kg)
          <input v-model.number="form.weightKg" required type="number" min="10" max="500" step="0.1" />
        </label>
        <label class="form-field full">
          평소 활동량
          <select v-model="form.activityLevel" required>
            <option value="LOW">낮음 — 주로 앉아서 생활해요</option>
            <option value="MEDIUM">보통 — 가벼운 운동을 해요</option>
            <option value="HIGH">높음 — 자주 운동해요</option>
          </select>
        </label>

        <p v-if="errorMessage" class="form-error full" role="alert">{{ errorMessage }}</p>
        <div class="button-row full">
          <button class="ghost-btn" type="button" @click="auth.logout">로그아웃</button>
          <button class="primary-btn" type="submit" :disabled="isSubmitting">
            {{ isSubmitting ? '저장 중…' : '입력 완료하고 시작하기' }}
          </button>
        </div>
      </form>
    </section>
  </main>
</template>
