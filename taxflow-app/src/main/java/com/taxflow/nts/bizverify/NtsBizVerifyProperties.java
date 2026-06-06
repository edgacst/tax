package com.taxflow.nts.bizverify;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "taxflow.nts.biz-verify")
public class NtsBizVerifyProperties {

    /**
     * 공공데이터포털에서 발급한 서비스키. 환경 변수 NTS_BIZ_VERIFY_SERVICE_KEY 권장.
     */
    private String serviceKey = "";

    private String baseUrl = "https://api.odcloud.kr/api/nts-businessman/v1";

    /**
     * false 이면 API 호출 없이 503 안내. 키 없이 로컬 개발 시 false 유지.
     */
    private boolean enabled = false;

    public boolean isConfigured() {
        return enabled && serviceKey != null && !serviceKey.isBlank();
    }
}
