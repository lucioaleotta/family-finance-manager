ALTER TABLE public.accounts
    ADD COLUMN user_id uuid;

ALTER TABLE public.transactions
    ADD COLUMN user_id uuid;

ALTER TABLE public.accounts
    DROP CONSTRAINT IF EXISTS uk_qtv290mh55xhggmpwosf5ag0v;

ALTER TABLE public.accounts
    ADD CONSTRAINT uk_accounts_user_name UNIQUE (user_id, name);

ALTER TABLE public.accounts
    ADD CONSTRAINT fk_accounts_user
    FOREIGN KEY (user_id)
    REFERENCES public.users (id);

ALTER TABLE public.transactions
    ADD CONSTRAINT fk_transactions_user
    FOREIGN KEY (user_id)
    REFERENCES public.users (id);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON public.accounts (user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON public.transactions (user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user_date ON public.transactions (user_id, date);
