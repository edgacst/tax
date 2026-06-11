export type UserProfile = {
  id: number
  tenantId: number
  tenantName: string
  email: string
  name: string
  role: string
}

export type TokenResponse = {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserProfile
}

export async function loginApi(email: string, password: string): Promise<TokenResponse> {
  const res = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  if (!res.ok) {
    const text = await res.text()
    try {
      const json = JSON.parse(text) as { message?: string }
      throw new Error(json.message || '로그인에 실패했습니다.')
    } catch (e) {
      if (e instanceof Error && e.message !== text) throw e
      throw new Error(text || '로그인에 실패했습니다.')
    }
  }
  return res.json() as Promise<TokenResponse>
}

export async function refreshApi(refreshToken: string): Promise<TokenResponse> {
  const res = await fetch('/api/v1/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
  if (!res.ok) {
    throw new Error('세션이 만료되었습니다.')
  }
  return res.json() as Promise<TokenResponse>
}

export async function logoutApi(refreshToken: string | null): Promise<void> {
  if (!refreshToken) return
  await fetch('/api/v1/auth/logout', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
}

export async function meApi(accessToken: string): Promise<UserProfile> {
  const res = await fetch('/api/v1/auth/me', {
    headers: { Accept: 'application/json', Authorization: `Bearer ${accessToken}` },
  })
  if (!res.ok) {
    throw new Error('사용자 정보를 불러올 수 없습니다.')
  }
  return res.json() as Promise<UserProfile>
}
