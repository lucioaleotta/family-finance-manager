package com.lucio.financeapp.reporting.application;

import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.transactions.api.TransactionFacade;
import com.lucio.financeapp.transactions.api.TransactionView;
import com.lucio.financeapp.transactions.domain.TransactionKind;
import com.lucio.financeapp.transactions.domain.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComputeMonthlyBalanceUseCaseTest {

        private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-00000000d100");

        @Mock
        private TransactionFacade transactionFacade;

        @InjectMocks
        private ComputeMonthlyBalanceUseCase useCase;

        @Test
        void shouldComputeBalanceUsingOnlyStandardTransactions() {
                YearMonth month = YearMonth.of(2026, 2);

                TransactionView income = new TransactionView(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                Money.of(new BigDecimal("1000.00"), Currency.EUR),
                                LocalDate.of(2026, 2, 1),
                                TransactionType.INCOME,
                                "SALARY",
                                "Salary",
                                TransactionKind.STANDARD,
                                null);

                TransactionView expense = new TransactionView(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                Money.of(new BigDecimal("200.00"), Currency.EUR),
                                LocalDate.of(2026, 2, 2),
                                TransactionType.EXPENSE,
                                "GROCERIES",
                                "Food",
                                TransactionKind.STANDARD,
                                null);

                TransactionView transfer = new TransactionView(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                Money.of(new BigDecimal("500.00"), Currency.EUR),
                                LocalDate.of(2026, 2, 3),
                                TransactionType.EXPENSE,
                                "Transfer",
                                "Transfer",
                                TransactionKind.TRANSFER,
                                UUID.randomUUID());

                when(transactionFacade.findByMonth(USER_ID, month)).thenReturn(List.of(income, expense, transfer));

                var result = useCase.handle(USER_ID, month);

                assertEquals(new BigDecimal("1000.00"), result.totalIncome());
                assertEquals(new BigDecimal("200.00"), result.totalExpense());
                assertEquals(new BigDecimal("800.00"), result.savings());
        }
}
