import { Building2, Star } from 'lucide-react'
import { useEffect, useState } from 'react'
import { PageHeader } from '../../components/PageHeader'
import { listWorkplaces } from '../../lib/workplacesApi'
import type { Workplace } from '../../types/domain'

export function WorkplaceListPage() {
  const [rows, setRows] = useState<Workplace[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    void listWorkplaces()
      .then(setRows)
      .catch((e) => setError(e instanceof Error ? e.message : '사업장 목록을 불러오지 못했습니다.'))
  }, [])

  return (
    <div>
      <PageHeader
        title="사업장"
        description="다중 사업장·기본 사업장을 관리합니다."
        actions={
          <button
            type="button"
            disabled
            className="rounded-xl border border-surface-border px-4 py-2.5 text-sm font-medium text-slate-500"
            title="추후 구현"
          >
            사업장 추가
          </button>
        }
      />

      {error && (
        <div className="mb-4 rounded-xl border border-rose-500/40 bg-rose-950/30 px-4 py-3 text-sm text-rose-200">
          {error}
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        {rows.map((w) => (
          <div
            key={w.id}
            className="rounded-2xl border border-surface-border bg-surface-card p-6 shadow-lg"
          >
            <div className="flex items-start justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-blue-600/20 text-blue-300">
                  <Building2 className="h-5 w-5" />
                </div>
                <div>
                  <h2 className="text-lg font-semibold text-white">{w.name}</h2>
                  <p className="text-xs text-slate-500">{w.bizNo}</p>
                </div>
              </div>
              {w.default && (
                <span className="inline-flex items-center gap-1 rounded-md bg-amber-900/40 px-2 py-1 text-xs font-medium text-amber-200">
                  <Star className="h-3 w-3 fill-current" />
                  기본
                </span>
              )}
            </div>
            <p className="mt-4 text-sm leading-relaxed text-slate-400">{w.address}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
