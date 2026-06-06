const defaultHeaders: HeadersInit = {
  Accept: 'application/json',
}

export async function fetchJson<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, {
    ...init,
    headers: {
      ...defaultHeaders,
      ...(init?.headers ?? {}),
    },
  })
  if (!res.ok) {
    const text = await res.text()
    throw new Error(text || `HTTP ${res.status}`)
  }
  if (res.status === 204) {
    return undefined as T
  }
  return res.json() as Promise<T>
}

export function tenantQs(tenantId?: number) {
  if (tenantId == null) return ''
  return `?tenantId=${encodeURIComponent(String(tenantId))}`
}
