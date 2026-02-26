package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.transactions.domain.TransactionKind;
import com.lucio.financeapp.transactions.domain.TransactionType;
import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTransferUseCaseTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-00000000c100");

    @Mock
    private TransactionRepository repository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CreateTransferUseCase useCase;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @Test
    void shouldCreateTwoTransferTransactionsWithSameTransferId() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        when(accountRepository.findByIdAndUserId(fromAccountId, USER_ID))
                .thenReturn(java.util.Optional.of(Account.of(USER_ID, "From", AccountType.CHECKING, Currency.EUR)));
        when(accountRepository.findByIdAndUserId(toAccountId, USER_ID))
                .thenReturn(java.util.Optional.of(Account.of(USER_ID, "To", AccountType.CHECKING, Currency.EUR)));

        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID transferId = useCase.handle(USER_ID, new CreateTransferUseCase.CreateTransferCommand(
                fromAccountId,
                toAccountId,
                new BigDecimal("10.00"),
                Currency.EUR,
                LocalDate.of(2026, 2, 12),
                "Giroconto test"));

        verify(repository, times(2)).save(transactionCaptor.capture());
        List<Transaction> saved = transactionCaptor.getAllValues();

        assertEquals(2, saved.size());
        assertNotNull(transferId);

        Transaction out = saved.get(0);
        Transaction in = saved.get(1);

        assertEquals(TransactionKind.TRANSFER, out.getKind());
        assertEquals(TransactionType.EXPENSE, out.getType());
        assertEquals(fromAccountId, out.getAccountId());
        assertEquals(transferId, out.getTransferId());

        assertEquals(TransactionKind.TRANSFER, in.getKind());
        assertEquals(TransactionType.INCOME, in.getType());
        assertEquals(toAccountId, in.getAccountId());
        assertEquals(transferId, in.getTransferId());
    }
}
