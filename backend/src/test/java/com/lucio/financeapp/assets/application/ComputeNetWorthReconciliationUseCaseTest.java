package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.api.NetWorthMonthlyView;
import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.LiquiditySnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.assets.domain.ports.LiquiditySnapshotRepository;
import com.lucio.financeapp.config.FinanceProperties;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.transactions.api.AccountView;
import com.lucio.financeapp.transactions.api.TransactionFacade;
import com.lucio.financeapp.transactions.api.TransactionView;
import com.lucio.financeapp.transactions.application.ListAccountsUseCase;
import com.lucio.financeapp.transactions.domain.AccountType;
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
class ComputeNetWorthReconciliationUseCaseTest {

        private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-00000000e400");

        @Mock
        private ComputeNetWorthTimelineUseCase netWorthTimeline;

        @Mock
        private TransactionFacade transactionFacade;

        @Mock
        private ListAccountsUseCase listAccounts;

        @Mock
        private LiquiditySnapshotRepository liquiditySnapshots;

        @Mock
        private InvestmentSnapshotRepository snapshots;

        @Mock
        private FinanceProperties financeProperties;

        @InjectMocks
        private ComputeNetWorthReconciliationUseCase useCase;

        @Test
        void shouldComputeReconciliationUsingPreviousMonthBaseline() {
                YearMonth jan = YearMonth.of(2026, 1);
                YearMonth feb = YearMonth.of(2026, 2);

                when(financeProperties.getBaseCurrency()).thenReturn("EUR");

                when(netWorthTimeline.handle(USER_ID, 2026)).thenReturn(List.of(
                                new NetWorthMonthlyView(jan, Currency.EUR, new BigDecimal("1100.00"),
                                                new BigDecimal("100.00"),
                                                new BigDecimal("1200.00")),
                                new NetWorthMonthlyView(feb, Currency.EUR, new BigDecimal("1200.00"),
                                                new BigDecimal("150.00"),
                                                new BigDecimal("1350.00"))));

                UUID liquidityAccountId = UUID.randomUUID();
                UUID investmentAccountId = UUID.randomUUID();
                when(listAccounts.handle(USER_ID)).thenReturn(List.of(
                                new AccountView(liquidityAccountId, "Liquidita", AccountType.LIQUIDITY, Currency.EUR),
                                new AccountView(investmentAccountId, "Investimenti", AccountType.INVESTMENT,
                                                Currency.EUR)));

                when(liquiditySnapshots.findByMonthBetween(YearMonth.of(2025, 12), YearMonth.of(2025, 12)))
                                .thenReturn(List.of(LiquiditySnapshot.of(YearMonth.of(2025, 12), liquidityAccountId,
                                                Money.of(new BigDecimal("900.00"), Currency.EUR), "baseline")));

                when(snapshots.findByMonthAndAccountIds(YearMonth.of(2025, 12), List.of(investmentAccountId)))
                                .thenReturn(List.of(InvestmentSnapshot.of(YearMonth.of(2025, 12),
                                                investmentAccountId,
                                                Money.of(new BigDecimal("100.00"), Currency.EUR),
                                                "baseline")));

                when(transactionFacade.findByMonth(USER_ID, jan)).thenReturn(List.of(
                                tx(new BigDecimal("300.00"), TransactionType.INCOME, TransactionKind.STANDARD),
                                tx(new BigDecimal("50.00"), TransactionType.EXPENSE, TransactionKind.STANDARD),
                                tx(new BigDecimal("999.00"), TransactionType.INCOME, TransactionKind.TRANSFER)));

                when(transactionFacade.findByMonth(USER_ID, feb)).thenReturn(List.of(
                                tx(new BigDecimal("100.00"), TransactionType.INCOME, TransactionKind.STANDARD),
                                tx(new BigDecimal("50.00"), TransactionType.EXPENSE, TransactionKind.STANDARD)));

                var result = useCase.handle(USER_ID, 2026);

                assertEquals(2, result.size());

                var janResult = result.get(0);
                assertEquals(new BigDecimal("250.00"), janResult.cashflow());
                assertEquals(new BigDecimal("1200.00"), janResult.netWorth());
                assertEquals(new BigDecimal("200.00"), janResult.netWorthDelta());
                assertEquals(new BigDecimal("-50.00"), janResult.unexplained());

                var febResult = result.get(1);
                assertEquals(new BigDecimal("50.00"), febResult.cashflow());
                assertEquals(new BigDecimal("1350.00"), febResult.netWorth());
                assertEquals(new BigDecimal("150.00"), febResult.netWorthDelta());
                assertEquals(new BigDecimal("100.00"), febResult.unexplained());
        }

        private static TransactionView tx(BigDecimal amount, TransactionType type, TransactionKind kind) {
                return new TransactionView(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                Money.of(amount, Currency.EUR),
                                LocalDate.of(2026, 1, 1),
                                type,
                                "CAT",
                                "desc",
                                kind,
                                kind == TransactionKind.TRANSFER ? UUID.randomUUID() : null);
        }
}
