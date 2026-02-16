create index idx_transactions_account_date
on transactions(account_id, date);

create index idx_transactions_transfer_id
on transactions(transfer_id);
