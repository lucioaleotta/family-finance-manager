package com.lucio.financeapp.transactions.domain;

public enum AccountType {
    CHECKING,
    LIQUIDITY,
    SAVINGS,
    CASH,
    CARD,
    INVESTMENT;

    public boolean isSupportedForManualAccount() {
        return this == CHECKING || this == LIQUIDITY || this == INVESTMENT;
    }
}
