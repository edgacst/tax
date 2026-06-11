import { fetchJson, tenantQs } from './http'
import type { Workplace } from '../types/domain'

type WorkplaceDto = {
  id: number
  name: string
  bizNo: string
  isDefault: boolean
  address: string
  ceoName: string
  bizType: string
  bizItem: string
  phone: string
  email: string
}

function toWorkplace(d: WorkplaceDto): Workplace {
  return {
    id: String(d.id),
    name: d.name,
    bizNo: d.bizNo,
    default: d.isDefault,
    address: d.address ?? '',
    ceoName: d.ceoName ?? '',
    bizType: d.bizType ?? '',
    bizItem: d.bizItem ?? '',
    phone: d.phone ?? '',
    email: d.email ?? '',
  }
}

export type SaveWorkplaceBody = {
  name: string
  bizNo: string
  address?: string
  ceoName?: string
  bizType?: string
  bizItem?: string
  phone?: string
  email?: string
  isDefault?: boolean
}

function toSavePayload(body: SaveWorkplaceBody) {
  return {
    name: body.name,
    bizNo: body.bizNo,
    address: body.address ?? '',
    ceoName: body.ceoName ?? '',
    bizType: body.bizType ?? '',
    bizItem: body.bizItem ?? '',
    phone: body.phone ?? '',
    email: body.email ?? '',
    isDefault: body.isDefault ?? false,
  }
}

export async function listWorkplaces(tenantId?: number): Promise<Workplace[]> {
  const rows = await fetchJson<WorkplaceDto[]>(`/api/v1/workplaces${tenantQs(tenantId)}`)
  return rows.map(toWorkplace)
}

export async function getWorkplace(id: string, tenantId?: number): Promise<Workplace> {
  const dto = await fetchJson<WorkplaceDto>(`/api/v1/workplaces/${id}${tenantQs(tenantId)}`)
  return toWorkplace(dto)
}

export async function createWorkplace(body: SaveWorkplaceBody, tenantId?: number): Promise<Workplace> {
  const dto = await fetchJson<WorkplaceDto>(`/api/v1/workplaces${tenantQs(tenantId)}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(toSavePayload(body)),
  })
  return toWorkplace(dto)
}

export async function updateWorkplace(
  id: string,
  body: SaveWorkplaceBody,
  tenantId?: number,
): Promise<Workplace> {
  const dto = await fetchJson<WorkplaceDto>(`/api/v1/workplaces/${id}${tenantQs(tenantId)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(toSavePayload(body)),
  })
  return toWorkplace(dto)
}
