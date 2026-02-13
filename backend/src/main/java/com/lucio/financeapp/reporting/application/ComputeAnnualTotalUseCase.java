package com.lucio.financeapp.reporting.application;

import com.lucio.financeapp.reporting.api.AnnualTotalView;
import com.lucio.financeapp.reporting.api.MonthlySummaryView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ComputeAnnualTotalUseCase {

    private final ComputeAnnualTimelineUseCase timelineUseCase;

    public ComputeAnnualTotalUseCase(ComputeAnnualTimelineUseCase timelineUseCase) {
        this.timelineUseCase = timelineUseCase;
    }

    public AnnualTotalView handle(int year) {
        List<MonthlySummaryView> timeline = timelineUseCase.handle(year);

        BigDecimal totalIncome = timeline.stream()
                .map(MonthlySummaryView::totalIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = timeline.stream()
                .map(MonthlySummaryView::totalExpense)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal annualResult = totalIncome.subtract(totalExpense);

        int monthsWithData = (int) timeline.stream()
                .filter(m -> m.totalIncome().signum() != 0 || m.totalExpense().signum() != 0)
                .count();

        BigDecimal avgMonthlySavings = monthsWithData == 0
                ? BigDecimal.ZERO
                : annualResult.divide(BigDecimal.valueOf(monthsWithData), 2, RoundingMode.HALF_UP);

        return new AnnualTotalView(year, totalIncome, totalExpense, annualResult, monthsWithData, avgMonthlySavings);
    }
}
