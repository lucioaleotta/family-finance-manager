package com.lucio.financeapp.transactions.api;

import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
class TransactionFacadeImpl implements TransactionFacade {

    private final TransactionRepository repository;

    TransactionFacadeImpl(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TransactionView> findByMonth(YearMonth month) {
        return repository.findByMonth(month).stream()
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
                tx.getDescription());

    }
}
