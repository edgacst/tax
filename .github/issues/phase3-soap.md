## 목표
`NTS_SUBMISSION_MODE=soap` 환경에서 실제 국세청 SOAP 엔드포인트로 세금계산서 전송

## 작업
- [ ] SOAP 요청/응답 스키마 정합
- [ ] 승인번호·오류코드 파싱
- [ ] 타임아웃·재시도
- [ ] 국세청 테스트베드 검증

## 관련 코드
- `SoapNtsSubmissionPort`
- `NtsSubmissionProperties`
