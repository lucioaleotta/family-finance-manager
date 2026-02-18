package com.lucio.financeapp.config;

import com.lucio.financeapp.shared.domain.Currency;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "finance")
public class FinanceProperties {
    private Currency baseCurrency = Currency.EUR;

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(Currency baseCurrency) {
        this.baseCurrency = baseCurrency;
    }
}
