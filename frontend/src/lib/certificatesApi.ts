import { fetchJson, tenantQs } from './http'
import type { CertificateMeta } from '../types/domain'

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

export async function listCertificates(tenantId?: number): Promise<CertificateMeta[]> {
  const rows = await fetchJson<CertificateDto[]>(`/api/v1/certificates${tenantQs(tenantId)}`)
  return rows.map(toCert)
}
