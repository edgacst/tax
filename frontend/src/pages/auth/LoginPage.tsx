import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'

export function LoginPage() {
  const login = useAuthStore((s) => s.login)
  const user = useAuthStore((s) => s.user)
  const navigate = useNavigate()
  const [email, setEmail] = useState('demo@taxflow.kr')
  const [password, setPassword] = useState('TaxFlow123!')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (user) {
    return <Navigate to="/" replace />
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      await login(email, password)
      navigate('/', { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : '로그인에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-surface px-4 py-12">
      <div className="w-full max-w-md rounded-2xl border border-surface-border bg-surface-card p-8 shadow-2xl">
        <h1 className="text-center text-2xl font-bold text-white">TaxFlow</h1>
        <p className="mt-2 text-center text-sm text-slate-400">전자세금계산서 SaaS</p>

        {error && (
          <div className="mt-4 rounded-xl border border-rose-500/40 bg-rose-950/30 px-4 py-3 text-sm text-rose-200">
            {error}
          </div>
        )}

        <form className="mt-8 space-y-4" onSubmit={(e) => void onSubmit(e)}>
          <div>
            <label htmlFor="email" className="mb-1 block text-xs font-medium text-slate-400">
              이메일
            </label>
            <input
              id="email"
              type="email"
              autoComplete="username"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/30"
              required
            />
          </div>
          <div>
            <label htmlFor="password" className="mb-1 block text-xs font-medium text-slate-400">
              비밀번호
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/30"
              required
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-xl bg-gradient-to-r from-blue-600 to-blue-700 py-3 text-sm font-semibold text-white shadow-lg transition hover:brightness-110 disabled:opacity-60"
          >
            {loading ? '로그인 중…' : '로그인'}
          </button>
        </form>
        <p className="mt-4 text-center text-xs text-slate-500">
          데모: demo@taxflow.kr / TaxFlow123!
        </p>
      </div>
    </div>
  )
}
