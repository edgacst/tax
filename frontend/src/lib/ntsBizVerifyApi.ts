import { fetchJson } from './http'

export type NtsBizVerifyMeta = {
  configured: boolean
  enabled: boolean
  baseUrl: string
}

export type BizStatusItem = {
  bizNo: string
  businessStatus: string
  businessStatusCode: string
  taxType: string
  taxTypeCode: string
  endDate: string
  utccYn: string
  registered: boolean
}

export type BizStatusResponse = {
  statusCode: string
  items: BizStatusItem[]
}

export type BizValidateItem = {
  bizNo: string
  valid: boolean
  validCode: string
  message: string
}

export type BizValidateResponse = {
  statusCode: string
  items: BizValidateItem[]
}

export async function fetchNtsBizVerifyMeta(): Promise<NtsBizVerifyMeta> {
  return fetchJson<NtsBizVerifyMeta>('/api/v1/nts/biz/meta')
}

export async function queryBizStatus(bizNumbers: string[]): Promise<BizStatusResponse> {
  return fetchJson<BizStatusResponse>('/api/v1/nts/biz/status', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ bizNumbers }),
  })
}

export async function validateBiz(body: {
  bizNo: string
  startDt: string
  ceoName: string
  ceoName2?: string
  corpName?: string
  corpNo?: string
  bizSector?: string
  bizType?: string
  address?: string
}): Promise<BizValidateResponse> {
  return fetchJson<BizValidateResponse>('/api/v1/nts/biz/validate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
}
