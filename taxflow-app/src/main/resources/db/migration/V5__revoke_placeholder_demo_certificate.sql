-- Flyway V4 시드의 placeholder 인증서는 실제 PKCS#12 파일이 없어 서명에 사용할 수 없음
UPDATE public.certificates
SET status = 'REVOKED', updated_at = now()
WHERE vault_path = 'vault/demo/cert'
  AND status = 'ACTIVE';
