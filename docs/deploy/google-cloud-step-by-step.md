# Guida completa (0 → 100): Deploy su Google Cloud per principianti

Questa guida è pensata per chi parte da zero e vuole pubblicare backend + frontend su Google Cloud, usando Cloud Run e GitHub Actions.

## A. Prima di iniziare (concetti base)

### Cosa userai
- **Google Cloud Project**: contenitore logico di tutte le risorse cloud.
- **Cloud Run**: esegue container (backend e frontend) senza gestire server.
- **Artifact Registry**: repository delle immagini Docker.
- **Cloud SQL**: database PostgreSQL gestito.
- **Secret Manager**: gestione sicura delle variabili sensibili.
- **GitHub Actions + OIDC**: deploy automatico senza salvare chiavi JSON statiche.

### Dove eseguire i comandi
Quasi tutti i comandi `gcloud ...` li lanci **dal tuo terminale locale** (oppure da Cloud Shell).

---

## B. Prerequisiti locali (da installare/configurare)

1. Account Google Cloud attivo.
2. Billing abilitato sul progetto che userai.
3. Repo GitHub con permessi admin.
4. Google Cloud CLI (`gcloud`) installata.

Verifica rapida in terminale:

```bash
gcloud version
```

Login:

```bash
gcloud auth login
gcloud auth application-default login
```

---

## C. Scegli i valori una volta sola (copia e incolla)

Sostituisci con i tuoi valori e riusa questi nomi in tutta la guida:

```bash
export PROJECT_ID="family-finance-manager-2026"
export PROJECT_NAME="family-finance-manager"
export REGION="europe-west1"
export REPOSITORY="family-finance"
export SQL_INSTANCE="finance-prod"
export SQL_DB="finance"
export SQL_USER="finance_app"
export BACKEND_SERVICE="family-finance-backend"
export FRONTEND_SERVICE="family-finance-frontend"
export GITHUB_OWNER="<GITHUB_OWNER>"
export GITHUB_REPO="family-finance-manager"
```

> Nota: `PROJECT_ID` deve essere globale e unico su Google Cloud.

---

## D. Crea (o seleziona) progetto Google Cloud

Se il progetto non esiste:

```bash
gcloud projects create "$PROJECT_ID" --name="$PROJECT_NAME"
```

Seleziona il progetto corrente:

```bash
gcloud config set project "$PROJECT_ID"
```

Verifica:

```bash
gcloud config get-value project
```

---

## E. Abilita API necessarie

```bash
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com \
  sqladmin.googleapis.com \
  iamcredentials.googleapis.com \
  cloudresourcemanager.googleapis.com
```

---

## F. Crea Artifact Registry

```bash
gcloud artifacts repositories create "$REPOSITORY" \
  --repository-format=docker \
  --location="$REGION" \
  --description="Docker images for family-finance-manager"
```

Verifica:

```bash
gcloud artifacts repositories list --location="$REGION"
```

---

## G. Crea Cloud SQL PostgreSQL

### 1) Istanza

```bash
gcloud sql instances create "$SQL_INSTANCE" \
  --database-version=POSTGRES_16 \
  --cpu=1 \
  --memory=3840MiB \
  --region="$REGION"
```

### 2) Database e utente applicativo

```bash
gcloud sql databases create "$SQL_DB" --instance="$SQL_INSTANCE"

read -s DB_PASSWORD
gcloud sql users create "$SQL_USER" --instance="$SQL_INSTANCE" --password="$DB_PASSWORD"
unset DB_PASSWORD
```

### 3) Recupera IP pubblico (setup iniziale semplice)

```bash
export CLOUD_SQL_IP="$(gcloud sql instances describe "$SQL_INSTANCE" --format='value(ipAddresses[0].ipAddress)')"
echo "$CLOUD_SQL_IP"
```

JDBC URL risultante:

```text
jdbc:postgresql://<CLOUD_SQL_IP>:5432/finance
```

---

## H. Crea Service Account per deploy GitHub Actions

```bash
gcloud iam service-accounts create github-deployer \
  --display-name="GitHub Deployer"
```

Assegna ruoli minimi:

```bash
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member="serviceAccount:github-deployer@$PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/run.admin"

gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member="serviceAccount:github-deployer@$PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/artifactregistry.writer"

gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member="serviceAccount:github-deployer@$PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"

gcloud projects add-iam-policy-binding "$PROJECT_ID" \
  --member="serviceAccount:github-deployer@$PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/iam.serviceAccountUser"
```

---

## I. Configura OIDC (Workload Identity Federation) con GitHub

### 1) Recupera project number

```bash
export PROJECT_NUMBER="$(gcloud projects describe "$PROJECT_ID" --format='value(projectNumber)')"
echo "$PROJECT_NUMBER"
```

### 2) Crea pool e provider

```bash
gcloud iam workload-identity-pools create github-pool \
  --location="global" \
  --display-name="GitHub Pool"

gcloud iam workload-identity-pools providers create-oidc github-provider \
  --location="global" \
  --workload-identity-pool="github-pool" \
  --display-name="GitHub Provider" \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository"
```

### 3) Permetti al tuo repository di impersonare il service account

```bash
gcloud iam service-accounts add-iam-policy-binding \
  "github-deployer@$PROJECT_ID.iam.gserviceaccount.com" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/projects/$PROJECT_NUMBER/locations/global/workloadIdentityPools/github-pool/attribute.repository/$GITHUB_OWNER/$GITHUB_REPO"
```

### 4) Salva il provider resource name (servirà in GitHub Secrets)

```bash
export WIF_PROVIDER="$(gcloud iam workload-identity-pools providers describe github-provider \
  --location=global \
  --workload-identity-pool=github-pool \
  --format='value(name)')"

echo "$WIF_PROVIDER"
```

---

## J. Crea i secrets applicativi su Secret Manager

### Funzione utile (crea o aggiorna)

```bash
upsert_secret () {
  local NAME="$1"
  local VALUE="$2"
  if gcloud secrets describe "$NAME" >/dev/null 2>&1; then
    echo -n "$VALUE" | gcloud secrets versions add "$NAME" --data-file=-
  else
    echo -n "$VALUE" | gcloud secrets create "$NAME" --data-file=-
  fi
}
```

### Secret minimi consigliati
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

Esempio:

```bash
upsert_secret SPRING_DATASOURCE_URL "jdbc:postgresql://$CLOUD_SQL_IP:5432/$SQL_DB"
upsert_secret SPRING_DATASOURCE_USERNAME "$SQL_USER"
upsert_secret SPRING_DATASOURCE_PASSWORD "<DB_PASSWORD>"
upsert_secret JWT_ISSUER "family-finance-prod"
upsert_secret JWT_SECRET "<VERY_STRONG_SECRET>"
```

---

## K. Configura repository GitHub (Settings → Secrets and variables → Actions)

### Variables
- `GCP_PROJECT_ID=$PROJECT_ID`
- `GCP_REGION=$REGION`
- `GCP_ARTIFACT_REPOSITORY=$REPOSITORY`
- `GCP_BACKEND_SERVICE=$BACKEND_SERVICE`
- `GCP_FRONTEND_SERVICE=$FRONTEND_SERVICE`
- `NEXT_PUBLIC_API_BASE=https://<BACKEND_URL>`

### Secrets
- `GCP_WORKLOAD_IDENTITY_PROVIDER=$WIF_PROVIDER`
- `GCP_SERVICE_ACCOUNT_EMAIL=github-deployer@$PROJECT_ID.iam.gserviceaccount.com`

---

## L. Primo deploy (ordine corretto)

1. Deploy backend (workflow `Deploy Backend to Cloud Run`).
2. Recupera URL backend pubblicato.
3. Aggiorna `NEXT_PUBLIC_API_BASE` in GitHub Variables.
4. Deploy frontend (workflow `Deploy Frontend to Cloud Run`).

---

## M. Verifica post-deploy (checklist)

1. Backend risponde (`/api/auth/...` o endpoint health se presente).
2. Frontend si apre senza errori JS in console browser.
3. Login/registrazione funzionanti.
4. Creazione transazione OK.
5. Dashboard carica dati.
6. Forgot/reset password completo.

---

## N. Troubleshooting rapido

### Errore `PERMISSION_DENIED` in GitHub Actions
- OIDC configurato male o ruolo IAM mancante.
- Ricontrolla `roles/iam.workloadIdentityUser` e `attribute.repository`.

### Deploy Cloud Run fallisce per immagine
- Verifica `GCP_ARTIFACT_REPOSITORY`, `REGION` e ruolo `artifactregistry.writer`.

### Backend non connette a Cloud SQL
- Verifica `SPRING_DATASOURCE_*` in Secret Manager.
- Controlla IP/utente/password DB.

### Frontend non chiama backend
- `NEXT_PUBLIC_API_BASE` errata.
- CORS non allineato su backend.

---

## O. Hardening consigliato (fase 2)

- Dominio custom + certificato gestito.
- Cloud SQL private IP + VPC connector.
- Cloud Run `min-instances=1` per ridurre cold start.
- Alerting su error rate/latency.
- Ambiente separato `staging`.

---

## P. Checklist finale (go-live)

- [ ] progetto creato e billing attivo
- [ ] API abilitate
- [ ] Artifact Registry pronto
- [ ] Cloud SQL operativo
- [ ] Secret Manager popolato
- [ ] OIDC GitHub configurato
- [ ] backend deployato
- [ ] frontend deployato
- [ ] smoke test funzionale applicativo completato
