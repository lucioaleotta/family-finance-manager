package com.lucio.financeapp.assets.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.config.FinanceProperties;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.shared.infrastructure.fx.FxRateService;
import com.lucio.financeapp.transactions.api.AccountBalanceView;
import com.lucio.financeapp.transactions.api.AccountView;
import com.lucio.financeapp.transactions.application.ComputeAccountBalanceUseCase;
import com.lucio.financeapp.transactions.application.ListAccountsUseCase;
import com.lucio.financeapp.transactions.domain.AccountType;

@ExtendWith(MockitoExtension.class)
class ComputeAssetsOverviewUseCaseTest {

    @Mock
    private ListAccountsUseCase listAccounts;

    @Mock
    private ComputeAccountBalanceUseCase accountBalance;

    @Mock
    private InvestmentSnapshotRepository snapshots;

    @Mock
    private FinanceProperties financeProperties;

    @Mock
    private FxRateService fxRateService;

    @InjectMocks
    private ComputeAssetsOverviewUseCase useCase;

    @Test
    void shouldConvertCurrenciesAndAggregateAnnualAndMonthly() {
        UUID eurAccount = UUID.fromString("00000000-0000-0000-0000-000000000010");
        UUID chfAccount = UUID.fromString("00000000-0000-0000-0000-000000000020");

        when(financeProperties.getBaseCurrency()).thenReturn(Currency.EUR);
        when(listAccounts.handle()).thenReturn(List.of(
                new AccountView(eurAccount, "Fineco", AccountType.CHECKING, Currency.EUR),
                new AccountView(chfAccount, "CHF", AccountType.CHECKING, Currency.CHF)));

        when(accountBalance.handle(eq(eurAccount), any(LocalDate.class)))
                .thenReturn(new AccountBalanceView(eurAccount, LocalDate.of(2026, 1, 31), new BigDecimal("100.00"),
                        Currency.EUR));
        when(accountBalance.handle(eq(chfAccount), any(LocalDate.class)))
                .thenReturn(new AccountBalanceView(chfAccount, LocalDate.of(2026, 1, 31), new BigDecimal("200.00"),
                        Currency.CHF));

        when(fxRateService.getRate(Currency.CHF, Currency.EUR)).thenReturn(new BigDecimal("1.10"));

        YearMonth jan = YearMonth.of(2026, 1);
        InvestmentSnapshot eurSnapshot = InvestmentSnapshot.of(jan,
                Money.of(new BigDecimal("1000.00"), Currency.EUR),
                "EUR snapshot");
        InvestmentSnapshot chfSnapshot = InvestmentSnapshot.of(jan,
                Money.of(new BigDecimal("500.00"), Currency.CHF),
                "CHF snapshot");

        when(snapshots.findByMonthBetween(YearMonth.of(2026, 1), YearMonth.of(2026, 12)))
                .thenReturn(List.of(eurSnapshot, chfSnapshot));

        var result = useCase.handle(2026);

        var january = result.monthly().get(0);
        assertEquals(new BigDecimal("320.00"), january.liquidity());
        assertEquals(new BigDecimal("1550.00"), january.investments());
        assertEquals(new BigDecimal("1870.00"), january.netWorth());

        var annual = result.annual();
        assertEquals(new BigDecimal("3840.00"), annual.liquidity());
        assertEquals(new BigDecimal("1550.00"), annual.investments());
        assertEquals(new BigDecimal("5390.00"), annual.netWorth());
    }
}
