<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  ADMIN_TOKEN_KEY,
  adminRequest,
  type AdminProfile,
  type ApiUsagePoint,
  type ApiUsageData,
  type AuditLog,
  type BatchHistory,
  type DashboardData,
  type NoticeHistory,
} from '../api/admin'

type AdminTab = 'overview' | 'usage' | 'batches' | 'notices' | 'audit'

const token = ref(localStorage.getItem(ADMIN_TOKEN_KEY))
const admin = ref<AdminProfile | null>(null)
const checkingSession = ref(Boolean(token.value))
const loading = ref(false)
const error = ref('')
const successMessage = ref('')
const loginId = ref('')
const password = ref('')
const activeTab = ref<AdminTab>('overview')
const dashboard = ref<DashboardData | null>(null)
const usage = ref<ApiUsageData | null>(null)
const batches = ref<BatchHistory[]>([])
const notices = ref<NoticeHistory[]>([])
const auditLogs = ref<AuditLog[]>([])
const noticeTitle = ref('')
const noticeContent = ref('')
const runningBatch = ref(false)
const sendingNotice = ref(false)
const selectedAuditNotice = ref<AuditLog | null>(null)

const navItems: Array<{ key: AdminTab; label: string; icon: string }> = [
  { key: 'overview', label: '대시보드', icon: '▦' },
  { key: 'usage', label: 'API 사용량', icon: '⌁' },
  { key: 'batches', label: '배치 작업', icon: '↻' },
  { key: 'notices', label: '공지사항', icon: '◉' },
  { key: 'audit', label: '감사 로그', icon: '≡' },
]

const summaryCards = computed(() => {
  const summary = dashboard.value?.summary
  if (!summary) return []
  return [
    { label: '전체 사용자', value: summary.totalUsers, hint: `오늘 +${summary.newUsersToday}`, tone: 'blue' },
    { label: '7일 활성 사용자', value: summary.activeUsers7Days, hint: '최근 로그인 기준', tone: 'green' },
    { label: '전체 등록 식품', value: summary.totalInventory, hint: `사용자당 평균 ${summary.totalUsers ? (summary.totalInventory / summary.totalUsers).toFixed(1) : 0}개`, tone: 'orange' },
    { label: '오늘 API 호출', value: summary.apiCallsToday, hint: 'OCR · 바코드 · AI 추천', tone: 'purple' },
  ]
})

const usageMax = computed(() => Math.max(1, ...(usage.value?.points.flatMap((point) => [point.ocr, point.barcode, point.recipeRecommendation]) ?? [1])))
const usageSuccessRate = computed(() => {
  if (!usage.value?.totalCalls) return 0
  return Math.round((usage.value.successfulCalls / usage.value.totalCalls) * 100)
})

function chartX(index: number) {
  const count = usage.value?.points.length ?? 0
  return count <= 1 ? 500 : 30 + (index / (count - 1)) * 940
}

function chartY(value: number) {
  return 220 - (value / usageMax.value) * 200
}

function linePoints(metric: keyof Pick<ApiUsagePoint, 'ocr' | 'barcode' | 'recipeRecommendation'>) {
  return usage.value?.points.map((point, index) => `${chartX(index)},${chartY(point[metric])}`).join(' ') ?? ''
}

function formatDate(value: string | null | undefined) {
  if (!value) return '-'
  return new Intl.DateTimeFormat('ko-KR', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).format(new Date(value))
}

function duration(value: number | null) {
  if (value == null) return '-'
  return value < 1000 ? `${value}ms` : `${(value / 1000).toFixed(1)}초`
}

function statusLabel(status: BatchHistory['status']) {
  return { RUNNING: '실행 중', SUCCESS: '완료', FAILED: '실패' }[status]
}

function actionLabel(action: string) {
  return { LOGIN: '로그인', LOGOUT: '로그아웃', SEND_NOTICE: '공지 발송', RUN_BATCH: '수동 배치 실행' }[action] ?? action
}

async function request<T>(path: string, options: RequestInit = {}) {
  try {
    return await adminRequest<T>(path, options, token.value)
  } catch (requestError) {
    if ((requestError as Error & { status?: number }).status === 401 || (requestError as Error & { status?: number }).status === 403) {
      clearSession()
      throw new Error('관리자 로그인이 만료되었습니다. 다시 로그인해 주세요.')
    }
    throw requestError
  }
}

async function login() {
  loading.value = true
  error.value = ''
  try {
    const response = await adminRequest<{ accessToken: string; admin: AdminProfile }>('/api/admin/auth/login', {
      method: 'POST',
      body: JSON.stringify({ loginId: loginId.value, password: password.value }),
    })
    token.value = response.accessToken
    admin.value = response.admin
    localStorage.setItem(ADMIN_TOKEN_KEY, response.accessToken)
    password.value = ''
    await loadAll()
  } catch (loginError) {
    error.value = loginError instanceof Error ? loginError.message : '로그인에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

async function logout() {
  try {
    await request<void>('/api/admin/auth/logout', { method: 'POST' })
  } catch {
    // 로컬 토큰은 서버 응답과 무관하게 제거합니다.
  }
  clearSession()
}

function clearSession() {
  localStorage.removeItem(ADMIN_TOKEN_KEY)
  token.value = null
  admin.value = null
  dashboard.value = null
}

async function loadAll() {
  const [dashboardData, usageData, batchData, noticeData, auditData] = await Promise.all([
    request<DashboardData>('/api/admin/dashboard'),
    request<ApiUsageData>('/api/admin/api-usage?hours=24'),
    request<BatchHistory[]>('/api/admin/batches?limit=30'),
    request<NoticeHistory[]>('/api/admin/notices?limit=30'),
    request<AuditLog[]>('/api/admin/audit-logs?limit=50'),
  ])
  dashboard.value = dashboardData
  usage.value = usageData
  batches.value = batchData
  notices.value = noticeData
  auditLogs.value = auditData
}

async function runBatch() {
  if (!window.confirm('유통기한 알림 배치를 지금 실행할까요?')) return
  runningBatch.value = true
  error.value = ''
  try {
    const result = await request<BatchHistory>('/api/admin/batches/expiration', { method: 'POST' })
    successMessage.value = `배치가 완료되어 알림 ${result.processedCount}건을 생성했습니다.`
    batches.value = await request<BatchHistory[]>('/api/admin/batches?limit=30')
    dashboard.value = await request<DashboardData>('/api/admin/dashboard')
  } catch (batchError) {
    error.value = batchError instanceof Error ? batchError.message : '배치 실행에 실패했습니다.'
  } finally {
    runningBatch.value = false
  }
}

async function sendNotice() {
  if (!noticeTitle.value.trim() || !noticeContent.value.trim()) return
  if (!window.confirm('탈퇴하지 않은 모든 사용자에게 이 공지를 발송할까요?')) return
  sendingNotice.value = true
  error.value = ''
  try {
    const result = await request<{ historyId: number; sentCount: number }>('/api/admin/notices', {
      method: 'POST',
      body: JSON.stringify({ title: noticeTitle.value, content: noticeContent.value }),
    })
    successMessage.value = `${result.sentCount}명에게 공지를 발송했습니다.`
    noticeTitle.value = ''
    noticeContent.value = ''
    notices.value = await request<NoticeHistory[]>('/api/admin/notices?limit=30')
    auditLogs.value = await request<AuditLog[]>('/api/admin/audit-logs?limit=50')
  } catch (noticeError) {
    error.value = noticeError instanceof Error ? noticeError.message : '공지 발송에 실패했습니다.'
  } finally {
    sendingNotice.value = false
  }
}

onMounted(async () => {
  if (!token.value) {
    checkingSession.value = false
    return
  }
  try {
    admin.value = await request<AdminProfile>('/api/admin/auth/me')
    await loadAll()
  } catch (sessionError) {
    error.value = sessionError instanceof Error ? sessionError.message : '관리자 세션을 확인하지 못했습니다.'
  } finally {
    checkingSession.value = false
  }
})
</script>

<template>
  <div v-if="checkingSession" class="admin-loading"><span></span><p>관리자 세션을 확인하고 있습니다.</p></div>

  <main v-else-if="!admin" class="admin-login-page">
    <form class="admin-login-card" @submit.prevent="login">
      <div class="admin-login-logo">Y</div>
      <p class="admin-eyebrow">YUMYUM CONSOLE</p>
      <h1>관리자 로그인</h1>
      <p class="admin-login-copy">서비스 운영을 위한 관리자 전용 공간입니다.</p>
      <label>아이디<input v-model.trim="loginId" autocomplete="username" maxlength="100" required placeholder="관리자 아이디" /></label>
      <label>비밀번호<input v-model="password" type="password" autocomplete="current-password" maxlength="100" required placeholder="비밀번호" /></label>
      <p v-if="error" class="admin-alert error">{{ error }}</p>
      <button class="admin-primary full" type="submit" :disabled="loading">{{ loading ? '로그인 중...' : '로그인' }}</button>
      <a href="/">← 사용자 서비스로 돌아가기</a>
    </form>
  </main>

  <div v-else class="admin-shell">
    <aside class="admin-sidebar">
      <a class="admin-brand" href="/admin"><span>Y</span><div><b>YumYum</b><small>ADMIN CONSOLE</small></div></a>
      <nav>
        <button v-for="item in navItems" :key="item.key" :class="{ active: activeTab === item.key }" @click="activeTab = item.key">
          <i>{{ item.icon }}</i>{{ item.label }}
        </button>
      </nav>
      <div class="admin-account"><span>{{ admin.loginId.slice(0, 1).toUpperCase() }}</span><div><b>{{ admin.loginId }}</b><small>{{ admin.role === 'SUPER_ADMIN' ? '최고 관리자' : '운영자' }}</small></div><button title="로그아웃" @click="logout">↪</button></div>
    </aside>

    <main class="admin-main">
      <header class="admin-topbar"><div><p>YumYum 운영 센터</p><b>{{ formatDate(new Date().toISOString()) }}</b></div><button class="admin-refresh" @click="loadAll">새로고침 ↻</button></header>
      <p v-if="error" class="admin-alert error">{{ error }} <button @click="error = ''">×</button></p>
      <p v-if="successMessage" class="admin-alert success">{{ successMessage }} <button @click="successMessage = ''">×</button></p>

      <section v-if="activeTab === 'overview'" class="admin-content">
        <div class="admin-page-head"><div><p class="admin-eyebrow">OVERVIEW</p><h1>서비스 현황</h1><p>사용자와 서비스 운영 상태를 한눈에 확인하세요.</p></div></div>
        <div class="admin-stats"><article v-for="card in summaryCards" :key="card.label" :class="card.tone"><p>{{ card.label }}</p><strong>{{ card.value.toLocaleString() }}</strong><small>{{ card.hint }}</small></article></div>
        <div class="admin-grid two">
          <section class="admin-panel"><div class="panel-head"><div><h2>최근 가입 사용자</h2><p>가장 최근 가입한 6명입니다.</p></div></div><div class="admin-table-wrap"><table><thead><tr><th>사용자</th><th>가입 경로</th><th>가입일</th></tr></thead><tbody><tr v-for="user in dashboard?.recentUsers" :key="user.id"><td><b>{{ user.nickname }}</b><small>{{ user.email || '이메일 없음' }}</small></td><td><span class="provider-pill">{{ user.provider }}</span></td><td>{{ formatDate(user.createdAt) }}</td></tr></tbody></table></div></section>
          <section class="admin-panel batch-summary"><div class="panel-head"><div><h2>최근 배치 작업</h2><p>유통기한 알림 생성 작업입니다.</p></div><button class="admin-link" @click="activeTab = 'batches'">전체 보기</button></div><template v-if="dashboard?.latestBatch"><span class="status-dot" :class="dashboard.latestBatch.status.toLowerCase()">{{ statusLabel(dashboard.latestBatch.status) }}</span><strong>{{ dashboard.latestBatch.processedCount }}건 처리</strong><dl><div><dt>실행 시각</dt><dd>{{ formatDate(dashboard.latestBatch.startedAt) }}</dd></div><div><dt>소요 시간</dt><dd>{{ duration(dashboard.latestBatch.durationMs) }}</dd></div><div><dt>실행 방식</dt><dd>{{ dashboard.latestBatch.triggerType === 'MANUAL' ? '수동' : '스케줄' }}</dd></div></dl></template><p v-else class="admin-empty">아직 실행된 배치가 없습니다.</p></section>
        </div>
      </section>

      <section v-else-if="activeTab === 'usage'" class="admin-content">
        <div class="admin-page-head"><div><p class="admin-eyebrow">API MONITORING</p><h1>API 사용량</h1><p>최근 24시간 OCR, 바코드, 레시피 추천 호출 현황입니다.</p></div></div>
        <div class="admin-stats compact"><article class="blue"><p>전체 호출</p><strong>{{ usage?.totalCalls.toLocaleString() ?? 0 }}</strong><small>최근 24시간</small></article><article class="green"><p>성공률</p><strong>{{ usageSuccessRate }}%</strong><small>{{ usage?.successfulCalls ?? 0 }}건 성공</small></article><article class="orange"><p>OCR</p><strong>{{ usage?.ocrCalls ?? 0 }}</strong><small>문서 인식</small></article><article class="blue"><p>바코드</p><strong>{{ usage?.barcodeCalls ?? 0 }}</strong><small>상품 조회</small></article><article class="purple"><p>레시피 추천</p><strong>{{ usage?.recipeRecommendationCalls ?? 0 }}</strong><small>레시피 생성</small></article></div>
        <section class="admin-panel usage-panel"><div class="panel-head"><div><h2>시간별 호출 추이</h2><p>점에 마우스를 올리면 시간별 호출 횟수를 확인할 수 있습니다.</p></div><div class="chart-legend"><span class="ocr">OCR</span><span class="barcode">바코드</span><span class="recipe">레시피 추천</span></div></div><div class="line-chart-wrap"><svg class="line-chart" viewBox="0 0 1000 240" preserveAspectRatio="none" role="img" aria-label="시간별 API 호출 꺾은선 그래프"><g class="chart-grid"><line v-for="y in [20, 70, 120, 170, 220]" :key="y" x1="30" :y1="y" x2="970" :y2="y" /></g><polyline class="chart-line ocr-line" :points="linePoints('ocr')" /><polyline class="chart-line barcode-line" :points="linePoints('barcode')" /><polyline class="chart-line recipe-line" :points="linePoints('recipeRecommendation')" /><g v-for="(point, index) in usage?.points" :key="point.hour" class="chart-points"><circle class="ocr-point" :cx="chartX(index)" :cy="chartY(point.ocr)" r="4"><title>{{ formatDate(point.hour) }} OCR {{ point.ocr }}회</title></circle><circle class="barcode-point" :cx="chartX(index)" :cy="chartY(point.barcode)" r="4"><title>{{ formatDate(point.hour) }} 바코드 {{ point.barcode }}회</title></circle><circle class="recipe-point" :cx="chartX(index)" :cy="chartY(point.recipeRecommendation)" r="4"><title>{{ formatDate(point.hour) }} 레시피 추천 {{ point.recipeRecommendation }}회</title></circle></g></svg><div class="chart-axis-labels"><small v-for="(point, index) in usage?.points" :key="point.hour">{{ index % 3 === 0 ? new Date(point.hour).getHours().toString().padStart(2, '0') + '시' : '' }}</small></div></div></section>
      </section>

      <section v-else-if="activeTab === 'batches'" class="admin-content">
        <div class="admin-page-head"><div><p class="admin-eyebrow">BATCH JOBS</p><h1>배치 작업 현황</h1><p>유통기한 알림 작업의 처리량과 실행 시간을 확인하세요.</p></div><button class="admin-primary" :disabled="runningBatch" @click="runBatch">{{ runningBatch ? '실행 중...' : '지금 실행' }}</button></div>
        <section class="admin-panel"><div class="admin-table-wrap"><table><thead><tr><th>상태</th><th>작업</th><th>실행 방식</th><th>처리량</th><th>소요 시간</th><th>시작 시각</th></tr></thead><tbody><tr v-for="batch in batches" :key="batch.id"><td><span class="status-dot" :class="batch.status.toLowerCase()">{{ statusLabel(batch.status) }}</span></td><td><b>유통기한 알림</b><small v-if="batch.errorMessage">{{ batch.errorMessage }}</small></td><td>{{ batch.triggerType === 'MANUAL' ? '수동' : '스케줄' }}</td><td>{{ batch.processedCount }}건</td><td>{{ duration(batch.durationMs) }}</td><td>{{ formatDate(batch.startedAt) }}</td></tr></tbody></table><p v-if="batches.length === 0" class="admin-empty">배치 실행 이력이 없습니다.</p></div></section>
      </section>

      <section v-else-if="activeTab === 'notices'" class="admin-content">
        <div class="admin-page-head"><div><p class="admin-eyebrow">ANNOUNCEMENTS</p><h1>공지사항 발송</h1><p>탈퇴하지 않은 모든 사용자의 알림함으로 안내를 보냅니다.</p></div></div>
        <div class="admin-grid notice-grid"><form class="admin-panel notice-form" @submit.prevent="sendNotice"><h2>새 공지 작성</h2><label>제목 <small>{{ noticeTitle.length }}/200</small><input v-model="noticeTitle" maxlength="200" required placeholder="공지 제목을 입력하세요" /></label><label>내용 <small>{{ noticeContent.length }}/2000</small><textarea v-model="noticeContent" maxlength="2000" required rows="8" placeholder="사용자에게 전달할 내용을 입력하세요"></textarea></label><p>발송 즉시 탈퇴하지 않은 전체 사용자의 알림 목록에 표시됩니다.</p><button class="admin-primary full" :disabled="sendingNotice">{{ sendingNotice ? '발송 중...' : '전체 사용자에게 발송' }}</button></form><section class="admin-panel notice-history"><div class="panel-head"><div><h2>최근 발송 이력</h2><p>최근 공지와 수신 대상 수입니다.</p></div></div><article v-for="notice in notices" :key="notice.id"><div><b>{{ notice.title }}</b><small>{{ formatDate(notice.createdAt) }} · {{ notice.adminLoginId }}</small></div><span>{{ notice.sentCount }}명</span><p>{{ notice.content }}</p></article><p v-if="notices.length === 0" class="admin-empty">발송한 공지가 없습니다.</p></section></div>
      </section>

      <section v-else class="admin-content">
        <div class="admin-page-head"><div><p class="admin-eyebrow">AUDIT TRAIL</p><h1>관리자 감사 로그</h1><p>로그인, 공지 발송, 수동 배치 실행 기록을 확인하세요.</p></div></div>
        <section class="admin-panel"><div class="admin-table-wrap"><table><thead><tr><th>관리자</th><th>작업</th><th>대상</th><th>상세</th><th>시각</th></tr></thead><tbody><tr v-for="log in auditLogs" :key="log.id"><td><b>{{ log.adminLoginId }}</b></td><td>{{ actionLabel(log.actionType) }}</td><td>{{ log.targetType || '-' }}<small v-if="log.targetId">#{{ log.targetId }}</small></td><td><button v-if="log.noticeTitle" class="audit-notice-link" type="button" @click="selectedAuditNotice = log">{{ log.noticeTitle }}</button><span v-else>{{ log.actionDetail || '-' }}</span></td><td>{{ formatDate(log.createdAt) }}</td></tr></tbody></table></div></section>
      </section>
    </main>
    <div v-if="selectedAuditNotice" class="admin-modal-backdrop" role="presentation" @click.self="selectedAuditNotice = null"><article class="admin-modal" role="dialog" aria-modal="true" aria-labelledby="audit-notice-title"><header><div><p class="admin-eyebrow">ANNOUNCEMENT</p><h2 id="audit-notice-title">{{ selectedAuditNotice.noticeTitle }}</h2></div><button type="button" aria-label="닫기" @click="selectedAuditNotice = null">×</button></header><p class="modal-meta">{{ formatDate(selectedAuditNotice.createdAt) }} · {{ selectedAuditNotice.adminLoginId }}</p><div class="modal-content">{{ selectedAuditNotice.noticeContent }}</div><footer><button class="admin-primary" type="button" @click="selectedAuditNotice = null">확인</button></footer></article></div>
  </div>
</template>

<style scoped>
.admin-shell{min-height:100svh;background:#f5f7fb;color:#172033}.admin-sidebar{position:fixed;inset:0 auto 0 0;width:15.5rem;display:flex;flex-direction:column;padding:1.5rem 1rem;background:#fff;border-right:1px solid #e6eaf0;z-index:5}.admin-brand{display:flex;gap:.75rem;align-items:center;padding:0 .5rem 2rem;color:inherit;text-decoration:none}.admin-brand>span,.admin-login-logo{display:grid;place-items:center;width:2.65rem;height:2.65rem;border-radius:.8rem;background:linear-gradient(135deg,#16a34a,#22c55e);color:#fff;font-size:1.35rem;font-weight:900;box-shadow:0 8px 20px #16a34a33}.admin-brand div{display:flex;flex-direction:column}.admin-brand b{font-size:1.1rem}.admin-brand small{color:#9aa3b2;font-size:.62rem;letter-spacing:.14em}.admin-sidebar nav{display:grid;gap:.3rem}.admin-sidebar nav button{display:flex;align-items:center;gap:.8rem;padding:.8rem .9rem;border-radius:.7rem;background:transparent;color:#697386;text-align:left;cursor:pointer;font-weight:650}.admin-sidebar nav button i{width:1.4rem;color:#8b95a7;font-style:normal;text-align:center;font-size:1.15rem}.admin-sidebar nav button:hover{background:#f4f8f5}.admin-sidebar nav button.active{background:#edf9f0;color:#15803d}.admin-sidebar nav button.active i{color:#16a34a}.admin-account{margin-top:auto;display:grid;grid-template-columns:auto 1fr auto;align-items:center;gap:.65rem;padding:.8rem .5rem;border-top:1px solid #edf0f4}.admin-account>span{display:grid;place-items:center;width:2.25rem;height:2.25rem;border-radius:50%;background:#dcfce7;color:#15803d;font-weight:800}.admin-account div{display:flex;min-width:0;flex-direction:column}.admin-account b{font-size:.85rem}.admin-account small{color:#8993a3;font-size:.7rem}.admin-account button{cursor:pointer;background:transparent;color:#8993a3;font-size:1.1rem}.admin-main{min-height:100svh;margin-left:15.5rem}.admin-topbar{height:4.4rem;display:flex;align-items:center;justify-content:space-between;padding:0 2.2rem;background:#fff;border-bottom:1px solid #e6eaf0}.admin-topbar div{display:flex;gap:.5rem;align-items:center}.admin-topbar p,.admin-topbar b{margin:0;font-size:.78rem}.admin-topbar p{color:#8993a3}.admin-refresh,.admin-link{background:transparent;color:#64748b;cursor:pointer;font-weight:650}.admin-content{max-width:90rem;margin:auto;padding:2.2rem}.admin-page-head{display:flex;align-items:flex-end;justify-content:space-between;margin-bottom:1.6rem}.admin-page-head h1{margin:.15rem 0;font-size:1.75rem;letter-spacing:-.03em}.admin-page-head p{margin:0;color:#7b8494;font-size:.88rem}.admin-eyebrow{color:#16a34a!important;font-size:.67rem!important;font-weight:800;letter-spacing:.14em}.admin-stats{display:grid;grid-template-columns:repeat(4,1fr);gap:1rem;margin-bottom:1rem}.admin-stats article{position:relative;overflow:hidden;padding:1.3rem 1.4rem;border:1px solid #e5e9ef;border-radius:1rem;background:#fff;box-shadow:0 1px 3px #0f172a08}.admin-stats article:after{content:'';position:absolute;right:-1rem;top:-1rem;width:5rem;height:5rem;border-radius:50%;opacity:.12;background:currentColor}.admin-stats p,.admin-stats strong,.admin-stats small{display:block;margin:0}.admin-stats p{color:#6b7484;font-size:.78rem;font-weight:650}.admin-stats strong{margin:.55rem 0 .3rem;color:#172033;font-size:1.8rem}.admin-stats small{color:#9199a7;font-size:.7rem}.admin-stats .blue{color:#2563eb}.admin-stats .green{color:#16a34a}.admin-stats .orange{color:#ea580c}.admin-stats .purple{color:#7c3aed}.admin-grid{display:grid;gap:1rem}.admin-grid.two{grid-template-columns:1.7fr 1fr}.admin-panel{border:1px solid #e5e9ef;border-radius:1rem;background:#fff;box-shadow:0 1px 3px #0f172a08}.panel-head{display:flex;align-items:center;justify-content:space-between;padding:1.25rem 1.35rem;border-bottom:1px solid #edf0f4}.panel-head h2,.notice-form h2{margin:0 0 .25rem;font-size:1rem}.panel-head p{margin:0;color:#8a93a2;font-size:.72rem}.admin-table-wrap{overflow:auto}table{width:100%;border-collapse:collapse;font-size:.78rem}th{padding:.75rem 1rem;background:#fafbfc;color:#7b8494;text-align:left;font-size:.68rem}td{padding:.9rem 1rem;border-top:1px solid #eff2f5;color:#596273;white-space:nowrap}td b,td small{display:block}td b{color:#283244}td small{margin-top:.2rem;color:#98a0ad;font-size:.66rem}.provider-pill{display:inline-block;padding:.25rem .45rem;border-radius:.35rem;background:#eef6ff;color:#2563eb;font-size:.65rem;font-weight:750}.batch-summary{padding-bottom:1.2rem}.batch-summary .panel-head{margin-bottom:1.2rem}.batch-summary>span,.batch-summary>strong,.batch-summary>dl{margin-left:1.35rem;margin-right:1.35rem}.batch-summary>strong{display:block;margin-top:.8rem;font-size:1.55rem}.batch-summary dl{margin-top:1rem}.batch-summary dl div{display:flex;justify-content:space-between;padding:.45rem 0;border-top:1px solid #f0f2f5;font-size:.72rem}.batch-summary dt{color:#8993a3}.batch-summary dd{margin:0;font-weight:650}.status-dot{display:inline-flex;align-items:center;gap:.35rem;padding:.3rem .55rem;border-radius:2rem;font-size:.67rem;font-weight:750}.status-dot:before{content:'';width:.38rem;height:.38rem;border-radius:50%;background:currentColor}.status-dot.success{background:#dcfce7;color:#15803d}.status-dot.failed{background:#fee2e2;color:#dc2626}.status-dot.running{background:#dbeafe;color:#2563eb}.admin-primary{padding:.72rem 1rem;border-radius:.65rem;background:#16a34a;color:#fff;cursor:pointer;font-weight:750;box-shadow:0 5px 14px #16a34a25}.admin-primary:hover{background:#15803d}.admin-primary:disabled{cursor:not-allowed;opacity:.55}.admin-primary.full{width:100%}.usage-panel{padding-bottom:1rem}.chart-legend{display:flex;gap:1rem;color:#727c8d;font-size:.7rem}.chart-legend span:before{content:'';display:inline-block;width:.5rem;height:.5rem;margin-right:.3rem;border-radius:.15rem;background:currentColor}.ocr{color:#2563eb}.barcode{color:#f59e0b}.recipe{color:#8b5cf6}.line-chart-wrap{padding:1.25rem 1.2rem .7rem}.line-chart{display:block;width:100%;height:16rem;overflow:visible}.chart-grid line{stroke:#e9edf2;stroke-width:1;vector-effect:non-scaling-stroke}.chart-line{fill:none;stroke-width:3;stroke-linejoin:round;stroke-linecap:round;vector-effect:non-scaling-stroke}.ocr-line{stroke:#2563eb}.barcode-line{stroke:#f59e0b}.recipe-line{stroke:#8b5cf6}.chart-points circle{stroke:#fff;stroke-width:2;vector-effect:non-scaling-stroke;cursor:help}.ocr-point{fill:#2563eb}.barcode-point{fill:#f59e0b}.recipe-point{fill:#8b5cf6}.chart-axis-labels{display:flex;padding:0 1.8%}.chart-axis-labels small{min-width:0;flex:1;color:#9aa2ae;text-align:center;font-size:.56rem}.notice-grid{grid-template-columns:minmax(20rem,.8fr) minmax(24rem,1.2fr)}.notice-form{padding:1.4rem}.notice-form label{position:relative;display:block;margin-top:1.1rem;color:#4e596b;font-size:.77rem;font-weight:700}.notice-form label small{position:absolute;right:0;color:#a0a7b2;font-weight:400}.notice-form input,.notice-form textarea,.admin-login-card input{width:100%;margin-top:.45rem;border:1px solid #dce1e8;border-radius:.65rem;padding:.75rem .8rem;background:#fbfcfd;outline:none;color:#172033}.notice-form textarea{resize:vertical}.notice-form input:focus,.notice-form textarea:focus,.admin-login-card input:focus{border-color:#22c55e;box-shadow:0 0 0 3px #22c55e15}.notice-form>p{margin:1rem 0;color:#8a93a2;font-size:.68rem}.notice-history{padding-bottom:.5rem}.notice-history article{display:grid;grid-template-columns:1fr auto;gap:.35rem;padding:1rem 1.3rem;border-bottom:1px solid #eff2f5}.notice-history article div{display:flex;flex-direction:column}.notice-history article b{font-size:.8rem}.notice-history article small{margin-top:.2rem;color:#98a0ad;font-size:.65rem}.notice-history article>span{align-self:start;padding:.25rem .45rem;border-radius:.4rem;background:#edf9f0;color:#15803d;font-size:.67rem;font-weight:750}.notice-history article p{grid-column:1/-1;margin:.35rem 0 0;color:#697386;font-size:.73rem;line-height:1.5}.audit-notice-link{padding:0;background:transparent;color:#2563eb;text-decoration:underline;text-underline-offset:3px;cursor:pointer;font-weight:700}.admin-modal-backdrop{position:fixed;inset:0;z-index:30;display:grid;place-items:center;padding:1rem;background:#0f172a66;backdrop-filter:blur(3px)}.admin-modal{width:min(100%,34rem);overflow:hidden;border-radius:1rem;background:#fff;box-shadow:0 25px 60px #0f172a33}.admin-modal header{display:flex;align-items:flex-start;justify-content:space-between;padding:1.3rem 1.4rem;border-bottom:1px solid #edf0f4}.admin-modal h2{margin:.2rem 0 0;font-size:1.15rem}.admin-modal header button{background:transparent;color:#7b8494;cursor:pointer;font-size:1.5rem}.modal-meta{margin:0;padding:1rem 1.4rem 0;color:#8a93a2;font-size:.7rem}.modal-content{min-height:8rem;padding:1rem 1.4rem;color:#455064;white-space:pre-wrap;line-height:1.7}.admin-modal footer{display:flex;justify-content:flex-end;padding:1rem 1.4rem;border-top:1px solid #edf0f4}.admin-alert{max-width:calc(90rem - 4.4rem);display:flex;justify-content:space-between;margin:1rem auto 0;padding:.75rem 1rem;border-radius:.65rem;font-size:.78rem}.admin-alert.error{background:#fef2f2;color:#b91c1c}.admin-alert.success{background:#f0fdf4;color:#15803d}.admin-alert button{background:transparent;color:inherit;cursor:pointer}.admin-empty{padding:2rem;color:#98a0ad;text-align:center;font-size:.78rem}.admin-login-page,.admin-loading{min-height:100svh;display:grid;place-items:center;background:radial-gradient(circle at 25% 10%,#dcfce7aa,transparent 28rem),radial-gradient(circle at 85% 90%,#dbeafe99,transparent 28rem),#f7f9fb}.admin-login-card{width:min(calc(100% - 2rem),26rem);padding:2.3rem;border:1px solid #e2e8e4;border-radius:1.35rem;background:#ffffffee;box-shadow:0 24px 60px #0f172a16}.admin-login-logo{margin:0 auto 1rem}.admin-login-card>p,.admin-login-card h1{text-align:center}.admin-login-card h1{margin:.2rem 0;font-size:1.65rem}.admin-login-copy{margin:0 0 1.7rem;color:#7b8494;font-size:.8rem}.admin-login-card label{display:block;margin:.9rem 0;color:#4e596b;font-size:.76rem;font-weight:700}.admin-login-card .admin-alert{margin:.8rem 0}.admin-login-card>a{display:block;margin-top:1.2rem;color:#798395;text-align:center;text-decoration:none;font-size:.72rem}.admin-loading{align-content:center;gap:1rem;color:#748091;font-size:.8rem}.admin-loading span{width:2rem;height:2rem;margin:auto;border:3px solid #d9e3dc;border-top-color:#16a34a;border-radius:50%;animation:spin .7s linear infinite}@keyframes spin{to{transform:rotate(360deg)}}
.admin-stats.compact{grid-template-columns:repeat(5,1fr)}
@media(max-width:900px){.admin-sidebar{position:sticky;top:0;width:100%;height:auto;display:block;padding:.7rem;overflow:auto}.admin-brand,.admin-account{display:none}.admin-sidebar nav{display:flex}.admin-sidebar nav button{white-space:nowrap}.admin-main{margin-left:0}.admin-topbar{display:none}.admin-stats,.admin-stats.compact{grid-template-columns:repeat(2,1fr)}.admin-grid.two,.notice-grid{grid-template-columns:1fr}.admin-content{padding:1.2rem}.admin-shell{display:block}}
@media(max-width:520px){.admin-stats,.admin-stats.compact{grid-template-columns:1fr}.admin-page-head{align-items:flex-start;gap:1rem;flex-direction:column}.admin-sidebar nav button{padding:.65rem}.admin-sidebar nav button i{display:none}.line-chart{height:13rem}.chart-axis-labels small{font-size:.48rem}}
</style>
