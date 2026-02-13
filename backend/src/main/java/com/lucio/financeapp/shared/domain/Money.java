package com.lucio.financeapp.shared.domain;

import com.lucio.financeapp.transactions.domain.Currency;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Embeddable
public class Money {

    @Column(name = "amount_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "amount_currency", nullable = false, length = 3)
    private Currency currency;

    protected Money() {
        // JPA
    }

    private Money(BigDecimal amount, Currency currency) {
        this.amount = normalize(amount);
        this.currency = Objects.requireNonNull(currency);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    private BigDecimal normalize(BigDecimal value) {
        Objects.requireNonNull(value);
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    // ===== Behavior =====

    public Money add(Money other) {
        checkCurrency(other);
        return new Money(this.amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        checkCurrency(other);
        return new Money(this.amount.subtract(other.amount), currency);
    }

    private void checkCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Cannot operate on different currencies: " +
                this.currency + " vs " + other.currency
            );
        }
    }

    // ===== Getters =====

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    // ===== Equality (IMPORTANT) =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0 &&
               currency == money.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }
}
