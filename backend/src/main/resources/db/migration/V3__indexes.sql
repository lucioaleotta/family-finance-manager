create index idx_transactions_kind
on transactions(kind);

create index idx_snapshots_month_currency
on investment_snapshots(month, currency);

alter table transactions
add constraint chk_amount_positive
check (amount_value > 0);

