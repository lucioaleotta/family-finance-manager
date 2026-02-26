package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.LiquiditySnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.assets.domain.ports.LiquiditySnapshotRepository;
import com.lucio.financeapp.config.FinanceProperties;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.transactions.api.AccountView;
import com.lucio.financeapp.transactions.application.ListAccountsUseCase;
import com.lucio.financeapp.transactions.domain.AccountType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComputeNetWorthTimelineUseCaseTest {

        private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-00000000e300");

        @Mock
        private ListAccountsUseCase listAccounts;

        @Mock
        private LiquiditySnapshotRepository liquiditySnapshots;

        @Mock
        private InvestmentSnapshotRepository snapshots;

        @Mock
        private FinanceProperties financeProperties;

        @InjectMocks
        private ComputeNetWorthTimelineUseCase useCase;

        @Test
        void shouldComputeTimelineByCurrencyIncludingInvestments() {
                UUID liquidityAccount = UUID.fromString("00000000-0000-0000-0000-000000000001");
                UUID investmentAccount = UUID.fromString("00000000-0000-0000-0000-000000000002");
                UUID chfAccount = UUID.fromString("00000000-0000-0000-0000-000000000003");

                when(financeProperties.getBaseCurrency()).thenReturn("EUR");

                when(listAccounts.handle(USER_ID)).thenReturn(List.of(
                                new AccountView(liquidityAccount, "Fineco", AccountType.LIQUIDITY, Currency.EUR),
                                new AccountView(investmentAccount, "Broker", AccountType.INVESTMENT, Currency.EUR),
                                new AccountView(chfAccount, "CHF", AccountType.CHECKING, Currency.CHF)));

                when(liquiditySnapshots.findByMonthBetween(any(YearMonth.class), any(YearMonth.class)))
                                .thenAnswer(invocation -> {
                                        YearMonth ym = invocation.getArgument(0);
                                        if (ym.equals(YearMonth.of(2026, 1)) || ym.equals(YearMonth.of(2026, 2))) {
                                                return List.of(LiquiditySnapshot.of(ym, liquidityAccount,
                                                                Money.of(new BigDecimal("150.00"), Currency.EUR),
                                                                "liq"));
                                        }
                                        return List.of();
                                });

                when(snapshots.findByMonthAndAccountIds(any(YearMonth.class), anyList()))
                                .thenAnswer(invocation -> {
                                        YearMonth ym = invocation.getArgument(0);
                                        if (ym.equals(YearMonth.of(2026, 2))) {
                                                return List.of(
                                                                InvestmentSnapshot.of(ym, investmentAccount,
                                                                                Money.of(new BigDecimal("1000.00"),
                                                                                                Currency.EUR),
                                                                                "PAC"));
                                        }
                                        return List.of();
                                });

                var result = useCase.handle(USER_ID, 2026);

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
