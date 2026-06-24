export type AdminRole = 'SUPER_ADMIN' | 'OPERATOR'

export interface AdminProfile {
  id: number
  loginId: string
  role: AdminRole
}

export interface DashboardSummary {
  totalUsers: number
  newUsersToday: number
  activeUsers7Days: number
  totalInventory: number
  apiCallsToday: number
}

export interface RecentUser {
  id: number
  nickname: string
  email: string | null
  provider: string
  createdAt: string
  lastLoginAt: string | null
}

export interface BatchHistory {
  id: number
  jobName: string
  triggerType: 'SCHEDULED' | 'MANUAL'
  status: 'RUNNING' | 'SUCCESS' | 'FAILED'
  processedCount: number
  durationMs: number | null
  startedAt: string
  finishedAt: string | null
  errorMessage: string | null
}

export interface DashboardData {
  summary: DashboardSummary
  recentUsers: RecentUser[]
  latestBatch: BatchHistory | null
}

export interface ApiUsagePoint {
  hour: string
  ocr: number
  barcode: number
  recipeRecommendation: number
}

export interface ApiUsageData {
  hours: number
  totalCalls: number
  successfulCalls: number
  points: ApiUsagePoint[]
}

export interface NoticeHistory {
  id: number
  adminLoginId: string
  title: string
  content: string
  sentCount: number
  createdAt: string
}

export interface AuditLog {
  id: number
  adminLoginId: string
  actionType: string
  targetType: string | null
  targetId: string | null
  actionDetail: string | null
  noticeTitle: string | null
  noticeContent: string | null
  createdAt: string
}

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')
export const ADMIN_TOKEN_KEY = 'yumyum_admin_access_token'

export async function adminRequest<T>(path: string, options: RequestInit = {}, token?: string | null): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })
  const body = await response.json().catch(() => null)
  if (!response.ok) {
    const error = new Error(body?.message ?? body?.msg ?? '요청을 처리하지 못했습니다.') as Error & { status: number }
    error.status = response.status
    throw error
  }
  return body?.data as T
}
