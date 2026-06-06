import { fetchJson, tenantQs } from './http'
import type { InvoiceStatus } from '../types/domain'

export type DashboardSummary = {
  approvedCount: number
  draftCount: number
  approvedSalesTotal: number
  partnerCount: number
  recentInvoices: {
    id: number
    serialNo: string
    issueDate: string
    partnerName: string
    total: number
    status: InvoiceStatus
    direction: 'issue' | 'receive'
  }[]
}

export async function fetchDashboardSummary(tenantId?: number): Promise<DashboardSummary> {
  return fetchJson<DashboardSummary>(`/api/v1/dashboard/summary${tenantQs(tenantId)}`)
}
