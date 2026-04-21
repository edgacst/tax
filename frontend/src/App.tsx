import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './layouts/AppLayout'
import { HomePage } from './pages/HomePage'
import { InvoicesPlaceholderPage } from './pages/InvoicesPlaceholderPage'
import { SettingsPlaceholderPage } from './pages/SettingsPlaceholderPage'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route index element={<HomePage />} />
          <Route path="invoices" element={<InvoicesPlaceholderPage />} />
          <Route path="settings" element={<SettingsPlaceholderPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
