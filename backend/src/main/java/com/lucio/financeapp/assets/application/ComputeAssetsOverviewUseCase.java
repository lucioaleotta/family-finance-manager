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
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lucio.financeapp.assets.api.AssetsAnnualView;
import com.lucio.financeapp.assets.api.AssetsMonthlyView;
import com.lucio.financeapp.assets.api.AssetsOverviewView;
import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.LiquiditySnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.assets.domain.ports.LiquiditySnapshotRepository;
import com.lucio.financeapp.config.FinanceProperties;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.shared.infrastructure.fx.FxRateService;
import com.lucio.financeapp.transactions.api.AccountView;
import com.lucio.financeapp.transactions.application.ListAccountsUseCase;
import com.lucio.financeapp.transactions.domain.AccountType;

@Service
@Transactional(readOnly = true)
public class ComputeAssetsOverviewUseCase {

    private final ListAccountsUseCase listAccounts;
    private final InvestmentSnapshotRepository snapshots;
    private final LiquiditySnapshotRepository liquiditySnapshots;
    private final FinanceProperties financeProperties;
    private final FxRateService fxRateService;

    public ComputeAssetsOverviewUseCase(ListAccountsUseCase listAccounts,
            InvestmentSnapshotRepository snapshots,
            LiquiditySnapshotRepository liquiditySnapshots,
            FinanceProperties financeProperties,
            FxRateService fxRateService) {
        this.listAccounts = listAccounts;
        this.snapshots = snapshots;
        this.liquiditySnapshots = liquiditySnapshots;
        this.financeProperties = financeProperties;
        this.fxRateService = fxRateService;
    }

    public AssetsOverviewView handle(UUID userId, int year) {
        Currency baseCurrency = Currency.valueOf(financeProperties.getBaseCurrency());
        YearMonth start = YearMonth.of(year, 1);
        YearMonth end = YearMonth.of(year, 12);

        List<AccountView> accounts = listAccounts.handle(userId);
        List<AccountView> liquidityAccounts = accounts.stream()
                .filter(a -> a.type() == AccountType.LIQUIDITY)
                .toList();
        List<AccountView> investmentAccounts = accounts.stream()
                .filter(a -> a.type() == AccountType.INVESTMENT)
                .toList();

        List<InvestmentSnapshot> investmentSnapshotList = snapshots.findByMonthBetween(start, end);
        List<LiquiditySnapshot> liquiditySnapshotList = liquiditySnapshots.findByMonthBetween(start, end);

        Map<java.util.UUID, AccountView> accountsById = new HashMap<>();
        for (AccountView account : liquidityAccounts) {
            accountsById.put(account.id(), account);
        }

        Map<java.util.UUID, List<LiquiditySnapshot>> liquidityByAccount = new HashMap<>();
        for (LiquiditySnapshot snapshot : liquiditySnapshotList) {
            if (accountsById.containsKey(snapshot.getAccountId())) {
                liquidityByAccount.computeIfAbsent(snapshot.getAccountId(), key -> new ArrayList<>()).add(snapshot);
            }
        }

        liquidityByAccount.values().forEach(list -> list.sort(Comparator.comparing(LiquiditySnapshot::getMonth)));

        Map<java.util.UUID, List<InvestmentSnapshot>> snapshotsByAccount = new HashMap<>();
        for (InvestmentSnapshot snapshot : investmentSnapshotList) {
            if (snapshot.getAccountId() == null) {
                continue;
            }
            snapshotsByAccount.computeIfAbsent(snapshot.getAccountId(), key -> new ArrayList<>()).add(snapshot);
        }

        snapshotsByAccount.values().forEach(list -> list.sort(Comparator.comparing(InvestmentSnapshot::getMonth)));

        Set<Currency> currencies = new HashSet<>();
        liquidityAccounts.forEach(a -> currencies.add(a.currency()));
        investmentAccounts.forEach(a -> currencies.add(a.currency()));

        Map<Currency, BigDecimal> fxRatesToBase = new HashMap<>();
        for (Currency currency : currencies) {
            if (currency != baseCurrency) {
                fxRatesToBase.put(currency, fxRateService.getRate(currency, baseCurrency));
            }
        }

        List<AssetsMonthlyView> monthly = new ArrayList<>(12);
        YearMonth current = start;
        Map<java.util.UUID, BigDecimal> latestInvestmentByAccount = new HashMap<>();
        Map<java.util.UUID, Integer> investmentCursorByAccount = new HashMap<>();
        Map<java.util.UUID, BigDecimal> latestLiquidityByAccount = new HashMap<>();
        Map<java.util.UUID, Integer> liquidityCursorByAccount = new HashMap<>();

        for (int i = 0; i < 12; i++) {
            BigDecimal liquidity = BigDecimal.ZERO;
            for (AccountView account : liquidityAccounts) {
                List<LiquiditySnapshot> accountSnapshots = liquidityByAccount.getOrDefault(account.id(), List.of());
                int cursor = liquidityCursorByAccount.getOrDefault(account.id(), 0);

                while (cursor < accountSnapshots.size() && !accountSnapshots.get(cursor).getMonth().isAfter(current)) {
                    latestLiquidityByAccount.put(account.id(), accountSnapshots.get(cursor).getLiquidity().getAmount());
                    cursor++;
                }

                liquidityCursorByAccount.put(account.id(), cursor);

                BigDecimal snapshotLiquidity = latestLiquidityByAccount.get(account.id());
                if (snapshotLiquidity != null) {
                    liquidity = liquidity
                            .add(convert(snapshotLiquidity, account.currency(), baseCurrency, fxRatesToBase));
                }
            }

            BigDecimal investments = BigDecimal.ZERO;
            for (AccountView account : investmentAccounts) {
                List<InvestmentSnapshot> accountSnapshots = snapshotsByAccount.getOrDefault(account.id(), List.of());
                int cursor = investmentCursorByAccount.getOrDefault(account.id(), 0);

                while (cursor < accountSnapshots.size()
                        && !accountSnapshots.get(cursor).getMonth().isAfter(current)) {
                    latestInvestmentByAccount.put(account.id(),
                            accountSnapshots.get(cursor).getTotalInvested().getAmount());
                    cursor++;
                }

                investmentCursorByAccount.put(account.id(), cursor);

                BigDecimal latest = latestInvestmentByAccount.get(account.id());
                if (latest != null) {
                    investments = investments.add(convert(latest, account.currency(), baseCurrency, fxRatesToBase));
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
