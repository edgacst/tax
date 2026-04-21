import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './layouts/AppShell'
import { LoginPage } from './pages/auth/LoginPage'
import { CertificateListPage } from './pages/certificates/CertificateListPage'
import { DashboardPage } from './pages/dashboard/DashboardPage'
import { InvoiceDetailPage } from './pages/invoices/InvoiceDetailPage'
import { InvoiceListPage } from './pages/invoices/InvoiceListPage'
import { InvoiceNewPage } from './pages/invoices/InvoiceNewPage'
import { PartnerListPage } from './pages/partners/PartnerListPage'
import { PartnerNewPage } from './pages/partners/PartnerNewPage'
import { SettingsPage } from './pages/settings/SettingsPage'
import { WorkplaceListPage } from './pages/workplaces/WorkplaceListPage'
import { ProtectedRoute } from './routes/ProtectedRoute'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<AppShell />}>
            <Route index element={<DashboardPage />} />
            <Route path="invoices" element={<InvoiceListPage />} />
            <Route path="invoices/new" element={<InvoiceNewPage />} />
            <Route path="invoices/:id" element={<InvoiceDetailPage />} />
            <Route path="partners" element={<PartnerListPage />} />
            <Route path="partners/new" element={<PartnerNewPage />} />
            <Route path="workplaces" element={<WorkplaceListPage />} />
            <Route path="certificates" element={<CertificateListPage />} />
            <Route path="settings" element={<SettingsPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
