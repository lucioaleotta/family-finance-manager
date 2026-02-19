package com.lucio.financeapp.shared.infrastructure.fx;

import java.math.BigDecimal;

import com.lucio.financeapp.shared.domain.Currency;

public interface FxRateService {
    BigDecimal getRate(Currency from, Currency to);
}
