import type { CertificateMeta } from '../../types/domain'

export const mockCertificates: CertificateMeta[] = [
  {
    id: 'c-1',
    type: '범용(기업)',
    subject: 'TAXFLOW DEMO CERT',
    validTo: '2027-03-15',
    status: 'active',
  },
  {
    id: 'c-2',
    type: '전자세금용',
    subject: 'TAXFLOW NTS SIGN',
    validTo: '2026-06-01',
    status: 'expiring',
  },
]
