import type { Workplace } from '../../types/domain'

export const mockWorkplaces: Workplace[] = [
  {
    id: 'w-1',
    name: '본사',
    bizNo: '1088123456',
    default: true,
    address: '서울특별시 강남구 테헤란로 123',
  },
  {
    id: 'w-2',
    name: '부산지점',
    bizNo: '6088123456',
    default: false,
    address: '부산광역시 해운대구 센텀중앙로 45',
  },
]
