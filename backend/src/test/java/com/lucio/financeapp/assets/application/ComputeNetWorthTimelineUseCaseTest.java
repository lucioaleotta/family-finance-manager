package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.transactions.api.AccountBalanceView;
import com.lucio.financeapp.transactions.api.AccountView;
import com.lucio.financeapp.transactions.application.ComputeAccountBalanceUseCase;
import com.lucio.financeapp.transactions.application.ListAccountsUseCase;
import com.lucio.financeapp.transactions.domain.AccountType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComputeNetWorthTimelineUseCaseTest {

    @Mock
    private ListAccountsUseCase listAccounts;

    @Mock
    private ComputeAccountBalanceUseCase accountBalance;

    @Mock
    private InvestmentSnapshotRepository snapshots;

    @InjectMocks
    private ComputeNetWorthTimelineUseCase useCase;

    @Test
    void shouldComputeTimelineByCurrencyIncludingInvestments() {
        UUID accountA = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID accountB = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID chfAccount = UUID.fromString("00000000-0000-0000-0000-000000000003");

        when(listAccounts.handle()).thenReturn(List.of(
                new AccountView(accountA, "Fineco", AccountType.CHECKING, Currency.EUR),
                new AccountView(accountB, "Cash", AccountType.CASH, Currency.EUR),
                new AccountView(chfAccount, "CHF", AccountType.CHECKING, Currency.CHF)
        ));

        when(accountBalance.handle(eq(accountA), any(LocalDate.class)))
                .thenReturn(new AccountBalanceView(accountA, LocalDate.of(2026, 1, 31), new BigDecimal("100.00"), Currency.EUR));
        when(accountBalance.handle(eq(accountB), any(LocalDate.class)))
                .thenReturn(new AccountBalanceView(accountB, LocalDate.of(2026, 1, 31), new BigDecimal("50.00"), Currency.EUR));

        when(snapshots.findByMonthAndCurrency(any(YearMonth.class), eq(Currency.EUR)))
                .thenAnswer(invocation -> {
                    YearMonth ym = invocation.getArgument(0);
                    if (ym.equals(YearMonth.of(2026, 2))) {
                        return Optional.of(InvestmentSnapshot.of(ym, Money.of(new BigDecimal("1000.00"), Currency.EUR), "PAC"));
                    }
                    return Optional.empty();
                });

        var result = useCase.handle(2026, Currency.EUR);

        assertEquals(12, result.size());

        var january = result.get(0);
        assertEquals(YearMonth.of(2026, 1), january.month());
        assertEquals(new BigDecimal("150.00"), january.liquidity());
        assertEquals(BigDecimal.ZERO, january.invested());
        assertEquals(new BigDecimal("150.00"), january.netWorth());

        var february = result.get(1);
        assertEquals(YearMonth.of(2026, 2), february.month());
        assertEquals(new BigDecimal("150.00"), february.liquidity());
        assertEquals(new BigDecimal("1000.00"), february.invested());
        assertEquals(new BigDecimal("1150.00"), february.netWorth());
    }
}
