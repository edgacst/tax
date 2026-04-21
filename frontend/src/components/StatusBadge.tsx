import type { CertificateMeta, InvoiceStatus } from '../types/domain'

const invoiceStyles: Record<
  InvoiceStatus,
  { label: string; className: string }
> = {
  draft: { label: '임시저장', className: 'bg-slate-600 text-slate-100' },
  submitted: { label: '국세청 전송', className: 'bg-amber-600/90 text-amber-50' },
  approved: { label: '승인', className: 'bg-emerald-700/90 text-emerald-50' },
  rejected: { label: '거부', className: 'bg-rose-700/90 text-rose-50' },
  cancelled: { label: '취소', className: 'bg-slate-700 text-slate-200' },
}

export function InvoiceStatusBadge({ status }: { status: InvoiceStatus }) {
  const s = invoiceStyles[status]
  return (
    <span className={`inline-flex rounded-md px-2 py-0.5 text-xs font-medium ${s.className}`}>
      {s.label}
    </span>
  )
}

const certStyles: Record<CertificateMeta['status'], { label: string; className: string }> = {
  active: { label: '정상', className: 'bg-emerald-800/80 text-emerald-100' },
  expiring: { label: '만료 임박', className: 'bg-amber-800/80 text-amber-100' },
  expired: { label: '만료', className: 'bg-rose-900/80 text-rose-100' },
}

export function CertStatusBadge({ status }: { status: CertificateMeta['status'] }) {
  const s = certStyles[status]
  return (
    <span className={`inline-flex rounded-md px-2 py-0.5 text-xs font-medium ${s.className}`}>
      {s.label}
    </span>
  )
}
