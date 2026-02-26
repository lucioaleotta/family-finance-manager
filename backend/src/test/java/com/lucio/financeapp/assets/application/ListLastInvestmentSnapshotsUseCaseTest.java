package com.lucio.financeapp.assets.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;

@ExtendWith(MockitoExtension.class)
class ListLastInvestmentSnapshotsUseCaseTest {

        @Mock
        private InvestmentSnapshotRepository repository;

        @Mock
        private AccountRepository accountRepository;

        @InjectMocks
        private ListLastInvestmentSnapshotsUseCase useCase;

        @Test
        void shouldReturnLastTwelveMonthsInAscendingOrderWithZeros() {
                YearMonth end = YearMonth.of(2026, 2);
                YearMonth start = YearMonth.of(2025, 3);
                UUID accountId = UUID.fromString("00000000-0000-0000-0000-000000000201");
                Account account = Account.of("Broker", AccountType.INVESTMENT, Currency.EUR);

                InvestmentSnapshot startSnapshot = InvestmentSnapshot.of(
                                start,
                                accountId,
                                Money.of(new BigDecimal("1000.00"), Currency.EUR),
                                "start");

                InvestmentSnapshot endSnapshot = InvestmentSnapshot.of(
                                end,
                                accountId,
                                Money.of(new BigDecimal("2500.00"), Currency.EUR),
                                "end");

                when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
                when(repository.findByMonthBetweenAndAccountId(start, end, accountId))
                                .thenReturn(List.of(endSnapshot, startSnapshot));

                var result = useCase.handle(end, accountId);

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
