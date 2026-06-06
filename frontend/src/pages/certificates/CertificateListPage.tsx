import { Upload } from 'lucide-react'
import { useEffect, useState } from 'react'
import { CertStatusBadge } from '../../components/StatusBadge'
import { PageHeader } from '../../components/PageHeader'
import { listCertificates } from '../../lib/certificatesApi'
import type { CertificateMeta } from '../../types/domain'

export function CertificateListPage() {
  const [rows, setRows] = useState<CertificateMeta[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    void listCertificates()
      .then(setRows)
      .catch((e) => setError(e instanceof Error ? e.message : '인증서 목록을 불러오지 못했습니다.'))
  }, [])

  return (
    <div>
      <PageHeader
        title="공인인증서"
        description="홈택스 전송용 인증서는 Vault에 암호화 저장됩니다."
        actions={
          <button
            type="button"
            disabled
            className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white opacity-60"
            title="추후 구현"
          >
            <Upload className="h-4 w-4" />
            인증서 등록
          </button>
        }
      />

      {error && (
        <div className="mb-4 rounded-xl border border-rose-500/40 bg-rose-950/30 px-4 py-3 text-sm text-rose-200">
          {error}
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2">
        {rows.length === 0 && !error ? (
          <p className="text-sm text-slate-500">등록된 인증서가 없습니다.</p>
        ) : (
          rows.map((c) => (
            <div
              key={c.id}
              className="rounded-2xl border border-surface-border bg-surface-card p-6 shadow-lg"
            >
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-xs text-slate-500">{c.type}</p>
                  <h2 className="mt-1 font-mono text-sm font-medium text-slate-200">{c.subject}</h2>
                </div>
                <CertStatusBadge status={c.status} />
              </div>
              <p className="mt-4 text-sm text-slate-400">
                만료일 <span className="tabular-nums text-slate-200">{c.validTo}</span>
              </p>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
