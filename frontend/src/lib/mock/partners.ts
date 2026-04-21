import type { Partner } from '../../types/domain'

export const mockPartners: Partner[] = [
  {
    id: 'p-1',
    bizNo: '1234567890',
    name: '(주)한빛유통',
    ceo: '김한빛',
    email: 'ap@hanbit.example',
    phone: '02-1234-5678',
    favorite: true,
  },
  {
    id: 'p-2',
    bizNo: '2345678901',
    name: '미래IT솔루션',
    ceo: '이미래',
    email: 'sales@mirae.example',
    phone: '031-987-6543',
    favorite: false,
  },
  {
    id: 'p-3',
    bizNo: '3456789012',
    name: '청계상사',
    ceo: '박청계',
    email: 'hello@cheonggye.example',
    phone: '02-555-1020',
    favorite: false,
  },
  {
    id: 'p-4',
    bizNo: '4567890123',
    name: '바다물류',
    ceo: '최바다',
    email: 'ops@bada.example',
    phone: '051-222-7788',
    favorite: true,
  },
  {
    id: 'p-5',
    bizNo: '5678901234',
    name: '산골농협',
    ceo: '정산골',
    email: 'acct@sangol.example',
    phone: '054-333-4411',
    favorite: false,
  },
]
