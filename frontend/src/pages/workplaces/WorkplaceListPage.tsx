import { Building2, Star } from 'lucide-react'
import { PageHeader } from '../../components/PageHeader'
import { mockWorkplaces } from '../../lib/mock/workplaces'

export function WorkplaceListPage() {
  return (
    <div>
      <PageHeader
        title="사업장"
        description="다중 사업장·기본 사업장을 관리합니다. (목업)"
        actions={
          <button
            type="button"
            disabled
            className="rounded-xl border border-surface-border px-4 py-2.5 text-sm font-medium text-slate-500"
            title="목업"
          >
            사업장 추가
          </button>
        }
      />

      <div className="grid gap-4 md:grid-cols-2">
        {mockWorkplaces.map((w) => (
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
