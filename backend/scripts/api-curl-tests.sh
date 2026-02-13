#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
MONTH="${MONTH:-$(date +%Y-%m)}"
YEAR="${YEAR:-$(date +%Y)}"
ACCOUNT_NAME="${ACCOUNT_NAME:-Fineco-$(date +%s)}"

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Errore: comando richiesto non trovato -> $1"
    exit 1
  }
}

extract_uuid() {
  tr -d '"[:space:]'
}

is_uuid() {
  [[ "$1" =~ ^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$ ]]
}

split_body_status() {
  local response="$1"
  HTTP_STATUS="$(printf '%s' "$response" | tail -n1)"
  HTTP_BODY="$(printf '%s' "$response" | sed '$d')"
}

require_http_status() {
  local expected="$1"
  if [[ "$HTTP_STATUS" != "$expected" ]]; then
    echo "Errore HTTP: atteso $expected, ottenuto $HTTP_STATUS"
    if [[ -n "${HTTP_BODY:-}" ]]; then
      echo "Body risposta:"
      printf '%s\n' "$HTTP_BODY" | pretty
    fi
    exit 1
  fi
}

pretty() {
  if command -v python3 >/dev/null 2>&1; then
    python3 -m json.tool 2>/dev/null || cat
  else
    cat
  fi
}

require_cmd curl

echo "== API smoke test =="
echo "BASE_URL=$BASE_URL"
echo "MONTH=$MONTH"
echo "YEAR=$YEAR"
echo "ACCOUNT_NAME=$ACCOUNT_NAME"
echo

echo "[1] Create account"
ACCOUNT_CREATE_RESPONSE=$(curl -sS -X POST "$BASE_URL/api/accounts" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"$ACCOUNT_NAME\",\"type\":\"CHECKING\",\"currency\":\"EUR\"}" \
  -w "\n%{http_code}")

split_body_status "$ACCOUNT_CREATE_RESPONSE"
require_http_status "201"

ACCOUNT_ID="$(printf '%s' "$HTTP_BODY" | extract_uuid)"
if ! is_uuid "$ACCOUNT_ID"; then
  echo "Errore: accountId non valido ricevuto: $ACCOUNT_ID"
  exit 1
fi

echo "ACCOUNT_ID=$ACCOUNT_ID"
echo

echo "[2] List accounts"
curl -sS "$BASE_URL/api/accounts" | pretty
echo

echo "[3] Create transaction"
TRANSACTION_CREATE_RESPONSE=$(curl -sS -X POST "$BASE_URL/api/transactions" \
  -H "Content-Type: application/json" \
  -d "{\"accountId\":\"$ACCOUNT_ID\",\"amount\":42.50,\"date\":\"${MONTH}-10\",\"type\":\"EXPENSE\",\"category\":\"GROCERIES\",\"description\":\"Spesa settimanale\"}" \
  -w "\n%{http_code}")

split_body_status "$TRANSACTION_CREATE_RESPONSE"
require_http_status "201"

TRANSACTION_ID="$(printf '%s' "$HTTP_BODY" | extract_uuid)"
if ! is_uuid "$TRANSACTION_ID"; then
  echo "Errore: transactionId non valido ricevuto: $TRANSACTION_ID"
  exit 1
fi

echo "TRANSACTION_ID=$TRANSACTION_ID"
echo

echo "[4] List transactions by month"
curl -sS "$BASE_URL/api/transactions?month=$MONTH" | pretty
echo

echo "[5] Update transaction"
UPDATE_STATUS=$(curl -sS -X PUT "$BASE_URL/api/transactions/$TRANSACTION_ID" \
  -H "Content-Type: application/json" \
  -d "{\"accountId\":\"$ACCOUNT_ID\",\"amount\":45.00,\"date\":\"${MONTH}-10\",\"type\":\"EXPENSE\",\"category\":\"GROCERIES\",\"description\":\"Spesa aggiornata\"}" \
  -o /dev/null -w "%{http_code}")
if [[ "$UPDATE_STATUS" != "204" ]]; then
  echo "Errore update: HTTP $UPDATE_STATUS"
  exit 1
fi
echo "HTTP $UPDATE_STATUS"
echo

echo "[6] Reporting monthly"
curl -sS "$BASE_URL/api/reporting/monthly?month=$MONTH" | pretty
echo

echo "[7] Reporting annual timeline"
curl -sS "$BASE_URL/api/reporting/annual/timeline?year=$YEAR" | pretty
echo

echo "[8] Reporting annual total"
curl -sS "$BASE_URL/api/reporting/annual/total?year=$YEAR" | pretty
echo

echo "[9] Delete transaction"
DELETE_STATUS=$(curl -sS -X DELETE "$BASE_URL/api/transactions/$TRANSACTION_ID" \
  -o /dev/null -w "%{http_code}")
if [[ "$DELETE_STATUS" != "204" ]]; then
  echo "Errore delete: HTTP $DELETE_STATUS"
  exit 1
fi
echo "HTTP $DELETE_STATUS"
echo

echo "[10] Final list transactions by month"
curl -sS "$BASE_URL/api/transactions?month=$MONTH" | pretty
echo

echo "Completato."
