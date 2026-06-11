import { Building2, Pencil, Plus, Star } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { PageHeader } from '../../components/PageHeader'
import { listWorkplaces } from '../../lib/workplacesApi'
import type { Workplace } from '../../types/domain'

export function WorkplaceListPage() {
  const [rows, setRows] = useState<Workplace[]>([])
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    setError(null)
    try {
      setRows(await listWorkplaces())
    } catch (e) {
      setError(e instanceof Error ? e.message : '사업장 목록을 불러오지 못했습니다.')
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  return (
    <div>
      <PageHeader
        title="사업장"
        description="다중 사업장·기본 사업장을 관리합니다."
        actions={
          <Link
            to="/workplaces/new"
            className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-500"
          >
            <Plus className="h-4 w-4" />
            사업장 추가
          </Link>
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
              <div className="flex items-center gap-2">
                {w.default && (
                  <span className="inline-flex items-center gap-1 rounded-md bg-amber-900/40 px-2 py-1 text-xs font-medium text-amber-200">
                    <Star className="h-3 w-3 fill-current" />
                    기본
                  </span>
                )}
                <Link
                  to={`/workplaces/${w.id}/edit`}
                  className="inline-flex items-center gap-1 rounded-lg border border-surface-border px-2.5 py-1.5 text-xs font-medium text-slate-300 hover:bg-slate-800"
                >
                  <Pencil className="h-3.5 w-3.5" />
                  수정
                </Link>
              </div>
            </div>
            {w.ceoName && <p className="mt-3 text-sm text-slate-400">대표: {w.ceoName}</p>}
            <p className="mt-2 text-sm leading-relaxed text-slate-400">{w.address || '주소 미입력'}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
