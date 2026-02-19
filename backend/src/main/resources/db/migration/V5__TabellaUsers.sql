CREATE TABLE public.users (
    id uuid NOT NULL,
    username character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    base_currency character varying(3) NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username)
);
