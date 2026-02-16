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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComputeAnnualAccountsTimelineUseCaseTest {

    @Mock
    private TransactionFacade transactionFacade;

    @InjectMocks
    private ComputeAnnualAccountsTimelineUseCase useCase;

    @Test
    void shouldReturn12MonthsForEachAccountWithZeroFill() {
        UUID accountA = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID accountB = UUID.fromString("00000000-0000-0000-0000-000000000002");

        when(transactionFacade.findByMonth(any(YearMonth.class))).thenReturn(List.of());
        when(transactionFacade.findByMonth(YearMonth.of(2026, 1))).thenReturn(List.of(
                tx(accountA, "100.00", TransactionType.INCOME)));
        when(transactionFacade.findByMonth(YearMonth.of(2026, 2))).thenReturn(List.of(
                tx(accountB, "40.00", TransactionType.EXPENSE)));

        var result = useCase.handle(2026);

        assertEquals(24, result.size());

        var janA = result.stream()
                .filter(r -> r.month().equals(YearMonth.of(2026, 1)) && r.accountId().equals(accountA))
                .findFirst()
                .orElseThrow();

        assertEquals(new BigDecimal("100.00"), janA.income());
        assertEquals(BigDecimal.ZERO, janA.expense());
        assertEquals(new BigDecimal("100.00"), janA.net());

        var janB = result.stream()
                .filter(r -> r.month().equals(YearMonth.of(2026, 1)) && r.accountId().equals(accountB))
                .findFirst()
                .orElseThrow();

        assertEquals(BigDecimal.ZERO, janB.income());
        assertEquals(BigDecimal.ZERO, janB.expense());
        assertEquals(BigDecimal.ZERO, janB.net());

        var febB = result.stream()
                .filter(r -> r.month().equals(YearMonth.of(2026, 2)) && r.accountId().equals(accountB))
                .findFirst()
                .orElseThrow();

        assertEquals(BigDecimal.ZERO, febB.income());
        assertEquals(new BigDecimal("40.00"), febB.expense());
        assertEquals(new BigDecimal("-40.00"), febB.net());
    }

    private static TransactionView tx(UUID accountId, String amount, TransactionType type) {
        return new TransactionView(
                UUID.randomUUID(),
                accountId,
                Money.of(new BigDecimal(amount), Currency.EUR),
                LocalDate.of(2026, 1, 1),
                type,
                "CAT",
                "desc",
                TransactionKind.STANDARD,
                null);
    }
}
