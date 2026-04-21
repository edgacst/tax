import { NavLink, Outlet } from 'react-router-dom'

const linkClass = ({ isActive }: { isActive: boolean }) =>
  [
    'rounded-lg px-3 py-2 text-sm font-medium transition',
    isActive ? 'bg-slate-700 text-white' : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200',
  ].join(' ')

export function AppLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="border-b border-surface-border bg-surface-card/80 backdrop-blur">
        <div className="mx-auto flex max-w-5xl flex-col gap-3 px-4 py-3 sm:flex-row sm:items-center sm:justify-between">
          <NavLink to="/" className="text-lg font-bold tracking-tight text-white">
            TaxFlow
          </NavLink>
          <nav className="flex flex-wrap gap-1">
            <NavLink to="/" end className={linkClass}>
              홈
            </NavLink>
            <NavLink to="/invoices" className={linkClass}>
              세금계산서
            </NavLink>
            <NavLink to="/settings" className={linkClass}>
              설정
            </NavLink>
          </nav>
        </div>
      </header>
      <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
