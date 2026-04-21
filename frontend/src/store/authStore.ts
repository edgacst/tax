import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type AuthUser = {
  email: string
  name: string
}

type AuthState = {
  user: AuthUser | null
  /** 비밀번호는 저장하지 않음. 시연용으로 이메일만으로 로그인 처리 */
  login: (email: string, _password: string) => void
  logout: () => void
}

function deriveName(email: string): string {
  const local = email.split('@')[0]?.trim()
  if (!local) return '사용자'
  return local.charAt(0).toUpperCase() + local.slice(1)
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      login: (email, _password) => {
        void _password
        const trimmed = email.trim().toLowerCase()
        set({ user: { email: trimmed, name: deriveName(trimmed) } })
      },
      logout: () => set({ user: null }),
    }),
    { name: 'taxflow-auth' },
  ),
)
