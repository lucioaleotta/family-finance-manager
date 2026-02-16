#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
MONTH="${MONTH:-$(date +%Y-%m)}"
YEAR="${YEAR:-$(date +%Y)}"
TIMESTAMP="$(date +%s)"
ACCOUNT_NAME_FROM="${ACCOUNT_NAME_FROM:-Fineco-${TIMESTAMP}}"
ACCOUNT_NAME_TO="${ACCOUNT_NAME_TO:-N26-${TIMESTAMP}}"
TRANSFER_AMOUNT="${TRANSFER_AMOUNT:-10.00}"
CLEANUP="${CLEANUP:-false}"
ASSERTS="${ASSERTS:-true}"

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

is_true() {
  local value="${1:-false}"
  local normalized
  normalized="$(printf '%s' "$value" | tr '[:upper:]' '[:lower:]')"
  [[ "$normalized" == "true" ]]
}

fetch_json() {
  local url="$1"
  local response
  response=$(curl -sS "$url" -w "\n%{http_code}")
  split_body_status "$response"
  require_http_status "200"
  printf '%s' "$HTTP_BODY"
}

assert_or_fail() {
  local description="$1"
  shift
  if "$@"; then
    echo "✔ Assert: $description"
  else
    echo "✘ Assert fallita: $description"
    exit 1
  fi
}

assert_python_json() {
  local description="$1"
  local json_payload="$2"
  local python_code="$3"

  if ! python3 -c "import json,sys; data=json.loads(sys.argv[1]); ${python_code}" "$json_payload"; then
    echo "✘ Assert fallita: $description"
    return 1
  fi

  echo "✔ Assert: $description"
}

require_cmd curl

if is_true "$ASSERTS"; then
  require_cmd python3
fi

echo "== API smoke test =="
echo "BASE_URL=$BASE_URL"
echo "MONTH=$MONTH"
echo "YEAR=$YEAR"
echo "ACCOUNT_NAME_FROM=$ACCOUNT_NAME_FROM"
echo "ACCOUNT_NAME_TO=$ACCOUNT_NAME_TO"
echo "TRANSFER_AMOUNT=$TRANSFER_AMOUNT"
echo "CLEANUP=$CLEANUP"
echo "ASSERTS=$ASSERTS"
echo

echo "[1] Create source account"
ACCOUNT_CREATE_RESPONSE=$(curl -sS -X POST "$BASE_URL/api/accounts" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"$ACCOUNT_NAME_FROM\",\"type\":\"CHECKING\",\"currency\":\"EUR\"}" \
  -w "\n%{http_code}")

split_body_status "$ACCOUNT_CREATE_RESPONSE"
require_http_status "201"

FROM_ACCOUNT_ID="$(printf '%s' "$HTTP_BODY" | extract_uuid)"
if ! is_uuid "$FROM_ACCOUNT_ID"; then
  echo "Errore: fromAccountId non valido ricevuto: $FROM_ACCOUNT_ID"
  exit 1
fi

echo "FROM_ACCOUNT_ID=$FROM_ACCOUNT_ID"
echo

echo "[2] Create destination account"
ACCOUNT2_CREATE_RESPONSE=$(curl -sS -X POST "$BASE_URL/api/accounts" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"$ACCOUNT_NAME_TO\",\"type\":\"CHECKING\",\"currency\":\"EUR\"}" \
  -w "\n%{http_code}")

split_body_status "$ACCOUNT2_CREATE_RESPONSE"
require_http_status "201"

TO_ACCOUNT_ID="$(printf '%s' "$HTTP_BODY" | extract_uuid)"
if ! is_uuid "$TO_ACCOUNT_ID"; then
  echo "Errore: toAccountId non valido ricevuto: $TO_ACCOUNT_ID"
  exit 1
fi

echo "TO_ACCOUNT_ID=$TO_ACCOUNT_ID"
echo

echo "[3] List accounts"
ACCOUNTS_JSON="$(fetch_json "$BASE_URL/api/accounts")"
printf '%s\n' "$ACCOUNTS_JSON" | pretty
if is_true "$ASSERTS"; then
  assert_python_json "accounts contiene entrambi gli account creati" "$ACCOUNTS_JSON" '
from_id = "'"$FROM_ACCOUNT_ID"'"
to_id = "'"$TO_ACCOUNT_ID"'"
ids = {item.get("id") for item in data}
assert from_id in ids and to_id in ids
'
fi
echo

echo "[4] Create transaction"
TRANSACTION_CREATE_RESPONSE=$(curl -sS -X POST "$BASE_URL/api/transactions" \
  -H "Content-Type: application/json" \
  -d "{\"accountId\":\"$FROM_ACCOUNT_ID\",\"amount\":42.50,\"currency\":\"EUR\",\"date\":\"${MONTH}-10\",\"type\":\"EXPENSE\",\"category\":\"GROCERIES\",\"description\":\"Spesa settimanale\"}" \
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

echo "[5] List transactions by month"
TX_BY_MONTH_JSON="$(fetch_json "$BASE_URL/api/transactions?month=$MONTH")"
printf '%s\n' "$TX_BY_MONTH_JSON" | pretty
if is_true "$ASSERTS"; then
  assert_python_json "transazione standard creata presente" "$TX_BY_MONTH_JSON" '
tx_id = "'"$TRANSACTION_ID"'"
tx = next((item for item in data if item.get("id") == tx_id), None)
assert tx is not None
assert tx.get("kind") == "STANDARD"
assert tx.get("type") == "EXPENSE"
'
fi
echo

echo "[6] Update transaction"
UPDATE_STATUS=$(curl -sS -X PUT "$BASE_URL/api/transactions/$TRANSACTION_ID" \
  -H "Content-Type: application/json" \
  -d "{\"accountId\":\"$FROM_ACCOUNT_ID\",\"amount\":45.00,\"currency\":\"EUR\",\"date\":\"${MONTH}-10\",\"type\":\"EXPENSE\",\"category\":\"GROCERIES\",\"description\":\"Spesa aggiornata\"}" \
  -o /dev/null -w "%{http_code}")
if [[ "$UPDATE_STATUS" != "204" ]]; then
  echo "Errore update: HTTP $UPDATE_STATUS"
  exit 1
fi
echo "HTTP $UPDATE_STATUS"
if is_true "$ASSERTS"; then
  TX_AFTER_UPDATE_JSON="$(fetch_json "$BASE_URL/api/transactions?month=$MONTH")"
  assert_python_json "transazione aggiornata correttamente" "$TX_AFTER_UPDATE_JSON" '
tx_id = "'"$TRANSACTION_ID"'"
tx = next((item for item in data if item.get("id") == tx_id), None)
assert tx is not None
amount = tx.get("amount", {}).get("amount")
assert round(float(amount), 2) == 45.00
assert tx.get("description") == "Spesa aggiornata"
'
fi
echo

echo "[7] Create transfer"
TRANSFER_CREATE_RESPONSE=$(curl -sS -X POST "$BASE_URL/api/transfers" \
  -H "Content-Type: application/json" \
  -d "{\"fromAccountId\":\"$FROM_ACCOUNT_ID\",\"toAccountId\":\"$TO_ACCOUNT_ID\",\"amount\":$TRANSFER_AMOUNT,\"currency\":\"EUR\",\"date\":\"${MONTH}-12\",\"description\":\"Giroconto test\"}" \
  -w "\n%{http_code}")

split_body_status "$TRANSFER_CREATE_RESPONSE"
require_http_status "201"

TRANSFER_ID="$(printf '%s' "$HTTP_BODY" | extract_uuid)"
if ! is_uuid "$TRANSFER_ID"; then
  echo "Errore: transferId non valido ricevuto: $TRANSFER_ID"
  exit 1
fi

echo "TRANSFER_ID=$TRANSFER_ID"
if is_true "$ASSERTS"; then
  TX_AFTER_TRANSFER_JSON="$(fetch_json "$BASE_URL/api/transactions?month=$MONTH")"
  assert_python_json "transfer genera due movimenti collegati" "$TX_AFTER_TRANSFER_JSON" '
transfer_id = "'"$TRANSFER_ID"'"
related = [item for item in data if item.get("transferId") == transfer_id]
assert len(related) == 2
types = sorted(item.get("type") for item in related)
assert types == ["EXPENSE", "INCOME"]
'
fi
echo

echo "[8] Reporting monthly"
REPORT_MONTHLY_JSON="$(fetch_json "$BASE_URL/api/reporting/monthly?month=$MONTH")"
printf '%s\n' "$REPORT_MONTHLY_JSON" | pretty
if is_true "$ASSERTS"; then
  assert_python_json "reporting monthly coerente" "$REPORT_MONTHLY_JSON" '
assert data.get("month") == "'"$MONTH"'"
income = float(data.get("totalIncome", 0))
expense = float(data.get("totalExpense", 0))
savings = float(data.get("savings", 0))
assert round(income - expense, 2) == round(savings, 2)
'
fi
echo

echo "[9] Reporting annual timeline"
REPORT_TIMELINE_JSON="$(fetch_json "$BASE_URL/api/reporting/annual/timeline?year=$YEAR")"
printf '%s\n' "$REPORT_TIMELINE_JSON" | pretty
if is_true "$ASSERTS"; then
  assert_python_json "reporting annual timeline valido" "$REPORT_TIMELINE_JSON" '
assert isinstance(data, list)
assert len(data) == 12
months = [item.get("month", "") for item in data]
assert all(str(m).startswith("'"$YEAR"'-") for m in months)
'
fi
echo

echo "[10] Reporting annual total"
REPORT_ANNUAL_JSON="$(fetch_json "$BASE_URL/api/reporting/annual/total?year=$YEAR")"
printf '%s\n' "$REPORT_ANNUAL_JSON" | pretty
if is_true "$ASSERTS"; then
  assert_python_json "reporting annual total coerente" "$REPORT_ANNUAL_JSON" '
assert int(data.get("year")) == int("'"$YEAR"'")
income = float(data.get("totalIncome", 0))
expense = float(data.get("totalExpense", 0))
result = float(data.get("annualResult", 0))
assert round(income - expense, 2) == round(result, 2)
'
fi
echo

echo "[11] Optional cleanup"
CLEANUP_NORMALIZED="$(printf '%s' "$CLEANUP" | tr '[:upper:]' '[:lower:]')"
if [[ "$CLEANUP_NORMALIZED" == "true" ]]; then
  IDS_TO_DELETE=("$TRANSACTION_ID")

  if command -v python3 >/dev/null 2>&1; then
    TRANSFER_TX_IDS=$(curl -sS "$BASE_URL/api/transactions?month=$MONTH" | python3 -c '
  import json
  import sys

  transfer_id = sys.argv[1]
  for tx in json.load(sys.stdin):
    if tx.get("transferId") == transfer_id and tx.get("id"):
      print(tx["id"])
  ' "$TRANSFER_ID")

    while IFS= read -r tx_id; do
      if [[ -n "$tx_id" ]]; then
        IDS_TO_DELETE+=("$tx_id")
      fi
    done <<< "$TRANSFER_TX_IDS"
  else
    echo "Avviso: python3 non disponibile, cleanup transfer parziale (solo TRANSACTION_ID)."
  fi

  DELETED_IDS="|"
  for tx_id in "${IDS_TO_DELETE[@]}"; do
    if ! is_uuid "$tx_id"; then
      continue
    fi
    if [[ "$DELETED_IDS" == *"|$tx_id|"* ]]; then
      continue
    fi

    DELETE_STATUS=$(curl -sS -X DELETE "$BASE_URL/api/transactions/$tx_id" \
      -o /dev/null -w "%{http_code}")
    if [[ "$DELETE_STATUS" != "204" ]]; then
      echo "Errore delete transaction $tx_id: HTTP $DELETE_STATUS"
      exit 1
    fi

    DELETED_IDS+="$tx_id|"
    echo "Deleted transaction $tx_id (HTTP $DELETE_STATUS)"
  done
else
  echo "Cleanup disattivato: dati lasciati nel DB."
fi
echo

echo "[12] Final list transactions by month"
TX_FINAL_JSON="$(fetch_json "$BASE_URL/api/transactions?month=$MONTH")"
printf '%s\n' "$TX_FINAL_JSON" | pretty
if is_true "$ASSERTS"; then
  if [[ "$CLEANUP_NORMALIZED" == "true" ]]; then
    assert_python_json "cleanup rimuove transazioni create nel run" "$TX_FINAL_JSON" '
forbidden = {"'"$TRANSACTION_ID"'", "'"$TRANSFER_ID"'"}
tx_ids = {item.get("id") for item in data}
transfer_ids = {item.get("transferId") for item in data}
assert "'"$TRANSACTION_ID"'" not in tx_ids
assert "'"$TRANSFER_ID"'" not in transfer_ids
'
  else
    assert_python_json "senza cleanup i dati del run restano nel DB" "$TX_FINAL_JSON" '
tx_ids = {item.get("id") for item in data}
transfer_ids = {item.get("transferId") for item in data}
assert "'"$TRANSACTION_ID"'" in tx_ids
assert "'"$TRANSFER_ID"'" in transfer_ids
'
  fi
fi
echo

echo "Completato."
