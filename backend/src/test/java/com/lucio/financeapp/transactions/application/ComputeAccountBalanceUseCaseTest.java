package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.transactions.domain.TransactionType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComputeAccountBalanceUseCaseTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-00000000b100");

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ComputeAccountBalanceUseCase useCase;

    @Test
    void shouldComputeBalanceFromIncomeAndExpense() {
        UUID accountId = UUID.randomUUID();
        LocalDate asOf = LocalDate.of(2026, 2, 28);

        Account account = Account.of(USER_ID, "Fineco", AccountType.CHECKING, Currency.EUR);
        when(accountRepository.findByIdAndUserId(accountId, USER_ID)).thenReturn(Optional.of(account));

        Transaction income = Transaction.standard(USER_ID, accountId, Money.of(new BigDecimal("1200.00"), Currency.EUR),
                asOf,
                TransactionType.INCOME, "SALARY", "Salary");
        Transaction expense = Transaction.standard(USER_ID, accountId, Money.of(new BigDecimal("250.00"), Currency.EUR),
                asOf,
                TransactionType.EXPENSE, "GROCERIES", "Food");

        when(transactionRepository.findByAccountUpTo(accountId, USER_ID, asOf)).thenReturn(List.of(income, expense));

        var result = useCase.handle(USER_ID, accountId, asOf);

        assertEquals(accountId, result.accountId());
        assertEquals(asOf, result.asOf());
        assertEquals(new BigDecimal("950.00"), result.balance());
        assertEquals(Currency.EUR, result.currency());
    }

    @Test
    void shouldThrowWhenAccountDoesNotExist() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findByIdAndUserId(accountId, USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> useCase.handle(USER_ID, accountId, LocalDate.of(2026, 2, 28)));

        assertTrue(exception.getMessage().contains("Account not found"));
    }
}
