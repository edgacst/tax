import { create } from 'zustand'

type UiState = {
  lastPingJson: string | null
  lastHttpStatus: number | null
  lastError: string | null
  setPingResult: (payload: { json: string; status: number; error: string | null }) => void
}

export const useUiStore = create<UiState>((set) => ({
  lastPingJson: null,
  lastHttpStatus: null,
  lastError: null,
  setPingResult: ({ json, status, error }) =>
    set({ lastPingJson: json, lastHttpStatus: status, lastError: error }),
}))
