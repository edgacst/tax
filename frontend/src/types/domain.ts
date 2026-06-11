export type InvoiceStatus = 'draft' | 'submitted' | 'approved' | 'rejected' | 'cancelled'

export interface Invoice {
  id: string
  serialNo: string
  issueDate: string
  workplaceName: string
  partnerName: string
  partnerBizNo: string
  supplyAmount: number
  tax: number
  total: number
  status: InvoiceStatus
  direction: 'issue' | 'receive'
  remark?: string
}

export interface Partner {
  id: string
  bizNo: string
  name: string
  ceo: string
  email: string
  phone: string
  favorite: boolean
}

export interface Workplace {
  id: string
  name: string
  bizNo: string
  default: boolean
  address: string
  ceoName: string
  bizType: string
  bizItem: string
  phone: string
  email: string
}

export interface CertificateMeta {
  id: string
  type: string
  subject: string
  validTo: string
  status: 'active' | 'expiring' | 'expired'
}
