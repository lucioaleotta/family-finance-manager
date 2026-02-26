package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpsertInvestmentSnapshotUseCaseTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-00000000e100");

    @Mock
    private InvestmentSnapshotRepository repository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UpsertInvestmentSnapshotUseCase useCase;

    @Captor
    private ArgumentCaptor<InvestmentSnapshot> snapshotCaptor;

    @Test
    void shouldCreateSnapshotWhenNotExists() {
        YearMonth month = YearMonth.of(2026, 2);
        UUID accountId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        Account account = Account.of(USER_ID, "Investimenti", AccountType.INVESTMENT, Currency.EUR);

        when(accountRepository.findByIdAndUserId(accountId, USER_ID)).thenReturn(Optional.of(account));
        when(repository.findByMonthAndAccountId(month, accountId)).thenReturn(Optional.empty());
        when(repository.save(any(InvestmentSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.handle(USER_ID, new UpsertInvestmentSnapshotUseCase.Command(
                month,
                accountId,
                new BigDecimal("15000.00"),
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
        UUID accountId = UUID.fromString("00000000-0000-0000-0000-000000000102");
        Account account = Account.of(USER_ID, "Investimenti", AccountType.INVESTMENT, Currency.EUR);
        InvestmentSnapshot existing = InvestmentSnapshot.of(month,
                accountId,
                Money.of(new BigDecimal("10000.00"), Currency.EUR),
                "old");

        when(accountRepository.findByIdAndUserId(accountId, USER_ID)).thenReturn(Optional.of(account));
        when(repository.findByMonthAndAccountId(month, accountId)).thenReturn(Optional.of(existing));
        when(repository.save(any(InvestmentSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.handle(USER_ID, new UpsertInvestmentSnapshotUseCase.Command(
                month,
                accountId,
                new BigDecimal("20000.00"),
                "new"));

        verify(repository).save(snapshotCaptor.capture());

        InvestmentSnapshot saved = snapshotCaptor.getValue();
        assertEquals(new BigDecimal("20000.00"), saved.getTotalInvested().getAmount());
        assertEquals("new", saved.getNote());
    }
}
