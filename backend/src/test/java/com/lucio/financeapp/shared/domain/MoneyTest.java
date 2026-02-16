package com.lucio.financeapp.shared.domain;

import com.lucio.financeapp.transactions.domain.Currency;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldNormalizeScaleOnCreation() {
        Money money = Money.of(new BigDecimal("10.555"), Currency.EUR);

        assertEquals(new BigDecimal("10.56"), money.getAmount());
        assertEquals(Currency.EUR, money.getCurrency());
    }

    @Test
    void shouldAddAmountsWithSameCurrency() {
        Money left = Money.of(new BigDecimal("5.10"), Currency.EUR);
        Money right = Money.of(new BigDecimal("2.25"), Currency.EUR);

        Money result = left.add(right);

        assertEquals(new BigDecimal("7.35"), result.getAmount());
        assertEquals(Currency.EUR, result.getCurrency());
    }

    @Test
    void shouldFailWhenCurrenciesDiffer() {
        Money eur = Money.of(new BigDecimal("5.00"), Currency.EUR);
        Money usd = Money.of(new BigDecimal("2.00"), Currency.CHF);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> eur.add(usd));

        assertTrue(exception.getMessage().contains("different currencies"));
    }
}
