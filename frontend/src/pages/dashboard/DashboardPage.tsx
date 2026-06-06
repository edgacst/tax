import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ChevronRight, Radio } from 'lucide-react'
import { InvoiceStatusBadge } from '../../components/StatusBadge'
import { fetchPing } from '../../lib/api'
import { fetchDashboardSummary, type DashboardSummary } from '../../lib/dashboardApi'
import { won } from '../../lib/format'
import { useAuthStore } from '../../store/authStore'
import { useUiStore } from '../../store/uiStore'

export function DashboardPage() {
  const name = useAuthStore((s) => s.user?.name)
  const { lastPingJson, lastHttpStatus, lastError, setPingResult } = useUiStore()
  const [pinging, setPinging] = useState(false)
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [summaryError, setSummaryError] = useState<string | null>(null)

  useEffect(() => {
    void fetchDashboardSummary()
      .then(setSummary)
      .catch((e) => setSummaryError(e instanceof Error ? e.message : '요약을 불러오지 못했습니다.'))
  }, [])

  const onPing = useCallback(async () => {
    setPinging(true)
    try {
      const data = await fetchPing()
      setPingResult({ json: JSON.stringify(data, null, 2), status: 200, error: null })
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e)
      setPingResult({
        json: msg,
        status: 0,
        error: '백엔드(8080) 미기동 또는 DB 미연결',
      })
    } finally {
      setPinging(false)
    }
  }, [setPingResult])

  const recent = summary?.recentInvoices ?? []

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-white">안녕하세요, {name}님</h1>
        <p className="mt-1 text-sm text-slate-400">오늘의 발행·승인 현황을 한눈에 확인하세요.</p>
      </div>

      {summaryError && (
        <div className="rounded-xl border border-rose-500/40 bg-rose-950/30 px-4 py-3 text-sm text-rose-200">
          {summaryError}
        </div>
      )}

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {[
          {
            label: '승인 건수',
            value: summary ? String(summary.approvedCount) : '—',
            hint: 'DB 집계',
          },
          { label: '임시저장', value: summary ? String(summary.draftCount) : '—', hint: '제출 전' },
          {
            label: '승인 매출 합계',
            value: summary ? won(summary.approvedSalesTotal) : '—',
            hint: '부가세 포함',
          },
          {
            label: '등록 거래처',
            value: summary ? `${summary.partnerCount}곳` : '—',
            hint: '마스터',
          },
        ].map((c) => (
          <div
            key={c.label}
            className="rounded-2xl border border-surface-border bg-surface-card p-5 shadow-lg shadow-black/20"
          >
            <p className="text-xs font-medium text-slate-500">{c.label}</p>
            <p className="mt-2 text-2xl font-semibold tracking-tight text-white">{c.value}</p>
            <p className="mt-1 text-xs text-slate-500">{c.hint}</p>
          </div>
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="rounded-2xl border border-surface-border bg-surface-card p-5 lg:col-span-2">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-sm font-semibold text-slate-200">최근 세금계산서</h2>
            <Link
              to="/invoices"
              className="inline-flex items-center gap-1 text-xs font-medium text-blue-400 hover:text-blue-300"
            >
              전체보기
              <ChevronRight className="h-3 w-3" />
            </Link>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[520px] text-left text-sm">
              <thead>
                <tr className="border-b border-surface-border text-xs text-slate-500">
                  <th className="pb-2 pr-3 font-medium">일자·일련번호</th>
                  <th className="pb-2 pr-3 font-medium">거래처</th>
                  <th className="pb-2 pr-3 font-medium text-right">합계</th>
                  <th className="pb-2 font-medium">상태</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border/80">
                {recent.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="py-6 text-center text-slate-500">
                      데이터가 없습니다.
                    </td>
                  </tr>
                ) : (
                  recent.map((row) => (
                    <tr key={row.id} className="text-slate-300">
                      <td className="py-2.5 pr-3">
                        <div className="font-medium text-slate-200">{row.issueDate}</div>
                        <div className="text-xs text-slate-500">{row.serialNo}</div>
                      </td>
                      <td className="py-2.5 pr-3">
                        <Link to={`/invoices/${row.id}`} className="hover:text-blue-300">
                          {row.partnerName}
                        </Link>
                      </td>
                      <td className="py-2.5 pr-3 text-right tabular-nums text-slate-200">
                        {won(row.total)}
                      </td>
                      <td className="py-2.5">
                        <InvoiceStatusBadge status={row.status} />
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        <div className="space-y-4">
          <div className="rounded-2xl border border-surface-border bg-surface-card p-5">
            <h2 className="mb-3 flex items-center gap-2 text-sm font-semibold text-slate-200">
              <Radio className="h-4 w-4 text-emerald-400" />
              백엔드 헬스
            </h2>
            <button
              type="button"
              onClick={onPing}
              disabled={pinging}
              className="w-full rounded-xl bg-slate-800 py-2 text-xs font-semibold text-slate-200 transition hover:bg-slate-700 disabled:opacity-50"
            >
              {pinging ? '호출 중…' : 'Ping (/api/v1/public/ping)'}
            </button>
            <pre className="mt-3 max-h-32 overflow-auto rounded-lg bg-[#0d1117] p-2 font-mono text-[10px] leading-relaxed text-slate-400">
              {lastPingJson ?? '버튼을 눌러 확인'}
            </pre>
            {lastHttpStatus === 200 && !lastError && <p className="mt-2 text-xs text-emerald-400">연결됨</p>}
            {lastError && <p className="mt-2 text-xs text-rose-400">{lastError}</p>}
          </div>
          <div className="rounded-2xl border border-dashed border-surface-border bg-slate-900/30 p-5">
            <p className="text-xs font-medium text-slate-500">월별 추이</p>
            <p className="mt-6 text-center text-sm text-slate-500">차트 영역 (Recharts 등 연동 예정)</p>
            <div className="mt-4 flex h-24 items-end justify-between gap-1 px-2">
              {[40, 65, 45, 80, 55, 70, 90].map((h, i) => (
                <div
                  key={i}
                  className="flex-1 rounded-t bg-blue-600/40"
                  style={{ height: `${h}%` }}
                />
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
