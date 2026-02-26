# Command Cheatsheet

Comandi rapidi per l’ambiente locale del backend.

## Docker (PostgreSQL locale)
Dalla root del progetto:

- Avvia PostgreSQL:
  ```bash
  docker compose -f backend/docker/postgres.yml up -d
  ```
- Ferma PostgreSQL:
  ```bash
  docker compose -f backend/docker/postgres.yml down
  ```
- Riavvia PostgreSQL:
  ```bash
  docker compose -f backend/docker/postgres.yml down && docker compose -f backend/docker/postgres.yml up -d
  ```
- Stato container:
  ```bash
  docker compose -f backend/docker/postgres.yml ps
  ```
- Log PostgreSQL:
  ```bash
  docker compose -f backend/docker/postgres.yml logs -f postgres
  ```

## Pod / Container list
Se stai usando Kubernetes:

- Lista pod (namespace corrente):
  ```bash
  kubectl get pods
  ```
- Lista pod in tutti i namespace:
  ```bash
  kubectl get pods -A
  ```

Se stai usando solo Docker Compose (senza Kubernetes):

- Lista container attivi:
  ```bash
  docker ps
  ```
- Lista tutti i container:
  ```bash
  docker ps -a
  ```

## PostgreSQL: vedere database e tabelle
Connessione al container Postgres (`finance-postgres`):

- Entra in `psql`:
  ```bash
  docker exec -it finance-postgres psql -U finance -d finance
  ```

Una volta dentro `psql`:

- Elenco database:
  ```sql
  \l
  ```
- Elenco schemi:
  ```sql
  \dn
  ```
- Elenco tabelle nello schema corrente:
  ```sql
  \dt
  ```
- Elenco tabelle di tutti gli schemi:
  ```sql
  \dt *.*
  ```
- Struttura di una tabella:
  ```sql
  \d nome_tabella
  ```
- Esci da `psql`:
  ```sql
  \q
  ```

## Query SQL rapide
- Prime 20 righe di una tabella:
  ```sql
  SELECT * FROM nome_tabella LIMIT 20;
  ```
- Conteggio record:
  ```sql
  SELECT COUNT(*) FROM nome_tabella;
  ```

## Spring Boot (backend)
Dalla cartella `backend`:

- Avvio in foreground (sviluppo):
  ```bash
  mvn spring-boot:run
  ```
- Stop in foreground:
  ```bash
  Ctrl + C
  ```
- Build jar:
  ```bash
  mvn clean package
  ```
- Avvio jar:
  ```bash
  java -jar target/finance-app-*.jar
  ```

### Esecuzione in background
- Avvia in background + log su file:
  ```bash
  mkdir -p .run logs && nohup mvn spring-boot:run > logs/backend.log 2>&1 & echo $! > .run/backend.pid
  ```
- Stop tramite PID salvato:
  ```bash
  kill "$(cat .run/backend.pid)" && rm -f .run/backend.pid
  ```
- Vedi log applicazione:
  ```bash
  tail -f logs/backend.log
  ```

### Stato e “statistiche” rapide
- Verifica se risponde su porta 8080:
  ```bash
  curl -i http://localhost:8080
  ```
- Trova PID del processo sulla porta 8080 (macOS):
  ```bash
  lsof -nP -iTCP:8080 -sTCP:LISTEN
  ```
- CPU / RAM del processo Java (sostituisci `<PID>`):
  ```bash
  ps -p <PID> -o pid,%cpu,%mem,etime,command
  ```

### Health e metriche (se Actuator è abilitato)
- Health:
  ```bash
  curl http://localhost:8080/actuator/health
  ```
- Info:
  ```bash
  curl http://localhost:8080/actuator/info
  ```
- Elenco metriche:
  ```bash
  curl http://localhost:8080/actuator/metrics
  ```

## Mini-guida `dev.sh`
Script: `backend/scripts/dev.sh`

Dalla cartella `backend`:

- Help rapido:
  ```bash
  ./scripts/dev.sh
  ```
- Avvio completo (Postgres + Spring Boot):
  ```bash
  ./scripts/dev.sh up
  ```
- Stato servizi:
  ```bash
  ./scripts/dev.sh status
  ```
- Log app:
  ```bash
  ./scripts/dev.sh logs app
  ```
- Log database:
  ```bash
  ./scripts/dev.sh logs db
  ```
- Stop completo:
  ```bash
  ./scripts/dev.sh down
  ```

Nota: i comandi Docker richiedono Docker Desktop/daemon attivo.

## Mini-guida `scripts/dev.sh` (root progetto)
Script: `scripts/dev.sh`

Dalla root del progetto:

- Avvio standard (profilo backend `dev`):
  ```bash
  ./scripts/dev.sh up
  ```
- Avvio con backend in profilo `prod`:
  ```bash
  ./scripts/dev.sh up-prod
  ```
- Avvio solo backend con profilo `prod`:
  ```bash
  ./scripts/dev.sh backend-start-prod
  ```

Variabili richieste per i comandi `prod`:

- `JWT_ISSUER`
- `JWT_SECRET`
- `JWT_ACCESS_TOKEN_MINUTES`
- `PASSWORD_RESET_TOKEN_MINUTES`
- `PASSWORD_RESET_URL`
- `PASSWORD_RESET_FROM_EMAIL`

Esempio rapido:

```bash
JWT_ISSUER=financeapp \
JWT_SECRET=change_me_with_strong_secret \
JWT_ACCESS_TOKEN_MINUTES=60 \
PASSWORD_RESET_TOKEN_MINUTES=30 \
PASSWORD_RESET_URL=https://app.example.com/reset-password \
PASSWORD_RESET_FROM_EMAIL=no-reply@example.com \
./scripts/dev.sh up-prod
```

## Mini-guida `api-curl-tests.sh`
Script: `backend/scripts/api-curl-tests.sh`

Dalla cartella `backend`:

- Esecuzione standard (lascia dati nel DB, default):
  ```bash
  ./scripts/api-curl-tests.sh
  ```
- Esecuzione con cleanup finale opzionale:
  ```bash
  CLEANUP=true ./scripts/api-curl-tests.sh
  ```
- Esecuzione con host custom:
  ```bash
  BASE_URL=http://localhost:8081 ./scripts/api-curl-tests.sh
  ```
- Esecuzione con mese/anno custom:
  ```bash
  MONTH=2026-02 YEAR=2026 ./scripts/api-curl-tests.sh
  ```

Variabili supportate:
- `BASE_URL` (default: `http://localhost:8080`)
- `MONTH` formato `YYYY-MM` (default: mese corrente)
- `YEAR` formato `YYYY` (default: anno corrente)
- `ACCOUNT_NAME_FROM` (default: `Fineco-<timestamp>`)
- `ACCOUNT_NAME_TO` (default: `N26-<timestamp>`)
- `TRANSFER_AMOUNT` (default: `10.00`)
- `CLEANUP` (default: `false`; se `true` elimina le transazioni create nel run)

Nota: lo script crea account, transazione e transfer di test; di default mantiene i dati per ispezione.

## Password Reset: configurazione e invio email

### Dove è implementato
- Generazione token + link + invio:
  - `backend/src/main/java/com/lucio/financeapp/users/application/RequestPasswordResetUseCase.java`
- Invio email (notifier):
  - `backend/src/main/java/com/lucio/financeapp/users/infrastructure/notifications/EmailPasswordResetNotifier.java`
- Proprietà:
  - `backend/src/main/java/com/lucio/financeapp/users/infrastructure/config/PasswordResetProperties.java`

### Proprietà applicative
Configurate con prefisso `security.password-reset`:

- `token-minutes`
- `reset-url`
- `from-email`

File usati:

- `backend/src/main/resources/application.yml` (profilo default: `dev`)
- `backend/src/main/resources/application-dev.yml`
- `backend/src/main/resources/application-prod.yml`

In produzione i valori sono richiesti via env:

- `JWT_ISSUER`
- `JWT_SECRET`
- `JWT_ACCESS_TOKEN_MINUTES`
- `PASSWORD_RESET_TOKEN_MINUTES`
- `PASSWORD_RESET_URL`
- `PASSWORD_RESET_FROM_EMAIL`

### SMTP reale (invio email vero)
È presente `spring-boot-starter-mail`, ma devi configurare `spring.mail.*`.

Esempio (da mettere in `application-prod.yml` o variabili ambiente):

```yaml
spring:
  mail:
    host: smtp.tuo-provider.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

Env consigliate:

- `MAIL_USERNAME`
- `MAIL_PASSWORD`

### Comportamento fallback
Se `JavaMailSender` non è disponibile, il sistema **non fallisce**: logga il link di reset nei log backend.
Utile in locale durante sviluppo/test.
