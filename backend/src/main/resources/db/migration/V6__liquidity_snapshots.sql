create table liquidity_snapshots (
    id uuid not null,
    month varchar(7) not null,
    account_id uuid not null,
    note varchar(255),
    amount_value numeric(19,4) not null,
    amount_currency varchar(3) not null,
    primary key (id),
    constraint uk_liquidity_snapshot_month_account unique (month, account_id),
    constraint fk_liquidity_snapshot_account foreign key (account_id) references accounts(id),
    constraint liquidity_snapshots_amount_currency_check check (amount_currency in ('EUR', 'CHF'))
);

create index idx_liquidity_snapshots_account_month
on liquidity_snapshots(account_id, month);
