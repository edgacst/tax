const defaultHeaders: HeadersInit = {
  Accept: 'application/json',
}

export async function fetchPing(): Promise<{ status: string; service: string }> {
  const res = await fetch('/api/v1/public/ping', { headers: defaultHeaders })
  if (!res.ok) {
    const text = await res.text()
    throw new Error(text || `HTTP ${res.status}`)
  }
  return res.json() as Promise<{ status: string; service: string }>
}
