import { Upload } from 'lucide-react'
import { CertStatusBadge } from '../../components/StatusBadge'
import { PageHeader } from '../../components/PageHeader'
import { mockCertificates } from '../../lib/mock/certificates'

export function CertificateListPage() {
  return (
    <div>
      <PageHeader
        title="공인인증서"
        description="홈택스 전송용 인증서는 Vault에 암호화 저장됩니다. (UI만)"
        actions={
          <button
            type="button"
            disabled
            className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white opacity-60"
            title="목업"
          >
            <Upload className="h-4 w-4" />
            인증서 등록
          </button>
        }
      />

      <div className="grid gap-4 md:grid-cols-2">
        {mockCertificates.map((c) => (
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
        ))}
      </div>
    </div>
  )
}
