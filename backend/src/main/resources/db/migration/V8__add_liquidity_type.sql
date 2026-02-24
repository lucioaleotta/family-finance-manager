-- Aggiungi il tipo LIQUIDITY al constraint accounts_type_check

ALTER TABLE accounts DROP CONSTRAINT IF EXISTS accounts_type_check;

ALTER TABLE accounts ADD CONSTRAINT accounts_type_check 
CHECK (type IN ('CHECKING', 'LIQUIDITY', 'SAVINGS', 'CASH', 'CARD', 'INVESTMENT'));
