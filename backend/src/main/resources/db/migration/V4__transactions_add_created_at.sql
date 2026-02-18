ALTER TABLE transactions
ADD COLUMN created_at TIMESTAMPTZ;

UPDATE transactions
SET created_at = now()
WHERE created_at IS NULL;

ALTER TABLE transactions
ALTER COLUMN created_at SET NOT NULL;

-- Indice per le query "mese" e "lista latest"
CREATE INDEX IF NOT EXISTS idx_transactions_date_created
ON transactions(date DESC, created_at DESC);

-- (utile spesso) query per account+data
CREATE INDEX IF NOT EXISTS idx_transactions_account_date_created
ON transactions(account_id, date DESC, created_at DESC);
