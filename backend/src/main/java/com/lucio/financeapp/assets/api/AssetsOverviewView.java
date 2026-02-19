package com.lucio.financeapp.assets.api;

import java.util.List;

import com.lucio.financeapp.shared.domain.Currency;

public record AssetsOverviewView(
        Currency currency,
        AssetsAnnualView annual,
        List<AssetsMonthlyView> monthly) {
}
