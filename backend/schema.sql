--
-- PostgreSQL database dump
--

\restrict ELIw7Oz6dl83T1aB4e1VYcJ8niG4SAuMtYPkSSkd4Wuef4hFbZHYQr6hDGMlYA7

-- Dumped from database version 16.11 (Debian 16.11-1.pgdg13+1)
-- Dumped by pg_dump version 16.11 (Debian 16.11-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: accounts; Type: TABLE; Schema: public; Owner: finance
--

CREATE TABLE public.accounts (
    id uuid NOT NULL,
    currency character varying(3) NOT NULL,
    name character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    CONSTRAINT accounts_currency_check CHECK (((currency)::text = ANY ((ARRAY['EUR'::character varying, 'CHF'::character varying])::text[]))),
    CONSTRAINT accounts_type_check CHECK (((type)::text = ANY ((ARRAY['CHECKING'::character varying, 'SAVINGS'::character varying, 'CASH'::character varying, 'CARD'::character varying, 'INVESTMENT'::character varying])::text[])))
);


ALTER TABLE public.accounts OWNER TO finance;

--
-- Name: event_publication; Type: TABLE; Schema: public; Owner: finance
--

CREATE TABLE public.event_publication (
    id uuid NOT NULL,
    completion_date timestamp(6) with time zone,
    event_type character varying(255),
    listener_id character varying(255),
    publication_date timestamp(6) with time zone,
    serialized_event character varying(255)
);


ALTER TABLE public.event_publication OWNER TO finance;

--
-- Name: investment_snapshots; Type: TABLE; Schema: public; Owner: finance
--

CREATE TABLE public.investment_snapshots (
    id uuid NOT NULL,
    currency character varying(3) NOT NULL,
    month character varying(7) NOT NULL,
    note character varying(255),
    amount_value numeric(19,4) NOT NULL,
    amount_currency character varying(3) NOT NULL,
    CONSTRAINT investment_snapshots_amount_currency_check CHECK (((amount_currency)::text = ANY ((ARRAY['EUR'::character varying, 'CHF'::character varying])::text[])))
);


ALTER TABLE public.investment_snapshots OWNER TO finance;

--
-- Name: transactions; Type: TABLE; Schema: public; Owner: finance
--

CREATE TABLE public.transactions (
    id uuid NOT NULL,
    account_id uuid,
    amount_value numeric(19,4) NOT NULL,
    amount_currency character varying(3) NOT NULL,
    category character varying(255) NOT NULL,
    date date NOT NULL,
    description character varying(255),
    kind character varying(255) NOT NULL,
    transfer_id uuid,
    type character varying(255) NOT NULL,
    CONSTRAINT transactions_amount_currency_check CHECK (((amount_currency)::text = ANY ((ARRAY['EUR'::character varying, 'CHF'::character varying])::text[]))),
    CONSTRAINT transactions_kind_check CHECK (((kind)::text = ANY ((ARRAY['STANDARD'::character varying, 'TRANSFER'::character varying])::text[]))),
    CONSTRAINT transactions_type_check CHECK (((type)::text = ANY ((ARRAY['INCOME'::character varying, 'EXPENSE'::character varying])::text[])))
);


ALTER TABLE public.transactions OWNER TO finance;

--
-- Name: accounts accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: finance
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_pkey PRIMARY KEY (id);


--
-- Name: event_publication event_publication_pkey; Type: CONSTRAINT; Schema: public; Owner: finance
--

ALTER TABLE ONLY public.event_publication
    ADD CONSTRAINT event_publication_pkey PRIMARY KEY (id);


--
-- Name: investment_snapshots investment_snapshots_pkey; Type: CONSTRAINT; Schema: public; Owner: finance
--

ALTER TABLE ONLY public.investment_snapshots
    ADD CONSTRAINT investment_snapshots_pkey PRIMARY KEY (id);


--
-- Name: transactions transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: finance
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_pkey PRIMARY KEY (id);


--
-- Name: investment_snapshots uk_investment_snapshot_month_currency; Type: CONSTRAINT; Schema: public; Owner: finance
--

ALTER TABLE ONLY public.investment_snapshots
    ADD CONSTRAINT uk_investment_snapshot_month_currency UNIQUE (month, currency);


--
-- Name: accounts uk_qtv290mh55xhggmpwosf5ag0v; Type: CONSTRAINT; Schema: public; Owner: finance
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT uk_qtv290mh55xhggmpwosf5ag0v UNIQUE (name);


--
-- PostgreSQL database dump complete
--

\unrestrict ELIw7Oz6dl83T1aB4e1VYcJ8niG4SAuMtYPkSSkd4Wuef4hFbZHYQr6hDGMlYA7

