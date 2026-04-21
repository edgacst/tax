-- ============================================
-- TaxFlow V2: Tenant schema template
-- This migration creates a function that initializes
-- a new tenant schema with all required tables.
-- ============================================

CREATE OR REPLACE FUNCTION create_tenant_schema(p_schema_name VARCHAR(100))
RETURNS VOID AS $$
BEGIN
    -- Create schema
    EXECUTE format('CREATE SCHEMA IF NOT EXISTS %I', p_schema_name);

    -- ── Workplaces ──
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I.workplaces (
            id              BIGSERIAL PRIMARY KEY,
            name            VARCHAR(200) NOT NULL,
            biz_number      VARCHAR(10) NOT NULL,
            biz_type        VARCHAR(100),
            biz_item        VARCHAR(100),
            ceo_name        VARCHAR(100),
            address         VARCHAR(500),
            phone           VARCHAR(20),
            email           VARCHAR(255),
            tax_office      VARCHAR(200),
            is_default      BOOLEAN NOT NULL DEFAULT false,
            status          VARCHAR(20) NOT NULL DEFAULT ''active'',
            created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
            updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
        )', p_schema_name);

    -- ── Partners (Customers/Vendors) ──
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I.partners (
            id              BIGSERIAL PRIMARY KEY,
            biz_number      VARCHAR(10) NOT NULL,
            name            VARCHAR(200) NOT NULL,
            ceo_name        VARCHAR(100),
            biz_type        VARCHAR(100),
            biz_item        VARCHAR(100),
            address         VARCHAR(500),
            email           VARCHAR(255),
            phone           VARCHAR(20),
            fax             VARCHAR(20),
            group_name      VARCHAR(100),
            payment_terms   VARCHAR(50),
            bank_name       VARCHAR(100),
            bank_account    VARCHAR(30),
            bank_holder     VARCHAR(100),
            is_favorite     BOOLEAN NOT NULL DEFAULT false,
            is_immediate    BOOLEAN NOT NULL DEFAULT false,
            status          VARCHAR(20) NOT NULL DEFAULT ''active'',
            created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
            updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
        )', p_schema_name);

    -- ── Invoice Serial Numbers ──
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I.invoice_serials (
            id              BIGSERIAL PRIMARY KEY,
            workplace_id    BIGINT NOT NULL,
            year            INT NOT NULL,
            direction       VARCHAR(10) NOT NULL,
            last_number     INT NOT NULL DEFAULT 0,
            prefix          VARCHAR(50) NOT NULL,
            created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
            updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
            UNIQUE(workplace_id, year, direction)
        )', p_schema_name);

    -- ── Tax Invoices ──
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I.tax_invoices (
            id                BIGSERIAL PRIMARY KEY,
            workplace_id      BIGINT NOT NULL REFERENCES %I.workplaces(id),
            partner_id        BIGINT NOT NULL REFERENCES %I.partners(id),
            issue_date        DATE NOT NULL,
            serial_number     VARCHAR(30) NOT NULL,
            total_amount      BIGINT NOT NULL CHECK (total_amount >= 0),
            total_tax         BIGINT NOT NULL CHECK (total_tax >= 0),
            total_grand       BIGINT NOT NULL CHECK (total_grand >= 0),
            remark            VARCHAR(500),
            status            VARCHAR(20) NOT NULL DEFAULT ''draft'',
            approval_number   VARCHAR(24),
            issue_type        VARCHAR(20) NOT NULL DEFAULT ''normal'',
            original_arn      VARCHAR(24),
            xml_content       TEXT,
            signed_xml        TEXT,
            pdf_path          VARCHAR(500),
            direction         VARCHAR(10) NOT NULL DEFAULT ''issue'',
            submitted_at      TIMESTAMPTZ,
            approved_at       TIMESTAMPTZ,
            created_by        BIGINT,
            created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
            updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
        )', p_schema_name, p_schema_name, p_schema_name);

    -- ── Invoice Items ──
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I.invoice_items (
            id                BIGSERIAL PRIMARY KEY,
            invoice_id        BIGINT NOT NULL REFERENCES %I.tax_invoices(id) ON DELETE CASCADE,
            seq               INT NOT NULL,
            item_name         VARCHAR(200) NOT NULL,
            spec              VARCHAR(200),
            quantity          DECIMAL(12,2) NOT NULL DEFAULT 1,
            unit_price        BIGINT NOT NULL CHECK (unit_price >= 0),
            amount            BIGINT NOT NULL CHECK (amount >= 0),
            tax_rate          DECIMAL(5,2) NOT NULL DEFAULT 10.00,
            tax               BIGINT NOT NULL CHECK (tax >= 0),
            remark            VARCHAR(500)
        )', p_schema_name, p_schema_name);

    -- ── Submission Logs ──
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS %I.submission_logs (
            id                BIGSERIAL PRIMARY KEY,
            invoice_id        BIGINT NOT NULL REFERENCES %I.tax_invoices(id),
            direction         VARCHAR(10) NOT NULL,
            request_xml       TEXT,
            response_code     VARCHAR(10),
            response_message  TEXT,
            approval_number   VARCHAR(24),
            submitted_at      TIMESTAMPTZ NOT NULL,
            result_received_at TIMESTAMPTZ
        )', p_schema_name, p_schema_name);

    -- ── Indexes ──
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_invoices_workplace ON %I.tax_invoices(workplace_id)', p_schema_name, p_schema_name);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_invoices_partner ON %I.tax_invoices(partner_id)', p_schema_name, p_schema_name);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_invoices_status ON %I.tax_invoices(status)', p_schema_name, p_schema_name);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_invoices_date ON %I.tax_invoices(issue_date)', p_schema_name, p_schema_name);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_invoices_serial ON %I.tax_invoices(serial_number)', p_schema_name, p_schema_name);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_items_invoice ON %I.invoice_items(invoice_id)', p_schema_name, p_schema_name);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_submission_invoice ON %I.submission_logs(invoice_id)', p_schema_name, p_schema_name);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_partners_biz ON %I.partners(biz_number)', p_schema_name, p_schema_name);
    EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_partners_name ON %I.partners(name)', p_schema_name, p_schema_name);

    -- ── Grant access (will be set via RLS policies or GRANT in production) ──
    RAISE NOTICE 'Tenant schema % created successfully', p_schema_name;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
