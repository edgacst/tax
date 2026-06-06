import { fetchJson } from './http'

export async function fetchPing(): Promise<{ status: string; service: string }> {
  return fetchJson<{ status: string; service: string }>('/api/v1/public/ping')
}
