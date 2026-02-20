package com.lucio.financeapp.transactions.domain;

import com.lucio.financeapp.shared.domain.Currency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTypeRestrictionTest {

    @Test
    void shouldAllowOnlyCheckingAndCardOnCreate() {
        assertDoesNotThrow(() -> Account.of("Conto", AccountType.CHECKING, Currency.EUR));
        assertDoesNotThrow(() -> Account.of("Carta", AccountType.CARD, Currency.EUR));

        assertThrows(IllegalArgumentException.class,
                () -> Account.of("Risparmio", AccountType.SAVINGS, Currency.EUR));
        assertThrows(IllegalArgumentException.class,
                () -> Account.of("Contanti", AccountType.CASH, Currency.EUR));
        assertThrows(IllegalArgumentException.class,
                () -> Account.of("Investimenti", AccountType.INVESTMENT, Currency.EUR));
    }

    @Test
    void shouldAllowOnlyCheckingAndCardOnUpdate() {
        Account account = Account.of("Conto", AccountType.CHECKING, Currency.EUR);

        assertDoesNotThrow(() -> account.update("Carta", AccountType.CARD, Currency.EUR));
        assertThrows(IllegalArgumentException.class,
                () -> account.update("Risparmio", AccountType.SAVINGS, Currency.EUR));
    }
}
