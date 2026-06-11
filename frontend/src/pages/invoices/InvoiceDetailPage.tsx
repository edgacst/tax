import { ArrowLeft, FileDown, Send } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, Navigate, useParams } from 'react-router-dom'
import { InvoiceStatusBadge } from '../../components/StatusBadge'
import { won } from '../../lib/format'
import { getInvoice, submitInvoice, type InvoiceDetailDto } from '../../lib/invoicesApi'
import type { InvoiceStatus } from '../../types/domain'

export function InvoiceDetailPage() {
  const { id } = useParams()
  const [row, setRow] = useState<InvoiceDetailDto | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    setLoading(true)
    void getInvoice(id)
      .then(setRow)
      .catch((e) => setError(e instanceof Error ? e.message : '상세를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [id])

  if (!id) {
    return <Navigate to="/invoices" replace />
  }

  if (!loading && (error || !row)) {
    return (
      <div>
        <Link to="/invoices" className="text-sm text-blue-400 hover:text-blue-300">
          목록으로
        </Link>
        <p className="mt-6 text-sm text-rose-300">{error ?? '세금계산서를 찾을 수 없습니다.'}</p>
      </div>
    )
  }

  if (!row) {
    return <p className="text-sm text-slate-500">불러오는 중…</p>
  }

  const canSubmit = row.status === 'draft' && row.direction === 'issue'

  async function handleSubmit() {
    if (!id || !canSubmit) return
    setSubmitting(true)
    setSubmitError(null)
    try {
      const res = await submitInvoice(id)
      setRow(res.invoice)
    } catch (e) {
      setSubmitError(e instanceof Error ? e.message : '국세청 전송에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
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
            {row.direction === 'receive' ? '매입' : '매출'}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <InvoiceStatusBadge status={row.status as InvoiceStatus} />
          <button
            type="button"
            disabled
            className="inline-flex items-center gap-2 rounded-xl border border-surface-border px-4 py-2 text-sm text-slate-500"
            title="추후 구현"
          >
            <FileDown className="h-4 w-4" />
            PDF
          </button>
          {canSubmit && (
            <button
              type="button"
              disabled={submitting}
              onClick={() => void handleSubmit()}
              className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-500 disabled:opacity-50"
            >
              <Send className="h-4 w-4" />
              {submitting ? '전송 중…' : '국세청 발행'}
            </button>
          )}
        </div>
      </div>

      {submitError && (
        <p className="mt-4 rounded-xl border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-300">
          {submitError}
        </p>
      )}

      {row.approvalNumber && (
        <section className="mt-6 rounded-2xl border border-emerald-500/30 bg-emerald-500/10 p-6">
          <h2 className="mb-2 text-sm font-semibold text-emerald-200">국세청 승인</h2>
          <p className="font-mono text-lg text-white">{row.approvalNumber}</p>
          {row.submittedAt && (
            <p className="mt-1 text-xs text-emerald-300/80">전송 시각: {row.submittedAt}</p>
          )}
        </section>
      )}

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

      {row.items.length > 0 && (
        <section className="mt-6 overflow-hidden rounded-2xl border border-surface-border bg-surface-card">
          <h2 className="border-b border-surface-border px-6 py-4 text-sm font-semibold text-slate-200">
            품목
          </h2>
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-900/40 text-xs text-slate-500">
              <tr>
                <th className="px-4 py-2">품명</th>
                <th className="px-4 py-2 text-right">수량</th>
                <th className="px-4 py-2 text-right">단가</th>
                <th className="px-4 py-2 text-right">공급가액</th>
                <th className="px-4 py-2 text-right">세액</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-border text-slate-300">
              {row.items.map((item) => (
                <tr key={item.id}>
                  <td className="px-4 py-2">{item.itemName}</td>
                  <td className="px-4 py-2 text-right tabular-nums">{item.quantity}</td>
                  <td className="px-4 py-2 text-right tabular-nums">{won(item.unitPrice)}</td>
                  <td className="px-4 py-2 text-right tabular-nums">{won(item.amount)}</td>
                  <td className="px-4 py-2 text-right tabular-nums">{won(item.tax)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}

      {row.remark && (
        <section className="mt-6 rounded-2xl border border-surface-border bg-surface-card p-6">
          <h2 className="mb-2 text-sm font-semibold text-slate-200">비고</h2>
          <p className="text-sm text-slate-300">{row.remark}</p>
        </section>
      )}
    </div>
  )
}
