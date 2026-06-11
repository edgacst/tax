package com.taxflow.nts.purchase;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "taxflow.nts.purchase")
public class NtsPurchaseProperties {

    private Mode mode = Mode.stub;

    private String soapEndpoint = "";

    private String soapAction = "";

    private String soapNamespace = "http://www.nts.go.kr/standard/ti/v1";

    private String soapListOperation = "ListPurchaseTaxInvoicesRequest";

    private int connectTimeoutMs = 10_000;

    private int readTimeoutMs = 60_000;

    private int maxRetries = 2;

    private long retryDelayMs = 1_000;

    public enum Mode {
        stub, soap
    }
}
