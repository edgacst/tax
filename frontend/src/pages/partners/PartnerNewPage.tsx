import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { PageHeader } from '../../components/PageHeader'
import { createPartner } from '../../lib/partnersApi'

export function PartnerNewPage() {
  const navigate = useNavigate()
  const [bizNo, setBizNo] = useState('')
  const [name, setName] = useState('')
  const [ceo, setCeo] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await createPartner({ bizNo, name, ceo, email, phone })
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
        description="사업자 진위확인 API는 백엔드 연동 후 호출합니다."
        actions={
          <Link to="/partners" className="text-sm font-medium text-slate-400 hover:text-blue-300">
            목록
          </Link>
        }
      />

      {error && (
        <div className="mb-4 rounded-xl border border-rose-500/40 bg-rose-950/30 px-4 py-3 text-sm text-rose-200">
          {error}
        </div>
      )}

      <form
        onSubmit={onSubmit}
        className="mx-auto max-w-2xl space-y-5 rounded-2xl border border-surface-border bg-surface-card p-6 shadow-lg"
      >
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <label className="mb-1 block text-xs text-slate-500">사업자등록번호</label>
            <input
              value={bizNo}
              onChange={(e) => setBizNo(e.target.value)}
              placeholder="0000000000"
              maxLength={10}
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
              required
            />
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
