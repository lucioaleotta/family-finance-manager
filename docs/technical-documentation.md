# Documentazione Tecnica - Family Finance Manager

## 1. Requisiti tecnici

### Backend
- Java 21+
- Maven 3.8+
- PostgreSQL 16 (locale via Docker)

### Frontend
- Node.js 18+
- npm

### Tooling
- Docker + Docker Compose (v2 preferito, v1 fallback)

## 2. Struttura repository
- `backend/`: API Spring Boot, dominio e persistenza.
- `frontend/`: web app Next.js.
- `docs/`: documentazione tecnica e deploy.
- `scripts/`: script orchestrazione locale a livello root.

## 3. Backend: avvio e lifecycle

### Avvio locale rapido
Da root progetto:

```bash
./scripts/dev.sh up
```

Da `backend/`:

```bash
./scripts/dev.sh up
```

### Build backend

```bash
cd backend
mvn clean compile -DskipTests
```

## 4. Configurazione applicativa
File:
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-dev.yml`
- `backend/src/main/resources/application-prod.yml`

Valori sensibili e runtime config in produzione tramite env vars/secret manager.

## 5. Database

### Migrazioni
Path:
- `backend/src/main/resources/db/migration`

Le migrazioni Flyway vengono applicate all'avvio backend.

### Backup/restore
Script ufficiali:
- export backup: `backend/scripts/db-export.sh`
- import restore: `backend/scripts/db-import.sh`
- smoke test backup/restore con cleanup automatico: `backend/scripts/db-smoke-test.sh`

Comandi disponibili anche da root progetto:

```bash
./scripts/dev.sh db-export
./scripts/dev.sh db-import ./backend/backups/<file>.dump
./scripts/dev.sh db-smoke-test
```

Lo smoke test esegue restore su database temporaneo `finance_smoke` e lo rimuove automaticamente a fine esecuzione.

Runbook operativo:
- `backend/docs/db-backup-restore.md`

## 6. Logging
Standard e convenzioni:
- `backend/docs/logging-standard.md`

Implementazione:
- configurazione Logback centralizzata (`logback-spring.xml`)
- correlation id per request HTTP (`X-Correlation-Id`)
- policy livelli consistente tra ambienti.

## 7. Sicurezza
- JWT stateless per autenticazione API.
- Password reset con token persistito e scadenza.
- Configurazione secret in `prod` da variabili ambiente.

## 8. Flussi principali
- login/registrazione/reset password;
- CRUD conti e transazioni;
- snapshot asset/liquidità;
- dashboard reporting mensile/annuale.

## 9. Operatività consigliata
- usare sempre script di progetto per uniformità;
- non loggare mai dati sensibili (token, password, secret);
- fare backup prima di deploy con migrazioni critiche;
- verificare restore periodico (backup non testato = backup non affidabile).

## 10. Troubleshooting veloce
- backend non parte: verificare DB up + env vars richieste;
- auth fallisce: controllare issuer/secret JWT;
- frontend non vede API: verificare `NEXT_PUBLIC_API_BASE`;
- restore fallito: verificare compatibilità dump + versione Postgres.
