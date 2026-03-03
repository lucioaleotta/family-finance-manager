# Runbook Backup / Restore PostgreSQL (Locale)

## Script disponibili
- Export backup: `backend/scripts/db-export.sh`
- Import restore: `backend/scripts/db-import.sh`
- Smoke test backup/restore con cleanup automatico: `backend/scripts/db-smoke-test.sh`

Entrambi supportano:
- connessione via container Docker (`finance-postgres`) come default;
- override parametri DB via environment variables.

## Variabili supportate
- `DB_HOST` (default: `localhost`)
- `DB_PORT` (default: `5432`)
- `DB_NAME` (default: `finance`)
- `DB_USER` (default: `finance`)
- `DB_PASSWORD` (default: `finance`)
- `DB_CONTAINER` (default: `finance-postgres`)

## Export (backup)
Da root progetto:

```bash
./backend/scripts/db-export.sh
```

Output default:
- directory: `backend/backups/`
- file: `finance_<timestamp>.dump`

Export con file custom:

```bash
./backend/scripts/db-export.sh ./backend/backups/pre-release.dump
```

## Import (restore)
### Restore su DB esistente (drop/recreate schema public)

```bash
./backend/scripts/db-import.sh ./backend/backups/finance_20260303_120000.dump
```

### Safety check
Lo script richiede conferma interattiva prima del restore (a meno di `FORCE=true`).

Esecuzione non interattiva:

```bash
FORCE=true ./backend/scripts/db-import.sh ./backend/backups/finance_20260303_120000.dump
```

## Note operative
- Eseguire restore con backend fermo quando possibile.
- Verificare sempre il risultato con smoke test API.
- Conservare backup versionati prima di deploy/migrazioni critiche.

## Verifica rapida post-restore
- count tabelle principali;
- login utente di test;
- caricamento dashboard e endpoint principali.

## Smoke test con cleanup automatico
Per verificare end-to-end backup/restore senza lasciare database temporanei:

```bash
cd backend
./scripts/dev.sh db-smoke-test
```

Comportamento:
- export del DB sorgente (`finance` di default);
- restore su DB temporaneo `finance_smoke`;
- verifica numero tabelle ripristinate;
- cleanup automatico finale: `DROP DATABASE IF EXISTS finance_smoke`.

Override opzionali:
- `SOURCE_DB_NAME` (default `finance`)
- `SMOKE_DB_NAME` (default `finance_smoke`)
- `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_CONTAINER`
