# 2026-04-21 Daily Memory

## TaxFlow 프로젝트 대규모 구현 진행중

### 프로젝트 개요
- 전자세금계산서 SaaS 플랫폼 (국세청 연계)
- Spring Boot 3.4 + Java 21 + PostgreSQL (schema-per-tenant) + React
- 워크스페이스: taxflow/backend

### 완료된 구현 (백엔드 ~94개 Java 파일)

#### 1. Security/Auth 계층 ✅ (subagent taxflow-security)
- JwtTokenProvider, JwtAuthenticationFilter
- SecurityConfig, CorsConfig, PasswordEncoderConfig
- AuthController (login, signup, refresh, logout, me, change-password, mfa/setup, mfa/verify, mfa/disable, verify-email)
- AuthService (full auth flow with MFA TOTP support)
- DTOs: LoginRequest, SignupRequest, JwtResponse, RefreshRequest, ChangePasswordRequest, MfaVerifyRequest, MfaDisableRequest, MfaSetupResponse
- UserRepository, UserService
- RefreshToken entity/repository, EmailVerification entity/repository
- GlobalExceptionHandler (exception 패키지 - 포괄적 버전 채택)
- Custom exceptions: ResourceNotFoundException, DuplicateResourceException, AuthenticationFailedException, ForbiddenException, BadRequestException, MfaRequiredException, ErrorResponse

#### 2. Business Domain 계층 ✅ (subagent taxflow-business)
- TenantRepository, TenantService, TenantController (admin)
- TenantContextHolder, TenantAspect (@TenantAware AOP), TenantSchemaFilter
- InvoiceService (full CRUD + auto-calc + serial generation + cancel/amendment), InvoiceController
- PartnerService (CRUD + search), PartnerController
- WorkplaceService (CRUD + default management), WorkplaceController
- DTOs: Invoice* (8개), Partner* (4개), Workplace* (3개)
- PageResponse<T> (common.dto)

#### 3. NTS 연계 + Certificate + Output ⏱️ (subagent taxflow-nts-integration, timed out but most files created)
- NtsXmlGenerator, NtsDigitalSignature, NtsApiClient, NtsService, NtsController
- JAXB schema: TaxInvoiceXml, PartyInfo, ItemInfo, TotalInfo
- CertificateService, CertificateController, Certificate entity
- PdfService, ExcelService, EmailService, OutputController
- InvoicePdfTemplate, NtsConfig, ExpiryCheckScheduler
- build.gradle 업데이트 (xmlsec, poi, openhtmltopdf 의존성 추가)

### 정리 작업
- common.exception 패키지 삭제 (exception 패키지와 중복 → exception 유지)
- common.dto.ErrorResponse는 exception.ErrorResponse와 충돌 → common.dto.ErrorResponse 삭제 필요 (진행중)

### 아직 미구현
- React 프론트엔드 (아예 시작 안됨)
- 빌드 테스트 (Gradle wrapper JAR 누락으로 컴파일 테스트 불가)
- README/문서

### 사용자 피드백
- 작업 진행 상황을 실시간으로 알기 어렵다는 피드백 (10:47)
