# Guida passo-passo: Deploy su Google Cloud

Questa guida pubblica backend e frontend su Cloud Run con pipeline GitHub Actions.

## 0) Prerequisiti
- Account Google Cloud attivo.
- Repo GitHub con permessi admin.
- `gcloud` installato e autenticato localmente.
- Billing abilitato sul progetto GCP.

## 1) Crea/Seleziona progetto GCP

```bash
gcloud projects create <PROJECT_ID> --name="family-finance-manager"
gcloud config set project <PROJECT_ID>
```

Se il progetto esiste già:

```bash
gcloud config set project <PROJECT_ID>
```

## 2) Abilita API necessarie

```bash
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com \
  sqladmin.googleapis.com \
  iamcredentials.googleapis.com \
  cloudresourcemanager.googleapis.com
```

## 3) Crea Artifact Registry

```bash
gcloud artifacts repositories create family-finance \
  --repository-format=docker \
  --location=<REGION> \
  --description="Docker images for family-finance-manager"
```

## 4) Crea Cloud SQL PostgreSQL

```bash
gcloud sql instances create finance-prod \
  --database-version=POSTGRES_16 \
  --cpu=1 \
  --memory=3840MiB \
  --region=<REGION>

gcloud sql databases create finance --instance=finance-prod
read -s DB_PASSWORD
gcloud sql users create finance_app --instance=finance-prod --password="$DB_PASSWORD"
unset DB_PASSWORD
```

Recupera IP pubblico istanza (setup iniziale semplice):

```bash
gcloud sql instances describe finance-prod --format="value(ipAddresses[0].ipAddress)"
```

Costruisci URL JDBC:

```text
jdbc:postgresql://<CLOUD_SQL_PUBLIC_IP>:5432/finance
```

## 5) Crea Service Account per GitHub Actions

```bash
gcloud iam service-accounts create github-deployer \
  --display-name="GitHub Deployer"
```

Assegna ruoli minimi:

```bash
gcloud projects add-iam-policy-binding <PROJECT_ID> \
  --member="serviceAccount:github-deployer@<PROJECT_ID>.iam.gserviceaccount.com" \
  --role="roles/run.admin"

gcloud projects add-iam-policy-binding <PROJECT_ID> \
  --member="serviceAccount:github-deployer@<PROJECT_ID>.iam.gserviceaccount.com" \
  --role="roles/artifactregistry.writer"

gcloud projects add-iam-policy-binding <PROJECT_ID> \
  --member="serviceAccount:github-deployer@<PROJECT_ID>.iam.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"

gcloud projects add-iam-policy-binding <PROJECT_ID> \
  --member="serviceAccount:github-deployer@<PROJECT_ID>.iam.gserviceaccount.com" \
  --role="roles/iam.serviceAccountUser"
```

## 6) Configura Workload Identity Federation (GitHub OIDC)

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

Permetti al repository di impersonare il service account:

```bash
gcloud iam service-accounts add-iam-policy-binding \
  github-deployer@<PROJECT_ID>.iam.gserviceaccount.com \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/projects/<PROJECT_NUMBER>/locations/global/workloadIdentityPools/github-pool/attribute.repository/<GITHUB_OWNER>/<GITHUB_REPO>"
```

Ottieni provider resource name:

```bash
gcloud iam workload-identity-pools providers describe github-provider \
  --location=global \
  --workload-identity-pool=github-pool \
  --format="value(name)"
```

## 7) Crea secrets in Secret Manager

Esempio (ripeti per ogni secret):

```bash
echo -n "<VALUE>" | gcloud secrets create JWT_SECRET --data-file=-
```

Se esiste già:

```bash
echo -n "<NEW_VALUE>" | gcloud secrets versions add JWT_SECRET --data-file=-
```

Elenco completo consigliato:
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

## 8) Configura GitHub repo settings

### GitHub Variables
- `GCP_PROJECT_ID=<PROJECT_ID>`
- `GCP_REGION=<REGION>` (es. `europe-west1`)
- `GCP_ARTIFACT_REPOSITORY=family-finance`
- `GCP_BACKEND_SERVICE=family-finance-backend`
- `GCP_FRONTEND_SERVICE=family-finance-frontend`
- `NEXT_PUBLIC_API_BASE=https://<BACKEND_DOMAIN_OR_URL>`

### GitHub Secrets
- `GCP_WORKLOAD_IDENTITY_PROVIDER=<provider_resource_name>`
- `GCP_SERVICE_ACCOUNT_EMAIL=github-deployer@<PROJECT_ID>.iam.gserviceaccount.com`

## 9) Deploy backend (prima volta)

- Vai su GitHub Actions.
- Esegui workflow `Deploy Backend to Cloud Run` (`Run workflow`).
- Al termine verifica URL backend e endpoint health (se disponibile).

## 10) Deploy frontend

- Imposta `NEXT_PUBLIC_API_BASE` all’URL pubblico backend.
- Esegui workflow `Deploy Frontend to Cloud Run`.
- Verifica apertura UI e chiamate API.

## 11) Configurazione email: cosa usare

Google Cloud non offre un SMTP transazionale nativo dedicato.

Opzioni consigliate:
1. **SendGrid** (semplice e diffuso)
2. **Mailgun / Brevo / Postmark**
3. **Google Workspace SMTP relay** (valido ma con limiti più rigidi)

Imposta nel provider email:
- Dominio mittente verificato
- SPF, DKIM, DMARC
- Credenziali SMTP usate nei secret `SPRING_MAIL_*`

## 12) Smoke test post-deploy

1. Registrazione/login utente
2. Creazione transazione
3. Dashboard caricata correttamente
4. `Forgot password` invia email
5. `Reset password` completa il flusso

## 13) Hardening consigliato (fase 2)

- Dominio custom + certificato gestito.
- Cloud SQL con private IP + VPC connector.
- Cloud Run min instances (1) per ridurre cold start.
- Alerting su error rate/latency.
- Ambiente `staging` separato da `prod`.
