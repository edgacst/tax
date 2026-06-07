-- pgAdmin: postgres DB 에 연결한 뒤 Query Tool 에서 전체 실행 (F5)
-- psql:  & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h 127.0.0.1 -f 이파일경로

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'taxflow') THEN
    CREATE ROLE taxflow WITH LOGIN PASSWORD 'taxflow_dev_password';
  ELSE
    ALTER ROLE taxflow WITH PASSWORD 'taxflow_dev_password';
  END IF;
END
$$;

CREATE DATABASE taxflow OWNER taxflow;
