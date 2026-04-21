-- ============================================
-- TaxFlow V1: Core schema (public)
-- ============================================

-- Enable pgcrypto for UUID generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ── Tenants ──
CREATE TABLE tenants (
    id              BIGSERIAL PRIMARY KEY,
    business_name   VARCHAR(200) NOT NULL,
    biz_number      VARCHAR(10) NOT NULL UNIQUE,
    schema_name     VARCHAR(100) NOT NULL UNIQUE,
    plan            VARCHAR(20) NOT NULL DEFAULT 'FREE',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    contact_email   VARCHAR(255),
    contact_phone   VARCHAR(20),
    max_workplaces  INT NOT NULL DEFAULT 1,
    max_users       INT NOT NULL DEFAULT 3,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tenants_biz_number ON tenants(biz_number);
CREATE INDEX idx_tenants_status ON tenants(status);

-- ── Users ──
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT REFERENCES tenants(id),
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    role            VARCHAR(30) NOT NULL DEFAULT 'VIEWER',
    mfa_secret      VARCHAR(255),
    mfa_enabled     BOOLEAN NOT NULL DEFAULT false,
    last_login_at   TIMESTAMPTZ,
    last_login_ip   INET,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(tenant_id, email)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_status ON users(status);

-- ── Refresh Tokens ──
CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL,
    device_info     VARCHAR(255),
    ip_address      INET,
    expires_at      TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- ── Certificates (metadata only; actual cert stored in Vault) ──
CREATE TABLE certificates (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    type            VARCHAR(50) NOT NULL,
    subject_dn      VARCHAR(500),
    issuer_dn       VARCHAR(500),
    serial_number   VARCHAR(100),
    valid_from      TIMESTAMPTZ,
    valid_to        TIMESTAMPTZ,
    vault_path      VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    uploaded_by     BIGINT REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_certs_tenant ON certificates(tenant_id);
CREATE INDEX idx_certs_valid_to ON certificates(valid_to);

-- ── Audit Logs ──
CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT,
    user_id         BIGINT,
    action          VARCHAR(50) NOT NULL,
    resource_type   VARCHAR(50),
    resource_id     BIGINT,
    ip_address      INET,
    user_agent      TEXT,
    request_id      VARCHAR(50),
    detail          JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_tenant ON audit_logs(tenant_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

-- ── Tenant Allowed IPs (optional security) ──
CREATE TABLE tenant_allowed_ips (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    ip_range        CIDR NOT NULL,
    description     VARCHAR(200),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_allowed_ips_tenant ON tenant_allowed_ips(tenant_id);

-- ── Email Verification ──
CREATE TABLE email_verifications (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    token           VARCHAR(255) NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ NOT NULL,
    verified        BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_email_verif_token ON email_verifications(token);
