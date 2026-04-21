import { Bell, Lock, User } from 'lucide-react'
import { useState } from 'react'
import { PageHeader } from '../../components/PageHeader'
import { useAuthStore } from '../../store/authStore'

const tabs = [
  { id: 'profile', label: '프로필', icon: User },
  { id: 'notify', label: '알림', icon: Bell },
  { id: 'security', label: '보안', icon: Lock },
] as const

type TabId = (typeof tabs)[number]['id']

export function SettingsPage() {
  const [tab, setTab] = useState<TabId>('profile')
  const user = useAuthStore((s) => s.user)

  return (
    <div>
      <PageHeader title="설정" description="계정·알림·보안 정책을 구성합니다. (토글은 목업)" />

      <div className="flex flex-wrap gap-2 border-b border-surface-border pb-4">
        {tabs.map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            type="button"
            onClick={() => setTab(id)}
            className={[
              'inline-flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-medium transition',
              tab === id
                ? 'bg-blue-600/25 text-blue-100 ring-1 ring-blue-500/40'
                : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200',
            ].join(' ')}
          >
            <Icon className="h-4 w-4" />
            {label}
          </button>
        ))}
      </div>

      <div className="mt-6 max-w-2xl space-y-6 rounded-2xl border border-surface-border bg-surface-card p-6">
        {tab === 'profile' && (
          <div className="space-y-4">
            <h2 className="text-sm font-semibold text-slate-200">로그인 정보</h2>
            <div className="grid gap-3 text-sm">
              <div className="flex justify-between gap-4 border-b border-surface-border py-2">
                <span className="text-slate-500">이름</span>
                <span className="text-slate-100">{user?.name}</span>
              </div>
              <div className="flex justify-between gap-4 border-b border-surface-border py-2">
                <span className="text-slate-500">이메일</span>
                <span className="text-slate-200">{user?.email}</span>
              </div>
            </div>
            <p className="text-xs text-slate-500">실제 프로필 수정은 백엔드 Users API와 연동합니다.</p>
          </div>
        )}

        {tab === 'notify' && (
          <div className="space-y-4">
            <ToggleRow label="인증서 만료 알림" description="만료 30·15·7일 전 메일" defaultOn />
            <ToggleRow label="승인 결과 알림" description="국세청 승인·거부 시 알림" defaultOn />
            <ToggleRow label="월간 리포트" description="매출·매입 요약" defaultOn={false} />
          </div>
        )}

        {tab === 'security' && (
          <div className="space-y-4">
            <ToggleRow label="2단계 인증 (TOTP)" description="로그인 시 OTP 입력" defaultOn={false} />
            <ToggleRow label="허용 IP 대역" description="테넌트별 접속 제한" defaultOn={false} />
            <p className="text-xs text-slate-500">세션 타임아웃·동시 로그인 제한은 서버 정책과 맞춥니다.</p>
          </div>
        )}
      </div>
    </div>
  )
}

function ToggleRow({
  label,
  description,
  defaultOn,
}: {
  label: string
  description: string
  defaultOn: boolean
}) {
  const [on, setOn] = useState(defaultOn)
  return (
    <div className="flex items-center justify-between gap-4 rounded-xl border border-surface-border bg-slate-900/30 px-4 py-3">
      <div>
        <p className="text-sm font-medium text-slate-100">{label}</p>
        <p className="text-xs text-slate-500">{description}</p>
      </div>
      <button
        type="button"
        role="switch"
        aria-checked={on}
        onClick={() => setOn(!on)}
        className={[
          'relative h-7 w-12 shrink-0 rounded-full transition',
          on ? 'bg-blue-600' : 'bg-slate-700',
        ].join(' ')}
      >
        <span
          className={[
            'absolute top-0.5 h-6 w-6 rounded-full bg-white shadow transition',
            on ? 'left-5' : 'left-0.5',
          ].join(' ')}
        />
      </button>
    </div>
  )
}
