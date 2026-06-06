import { Plus, Search, Star } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { PageHeader } from '../../components/PageHeader'
import { listPartners } from '../../lib/partnersApi'
import type { Partner } from '../../types/domain'

export function PartnerListPage() {
  const [q, setQ] = useState('')
  const [rows, setRows] = useState<Partner[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async (search: string) => {
    setLoading(true)
    setError(null)
    try {
      setRows(await listPartners(search || undefined))
    } catch (e) {
      setError(e instanceof Error ? e.message : '거래처 목록을 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const t = window.setTimeout(() => void load(q), 200)
    return () => window.clearTimeout(t)
  }, [q, load])

  return (
    <div>
      <PageHeader
        title="거래처"
        description="사업자등록번호·연락처를 관리합니다."
        actions={
          <Link
            to="/partners/new"
            className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-500"
          >
            <Plus className="h-4 w-4" />
            거래처 등록
          </Link>
        }
      />

      <div className="relative mb-4 max-w-md">
        <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
        <input
          type="search"
          placeholder="상호, 사업자번호, 대표자명…"
          value={q}
          onChange={(e) => setQ(e.target.value)}
          className="w-full rounded-xl border border-surface-border bg-slate-900/40 py-2.5 pl-10 pr-3 text-sm text-white outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/25"
        />
      </div>

      {error && (
        <div className="mb-4 rounded-xl border border-rose-500/40 bg-rose-950/30 px-4 py-3 text-sm text-rose-200">
          {error}
        </div>
      )}

      <div className="overflow-hidden rounded-2xl border border-surface-border bg-surface-card shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[640px] text-left text-sm">
            <thead className="border-b border-surface-border bg-slate-900/40 text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-3 font-medium" />
                <th className="px-4 py-3 font-medium">사업자번호</th>
                <th className="px-4 py-3 font-medium">상호</th>
                <th className="px-4 py-3 font-medium">대표자</th>
                <th className="px-4 py-3 font-medium">이메일</th>
                <th className="px-4 py-3 font-medium">전화</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-border">
              {!loading && rows.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-slate-500">
                    거래처가 없습니다.
                  </td>
                </tr>
              ) : (
                rows.map((p) => (
                  <tr key={p.id} className="text-slate-300 hover:bg-slate-900/30">
                    <td className="px-4 py-3">
                      {p.favorite ? (
                        <Star className="h-4 w-4 fill-amber-400 text-amber-400" />
                      ) : (
                        <Star className="h-4 w-4 text-slate-600" />
                      )}
                    </td>
                    <td className="px-4 py-3 tabular-nums text-slate-200">{p.bizNo}</td>
                    <td className="px-4 py-3 font-medium text-slate-100">{p.name}</td>
                    <td className="px-4 py-3">{p.ceo}</td>
                    <td className="px-4 py-3 text-slate-400">{p.email}</td>
                    <td className="px-4 py-3 tabular-nums text-slate-400">{p.phone}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
