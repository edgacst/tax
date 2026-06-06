import { fetchJson, tenantQs } from './http'
import type { Partner } from '../types/domain'

type PartnerDto = {
  id: number
  bizNo: string
  name: string
  ceo: string
  email: string
  phone: string
  favorite: boolean
}

function toPartner(d: PartnerDto): Partner {
  return {
    id: String(d.id),
    bizNo: d.bizNo,
    name: d.name,
    ceo: d.ceo ?? '',
    email: d.email ?? '',
    phone: d.phone ?? '',
    favorite: d.favorite,
  }
}

export async function listPartners(q?: string, tenantId?: number): Promise<Partner[]> {
  const params = new URLSearchParams()
  if (tenantId != null) params.set('tenantId', String(tenantId))
  if (q?.trim()) params.set('q', q.trim())
  const qs = params.toString() ? `?${params}` : ''
  const rows = await fetchJson<PartnerDto[]>(`/api/v1/partners${qs}`)
  return rows.map(toPartner)
}

export type CreatePartnerBody = {
  bizNo: string
  name: string
  ceo?: string
  email?: string
  phone?: string
  favorite?: boolean
}

export async function createPartner(body: CreatePartnerBody, tenantId?: number): Promise<Partner> {
  const dto = await fetchJson<PartnerDto>(`/api/v1/partners${tenantQs(tenantId)}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      bizNo: body.bizNo,
      name: body.name,
      ceo: body.ceo ?? '',
      email: body.email ?? '',
      phone: body.phone ?? '',
      favorite: body.favorite ?? false,
    }),
  })
  return toPartner(dto)
}
