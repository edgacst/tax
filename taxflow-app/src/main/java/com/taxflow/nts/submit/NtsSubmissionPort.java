package com.taxflow.nts.submit;

/**
 * 국세청 전자세금계산서 전송 포트.
 * <p>운영: SOAP 구현체 / 개발: {@link StubNtsSubmissionPort}</p>
 */
public interface NtsSubmissionPort {

    NtsSubmissionResult submit(String signedXml, String supplierBizNo);
}
