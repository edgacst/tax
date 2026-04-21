import {
  Building2,
  FileText,
  LayoutDashboard,
  LogOut,
  Menu,
  Settings,
  Shield,
  Users,
  X,
} from 'lucide-react'
import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'

const nav = [
  { to: '/', label: '대시보드', icon: LayoutDashboard, end: true },
  { to: '/invoices', label: '세금계산서', icon: FileText, end: false },
  { to: '/partners', label: '거래처', icon: Users, end: false },
  { to: '/workplaces', label: '사업장', icon: Building2, end: false },
  { to: '/certificates', label: '공인인증서', icon: Shield, end: false },
  { to: '/settings', label: '설정', icon: Settings, end: false },
] as const

function navClass(active: boolean) {
  return [
    'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition',
    active ? 'bg-blue-600/25 text-blue-100' : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200',
  ].join(' ')
}

export function AppShell() {
  const [open, setOpen] = useState(false)
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)
  const navigate = useNavigate()

  return (
    <div className="flex min-h-screen bg-surface text-slate-100">
      {/* 모바일 오버레이 */}
      {open && (
        <button
          type="button"
          aria-label="메뉴 닫기"
          className="fixed inset-0 z-40 bg-black/60 lg:hidden"
          onClick={() => setOpen(false)}
        />
      )}

      <aside
        className={[
          'fixed inset-y-0 left-0 z-50 flex w-64 min-h-screen transform flex-col border-r border-surface-border bg-surface-card transition-transform lg:static lg:translate-x-0',
          open ? 'translate-x-0' : '-translate-x-full lg:translate-x-0',
        ].join(' ')}
      >
        <div className="flex h-14 shrink-0 items-center justify-between border-b border-surface-border px-4 lg:h-16">
          <span className="text-lg font-bold tracking-tight text-white">TaxFlow</span>
          <button
            type="button"
            className="rounded-lg p-2 text-slate-400 hover:bg-slate-800 hover:text-white lg:hidden"
            onClick={() => setOpen(false)}
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <nav className="flex-1 space-y-1 overflow-y-auto p-3">
          {nav.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              onClick={() => setOpen(false)}
              className={({ isActive }) => navClass(isActive)}
            >
              <Icon className="h-4 w-4 shrink-0 opacity-80" />
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="mt-auto shrink-0 border-t border-surface-border p-3">
          <div className="mb-2 truncate rounded-lg bg-slate-900/60 px-3 py-2 text-xs text-slate-400">
            <div className="truncate font-medium text-slate-200">{user?.name}</div>
            <div className="truncate">{user?.email}</div>
          </div>
          <button
            type="button"
            onClick={() => {
              logout()
              navigate('/login', { replace: true })
            }}
            className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium text-slate-400 transition hover:bg-rose-950/40 hover:text-rose-200"
          >
            <LogOut className="h-4 w-4" />
            로그아웃
          </button>
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col lg:ml-0">
        <header className="sticky top-0 z-30 flex h-14 items-center gap-3 border-b border-surface-border bg-surface/90 px-4 backdrop-blur lg:h-16 lg:px-6">
          <button
            type="button"
            className="rounded-lg p-2 text-slate-300 hover:bg-slate-800 lg:hidden"
            onClick={() => setOpen(true)}
          >
            <Menu className="h-5 w-5" />
          </button>
          <div className="text-sm text-slate-500">전자세금계산서 · 멀티테넌트 SaaS</div>
        </header>
        <main className="flex-1 px-4 py-6 lg:px-8 lg:py-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
