import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export type OAuthProvider = 'google' | 'naver' | 'kakao'
export type Gender = 'M' | 'F' | 'OTHER'
export type ActivityLevel = 'LOW' | 'MEDIUM' | 'HIGH'

export interface OnboardingForm {
  nickname: string
  email: string
  gender: Gender
  birthDate: string
  heightCm: number
  weightKg: number
  activityLevel: ActivityLevel
}

export interface UserProfile extends OnboardingForm {
  id: number
  provider: OAuthProvider
  profileImageUrl: string | null
}

interface TokenRefreshPayload {
  accessToken: string
  refreshToken: string
  role: string
}

const ACCESS_TOKEN_KEY = 'yumyum_access_token'
const REFRESH_TOKEN_KEY = 'yumyum_refresh_token'
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080').replace(/\/$/, '')

function getRole(token: string | null) {
  if (!token) return null
  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
    const claims = JSON.parse(atob(padded))
    const authorities = Array.isArray(claims.authorities) ? claims.authorities : []
    if (authorities.includes('ROLE_USER')) return 'USER'
    if (authorities.includes('ROLE_GUEST')) return 'GUEST'
  } catch {
    return null
  }
  return null
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem(ACCESS_TOKEN_KEY))
  const refreshToken = ref(localStorage.getItem(REFRESH_TOKEN_KEY))
  const error = ref<string | null>(null)
  const notice = ref<string | null>(null)
  const profile = ref<UserProfile | null>(null)
  const isNewUser = ref(false)
  let refreshRequest: Promise<string | null> | null = null

  const isAuthenticated = computed(() => Boolean(accessToken.value))
  const requiresOnboarding = computed(() => getRole(accessToken.value) === 'GUEST')

  function storeTokens(nextAccessToken: string, nextRefreshToken: string) {
    localStorage.setItem(ACCESS_TOKEN_KEY, nextAccessToken)
    localStorage.setItem(REFRESH_TOKEN_KEY, nextRefreshToken)
    accessToken.value = nextAccessToken
    refreshToken.value = nextRefreshToken
  }

  function login(provider: OAuthProvider) {
    error.value = null

    const callbackUrl = `${window.location.origin}/login/callback`
    const loginUrl = new URL(`/api/user/oauth2/${provider}`, API_BASE_URL)
    loginUrl.searchParams.set('mode', 'login')
    loginUrl.searchParams.set('redirect_uri', callbackUrl)
    window.location.assign(loginUrl.toString())
  }

  function completeOAuthLogin(search = window.location.search) {
    const params = new URLSearchParams(search)
    if (params.get('unlinked') === 'true') {
      logout()
      notice.value = '회원 탈퇴와 소셜 계정 연결 해제가 완료되었습니다.'
      return true
    }
    const oauthError = params.get('error')
    const nextAccessToken = params.get('access_token')
    const nextRefreshToken = params.get('refresh_token')

    if (oauthError) {
      error.value = oauthError
      return false
    }

    if (!nextAccessToken || !nextRefreshToken) {
      error.value = '로그인 정보를 확인할 수 없습니다. 다시 시도해 주세요.'
      return false
    }

    storeTokens(nextAccessToken, nextRefreshToken)
    isNewUser.value = params.get('is_new_user') === 'true'
    error.value = null
    return true
  }

  function logout() {
    localStorage.removeItem(ACCESS_TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    accessToken.value = null
    refreshToken.value = null
    isNewUser.value = false
    error.value = null
    profile.value = null
  }

  function expireSession() {
    logout()
    notice.value = '로그인이 만료되었습니다. 다시 로그인해 주세요.'
  }

  async function refreshAccessToken() {
    if (!refreshToken.value) {
      expireSession()
      return null
    }

    if (!refreshRequest) {
      refreshRequest = (async () => {
        const response = await fetch(`${API_BASE_URL}/api/users/token/refresh`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken: refreshToken.value }),
        })
        const body = await response.json().catch(() => null)
        if (!response.ok || !body?.data) {
          expireSession()
          throw new Error(body?.message ?? body?.msg ?? '로그인이 만료되었습니다. 다시 로그인해 주세요.')
        }

        const tokens = body.data as TokenRefreshPayload
        storeTokens(tokens.accessToken, tokens.refreshToken)
        return tokens.accessToken
      })().finally(() => {
        refreshRequest = null
      })
    }

    return refreshRequest
  }

  async function authorizedFetch(input: string, init: RequestInit = {}) {
    const headers = new Headers(init.headers)
    if (accessToken.value) {
      headers.set('Authorization', `Bearer ${accessToken.value}`)
    }

    let response = await fetch(input, {
      ...init,
      headers,
    })

    if (response.status !== 401) {
      return response
    }

    const nextAccessToken = await refreshAccessToken()
    if (!nextAccessToken) {
      return response
    }

    const retryHeaders = new Headers(init.headers)
    retryHeaders.set('Authorization', `Bearer ${nextAccessToken}`)
    return fetch(input, {
      ...init,
      headers: retryHeaders,
    })
  }

  async function fetchProfile() {
    const response = await authorizedFetch(`${API_BASE_URL}/api/users/me`)
    const body = await response.json().catch(() => null)
    if (response.status === 401) {
      expireSession()
      throw new Error('로그인이 만료되었습니다. 다시 로그인해 주세요.')
    }
    if (!response.ok || !body?.data) throw new Error(body?.message ?? body?.msg ?? '내 정보를 불러오지 못했습니다.')
    profile.value = body.data
    return body.data as UserProfile
  }

  async function updateProfile(form: OnboardingForm) {
    const response = await authorizedFetch(`${API_BASE_URL}/api/users/me`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form),
    })
    const body = await response.json().catch(() => null)
    if (response.status === 401) {
      expireSession()
      throw new Error('로그인이 만료되었습니다. 다시 로그인해 주세요.')
    }
    if (!response.ok || !body?.data) throw new Error(body?.message ?? body?.msg ?? '내 정보를 수정하지 못했습니다.')
    profile.value = body.data
    return body.data as UserProfile
  }

  function unlink(provider: OAuthProvider) {
    const callbackUrl = `${window.location.origin}/login/callback`
    const unlinkUrl = new URL(`/api/user/oauth2/${provider.toLowerCase()}`, API_BASE_URL)
    unlinkUrl.searchParams.set('mode', 'unlink')
    unlinkUrl.searchParams.set('redirect_uri', callbackUrl)
    window.location.assign(unlinkUrl.toString())
  }

  async function completeOnboarding(form: OnboardingForm) {
    error.value = null
    const response = await authorizedFetch(`${API_BASE_URL}/api/users/me/onboarding`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form),
    })
    const body = await response.json().catch(() => null)
    if (response.status === 401) {
      expireSession()
      throw new Error('로그인이 만료되었습니다. 다시 로그인해 주세요.')
    }
    if (!response.ok || !body?.data) {
      throw new Error(body?.msg ?? '추가 정보를 저장하지 못했습니다.')
    }

    storeTokens(body.data.accessToken, body.data.refreshToken)
    isNewUser.value = false
  }

  return {
    accessToken,
    error,
    notice,
    profile,
    isAuthenticated,
    requiresOnboarding,
    isNewUser,
    authorizedFetch,
    login,
    completeOAuthLogin,
    completeOnboarding,
    fetchProfile,
    refreshAccessToken,
    updateProfile,
    unlink,
    logout,
    expireSession,
  }
})
