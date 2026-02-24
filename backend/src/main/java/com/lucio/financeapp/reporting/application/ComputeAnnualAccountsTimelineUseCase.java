package com.lucio.financeapp.reporting.application;

import com.lucio.financeapp.reporting.api.MonthlyAccountTimelineView;
import com.lucio.financeapp.transactions.api.TransactionFacade;
import com.lucio.financeapp.transactions.api.TransactionView;
import com.lucio.financeapp.transactions.domain.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ComputeAnnualAccountsTimelineUseCase {

    private final TransactionFacade transactionFacade;

    public ComputeAnnualAccountsTimelineUseCase(TransactionFacade transactionFacade) {
        this.transactionFacade = transactionFacade;
    }

    public List<MonthlyAccountTimelineView> handle(UUID userId, int year) {
        // month -> (accountId -> list of tx)
        Map<YearMonth, Map<UUID, List<TransactionView>>> index = new LinkedHashMap<>();

        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(year, m);
            List<TransactionView> txs = transactionFacade.findByMonth(userId, ym);

            Map<UUID, List<TransactionView>> byAccount = txs.stream()
                    .filter(t -> t.accountId() != null)
                    .collect(Collectors.groupingBy(TransactionView::accountId));

            index.put(ym, byAccount);
        }

        // All accounts encountered during the year
        Set<UUID> allAccounts = index.values().stream()
                .flatMap(m -> m.keySet().stream())
                .collect(Collectors.toCollection(TreeSet::new)); // stable ordering

        List<MonthlyAccountTimelineView> result = new ArrayList<>();

        for (YearMonth ym : index.keySet()) {
            Map<UUID, List<TransactionView>> byAccount = index.get(ym);

            for (UUID accountId : allAccounts) {
                List<TransactionView> txs = byAccount.getOrDefault(accountId, List.of());

                BigDecimal income = txs.stream()
                        .filter(t -> t.type() == TransactionType.INCOME)
                        .map(t -> t.amount().getAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal expense = txs.stream()
                        .filter(t -> t.type() == TransactionType.EXPENSE)
                        .map(t -> t.amount().getAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                result.add(new MonthlyAccountTimelineView(
                        ym,
                        accountId,
                        income,
                        expense,
                        income.subtract(expense)));
            }
        }

        // order: month asc, accountId asc
        result.sort(Comparator
                .comparing(MonthlyAccountTimelineView::month)
                .thenComparing(MonthlyAccountTimelineView::accountId));

        return result;
    }
}
