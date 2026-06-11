import { fetchJson, tenantQs } from './http'
import type { Invoice, InvoiceStatus } from '../types/domain'

type InvoiceDto = {
  id: number
  serialNo: string
  issueDate: string
  workplaceName: string
  partnerName: string
  partnerBizNo: string
  supplyAmount: number
  tax: number
  total: number
  status: string
  direction: string
  remark?: string | null
  approvalNumber?: string | null
  submittedAt?: string | null
}

export type InvoiceItemDto = {
  id: number
  seq: number
  itemName: string
  spec?: string | null
  quantity: number
  unitPrice: number
  amount: number
  tax: number
}

export type InvoiceDetailDto = InvoiceDto & { items: InvoiceItemDto[] }

function toInvoice(d: InvoiceDto): Invoice {
  return {
    id: String(d.id),
    serialNo: d.serialNo,
    issueDate: d.issueDate,
    workplaceName: d.workplaceName,
    partnerName: d.partnerName,
    partnerBizNo: d.partnerBizNo,
    supplyAmount: d.supplyAmount,
    tax: d.tax,
    total: d.total,
    status: d.status as InvoiceStatus,
    direction: d.direction === 'receive' ? 'receive' : 'issue',
    remark: d.remark ?? undefined,
  }
}

export async function listInvoices(q?: string, tenantId?: number): Promise<Invoice[]> {
  const params = new URLSearchParams()
  if (tenantId != null) params.set('tenantId', String(tenantId))
  if (q?.trim()) params.set('q', q.trim())
  const qs = params.toString() ? `?${params}` : ''
  const rows = await fetchJson<InvoiceDto[]>(`/api/v1/invoices${qs}`)
  return rows.map(toInvoice)
}

export async function getInvoice(id: string, tenantId?: number): Promise<InvoiceDetailDto> {
  const base = `/api/v1/invoices/${encodeURIComponent(id)}`
  const qs = tenantId != null ? `?tenantId=${tenantId}` : ''
  return fetchJson<InvoiceDetailDto>(`${base}${qs}`)
}

export type CreateInvoiceBody = {
  workplaceId: number
  partnerId: number
  issueDate: string
  remark?: string
  direction?: 'issue' | 'receive'
  items: { itemName: string; quantity: number; unitPrice: number }[]
}

export async function createInvoiceDraft(body: CreateInvoiceBody, tenantId?: number): Promise<InvoiceDetailDto> {
  return fetchJson<InvoiceDetailDto>(`/api/v1/invoices${tenantQs(tenantId)}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      workplaceId: body.workplaceId,
      partnerId: body.partnerId,
      issueDate: body.issueDate,
      remark: body.remark ?? '',
      direction: body.direction ?? 'issue',
      items: body.items,
    }),
  })
}

export type InvoiceSubmitResponse = {
  invoice: InvoiceDetailDto
  approvalNumber: string
  submissionMode: string
  message: string
}

export async function submitInvoice(id: string, tenantId?: number): Promise<InvoiceSubmitResponse> {
  const base = `/api/v1/invoices/${encodeURIComponent(id)}/submit`
  const qs = tenantId != null ? `?tenantId=${tenantId}` : ''
  return fetchJson<InvoiceSubmitResponse>(`${base}${qs}`, { method: 'POST' })
}
