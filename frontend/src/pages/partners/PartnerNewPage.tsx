import { Search, ShieldCheck } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { PageHeader } from '../../components/PageHeader'
import { fetchNtsBizVerifyMeta } from '../../lib/ntsBizVerifyApi'
import { queryBizStatus, validateBiz } from '../../lib/ntsBizVerifyApi'
import { createPartner } from '../../lib/partnersApi'

export function PartnerNewPage() {
  const navigate = useNavigate()
  const [bizNo, setBizNo] = useState('')
  const [startDt, setStartDt] = useState('')
  const [name, setName] = useState('')
  const [ceo, setCeo] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [saving, setSaving] = useState(false)
  const [checking, setChecking] = useState(false)
  const [apiReady, setApiReady] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [verifyMsg, setVerifyMsg] = useState<string | null>(null)

  useEffect(() => {
    void fetchNtsBizVerifyMeta()
      .then((m) => setApiReady(m.configured))
      .catch(() => setApiReady(false))
  }, [])

  async function onStatusCheck() {
    setChecking(true)
    setError(null)
    setVerifyMsg(null)
    try {
      const digits = bizNo.replace(/\D/g, '')
      const res = await queryBizStatus([digits])
      const item = res.items[0]
      if (!item) {
        setVerifyMsg('응답 데이터가 없습니다.')
        return
      }
      setVerifyMsg(
        item.registered
          ? `등록됨 · ${item.taxType}${item.businessStatus ? ` · ${item.businessStatus}` : ''}`
          : item.taxType || '조회 완료',
      )
    } catch (e) {
      setError(e instanceof Error ? e.message : '상태조회에 실패했습니다.')
    } finally {
      setChecking(false)
    }
  }

  async function onValidateCheck() {
    setChecking(true)
    setError(null)
    setVerifyMsg(null)
    try {
      const res = await validateBiz({
        bizNo: bizNo.replace(/\D/g, ''),
        startDt: startDt.replace(/\D/g, ''),
        ceoName: ceo,
        corpName: name || undefined,
      })
      const item = res.items[0]
      if (!item) {
        setVerifyMsg('응답 데이터가 없습니다.')
        return
      }
      setVerifyMsg(item.valid ? `진위확인 일치 — ${item.message}` : `불일치 — ${item.message}`)
    } catch (e) {
      setError(e instanceof Error ? e.message : '진위확인에 실패했습니다.')
    } finally {
      setChecking(false)
    }
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await createPartner({ bizNo: bizNo.replace(/\D/g, ''), name, ceo, email, phone })
      navigate('/partners')
    } catch (err) {
      setError(err instanceof Error ? err.message : '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <PageHeader
        title="거래처 등록"
        description="국세청 사업자 상태조회·진위확인 후 저장할 수 있습니다."
        actions={
          <Link to="/partners" className="text-sm font-medium text-slate-400 hover:text-blue-300">
            목록
          </Link>
        }
      />

      {!apiReady && (
        <div className="mb-4 rounded-xl border border-amber-500/30 bg-amber-950/20 px-4 py-3 text-sm text-amber-100">
          사업자 조회 API 미설정 — 서버에 NTS_BIZ_VERIFY_SERVICE_KEY 를 넣으면 확인 버튼이 동작합니다.{' '}
          <Link to="/tools/biz-verify" className="underline hover:text-white">
            사업자 조회 화면
          </Link>
        </div>
      )}

      {error && (
        <div className="mb-4 rounded-xl border border-rose-500/40 bg-rose-950/30 px-4 py-3 text-sm text-rose-200">
          {error}
        </div>
      )}
      {verifyMsg && (
        <div className="mb-4 rounded-xl border border-emerald-500/30 bg-emerald-950/20 px-4 py-3 text-sm text-emerald-100">
          {verifyMsg}
        </div>
      )}

      <form
        onSubmit={onSubmit}
        className="mx-auto max-w-2xl space-y-5 rounded-2xl border border-surface-border bg-surface-card p-6 shadow-lg"
      >
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <label className="mb-1 block text-xs text-slate-500">사업자등록번호</label>
            <div className="flex flex-wrap gap-2">
              <input
                value={bizNo}
                onChange={(e) => setBizNo(e.target.value)}
                placeholder="0000000000"
                maxLength={12}
                className="min-w-0 flex-1 rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
                required
              />
              <button
                type="button"
                disabled={!apiReady || checking || bizNo.replace(/\D/g, '').length !== 10}
                onClick={() => void onStatusCheck()}
                className="inline-flex items-center gap-1 rounded-xl border border-surface-border px-3 py-2 text-xs font-medium text-slate-200 hover:bg-slate-800 disabled:opacity-50"
              >
                <Search className="h-3.5 w-3.5" />
                상태조회
              </button>
            </div>
          </div>
          <div>
            <label className="mb-1 block text-xs text-slate-500">개업일자 (진위확인용)</label>
            <input
              value={startDt}
              onChange={(e) => setStartDt(e.target.value)}
              placeholder="YYYYMMDD"
              maxLength={8}
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
            />
          </div>
          <div className="flex items-end">
            <button
              type="button"
              disabled={
                !apiReady ||
                checking ||
                bizNo.replace(/\D/g, '').length !== 10 ||
                startDt.replace(/\D/g, '').length !== 8 ||
                !ceo.trim()
              }
              onClick={() => void onValidateCheck()}
              className="inline-flex w-full items-center justify-center gap-1 rounded-xl border border-violet-500/40 bg-violet-950/30 px-3 py-2.5 text-xs font-medium text-violet-200 hover:bg-violet-900/40 disabled:opacity-50"
            >
              <ShieldCheck className="h-3.5 w-3.5" />
              진위확인
            </button>
          </div>
          <div className="sm:col-span-2">
            <label className="mb-1 block text-xs text-slate-500">상호</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
              required
            />
          </div>
          <div>
            <label className="mb-1 block text-xs text-slate-500">대표자명</label>
            <input
              value={ceo}
              onChange={(e) => setCeo(e.target.value)}
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs text-slate-500">전화</label>
            <input
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
            />
          </div>
          <div className="sm:col-span-2">
            <label className="mb-1 block text-xs text-slate-500">이메일</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
            />
          </div>
        </div>
        <div className="flex justify-end gap-2 border-t border-surface-border pt-4">
          <Link
            to="/partners"
            className="rounded-xl border border-surface-border px-4 py-2 text-sm text-slate-300 hover:bg-slate-800"
          >
            취소
          </Link>
          <button
            type="submit"
            disabled={saving}
            className="rounded-xl bg-blue-600 px-5 py-2 text-sm font-semibold text-white hover:bg-blue-500 disabled:opacity-60"
          >
            {saving ? '저장 중…' : '저장'}
          </button>
        </div>
      </form>
    </div>
  )
}
