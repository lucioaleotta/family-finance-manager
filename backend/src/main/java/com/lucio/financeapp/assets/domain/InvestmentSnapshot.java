package com.lucio.financeapp.assets.domain;

import com.lucio.financeapp.shared.domain.Money;
import jakarta.persistence.*;

import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "investment_snapshots", uniqueConstraints = @UniqueConstraint(name = "uk_investment_snapshot_month_account", columnNames = {
        "month", "account_id" }))
public class InvestmentSnapshot {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 7) // "YYYY-MM"
    private String month;

    @Column(name = "account_id")
    private UUID accountId;

    @Embedded
    private Money totalInvested;

    @Column(nullable = false, length = 3)
    private String currency;

    private String note;

    protected InvestmentSnapshot() {
    }

    private InvestmentSnapshot(YearMonth month, UUID accountId, Money totalInvested, String note) {
        this.month = month.toString();
        this.accountId = accountId;
        this.totalInvested = totalInvested;
        this.currency = totalInvested.getCurrency().name();
        this.note = note;
    }

    public static InvestmentSnapshot of(YearMonth month, UUID accountId, Money totalInvested, String note) {
        return new InvestmentSnapshot(month, accountId, totalInvested, note);
    }

    public UUID getId() {
        return id;
    }

    public YearMonth getMonth() {
        return YearMonth.parse(month);
    }

    public UUID getAccountId() {
        return accountId;
    }

    public Money getTotalInvested() {
        return totalInvested;
    }

    public String getNote() {
        return note;
    }

    public void update(Money totalInvested, String note) {
        this.totalInvested = totalInvested;
        this.currency = totalInvested.getCurrency().name();
        this.note = note;
    }

}
