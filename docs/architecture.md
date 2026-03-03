# Architettura Software - Family Finance Manager

## Obiettivo
Family Finance Manager è una piattaforma per la gestione delle finanze familiari con funzionalità di:
- registrazione movimenti e trasferimenti;
- gestione conti e snapshot patrimoniali;
- reporting mensile/annuale;
- autenticazione JWT e reset password.

## Panorama architetturale
Il sistema è composto da:
- **Frontend**: Next.js (App Router) + TypeScript, UI e interazione utente.
- **Backend**: Spring Boot 3 (Java 21), API REST, logica applicativa e sicurezza.
- **Database**: PostgreSQL, schema evoluto tramite Flyway.

Architettura complessiva:
- stile **Modular Monolith** lato backend (Spring Modulith);
- organizzazione interna per moduli funzionali (`transactions`, `assets`, `reporting`, `users`, `shared`);
- approccio **Hexagonal/Clean** nei moduli principali (application/domain/infrastructure).

## Moduli backend (Spring Modulith)
### `transactions`
Responsabilità:
- gestione conti;
- registrazione movimenti e trasferimenti;
- calcolo bilanci per conto.

### `assets`
Responsabilità:
- snapshot liquidità e investimenti;
- panoramica patrimonio;
- riconciliazione net worth.

### `reporting`
Responsabilità:
- aggregazioni mensili/annuali;
- timeline e viste di sintesi.

### `users`
Responsabilità:
- registrazione utente;
- autenticazione JWT;
- flusso forgot/reset password.

### `shared`
Responsabilità:
- concetti comuni (es. `Money`, `Currency`);
- componenti cross-module a basso accoppiamento.

## Strati applicativi backend
Ogni modulo segue tipicamente:
- **domain**: regole di business pure, entità/value object/porte;
- **application**: use case orchestrativi;
- **infrastructure**: adapter tecnici (web, JPA, security, notifier).

Vantaggi:
- separazione responsabilità;
- testabilità alta dei use case;
- facilità di evoluzione per modulo.

## Frontend
Struttura principale:
- route in `frontend/app` (separate aree auth/app);
- componenti riusabili in `frontend/components`;
- client API in `frontend/lib/api.ts`.

Comunicazione:
- frontend chiama API backend su `/api/**`;
- autenticazione bearer token JWT.

## Dati e persistenza
- Database PostgreSQL.
- Migrazioni in `backend/src/main/resources/db/migration`.
- Versionamento schema affidato a Flyway all'avvio backend.

## Sicurezza
- Stateless auth con JWT.
- Filtro JWT in catena Spring Security.
- Endpoint `api/auth/**` pubblici; resto autenticato.
- Password reset token con scadenza e persistenza DB.

## Configurazione ambienti
File principali backend:
- `application.yml` (default + base config)
- `application-dev.yml`
- `application-prod.yml`

Configurazione runtime via environment variables per `prod`.

## Logging e osservabilità
Standard applicativo definito in:
- `backend/docs/logging-standard.md`

Principi:
- log strutturati e coerenti;
- correlation id per tracciamento request;
- livelli (`ERROR/WARN/INFO/DEBUG`) con policy comune.

## Deployment
Deployment target documentato in:
- `docs/deploy/google-cloud-step-by-step.md`

Stack cloud attuale:
- Cloud Run (backend + frontend)
- Artifact Registry
- Cloud SQL PostgreSQL
- Secret Manager
- GitHub Actions (CI/CD)

## Decisioni architetturali principali
1. **Modular monolith** invece di microservizi per ridurre overhead operativo iniziale.
2. **JWT stateless** per semplicità e scalabilità orizzontale.
3. **Flyway** per controllo evoluzione schema e ripetibilità ambienti.
4. **Docker Compose locale** per esperienza sviluppo rapida.

## Evoluzioni consigliate (roadmap)
- introdurre OpenTelemetry (trace distribuito);
- dashboard metriche (es. Prometheus/Grafana o Cloud Monitoring);
- ambiente `staging` separato;
- backup automatizzati e retention policy gestita da scheduler.
