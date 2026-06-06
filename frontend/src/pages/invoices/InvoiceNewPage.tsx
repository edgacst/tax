import { Plus, Trash2 } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { PageHeader } from '../../components/PageHeader'
import { createInvoiceDraft } from '../../lib/invoicesApi'
import { listPartners } from '../../lib/partnersApi'
import { listWorkplaces } from '../../lib/workplacesApi'
import { won } from '../../lib/format'
import type { Partner, Workplace } from '../../types/domain'

type Line = { id: string; name: string; qty: number; unitPrice: number }

let lineId = 0
function newLine(): Line {
  lineId += 1
  return { id: `l-${lineId}`, name: '', qty: 1, unitPrice: 0 }
}

export function InvoiceNewPage() {
  const navigate = useNavigate()
  const [workplaces, setWorkplaces] = useState<Workplace[]>([])
  const [partners, setPartners] = useState<Partner[]>([])
  const [workplaceId, setWorkplaceId] = useState('')
  const [partnerId, setPartnerId] = useState('')
  const [issueDate, setIssueDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [remark, setRemark] = useState('')
  const [lines, setLines] = useState<Line[]>([newLine(), newLine()])
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    void Promise.all([listWorkplaces(), listPartners()])
      .then(([w, p]) => {
        setWorkplaces(w)
        setPartners(p)
        setWorkplaceId(w.find((x) => x.default)?.id ?? w[0]?.id ?? '')
        setPartnerId(p[0]?.id ?? '')
      })
      .catch((e) => setError(e instanceof Error ? e.message : '마스터 데이터를 불러오지 못했습니다.'))
  }, [])

  const totals = useMemo(() => {
    const supply = lines.reduce((a, l) => a + l.qty * l.unitPrice, 0)
    const tax = Math.floor(supply * 0.1)
    return { supply, tax, grand: supply + tax }
  }, [lines])

  function updateLine(id: string, patch: Partial<Line>) {
    setLines((prev) => prev.map((l) => (l.id === id ? { ...l, ...patch } : l)))
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    const items = lines
      .filter((l) => l.name.trim() && l.unitPrice > 0)
      .map((l) => ({ itemName: l.name.trim(), quantity: l.qty, unitPrice: l.unitPrice }))
    if (items.length === 0) {
      setError('품목을 1개 이상 입력하세요.')
      return
    }
    setSaving(true)
    setError(null)
    try {
      const created = await createInvoiceDraft({
        workplaceId: Number(workplaceId),
        partnerId: Number(partnerId),
        issueDate,
        remark,
        items,
      })
      navigate(`/invoices/${created.id}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <PageHeader
        title="세금계산서 발행"
        description="공급자·공급받는자·품목을 입력합니다."
        actions={
          <Link
            to="/invoices"
            className="rounded-xl border border-surface-border px-4 py-2 text-sm font-medium text-slate-300 hover:bg-slate-800"
          >
            취소
          </Link>
        }
      />

      {error && (
        <div className="mb-4 rounded-xl border border-rose-500/40 bg-rose-950/30 px-4 py-3 text-sm text-rose-200">
          {error}
        </div>
      )}

      <form onSubmit={onSubmit} className="space-y-8">
        <section className="rounded-2xl border border-surface-border bg-surface-card p-6 shadow-lg">
          <h2 className="mb-4 text-sm font-semibold text-slate-200">기본 정보</h2>
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="mb-1 block text-xs text-slate-500">사업장</label>
              <select
                value={workplaceId}
                onChange={(e) => setWorkplaceId(e.target.value)}
                className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
              >
                {workplaces.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.name} ({w.bizNo})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="mb-1 block text-xs text-slate-500">공급받는자(거래처)</label>
              <select
                value={partnerId}
                onChange={(e) => setPartnerId(e.target.value)}
                className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
              >
                {partners.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name} · {p.bizNo}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="mb-1 block text-xs text-slate-500">작성일자</label>
              <input
                type="date"
                value={issueDate}
                onChange={(e) => setIssueDate(e.target.value)}
                className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
              />
            </div>
            <div className="md:col-span-2">
              <label className="mb-1 block text-xs text-slate-500">비고</label>
              <input
                value={remark}
                onChange={(e) => setRemark(e.target.value)}
                placeholder="선택"
                className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white placeholder:text-slate-600"
              />
            </div>
          </div>
        </section>

        <section className="rounded-2xl border border-surface-border bg-surface-card p-6 shadow-lg">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-sm font-semibold text-slate-200">품목</h2>
            <button
              type="button"
              onClick={() => setLines((l) => [...l, newLine()])}
              className="inline-flex items-center gap-1 rounded-lg bg-slate-800 px-3 py-1.5 text-xs font-medium text-slate-200 hover:bg-slate-700"
            >
              <Plus className="h-3.5 w-3.5" />
              행 추가
            </button>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[640px] text-left text-sm">
              <thead className="text-xs text-slate-500">
                <tr>
                  <th className="pb-2 pr-2 font-medium">품목명</th>
                  <th className="pb-2 pr-2 font-medium">수량</th>
                  <th className="pb-2 pr-2 font-medium">단가</th>
                  <th className="pb-2 pr-2 font-medium text-right">금액</th>
                  <th className="pb-2 w-10" />
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-border">
                {lines.map((line) => (
                  <tr key={line.id}>
                    <td className="py-2 pr-2">
                      <input
                        value={line.name}
                        onChange={(e) => updateLine(line.id, { name: e.target.value })}
                        placeholder="품목명"
                        className="w-full rounded-lg border border-surface-border bg-slate-900/50 px-2 py-2 text-sm"
                      />
                    </td>
                    <td className="py-2 pr-2">
                      <input
                        type="number"
                        min={1}
                        value={line.qty}
                        onChange={(e) => updateLine(line.id, { qty: Number(e.target.value) || 0 })}
                        className="w-24 rounded-lg border border-surface-border bg-slate-900/50 px-2 py-2 text-sm tabular-nums"
                      />
                    </td>
                    <td className="py-2 pr-2">
                      <input
                        type="number"
                        min={0}
                        value={line.unitPrice || ''}
                        onChange={(e) => updateLine(line.id, { unitPrice: Number(e.target.value) || 0 })}
                        className="w-36 rounded-lg border border-surface-border bg-slate-900/50 px-2 py-2 text-sm tabular-nums"
                      />
                    </td>
                    <td className="py-2 pr-2 text-right tabular-nums text-slate-200">
                      {won(line.qty * line.unitPrice)}
                    </td>
                    <td className="py-2 text-right">
                      <button
                        type="button"
                        onClick={() => setLines((prev) => prev.filter((x) => x.id !== line.id))}
                        className="rounded p-1.5 text-slate-500 hover:bg-rose-950/50 hover:text-rose-300"
                        aria-label="행 삭제"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="mt-4 flex flex-col items-end gap-1 border-t border-surface-border pt-4 text-sm">
            <div className="flex w-64 justify-between text-slate-400">
              <span>공급가액</span>
              <span className="tabular-nums text-slate-200">{won(totals.supply)}</span>
            </div>
            <div className="flex w-64 justify-between text-slate-400">
              <span>세액 (10%)</span>
              <span className="tabular-nums text-slate-200">{won(totals.tax)}</span>
            </div>
            <div className="flex w-64 justify-between text-base font-semibold text-white">
              <span>합계</span>
              <span className="tabular-nums">{won(totals.grand)}</span>
            </div>
          </div>
        </section>

        <div className="flex flex-wrap justify-end gap-2">
          <Link
            to="/invoices"
            className="rounded-xl border border-surface-border px-5 py-2.5 text-sm font-medium text-slate-300 hover:bg-slate-800"
          >
            나가기
          </Link>
          <button
            type="submit"
            disabled={saving}
            className="rounded-xl bg-blue-600 px-6 py-2.5 text-sm font-semibold text-white shadow hover:bg-blue-500 disabled:opacity-60"
          >
            {saving ? '저장 중…' : '임시저장'}
          </button>
          <button
            type="button"
            onClick={() => {
              window.alert('목업: 국세청 전송 플로우는 NTS 연동 후 구현')
            }}
            className="rounded-xl bg-emerald-700 px-6 py-2.5 text-sm font-semibold text-white shadow hover:bg-emerald-600"
          >
            전자세금계산서 발행
          </button>
        </div>
      </form>
    </div>
  )
}
