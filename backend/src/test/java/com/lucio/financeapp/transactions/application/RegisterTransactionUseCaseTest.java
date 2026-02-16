package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.transactions.domain.TransactionType;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterTransactionUseCaseTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private DefaultAccountService defaultAccountService;

    @InjectMocks
    private RegisterTransactionUseCase useCase;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @Test
    void shouldUseProvidedAccountIdWhenPresent() {
        UUID accountId = UUID.randomUUID();
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID result = useCase.handle(new RegisterTransactionUseCase.RegisterTransactionCommand(
                accountId,
                new BigDecimal("45.00"),
                Currency.EUR,
                LocalDate.of(2026, 2, 10),
                TransactionType.EXPENSE,
                "GROCERIES",
                "Spesa"));

        verify(defaultAccountService, never()).getOrCreateDefaultAccountId();
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
        when(defaultAccountService.getOrCreateDefaultAccountId()).thenReturn(defaultAccountId);
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.handle(new RegisterTransactionUseCase.RegisterTransactionCommand(
                null,
                new BigDecimal("20.00"),
                Currency.EUR,
                LocalDate.of(2026, 2, 11),
                TransactionType.INCOME,
                "SALARY",
                "Entrata"));

        verify(defaultAccountService).getOrCreateDefaultAccountId();
        verify(repository).save(transactionCaptor.capture());

        Transaction saved = transactionCaptor.getValue();
        assertEquals(defaultAccountId, saved.getAccountId());
        assertEquals(TransactionType.INCOME, saved.getType());
        assertEquals("SALARY", saved.getCategory());
    }
}
