<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useAuthStore, type ActivityLevel, type Gender, type UserProfile } from '../stores/auth'

const auth = useAuthStore()
const loading = ref(true)
const saving = ref(false)
const message = ref('')
const error = ref('')
const showWithdrawal = ref(false)
const withdrawalText = ref('')
const isWithdrawalConfirmed = computed(() => withdrawalText.value.trim() === '탈퇴합니다')
const form = reactive({ nickname: '', email: '', gender: 'OTHER' as Gender, birthDate: '', heightCm: 170, weightKg: 65, activityLevel: 'MEDIUM' as ActivityLevel })

function fillForm(profile: UserProfile) {
  Object.assign(form, profile, { heightCm: Number(profile.heightCm), weightKg: Number(profile.weightKg) })
}

onMounted(async () => {
  try { fillForm(await auth.fetchProfile()) }
  catch (e) { error.value = e instanceof Error ? e.message : '내 정보를 불러오지 못했습니다.' }
  finally { loading.value = false }
})

async function save() {
  saving.value = true; error.value = ''; message.value = ''
  try { fillForm(await auth.updateProfile({ ...form })); message.value = '내 정보가 저장되었습니다.' }
  catch (e) { error.value = e instanceof Error ? e.message : '내 정보를 저장하지 못했습니다.' }
  finally { saving.value = false }
}

function syncWithdrawalText(event: Event) {
  withdrawalText.value = (event.target as HTMLInputElement).value
}

function withdraw() {
  if (auth.profile && isWithdrawalConfirmed.value) auth.unlink(auth.profile.provider)
}
</script>

<template>
  <section class="page container medium my-page">
    <div class="page-head"><div><h1>마이페이지</h1><p>기본 정보와 건강 정보를 관리할 수 있어요.</p></div></div>
    <div v-if="loading" class="panel padded">내 정보를 불러오는 중입니다.</div>
    <template v-else>
      <form class="panel padded form-grid" @submit.prevent="save">
        <div v-if="auth.profile" class="profile-summary full">
          <img v-if="auth.profile.profileImageUrl" :src="auth.profile.profileImageUrl" alt="프로필 이미지" />
          <div v-else class="profile-placeholder">{{ form.nickname.slice(0, 1) || '?' }}</div>
          <div><strong>{{ form.nickname }}</strong><small>{{ auth.profile.provider }} 계정으로 연결됨</small></div>
        </div>
        <label class="form-field">닉네임<input v-model.trim="form.nickname" required maxlength="100" /></label>
        <label class="form-field">이메일<input v-model.trim="form.email" required type="email" maxlength="255" /></label>
        <label class="form-field">성별<select v-model="form.gender"><option value="M">남성</option><option value="F">여성</option><option value="OTHER">기타</option></select></label>
        <label class="form-field">생년월일<input v-model="form.birthDate" required type="date" /></label>
        <label class="form-field">키 (cm)<input v-model.number="form.heightCm" required type="number" min="50" max="300" step="0.1" /></label>
        <label class="form-field">몸무게 (kg)<input v-model.number="form.weightKg" required type="number" min="10" max="500" step="0.1" /></label>
        <label class="form-field full">활동량<select v-model="form.activityLevel"><option value="LOW">적음</option><option value="MEDIUM">보통</option><option value="HIGH">많음</option></select></label>
        <p v-if="message" class="form-success full">{{ message }}</p>
        <p v-if="error" class="form-error full">{{ error }}</p>
        <div class="button-row full"><button class="primary-btn" type="submit" :disabled="saving">{{ saving ? '저장 중...' : '변경사항 저장' }}</button></div>
      </form>
      <section class="panel padded withdrawal-panel">
        <h2>회원 탈퇴</h2>
        <p>냉장고 데이터가 더 이상 표시되지 않으며, 연결된 소셜 계정의 앱 연결도 해제됩니다.</p>
        <button v-if="!showWithdrawal" class="danger-btn" type="button" @click="showWithdrawal = true">회원 탈퇴</button>
        <div v-else class="withdrawal-confirm">
          <label class="form-field">확인을 위해 <strong>탈퇴합니다</strong>를 입력해 주세요.<input :value="withdrawalText" autocomplete="off" @input="syncWithdrawalText" @compositionend="syncWithdrawalText" /></label>
          <div class="button-row">
            <button class="ghost-btn" type="button" @click="showWithdrawal = false; withdrawalText = ''">취소</button>
            <button class="danger-btn" type="button" :disabled="!isWithdrawalConfirmed" @click="withdraw">연결 해제 및 탈퇴</button>
          </div>
        </div>
      </section>
    </template>
  </section>
</template>
