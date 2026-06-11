# TaxFlow

홈택스 연계 **전자세금계산서 · 부가가치세 신고** SaaS (멀티테넌트)

| 항목 | 내용 |
|------|------|
| 저장소 | https://github.com/edgacst/tax |
| 백엔드 | Java 17, Spring Boot 3.4, PostgreSQL, Flyway |
| 프론트 | React 18, TypeScript, Vite, Tailwind |
| 연계 방향 | 국세청 홈택스 직접 연계 (SOAP/XML) |

상세 기획: [`docs/taxflow-product-spec.md`](docs/taxflow-product-spec.md)

---

## 로드맵 & 진행 상황

| 단계 | 내용 | 상태 |
|------|------|------|
| **Phase 1** | JWT 인증, 테넌트 격리, 공인인증서 Vault, 감사 로그 | ✅ 완료 |
| **Phase 2** | 세금계산서 CRUD, XML 생성, XMLDSig 서명, 국세청 전송(stub) | ✅ 완료 |
| **Phase 3** | 국세청 SOAP 실연동, 매출·매입 동기화, 재전송·오류 처리 | 🚧 진행 예정 |
| **Phase 4** | PDF·이메일·엑셀, ERP 연동 API | 📋 예정 |
| **Phase 5** | 테넌트 가입, RBAC, 사용량·요금제 | 📋 예정 |
| **Phase 6** | CI/CD, 2FA, 보안 검증, 운영 가이드 | 📋 예정 |

이슈·마일스톤: [GitHub Issues](https://github.com/edgacst/tax/issues)

---

## 빠른 시작 (Windows)

### 사전 요구

- **JDK 17+**
- **Node.js 20+**
- **PostgreSQL 16+** (로컬 5432) 또는 Docker Compose

### 1. 환경 파일

```powershell
cd C:\Users\USER\Desktop\tax
copy .env.example .env
```

`.env`에서 필요 시 수정:

- `DB_PORT` — 로컬 Postgres는 `5432`, Docker Compose는 `15432`
- `NTS_BIZ_VERIFY_SERVICE_KEY` — [공공데이터포털](https://www.data.go.kr) 사업자 조회 API 키

### 2. DB 초기화 (최초 1회)

```powershell
.\scripts\init-db.ps1
```

`.env`에 `POSTGRES_SUPERUSER_PASSWORD`를 넣거나, 실행 시 postgres 비밀번호를 입력합니다.

### 3. 개발 서버 실행

```powershell
.\scripts\start-dev.ps1
```

| 서비스 | URL |
|--------|-----|
| 프론트 | http://127.0.0.1:5173 |
| 백엔드 API | http://127.0.0.1:8080 |
| Swagger | http://127.0.0.1:8080/swagger-ui.html |

백엔드만 재시작: `.\scripts\restart-backend.ps1`

### 4. 로그인 (dev 프로필)

| 항목 | 값 |
|------|-----|
| 이메일 | `demo@taxflow.kr` |
| 비밀번호 | `TaxFlow123!` |

---

## 주요 기능 (현재)

- **사업장·거래처** CRUD, 사업자등록번호 진위확인 (공공데이터 API)
- **세금계산서** 임시저장, 일련번호 자동 채번, 품목·세액 자동 계산
- **공인인증서** PKCS#12 업로드, AES-256-GCM 로컬 Vault 저장
- **국세청 발행** XML 생성 → XMLDSig 서명 → 전송 (`stub` 모드: 가상 승인번호)
- **감사 로그** 로그인·인증서·세금계산서 이벤트 기록

### 국세청 발행 흐름

1. 공인인증서 탭에서 `.p12` / `.pfx` 등록
2. 세금계산서 **임시저장** (매출)
3. 상세 화면 **국세청 발행** 클릭
4. 승인번호·`submission_logs` 저장

---

## 프로젝트 구조

```
tax/
├── taxflow-app/          # Spring Boot API
│   └── src/main/java/com/taxflow/
│       ├── auth/         # JWT 로그인·갱신
│       ├── certificate/  # 인증서 Vault
│       ├── invoice/      # 세금계산서
│       └── nts/          # 국세청 연계 (biz-verify, submit, purchase)
├── frontend/             # React SPA
├── scripts/              # PowerShell 개발 스크립트
├── docs/                 # 기획·설계 문서
└── docker/               # Postgres 등
```

---

## 환경 변수

| 변수 | 설명 |
|------|------|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL |
| `JWT_SECRET` | JWT 서명 키 (운영 필수 변경) |
| `TAXFLOW_CERT_MASTER_KEY` | 인증서 Vault 마스터 키 (32-byte Base64) |
| `NTS_BIZ_VERIFY_ENABLED` | 사업자 조회 API 사용 여부 |
| `NTS_BIZ_VERIFY_SERVICE_KEY` | 공공데이터포털 인증키 |
| `NTS_SUBMISSION_MODE` | `stub`(기본) 또는 `soap` |
| `NTS_SUBMISSION_SOAP_ENDPOINT` | 국세청 SOAP 엔드포인트 (soap 모드) |

`.env`와 `data/certificate-vault/`는 Git에 올리지 않습니다.

---

## API 예시

```http
POST /api/v1/auth/login
POST /api/v1/invoices
POST /api/v1/invoices/{id}/submit
GET  /api/v1/audit-logs
```

인증: `Authorization: Bearer <access_token>`

---

## 빌드

```powershell
.\gradlew.bat :taxflow-app:bootRun --args="--spring.profiles.active=dev"

cd frontend
npm install
npm run dev
```

---

## 기여 · 이슈

- 버그·기능 요청: [Issues](https://github.com/edgacst/tax/issues/new/choose)
- Phase별 작업은 마일스톤으로 분류되어 있습니다.

---

## 라이선스

미정 — 상용 SaaS 프로젝트
