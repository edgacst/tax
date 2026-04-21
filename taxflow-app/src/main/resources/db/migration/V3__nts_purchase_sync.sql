-- 매입 전자세금계산서 수신·동기화 (public 스키마 — 테넌트별 실서비스 시 tenant 스키마로 이전 가능)

INSERT INTO tenants (business_name, biz_number, schema_name, plan, status, contact_email, max_workplaces, max_users)
SELECT 'TaxFlow 데모', '1088123456', 'tenant_demo', 'ENTERPRISE', 'ACTIVE', 'demo@taxflow.kr', 5, 50
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE schema_name = 'tenant_demo');

CREATE TABLE nts_sync_runs (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    sync_type           VARCHAR(50) NOT NULL DEFAULT 'PURCHASE_POLL',
    status              VARCHAR(20) NOT NULL,
    records_fetched     INT NOT NULL DEFAULT 0,
    records_inserted    INT NOT NULL DEFAULT 0,
    error_message       TEXT,
    started_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at        TIMESTAMPTZ
);

CREATE INDEX idx_nts_sync_tenant_started ON nts_sync_runs(tenant_id, started_at DESC);

CREATE TABLE purchase_invoice_receipts (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    nts_approval_number       VARCHAR(30) NOT NULL,
    issue_date                DATE NOT NULL,
    supplier_biz_no           VARCHAR(10) NOT NULL,
    supplier_name             VARCHAR(200) NOT NULL,
    buyer_biz_no              VARCHAR(10) NOT NULL,
    supply_amount             BIGINT NOT NULL,
    tax_amount                BIGINT NOT NULL,
    total_amount              BIGINT NOT NULL,
    direction                 VARCHAR(10) NOT NULL DEFAULT 'receive',
    raw_json                  JSONB,
    synced_at                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    sync_run_id               BIGINT REFERENCES nts_sync_runs(id),
    UNIQUE (tenant_id, nts_approval_number)
);

CREATE INDEX idx_purchase_tenant ON purchase_invoice_receipts(tenant_id);
CREATE INDEX idx_purchase_issue_date ON purchase_invoice_receipts(issue_date DESC);
CREATE INDEX idx_purchase_synced ON purchase_invoice_receipts(tenant_id, synced_at DESC);
