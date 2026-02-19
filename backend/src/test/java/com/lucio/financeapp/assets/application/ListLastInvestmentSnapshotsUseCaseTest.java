package com.lucio.financeapp.assets.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.shared.domain.Money;

@ExtendWith(MockitoExtension.class)
class ListLastInvestmentSnapshotsUseCaseTest {

    @Mock
    private InvestmentSnapshotRepository repository;

    @InjectMocks
    private ListLastInvestmentSnapshotsUseCase useCase;

    @Test
    void shouldReturnLastTwelveMonthsInAscendingOrderWithZeros() {
        YearMonth end = YearMonth.of(2026, 2);
        YearMonth start = YearMonth.of(2025, 3);

        InvestmentSnapshot startSnapshot = InvestmentSnapshot.of(
                start,
                Money.of(new BigDecimal("1000.00"), Currency.EUR),
                "start");

        InvestmentSnapshot endSnapshot = InvestmentSnapshot.of(
                end,
                Money.of(new BigDecimal("2500.00"), Currency.EUR),
                "end");

        when(repository.findByMonthBetween(start, end, Currency.EUR))
                .thenReturn(List.of(endSnapshot, startSnapshot));

        var result = useCase.handle(end, Currency.EUR);

        assertEquals(12, result.size());
        assertEquals(start, result.get(0).month());
        assertEquals(new BigDecimal("1000.00"), result.get(0).totalInvested());
        assertEquals("start", result.get(0).note());

        assertEquals(end, result.get(11).month());
        assertEquals(new BigDecimal("2500.00"), result.get(11).totalInvested());
        assertEquals("end", result.get(11).note());

        var middle = result.get(5);
        assertEquals(BigDecimal.ZERO, middle.totalInvested());
        assertNull(middle.note());
    }
}
