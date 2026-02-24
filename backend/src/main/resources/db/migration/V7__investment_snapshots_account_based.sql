alter table investment_snapshots
    add column account_id uuid;

alter table investment_snapshots
    drop constraint if exists uk_investment_snapshot_month_currency;

alter table investment_snapshots
    add constraint fk_investment_snapshot_account
    foreign key (account_id) references accounts(id);

alter table investment_snapshots
    add constraint uk_investment_snapshot_month_account
    unique (month, account_id);

create index if not exists idx_investment_snapshots_account_month
on investment_snapshots(account_id, month);
