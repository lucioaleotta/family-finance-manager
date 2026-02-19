package com.lucio.financeapp.assets.application;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lucio.financeapp.assets.api.InvestmentSnapshotView;
import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.shared.domain.Currency;

@Service
@Transactional(readOnly = true)
public class ListLastInvestmentSnapshotsUseCase {

    private final InvestmentSnapshotRepository repository;

    public ListLastInvestmentSnapshotsUseCase(InvestmentSnapshotRepository repository) {
        this.repository = repository;
    }

    public List<InvestmentSnapshotView> handle(YearMonth endMonth, Currency currency) {
        YearMonth end = endMonth == null ? YearMonth.now() : endMonth;
        YearMonth start = end.minusMonths(11);

        List<InvestmentSnapshot> snapshots = repository.findByMonthBetween(start, end, currency);
        Map<YearMonth, InvestmentSnapshot> byMonth = snapshots.stream()
                .collect(Collectors.toMap(InvestmentSnapshot::getMonth, Function.identity(), (a, b) -> a));

        List<InvestmentSnapshotView> out = new ArrayList<>(12);
        YearMonth current = start;
        for (int i = 0; i < 12; i++) {
            InvestmentSnapshot snapshot = byMonth.get(current);
            if (snapshot != null) {
                out.add(new InvestmentSnapshotView(
                        snapshot.getMonth(),
                        snapshot.getTotalInvested().getAmount(),
                        currency,
                        snapshot.getNote()));
            } else {
                out.add(new InvestmentSnapshotView(
                        current,
                        BigDecimal.ZERO,
                        currency,
                        null));
            }
            current = current.plusMonths(1);
        }

        return out;
    }
}
