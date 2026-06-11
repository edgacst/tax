import { fetchJson } from './http'
import type { CertificateMeta } from '../types/domain'
import { useAuthStore } from '../store/authStore'

type CertificateDto = {
  id: number
  type: string
  subject: string
  validTo: string
  status: string
}

function toCert(d: CertificateDto): CertificateMeta {
  return {
    id: String(d.id),
    type: d.type,
    subject: d.subject,
    validTo: d.validTo,
    status: d.status as CertificateMeta['status'],
  }
}

export async function listCertificates(): Promise<CertificateMeta[]> {
  const rows = await fetchJson<CertificateDto[]>('/api/v1/certificates')
  return rows.map(toCert)
}

export async function uploadCertificate(file: File, password: string): Promise<CertificateMeta> {
  const form = new FormData()
  form.append('file', file)
  form.append('password', password)

  const token = useAuthStore.getState().accessToken
  const res = await fetch('/api/v1/certificates', {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: form,
  })
  if (!res.ok) {
    const text = await res.text()
    try {
      const json = JSON.parse(text) as { message?: string }
      throw new Error(json.message || text)
    } catch (e) {
      if (e instanceof Error && e.message !== text) throw e
      throw new Error(text || '업로드에 실패했습니다.')
    }
  }
  return toCert((await res.json()) as CertificateDto)
}

export async function deleteCertificate(id: string): Promise<void> {
  await fetchJson<void>(`/api/v1/certificates/${id}`, { method: 'DELETE' })
}
