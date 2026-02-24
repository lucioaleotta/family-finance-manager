package com.lucio.financeapp.transactions.api;

import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
class TransactionFacadeImpl implements TransactionFacade {

    private final TransactionRepository repository;

    TransactionFacadeImpl(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TransactionView> findByMonth(UUID userId, YearMonth month) {
        return repository.findByMonthAndUserId(month, userId).stream()
                .map(TransactionFacadeImpl::toView)
                .toList();
    }

    @Override
    public List<TransactionView> findByMonthAndAccount(UUID userId, YearMonth month, UUID accountId) {
        return repository.findByMonthAndAccount(month, accountId, userId).stream()
                .map(TransactionFacadeImpl::toView)
                .toList();
    }

    @Override
    public List<TransactionView> findByAccountUpTo(UUID userId, UUID accountId, LocalDate asOf) {
        return repository.findByAccountUpTo(accountId, userId, asOf).stream()
                .map(TransactionFacadeImpl::toView)
                .toList();
    }

    private static TransactionView toView(Transaction tx) {
        return new TransactionView(
                tx.getId(),
                tx.getAccountId(),
                tx.getAmount(),
                tx.getDate(),
                tx.getType(),
                tx.getCategory(),
                tx.getDescription(),
                tx.getKind(),
                tx.getTransferId());

    }
}
