import { fetchJson, tenantQs } from './http'

export type NtsSyncRunStatus = 'RUNNING' | 'SUCCESS' | 'FAILED'

export type PurchaseReceiptDto = {
  id: number
  ntsApprovalNumber: string
  issueDate: string
  supplierBizNo: string
  supplierName: string
  buyerBizNo: string
  supplyAmount: number
  taxAmount: number
  totalAmount: number
  syncedAt: string
  syncRunId: number | null
}

export type SyncResponseDto = {
  syncRunId: number
  recordsFetched: number
  recordsInserted: number
  message: string
}

export type SyncRunDto = {
  id: number
  status: NtsSyncRunStatus
  recordsFetched: number
  recordsInserted: number
  errorMessage: string | null
  startedAt: string
  completedAt: string | null
}

/** tenantId 생략 시 백엔드가 `tenant_demo` 테넌트로 해석합니다. */
export async function syncNtsPurchases(tenantId?: number): Promise<SyncResponseDto> {
  return fetchJson<SyncResponseDto>(`/api/v1/nts/purchases/sync${tenantQs(tenantId)}`, { method: 'POST' })
}

export async function listNtsPurchaseReceipts(tenantId?: number): Promise<PurchaseReceiptDto[]> {
  return fetchJson<PurchaseReceiptDto[]>(`/api/v1/nts/purchases${tenantQs(tenantId)}`)
}

export async function listNtsPurchaseSyncRuns(tenantId?: number): Promise<SyncRunDto[]> {
  return fetchJson<SyncRunDto[]>(`/api/v1/nts/purchases/sync/runs${tenantQs(tenantId)}`)
}
