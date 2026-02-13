package com.lucio.financeapp.reporting.application;

import com.lucio.financeapp.reporting.api.MonthlySummaryView;
import com.lucio.financeapp.transactions.api.TransactionFacade;
import com.lucio.financeapp.transactions.api.TransactionView;
import com.lucio.financeapp.transactions.domain.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ComputeAnnualTimelineUseCase {

    private final TransactionFacade transactionFacade;

    public ComputeAnnualTimelineUseCase(TransactionFacade transactionFacade) {
        this.transactionFacade = transactionFacade;
    }

    public List<MonthlySummaryView> handle(int year) {
        List<MonthlySummaryView> result = new ArrayList<>(12);

        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(year, m);
            List<TransactionView> txs = transactionFacade.findByMonth(ym);

            BigDecimal income = txs.stream()
                    .filter(t -> t.type() == TransactionType.INCOME)
                    .map(tx -> tx.amount().getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = txs.stream()
                    .filter(t -> t.type() == TransactionType.EXPENSE)
                    .map(tx -> tx.amount().getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal monthlyResult = income.subtract(expense);

            result.add(new MonthlySummaryView(ym, income, expense, monthlyResult));
        }

        return result;
    }
}
