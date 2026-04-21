import { CloudDownload, Plus, RefreshCw, Search } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { InvoiceStatusBadge } from '../../components/StatusBadge'
import { PageHeader } from '../../components/PageHeader'
import { won } from '../../lib/format'
import { mockInvoices } from '../../lib/mock/invoices'
import {
  listNtsPurchaseReceipts,
  listNtsPurchaseSyncRuns,
  syncNtsPurchases,
  type PurchaseReceiptDto,
  type SyncRunDto,
} from '../../lib/ntsPurchasesApi'

type InvoiceTab = 'issued' | 'nts'

function syncRunStatusClass(status: SyncRunDto['status']) {
  switch (status) {
    case 'SUCCESS':
      return 'text-emerald-400'
    case 'FAILED':
      return 'text-rose-400'
    default:
      return 'text-amber-300'
  }
}

export function InvoiceListPage() {
  const [q, setQ] = useState('')
  const [tab, setTab] = useState<InvoiceTab>('issued')

  const [ntsReceipts, setNtsReceipts] = useState<PurchaseReceiptDto[]>([])
  const [ntsRuns, setNtsRuns] = useState<SyncRunDto[]>([])
  const [ntsLoading, setNtsLoading] = useState(false)
  const [ntsSyncing, setNtsSyncing] = useState(false)
  const [ntsError, setNtsError] = useState<string | null>(null)
  const [ntsToast, setNtsToast] = useState<string | null>(null)

  const loadNts = useCallback(async () => {
    setNtsLoading(true)
    setNtsError(null)
    try {
      const [receipts, runs] = await Promise.all([
        listNtsPurchaseReceipts(),
        listNtsPurchaseSyncRuns(),
      ])
      setNtsReceipts(receipts)
      setNtsRuns(runs)
    } catch (e) {
      setNtsError(e instanceof Error ? e.message : '매입 수신 목록을 불러오지 못했습니다.')
    } finally {
      setNtsLoading(false)
    }
  }, [])

  useEffect(() => {
    if (tab !== 'nts') return
    void loadNts()
  }, [tab, loadNts])

  const onSyncNts = async () => {
    setNtsSyncing(true)
    setNtsError(null)
    setNtsToast(null)
    try {
      const r = await syncNtsPurchases()
      setNtsToast(
        `동기화 완료 · 조회 ${r.recordsFetched}건 · 신규 ${r.recordsInserted}건${r.message ? ` — ${r.message}` : ''}`,
      )
      await loadNts()
    } catch (e) {
      setNtsError(e instanceof Error ? e.message : '동기화에 실패했습니다.')
    } finally {
      setNtsSyncing(false)
    }
  }

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
        description={
          tab === 'issued'
            ? '매출·매입 발행 내역을 조회합니다. (사내 목업)'
            : '홈택스 매입분을 수신·동기화합니다. (백엔드 스텁/DB 연동)'
        }
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

      <div className="mb-6 flex flex-wrap gap-2 rounded-2xl border border-surface-border bg-slate-900/30 p-1">
        <button
          type="button"
          onClick={() => setTab('issued')}
          className={`rounded-xl px-4 py-2 text-sm font-medium transition ${
            tab === 'issued'
              ? 'bg-slate-800 text-white shadow'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          사내 발행 내역
        </button>
        <button
          type="button"
          onClick={() => setTab('nts')}
          className={`rounded-xl px-4 py-2 text-sm font-medium transition ${
            tab === 'nts'
              ? 'bg-slate-800 text-white shadow'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          홈택스 매입 수신
        </button>
      </div>

      {tab === 'issued' ? (
        <>
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
        </>
      ) : (
        <div className="space-y-6">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                onClick={() => void onSyncNts()}
                disabled={ntsSyncing}
                className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-4 py-2.5 text-sm font-semibold text-white shadow hover:bg-violet-500 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {ntsSyncing ? (
                  <RefreshCw className="h-4 w-4 animate-spin" />
                ) : (
                  <CloudDownload className="h-4 w-4" />
                )}
                매입 동기화
              </button>
              <button
                type="button"
                onClick={() => void loadNts()}
                disabled={ntsLoading}
                className="inline-flex items-center gap-2 rounded-xl border border-surface-border bg-slate-900/40 px-4 py-2.5 text-sm font-medium text-slate-200 hover:bg-slate-800/60 disabled:cursor-not-allowed disabled:opacity-60"
              >
                <RefreshCw className={`h-4 w-4 ${ntsLoading ? 'animate-spin' : ''}`} />
                새로고침
              </button>
            </div>
            <p className="text-xs text-slate-500">
              수신 <span className="font-medium text-slate-300">{ntsReceipts.length}</span>건
            </p>
          </div>

          {ntsError && (
            <div className="rounded-xl border border-rose-500/40 bg-rose-950/30 px-4 py-3 text-sm text-rose-200">
              {ntsError}
            </div>
          )}
          {ntsToast && !ntsError && (
            <div className="rounded-xl border border-emerald-500/30 bg-emerald-950/20 px-4 py-3 text-sm text-emerald-100">
              {ntsToast}
            </div>
          )}

          <div>
            <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
              최근 동기화 실행
            </h3>
            <div className="overflow-hidden rounded-2xl border border-surface-border bg-surface-card shadow-xl">
              <div className="overflow-x-auto">
                <table className="w-full min-w-[640px] text-left text-sm">
                  <thead className="border-b border-surface-border bg-slate-900/40 text-xs uppercase tracking-wide text-slate-500">
                    <tr>
                      <th className="px-4 py-3 font-medium">상태</th>
                      <th className="px-4 py-3 font-medium text-right">조회</th>
                      <th className="px-4 py-3 font-medium text-right">적재</th>
                      <th className="px-4 py-3 font-medium">시작</th>
                      <th className="px-4 py-3 font-medium">종료</th>
                      <th className="px-4 py-3 font-medium">오류</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-surface-border">
                    {ntsRuns.length === 0 && !ntsLoading ? (
                      <tr>
                        <td colSpan={6} className="px-4 py-8 text-center text-sm text-slate-500">
                          동기화 이력이 없습니다. 「매입 동기화」를 눌러 보세요.
                        </td>
                      </tr>
                    ) : (
                      ntsRuns.map((run) => (
                        <tr key={run.id} className="text-slate-300 hover:bg-slate-900/30">
                          <td className={`px-4 py-3 text-xs font-semibold ${syncRunStatusClass(run.status)}`}>
                            {run.status}
                          </td>
                          <td className="px-4 py-3 text-right tabular-nums">{run.recordsFetched}</td>
                          <td className="px-4 py-3 text-right tabular-nums">{run.recordsInserted}</td>
                          <td className="px-4 py-3 text-xs text-slate-400">
                            {new Date(run.startedAt).toLocaleString('ko-KR')}
                          </td>
                          <td className="px-4 py-3 text-xs text-slate-400">
                            {run.completedAt ? new Date(run.completedAt).toLocaleString('ko-KR') : '—'}
                          </td>
                          <td className="max-w-[200px] truncate px-4 py-3 text-xs text-rose-300" title={run.errorMessage ?? ''}>
                            {run.errorMessage ?? '—'}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div>
            <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
              수신 매입분
            </h3>
            <div className="overflow-hidden rounded-2xl border border-surface-border bg-surface-card shadow-xl">
              <div className="overflow-x-auto">
                <table className="w-full min-w-[900px] text-left text-sm">
                  <thead className="border-b border-surface-border bg-slate-900/40 text-xs uppercase tracking-wide text-slate-500">
                    <tr>
                      <th className="px-4 py-3 font-medium">승인번호</th>
                      <th className="px-4 py-3 font-medium">작성일</th>
                      <th className="px-4 py-3 font-medium">공급자</th>
                      <th className="px-4 py-3 font-medium text-right">공급가액</th>
                      <th className="px-4 py-3 font-medium text-right">세액</th>
                      <th className="px-4 py-3 font-medium text-right">합계</th>
                      <th className="px-4 py-3 font-medium">수신 시각</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-surface-border">
                    {ntsReceipts.length === 0 && !ntsLoading ? (
                      <tr>
                        <td colSpan={7} className="px-4 py-8 text-center text-sm text-slate-500">
                          수신된 매입분이 없습니다. 백엔드가 떠 있고 DB에 `tenant_demo` 시드가 있어야 합니다.
                        </td>
                      </tr>
                    ) : (
                      ntsReceipts.map((r) => (
                        <tr key={r.id} className="text-slate-300 hover:bg-slate-900/30">
                          <td className="max-w-[140px] truncate px-4 py-3 font-mono text-xs" title={r.ntsApprovalNumber}>
                            {r.ntsApprovalNumber}
                          </td>
                          <td className="px-4 py-3 tabular-nums text-slate-200">{r.issueDate}</td>
                          <td className="px-4 py-3">
                            <div className="font-medium text-slate-100">{r.supplierName}</div>
                            <div className="text-xs text-slate-500">{r.supplierBizNo}</div>
                          </td>
                          <td className="px-4 py-3 text-right tabular-nums">{won(r.supplyAmount)}</td>
                          <td className="px-4 py-3 text-right tabular-nums">{won(r.taxAmount)}</td>
                          <td className="px-4 py-3 text-right tabular-nums font-medium text-slate-100">
                            {won(r.totalAmount)}
                          </td>
                          <td className="px-4 py-3 text-xs text-slate-400">
                            {new Date(r.syncedAt).toLocaleString('ko-KR')}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
