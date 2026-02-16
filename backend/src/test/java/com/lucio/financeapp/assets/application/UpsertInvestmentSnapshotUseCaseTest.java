package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.shared.domain.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpsertInvestmentSnapshotUseCaseTest {

    @Mock
    private InvestmentSnapshotRepository repository;

    @InjectMocks
    private UpsertInvestmentSnapshotUseCase useCase;

    @Captor
    private ArgumentCaptor<InvestmentSnapshot> snapshotCaptor;

    @Test
    void shouldCreateSnapshotWhenNotExists() {
        YearMonth month = YearMonth.of(2026, 2);
        when(repository.findByMonthAndCurrency(month, Currency.EUR)).thenReturn(Optional.empty());
        when(repository.save(any(InvestmentSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.handle(new UpsertInvestmentSnapshotUseCase.Command(
                month,
                new BigDecimal("15000.00"),
                Currency.EUR,
                "PAC"));

        verify(repository).save(snapshotCaptor.capture());

        InvestmentSnapshot saved = snapshotCaptor.getValue();
        assertEquals(month, saved.getMonth());
        assertEquals(new BigDecimal("15000.00"), saved.getTotalInvested().getAmount());
        assertEquals("PAC", saved.getNote());
    }

    @Test
    void shouldUpdateExistingSnapshot() {
        YearMonth month = YearMonth.of(2026, 2);
        InvestmentSnapshot existing = InvestmentSnapshot.of(month,
                com.lucio.financeapp.shared.domain.Money.of(new BigDecimal("10000.00"), Currency.EUR),
                "old");

        when(repository.findByMonthAndCurrency(month, Currency.EUR)).thenReturn(Optional.of(existing));
        when(repository.save(any(InvestmentSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.handle(new UpsertInvestmentSnapshotUseCase.Command(
                month,
                new BigDecimal("20000.00"),
                Currency.EUR,
                "new"));

        verify(repository).save(snapshotCaptor.capture());

        InvestmentSnapshot saved = snapshotCaptor.getValue();
        assertEquals(new BigDecimal("20000.00"), saved.getTotalInvested().getAmount());
        assertEquals("new", saved.getNote());
    }
}
