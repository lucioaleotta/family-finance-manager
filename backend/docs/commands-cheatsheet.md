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

## Mini-guida `api-curl-tests.sh`
Script: `backend/scripts/api-curl-tests.sh`

Dalla cartella `backend`:

- Esecuzione standard:
  ```bash
  ./scripts/api-curl-tests.sh
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

Nota: lo script crea account e transazione di test, poi elimina la transazione alla fine.
