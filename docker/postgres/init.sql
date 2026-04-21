-- TaxFlow init DB script (optional: mount as PostgreSQL docker-entrypoint-initdb.d)
-- Requires superuser context for GRANT on database.

GRANT ALL PRIVILEGES ON DATABASE taxflow TO taxflow;

CREATE EXTENSION IF NOT EXISTS pgcrypto;
