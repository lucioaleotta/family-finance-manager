package com.lucio.financeapp.reporting.application;

import com.lucio.financeapp.reporting.api.MonthlySummaryView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComputeAnnualTotalUseCaseTest {

    @Mock
    private ComputeAnnualTimelineUseCase timelineUseCase;

    @InjectMocks
    private ComputeAnnualTotalUseCase useCase;

    @Test
    void shouldComputeYearlyTotalsAndAverageOnMonthsWithData() {
        when(timelineUseCase.handle(2026)).thenReturn(List.of(
                new MonthlySummaryView(YearMonth.of(2026, 1), new BigDecimal("100.00"), new BigDecimal("20.00"),
                        new BigDecimal("80.00")),
                new MonthlySummaryView(YearMonth.of(2026, 2), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                new MonthlySummaryView(YearMonth.of(2026, 3), new BigDecimal("50.00"), new BigDecimal("70.00"),
                        new BigDecimal("-20.00"))));

        var result = useCase.handle(2026);

        assertEquals(new BigDecimal("150.00"), result.totalIncome());
        assertEquals(new BigDecimal("90.00"), result.totalExpense());
        assertEquals(new BigDecimal("60.00"), result.annualResult());
        assertEquals(2, result.monthsWithData());
        assertEquals(new BigDecimal("30.00"), result.avgMonthlySavings());
    }

    @Test
    void shouldReturnZeroAverageWhenNoData() {
        when(timelineUseCase.handle(2027)).thenReturn(List.of(
                new MonthlySummaryView(YearMonth.of(2027, 1), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));

        var result = useCase.handle(2027);

        assertEquals(0, result.monthsWithData());
        assertEquals(BigDecimal.ZERO, result.avgMonthlySavings());
    }
}
