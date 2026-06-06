import { fetchJson, tenantQs } from './http'
import type { Workplace } from '../types/domain'

type WorkplaceDto = {
  id: number
  name: string
  bizNo: string
  isDefault: boolean
  address: string
}

function toWorkplace(d: WorkplaceDto): Workplace {
  return {
    id: String(d.id),
    name: d.name,
    bizNo: d.bizNo,
    default: d.isDefault,
    address: d.address ?? '',
  }
}

export async function listWorkplaces(tenantId?: number): Promise<Workplace[]> {
  const rows = await fetchJson<WorkplaceDto[]>(`/api/v1/workplaces${tenantQs(tenantId)}`)
  return rows.map(toWorkplace)
}
