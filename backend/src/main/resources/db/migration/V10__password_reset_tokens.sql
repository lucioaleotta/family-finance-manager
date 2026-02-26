ALTER TABLE public.users
    ADD COLUMN email character varying(255);

UPDATE public.users
SET email = lower(username)
WHERE email IS NULL;

ALTER TABLE public.users
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE public.users
    ADD CONSTRAINT uk_users_email UNIQUE (email);

CREATE TABLE public.password_reset_tokens (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    token_hash character varying(64) NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    used_at timestamp with time zone,
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES public.users (id) ON DELETE CASCADE,
    CONSTRAINT uk_password_reset_tokens_hash UNIQUE (token_hash)
);

CREATE INDEX idx_password_reset_tokens_user_id ON public.password_reset_tokens (user_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON public.password_reset_tokens (expires_at);
