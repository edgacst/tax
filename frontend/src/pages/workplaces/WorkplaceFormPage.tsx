import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { PageHeader } from '../../components/PageHeader'
import { createWorkplace, getWorkplace, updateWorkplace } from '../../lib/workplacesApi'

export function WorkplaceFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)

  const [name, setName] = useState('')
  const [bizNo, setBizNo] = useState('')
  const [address, setAddress] = useState('')
  const [ceoName, setCeoName] = useState('')
  const [bizType, setBizType] = useState('')
  const [bizItem, setBizItem] = useState('')
  const [phone, setPhone] = useState('')
  const [email, setEmail] = useState('')
  const [isDefault, setIsDefault] = useState(false)
  const [loading, setLoading] = useState(isEdit)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    void getWorkplace(id)
      .then((w) => {
        setName(w.name)
        setBizNo(w.bizNo)
        setAddress(w.address)
        setCeoName(w.ceoName)
        setBizType(w.bizType)
        setBizItem(w.bizItem)
        setPhone(w.phone)
        setEmail(w.email)
        setIsDefault(w.default)
      })
      .catch((e) => setError(e instanceof Error ? e.message : '사업장 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [id])

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    const digits = bizNo.replace(/\D/g, '')
    if (digits.length !== 10) {
      setError('사업자등록번호는 10자리 숫자여야 합니다.')
      setSaving(false)
      return
    }
    const payload = {
      name: name.trim(),
      bizNo: digits,
      address,
      ceoName,
      bizType,
      bizItem,
      phone,
      email,
      isDefault,
    }
    try {
      if (isEdit && id) {
        await updateWorkplace(id, payload)
      } else {
        await createWorkplace(payload)
      }
      navigate('/workplaces')
    } catch (err) {
      setError(err instanceof Error ? err.message : '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <p className="text-sm text-slate-400">불러오는 중…</p>
  }

  return (
    <div>
      <PageHeader
        title={isEdit ? '사업장 수정' : '사업장 등록'}
        description="세금계산서 발행 단위가 되는 사업장 정보를 입력합니다."
        actions={
          <Link to="/workplaces" className="text-sm font-medium text-slate-400 hover:text-blue-300">
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
            <label className="mb-1 block text-xs text-slate-500">사업장명 *</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
              required
            />
          </div>
          <div className="sm:col-span-2">
            <label className="mb-1 block text-xs text-slate-500">사업자등록번호 *</label>
            <input
              value={bizNo}
              onChange={(e) => setBizNo(e.target.value)}
              placeholder="0000000000"
              maxLength={12}
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 font-mono text-sm text-white"
              required
            />
          </div>
          <div>
            <label className="mb-1 block text-xs text-slate-500">대표자명</label>
            <input
              value={ceoName}
              onChange={(e) => setCeoName(e.target.value)}
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
          <div>
            <label className="mb-1 block text-xs text-slate-500">업태</label>
            <input
              value={bizType}
              onChange={(e) => setBizType(e.target.value)}
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs text-slate-500">종목</label>
            <input
              value={bizItem}
              onChange={(e) => setBizItem(e.target.value)}
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white"
            />
          </div>
          <div className="sm:col-span-2">
            <label className="mb-1 block text-xs text-slate-500">주소</label>
            <input
              value={address}
              onChange={(e) => setAddress(e.target.value)}
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
          <div className="sm:col-span-2">
            <label className="inline-flex cursor-pointer items-center gap-2 text-sm text-slate-300">
              <input
                type="checkbox"
                checked={isDefault}
                onChange={(e) => setIsDefault(e.target.checked)}
                className="rounded border-surface-border bg-slate-900"
              />
              기본 사업장으로 설정
            </label>
          </div>
        </div>
        <div className="flex justify-end gap-2 border-t border-surface-border pt-4">
          <Link
            to="/workplaces"
            className="rounded-xl border border-surface-border px-4 py-2 text-sm text-slate-300 hover:bg-slate-800"
          >
            취소
          </Link>
          <button
            type="submit"
            disabled={saving}
            className="rounded-xl bg-blue-600 px-5 py-2 text-sm font-semibold text-white hover:bg-blue-500 disabled:opacity-60"
          >
            {saving ? '저장 중…' : isEdit ? '수정' : '등록'}
          </button>
        </div>
      </form>
    </div>
  )
}
