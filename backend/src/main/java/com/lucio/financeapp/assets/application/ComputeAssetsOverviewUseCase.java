package com.lucio.financeapp.assets.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lucio.financeapp.assets.api.AssetsAnnualView;
import com.lucio.financeapp.assets.api.AssetsMonthlyView;
import com.lucio.financeapp.assets.api.AssetsOverviewView;
import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.config.FinanceProperties;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.shared.infrastructure.fx.FxRateService;
import com.lucio.financeapp.transactions.api.AccountBalanceView;
import com.lucio.financeapp.transactions.api.AccountView;
import com.lucio.financeapp.transactions.application.ComputeAccountBalanceUseCase;
import com.lucio.financeapp.transactions.application.ListAccountsUseCase;

@Service
@Transactional(readOnly = true)
public class ComputeAssetsOverviewUseCase {

    private final ListAccountsUseCase listAccounts;
    private final ComputeAccountBalanceUseCase accountBalance;
    private final InvestmentSnapshotRepository snapshots;
    private final FinanceProperties financeProperties;
    private final FxRateService fxRateService;

    public ComputeAssetsOverviewUseCase(ListAccountsUseCase listAccounts,
            ComputeAccountBalanceUseCase accountBalance,
            InvestmentSnapshotRepository snapshots,
            FinanceProperties financeProperties,
            FxRateService fxRateService) {
        this.listAccounts = listAccounts;
        this.accountBalance = accountBalance;
        this.snapshots = snapshots;
        this.financeProperties = financeProperties;
        this.fxRateService = fxRateService;
    }

    public AssetsOverviewView handle(int year) {
        Currency baseCurrency = financeProperties.getBaseCurrency();
        YearMonth start = YearMonth.of(year, 1);
        YearMonth end = YearMonth.of(year, 12);

        List<AccountView> accounts = listAccounts.handle();
        List<InvestmentSnapshot> snapshotList = snapshots.findByMonthBetween(start, end);

        Map<Currency, List<InvestmentSnapshot>> snapshotsByCurrency = new HashMap<>();
        for (InvestmentSnapshot snapshot : snapshotList) {
            Currency currency = snapshot.getTotalInvested().getCurrency();
            snapshotsByCurrency.computeIfAbsent(currency, key -> new ArrayList<>()).add(snapshot);
        }

        snapshotsByCurrency.values().forEach(list -> list.sort(Comparator.comparing(InvestmentSnapshot::getMonth)));

        Set<Currency> currencies = new HashSet<>();
        accounts.forEach(a -> currencies.add(a.currency()));
        snapshotList.forEach(s -> currencies.add(s.getTotalInvested().getCurrency()));

        Map<Currency, BigDecimal> fxRatesToBase = new HashMap<>();
        for (Currency currency : currencies) {
            if (currency != baseCurrency) {
                fxRatesToBase.put(currency, fxRateService.getRate(currency, baseCurrency));
            }
        }

        List<AssetsMonthlyView> monthly = new ArrayList<>(12);
        YearMonth current = start;
        Map<Currency, BigDecimal> latestSnapshotByCurrency = new HashMap<>();
        Map<Currency, Integer> snapshotCursorByCurrency = new HashMap<>();
        for (int i = 0; i < 12; i++) {
            BigDecimal liquidity = BigDecimal.ZERO;
            for (AccountView account : accounts) {
                AccountBalanceView balanceView = accountBalance.handle(account.id(), current.atEndOfMonth());
                liquidity = liquidity.add(convert(balanceView.balance(), balanceView.currency(), baseCurrency,
                        fxRatesToBase));
            }

            BigDecimal investments = BigDecimal.ZERO;
            for (Map.Entry<Currency, List<InvestmentSnapshot>> entry : snapshotsByCurrency.entrySet()) {
                Currency currency = entry.getKey();
                List<InvestmentSnapshot> currencySnapshots = entry.getValue();
                int cursor = snapshotCursorByCurrency.getOrDefault(currency, 0);

                while (cursor < currencySnapshots.size()
                        && !currencySnapshots.get(cursor).getMonth().isAfter(current)) {
                    latestSnapshotByCurrency.put(currency,
                            currencySnapshots.get(cursor).getTotalInvested().getAmount());
                    cursor++;
                }

                snapshotCursorByCurrency.put(currency, cursor);

                BigDecimal latest = latestSnapshotByCurrency.get(currency);
                if (latest != null) {
                    investments = investments.add(convert(latest, currency, baseCurrency, fxRatesToBase));
                }
            }

            BigDecimal netWorth = liquidity.add(investments);
            monthly.add(new AssetsMonthlyView(current, baseCurrency, liquidity, investments, netWorth));
            current = current.plusMonths(1);
        }

        int selectedMonth = (year == Year.now().getValue()) ? YearMonth.now().getMonthValue() : 12;
        BigDecimal annualLiquidity = monthly.get(selectedMonth - 1).liquidity();
        BigDecimal annualInvestments = monthly.get(selectedMonth - 1).investments();

        BigDecimal annualNetWorth = annualLiquidity.add(annualInvestments);

        AssetsAnnualView annual = new AssetsAnnualView(year, baseCurrency, annualLiquidity, annualInvestments,
                annualNetWorth);

        return new AssetsOverviewView(baseCurrency, annual, monthly);
    }

    private BigDecimal convert(BigDecimal amount, Currency from, Currency base,
            Map<Currency, BigDecimal> fxRatesToBase) {
        if (from == base) {
            return amount;
        }
        BigDecimal rate = fxRatesToBase.getOrDefault(from, BigDecimal.ONE);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
