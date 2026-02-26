package com.lucio.financeapp.reporting.application;

import com.lucio.financeapp.reporting.api.AnnualAccountSummaryView;
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
public class ComputeAnnualAccountsUseCase {

    private final TransactionFacade transactionFacade;

    public ComputeAnnualAccountsUseCase(TransactionFacade transactionFacade) {
        this.transactionFacade = transactionFacade;
    }

    public List<AnnualAccountSummaryView> handle(UUID userId, int year) {
        List<TransactionView> all = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            all.addAll(transactionFacade.findByMonth(userId, YearMonth.of(year, m)));
        }

        Map<UUID, List<TransactionView>> byAccount = all.stream()
                .filter(t -> t.accountId() != null)
                .collect(Collectors.groupingBy(TransactionView::accountId));

        List<AnnualAccountSummaryView> result = new ArrayList<>();

        for (var entry : byAccount.entrySet()) {
            UUID accountId = entry.getKey();
            List<TransactionView> txs = entry.getValue();

            BigDecimal income = txs.stream()
                    .filter(t -> t.type() == TransactionType.INCOME)
                    .map(t -> t.amount().getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = txs.stream()
                    .filter(t -> t.type() == TransactionType.EXPENSE)
                    .map(t -> t.amount().getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(new AnnualAccountSummaryView(year, accountId, income, expense, income.subtract(expense)));
        }

        result.sort(Comparator.comparing(AnnualAccountSummaryView::accountId));
        return result;
    }
}
