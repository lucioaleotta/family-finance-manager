package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.shared.domain.Currency;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
@Transactional
public class UpsertInvestmentSnapshotUseCase {

    private final InvestmentSnapshotRepository repository;

    public UpsertInvestmentSnapshotUseCase(InvestmentSnapshotRepository repository) {
        this.repository = repository;
    }

    public void handle(Command command) {
        Money money = Money.of(command.totalInvested(), command.currency());

        InvestmentSnapshot snapshot = repository.findByMonthAndCurrency(command.month(), command.currency())
                .orElseGet(() -> InvestmentSnapshot.of(command.month(), money, command.note()));

        snapshot.update(money, command.note());
        repository.save(snapshot);

    }

    public record Command(YearMonth month, BigDecimal totalInvested, Currency currency, String note) {
    }
}
