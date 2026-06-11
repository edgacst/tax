import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { loginApi, logoutApi, refreshApi, type UserProfile } from '../lib/authApi'

export type AuthUser = UserProfile

type AuthState = {
  user: AuthUser | null
  accessToken: string | null
  refreshToken: string | null
  login: (email: string, password: string) => Promise<void>
  logout: () => Promise<void>
  refreshSession: () => Promise<boolean>
  setSession: (accessToken: string, refreshToken: string, user: AuthUser) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      setSession: (accessToken, refreshToken, user) =>
        set({ accessToken, refreshToken, user }),
      login: async (email, password) => {
        const res = await loginApi(email, password)
        set({
          accessToken: res.accessToken,
          refreshToken: res.refreshToken,
          user: res.user,
        })
      },
      logout: async () => {
        const rt = get().refreshToken
        set({ user: null, accessToken: null, refreshToken: null })
        await logoutApi(rt)
      },
      refreshSession: async () => {
        const rt = get().refreshToken
        if (!rt) return false
        try {
          const res = await refreshApi(rt)
          set({
            accessToken: res.accessToken,
            refreshToken: res.refreshToken,
            user: res.user,
          })
          return true
        } catch {
          set({ user: null, accessToken: null, refreshToken: null })
          return false
        }
      },
    }),
    {
      name: 'taxflow-auth',
      partialize: (s) => ({
        user: s.user,
        accessToken: s.accessToken,
        refreshToken: s.refreshToken,
      }),
    },
  ),
)
