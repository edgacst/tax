const defaultHeaders: HeadersInit = {
  Accept: 'application/json',
}

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

function qs(tenantId?: number) {
  if (tenantId == null) return ''
  return `?tenantId=${encodeURIComponent(String(tenantId))}`
}

async function parseError(res: Response): Promise<Error> {
  const text = await res.text()
  return new Error(text || `HTTP ${res.status}`)
}

/** tenantId 생략 시 백엔드가 `tenant_demo` 테넌트로 해석합니다. */
export async function syncNtsPurchases(tenantId?: number): Promise<SyncResponseDto> {
  const res = await fetch(`/api/v1/nts/purchases/sync${qs(tenantId)}`, {
    method: 'POST',
    headers: defaultHeaders,
  })
  if (!res.ok) throw await parseError(res)
  return res.json() as Promise<SyncResponseDto>
}

export async function listNtsPurchaseReceipts(tenantId?: number): Promise<PurchaseReceiptDto[]> {
  const res = await fetch(`/api/v1/nts/purchases${qs(tenantId)}`, { headers: defaultHeaders })
  if (!res.ok) throw await parseError(res)
  return res.json() as Promise<PurchaseReceiptDto[]>
}

export async function listNtsPurchaseSyncRuns(tenantId?: number): Promise<SyncRunDto[]> {
  const res = await fetch(`/api/v1/nts/purchases/sync/runs${qs(tenantId)}`, { headers: defaultHeaders })
  if (!res.ok) throw await parseError(res)
  return res.json() as Promise<SyncRunDto[]>
}
