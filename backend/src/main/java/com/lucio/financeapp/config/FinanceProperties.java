package com.lucio.financeapp.config;

import com.lucio.financeapp.shared.domain.Currency;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "finance")
public class FinanceProperties {
    private Currency baseCurrency = Currency.EUR;
    private String fxBaseUrl = "https://api.frankfurter.app";

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(Currency baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getFxBaseUrl() {
        return fxBaseUrl;
    }

    public void setFxBaseUrl(String fxBaseUrl) {
        this.fxBaseUrl = fxBaseUrl;
    }
}
