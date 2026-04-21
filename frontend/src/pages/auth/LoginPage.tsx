import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'

export function LoginPage() {
  const login = useAuthStore((s) => s.login)
  const user = useAuthStore((s) => s.user)
  const navigate = useNavigate()
  const [email, setEmail] = useState('demo@taxflow.kr')
  const [password, setPassword] = useState('')

  if (user) {
    return <Navigate to="/" replace />
  }

  function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    login(email, password)
    navigate('/', { replace: true })
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-surface px-4 py-12">
      <div className="w-full max-w-md rounded-2xl border border-surface-border bg-surface-card p-8 shadow-2xl">
        <h1 className="text-center text-2xl font-bold text-white">TaxFlow</h1>
        <p className="mt-2 text-center text-sm text-slate-400">전자세금계산서 SaaS · 시연 로그인</p>

        <form className="mt-8 space-y-4" onSubmit={onSubmit}>
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
              className="w-full rounded-xl border border-surface-border bg-slate-900/50 px-3 py-2.5 text-sm text-white outline-none ring-blue-500/0 transition focus:border-blue-500 focus:ring-2 focus:ring-blue-500/30"
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
              placeholder="시연: 아무 값"
            />
          </div>
          <button
            type="submit"
            className="w-full rounded-xl bg-gradient-to-r from-blue-600 to-blue-700 py-3 text-sm font-semibold text-white shadow-lg transition hover:brightness-110"
          >
            로그인
          </button>
        </form>
        <p className="mt-4 text-center text-xs text-slate-500">
          비밀번호는 검증하지 않습니다. API 연동 시 Spring Security JWT로 교체하면 됩니다.
        </p>
      </div>
    </div>
  )
}
