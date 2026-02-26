package com.lucio.financeapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "finance")
public class FinanceProperties {
    private String baseCurrency = "EUR";
    private String fxBaseUrl = "https://api.frankfurter.app";

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getFxBaseUrl() {
        return fxBaseUrl;
    }

    public void setFxBaseUrl(String fxBaseUrl) {
        this.fxBaseUrl = fxBaseUrl;
    }
}
