package com.lucio.financeapp.reporting.application;

import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.shared.domain.Money;
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
class ComputeMonthlyAccountsUseCaseTest {

    @Mock
    private TransactionFacade transactionFacade;

    @InjectMocks
    private ComputeMonthlyAccountsUseCase useCase;

    @Test
    void shouldGroupByAccountAndSortByAccountId() {
        YearMonth month = YearMonth.of(2026, 2);
        UUID accountA = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID accountB = UUID.fromString("00000000-0000-0000-0000-000000000002");

        when(transactionFacade.findByMonth(month)).thenReturn(List.of(
                tx(accountB, "20.00", TransactionType.EXPENSE),
                tx(accountA, "100.00", TransactionType.INCOME),
                tx(accountA, "40.00", TransactionType.EXPENSE),
                tx(null, "999.00", TransactionType.INCOME)));

        var result = useCase.handle(month);

        assertEquals(2, result.size());

        var first = result.get(0);
        assertEquals(accountA, first.accountId());
        assertEquals(new BigDecimal("100.00"), first.income());
        assertEquals(new BigDecimal("40.00"), first.expense());
        assertEquals(new BigDecimal("60.00"), first.net());

        var second = result.get(1);
        assertEquals(accountB, second.accountId());
        assertEquals(BigDecimal.ZERO, second.income());
        assertEquals(new BigDecimal("20.00"), second.expense());
        assertEquals(new BigDecimal("-20.00"), second.net());
    }

    private static TransactionView tx(UUID accountId, String amount, TransactionType type) {
        return new TransactionView(
                UUID.randomUUID(),
                accountId,
                Money.of(new BigDecimal(amount), Currency.EUR),
                LocalDate.of(2026, 2, 1),
                type,
                "CAT",
                "desc",
                TransactionKind.STANDARD,
                null);
    }
}
