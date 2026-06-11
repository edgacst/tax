package com.taxflow.nts.submit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "taxflow.nts.submission")
public class NtsSubmissionProperties {

    /**
     * stub: 로컬·개발 (가상 승인번호) / soap: 국세청 SOAP 엔드포인트 전송
     */
    private Mode mode = Mode.stub;

    private String soapEndpoint = "";

    public enum Mode {
        stub, soap
    }
}
