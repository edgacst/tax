-- tenant_demo 스키마 생성 및 데모 마스터·세금계산서 시드

SELECT create_tenant_schema('tenant_demo');

INSERT INTO tenant_demo.workplaces (name, biz_number, address, is_default, status)
SELECT '본사', '1088123456', '서울특별시 강남구 테헤란로 123', true, 'active'
WHERE NOT EXISTS (SELECT 1 FROM tenant_demo.workplaces);

INSERT INTO tenant_demo.workplaces (name, biz_number, address, is_default, status)
SELECT '부산지점', '6088123456', '부산광역시 해운대구 센텀중앙로 45', false, 'active'
WHERE NOT EXISTS (SELECT 1 FROM tenant_demo.workplaces WHERE name = '부산지점');

INSERT INTO tenant_demo.partners (biz_number, name, ceo_name, email, phone, is_favorite, status)
SELECT v.biz_number, v.name, v.ceo_name, v.email, v.phone, v.is_favorite, 'active'
FROM (VALUES
    ('1234567890', '(주)한빛유통', '김한빛', 'ap@hanbit.example', '02-1234-5678', true),
    ('2345678901', '미래IT솔루션', '이미래', 'sales@mirae.example', '031-987-6543', false),
    ('3456789012', '청계상사', '박청계', 'hello@cheonggye.example', '02-555-1020', false),
    ('4567890123', '바다물류', '최바다', 'ops@bada.example', '051-222-7788', true),
    ('5678901234', '산골농협', '정산골', 'acct@sangol.example', '054-333-4411', false)
) AS v(biz_number, name, ceo_name, email, phone, is_favorite)
WHERE NOT EXISTS (SELECT 1 FROM tenant_demo.partners);

INSERT INTO tenant_demo.tax_invoices (
    workplace_id, partner_id, issue_date, serial_number,
    total_amount, total_tax, total_grand, remark, status, direction
)
SELECT w.id, p.id, v.issue_date::date, v.serial_number,
       v.total_amount, v.total_tax, v.total_grand, v.remark, v.status, v.direction
FROM (VALUES
    ('본사', '1234567890', '2026-04-18', '2026-SEOUL-0001', 1000000, 100000, 1100000, '4월 분 자재', 'approved', 'issue'),
    ('본사', '2345678901', '2026-04-19', '2026-SEOUL-0002', 450000, 45000, 495000, NULL, 'submitted', 'issue'),
    ('본사', '3456789012', '2026-04-19', '2026-SEOUL-0003', 88000, 8800, 96800, NULL, 'draft', 'issue'),
    ('부산지점', '4567890123', '2026-04-20', '2026-SEOUL-0004', 3200000, 320000, 3520000, NULL, 'approved', 'issue'),
    ('부산지점', '1234567890', '2026-04-20', '2026-BUSAN-0001', 220000, 22000, 242000, NULL, 'rejected', 'issue'),
    ('본사', '5678901234', '2026-04-17', 'RCV-2026-0042', 50000, 5000, 55000, NULL, 'approved', 'receive')
) AS v(workplace_name, partner_biz, issue_date, serial_number, total_amount, total_tax, total_grand, remark, status, direction)
JOIN tenant_demo.workplaces w ON w.name = v.workplace_name
JOIN tenant_demo.partners p ON p.biz_number = v.partner_biz
WHERE NOT EXISTS (SELECT 1 FROM tenant_demo.tax_invoices);

INSERT INTO certificates (tenant_id, type, subject_dn, valid_from, valid_to, vault_path, status)
SELECT t.id, 'PKCS12', 'CN=TaxFlow Demo,O=TaxFlow', now(), now() + interval '180 days', 'vault/demo/cert', 'ACTIVE'
FROM tenants t
WHERE t.schema_name = 'tenant_demo'
  AND NOT EXISTS (SELECT 1 FROM certificates c WHERE c.tenant_id = t.id);
