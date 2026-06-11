import { useAuthStore } from '../store/authStore'

const defaultHeaders: HeadersInit = {
  Accept: 'application/json',
}

function authHeaders(): HeadersInit {
  const token = useAuthStore.getState().accessToken
  if (!token) return {}
  return { Authorization: `Bearer ${token}` }
}

export async function fetchJson<T>(path: string, init?: RequestInit): Promise<T> {
  const doFetch = () =>
    fetch(path, {
      ...init,
      headers: {
        ...defaultHeaders,
        ...authHeaders(),
        ...(init?.headers ?? {}),
      },
    })

  let res = await doFetch()
  if (res.status === 401 && useAuthStore.getState().refreshToken) {
    const ok = await useAuthStore.getState().refreshSession()
    if (ok) {
      res = await doFetch()
    }
  }

  if (!res.ok) {
    const text = await res.text()
    try {
      const json = JSON.parse(text) as { message?: string; error?: string }
      throw new Error(json.message || json.error || text || `HTTP ${res.status}`)
    } catch (e) {
      if (e instanceof Error && e.message !== text) {
        throw e
      }
      throw new Error(text || `HTTP ${res.status}`)
    }
  }
  if (res.status === 204) {
    return undefined as T
  }
  return res.json() as Promise<T>
}

/** @deprecated tenant is taken from JWT */
export function tenantQs(_tenantId?: number) {
  return ''
}
