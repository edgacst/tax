import { Trash2, Upload } from 'lucide-react'
import { useCallback, useEffect, useRef, useState } from 'react'
import { CertStatusBadge } from '../../components/StatusBadge'
import { PageHeader } from '../../components/PageHeader'
import { deleteCertificate, listCertificates, uploadCertificate } from '../../lib/certificatesApi'
import type { CertificateMeta } from '../../types/domain'

export function CertificateListPage() {
  const [rows, setRows] = useState<CertificateMeta[]>([])
  const [error, setError] = useState<string | null>(null)
  const [uploadOpen, setUploadOpen] = useState(false)
  const [password, setPassword] = useState('')
  const [uploading, setUploading] = useState(false)
  const fileRef = useRef<HTMLInputElement>(null)

  const load = useCallback(async () => {
    setError(null)
    try {
      setRows(await listCertificates())
    } catch (e) {
      setError(e instanceof Error ? e.message : '인증서 목록을 불러오지 못했습니다.')
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  async function onUpload() {
    const file = fileRef.current?.files?.[0]
    if (!file) {
      setError('PKCS#12 파일(.p12/.pfx)을 선택하세요.')
      return
    }
    if (!password) {
      setError('인증서 비밀번호를 입력하세요.')
      return
    }
    setUploading(true)
    setError(null)
    try {
      await uploadCertificate(file, password)
      setUploadOpen(false)
      setPassword('')
      if (fileRef.current) fileRef.current.value = ''
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : '업로드에 실패했습니다.')
    } finally {
      setUploading(false)
    }
  }

  async function onDelete(id: string) {
    if (!window.confirm('인증서를 삭제할까요?')) return
    setError(null)
    try {
      await deleteCertificate(id)
      await load()
    } catch (e) {
      setError(e instanceof Error ? e.message : '삭제에 실패했습니다.')
    }
  }

  return (
    <div>
      <PageHeader
        title="공인인증서"
        description="홈택스 전송용 인증서는 AES-256으로 암호화 저장됩니다."
        actions={
          <button
            type="button"
            onClick={() => setUploadOpen((v) => !v)}
            className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-500"
          >
            <Upload className="h-4 w-4" />
            인증서 등록
          </button>
        }
      />

      {uploadOpen && (
        <section className="mb-6 max-w-xl space-y-3 rounded-2xl border border-surface-border bg-surface-card p-5">
          <p className="text-sm text-slate-400">PKCS#12 (.p12 / .pfx) 파일과 인증서 비밀번호</p>
          <input
            ref={fileRef}
            type="file"
            accept=".p12,.pfx"
            className="block w-full text-sm text-slate-300"
          />
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="인증서 비밀번호"
            className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
          />
          <button
            type="button"
            disabled={uploading}
            onClick={() => void onUpload()}
            className="rounded-xl bg-violet-600 px-4 py-2 text-sm font-semibold text-white hover:bg-violet-500 disabled:opacity-60"
          >
            {uploading ? '업로드 중…' : '저장'}
          </button>
        </section>
      )}

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
                <div className="flex items-center gap-2">
                  <CertStatusBadge status={c.status} />
                  <button
                    type="button"
                    onClick={() => void onDelete(c.id)}
                    className="rounded-lg border border-surface-border p-1.5 text-slate-400 hover:bg-slate-800 hover:text-rose-300"
                    title="삭제"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
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
