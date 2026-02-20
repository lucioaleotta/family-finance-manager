package com.lucio.financeapp.assets.domain;

import com.lucio.financeapp.shared.domain.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "liquidity_snapshots", uniqueConstraints = @UniqueConstraint(name = "uk_liquidity_snapshot_month_account", columnNames = {
        "month", "account_id" }))
public class LiquiditySnapshot {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "month", nullable = false, length = 7)
    private String month;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Embedded
    private Money liquidity;

    private String note;

    protected LiquiditySnapshot() {
    }

    private LiquiditySnapshot(YearMonth month, UUID accountId, Money liquidity, String note) {
        this.month = month.toString();
        this.accountId = accountId;
        this.liquidity = liquidity;
        this.note = note;
    }

    public static LiquiditySnapshot of(YearMonth month, UUID accountId, Money liquidity, String note) {
        return new LiquiditySnapshot(month, accountId, liquidity, note);
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

    public Money getLiquidity() {
        return liquidity;
    }

    public String getNote() {
        return note;
    }

    public void update(Money liquidity, String note) {
        this.liquidity = liquidity;
        this.note = note;
    }
}
