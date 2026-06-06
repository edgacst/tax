import { Search, ShieldCheck } from 'lucide-react'
import { useEffect, useState } from 'react'
import { PageHeader } from '../../components/PageHeader'
import {
  fetchNtsBizVerifyMeta,
  queryBizStatus,
  validateBiz,
  type BizStatusItem,
  type BizValidateItem,
  type NtsBizVerifyMeta,
} from '../../lib/ntsBizVerifyApi'

type Tab = 'status' | 'validate'

export function BizVerifyPage() {
  const [tab, setTab] = useState<Tab>('status')
  const [meta, setMeta] = useState<NtsBizVerifyMeta | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const [statusInput, setStatusInput] = useState('')
  const [statusResults, setStatusResults] = useState<BizStatusItem[]>([])

  const [bizNo, setBizNo] = useState('')
  const [startDt, setStartDt] = useState('')
  const [ceoName, setCeoName] = useState('')
  const [corpName, setCorpName] = useState('')
  const [validateResult, setValidateResult] = useState<BizValidateItem | null>(null)

  useEffect(() => {
    void fetchNtsBizVerifyMeta()
      .then(setMeta)
      .catch(() => setMeta({ configured: false, enabled: false, baseUrl: '' }))
  }, [])

  const onStatus = async () => {
    setLoading(true)
    setError(null)
    setStatusResults([])
    try {
      const numbers = statusInput
        .split(/[\s,]+/)
        .map((s) => s.replace(/\D/g, ''))
        .filter((s) => s.length === 10)
      if (numbers.length === 0) {
        setError('10자리 사업자등록번호를 입력하세요. (여러 건은 쉼표·줄바꿈 구분)')
        return
      }
      const res = await queryBizStatus(numbers)
      setStatusResults(res.items)
    } catch (e) {
      setError(e instanceof Error ? e.message : '상태조회에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const onValidate = async () => {
    setLoading(true)
    setError(null)
    setValidateResult(null)
    try {
      const dt = startDt.replace(/\D/g, '')
      const res = await validateBiz({
        bizNo: bizNo.replace(/\D/g, ''),
        startDt: dt,
        ceoName,
        corpName: corpName || undefined,
      })
      setValidateResult(res.items[0] ?? null)
    } catch (e) {
      setError(e instanceof Error ? e.message : '진위확인에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <PageHeader
        title="사업자 조회"
        description="국세청 사업자등록정보 상태조회·진위확인 (공공데이터포털 Open API)"
      />

      <div className="mb-6 rounded-xl border border-surface-border bg-slate-900/40 px-4 py-3 text-sm text-slate-300">
        {meta?.configured ? (
          <span className="text-emerald-300">API 연동 준비됨 — 조회 가능</span>
        ) : (
          <span>
            서버에{' '}
            <code className="rounded bg-slate-800 px-1 text-xs">NTS_BIZ_VERIFY_ENABLED=true</code> 와{' '}
            <code className="rounded bg-slate-800 px-1 text-xs">NTS_BIZ_VERIFY_SERVICE_KEY</code> 를
            설정하세요. (.env.example 참고)
          </span>
        )}
      </div>

      <div className="mb-6 flex gap-2 rounded-2xl border border-surface-border bg-slate-900/30 p-1">
        <button
          type="button"
          onClick={() => setTab('status')}
          className={`rounded-xl px-4 py-2 text-sm font-medium ${
            tab === 'status' ? 'bg-slate-800 text-white' : 'text-slate-400'
          }`}
        >
          상태조회
        </button>
        <button
          type="button"
          onClick={() => setTab('validate')}
          className={`rounded-xl px-4 py-2 text-sm font-medium ${
            tab === 'validate' ? 'bg-slate-800 text-white' : 'text-slate-400'
          }`}
        >
          진위확인
        </button>
      </div>

      {error && (
        <div className="mb-4 rounded-xl border border-rose-500/40 bg-rose-950/30 px-4 py-3 text-sm text-rose-200">
          {error}
        </div>
      )}

      {tab === 'status' ? (
        <section className="max-w-2xl space-y-4 rounded-2xl border border-surface-border bg-surface-card p-6">
          <label className="block text-xs text-slate-500">사업자등록번호 (최대 100건)</label>
          <textarea
            value={statusInput}
            onChange={(e) => setStatusInput(e.target.value)}
            rows={3}
            placeholder="1234567890&#10;2345678901"
            className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 font-mono text-sm text-white"
          />
          <button
            type="button"
            onClick={() => void onStatus()}
            disabled={loading}
            className="inline-flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-500 disabled:opacity-60"
          >
            <Search className="h-4 w-4" />
            {loading ? '조회 중…' : '상태조회'}
          </button>
          {statusResults.length > 0 && (
            <ul className="space-y-3 border-t border-surface-border pt-4">
              {statusResults.map((r) => (
                <li key={r.bizNo} className="rounded-lg bg-slate-900/50 p-3 text-sm">
                  <p className="font-mono font-medium text-white">{r.bizNo}</p>
                  <p className={r.registered ? 'text-emerald-300' : 'text-amber-300'}>{r.taxType}</p>
                  {r.businessStatus && (
                    <p className="text-slate-400">사업자 상태: {r.businessStatus}</p>
                  )}
                </li>
              ))}
            </ul>
          )}
        </section>
      ) : (
        <section className="max-w-2xl space-y-4 rounded-2xl border border-surface-border bg-surface-card p-6">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <label className="mb-1 block text-xs text-slate-500">사업자등록번호 *</label>
              <input
                value={bizNo}
                onChange={(e) => setBizNo(e.target.value)}
                maxLength={12}
                className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
              />
            </div>
            <div>
              <label className="mb-1 block text-xs text-slate-500">개업일자 (YYYYMMDD) *</label>
              <input
                value={startDt}
                onChange={(e) => setStartDt(e.target.value)}
                placeholder="20000101"
                maxLength={8}
                className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
              />
            </div>
            <div>
              <label className="mb-1 block text-xs text-slate-500">대표자명 *</label>
              <input
                value={ceoName}
                onChange={(e) => setCeoName(e.target.value)}
                className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
              />
            </div>
            <div className="sm:col-span-2">
              <label className="mb-1 block text-xs text-slate-500">상호 (선택)</label>
              <input
                value={corpName}
                onChange={(e) => setCorpName(e.target.value)}
                className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
              />
            </div>
          </div>
          <button
            type="button"
            onClick={() => void onValidate()}
            disabled={loading}
            className="inline-flex items-center gap-2 rounded-xl bg-violet-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-violet-500 disabled:opacity-60"
          >
            <ShieldCheck className="h-4 w-4" />
            {loading ? '확인 중…' : '진위확인'}
          </button>
          {validateResult && (
            <div
              className={`rounded-lg p-3 text-sm ${
                validateResult.valid
                  ? 'border border-emerald-500/30 bg-emerald-950/30 text-emerald-100'
                  : 'border border-amber-500/30 bg-amber-950/30 text-amber-100'
              }`}
            >
              <p className="font-medium">{validateResult.valid ? '일치' : '불일치'}</p>
              <p className="mt-1 text-xs opacity-90">{validateResult.message}</p>
            </div>
          )}
        </section>
      )}
    </div>
  )
}
