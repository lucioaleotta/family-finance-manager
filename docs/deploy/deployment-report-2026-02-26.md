# Deployment Report - 2026-02-26

## Scope
Impostazione base di deployment su Google Cloud con CI/CD da GitHub Actions per backend e frontend.

## Cosa è stato implementato

### 1) Containerizzazione
- `backend/Dockerfile`
  - Build Maven multi-stage
  - Runtime Java 21 JRE
  - Avvio con profilo `prod`
  - Porta dinamica compatibile Cloud Run (`PORT`)
- `frontend/Dockerfile`
  - Build Next.js multi-stage
  - Supporto build arg `NEXT_PUBLIC_API_BASE`
  - Runtime ottimizzato con output standalone
  - Porta 8080 per Cloud Run

### 2) CI/CD con GitHub Actions
- `.github/workflows/deploy-backend-cloud-run.yml`
  - Trigger su push `main` (path backend) e manuale
  - Auth GCP via Workload Identity Federation
  - Build + push image su Artifact Registry
  - Deploy su Cloud Run con `--set-secrets` per variabili sensibili
- `.github/workflows/deploy-frontend-cloud-run.yml`
  - Trigger su push `main` (path frontend) e manuale
  - Auth GCP via Workload Identity Federation
  - Build + push image frontend su Artifact Registry
  - Deploy su Cloud Run

### 3) Allineamento frontend per runtime container
- `frontend/next.config.ts`
  - Abilitato `output: "standalone"` per immagine più leggera e start server diretto.

### 4) Documentazione operativa
- `docs/deploy/google-cloud-step-by-step.md`
  - Procedura completa bootstrap GCP
  - Mappa secrets da configurare
  - Scelta/integrazione mail provider
  - Esecuzione primo deploy e verifica

## Configurazione richiesta (non committata)
Per eseguire i workflow servono questi GitHub Variables/Secrets:

### GitHub Variables
- `GCP_PROJECT_ID`
- `GCP_REGION`
- `GCP_ARTIFACT_REPOSITORY`
- `GCP_BACKEND_SERVICE`
- `GCP_FRONTEND_SERVICE`
- `NEXT_PUBLIC_API_BASE`

### GitHub Secrets
- `GCP_WORKLOAD_IDENTITY_PROVIDER`
- `GCP_SERVICE_ACCOUNT_EMAIL`

### Secret Manager (GCP)
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_ISSUER`
- `JWT_SECRET`
- `JWT_ACCESS_TOKEN_MINUTES`
- `PASSWORD_RESET_TOKEN_MINUTES`
- `PASSWORD_RESET_URL`
- `PASSWORD_RESET_FROM_EMAIL`
- `SPRING_MAIL_HOST`
- `SPRING_MAIL_PORT`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH`
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE`

## Note importanti
- Google Cloud non offre un SMTP transazionale nativo per applicazioni come alternativa diretta a SendGrid/Mailgun.
- Per produzione è consigliato un provider email dedicato con SPF/DKIM/DMARC configurati sul dominio.
- La pipeline creata è pronta, ma resta da eseguire il bootstrap iniziale di progetto/identity/secrets su GCP.

## Prossimi step suggeriti
1. Eseguire bootstrap GCP e WIF (seguendo la guida).
2. Creare i secret in Secret Manager.
3. Configurare Variables/Secrets su GitHub.
4. Lanciare workflow backend, poi frontend.
5. Validare smoke test su endpoint API e flow reset password.
