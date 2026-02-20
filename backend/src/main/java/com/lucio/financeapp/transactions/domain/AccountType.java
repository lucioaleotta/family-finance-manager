package com.lucio.financeapp.transactions.domain;

public enum AccountType {
    CHECKING,
    SAVINGS,
    CASH,
    CARD,
    INVESTMENT;

    public boolean isSupportedForManualAccount() {
        return this == CHECKING || this == CARD;
    }
}
