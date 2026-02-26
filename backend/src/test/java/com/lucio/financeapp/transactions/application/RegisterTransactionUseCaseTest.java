package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.transactions.domain.TransactionType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterTransactionUseCaseTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-00000000f100");

    @Mock
    private TransactionRepository repository;

    @Mock
    private DefaultAccountService defaultAccountService;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private RegisterTransactionUseCase useCase;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @Test
    void shouldUseProvidedAccountIdWhenPresent() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findByIdAndUserId(accountId, USER_ID))
                .thenReturn(Optional.of(Account.of(USER_ID, "Main", AccountType.CHECKING, Currency.EUR)));
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID result = useCase.handle(USER_ID, new RegisterTransactionUseCase.RegisterTransactionCommand(
                accountId,
                new BigDecimal("45.00"),
                Currency.EUR,
                LocalDate.of(2026, 2, 10),
                TransactionType.EXPENSE,
                "GROCERIES",
                "Spesa"));

        verify(defaultAccountService, never()).getOrCreateDefaultAccountId(any(UUID.class), any(Currency.class));
        verify(repository).save(transactionCaptor.capture());

        Transaction saved = transactionCaptor.getValue();
        assertNotNull(result);
        assertEquals(accountId, saved.getAccountId());
        assertEquals(new BigDecimal("45.00"), saved.getAmount().getAmount());
        assertEquals(Currency.EUR, saved.getAmount().getCurrency());
    }

    @Test
    void shouldFallbackToDefaultAccountWhenAccountIdIsNull() {
        UUID defaultAccountId = UUID.randomUUID();
        when(defaultAccountService.getOrCreateDefaultAccountId(USER_ID, Currency.EUR)).thenReturn(defaultAccountId);
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.handle(USER_ID, new RegisterTransactionUseCase.RegisterTransactionCommand(
                null,
                new BigDecimal("20.00"),
                Currency.EUR,
                LocalDate.of(2026, 2, 11),
                TransactionType.INCOME,
                "SALARY",
                "Entrata"));

        verify(defaultAccountService).getOrCreateDefaultAccountId(USER_ID, Currency.EUR);
        verify(repository).save(transactionCaptor.capture());

        Transaction saved = transactionCaptor.getValue();
        assertEquals(defaultAccountId, saved.getAccountId());
        assertEquals(TransactionType.INCOME, saved.getType());
        assertEquals("SALARY", saved.getCategory());
    }
}
