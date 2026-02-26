package com.lucio.financeapp.transactions.domain;

import com.lucio.financeapp.shared.domain.Currency;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTypeRestrictionTest {

        private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-00000000aaaa");

        @Test
        void shouldAllowOnlyCheckingLiquidityAndInvestmentOnCreate() {
                assertDoesNotThrow(() -> Account.of(USER_ID, "Conto", AccountType.CHECKING, Currency.EUR));
                assertDoesNotThrow(() -> Account.of(USER_ID, "Liquidita", AccountType.LIQUIDITY, Currency.EUR));
                assertDoesNotThrow(() -> Account.of(USER_ID, "Investimenti", AccountType.INVESTMENT, Currency.EUR));

                assertThrows(IllegalArgumentException.class,
                                () -> Account.of(USER_ID, "Carta", AccountType.CARD, Currency.EUR));
                assertThrows(IllegalArgumentException.class,
                                () -> Account.of(USER_ID, "Risparmio", AccountType.SAVINGS, Currency.EUR));
                assertThrows(IllegalArgumentException.class,
                                () -> Account.of(USER_ID, "Contanti", AccountType.CASH, Currency.EUR));
        }

        @Test
        void shouldAllowOnlyCheckingLiquidityAndInvestmentOnUpdate() {
                Account account = Account.of(USER_ID, "Conto", AccountType.CHECKING, Currency.EUR);

                assertDoesNotThrow(() -> account.update("Liquidita", AccountType.LIQUIDITY, Currency.EUR));
                assertDoesNotThrow(() -> account.update("Investimenti", AccountType.INVESTMENT, Currency.EUR));
                assertThrows(IllegalArgumentException.class,
                                () -> account.update("Carta", AccountType.CARD, Currency.EUR));
                assertThrows(IllegalArgumentException.class,
                                () -> account.update("Risparmio", AccountType.SAVINGS, Currency.EUR));
        }
}
