# 전자세금계산서 발행 시스템 — 프로젝트 기획서

## 1. 프로젝트 개요

| 항목 | 내용 |
|------|------|
| 프로젝트명 | TaxFlow — 전자세금계산서 SaaS 플랫폼 |
| 연계 방식 | 국세청 홈택스 직접 연계 |
| 배포 형태 | 웹 서비스 (Multi-tenant SaaS) |
| 대상 | 다중 고객사 |
| 보안 등급 | 최고 (금융/세무급 보안 요구사항 적용) |

---

## 2. 기술 스택 (보안 최우선 기준)

### Backend
- **Java 21 (LTS) + Spring Boot 3.x**
  - 이유: 국세청 SDK/라이브러리 Java 최우선 지원, JVM 보안 매니저, 사실상 국내 세무/금융 표준
- **Spring Security** — JWT + OAuth2, Row-Level Security
- **Apache CXF** — 국세청 SOAP/HTTPS API 통신

### Frontend
- **React 18 + TypeScript** — SPA, 정적 타입 보장
- **TailwindCSS** — UI
- **State**: Zustand or Jotai

### Database
- **PostgreSQL 16** — Row-Level Security (RLS) 기반 멀티테넌트 격리
- **Redis** — 세션, 캐시, Rate Limiting

### 인프라
- **AWS** (Korea - Seoul Region)
  - ALB + EC2 Auto Scaling 또는 EKS
  - RDS PostgreSQL (Multi-AZ, Encryption at Rest)
  - KMS — 공인인증서 암호화 키 관리
  - WAF + Shield — DDoS/SQL Injection 방어
  - CloudTrail — 모든 API 호출 감사 로그
  - VPC Private Subnet — DB는 외부 접근 불가

### 보안 인프라
- **HashiCorp Vault** — 공인인증서, API 키, DB credential 관리
- **TLS 1.3** 전 구간 암호화
- **Content Security Policy (CSP)** + XSS/CSRF 완벽 방어
- **정기 보안 감사** (코드 스캔 + 침투 테스트)

### CI/CD
- **GitHub Actions** + **SonarQube** (정적 분석) + **Trivy** (취약점 스캔)

---

## 3. 멀티테넌트 아키텍처

### 테넌트 격리 전략: Schema-per-Tenant (PostgreSQL)

```
taxflow_db
├── public          — 시스템 마스터 (사용자 계정, 테넌트 정보, 결제)
├── tenant_001      — 고객사 A 데이터
├── tenant_002      — 고객사 B 데이터
└── tenant_xxx      — ...
```

**이유**:
- Row-Level Security보다 완전한 격리 (SQL 실수로 타 테넌트 노출 불가)
- 테넌트별 백업/복구 개별 가능
- 개별 스키마 마이그레이션 가능
- 국세청 감사 시 테넌트별 데이터 추출 용이

### 테넌트 식별
- JWT 토큰에 tenant_id 포함
- Request 시작 시 `SET search_path TO tenant_xxx, public`
- 전역 필터가 아닌 물리적 분리 → zero-trust

---

## 4. 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                        Client                            │
│                    React + TypeScript                     │
└────────────────────────┬────────────────────────────────┘
                         │ HTTPS (TLS 1.3)
                    ┌────▼────┐
                    │   WAF   │
                    └────┬────┘
                    ┌────▼────────────────────────────┐
                    │   API Gateway (Spring Cloud)     │
                    │   - Rate Limiting                │
                    │   - Auth (JWT)                   │
                    │   - Request Logging              │
                    └────┬──────────────┬──────────────┘
                         │              │
              ┌──────────▼──┐   ┌──────▼──────────┐
              │  Tax Service │   │  Tenant Service  │
              │  - 세금계산서  │   │  - 회원/사업자    │
              │  - XML 생성   │   │  - 거래처 마스터   │
              │  - 전자서명    │   │  - 설정 관리       │
              │  - 국세청 전송  │   │  - 결제/구독      │
              └──────┬───────┘   └──────┬──────────┘
                     │                   │
              ┌──────▼───────┐   ┌──────▼──────────┐
              │   Vault      │   │   PostgreSQL     │
              │  - 공인인증서  │   │  (Schema-per-   │
              │  - 암호화 키   │   │   Tenant)       │
              └──────────────┘   │  + Redis Cache   │
                                 └──────┬──────────┘
                                        │
                                 ┌──────▼──────────┐
                                 │   국세청 홈택스    │
                                 │  (HTTPS SOAP)    │
                                 └─────────────────┘
```

---

## 5. 핵심 기능 목록

### 5.1 세금계산서 관리
| 기능 | 설명 |
|------|------|
| **일반 세금계산서 발행** | 공급자/공급받는자, 품목, 금액, 세액 자동 계산 |
| **수정 세금계산서** | 원본 대비 수정 내역 명시, 국세청 수정 통보 |
| **위수탁 세금계산서** | 위탁자/수탁자 관계 처리 |
| **영수/청구서 전환** | 세금계산서 ↔ 계산서/영수증 상호 전환 |
| **승인번호 관리** | 국세청 승인번호 자동 수신/매핑 |
| **일련번호 자동 발급** | 연도별/사업장별 자동 채번 |
| **삭제/폐기** | 미승인 건 삭제, 승인건 폐기 처리 (국세청 통보) |

### 5.2 거래처 관리
| 기능 | 설명|
|------|------|
| 거래처 마스터 | 사업자등록번호, 상호, 주소, 업태/종목 관리|
| 사업자등록번호 검증 | 국세청 사업자등록번호 진위확인 API 연동 |
| 즉시발급대상자 확인 | 매출/매입 즉시발급 의무 대상 여부 판단 |
| 거래처 그룹 | 빈도별 분류, 즐겨찾기 |

### 5.3 국세청 연계
| 기능 | 설명 |
|------|------|
| **홈택스 인증** | 공인인증서 기반 로그인 및 세션 관리 |
| **세금계산서 제출** | XML 생성 → 전자서명 → HTTPS 전송 |
| **승인 결과 수신** | 실시간/배치 승인결과 조회 |
| **매출/매입 조회** | 국세청 발행내역 동기화 |
| **전송내역 조회** | 발행/수신/승인/거부 상태 추적 |
| **부가가치세 신고 연동** | 매출/매입 집계 → 신고서 데이터 생성 |

### 5.4 공인인증서 관리
| 기능 | 설명 |
|------|------|
| **인증서 등록** | PKCS#12 (.p12/.pfx) 업로드 + 비밀번호 등록 |
| **Vault 암호화 저장** | 인증서 비밀번호 Vault에 암호화 보관 |
| **갱신 알림** | 만료일 30/15/7일 전 알림 |
| **자동 갱신** | 갱신 프로세스 안내 (국민신문고 등) |
| **접근 제한** | 테넌트 관리자만 접근 가능 |

### 5.5 출력/공유
| 기능 | 설명 |
|------|------|
| **PDF 생성** | 국세청 표준 양식 + 회사 로고 워터마크 |
| **이메일 발송** | 공급받는자에게 세금계산서 이메일 전송 |
| **인쇄** | A4 표준 양식 인쇄 |
| **엑셀 내보내기** | 월별/기간별 발행 내역 엑셀 다운로드 |
| **API 제공** | 타 ERP/회계 시스템 연동용 REST API |

### 5.6 멀티테넌트 관리
| 기능 | 설명 |
|------|------|
| **테넌트 가입** | 자가 가입 + 관리자 승인 |
| **사업장 관리** | 1테넌트 N사업장 (사업장별 권한 분리) |
| **사용자 관리** | 관리자/담당자/열람 권한 (RBAC) |
| **감사 로그** | 모든 CRUD + 로그인 + API 호출 기록 |
| **사용량 추적** | 발행 건수, API 호출 횟수 |
| **요금제/결제** | Free / Standard / Enterprise 플랜 |

### 5.7 보안/컴플라이언스
| 기능 | 설명 |
|------|------|
| **2FA 인증** | TOTP 기반 이중인증 |
| **IP 제한** | 테넌트별 허용 IP 설정 |
| **세션 관리** | 자동 로그아웃 (15분), 동시 로그인 제한 |
| **데이터 암호화** | 전송(TLS 1.3) + 저장(AES-256) |
| **개인정보 마스킹** | 주민번호/계좌번호 화면 표시 시 마스킹 |
| **접근 로그** | 모든 인증/인가 실패 기록 + 알림 |

---

## 6. 데이터베이스 스키마 (주요 테이블)

### public 스키마 (시스템)

```sql
-- 테넌트
CREATE TABLE tenants (
    id            BIGSERIAL PRIMARY KEY,
    business_name VARCHAR(200) NOT NULL,
    biz_number    VARCHAR(10) NOT NULL UNIQUE,  -- 사업자등록번호
    schema_name   VARCHAR(100) NOT NULL UNIQUE,
    plan          VARCHAR(20) DEFAULT 'free',
    status        VARCHAR(20) DEFAULT 'active',
    created_at    TIMESTAMPTZ DEFAULT now(),
    expires_at    TIMESTAMPTZ
);

-- 시스템 사용자
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT REFERENCES tenants(id),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- bcrypt
    name          VARCHAR(100) NOT NULL,
    role          VARCHAR(20) NOT NULL,   -- super_admin, tenant_admin, manager, viewer
    mfa_secret    VARCHAR(255),
    mfa_enabled   BOOLEAN DEFAULT false,
    last_login_at TIMESTAMPTZ,
    status        VARCHAR(20) DEFAULT 'active',
    created_at    TIMESTAMPTZ DEFAULT now()
);

-- 공인인증서 메타데이터 (실제 인증서는 Vault에 저장)
CREATE TABLE certificates (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT REFERENCES tenants(id),
    type          VARCHAR(50) NOT NULL,   --signgum, nps, bank
    subject_dn    VARCHAR(500),
    issuer_dn     VARCHAR(500),
    valid_from    TIMESTAMPTZ,
    valid_to      TIMESTAMPTZ,
    vault_ref     VARCHAR(255) NOT NULL,  -- Vault path
    status        VARCHAR(20) DEFAULT 'active',
    created_at    TIMESTAMPTZ DEFAULT now()
);

-- 감사 로그
CREATE TABLE audit_logs (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT,
    user_id       BIGINT,
    action        VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id   BIGINT,
    ip_address    INET,
    user_agent    TEXT,
    detail        JSONB,
    created_at    TIMESTAMPTZ DEFAULT now()
);
```

### tenant_xxx 스키마 (테넌트별)

```sql
-- 사업장
CREATE TABLE workplaces (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    biz_number    VARCHAR(10) NOT NULL,
    biz_type      VARCHAR(100),   -- 업태
    biz_item      VARCHAR(100),   -- 종목
    ceo_name      VARCHAR(100),
    address       VARCHAR(500),
    phone         VARCHAR(20),
    email         VARCHAR(255),
    is_default    BOOLEAN DEFAULT false
);

-- 거래처
CREATE TABLE partners (
    id            BIGSERIAL PRIMARY KEY,
    biz_number    VARCHAR(10) NOT NULL,
    name          VARCHAR(200) NOT NULL,
    ceo_name      VARCHAR(100),
    biz_type      VARCHAR(100),
    biz_item      VARCHAR(100),
    address       VARCHAR(500),
    email         VARCHAR(255),
    phone         VARCHAR(20),
    group_name    VARCHAR(100),
    is_favorite   BOOLEAN DEFAULT false
);

-- 세금계산서
CREATE TABLE tax_invoices (
    id                BIGSERIAL PRIMARY KEY,
    workplace_id      BIGINT REFERENCES workplaces(id),
    partner_id        BIGINT REFERENCES partners(id),
    issue_date        DATE NOT NULL,
    serial_number     VARCHAR(20) NOT NULL,   -- 연도-사업장-001
    total_amount      BIGINT NOT NULL,         -- 공급가액 합계
    total_tax         BIGINT NOT NULL,         -- 부가세 합계
    total_grand       BIGINT NOT NULL,         -- 총액
    status            VARCHAR(20) NOT NULL,    -- draft, submitted, approved, rejected, cancelled
    approval_number   VARCHAR(24),             -- 국세청 승인번호
    issue_type        VARCHAR(20) DEFAULT 'normal',  -- normal, amendment, credit_note
    original_arn      VARCHAR(24),             -- 수정 원본 승인번호
    xml_content       TEXT,                    -- 생성된 XML
    pdf_path          VARCHAR(500),
    submitted_at      TIMESTAMPTZ,
    approved_at       TIMESTAMPTZ,
    created_by        BIGINT,
    created_at        TIMESTAMPTZ DEFAULT now(),
    updated_at        TIMESTAMPTZ DEFAULT now()
);

-- 세금계산서 품목
CREATE TABLE invoice_items (
    id                BIGSERIAL PRIMARY KEY,
    invoice_id        BIGINT REFERENCES tax_invoices(id) ON DELETE CASCADE,
    item_name         VARCHAR(200) NOT NULL,
    spec              VARCHAR(200),           -- 규격
    quantity          DECIMAL(12,2) NOT NULL,
    unit_price        DECIMAL(15,0) NOT NULL,
    amount            BIGINT NOT NULL,         -- 금액 = 수량 × 단가
    tax_rate          DECIMAL(5,2) DEFAULT 10.00,
    tax               BIGINT NOT NULL,         -- 세액
    remark            VARCHAR(500)
);

-- 국세청 전송 이력
CREATE TABLE submission_logs (
    id                BIGSERIAL PRIMARY KEY,
    invoice_id        BIGINT REFERENCES tax_invoices(id),
    direction         VARCHAR(10) NOT NULL,   -- issue(매출), receive(매입)
    request_xml       TEXT,
    response_code     VARCHAR(10),
    response_message  TEXT,
    approval_number   VARCHAR(24),
    submitted_at      TIMESTAMPTZ NOT NULL,
    result_received_at TIMESTAMPTZ
);
```

---

## 7. 국세청 연계 기술 상세

### 7.1 전체 처리 흐름

```
1. 사용자가 세금계산서 입력 (웹 UI)
       │
2. 데이터 검증
   - 사업자등록번호 유효성
   - 금액/세액 계산 검증
   - 필수 항목 확인
       │
3. 국세청 표준 XML 생성
   - XML Schema (xsd) 준수
   - UTF-8 인코딩
       │
4. 전자서명 (XMLDSig)
   - 공인인증서 로드 (Vault → 복호화)
   - X.509 서명 생성 (RSA + SHA-256)
   - XMLDSig 형식으로 서명 삽입
       │
5. 국세청 HTTPS API 전송
   - URL: https://www.hometax.go.kr/...
   - SOAP/XML over HTTPS
   - Content-Type: text/xml; charset=UTF-8
       │
6. 응답 처리
   - 성공: 승인번호 수신 → DB 저장
   - 실패: 에러코드/메시지 → 사용자 알림
   - 대기: 배치 확인 스케줄링
       │
7. 완료 알림
   - 사용자 웹 UI 업데이트
   - 이메일 통지 (선택)
```

### 7.2 전자서명 구현 포인트

```java
// 핵심 라이브러리
import java.security.*;
import java.security.cert.X509Certificate;
import javax.xml.crypto.dsig.*;
import org.apache.santuario.xmlsec.*;

// 서명 프로세스
1. Vault에서 공인인증서(p12) + 비밀번호 복호화
2. KeyStore 로드 → PrivateKey + CertificateChain 추출
3. XML Canonicalization (C14N)
4. Digest 생성 (SHA-256)
5. Signature 생성 (RSA-SHA256)
6. XMLDSig 서명 요소 생성 및 삽입
7. Base64 인코딩
```

### 7.3 주의사항
- **국세청 API는 공식 문서가 제한적** — 실제 연동 시 Hometax 웹 프락시 분석 필요
- **공인인증서는 PC에 설치된 것 사용** → 서버 환경에서는 p12 파일 등록 필요
- **세션 타임아웃** — 국세청 세션은 짧음, 자동 갱신 로직 필요
- **전송 제한** — 대량 전송 시 간격 제한 (Rate Limit) 준수
- **테스트 환경** — 국세청에서 제공하는 테스트 서버 활용 필요

---

## 8. 구현 단계 (총 6단계)

### Phase 1: 기반 인프라 (2~3주)
- [ ] 프로젝트 초기 설정 (Spring Boot + React)
- [ ] PostgreSQL + Schema-per-Tenant 구조
- [ ] Vault 연동 + 공인인증서 관리
- [ ] 인증/인가 (JWT + 2FA)
- [ ] 감사 로그 시스템
- [ ] CI/CD 파이프라인

### Phase 2: 핵심 세금계산서 기능 (3~4주)
- [ ] 세금계산서 CRUD (발행/수정/삭제)
- [ ] 품목 관리 + 자동 계산
- [ ] 거래처 마스터 관리
- [ ] 사업장 관리
- [ ] 일련번호 자동 발급

### Phase 3: 국세청 연계 (4~5주) ★가장 복잡
- [ ] 국세청 XML 스키마 구현
- [ ] 전자서명 (XMLDSig) 구현
- [ ] 홈택스 API 통신 모듈
- [ ] 제출/승인/조회 API
- [ ] 오류 처리 + 재시도 로직
- [ ] 테스트 서버 검증

### Phase 4: 출력 & 연동 (2~3주)
- [ ] PDF 생성 (표준 양식)
- [ ] 이메일 발송
- [ ] 엑셀 내보내기
- [ ] 외부 API (ERP 연동용 REST API)

### Phase 5: 멀티테넌트 관리 (2주)
- [ ] 테넌트 가입/관리
- [ ] RBAC 권한 시스템
- [ ] 사용량 추적
- [ ] 요금제/결제 연동
- [ ] 관리자 대시보드

### Phase 6: 보안 강화 & 검증 (2~3주)
- [ ] 보안 감사 (취약점 스캔 + 침투 테스트)
- [ ] WAF/IPS 설정 최적화
- [ ] 부하 테스트 (JMeter/Gatling)
- [ ] 국세청 연계 최종 검증 (실환경)
- [ ] 사용자 매뉴얼 + 운영 가이드

**총 예상 기간: 15~20주 (약 4~5개월)**

---

## 9. 예상 위험 및 대응

| 위험 | 영향 | 대응 |
|------|------|------|
| 국세청 API 변경/비공식 | 연동 실패 | 대행사 API를 fallback으로 확보 |
| 공인인증서 관리 복잡도 | 사용자 이탈 | 가이드 + 자동 갱신 알림 |
| 대량 전송 시 성능 | 타임아웃/에러 | 비동기 큐 + 배치 처리 |
| 데이터 유출 | 법적/재정적 손실 | 암호화 + 감사 + 접근제한 |
| 멀티테넌트 데이터 혼선 | 심각한 보안사고 | Schema-per-Tenant + QA |
| 규제 변경 | 시스템 수정 필요 | 모듈화 + 유연한 설정 |

---

## 10. 향후 확장

- **전자명세서** 연동 (국세청)
- **간이전자세금계산서** 지원
- **AI 자동 분류** — 품목코드 자동 추천
- **모바일 앱** — 출장 발행
- **외환 세금계산서** — 외화 거래 지원
- ** Slack/Teams 알림** — 발행/승인 알림
