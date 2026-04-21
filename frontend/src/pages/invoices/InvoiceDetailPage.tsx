import { ArrowLeft, FileDown, Send } from 'lucide-react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { InvoiceStatusBadge } from '../../components/StatusBadge'
import { won } from '../../lib/format'
import { getInvoiceById } from '../../lib/mock/invoices'

export function InvoiceDetailPage() {
  const { id } = useParams()
  const row = id ? getInvoiceById(id) : undefined

  if (!row) {
    return <Navigate to="/invoices" replace />
  }

  return (
    <div>
      <div className="mb-6">
        <Link
          to="/invoices"
          className="inline-flex items-center gap-1 text-sm font-medium text-slate-400 hover:text-blue-300"
        >
          <ArrowLeft className="h-4 w-4" />
          목록으로
        </Link>
      </div>

      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <p className="text-xs text-slate-500">{row.serialNo}</p>
          <h1 className="text-2xl font-bold text-white">세금계산서 상세</h1>
          <p className="mt-1 text-sm text-slate-400">
            {row.issueDate} · {row.workplaceName} ·{' '}
            {row.direction === 'issue' ? '매출' : '매입'}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <InvoiceStatusBadge status={row.status} />
          <button
            type="button"
            disabled
            className="inline-flex items-center gap-2 rounded-xl border border-surface-border px-4 py-2 text-sm text-slate-500"
            title="목업"
          >
            <FileDown className="h-4 w-4" />
            PDF
          </button>
          <button
            type="button"
            disabled
            className="inline-flex items-center gap-2 rounded-xl border border-surface-border px-4 py-2 text-sm text-slate-500"
            title="목업"
          >
            <Send className="h-4 w-4" />
            재전송
          </button>
        </div>
      </div>

      <div className="mt-8 grid gap-6 lg:grid-cols-2">
        <section className="rounded-2xl border border-surface-border bg-surface-card p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-200">공급받는자</h2>
          <dl className="space-y-3 text-sm">
            <div className="flex justify-between gap-4">
              <dt className="text-slate-500">상호</dt>
              <dd className="text-right font-medium text-slate-100">{row.partnerName}</dd>
            </div>
            <div className="flex justify-between gap-4">
              <dt className="text-slate-500">사업자등록번호</dt>
              <dd className="text-right tabular-nums text-slate-200">{row.partnerBizNo}</dd>
            </div>
          </dl>
        </section>
        <section className="rounded-2xl border border-surface-border bg-surface-card p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-200">금액</h2>
          <dl className="space-y-3 text-sm">
            <div className="flex justify-between gap-4">
              <dt className="text-slate-500">공급가액</dt>
              <dd className="tabular-nums text-slate-200">{won(row.supplyAmount)}</dd>
            </div>
            <div className="flex justify-between gap-4">
              <dt className="text-slate-500">세액</dt>
              <dd className="tabular-nums text-slate-200">{won(row.tax)}</dd>
            </div>
            <div className="flex justify-between gap-4 border-t border-surface-border pt-3 text-base font-semibold text-white">
              <dt>합계</dt>
              <dd className="tabular-nums">{won(row.total)}</dd>
            </div>
          </dl>
        </section>
      </div>

      {row.remark && (
        <section className="mt-6 rounded-2xl border border-surface-border bg-surface-card p-6">
          <h2 className="mb-2 text-sm font-semibold text-slate-200">비고</h2>
          <p className="text-sm text-slate-300">{row.remark}</p>
        </section>
      )}
    </div>
  )
}
