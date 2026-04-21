import { Plus, Search } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { InvoiceStatusBadge } from '../../components/StatusBadge'
import { PageHeader } from '../../components/PageHeader'
import { won } from '../../lib/format'
import { mockInvoices } from '../../lib/mock/invoices'

export function InvoiceListPage() {
  const [q, setQ] = useState('')

  const rows = useMemo(() => {
    const s = q.trim().toLowerCase()
    if (!s) return mockInvoices
    return mockInvoices.filter(
      (i) =>
        i.partnerName.toLowerCase().includes(s) ||
        i.serialNo.toLowerCase().includes(s) ||
        i.partnerBizNo.includes(s),
    )
  }, [q])

  return (
    <div>
      <PageHeader
        title="세금계산서"
        description="매출·매입 발행 내역을 조회합니다. (목업 데이터)"
        actions={
          <Link
            to="/invoices/new"
            className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow hover:bg-blue-500"
          >
            <Plus className="h-4 w-4" />
            새로 발행
          </Link>
        }
      />

      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="relative max-w-md flex-1">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
          <input
            type="search"
            placeholder="거래처명, 일련번호, 사업자번호 검색…"
            value={q}
            onChange={(e) => setQ(e.target.value)}
            className="w-full rounded-xl border border-surface-border bg-slate-900/40 py-2.5 pl-10 pr-3 text-sm text-white outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/25"
          />
        </div>
        <p className="text-xs text-slate-500">
          총 <span className="font-medium text-slate-300">{rows.length}</span>건
        </p>
      </div>

      <div className="overflow-hidden rounded-2xl border border-surface-border bg-surface-card shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[720px] text-left text-sm">
            <thead className="border-b border-surface-border bg-slate-900/40 text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-3 font-medium">구분</th>
                <th className="px-4 py-3 font-medium">일자 / 일련번호</th>
                <th className="px-4 py-3 font-medium">사업장</th>
                <th className="px-4 py-3 font-medium">거래처</th>
                <th className="px-4 py-3 font-medium text-right">합계</th>
                <th className="px-4 py-3 font-medium">상태</th>
                <th className="px-4 py-3 font-medium" />
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-border">
              {rows.map((row) => (
                <tr key={row.id} className="text-slate-300 hover:bg-slate-900/30">
                  <td className="px-4 py-3">
                    <span
                      className={
                        row.direction === 'issue'
                          ? 'text-xs text-blue-300'
                          : 'text-xs text-violet-300'
                      }
                    >
                      {row.direction === 'issue' ? '매출' : '매입'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="font-medium text-slate-100">{row.issueDate}</div>
                    <div className="text-xs text-slate-500">{row.serialNo}</div>
                  </td>
                  <td className="px-4 py-3 text-slate-400">{row.workplaceName}</td>
                  <td className="px-4 py-3">
                    <div className="font-medium text-slate-100">{row.partnerName}</div>
                    <div className="text-xs text-slate-500">{row.partnerBizNo}</div>
                  </td>
                  <td className="px-4 py-3 text-right tabular-nums font-medium text-slate-100">
                    {won(row.total)}
                  </td>
                  <td className="px-4 py-3">
                    <InvoiceStatusBadge status={row.status} />
                  </td>
                  <td className="px-4 py-3 text-right">
                    <Link
                      to={`/invoices/${row.id}`}
                      className="text-xs font-medium text-blue-400 hover:text-blue-300"
                    >
                      상세
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
